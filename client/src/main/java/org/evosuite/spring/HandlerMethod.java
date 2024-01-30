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
 * License from repo org/springframework/spring-web/5.1.2.RELEASE
 * org/springframework/web/method/HandlerMethod
 * Code taken and adapted to work with EvoSuite
 */

package org.evosuite.spring;

import java.lang.reflect.Method;

public class HandlerMethod {
  Class<?> handler;
  Method method;

  public HandlerMethod(Class<?> handler, Method method) {
    this.handler = handler;
    this.method = method;
  }

  public Method getMethod() {
    return method;
  }

  public Class<?> getHandler() {
      return handler;
  }

}
