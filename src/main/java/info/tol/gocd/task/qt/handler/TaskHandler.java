/*
 * Copyright 2017 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package info.tol.gocd.task.qt.handler;

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.go.plugin.api.task.JobConsoleLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import info.tol.gocd.task.qt.Qt;
import info.tol.gocd.task.qt.Qt2;
import info.tol.gocd.task.qt.QtConfig;
import info.tol.gocd.task.qt.QtInstaller;
import info.tol.gocd.task.qt.QtRepoGen;
import info.tol.gocd.task.util.TaskRequest;
import info.tol.gocd.task.util.TaskResponse;
import info.tol.gocd.util.Environment;
import info.tol.gocd.util.request.RequestHandler;

/**
 * This message is sent by the GoCD agent to the plugin to execute the task.
 *
 * <pre>
 * {
 *   "config": {
 *     "ftp_server": {
 *       "secure": false,
 *       "value": "ftp.example.com",
 *       "required": true
 *     },
 *     "remote_dir": {
 *       "secure": false,
 *       "value": "/pub/",
 *       "required": true
 *     }
 *   },
 *   "context": {
 *     "workingDirectory": "working-dir",
 *     "environmentVariables": {
 *       "ENV1": "VAL1",
 *       "ENV2": "VAL2"
 *     }
 *   }
 * }
 * </pre>
 */
public class TaskHandler implements RequestHandler {


  private enum Build {
    QMAKE,
    MAKE,
    TEST
  }


  private final JobConsoleLogger console;

  /**
   * Constructs an instance of {@link TaskHandler}.
   *
   * @param console
   */
  public TaskHandler(JobConsoleLogger console) {
    this.console = console;
  }


  /**
   * Handles a request and provides a response.
   *
   * @param request
   */
  @Override
  public GoPluginApiResponse handle(GoPluginApiRequest request) {
    TaskRequest task = TaskRequest.of(request);
    QtConfig config = QtConfig.of(task.getConfig());

    this.console.printLine("Launching command on: " + task.getWorkingDirectory());
    this.console.printEnvironment(task.getEnvironment().toMap());

    try {
      switch (config.getBuild()) {
        case "BUILD":
          if (config.getTarget() == null) {
            return TaskResponse.failure("No target defined").toResponse();
          }

          TaskResponse response = process(config, task, console, Build.QMAKE, null);
          if (response.responseCode() == DefaultGoApiResponse.SUCCESS_RESPONSE_CODE) {
            String targets = config.getTarget();
            for (String t : targets.split(",")) {
              response = process(config, task, console, Build.MAKE, t.trim());
              if (response.responseCode() != DefaultGoApiResponse.SUCCESS_RESPONSE_CODE) {
                return response.toResponse();
              }
            }
            if (targets.endsWith(",")) {
              response = process(config, task, console, Build.MAKE, "");
              if (response.responseCode() != DefaultGoApiResponse.SUCCESS_RESPONSE_CODE) {
                return response.toResponse();
              }
            }
          }
          return response.toResponse();

        case "TEST":
          return process(config, task, console, Build.TEST, config.getTarget()).toResponse();

        case "REPOSITORY":
          Process process = createRepogen(task, config.getPackages());
          this.console.readErrorOf(process.getErrorStream());
          this.console.readOutputOf(process.getInputStream());

          int exitCode = process.waitFor();
          process.destroy();
          return (exitCode == 0) ? TaskResponse.success("Executed the build").toResponse()
              : TaskResponse.failure("Could not execute build! Process returned with status code " + exitCode)
                  .toResponse();


        case "ONLINE":
        case "OFFLINE":
        case "INSTALLER":
          process =
              createInstaller(task, config.getTarget(), config.getBuild(), config.getCommand(), config.getPackages());
          this.console.readErrorOf(process.getErrorStream());
          this.console.readOutputOf(process.getInputStream());

          exitCode = process.waitFor();
          process.destroy();
          return (exitCode == 0) ? TaskResponse.success("Executed the build").toResponse()
              : TaskResponse.failure("Could not execute build! Process returned with status code " + exitCode)
                  .toResponse();

        default:
          return TaskResponse.success("Nothing to do").toResponse();
      }
    } catch (Throwable e) {
      if (e.getMessage() == null) {
        Arrays.asList(e.getStackTrace()).forEach(el -> this.console.printLine(el.toString()));
      }
      return TaskResponse.failure(e.getMessage()).toResponse();
    }
  }


  /**
   * Process a single build.
   *
   * @param config
   * @param context
   * @param console
   * @param build
   * @param target
   */
  private TaskResponse process(QtConfig config, TaskRequest request, JobConsoleLogger console, Build build,
      String target) throws IOException, InterruptedException {
    ProcessBuilder builder = createCommand(request, config, build, target);
    builder.directory(new File(request.getWorkingDirectory()));
    builder.environment().putAll(request.getEnvironment().toMap());
    updateEnvironment(builder, request, config);

    console.printLine("Launching command: " + builder.command());
    console.printEnvironment(builder.environment());

    Process process = builder.start();
    console.readErrorOf(process.getErrorStream());
    console.readOutputOf(process.getInputStream());

    int exitCode = process.waitFor();
    process.destroy();
    return (exitCode == 0) ? TaskResponse.success("Executed the build")
        : TaskResponse.failure("Could not execute build! Process returned with status code " + exitCode);
  }


  /**
   * Create the {@link ProcessBuilder} with the command.
   *
   * @param context
   * @param config
   * @param build
   * @param target
   */
  private ProcessBuilder createCommand(TaskRequest request, QtConfig config, Build build, String target) {
    boolean isWindows = Qt2.isWindows();
    List<String> args = new ArrayList<>();
    if (isWindows) {
      args.add("vcvarsall.bat");
      args.add("x86_amd64");
      args.add("&");
    }

    String qtHome = request.getEnvironment().get(Qt2.QT_HOME);
    switch (build) {
      case QMAKE:
        Path path = Paths.get(Qt2.getArch(request, config), "bin", "qmake");

        args.add(new File(qtHome, path.toString()).getAbsolutePath());
        args.add("-spec");
        args.add(Qt2.getSpec(request, config));

        // Adding CONFIG+=
        Qt2.getConfig(request, config).forEach(c -> args.add("CONFIG+=" + c));

        args.add(config.getCommand());
        break;

      case TEST:
        path = Paths.get(Qt2.getArch(request, config), "bin");
        String bin = new File(qtHome, path.toString()).getAbsolutePath();
        if (isWindows) {
          args.add("set PATH=" + bin + ";%PATH%");
          args.add("&");
        } else {
          args.add("LD_LIBRARY_PATH=$LD_LIBRARY_PATH:" + bin);
        }

        String testCase = config.getCommand();
        if ((testCase == null) || testCase.trim().isEmpty()) {
          testCase = config.getTarget();
        }
        if (isWindows) {
          testCase += ".exe";
        }

        String spec = Qt2.getSpec(request, config);
        Path test = Paths.get(request.getWorkingDirectory(), "build", spec, "bin");
        args.add(new File(test.toFile(), testCase).getAbsolutePath());
        args.add("-xunitxml");
        break;

      case MAKE:
        args.add(isWindows ? "jom" : "make");
        if ((target != null) && !target.isEmpty()) {
          args.add(target);
        }
        break;
    }

    String cmd = isWindows ? "cmd" : "sh";
    String cmdArg = isWindows ? "/c" : "-c";
    String argument = String.join(" ", args);
    return new ProcessBuilder(cmd, cmdArg, argument);
  }

  /**
   * Create the {@link ProcessBuilder} with the command.
   *
   * @param builder
   * @param request
   * @param config
   */
  private void updateEnvironment(ProcessBuilder builder, TaskRequest request, QtConfig config) {
    String release = request.getEnvironment().get(Qt2.RELEASE);

    builder.environment().put(Qt2.QT_ARCH, Qt2.getArch(request, config));
    builder.environment().put(Qt2.QT_SPEC, Qt2.getSpec(request, config));

    if (request.getEnvironment().isSet(Qt2.QT_REPOSITORY) && (release != null)) {
      Path repository = Paths.get(request.getEnvironment().get(Qt2.QT_REPOSITORY));
      builder.environment().put(Qt2.QT_REPO, repository.resolve(release).toString());
    }

    String spec = Qt2.getSpec(request, config);
    Path buildPath = new File(request.getWorkingDirectory()).getAbsoluteFile().toPath().resolve("build");
    Path base = buildPath.resolve(spec);

    builder.environment().put(Qt2.QT_BUILD, buildPath.toString());
    builder.environment().put(Qt2.QML2_IMPORT_PATH, base.resolve("qml").toString());
    builder.environment().put(Qt2.QT_PLUGIN_PATH, base.resolve("plugins").toString());
  }


  /**
   * Create an repository generator.
   *
   * @param task
   * @param task
   */
  protected Process createRepogen(TaskRequest task, String packages) throws IOException {
    File workingDir = new File(task.getWorkingDirectory());

    QtRepoGen builder = QtRepoGen.of(workingDir, task.getEnvironment());
    builder.setUpdate();
    // builder.addModules(TaskHandler.toModules(source, task.getWorkingDirectory(),
    // task.getEnvironment()));
    builder.setPackagePath(packages);
    builder.setRepositoryPath(Qt.PATH_REPOSITORY);
    return builder.build();
  }

  /**
   * Create an installer.
   *
   * @param task
   * @param name
   * @param build
   * @param config
   */
  protected Process createInstaller(TaskRequest task, String name, String build, String config, String packages)
      throws IOException {
    File workingDir = new File(task.getWorkingDirectory());

    QtInstaller builder = QtInstaller.of(workingDir, task.getEnvironment());
    builder.setName(name).setMode(build);
    builder.setConfig(config);
    // builder.addModules(TaskHandler.toModules(source, task.getWorkingDirectory(),
    // task.getEnvironment()));
    builder.setPackagePath(packages);

    builder.log(this.console);

    return builder.build();
  }

  /**
   * Converts the modules with it's dependencies
   *
   * @param task
   * @param packages
   * @param workingDir
   * @param environment
   */
  protected static List<String> toModules(String text, String packages, String workingDir, Environment environment) {
    List<String> modules = new ArrayList<>();
    if (text != null) {
      for (String name : Arrays.asList(text.split("(,|\\s|\\n|\\r\\n)"))) {
        if (!name.trim().isEmpty()) {
          modules.add(name);
        }
      }

      for (File file : new File(workingDir, packages).listFiles()) {
        for (String module : new ArrayList<>(modules)) {
          if (!modules.contains(file.getName()) && module.startsWith(file.getName())) {
            modules.add(file.getName());
          }
        }
      }
    }
    return modules;
  }
}
