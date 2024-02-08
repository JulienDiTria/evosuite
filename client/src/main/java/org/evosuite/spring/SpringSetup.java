package org.evosuite.spring;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SpringSetup {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final SpringSetup instance = new SpringSetup();
    private final RequestMappingHandlerMapping requestMappingHandlerMapping = new RequestMappingHandlerMapping();
    private SpringSetupRunner springSetupRunner;

    private SpringSetup() {
    }

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

    public static void setup(Object controller) throws Exception {
        Class<?> clazz = getClassForObject(controller);
        RunNotifier runNotifier = new RunNotifier();

        instance.springSetupRunner = new SpringSetupRunner(clazz);
        instance.springSetupRunner.run(runNotifier);

        Object testInstance = instance.springSetupRunner.testInstance;
        MockMvc mockMvc = getFieldValue(testInstance, "mockMvc");

        mockMvc.perform(get("/owners")
                .param("lastName", "Smith"))
            .andExpect(status().isOk());

        System.out.println("MockMvc: " + mockMvc);
        System.out.println("SpringSetupRunner executed");
    }

    static <T> T getFieldValue(Object holder, String fieldName) {
        Field field = null;
        Object fieldValue = null;
        try {
            field = holder.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            fieldValue = field.get(holder);
            return (T) fieldValue;
        } catch (NoSuchFieldException | IllegalAccessException | ClassCastException e) {
            throw new RuntimeException(e);
        }
    }

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
}
