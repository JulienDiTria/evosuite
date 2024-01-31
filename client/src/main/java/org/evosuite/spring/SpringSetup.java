package org.evosuite.spring;

public class SpringSetup {

  private final RequestMappingHandlerMapping requestMappingHandlerMapping = new RequestMappingHandlerMapping();

  /**
   * Process the candidate controller which consists of
   * - registering the handler methods.
   *
   * @param controller the candidate controller
   */
  public void processCandidateController(Object controller) {
    requestMappingHandlerMapping.processCandidateController(controller);
  }
}
