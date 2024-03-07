package org.evosuite.spring;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpringRunListener extends RunListener {

    private static final Logger logger = LoggerFactory.getLogger(SpringRunListener.class);

    private static final String receive_description_format = "received {} with description {}";
    private static final String receive_result_format = "received {} with result {}";
    private static final String receive_failure_format = "received {} with failure {}";

    private CountDownLatch latch;

    private void testDone() {
        latch.countDown();
    }

    public SpringRunListener(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void testRunStarted(Description description) throws Exception {
        logger.warn(receive_description_format, "testRunStarted", description);
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        logger.warn(receive_result_format, "testRunFinished", result);
        testDone();
    }

    @Override
    public void testSuiteStarted(Description description) throws Exception {
        logger.info(receive_description_format, "testSuiteStarted", description);
    }

    @Override
    public void testSuiteFinished(Description description) throws Exception {
        logger.warn(receive_description_format, "testSuiteFinished", description);
        testDone();
    }

    @Override
    public void testStarted(Description description) throws Exception {
        logger.warn(receive_description_format, "testStarted", description);
    }

    @Override
    public void testFinished(Description description) throws Exception {
        logger.warn(receive_description_format, "testFinished", description);

        latch.countDown();
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        logger.warn(receive_failure_format, "testFailure", failure);

        testDone();
    }

    @Override
    public void testAssumptionFailure(Failure failure) {
        logger.warn(receive_failure_format, "testAssumptionFailure", failure);

        testDone();
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        logger.warn(receive_description_format, "testIgnored", description);

        testDone();
    }
}
