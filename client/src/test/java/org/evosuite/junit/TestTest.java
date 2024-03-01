package org.evosuite.junit;

import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.utils.TestCaseUtils;

public class TestTest {

    private static ExecutionResult executionResult;

    @org.junit.Test
    public void testTestNormal(){

        executionResult = TestCaseExecutor.runTest(TestCaseUtils.testCaseForInteger());
        Test test = new Test(executionResult);

        System.out.println(test.toCode());
    }

    @org.junit.Test
    public void testTestSpring(){

        executionResult = TestCaseExecutor.runTest(TestCaseUtils.testCaseForSpring());
        Test test = new Test(executionResult);

        System.out.println(test.toCode());
    }

}
