package org.evosuite.spring;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class SpringRunListener extends RunListener {

    private CountDownLatch latch;

    private void testDone() {
        latch.countDown();
    }

    public SpringRunListener(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void testRunStarted(Description description) throws Exception {
        testDone();
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        testDone();
    }

    @Override
    public void testSuiteStarted(Description description) throws Exception {
        testDone();
    }

    @Override
    public void testSuiteFinished(Description description) throws Exception {
        testDone();
    }

    @Override
    public void testStarted(Description description) throws Exception {
        testDone();
    }

    @Override
    public void testFinished(Description description) throws Exception {
        latch.countDown();
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        testDone();
    }

    @Override
    public void testAssumptionFailure(Failure failure) {
        testDone();
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        testDone();
    }
}
