/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * License from repo org/springframework/spring-test/5.1.2.RELEASE
 * org/springframework/test/web/servlet/DefaultMvcResult
 * Code taken and adapted to work with EvoSuite
 */

package org.evosuite.spring;

public class SmockDefaultMvcResult implements SmockMvcResult {

  private final SmockRequest mockRequest;
  private final SmockResponse mockResponse;

  public SmockDefaultMvcResult(SmockRequest request, SmockResponse response) {
    this.mockRequest = request;
    this.mockResponse = response;
  }

  @Override
  public SmockRequest getRequest() {
    return this.mockRequest;
  }

  @Override
  public SmockResponse getResponse() {
    return this.mockResponse;
  }
}
