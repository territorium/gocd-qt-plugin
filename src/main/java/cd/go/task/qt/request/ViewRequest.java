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

import com.thoughtworks.go.plugin.api.response.DefaultGoApiResponse;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.HashMap;

import cd.go.task.qt.QtPlugin;
import cd.go.task.qt.Util;

public class ViewRequest {

  public static GoPluginApiResponse of(String display, String template) {
    HashMap<String, String> view = new HashMap<>();
    try {
      view.put("displayValue", display);
      view.put("template", Util.readResource(template));
      return new DefaultGoPluginApiResponse(DefaultGoApiResponse.SUCCESS_RESPONSE_CODE, QtPlugin.GSON.toJson(view));
    } catch (Exception e) {
      String errorMessage = "Failed to find template: " + e.getMessage();
      view.put("exception", errorMessage);
      QtPlugin.LOGGER.error(errorMessage, e);
    }
    return new DefaultGoPluginApiResponse(DefaultGoApiResponse.INTERNAL_ERROR, QtPlugin.GSON.toJson(view));
  }
}
