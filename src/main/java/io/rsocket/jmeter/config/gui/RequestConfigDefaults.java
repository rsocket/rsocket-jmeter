/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.rsocket.jmeter.config.gui;

import io.rsocket.jmeter.RSocketSamplerBase;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import org.apache.jmeter.protocol.http.config.gui.UrlConfigGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;

/**
 * Default option value settings for {@link UrlConfigGui}.
 */
public class RequestConfigDefaults implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Available HTTP methods to be shown in the {@link UrlConfigGui}.
   */
  private List<String> validRequestTypesList;

  /**
   * The default HTTP method to be selected in the {@link UrlConfigGui}.
   */
  private String defaultRequestType = RSocketSamplerBase.DEFAULT_REQUEST_TYPE;

  /**
   * Return available HTTP methods to be shown in the {@link UrlConfigGui}, returning {@link
   * HTTPSamplerBase#getValidMethodsAsArray()} by default if not reset.
   *
   * @return available HTTP methods to be shown in the {@link UrlConfigGui}
   */
  public String[] getValidRequestTypes() {
    if (validRequestTypesList != null) {
      return validRequestTypesList.toArray(new String[validRequestTypesList.size()]);
    }
    return RSocketSamplerBase.getValidReqyestTypesAsArray();
  }

  /**
   * Set available HTTP methods to be shown in the {@link UrlConfigGui}.
   *
   * @param validMethods available HTTP methods
   * @throws IllegalArgumentException if the input array is empty
   */
  public void setValidRequestTypesList(String[] validRequestTypes) {
    if (validRequestTypes == null || validRequestTypes.length == 0) {
      throw new IllegalArgumentException("HTTP methods array is empty.");
    }
    this.validRequestTypesList = Arrays.asList(validRequestTypes);
  }

  /**
   * Return the default HTTP method to be selected in the {@link UrlConfigGui}.
   *
   * @return the default HTTP method to be selected in the {@link UrlConfigGui}
   */
  public String getDefaultRequestType() {
    return defaultRequestType;
  }

  /**
   * Set the default HTTP method to be selected in the {@link UrlConfigGui}.
   *
   * @param defaultMethod the default HTTP method to be selected in the {@link UrlConfigGui}
   */
  public void setDefaultRequestType(String defaultRequestType) {
    this.defaultRequestType = defaultRequestType;
  }
}
