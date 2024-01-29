package org.evosuite.spring;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

public class MappingRegistry<T> {

  private final Map<T, Method> mappingLookup = new LinkedHashMap<>();

  public void register(T mapping, Object handler, Method method) {
    this.mappingLookup.put(mapping, method);
  }

  @Override
  public String toString() {
      return this.mappingLookup.toString();
  }
}
