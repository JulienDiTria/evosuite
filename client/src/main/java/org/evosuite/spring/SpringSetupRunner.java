package org.evosuite.spring;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.sandbox.Sandbox;
import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.statements.Fail;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import static org.evosuite.utils.LoggingUtils.loadLogbackForEvoSuite;

public class SpringSetupRunner extends SpringJUnit4ClassRunner {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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

    public static <T> T getFieldValue(Object holder, String fieldName) {
        Field field = null;
        Object fieldValue = null;
        try {
            field = holder.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            fieldValue = field.get(holder);
            return (T) fieldValue;
        } catch (NoSuchFieldException | IllegalAccessException | ClassCastException e) {
            logger.error("getFieldValue Error getting field value: " + fieldName, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Perform the same logic as {@link SpringJUnit4ClassRunnerrunChild - (FrameworkMethod, RunNotifier)}, except the statement is not
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
                if (statement instanceof Fail) {
                    logger.error("runChild - statement is Fail {}", statement);
                    notifier.fireTestFailure(new Failure(description, new RuntimeException("statement is Fail : " + statement)));
                }
                else {
                    notifier.fireTestFinished(description);
                }
            } catch (Throwable ex) {
                statement = new Fail(ex);
                logger.error("runChild - statement creation failed with error", ex);
                notifier.fireTestFailure(new Failure(description, new RuntimeException("statement creation failed", ex)));
            }
        }
    }

    /**
     * Intercept the methodBlock to save the framework method and test instance then call the super methodBlock
     * {@link SpringJUnit4ClassRunnermethodBlock - (FrameworkMethod)} .
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
            restoreLoggerConfig();
            logger.error("methodBlock - error when trying to create the test: " + frameworkMethod.getName(), ex);
            return new Fail(ex);
        }
        restoreLoggerConfig();

        mockMvc = getFieldValue(testInstance, "mockMvc0");
        handlerMethods = getHandlerMethodsFromMockMvc(mockMvc);
        this.frameworkMethod = frameworkMethod;

        return methodInvoker(frameworkMethod, testInstance);
    }

    /**
     * Restore the logger configuration to the original state.
     * <p>
     *     This can be used after loading Spring context (as Spring overload the logger configuration)
     */
    private void restoreLoggerConfig() {
        // as in the client, the runtime sandbox might be activated and not allow to create new sockets connections.
        // Disable it just for the logger configuration restoration
        Sandbox.SandboxMode currentSandboxMode = RuntimeSettings.sandboxMode;
        RuntimeSettings.sandboxMode = Sandbox.SandboxMode.OFF;
        loadLogbackForEvoSuite();
        RuntimeSettings.sandboxMode = currentSandboxMode;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("SpringSetupRunner{\n");

        stringBuilder.append("\tstatement=").append(statement).append("\n");
        stringBuilder.append("\tframeworkMethod=").append(frameworkMethod).append("\n");
        stringBuilder.append("\ttestInstance=").append(testInstance).append("\n");
        stringBuilder.append("\tmockMvc=").append(mockMvc).append("\n");
        stringBuilder.append("\thandlerMethods=").append(handlerMethods).append("\n");

//        stringBuilder.append("\ttestContextManager.testContext=").append(this.getTestContextManager().getTestContext()).append("\n");
        stringBuilder.append("\ttestContextManager.testExecutionsListeners.size=").append(this.getTestContextManager().getTestExecutionListeners().size()).append("\n");
//        stringBuilder.append("\ttestContextManager.testExecutionsListeners=").append(this.getTestContextManager().getTestExecutionListeners().stream().map(
//            Objects::toString).collect(Collectors.toList())).append("\n");

        stringBuilder.append("}");
        return stringBuilder.toString();
    }
}
