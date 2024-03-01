package org.evosuite.junit;

import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.utils.TestCaseUtils;
import org.junit.Test;

public class TestSuiteTest {
    @Test
    public void testDefault(){
        ExecutionResult executionResult = TestCaseExecutor.runTest(TestCaseUtils.testCaseForInteger());
        TestSuite testSuite = new TestSuite("TestSuite", executionResult);

        System.out.println(testSuite.toCode());
    }
}
