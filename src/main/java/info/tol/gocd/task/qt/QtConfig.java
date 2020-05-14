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

package info.tol.gocd.task.qt;

import info.tol.gocd.task.util.TaskConfig;

// TODO: edit this to map to the fields in your task configuration
public class QtConfig {

  private final TaskConfig config;


  public QtConfig(TaskConfig config) {
    this.config = config;
  }

  public String getBuild() {
    return config.getValue(Qt2Config.BUILD);
  }

  public String getTarget() {
    return config.getValue(Qt2Config.TARGET);
  }

  public String getCommand() {
    return config.getValue(Qt2Config.COMMAND);
  }

  public String getPackages() {
    return config.getValue(Qt2Config.PACKAGES);
  }

  public static QtConfig of(TaskConfig config) {
    return new QtConfig(config);
  }
}
