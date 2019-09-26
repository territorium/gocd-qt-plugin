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

import com.google.gson.JsonObject;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.Base64;

import cd.go.task.qt.QtPlugin;
import cd.go.task.qt.Util;

public class GetIconRequest {

  public GoPluginApiResponse execute() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("content_type", getContentType());
    jsonObject.addProperty("data", Base64.getEncoder().encodeToString(Util.readResourceBytes(getIcon())));
    return DefaultGoPluginApiResponse.success(QtPlugin.GSON.toJson(jsonObject));
  }

  private String getContentType() {
    return "image/png";
  }

  private String getIcon() {
    return "/plugin-icon.png";
  }
}
