/*
 * Copyright 2002-2017 the original author or authors.
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
 * org/springframework/test/web/servlet/RequestBuilder.java
 * Code taken and adapted to work with EvoSuite
 */

package org.evosuite.spring;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class SmockRequestBuilder {

//      private static HTTP

      private final String method;

      private final URI url;

      private final MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

      public SmockRequestBuilder(HttpMethod httpMethod, String url) throws URISyntaxException {
            this(httpMethod.name(), new URI(url));
      }

      private SmockRequestBuilder(String method, URI url) {
            Assert.notNull(method, "'httpMethod' is required");
            Assert.notNull(url, "'url' is required");
            this.method = method;
            this.url = url;
      }

      SmockRequest buildRequest() throws Exception {
            SmockRequest request = new SmockRequest();
//            request.setMethod(this.method);
//            request.setUrl(this.url);
//            request.setParameters(this.parameters);
//            request.();
            return request;
      }

      public SmockRequestBuilder param(String name, String... values) {
            addToMultiValueMap(this.parameters, name, values);
            return this;
      }

      private static <T> void addToMultiValueMap(MultiValueMap<String, T> map, String name, T[] values) {
            Assert.hasLength(name, "'name' must not be empty");
            Assert.notEmpty(values, "'values' must not be empty");
            for (T value : values) {
                  map.add(name, value);
            }
      }
}
