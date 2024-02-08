package org.evosuite.spring;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

public class SpringSetup {

    private static final SpringSetup instance = new SpringSetup();

    private SpringSetup(){}

    private final RequestMappingHandlerMapping requestMappingHandlerMapping = new RequestMappingHandlerMapping();
    private SpringJUnit4ClassRunner springJUnit4ClassRunner;

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

    public static void setup(Object controller) throws InitializationError {
        Class<?> clazz = RequestMappingHandlerMapping.getClassForObject(controller);
        RunNotifier runNotifier = new RunNotifier();

        instance.springJUnit4ClassRunner = new SpringJUnit4ClassRunner(clazz);
        instance.springJUnit4ClassRunner.run(runNotifier);
    }
}
