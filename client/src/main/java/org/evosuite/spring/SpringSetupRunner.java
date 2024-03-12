package org.evosuite.spring;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.evosuite.spring.proxySpring.ProxyTestExecutionListener;
import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.statements.Fail;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

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

    public static <T> T getSuperFieldValue(Object holder, String fieldName) {
        Field field = null;
        Object fieldValue = null;
        try {
            field = holder.getClass().getSuperclass().getDeclaredField(fieldName);
            field.setAccessible(true);
            fieldValue = field.get(holder);
            return (T) fieldValue;
        } catch (NoSuchFieldException | IllegalAccessException | ClassCastException e) {
            logger.error("getSuperFieldValue Error getting field value: " + fieldName, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Perform the same logic as {@link SpringJUnit4ClassRunnerrunChild - (FrameworkMethod, RunNotifier)}, except the statement is not
     * executed and only saved for later use.
     */
    @Override
    protected void runChild(FrameworkMethod frameworkMethod, RunNotifier notifier) {
        logger.info("runChild - Running child: {}", frameworkMethod.getName());
        Description description = describeChild(frameworkMethod);
        if (isTestMethodIgnored(frameworkMethod)) {
            logger.info("runChild - TestMethodIgnored: {}", frameworkMethod.getName());
            notifier.fireTestIgnored(description);
        } else {
            try {
                statement = methodBlock(frameworkMethod);
                if (statement instanceof Fail) {
                    logger.error("runChild - statement creation failed with error: {}", statement);
                }
                logger.info("runChild - statement received ok : {}", statement);
            } catch (Throwable ex) {
                statement = new Fail(ex);
                logger.error("runChild - statement creation failed with error: {}", ex.toString());
            }
            // THIS IS THE EXECUTION OF THE STATEMENT WHICH WE DON'T WANT
//            runLeaf(statement, description, notifier);
            notifier.fireTestFinished(description);
            logger.info("runChild - done ok");
        }
    }

    /**
     * Intercept the methodBlock to save the framework method and test instance then call the super methodBlock
     * {@link SpringJUnit4ClassRunnermethodBlock - (FrameworkMethod)} .
     */
    @Override
    protected Statement methodBlock(FrameworkMethod frameworkMethod) {
        logger.info("methodBlock - frameworkMethod: {}", frameworkMethod.getName());
        try {
            testInstance = new ReflectiveCallable() {
                @Override
                protected Object runReflectiveCall() throws Throwable {
                    Object test = createTest();
                    logger.warn("methodBlock - test created: {}", test);
                    return test;
                }
            }.run();
        } catch (Throwable ex) {
            logger.error("methodBlock - error when trying to create the test: " + frameworkMethod.getName(), ex);
            return new Fail(ex);
        }
        logger.info("methodBlock - test created for: {}", frameworkMethod.getName());
        logger.info("methodBlock - testInstance: {}", testInstance);

        mockMvc = getFieldValue(testInstance, "mockMvc0");
        logger.info("methodBlock - mockMvc object: {}", mockMvc);

        handlerMethods = getHandlerMethodsFromMockMvc(mockMvc);
        logger.info("methodBlock - handlerMethods: {}", handlerMethods);

        this.frameworkMethod = frameworkMethod;
        logger.info("methodBlock - done: {}", frameworkMethod.getName());

        return methodInvoker(frameworkMethod, testInstance);
    }

    @Override
    protected Object createTest() throws Exception {

        Class<?> aClass = this.getClass();
        Package aPackage = aClass.getPackage();
        logger.warn("createTest - class: {} in package {}", aClass, aPackage);

        TestContextManager testContextManager = getTestContextManager();
        List<TestExecutionListener> testExecutionListeners = testContextManager.getTestExecutionListeners();
        for (int i = 0; i < testExecutionListeners.size(); i++) {
            TestExecutionListener testExecutionListener = testExecutionListeners.get(i);
            if (!(testExecutionListener instanceof ProxyTestExecutionListener)) {
                ProxyTestExecutionListener proxyTestExecutionListener = new ProxyTestExecutionListener(testExecutionListener);
                testExecutionListeners.set(i, proxyTestExecutionListener);
            }
        }
        Collections.shuffle(testExecutionListeners);

        Object testInstance = getTestClass().getOnlyConstructor().newInstance();
        MockMvc mockMvc0 = getFieldValue(testInstance, "mockMvc0");
        logger.warn("createTest before preparation - testInstance.mockMvc0: {}", mockMvc0);
        try {
            testContextManager.prepareTestInstance(testInstance);
        } catch (Exception e) {
            logger.error("createTest - error when trying to prepare the test instance: " + testInstance, e);
            throw e;
        }
        MockMvc mockMvc1 = getFieldValue(testInstance, "mockMvc0");
        logger.warn("createTest after  preparation - testInstance.mockMvc0: {}", mockMvc1);

        return testInstance;
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
