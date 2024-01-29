package org.evosuite.spring;

@FunctionalInterface
public interface SmockResultHandler {

  /**
   * Perform an action on the given result.
   *
   * @param result the result of the executed request
   * @throws Exception if a failure occurs
   */
  void handle(SmockMvcResult result) throws Exception;

}
