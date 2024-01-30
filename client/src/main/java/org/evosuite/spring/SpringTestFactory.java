package org.evosuite.spring;

import com.sun.tools.javac.util.List;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.variable.ConstantValue;
import org.evosuite.testcase.variable.NullReference;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.variable.VariableReferenceImpl;
import org.evosuite.utils.generic.GenericClassFactory;
import org.evosuite.utils.generic.GenericMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.mvc.condition.NameValueExpression;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

public class SpringTestFactory {

  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static TestCase createTestCaseForRequestMapping(RequestMappingInfo requestMappingInfo) {
    logger.debug("createTestCaseForRequestMapping");
    TestCase tc = new DefaultTestCase();
    VariableReference requestBuilder = addRequestBuilder(tc, requestMappingInfo);
    logger.debug(requestBuilder.toString());
    addMockPerform(tc);
    addAsserts(tc);
    return tc;
  }

  private static VariableReference addRequestBuilder(TestCase tc, RequestMappingInfo requestMappingInfo) {
    logger.debug("addRequestBuilder");
    VariableReference requestBuilder = createRequestBuilder(tc, requestMappingInfo);
    requestBuilder = addParamsToRequestBuilder(tc, requestBuilder, requestMappingInfo);
    return requestBuilder;
  }

  private static VariableReference createRequestBuilder(TestCase tc, RequestMappingInfo requestMappingInfo) {
    logger.debug("createRequestBuilder");
    // get the url template as the first pattern into a string constant
    String urlValue = requestMappingInfo.getPatternsCondition().getPatterns().iterator().next();
    ConstantValue urlTemplate = new ConstantValue(tc, GenericClassFactory.get(String.class), urlValue);

    // null ref for other params of the get method
    NullReference nullRef = new NullReference(tc, Object[].class);

    // create the request builder
    Method method = null;
    try {
      method = MockMvcRequestBuilders.class.getMethod("get", String.class, Object[].class);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
    GenericMethod genericMethod = new GenericMethod(method, MockMvcRequestBuilders.class);
    VariableReference retVal = new VariableReferenceImpl(tc, method.getReturnType());
    MethodStatement statement = new MethodStatement(tc, genericMethod, null, List.of(urlTemplate, nullRef), retVal);

    // add the statement to the test case
    VariableReference requestBuilder = tc.addStatement(statement);
    return requestBuilder;
  }

  private static VariableReference addParamsToRequestBuilder(TestCase tc, VariableReference requestBuilder,
      RequestMappingInfo requestMappingInfo) {
    logger.debug("addParamsToRequestBuilder");

    // get the params as a list of string constants
    VariableReference ref = requestMappingInfo.getParamsCondition().getExpressions().stream().map(param ->
        addParamToRequestBuilder(tc, requestBuilder, param)
    ).reduce((a, b) -> b).orElse(null);
    logger.debug(ref.toString());
    return ref;
  }

  private static VariableReference addParamToRequestBuilder(TestCase tc, VariableReference requestBuilder, NameValueExpression param) {
    logger.debug("addParamToRequestBuilder");

    // get the param name and value as string constants
    ConstantValue paramName = new ConstantValue(tc, GenericClassFactory.get(String.class), param.getName());
    ConstantValue paramValue = new ConstantValue(tc, GenericClassFactory.get(String.class), param.getValue());

    // call the param method on the request builder
    Method method = null;
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

  private static void addMockPerform(TestCase tc) {
    logger.debug("addMockPerform");
  }

  private static void addAsserts(TestCase tc) {
    logger.debug("addAsserts");
  }

}
