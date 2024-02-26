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

import com.sun.tools.javac.util.List;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.variable.VariableReferenceImpl;
import org.evosuite.utils.generic.GenericConstructor;
import org.evosuite.utils.generic.GenericMethod;
import org.mockito.Mock;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

public class SmockMvc {

  public static MockMvc defaultMockMvc() {
    return MockMvcBuilders.standaloneSetup().build();
  }

  /*
   * ***********************
   * EVOSUITE helpers
   * ***********************
   */

  /**
   * Helper method to create a MockMvc object in EvoSuite test cases.
   *
   * MockMvc mockMvc = SmockMvc.defaultMockMvc();
   *
   * @param tc the test case to add the SmockMvc object to
   * @return the variable reference to the SmockMvc object
   */
  public static VariableReference createMockMvc(TestCase tc, int position) throws ConstructionFailedException {
    // get the constructor by reflection
    Method method;
    try {
      method = SmockMvc.class.getMethod("defaultMockMvc");
    } catch (NoSuchMethodException e) {
      throw new ConstructionFailedException(e.getClass().getName() +" : " + e.getMessage());
    }

    // create the constructor statement
    GenericMethod genericMethod = new GenericMethod(method, MockMvc.class);
    MethodStatement statement = new MethodStatement(tc, genericMethod, null, Collections.emptyList());

    // add the statement to the test case
    return tc.addStatement(statement, position);
  }

  /**
   * Helper method to perform a request contained by a request builder and return the ResultActions wrapping around the result.
   *
   * ResultActions resultActions = mockMvc.perform(requestBuilder);
   *
   * @param tc the test case in which the request is performed
   * @param mockMvc the SmockMvc object to perform the request
   * @param requestBuilder the request builder that contains the request to be performed
   * @return the result actions wrapping around the result
   */
  public static VariableReference mockPerform(TestCase tc, int position, VariableReference mockMvc, VariableReference requestBuilder)
      throws ConstructionFailedException {
    // get the "perform" method of the smockMvc by reflection
    Method method = null;
    try {
      method = MockMvc.class.getMethod("perform", RequestBuilder.class);
    } catch (NoSuchMethodException e) {
      throw new ConstructionFailedException(e.getClass().getName() +" : " + e.getMessage());
    }

    // create the method statement parameters
    GenericMethod genericMethod = new GenericMethod(method, MockMvc.class);
    VariableReference retVal = new VariableReferenceImpl(tc, genericMethod.getReturnType());
    MethodStatement statement = new MethodStatement(tc, genericMethod, mockMvc, List.of(requestBuilder), retVal);

    // add the statement to the test case
      return tc.addStatement(statement, position);
  }
}
