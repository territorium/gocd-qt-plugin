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

package cd.go.task.qt;

import java.util.Map;

// TODO: edit this to map to the fields in your task configuration
public class Config {

  public static final String REQUEST_BUILD   = "Build";
  public static final String REQUEST_TARGET  = "Target";
  public static final String REQUEST_COMMAND = "Command";


  private final String build;
  private final String target;
  private final String command;


  public Config(Map<String, Map<String, String>> config) {
    this.build = getValue(config, Config.REQUEST_BUILD);
    this.target = getValue(config, Config.REQUEST_TARGET);
    this.command = getValue(config, Config.REQUEST_COMMAND);
  }

  private String getValue(Map<String, Map<String, String>> config, String property) {
    return config.get(property).get("value");
  }

  public String getBuild() {
    return build;
  }

  public String getTarget() {
    return target;
  }

  public String getCommand() {
    return command;
  }
}
