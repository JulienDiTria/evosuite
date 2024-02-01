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

  /**
   * Creates a test case for the given request mapping info.
   * The test case contains a request builder that is created using the SmockRequestBuilder class.
   * The request builder is then used to perform the request and the result is returned.
   * The test case doesn't contain any assertions at this stage.
   *
   * @param requestMappingInfo the request mapping info provided by the Spring framework corresponding to that request
   * @return the test case containing the request builder and the result
   */
  public static TestCase createTestCaseForRequestMapping(RequestMappingInfo requestMappingInfo) {
    logger.warn("createTestCaseForRequestMapping");
    TestCase tc = new DefaultTestCase();
    VariableReference requestBuilder = addRequestBuilder(tc, requestMappingInfo);
    logger.debug("{}", requestBuilder);
    VariableReference mvcResult = performAndGetResult(tc, requestBuilder);
    return tc;
  }

  private static VariableReference addRequestBuilder(TestCase tc, RequestMappingInfo requestMappingInfo) {
    logger.debug("addRequestBuilder");
    VariableReference requestBuilder = SmockRequestBuilder.createRequestBuilder(tc, requestMappingInfo);
    requestBuilder = SmockRequestBuilder.addParamsToRequestBuilder(tc, requestBuilder, requestMappingInfo);
    return requestBuilder;
  }

  /**
   * Creates a request builder for the given request mapping info using the MockMvcRequestBuilders class.
   *
   * @param tc the test case to add the request builder to
   * @param requestMappingInfo the request mapping info provided by the Spring framework corresponding to that request
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


  /**
   * Performs the request contained by the request builder and return the ResultActions wrapping around the result.
   *
   * SmockMvc smockMvc = New SmockMvc();
   * ResultActions resultActions = smockMvc.perform(requestBuilder);
   * MvcResult mvcResult = resultActions.andReturn();
   *
   * @param tc the test case in which the request is performed
   * @param requestBuilder the request builder that contains the request to be performed
   */
  private static VariableReference performAndGetResult(TestCase tc, VariableReference requestBuilder) {
    logger.debug("performAndGetResult");

    // create the smockMVC object
    VariableReference smockMvc = SmockMvc.createSmockMvc(tc);

    // call perform and get the result actions
    VariableReference resultActions = SmockMvc.perform(tc, smockMvc, requestBuilder);

    // return the mvcResult from the result actions
    VariableReference mvcResult = SmockResultActions.andReturn(tc, resultActions);
    return mvcResult;
  }

}
