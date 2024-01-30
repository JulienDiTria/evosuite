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
 * License from repo org/springframework/spring-webmvc/5.1.2.RELEASE
 * org/springframework/web/servlet/handler/AbstractHandlerMethodMapping.MappingRegistry
 * Code taken and adapted to work with EvoSuite
 */

package org.evosuite.spring;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MappingRegistry<T> {

  private final Map<T, HandlerMethod> mappingLookup = new LinkedHashMap<>();

  public void register(T mapping, Class<?> handler, Method method) {
    this.mappingLookup.put(mapping, new HandlerMethod(handler, method));
  }

  @Override
  public String toString() {
      return this.mappingLookup.entrySet().stream()
          .map(entry -> entry.getKey().toString() + " -> " + entry.getValue().getMethod().toString())
          .collect(Collectors.joining(",\n ", "[", "]"));
  }
}
