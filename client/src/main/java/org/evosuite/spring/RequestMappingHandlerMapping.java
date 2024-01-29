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

  /**
   * Detects handler methods at initialization.
   * @param handler the full classname of a handler or a handler instance
   * @throws Exception if initialization fails
   */
  public void detectHandlerMethods(Object handler) {
    Class<?> handlerType = getClassForObject(handler);

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
        registerHandlerMethod(handler, invocableMethod, mapping);
      });
    }
  }

  private Class<?> getClassForObject(Object object) {
    Class<?> clazz;
    try {
      clazz = (object instanceof String ? Class.forName((String) object) : object.getClass());
    } catch (ClassNotFoundException e) {
      clazz = null;
      logger.error("Class not found for: {}", object);
    }
    return clazz;
  }

  void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
    this.registry.register(mapping, handler, method);
  }

  RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
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

  RequestMappingInfo createRequestMappingInfo(
      RequestMapping requestMapping, @Nullable RequestCondition<?> customCondition) {

    RequestMappingInfo.Builder builder = RequestMappingInfo
//        .paths(resolveEmbeddedValuesInPatterns(requestMapping.path()))
        .paths(requestMapping.path())
        .methods(requestMapping.method())
        .params(requestMapping.params())
        .headers(requestMapping.headers())
        .consumes(requestMapping.consumes())
        .produces(requestMapping.produces())
        .mappingName(requestMapping.name());
    if (customCondition != null) {
      builder.customCondition(customCondition);
    }
//    return builder.options(this.config).build();
    return builder.build();
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
  String getPathPrefix(Class<?> handlerType) {
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

  @Override
  public String toString() {
    return this.registry.toString();
  }
}
