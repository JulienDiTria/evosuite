package org.evosuite.utils;

import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.spring.SpringTestFactory;
import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.variable.ArrayReference;
import org.evosuite.testcase.variable.VariableReference;

public class TestCaseUtils {

    public static TestCase testCaseForDouble(){
        try {
            return testCaseForDoubleInternal();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static TestCase testCaseForDoubleInternal() throws NoSuchMethodException {
        TestCaseBuilder builder = new TestCaseBuilder();
        ArrayReference doubleArray0 = builder.appendArrayStmt(Double[].class, 10);
        VariableReference double0 = builder.appendNull(Double.class);
        builder.appendAssignment(doubleArray0, 0, double0);
        builder.appendAssignment(double0, doubleArray0, 0);
        builder.appendMethod(double0, Double.class.getMethod("floatValue"));
        return builder.getDefaultTestCase();
    }

    public static TestCase testCaseForFloat(){
        try {
            return testCaseForFloatInternal();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static TestCase testCaseForFloatInternal() throws NoSuchMethodException {
        TestCaseBuilder builder = new TestCaseBuilder();
        ArrayReference floatArray0 = builder.appendArrayStmt(Float[].class, 10);
        VariableReference float0 = builder.appendNull(Float.class);
        builder.appendAssignment(floatArray0, 0, float0);
        builder.appendAssignment(float0, floatArray0, 0);
        builder.appendMethod(float0, Float.class.getMethod("toString"));
        return builder.getDefaultTestCase();
    }

    public static TestCase testCaseForInteger(){
        try {
            return testCaseForIntegerInternal();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static TestCase testCaseForIntegerInternal() throws NoSuchMethodException {
        TestCaseBuilder builder = new TestCaseBuilder();
        ArrayReference integerArray0 = builder.appendArrayStmt(Integer[].class, 10);
        VariableReference integer0 = builder.appendNull(Integer.class);
        builder.appendAssignment(integerArray0, 0, integer0);
        builder.appendAssignment(integer0, integerArray0, 0);
        builder.appendMethod(integer0, Integer.class.getMethod("toString"));
        return builder.getDefaultTestCase();
    }

    public static TestCase testCaseForLong(){
        try {
            return testCaseForLongInternal();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static TestCase testCaseForLongInternal() throws NoSuchMethodException {
        TestCaseBuilder builder = new TestCaseBuilder();
        ArrayReference longArray0 = builder.appendArrayStmt(Long[].class, 10);
        VariableReference long0 = builder.appendNull(Long.class);
        builder.appendAssignment(longArray0, 0, long0);
        builder.appendAssignment(long0, longArray0, 0);
        builder.appendMethod(long0, Long.class.getMethod("toString"));
        return builder.getDefaultTestCase();
    }

    public static TestCase testCaseForSpring(){
        try {
            return testCaseForSpringInternal();
        } catch (ConstructionFailedException e) {
            throw new RuntimeException(e);
        }
    }

    private static TestCase testCaseForSpringInternal() throws ConstructionFailedException {
        TestCase testCase = new DefaultTestCase();
        SpringTestFactory.insertRandomSpringCall(testCase, 0);
        return testCase;
    }

}
