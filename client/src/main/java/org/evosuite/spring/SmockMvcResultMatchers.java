package org.evosuite.spring;

import com.sun.tools.javac.util.List;
import java.lang.reflect.Method;
import java.util.Collections;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.variable.ConstantValue;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.variable.VariableReferenceImpl;
import org.evosuite.utils.generic.GenericClassFactory;
import org.evosuite.utils.generic.GenericMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.result.StatusResultMatchers;

public class SmockMvcResultMatchers {

    public static void addResultMatcher(TestCase testCase, int position, VariableReference mvcResult) {
        int length;

        // add specific result matcher
        length = testCase.size();
        VariableReference specificResultMatcher = SmockMvcResultMatchers.createSpecificResultMatcher(testCase, position);
        position += testCase.size() - length;

        // get a result mather from the specific result matcher
        length = testCase.size();
        VariableReference resultMatcher = SmockMvcResultMatchers.getResultMatcher(testCase, position, specificResultMatcher);
        position += testCase.size() - length;

        // call the match on the result matcher with the result
        length = testCase.size();
        VariableReference match = SmockMvcResultMatchers.match(testCase, position, resultMatcher, mvcResult);
        position += testCase.size() - length;
    }

    private static VariableReference createSpecificResultMatcher(TestCase testCase, int position) {
        Method method;
        try {
            method = MockMvcResultMatchers.class.getMethod("status");
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }

        GenericMethod genericMethod = new GenericMethod(method, MockMvcResultMatchers.class);
        VariableReference specificResultMatcher = new VariableReferenceImpl(testCase, genericMethod.getReturnType());
        MethodStatement statement = new MethodStatement(testCase, genericMethod, null, Collections.emptyList(), specificResultMatcher);

        testCase.addStatement(statement, position);
        return specificResultMatcher;
    }

    private static VariableReference getResultMatcher(TestCase testCase, int position, VariableReference specificResultMatcher) {

        ConstantValue statusValue = new ConstantValue(testCase, GenericClassFactory.get(int.class), HttpStatus.OK.value());

        Method method;
        try {
            method = StatusResultMatchers.class.getMethod("is", int.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }

        GenericMethod genericMethod = new GenericMethod(method, StatusResultMatchers.class);
        VariableReference resultMatcher = new VariableReferenceImpl(testCase, genericMethod.getReturnType());
        MethodStatement statement = new MethodStatement(testCase, genericMethod, specificResultMatcher, List.of(statusValue), resultMatcher);

        testCase.addStatement(statement, position);
        return resultMatcher;
    }

    private static VariableReference match(TestCase testCase, int position, VariableReference resultMatcher, VariableReference mvcResult) {

        Method method;
        try {
            method = ResultMatcher.class.getMethod("match", MvcResult.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }

        GenericMethod genericMethod = new GenericMethod(method, ResultMatcher.class);
        VariableReference match = new VariableReferenceImpl(testCase, genericMethod.getReturnType());
        MethodStatement statement = new MethodStatement(testCase, genericMethod, resultMatcher, List.of(mvcResult), match);

        testCase.addStatement(statement, position);
        return match;
    }
}
