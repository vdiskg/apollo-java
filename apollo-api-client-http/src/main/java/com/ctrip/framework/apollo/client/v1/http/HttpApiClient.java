/*
 * Copyright 2022 Apollo Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.ctrip.framework.apollo.client.v1.http;

import com.ctrip.framework.apollo.client.v1.api.ApiClient;
import com.ctrip.framework.apollo.client.v1.api.config.ConfigClient;
import com.ctrip.framework.apollo.client.v1.api.meta.MetaClient;
import com.ctrip.framework.apollo.client.v1.http.config.HttpConfigClient;
import com.ctrip.framework.apollo.client.v1.http.meta.HttpMetaClient;
import java.util.Objects;

public class HttpApiClient implements ApiClient {

  private final HttpMetaClient metaClient;

  private final HttpConfigClient configClient;

  public HttpApiClient(HttpMetaClient metaClient, HttpConfigClient configClient) {
    Objects.requireNonNull(metaClient, "metaClient");
    Objects.requireNonNull(configClient, "configClient");

    this.metaClient = metaClient;
    this.configClient = configClient;
  }

  @Override
  public MetaClient metaClient() {
    return this.metaClient;
  }

  @Override
  public ConfigClient configClient() {
    return this.configClient;
  }
}
