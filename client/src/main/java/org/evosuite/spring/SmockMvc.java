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
 * License from repo org/springframework/spring-test/5.1.2.RELEASE/
 * org/springframework/test/web/servlet/MockMvc.java
 * Code taken and adapted to work with EvoSuite
 */

package org.evosuite.spring;

public class SmockMvc {

  private final RequestMappingHandlerMapping requestMappingHandlerMapping;

  public SmockMvc(){
    requestMappingHandlerMapping = new RequestMappingHandlerMapping();
  }

  /**
   * Process the candidate controller which consists of
   * - registering the handler methods.
   *
   * @param controller the candidate controller
   */
  public void processCandidateController(Object controller) {
    requestMappingHandlerMapping.processCandidateController(controller);
  }

  /**
   * Perform the request contained by the request builder and return the ResultActions wrapping around the result.
   *
   * @param requestBuilder the request builder that contains the request to be performed
   * @return the result actions wrapping around the result
   * @throws Exception
   */
  public SmockResultActions perform(SmockRequestBuilder requestBuilder) throws Exception {

    // build the request
    SmockRequest request = requestBuilder.buildRequest();

    // execute the request
    SmockResponse response = request.execute();

    // put the response into a mvc result
    final SmockMvcResult mvcResult = new SmockDefaultMvcResult(request, response);

    // wrap around the mvc result
    return new SmockResultActions(mvcResult);
  }

}
