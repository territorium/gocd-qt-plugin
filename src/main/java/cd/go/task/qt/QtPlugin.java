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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.Arrays;

import cd.go.task.qt.request.ExecuteRequest;
import cd.go.task.qt.request.ConfigRequest;
import cd.go.task.qt.request.IconRequest;
import cd.go.task.qt.request.ViewRequest;
import cd.go.task.qt.request.Validation;

@Extension
public class QtPlugin implements GoPlugin {

  public static final Gson   GSON   = new GsonBuilder().serializeNulls().create();
  public static final Logger LOGGER = Logger.getLoggerFor(QtPlugin.class);

  @Override
  public GoPluginIdentifier pluginIdentifier() {
    return new GoPluginIdentifier("task", Arrays.asList("1.0"));
  }

  @Override
  public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {}

  @Override
  public GoPluginApiResponse handle(GoPluginApiRequest request) throws UnhandledRequestTypeException {
    switch (request.requestName()) {
      case "icon":
        return IconRequest.of("image/png", "/plugin-icon.png");
      case "view":
        return ViewRequest.of("Qt Task", "/task.template.html");
      case "configuration":
        return ConfigRequest.of();
      case "validate":
        return Validation.of(request);
      case "execute":
        return ExecuteRequest.execute(request, new QtTaskExecutor());
      default:
        throw new UnhandledRequestTypeException(request.requestName());
    }
  }
}
