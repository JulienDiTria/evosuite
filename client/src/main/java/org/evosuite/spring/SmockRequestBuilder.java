/*
 * Copyright 2002-2017 the original author or authors.
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
 * org/springframework/test/web/servlet/RequestBuilder.java
 * Code taken and adapted to work with EvoSuite
 */

package org.evosuite.spring;

import com.sun.tools.javac.util.List;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.statements.ArrayStatement;
import org.evosuite.testcase.statements.AssignmentStatement;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.ArrayIndex;
import org.evosuite.testcase.variable.ArrayReference;
import org.evosuite.testcase.variable.ConstantValue;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.variable.VariableReferenceImpl;
import org.evosuite.utils.generic.GenericClassFactory;
import org.evosuite.utils.generic.GenericConstructor;
import org.evosuite.utils.generic.GenericMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.mvc.condition.NameValueExpression;
import org.springframework.web.servlet.mvc.condition.ParamsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

public class SmockRequestBuilder {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Helper to create a MockHttpServletRequestBuilder for a http get request in evosuite for the given url. Used to generate a call to
     * {@link MockMvcRequestBuilders#get(String urlTemplate, Object... uriVars)}.
     *
     * @param tc  the test case to add the request builder to
     * @param url the url to create the request builder for
     * @return the request builder reference added to the testcase
     */
    public static VariableReference createGetRequestBuilder(TestCase tc, String url) {
        logger.debug("createGetRequestBuilder");

        // put the url into a string constant for parameter : urlTemplate
        ConstantValue urlValue = new ConstantValue(tc, GenericClassFactory.get(String.class), url);

        // create a new empty string array for parameter : uriVars
        ArrayStatement arrayStmt = new ArrayStatement(tc, Object[].class, 1);
        VariableReference arrayRef = tc.addStatement(arrayStmt);

        // get the method by reflection
        Method method = null;
        try {
            method = MockMvcRequestBuilders.class.getMethod("get", String.class, Object[].class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        // create the method statement
        GenericMethod genericMethod = new GenericMethod(method, MockMvcRequestBuilders.class);
        VariableReference retVal = new VariableReferenceImpl(tc, genericMethod.getReturnType());
        Statement statement = new MethodStatement(tc, genericMethod, null, List.of(urlValue, arrayRef), retVal);

        // add the statement to the test case
        VariableReference requestBuilder = tc.addStatement(statement);

        return requestBuilder;
    }

    /**
     * Wrapper around MockMvcRequestBuilders.request to create a MockHttpServletRequestBuilder for the given http method and url as a
     * string. Used to generate a call to {@link MockMvcRequestBuilders#request(HttpMethod httpMethod, URI uri)}.
     *
     * @param httpMethod The HTTP method to use
     * @param url The URL to use
     * @return A request builder for the given request
     * @throws URISyntaxException if the given URL is not a valid URI
     */
    public static MockHttpServletRequestBuilder request(HttpMethod httpMethod, String url) throws URISyntaxException {
        return MockMvcRequestBuilders.request(httpMethod, new URI(url));
    }

    /*
     * ***********************
     * EVOSUITE helpers
     * ***********************
     */

    /**
     * Helper to create a MockHttpServletRequestBuilder in evosuite. Creates a request builder for the given request mapping info and
     * appends it to the test case.
     * <p>
     * MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.request(httpMethod, uri)
     *
     * @param tc                 the test case to add the request builder to
     * @param requestMappingInfo the request mapping info provided by the Spring framework corresponding to that request
     * @return the request builder reference added to the testcase
     */
    public static VariableReference createRequestBuilder(TestCase tc, RequestMappingInfo requestMappingInfo) {
        return createRequestBuilder(tc, tc.size(), requestMappingInfo);
    }

    /**
     * Helper to create a MockHttpServletRequestBuilder in evosuite. Creates a request builder for the given request mapping info and adds
     * it to the test case at the given position.
     * <p>
     * MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.request(httpMethod, uri)
     *
     * @param tc                 the test case to add the request builder to
     * @param position           the position in the test case where the request builder is added
     * @param requestMappingInfo the request mapping info provided by the Spring framework corresponding to that request
     * @return the request builder reference added to the testcase
     */
    public static VariableReference createRequestBuilder(TestCase tc, int position, RequestMappingInfo requestMappingInfo) {
        logger.debug("createRequestBuilder");

        // get the http method as the request method into a new HttpMethod enum
        HttpMethod httpMethod = HttpMethod.resolve(requestMappingInfo.getMethodsCondition().getMethods().iterator().next().name());
        ConstantValue httpMethodValue = new ConstantValue(tc, GenericClassFactory.get(HttpMethod.class), httpMethod);

        // TODO 22.02.2024 Julien Di Tria
        //  url value can be GA variable to be mutated (if not given by the request mapping info)
        // get the url template as the first pattern into a string constant
        String url = requestMappingInfo.getPatternsCondition().getPatterns().iterator().next();
        ConstantValue urlValue = new ConstantValue(tc, GenericClassFactory.get(String.class), url);

        // get the method by reflection
        Method method = null;
        try {
            method = SmockRequestBuilder.class.getMethod("request", HttpMethod.class, String.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        // create the constructor statement
        GenericMethod genericMethod = new GenericMethod(method, MockMvcRequestBuilders.class);
        Statement statement = new MethodStatement(tc, genericMethod, null, List.of(httpMethodValue, urlValue));

        // add the statement to the test case
        return tc.addStatement(statement, position);
    }

    /**
     * Adds the params given in the request mapping info to the request builder. Each param is added as a call to the param method on the
     * request builder and return the request builder reference.
     *
     * @param tc                 the test case in which the params are added
     * @param requestBuilder     the request builder to add the params to
     * @param requestMappingInfo the request mapping info provided by the Spring framework containing a seed for the parameters
     */
    public static void addParamsToRequestBuilder(TestCase tc, VariableReference requestBuilder,
        RequestMappingInfo requestMappingInfo) {
        addParamsToRequestBuilder(tc, tc.size(), requestBuilder, requestMappingInfo);
    }

    /**
     * Adds the params given in the request mapping info to the request builder. Each param is added as a call to the param method on the
     * request builder and return the request builder reference.
     *
     * @param tc                 the test case in which the params are added
     * @param requestBuilder     the request builder to add the params to
     * @param requestMappingInfo the request mapping info provided by the Spring framework containing a seed for the parameters
     */
    public static void addParamsToRequestBuilder(TestCase tc, int position, VariableReference requestBuilder,
        RequestMappingInfo requestMappingInfo) {
        logger.debug("addParamsToRequestBuilder");

        // get the params as a list of string constants
        ParamsRequestCondition paramsRequestCondition = requestMappingInfo.getParamsCondition();
        Set<NameValueExpression<String>> expressions = paramsRequestCondition.getExpressions();

        // TODO 22.02.2024 Julien Di Tria
        //  param name can be GA variable to be mutated (sometimes adding that param, somtimes not)
        int length;
        for(NameValueExpression<String> param : expressions) {
              length = tc.size();
              addParamToRequestBuilder(tc, position, requestBuilder, param);
              position += (tc.size() - length);
        }
    }

    /**
     * Adds the param given to the request builder. The param is added as a call to the param method on the request builder and return the
     * request builder reference.
     *
     * @param tc             the test case in which the param is added
     * @param requestBuilder the request builder to add the param to
     * @param param          the param to add
     * @return the request builder reference added to the testcase
     */
    private static void addParamToRequestBuilder(TestCase tc, int position, VariableReference requestBuilder,
        NameValueExpression<String> param) {
        logger.debug("addParamToRequestBuilder");

        // TODO 01.02.2024 Julien Di Tria
        //  param value can be GA variable to be mutated (based on value pool and guided by the actual parameter in the function call)
        // get the param name and value as string constants
        ConstantValue paramName = new ConstantValue(tc, GenericClassFactory.get(String.class), param.getName());
        ConstantValue paramValue = new ConstantValue(tc, GenericClassFactory.get(String.class), param.getValue());

        // put the param value into a new string array
        ArrayStatement arrayStmt = new ArrayStatement(tc, String[].class, 1);
        tc.addStatement(arrayStmt, position);
        ArrayReference arrayRef = (ArrayReference) arrayStmt.getReturnValue();
        ArrayIndex arrayIndex = new ArrayIndex(tc, arrayRef, 0);
        AssignmentStatement assignmentStatement = new AssignmentStatement(tc, arrayIndex, paramValue);
        tc.addStatement(assignmentStatement, position+1);

        // create the param method on the request builder
        Method method;
        try {
            method = MockHttpServletRequestBuilder.class.getMethod("param", String.class, String[].class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        GenericMethod genericMethod = new GenericMethod(method, MockHttpServletRequestBuilder.class);
        VariableReference retVal = new VariableReferenceImpl(tc, genericMethod.getReturnType());
        MethodStatement statement = new MethodStatement(tc, genericMethod, requestBuilder, List.of(paramName, arrayRef), retVal);
        tc.addStatement(statement, position+2);
    }

}
