package org.evosuite.spring;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.statements.Fail;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

public class SpringSetupRunner extends SpringJUnit4ClassRunner {

    public Statement statement;
    public FrameworkMethod frameworkMethod;
    public Object testInstance;

    /**
     * The MockMvc instance setup by spring context
     */
    public MockMvc mockMvc;

    /**
     * The mapping between the request mapping info and the handler method for the controller setup by spring context
     */
    public Map<RequestMappingInfo, HandlerMethod> handlerMethods;

    /**
     * Construct a new {@code SpringSetupRunner} and initialize a
     * {@link org.springframework.test.context.TestContextManager TestContextManager} to provide Spring testing functionality to standard
     * JUnit 4 tests.
     *
     * @param clazz the test class to be run
     * @see #createTestContextManager(Class)
     */
    public SpringSetupRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
    }

    /**
     * Get the handler methods from the MockMvc instance
     *
     * @param mockMvc the MockMvc instance
     * @return the handler methods
     */
    private static Map<RequestMappingInfo, HandlerMethod> getHandlerMethodsFromMockMvc(MockMvc mockMvc) {
        DispatcherServlet dispatcherServlet = mockMvc.getDispatcherServlet();
        List<HandlerMapping> handlerMappings = dispatcherServlet.getHandlerMappings();
        Optional<RequestMappingHandlerMapping> requestMappingHandlerMapping = handlerMappings.stream()
            .filter(handlerMapping -> handlerMapping instanceof RequestMappingHandlerMapping)
            .map(handlerMapping -> ((RequestMappingHandlerMapping) handlerMapping))
            .findFirst();
        return requestMappingHandlerMapping.map(RequestMappingHandlerMapping::getHandlerMethods).orElse(null);
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
     * Perform the same logic as {@link SpringJUnit4ClassRunner#runChild(FrameworkMethod, RunNotifier)}, except the statement is not
     * executed and only saved for later use.
     */
    @Override
    protected void runChild(FrameworkMethod frameworkMethod, RunNotifier notifier) {
        Description description = describeChild(frameworkMethod);
        if (isTestMethodIgnored(frameworkMethod)) {
            notifier.fireTestIgnored(description);
        } else {
            try {
                statement = methodBlock(frameworkMethod);
            } catch (Throwable ex) {
                statement = new Fail(ex);
            }
            // THIS IS THE EXECUTION OF THE STATEMENT WHICH WE DON'T WANT
//            runLeaf(statement, description, notifier);
        }
    }

    /**
     * Intercept the methodBlock to save the framework method and test instance then call the super methodBlock
     * {@link SpringJUnit4ClassRunner#methodBlock(FrameworkMethod)} .
     */
    @Override
    protected Statement methodBlock(FrameworkMethod frameworkMethod) {
        try {
            testInstance = new ReflectiveCallable() {
                @Override
                protected Object runReflectiveCall() throws Throwable {
                    return createTest();
                }
            }.run();
        } catch (Throwable ex) {
            return new Fail(ex);
        }
        mockMvc = getFieldValue(testInstance, "mockMvc0");
        handlerMethods = getHandlerMethodsFromMockMvc(mockMvc);
        this.frameworkMethod = frameworkMethod;

        return super.methodBlock(frameworkMethod);
    }
}
