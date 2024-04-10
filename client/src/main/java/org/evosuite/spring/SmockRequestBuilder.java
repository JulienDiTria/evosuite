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

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.evosuite.Properties;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.seeding.ConstantPool;
import org.evosuite.seeding.ConstantPoolManager;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.statements.ArrayStatement;
import org.evosuite.testcase.statements.AssignmentStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.ArrayIndex;
import org.evosuite.testcase.variable.ArrayReference;
import org.evosuite.testcase.variable.ConstantValue;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.variable.VariableReferenceImpl;
import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.generic.GenericClassFactory;
import org.evosuite.utils.generic.GenericMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.NameValueExpression;
import org.springframework.web.servlet.mvc.condition.ParamsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.util.UriComponentsBuilder;

import static org.apache.commons.lang3.StringUtils.contains;

public class SmockRequestBuilder {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
     * Add a request builder to the test case for the given request mapping info. The request builder is created using
     * the request mapping info as a seed. The request builder is then used to add the params and other
     * options to itself and the reference to the request builder is returned.
     *
     * @param testCase the test case to add the request builder to
     * @param position the position in the test case where the request builder is added
     * @param requestMappingInfo the request mapping info provided by the Spring framework corresponding to that request
     * @return the request builder reference added to the testcase
     * @throws ConstructionFailedException if the construction of the request builder fails
     */
    public static VariableReference addRequestBuilder(TestCase testCase, int position, RequestMappingInfo requestMappingInfo,
        HandlerMethod handlerMethod) throws ConstructionFailedException {
        int length = testCase.size();
        VariableReference requestBuilder = SmockRequestBuilder.createRequestBuilder(testCase, position, requestMappingInfo, handlerMethod);
        position += (testCase.size() - length);

        requestBuilder = SmockRequestBuilder.addParamsToRequestBuilder(testCase, position, requestBuilder, requestMappingInfo);

        // TODO 26.02.2024 Julien Di Tria : add other options...
        return requestBuilder;
    }

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
    public static VariableReference createRequestBuilder(TestCase tc, RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod)
        throws ConstructionFailedException {
        return createRequestBuilder(tc, tc.size(), requestMappingInfo, handlerMethod);
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
    public static VariableReference createRequestBuilder(TestCase tc, int position, RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod)
        throws ConstructionFailedException {
        logger.debug("createRequestBuilder");

        // get the http method as the request method into a new HttpMethod enum
        HttpMethod httpMethod = HttpMethod.resolve(requestMappingInfo.getMethodsCondition().getMethods().iterator().next().name());
        ConstantValue httpMethodValue = new ConstantValue(tc, GenericClassFactory.get(HttpMethod.class), httpMethod);

        // get the url template
        String url = createValidUriFromUrl(requestMappingInfo, handlerMethod);
        ConstantValue urlValue = new ConstantValue(tc, GenericClassFactory.get(String.class), url);

        // get the method by reflection
        Method method = null;
        try {
            method = SmockRequestBuilder.class.getMethod("request", HttpMethod.class, String.class);
        } catch (NoSuchMethodException e) {
            throw new ConstructionFailedException(e.getClass().getName() +" : " + e.getMessage());
        }

        // create the constructor statement
        GenericMethod genericMethod = new GenericMethod(method, MockMvcRequestBuilders.class);
        Statement statement = new MethodStatement(tc, genericMethod, null, Arrays.asList(httpMethodValue, urlValue));

        // add the statement to the test case
        return tc.addStatement(statement, position);
    }

    private static String createValidUriFromUrl(RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod) throws ConstructionFailedException {

        // TODO 22.02.2024 Julien Di Tria
        //  url value can be GA variable to be mutated (if not given by the request mapping info)
        String url = requestMappingInfo.getPatternsCondition().getPatterns().iterator().next();

        if (handlerMethod == null) {
            return validUriFromOnUrl(url, 0);
        }

        Map<String, Object> vars = new HashMap<>();
        // check each parameter of the handler method to see if they are used in the url based on the annotations
        for(MethodParameter methodParameter : handlerMethod.getMethodParameters()) {
            if (methodParameter.hasParameterAnnotation(PathVariable.class)){
                PathVariable pathVariable = methodParameter.getParameterAnnotation(PathVariable.class);
                if (pathVariable != null && StringUtils.hasText(pathVariable.value())){
                    // check if used in the url
                    String name = pathVariable.value();
                    if (contains(url, "{" + name + "}")) {
                        // if used, generate a random value for the parameter
                        Type parameterType = methodParameter.getParameterType();
                        GenericClass<?> clazz = GenericClassFactory.get(parameterType);
                        Object value = ConstantPoolManager.getInstance().getConstantPool().getRandomValue(clazz);

                        // add the value to the vars map
                        vars.put(name, value);
                    }
                }
            }
        }

        try {
            // try to build the url with the variables
            return UriComponentsBuilder.fromUriString(url).buildAndExpand(vars).encode().toUriString();
        } catch (IllegalArgumentException e1) {
            throw new ConstructionFailedException("unable to generate a valid url from: " + url + " with vars: " + vars);
        }
    }

    /**
     * Adds the params given in the request mapping info to the request builder. Each param is added as a call to the param method on the
     * request builder and return the request builder reference.
     *
     * @param tc                 the test case in which the params are added
     * @param requestBuilder     the request builder to add the params to
     * @param requestMappingInfo the request mapping info provided by the Spring framework containing a seed for the parameters
     */
    public static VariableReference addParamsToRequestBuilder(TestCase tc, VariableReference requestBuilder,
        RequestMappingInfo requestMappingInfo) throws ConstructionFailedException {
        return addParamsToRequestBuilder(tc, tc.size(), requestBuilder, requestMappingInfo);
    }

    /**
     * Adds the params given in the request mapping info to the request builder. Each param is added as a call to the param method on the
     * request builder and return the request builder reference.
     *
     * @param tc                 the test case in which the params are added
     * @param requestBuilder     the request builder to add the params to
     * @param requestMappingInfo the request mapping info provided by the Spring framework containing a seed for the parameters
     */
    public static VariableReference addParamsToRequestBuilder(TestCase tc, int position, VariableReference requestBuilder,
        RequestMappingInfo requestMappingInfo) throws ConstructionFailedException {
        logger.debug("addParamsToRequestBuilder");

        // get the params as a list of string constants
        ParamsRequestCondition paramsRequestCondition = requestMappingInfo.getParamsCondition();
        Set<NameValueExpression<String>> expressions = paramsRequestCondition.getExpressions();

        // TODO 22.02.2024 Julien Di Tria
        //  param name can be GA variable to be mutated (sometimes adding that param, somtimes not)
        VariableReference retVal = requestBuilder;
        int length;
        for(NameValueExpression<String> param : expressions) {
            length = tc.size();
            retVal = addParamToRequestBuilder(tc, position, requestBuilder, param);
            position += (tc.size() - length);
        }
        return retVal;
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
    private static VariableReference addParamToRequestBuilder(TestCase tc, int position, VariableReference requestBuilder,
        NameValueExpression<String> param) throws ConstructionFailedException {
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
            throw new ConstructionFailedException(e.getClass().getName() +" : " + e.getMessage());
        }
        GenericMethod genericMethod = new GenericMethod(method, MockHttpServletRequestBuilder.class);
        VariableReference retVal = new VariableReferenceImpl(tc, genericMethod.getReturnType());
        MethodStatement statement = new MethodStatement(tc, genericMethod, requestBuilder, Arrays.asList(paramName, arrayRef), retVal);
        return tc.addStatement(statement, position+2);
    }

    /**
     * Helper to generate a valid URL string from a urlTemplate.
     * @param url the url to validate
     * @param recursionDepth the current recursion depth
     * @return a valid URL string
     * @throws ConstructionFailedException if the max recursion depth is reached
     */
    private static String validUriFromOnUrl(String url, int recursionDepth) throws ConstructionFailedException {
        try{
            new URI(url);
            return url;
        } catch (URISyntaxException e) {
//            logger.warn("Invalid URI: {}, depth {} ", url, recursionDepth);

            if (recursionDepth < Properties.MAX_RECURSION) {
                // add a variable for each path variable in the url
                Object[] vars = new Object[recursionDepth];
                for (int i = 0; i < recursionDepth; i++) {
                    // TODO 22.02.2024 Julien Di Tria : Should be random vars or GA vars
                    vars[i] = "var" + i;
                }
                try {
                    // try to build the url with the variables
                    return UriComponentsBuilder.fromUriString(url).buildAndExpand(vars).encode().toUriString();
                } catch (IllegalArgumentException e1) {
                    // recursively try to generate a valid url
                    return validUriFromOnUrl(url, recursionDepth + 1);
                }
            } else {
                throw new ConstructionFailedException("unable to generate a valid url from: " + url);
            }
        }
    }
}
