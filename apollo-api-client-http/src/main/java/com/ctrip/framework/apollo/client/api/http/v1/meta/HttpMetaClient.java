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

import com.ctrip.framework.apollo.client.api.http.v1.util.InternalCollectionUtil;
import com.ctrip.framework.apollo.client.api.http.v1.util.InternalHttpUtil;
import com.ctrip.framework.apollo.client.api.v1.Endpoint;
import com.ctrip.framework.apollo.client.api.v1.meta.ConfigServiceInstance;
import com.ctrip.framework.apollo.client.api.v1.meta.DiscoveryRequest;
import com.ctrip.framework.apollo.client.api.v1.meta.MetaClient;
import com.ctrip.framework.apollo.client.api.v1.meta.MetaException;
import com.ctrip.framework.apollo.core.dto.ServiceDTO;
import com.ctrip.framework.apollo.core.http.HttpTransport;
import com.ctrip.framework.apollo.core.http.HttpTransportException;
import com.ctrip.framework.apollo.core.http.HttpTransportRequest;
import com.ctrip.framework.apollo.core.http.HttpTransportResponse;
import com.ctrip.framework.apollo.core.http.HttpTransportStatusCodeException;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HttpMetaClient implements MetaClient {

  private static final Type GET_SERVICES_RESPONSE_TYPE = new TypeToken<List<ServiceDTO>>() {
  }.getType();

  private final HttpTransport httpTransport;

  private final HttpMetaClientProperties properties;

  public HttpMetaClient(HttpTransport httpTransport, HttpMetaClientProperties properties) {
    Objects.requireNonNull(httpTransport, "httpTransport");
    Objects.requireNonNull(properties, "properties");
    this.httpTransport = httpTransport;
    this.properties = properties;
  }

  @Override
  public String traceGetServices(Endpoint endpoint, DiscoveryRequest request) {
    return this.toGetServicesUri(endpoint, request);
  }

  private String toGetServicesUri(Endpoint endpoint, DiscoveryRequest request) {
    Map<String, String> queryParams = Maps.newHashMap();
    queryParams.put("appId", request.getAppId());
    String clientIp = request.getClientIp();
    if (!Strings.isNullOrEmpty(clientIp)) {
      queryParams.put("ip", clientIp);
    }

    String actualAddress = InternalHttpUtil.getActualAddress(endpoint);

    String query = InternalHttpUtil.toQueryString(queryParams);

    return MessageFormat.format("{0}/services/config{1}", actualAddress, query);
  }

  @Override
  public List<ConfigServiceInstance> getServices(Endpoint endpoint, DiscoveryRequest request) {
    HttpTransportRequest httpTransportRequest = this.toGetServicesHttpRequest(endpoint, request);
    HttpTransportResponse<List<ServiceDTO>> httpTransportResponse = this.doGetInternal(
        "Get config services",
        () -> this.httpTransport.doGet(httpTransportRequest, GET_SERVICES_RESPONSE_TYPE));
    List<ServiceDTO> serviceDTOList = httpTransportResponse.getBody();
    if (InternalCollectionUtil.isEmpty(serviceDTOList)) {
      return Collections.emptyList();
    }
    List<ConfigServiceInstance> configServiceInstanceList = new ArrayList<>(serviceDTOList.size());
    for (ServiceDTO serviceDTO : serviceDTOList) {
      ConfigServiceInstance configServiceInstance = ConfigServiceInstance.builder()
          .serviceId(serviceDTO.getAppName()).instanceId(serviceDTO.getInstanceId())
          .address(serviceDTO.getHomepageUrl()).build();
      configServiceInstanceList.add(configServiceInstance);
    }
    return Collections.unmodifiableList(configServiceInstanceList);
  }

  private HttpTransportRequest toGetServicesHttpRequest(Endpoint endpoint,
      DiscoveryRequest request) {
    String uri = this.toGetServicesUri(endpoint, request);
    return HttpTransportRequest.builder().url(uri)
        .connectTimeout(this.properties.getDiscoveryConnectTimeout())
        .readTimeout(this.properties.getDiscoveryReadTimeout()).build();
  }

  private <T> HttpTransportResponse<T> doGetInternal(String scene,
      Supplier<HttpTransportResponse<T>> action) {
    HttpTransportResponse<T> httpTransportResponse;
    try {
      httpTransportResponse = action.get();
    } catch (HttpTransportStatusCodeException e) {
      throw new MetaException(
          MessageFormat.format("{0} failed. Http status code: {1}", scene, e.getStatusCode()), e);
    } catch (HttpTransportException e) {
      throw new MetaException(MessageFormat.format("{0} failed. Http error: {1}", scene,
          e.getLocalizedMessage()), e);
    } catch (Throwable e) {
      throw new MetaException(
          MessageFormat.format("{0} failed. Error: {1}", scene, e.getLocalizedMessage()),
          e);
    }
    return httpTransportResponse;
  }
}
