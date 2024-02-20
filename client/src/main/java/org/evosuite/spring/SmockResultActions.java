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
 * org/springframework/test/web/servlet/ResultActions.java
 * Code taken and adapted to work with EvoSuite
 */

package org.evosuite.spring;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Collections;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.variable.VariableReferenceImpl;
import org.evosuite.utils.generic.GenericMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.web.servlet.ResultActions;

public class SmockResultActions {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  SmockMvcResult mvcResult;

  public SmockResultActions(SmockMvcResult mvcResult) {
    this.mvcResult = mvcResult;
  }

  public SmockResultActions andExpect(SmockResultMatcher matcher) throws Exception {
    matcher.match(mvcResult);
    return this;
  }

  public SmockResultActions andDo(SmockResultHandler handler) throws Exception {
    handler.handle(mvcResult);
    return this;
  }

  public SmockMvcResult andReturn() {
    return mvcResult;
  }

  /**
   * Add a statement to the test case that calls the "andReturn" method on the result actions.
   *
   * @param tc the test case on which to add the statement
   * @param resultActions the result actions on which to call the "andReturn" method
   * @return the variable reference to the SmockMvcResult returned by the "andReturn" method
   */
  public static VariableReference smockAndReturn(TestCase tc, VariableReference resultActions){
    // get the "andReturn" method by reflection
    Method method;
    try {
      method = SmockResultActions.class.getMethod("andReturn");
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }

    // create the method statement
    GenericMethod genericMethod = new GenericMethod(method, SmockResultActions.class);
    VariableReference retVal = new VariableReferenceImpl(tc, genericMethod.getReturnType());
    MethodStatement statement = new MethodStatement(tc, genericMethod, resultActions, Collections.emptyList(), retVal);

    // add the statement to the test case
    VariableReference mvcResult = tc.addStatement(statement);
    return mvcResult;
  }

  /**
   * Add a statement to the test case that calls the "andReturn" method on the result actions.
   *
   * @param tc the test case on which to add the statement
   * @param resultActions the result actions on which to call the "andReturn" method
   * @return the variable reference to the SmockMvcResult returned by the "andReturn" method
   */
  public static VariableReference andReturn(TestCase tc, VariableReference resultActions){
    // get the "andReturn" method by reflection
    Method method;
    try {
      method = ResultActions.class.getMethod("andReturn");
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }

    // create the method statement
    GenericMethod genericMethod = new GenericMethod(method, ResultActions.class);
    VariableReference retVal = new VariableReferenceImpl(tc, genericMethod.getReturnType());
    MethodStatement statement = new MethodStatement(tc, genericMethod, resultActions, Collections.emptyList(), retVal);

    // add the statement to the test case
    VariableReference mvcResult = tc.addStatement(statement);
    return mvcResult;
  }
}
