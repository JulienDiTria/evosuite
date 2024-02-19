package org.evosuite.spring;

import java.lang.invoke.MethodHandles;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.variable.VariableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    TestCase tc = new DefaultTestCase();
    VariableReference requestBuilder = addRequestBuilder(tc, requestMappingInfo);
    logger.debug("{}", requestBuilder);
    VariableReference mvcResult = performAndGetResult(tc, requestBuilder);
    return tc;
  }

  public static VariableReference addRequestBuilder(TestCase tc, RequestMappingInfo requestMappingInfo) {
    logger.debug("addRequestBuilder");
    VariableReference requestBuilder = SmockRequestBuilder.createRequestBuilder(tc, requestMappingInfo);
    SmockRequestBuilder.addParamsToRequestBuilder(tc, requestBuilder, requestMappingInfo);
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
