package org.evosuite.spring;

@FunctionalInterface
public interface SmockResultMatcher {
  void match(SmockMvcResult result) throws Exception;

  static SmockResultMatcher matchAll(SmockResultMatcher... matchers) {
    return result -> {
      for (SmockResultMatcher matcher : matchers) {
        matcher.match(result);
      }
    };
  }
}