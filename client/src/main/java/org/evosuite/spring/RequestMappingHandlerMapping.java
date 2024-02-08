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
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringValueResolver;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

public class RequestMappingHandlerMapping {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final MappingRegistry registry = new MappingRegistry();

    private final Map<String, Predicate<Class<?>>> pathPrefixes = new LinkedHashMap<>();

    @Nullable
    private final StringValueResolver embeddedValueResolver = null;

    private RequestMappingInfo.BuilderConfiguration config = new RequestMappingInfo.BuilderConfiguration();

    /**
     * Get the class for an object.
     *
     * @param object the object for which to find the class
     * @return the class of the object
     */
    public static Class<?> getClassForObject(Object object) {
        Class<?> clazz;
        try {
            clazz = (object instanceof String ? Class.forName((String) object) : object.getClass());
        } catch (ClassNotFoundException e) {
            clazz = null;
            logger.error("Class not found for: {}", object);
        }
        if (clazz == Class.class) {
            clazz = getClassForObject(((Class<?>) object).getName());
        }
        return clazz;
    }

    //region create registry of handler methods
    public void processCandidateController(Object controller) {
        Class<?> clazz = getClassForObject(controller);
        if (clazz != null && isHandler(clazz)) {
            detectHandlerMethods(clazz);
        }
    }

    /**
     * Whether the given object is a handler with handler methods. Expects a handler to have either a type-level @Controller annotation or a
     * type-level @RequestMapping annotation.
     *
     * @param object the object to check
     * @return true if the object is a handler, false otherwise
     */
    private boolean isHandler(Class<?> object) {
        return (AnnotatedElementUtils.hasAnnotation(object, Controller.class) ||
            AnnotatedElementUtils.hasAnnotation(object, RequestMapping.class));
    }

    /**
     * Detects handler methods at initialization.
     *
     * @param handlerType the full classname of a handler or a handler instance
     */
    private void detectHandlerMethods(Class<?> handlerType) {
        if (handlerType != null) {
            Class<?> userType = ClassUtils.getUserClass(handlerType);
            Map<Method, RequestMappingInfo> methods = MethodIntrospector.selectMethods(userType,
                (MethodIntrospector.MetadataLookup<RequestMappingInfo>) method -> {
                    try {
                        return getMappingForMethod(method, userType);
                    } catch (Throwable ex) {
                        throw new IllegalStateException("Invalid mapping on handler class [" +
                            userType.getName() + "]: " + method, ex);
                    }
                });
            methods.forEach((method, mapping) -> {
                Method invocableMethod = AopUtils.selectInvocableMethod(method, userType);
                registerHandlerMethod(handlerType, invocableMethod, mapping);
            });
        }
    }

    /**
     * Register a handler method into the local registry.
     *
     * @param handler the handler object
     * @param method  the method to register
     * @param mapping the request mapping conditions associated with the handler method
     */
    private void registerHandlerMethod(Class<?> handler, Method method, RequestMappingInfo mapping) {
        this.registry.register(mapping, handler, method);
    }

    private RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        RequestMappingInfo info = createRequestMappingInfo(method);
        if (info != null) {
            RequestMappingInfo typeInfo = createRequestMappingInfo(handlerType);
            if (typeInfo != null) {
                info = typeInfo.combine(info);
            }
            String prefix = getPathPrefix(handlerType);
            if (prefix != null) {
                info = RequestMappingInfo.paths(prefix).build().combine(info);
            }
        }
        return info;
    }

    private RequestMappingInfo createRequestMappingInfo(AnnotatedElement element) {
        RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(element, RequestMapping.class);
        RequestCondition<?> condition = (element instanceof Class ?
            getCustomTypeCondition((Class<?>) element) : getCustomMethodCondition((Method) element));
        return (requestMapping != null ? createRequestMappingInfo(requestMapping, condition) : null);
    }

    private RequestMappingInfo createRequestMappingInfo(
        RequestMapping requestMapping, @Nullable RequestCondition<?> customCondition) {

        RequestMappingInfo.Builder builder = RequestMappingInfo
            .paths(resolveEmbeddedValuesInPatterns(requestMapping.path()))
            .methods(requestMapping.method())
            .params(requestMapping.params())
            .headers(requestMapping.headers())
            .consumes(requestMapping.consumes())
            .produces(requestMapping.produces())
            .mappingName(requestMapping.name());
        if (customCondition != null) {
            builder.customCondition(customCondition);
        }
        return builder.options(this.config).build();
    }

    @Nullable
    protected RequestCondition<?> getCustomTypeCondition(Class<?> handlerType) {
        return null;
    }

    @Nullable
    protected RequestCondition<?> getCustomMethodCondition(Method method) {
        return null;
    }

    @Nullable
    private String getPathPrefix(Class<?> handlerType) {
        for (Map.Entry<String, Predicate<Class<?>>> entry : this.pathPrefixes.entrySet()) {
            if (entry.getValue().test(handlerType)) {
                String prefix = entry.getKey();
                if (this.embeddedValueResolver != null) {
                    prefix = this.embeddedValueResolver.resolveStringValue(prefix);
                }
                return prefix;
            }
        }
        return null;
    }

    protected String[] resolveEmbeddedValuesInPatterns(String[] patterns) {
        if (this.embeddedValueResolver == null) {
            return patterns;
        } else {
            String[] resolvedPatterns = new String[patterns.length];
            for (int i = 0; i < patterns.length; i++) {
                resolvedPatterns[i] = this.embeddedValueResolver.resolveStringValue(patterns[i]);
            }
            return resolvedPatterns;
        }
    }

    //endregion

    //region get the handler for a request
    public SmockHandlerExecutionChain getHandler(SmockRequest request) {
        return null;
    }

    /**
     * Look up a handler method for the given request.
     */
//    private HandlerMethod getHandlerInternal(SmockRequest request) throws Exception {
//        String lookupPath = getUrlPathHelper().getLookupPathForRequest(request);
//        HandlerMethod handlerMethod = lookupHandlerMethod(lookupPath, request);
//        return (handlerMethod != null ? handlerMethod.createWithResolvedBean() : null);
//    }

    /**
     * Look up the best-matching handler method for the current request.
     * If multiple matches are found, the best match is selected.
     * @param lookupPath mapping lookup path within the current servlet mapping
     * @param request the current request
     * @return the best-matching handler method, or {@code null} if no match
     */
    @Nullable
    private HandlerMethod lookupHandlerMethod(String lookupPath, SmockRequest request) throws Exception {
        List<RequestMappingInfo> matches = new ArrayList<>();
        List<RequestMappingInfo> directPathMatches = this.registry.getMappingsByUrl(lookupPath);
        if (directPathMatches != null) {
            addMatchingMappings(directPathMatches, matches);
        }
        if (matches.isEmpty()) {
            // No choice but to go through all mappings...
            addMatchingMappings(this.registry.getMappings(), matches);
        }

//        if (!matches.isEmpty()) {
//            Comparator<AbstractHandlerMethodMapping.Match> comparator =
//                new AbstractHandlerMethodMapping.MatchComparator(getMappingComparator(request));
//            matches.sort(comparator);
//            AbstractHandlerMethodMapping.Match bestMatch = matches.get(0);
//            if (matches.size() > 1) {
//                if (logger.isTraceEnabled()) {
//                    logger.trace(matches.size() + " matching mappings: " + matches);
//                }
//                if (CorsUtils.isPreFlightRequest(request)) {
//                    return PREFLIGHT_AMBIGUOUS_MATCH;
//                }
//                AbstractHandlerMethodMapping.Match secondBestMatch = matches.get(1);
//                if (comparator.compare(bestMatch, secondBestMatch) == 0) {
//                    Method m1 = bestMatch.handlerMethod.getMethod();
//                    Method m2 = secondBestMatch.handlerMethod.getMethod();
//                    String uri = request.getRequestURI();
//                    throw new IllegalStateException(
//                        "Ambiguous handler methods mapped for '" + uri + "': {" + m1 + ", " + m2 + "}");
//                }
//            }
//            handleMatch(bestMatch.mapping, lookupPath, request);
//            return bestMatch.handlerMethod;
//        } else {
//            return handleNoMatch(this.mappingRegistry.getMappings().keySet(), lookupPath, request);
//        }
        return null;
    }

    private void addMatchingMappings(Collection<RequestMappingInfo> mappings, List<RequestMappingInfo> matches) {
        matches.addAll(mappings);
    }

    //endregion

    @Override
    public String toString() {
        return this.registry.toString();
    }

}
