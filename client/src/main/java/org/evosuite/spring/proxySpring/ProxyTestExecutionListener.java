package org.evosuite.spring.proxySpring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.SpringBootDependencyInjectionTestExecutionListener;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

public class ProxyTestExecutionListener implements TestExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(ProxyTestExecutionListener.class);

    private final TestExecutionListener testExecutionListener;
    private final String name;

    public ProxyTestExecutionListener(TestExecutionListener testExecutionListener) {

        if (testExecutionListener instanceof SpringBootDependencyInjectionTestExecutionListener) {
            this.testExecutionListener =
                new ProxySpringBootDepInject((SpringBootDependencyInjectionTestExecutionListener) testExecutionListener);
        } else {
            this.testExecutionListener = testExecutionListener;
        }
        this.name = this.testExecutionListener.getClass().getSimpleName();
    }

    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
        logger.warn("{} beforeTestClass pre", name);
        testExecutionListener.beforeTestClass(testContext);
        logger.warn("{} beforeTestClass aft", name);
    }

    @Override
    public void prepareTestInstance(TestContext testContext) throws Exception {
        logger.warn("{} prepareTestInstance pre", name);
        testExecutionListener.prepareTestInstance(testContext);
        logger.warn("{} prepareTestInstance aft", name);
    }

    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        logger.warn("{} beforeTestMethod pre", name);
        testExecutionListener.beforeTestMethod(testContext);
        logger.warn("{} beforeTestMethod aft", name);
    }

    @Override
    public void beforeTestExecution(TestContext testContext) throws Exception {
        logger.warn("{} beforeTestExecution pre", name);
        testExecutionListener.beforeTestExecution(testContext);
        logger.warn("{} beforeTestExecution aft", name);
    }

    @Override
    public void afterTestExecution(TestContext testContext) throws Exception {
        logger.warn("{} afterTestExecution pre", name);
        testExecutionListener.afterTestExecution(testContext);
        logger.warn("{} afterTestExecution aft", name);
    }

    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        logger.warn("{} afterTestMethod pre", name);
        testExecutionListener.afterTestMethod(testContext);
        logger.warn("{} afterTestMethod aft", name);
    }

    @Override
    public void afterTestClass(TestContext testContext) throws Exception {
        logger.warn("{} afterTestClass pre", name);
        testExecutionListener.afterTestClass(testContext);
        logger.warn("{} afterTestClass aft", name);
    }
}
