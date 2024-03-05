package org.evosuite.junit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.utils.TestCaseUtils;
import org.junit.Test;

public class TestSuiteTest {
    @Test
    public void testOneTestCase(){
        ExecutionResult executionResult = TestCaseExecutor.runTest(TestCaseUtils.testCaseForInteger());
        TestSuite testSuite = new TestSuite("TestSuite", executionResult);

        System.out.println(testSuite.toCode());
    }

    @Test
    public void testTwoTestCases(){
        ArrayList<ExecutionResult> results = new ArrayList<>();
        results.add(TestCaseExecutor.runTest(TestCaseUtils.testCaseForInteger()));
        results.add(TestCaseExecutor.runTest(TestCaseUtils.testCaseForDouble()));
        TestSuite testSuite = new TestSuite("TestSuite", results);

        System.out.println(testSuite.toCode());
    }

    @Test
    public void testTestCaseWithSpringAndException(){
        ExecutionResult executionResult = TestCaseExecutor.runTest(TestCaseUtils.testCaseForSpring());
        TestSuite testSuite = new TestSuite("TestSuite", executionResult);

        System.out.println(testSuite.toCode());
    }



}
