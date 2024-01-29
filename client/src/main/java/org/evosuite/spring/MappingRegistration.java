package org.evosuite.spring;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public class MappingRegistration<T> {

  private final T mapping;

  private final Method handlerMethod;

  private final List<String> directUrls;

  @Nullable
  private final String mappingName;

  public MappingRegistration(T mapping, Method handlerMethod,
      @Nullable List<String> directUrls, @Nullable String mappingName) {

    Assert.notNull(mapping, "Mapping must not be null");
    Assert.notNull(handlerMethod, "HandlerMethod must not be null");
    this.mapping = mapping;
    this.handlerMethod = handlerMethod;
    this.directUrls = (directUrls != null ? directUrls : Collections.emptyList());
    this.mappingName = mappingName;
  }

  public T getMapping() {
    return this.mapping;
  }

  public Method getHandlerMethod() {
    return this.handlerMethod;
  }

  public List<String> getDirectUrls() {
    return this.directUrls;
  }

  @Nullable
  public String getMappingName() {
    return this.mappingName;
  }
}