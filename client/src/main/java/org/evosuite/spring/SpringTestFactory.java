package org.evosuite.spring;

import com.sun.tools.javac.util.List;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.StringPrimitiveStatement;
import org.evosuite.testcase.variable.ConstantValue;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.variable.VariableReferenceImpl;
import org.evosuite.utils.generic.GenericClassFactory;
import org.evosuite.utils.generic.GenericMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

public class SpringTestFactory {

  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static TestCase createTestCaseForRequestMapping(RequestMappingInfo requestMappingInfo) {
    TestCase tc = new DefaultTestCase();
    addRequestBuilder(tc, requestMappingInfo);
    addMockPerform(tc);
    addAsserts(tc);
    return tc;
  }

    private static void addRequestBuilder(TestCase tc, RequestMappingInfo requestMappingInfo) {
      createRequestBuilder(tc, requestMappingInfo);
      addParamsToRequestBuilder(tc, requestMappingInfo);
    }

    private static void createRequestBuilder(TestCase tc, RequestMappingInfo requestMappingInfo) {
      // get the url template as the first pattern into a string constant
      String urlValue = requestMappingInfo.getPatternsCondition().getPatterns().iterator().next();
      ConstantValue urlTemplate = new ConstantValue(tc, GenericClassFactory.get(String.class), urlValue);

      // create the request builder
      GenericMethod method = new GenericMethod(MockMvcRequestBuilders.class.getMethod("get", String.class, Object[].class), "org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder");
      VariableReference retVal = new VariableReferenceImpl(tc, method.getReturnType());
      MethodStatement statement = new MethodStatement(tc, method, null, List.of(urlTemplate), retVal);

      // add the statement to the test case
      tc.addStatement(statement);
    }

    private static void addParamsToRequestBuilder(TestCase tc, RequestMappingInfo requestMappingInfo) {
      // get the params as a list of string constants
      ArrayList<StringPrimitiveStatement> params = new ArrayList<>();
      requestMappingInfo.getParamsCondition().getExpressions().forEach((param) -> {
        params.add(new StringPrimitiveStatement(tc, param.toString()));
      });

      // add the params to the request builder
      MethodStatement statement = new MethodStatement(tc, "params");
      statement.setMethodCall("params");
      statement.addParam(params);
      tc.addStatement(statement);
    }

    private static void addParamToRequestBuilder(TestCase tc, String param) {
      MethodStatement statement = new MethodStatement(tc, "param");
      statement.setMethodCall("param");
      statement.addParam(param);
      tc.addStatement(statement);
    }

    private static void addMockPerform(TestCase tc) {
        String methodName = "mockPerform";
        MethodStatement ms = new MethodStatement(tc, methodName);
        ms.setMethodCall("perform");
        tc.addStatement(ms);
    }

    private static void addAsserts(TestCase tc) {
        String methodName = "asserts";
        MethodStatement ms = new MethodStatement(tc, methodName);
        ms.setMethodCall("andExpect");
        ms.addParam("status");
        ms.addParam("isOk");
        tc.addStatement(ms);
    }

}
