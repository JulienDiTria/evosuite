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
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.variable.VariableReferenceImpl;
import org.evosuite.utils.generic.GenericConstructor;
import org.evosuite.utils.generic.GenericMethod;
import org.springframework.http.HttpMethod;

public class SmockMvc {

  public SmockMvc(){
    // empty constructor
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

  /**
   * Helper method to create a SmockMvc object in EvoSuite test cases.
   *
   * SmockMVC smockMvc = new SmockMvc();
   *
   * @param tc the test case to add the SmockMvc object to
   * @return the variable reference to the SmockMvc object
   */
  public static VariableReference createSmockMvc(TestCase tc){
    // get the constructor by reflection
    Constructor<?> constructor = null;
    try {
      constructor = SmockMvc.class.getConstructor();
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }

    // create the constructor statement
    GenericConstructor genericConstructor = new GenericConstructor(constructor, SmockMvc.class);
    ConstructorStatement statement = new ConstructorStatement(tc, genericConstructor, Collections.emptyList());

    // add the statement to the test case
    VariableReference smockMvc = tc.addStatement(statement);
    return smockMvc;
  }

  /**
   * Helper method to perform a request contained by a request builder and return the ResultActions wrapping around the result.
   *
   * ResultActions resultActions = smockMvc.perform(requestBuilder);
   *
   * @param tc the test case in which the request is performed
   * @param smockMvc the SmockMvc object to perform the request
   * @param requestBuilder the request builder that contains the request to be performed
   * @return the result actions wrapping around the result
   */
  public static VariableReference perform(TestCase tc, VariableReference smockMvc, VariableReference requestBuilder){
    // get the "perform" method of the smockMvc by reflection
    Method method = null;
    try {
      method = SmockMvc.class.getMethod("perform", SmockRequestBuilder.class);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }

    // create the method statement parameters
    GenericMethod genericMethod = new GenericMethod(method, SmockMvc.class);
    VariableReference retVal = new VariableReferenceImpl(tc, genericMethod.getReturnType());

    // TODO 31.01.2024 Julien Di Tria
    //  This MethodStatement should be extended into a new class and replaced in order to DSE the perform method
    //  (specifically the request.execute() method) to run as Spring would do instead of concrete execution
    MethodStatement statement = new MethodStatement(tc, genericMethod, smockMvc, List.of(requestBuilder), retVal);

    // add the statement to the test case
    VariableReference resultActions = tc.addStatement(statement);
    return resultActions;
  }
}
