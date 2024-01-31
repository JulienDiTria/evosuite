package org.evosuite.spring;

import com.sun.tools.javac.util.List;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.statements.ArrayStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.variable.ArrayReference;
import org.evosuite.testcase.variable.ConstantValue;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.variable.VariableReferenceImpl;
import org.evosuite.utils.generic.GenericClassFactory;
import org.evosuite.utils.generic.GenericMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.mvc.condition.NameValueExpression;
import org.springframework.web.servlet.mvc.condition.ParamsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

public class SpringTestFactory {

  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static TestCase createTestCaseForRequestMapping(RequestMappingInfo requestMappingInfo) {
    logger.warn("createTestCaseForRequestMapping");
    TestCase tc = new DefaultTestCase();
    VariableReference requestBuilder = addRequestBuilder(tc, requestMappingInfo);
    logger.debug("{}", requestBuilder);
    VariableReference mvcResult = performAndGetResult(tc, requestBuilder);
    addAsserts(tc);
    return tc;
  }

  private static VariableReference addRequestBuilder(TestCase tc, RequestMappingInfo requestMappingInfo) {
    logger.debug("addRequestBuilder");
    VariableReference requestBuilder = createRequestBuilder(tc, requestMappingInfo);
    requestBuilder = addParamsToRequestBuilder(tc, requestBuilder, requestMappingInfo);
    return requestBuilder;
  }

  /**
   * Creates a request builder for the given request mapping info using the SmockRequestBuilder class.
   * @param tc
   * @param requestMappingInfo
   * @return
   */
  private static VariableReference createRequestBuilder(TestCase tc, RequestMappingInfo requestMappingInfo) {
    return SmockRequestBuilder.createRequestBuilder(tc, requestMappingInfo);
  }

  /**
   * Creates a request builder for the given request mapping info using the MockMvcRequestBuilders class.
   * @param tc the test case to add the request builder to
   * @param requestMappingInfo the request mapping info to create the request builder for
   * @return the request builder reference added to the testcase
   */
  private static VariableReference createRequestBuilderWithMockMvcRequestBuilders(TestCase tc, RequestMappingInfo requestMappingInfo) {
    logger.debug("createRequestBuilder");
    // get the url template as the first pattern into a string constant
    String urlValue = requestMappingInfo.getPatternsCondition().getPatterns().iterator().next();
    ConstantValue urlTemplate = new ConstantValue(tc, GenericClassFactory.get(String.class), urlValue);

    // set empty array for the other parameters of the get method
    ArrayReference tmpRef = new ArrayReference(tc, Object[].class);
    VariableReference emptyArrayRef = tc.addStatement(new ArrayStatement(tc, tmpRef, new int[]{0}));

    // create the request builder
    Method method = null;
    try {
      method = MockMvcRequestBuilders.class.getMethod("get", String.class, Object[].class);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
    GenericMethod genericMethod = new GenericMethod(method, MockMvcRequestBuilders.class);
    VariableReference retVal = new VariableReferenceImpl(tc, method.getReturnType());
    MethodStatement statement = new MethodStatement(tc, genericMethod, null, List.of(urlTemplate, emptyArrayRef), retVal);

    // add the statement to the test case
    VariableReference requestBuilder = tc.addStatement(statement);
    return requestBuilder;
  }

  private static VariableReference addParamsToRequestBuilder(TestCase tc, VariableReference requestBuilder,
      RequestMappingInfo requestMappingInfo) {
    logger.debug("addParamsToRequestBuilder");

    // get the params as a list of string constants
    ParamsRequestCondition paramsRequestCondition = requestMappingInfo.getParamsCondition();
    Set<NameValueExpression<String>> expressions = paramsRequestCondition.getExpressions();
    VariableReference ref = expressions.stream().map(param ->
        addParamToRequestBuilder(tc, requestBuilder, param)
    ).reduce((a, b) -> b).orElse(requestBuilder);
    logger.debug("{}", ref);
    return ref;
  }

  private static VariableReference addParamToRequestBuilder(TestCase tc, VariableReference requestBuilder, NameValueExpression param) {
    logger.debug("addParamToRequestBuilder");

    // TODO 31.01.2024 Julien Di Tria
    //  param value (and maybe name) can be GA variable to be mutated

    // get the param name and value as string constants
    ConstantValue paramName = new ConstantValue(tc, GenericClassFactory.get(String.class), param.getName());
    ConstantValue paramValue = new ConstantValue(tc, GenericClassFactory.get(String.class), param.getValue());

    // call the param method on the request builder
    Method method;
    try {
      method = MockHttpServletRequestBuilder.class.getMethod("param", String.class, String[].class);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
    GenericMethod genericMethod = new GenericMethod(method, MockHttpServletRequestBuilder.class);
    VariableReference retVal = new VariableReferenceImpl(tc, genericMethod.getReturnType());
    MethodStatement statement = new MethodStatement(tc, genericMethod, requestBuilder, List.of(paramName, paramValue), retVal);
    requestBuilder = tc.addStatement(statement);
    return requestBuilder;
  }

  /**
   * SmockMvc mockMvc = New SmockMvc();
   * ResultActions resultActions = mockMvc.perform(requestBuilder);
   * @param tc
   */
  private static VariableReference performAndGetResult(TestCase tc, VariableReference requestBuilder) {
    logger.debug("addMockPerform");

    // create the mock perform method
    VariableReference smockMvc = SmockMvc.createSmockMvc(tc);

    // call "perform" method on the smockMvc object
    Method method = null;
    try {
      method = SmockMvc.class.getMethod("perform", SmockRequestBuilder.class);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
    GenericMethod genericMethod = new GenericMethod(method, SmockMvc.class);
    VariableReference retVal = new VariableReferenceImpl(tc, genericMethod.getReturnType());

    // TODO 31.01.2024 Julien Di Tria
    //  This MethodStatement should be extended into a new class and replaced in order to DSE the perform method
    //  (specifically the request.execute() method) to run as Spring would do instead of concrete execution
    MethodStatement statement = new MethodStatement(tc, genericMethod, smockMvc, List.of(requestBuilder), retVal);
    VariableReference resultActions = tc.addStatement(statement);

    // return the result actions from
    VariableReference mvcResult = getResult(tc, resultActions);
    return mvcResult;
  }

  private static VariableReference getResult(TestCase tc, VariableReference resultActions) {
    logger.debug("getResult");

    // call "andReturn" method on the resultActions object
    Method method = null;
    try {
      method = ResultActions.class.getMethod("andReturn");
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
    GenericMethod genericMethod = new GenericMethod(method, ResultActions.class);
    VariableReference retVal = new VariableReferenceImpl(tc, genericMethod.getReturnType());
    MethodStatement statement = new MethodStatement(tc, genericMethod, resultActions, Collections.emptyList(), retVal);
    VariableReference mvcResult = tc.addStatement(statement);
    return mvcResult;
  }

  private static void addAsserts(TestCase tc) {
    logger.debug("addAsserts");
  }

}
