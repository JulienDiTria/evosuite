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
import java.util.Arrays;
import java.util.Set;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.statements.ArrayStatement;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.mvc.condition.NameValueExpression;
import org.springframework.web.servlet.mvc.condition.ParamsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

public class SmockRequestBuilder {

      private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

//      private static HTTP

      private final String method;

      private final URI url;

      private final MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

      public SmockRequestBuilder(HttpMethod httpMethod, String url) throws URISyntaxException {
            this(httpMethod.name(), new URI(url));
      }

      private SmockRequestBuilder(String method, URI url) {
            Assert.notNull(method, "'httpMethod' is required");
            Assert.notNull(url, "'url' is required");
            this.method = method;
            this.url = url;
      }

      SmockRequest buildRequest() throws Exception {
            SmockRequest request = new SmockRequest();
//            request.setMethod(this.method);
//            request.setUrl(this.url);
//            request.setParameters(this.parameters);
//            request.();
            return request;
      }

      public SmockRequestBuilder param(String name, String... values) {
            addToMultiValueMap(this.parameters, name, values);
            return this;
      }

      private static <T> void addToMultiValueMap(MultiValueMap<String, T> map, String name, T[] values) {
            Assert.hasLength(name, "'name' must not be empty");
            Assert.notEmpty(values, "'values' must not be empty");
            for (T value : values) {
                  map.add(name, value);
            }
      }

      /*
       * ***********************
       * EVOSUITE helpers
       * ***********************
       */

      /**
       * Helper to create a SmockMvcRequestBuilder in evosuite.
       * Creates a request builder for the given request mapping info.
       *
       * SmockRequestBuilder requestBuilder = new SmockRequestBuilder(HttpMethod.GET, "/api/1");
       *
       * @param tc the test case to add the request builder to
       * @param requestMappingInfo the request mapping info provided by the Spring framework corresponding to that request
       * @return the request builder reference added to the testcase
       */
      public static VariableReference createRequestBuilder(TestCase tc, RequestMappingInfo requestMappingInfo) {
            logger.debug("createRequestBuilder");

            // get the http method as the request method into a new HttpMethod enum
            HttpMethod httpMethod = HttpMethod.resolve(requestMappingInfo.getMethodsCondition().getMethods().iterator().next().name());
            ConstantValue httpMethodValue = new ConstantValue(tc, GenericClassFactory.get(HttpMethod.class), httpMethod);

            // TODO 01.02.2024 Julien Di Tria
            //  url value can be GA variable to be mutated (if not given by the request mapping info)
            // get the url template as the first pattern into a string constant
            String url = requestMappingInfo.getPatternsCondition().getPatterns().iterator().next();
            ConstantValue urlValue = new ConstantValue(tc, GenericClassFactory.get(String.class), url);

            // get the constructor by reflection
            Constructor<?> constructor = null;
            try {
                  constructor = SmockRequestBuilder.class.getConstructor(HttpMethod.class, String.class);
            } catch (NoSuchMethodException e) {
                  throw new RuntimeException(e);
            }

            // create the constructor statement
            GenericConstructor genericConstructor = new GenericConstructor(constructor, SmockRequestBuilder.class);
            ConstructorStatement statement = new ConstructorStatement(tc, genericConstructor, List.of(httpMethodValue, urlValue));

            // add the statement to the test case
            VariableReference requestBuilder = tc.addStatement(statement);
            return requestBuilder;
      }

      /**
       * Adds the params given in the request mapping info to the request builder.
       * Each param is added as a call to the param method on the request builder and return the request builder reference.
       *
       * @param tc the test case in which the params are added
       * @param requestBuilder the request builder to add the params to
       * @param requestMappingInfo the request mapping info provided by the Spring framework containing a seed for the parameters
       * @return the request builder reference added to the testcase
       */
      public static VariableReference addParamsToRequestBuilder(TestCase tc, VariableReference requestBuilder,
          RequestMappingInfo requestMappingInfo) {
            logger.debug("addParamsToRequestBuilder");

            // TODO 01.02.2024 Julien Di Tria
            //  param name can be GA variable to be mutated (sometimes adding that param, somtimes not)
            // get the params as a list of string constants
            ParamsRequestCondition paramsRequestCondition = requestMappingInfo.getParamsCondition();
            Set<NameValueExpression<String>> expressions = paramsRequestCondition.getExpressions();
            VariableReference ref = expressions.stream().map(param ->
                addParamToRequestBuilder(tc, requestBuilder, param)
            ).reduce((a, b) -> b).orElse(requestBuilder);
            logger.debug("{}", ref);
            return ref;
      }

      /**
       * Adds the param given to the request builder.
       * The param is added as a call to the param method on the request builder and return the request builder reference.
       *
       * @param tc the test case in which the param is added
       * @param requestBuilder the request builder to add the param to
       * @param param the param to add
       * @return the request builder reference added to the testcase
       */
      private static VariableReference addParamToRequestBuilder(TestCase tc, VariableReference requestBuilder, NameValueExpression param) {
            logger.debug("addParamToRequestBuilder");

            // TODO 01.02.2024 Julien Di Tria
            //  param value can be GA variable to be mutated (based on value pool and guided by the actual parameter in the function call)
            // get the param name and value as string constants
            ConstantValue paramName = new ConstantValue(tc, GenericClassFactory.get(String.class), param.getName());
            ConstantValue paramValue = new ConstantValue(tc, GenericClassFactory.get(String.class), param.getValue());

            // create the param method on the request builder
            Method method;
            try {
                  method = SmockRequestBuilder.class.getMethod("param", String.class, String[].class);
            } catch (NoSuchMethodException e) {
                  throw new RuntimeException(e);
            }
            GenericMethod genericMethod = new GenericMethod(method, MockHttpServletRequestBuilder.class);
            VariableReference retVal = new VariableReferenceImpl(tc, genericMethod.getReturnType());
            MethodStatement statement = new MethodStatement(tc, genericMethod, requestBuilder, List.of(paramName, paramValue), retVal);
            requestBuilder = tc.addStatement(statement);
            return requestBuilder;
      }
}
