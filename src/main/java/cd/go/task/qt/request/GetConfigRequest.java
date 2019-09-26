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

package cd.go.task.qt.request;

import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.HashMap;
import java.util.LinkedHashMap;

import cd.go.task.qt.Config;
import cd.go.task.qt.QtPlugin;

// TODO: change this to allow configuration options in your configuration
public class GetConfigRequest {

  public GoPluginApiResponse execute() {
    HashMap<String, Object> config = new LinkedHashMap<>();

    HashMap<String, Object> requestBuild = new HashMap<>();
    requestBuild.put("default-value", "BUILD");
    requestBuild.put("display-order", "2");
    requestBuild.put("display-name", "Build");
    requestBuild.put("required", true);
    config.put(Config.REQUEST_BUILD, requestBuild);

    HashMap<String, Object> requestCommand = new HashMap<>();
    requestCommand.put("display-order", "1");
    requestCommand.put("display-name", "Command:");
    requestCommand.put("required", false);
    config.put(Config.REQUEST_COMMAND, requestCommand);

    HashMap<String, Object> requestTarget = new HashMap<>();
    requestTarget.put("display-order", "1");
    requestTarget.put("display-name", "Target:");
    requestTarget.put("required", false);
    config.put(Config.REQUEST_TARGET, requestTarget);

    return DefaultGoPluginApiResponse.success(QtPlugin.GSON.toJson(config));
  }
}
