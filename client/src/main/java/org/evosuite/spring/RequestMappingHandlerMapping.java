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
 * License from repo org/springframework/spring-webmvc/5.1.2.RELEASE
 * org/springframework/web/servlet/mvc/method/annotation/RequestMappingHandlerMapping
 * Code taken and adapted to work with EvoSuite
 */

package org.evosuite.spring;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import static org.evosuite.spring.SpringSupport.getClassForObject;

public class RequestMappingHandlerMapping {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final RequestMappingInfo mockRequestMappingInfo =
        RequestMappingInfo
            .paths("/owners/new")
            .methods(RequestMethod.GET)
            .params("lastName=Smith", "firstName=Will")
            .build();
    private static final HashMap<Class<?>, Boolean> handlerTypes = new HashMap<>();

    private RequestMappingHandlerMapping() {}

    /**
     * Check whether the given object is a spring controller (able to handle methods)
     *
     * @param controller the object to check
     * @return true if the object is a handler, false otherwise
     */
    public static boolean isHandlerType(Object controller) {
        Class<?> clazz = getClassForObject(controller);

        if (!handlerTypes.containsKey(clazz)) {
            handlerTypes.put(clazz, clazz != null && isHandler(clazz));
        }

        return handlerTypes.get(clazz);
    }

    /**
     * Whether the given object is a handler with handler methods. Expects a handler to have either a type-level @Controller annotation or a
     * type-level @RequestMapping annotation.
     *
     * @param object the object to check
     * @return true if the object is a handler, false otherwise
     */
    private static boolean isHandler(Class<?> object) {
        return (AnnotatedElementUtils.hasAnnotation(object, Controller.class) ||
            AnnotatedElementUtils.hasAnnotation(object, RequestMapping.class));
    }

    public static RequestMappingInfo getRequestMappingInfo() {
        // TODO 2023.02.23 Julien Di Tria
        //  Should fail. For now this is just a warning and we use a mock request mapping info.
        logger.warn("No request mapping info available, using mock data");
        return mockRequestMappingInfo;
    }
}
