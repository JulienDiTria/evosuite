package org.evosuite.spring;

public class SpringSetup {

    private static final SpringSetup instance = new SpringSetup();

    private SpringSetup(){}

    private final RequestMappingHandlerMapping requestMappingHandlerMapping = new RequestMappingHandlerMapping();

    /**
     * Process the candidate controller which consists of - registering the handler methods.
     *
     * @param controller the candidate controller
     */
    public static void processCandidateController(Object controller) {
        instance.requestMappingHandlerMapping.processCandidateController(controller);
    }

    public static RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        return instance.requestMappingHandlerMapping;
    }
}
