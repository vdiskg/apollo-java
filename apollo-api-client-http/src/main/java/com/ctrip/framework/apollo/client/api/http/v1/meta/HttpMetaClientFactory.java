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
package com.ctrip.framework.apollo.client.api.http.v1.meta;

import com.ctrip.framework.apollo.client.api.v1.meta.MetaClient;
import com.ctrip.framework.apollo.core.http.HttpTransport;
import com.ctrip.framework.apollo.core.http.HttpTransportFactory;
import com.ctrip.framework.apollo.core.http.HttpTransportProperties;
import com.ctrip.framework.foundation.internals.ServiceBootstrap;
import java.util.Objects;

public class HttpMetaClientFactory {

  private HttpMetaClientFactory() {
    throw new UnsupportedOperationException();
  }

  public static MetaClient createClient(HttpMetaClientProperties properties) {

    HttpTransportFactory transportFactory = ServiceBootstrap.loadPrimary(
        HttpTransportFactory.class);

    HttpTransportProperties getServicesProperties = HttpTransportProperties.builder()
        .defaultConnectTimeout(properties.getDiscoveryConnectTimeout())
        .defaultReadTimeout(properties.getDiscoveryReadTimeout())
        .build();

    HttpTransport getServicesTransport = transportFactory.create(getServicesProperties);
    Objects.requireNonNull(getServicesTransport, "getServicesTransport");
    return new HttpMetaClient(getServicesTransport);
  }
}
