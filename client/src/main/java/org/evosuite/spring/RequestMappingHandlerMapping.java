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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import org.evosuite.testcase.TestFactory;
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
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

public class RequestMappingHandlerMapping {

  private static final Logger logger = LoggerFactory.getLogger(TestFactory.class);

  private final MappingRegistry<RequestMappingInfo> registry = new MappingRegistry<>();

  private final Map<String, Predicate<Class<?>>> pathPrefixes = new LinkedHashMap<>();

  @Nullable
  private final StringValueResolver embeddedValueResolver = null;

  private RequestMappingInfo.BuilderConfiguration config = new RequestMappingInfo.BuilderConfiguration();

  public void processCandidateController(Object controller) {
    Class<?> clazz = getClassForObject(controller);
    if (clazz != null && isHandler(clazz)) {
      detectHandlerMethods(clazz);
    }
  }

  /**
   * Whether the given object is a handler with handler methods.
   * Expects a handler to have either a type-level @Controller annotation or a type-level @RequestMapping annotation.
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
   * @param handler the full classname of a handler or a handler instance
   */
  private void detectHandlerMethods(Class<?> handlerType) {
    if (handlerType != null) {
      Class<?> userType = ClassUtils.getUserClass(handlerType);
      Map<Method, RequestMappingInfo> methods = MethodIntrospector.selectMethods(userType,
          (MethodIntrospector.MetadataLookup<RequestMappingInfo>) method -> {
            try {
              return getMappingForMethod(method, userType);
            }
            catch (Throwable ex) {
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
   * Get the class for an object.
   * @param object the object for which to find the class
   * @return the class of the object
   */
  private static Class<?> getClassForObject(Object object) {
    Class<?> clazz;
    try {
      clazz = (object instanceof String ? Class.forName((String) object) : object.getClass());
    } catch (ClassNotFoundException e) {
      clazz = null;
      logger.error("Class not found for: {}", object);
    }
    return clazz;
  }

  /**
   * Register a handler method into the local registry.
   * @param handler the handler object
   * @param method the method to register
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
    }
    else {
      String[] resolvedPatterns = new String[patterns.length];
      for (int i = 0; i < patterns.length; i++) {
        resolvedPatterns[i] = this.embeddedValueResolver.resolveStringValue(patterns[i]);
      }
      return resolvedPatterns;
    }
  }

  @Override
  public String toString() {
    return this.registry.toString();
  }
}
