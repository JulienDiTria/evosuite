package org.evosuite.spring;

import java.lang.reflect.Method;
import java.util.Collections;
import org.evosuite.ga.ConstructionFailedException;
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

    /**
     * Add a random ResultMatcher to the testCase and call the match function on it with the given MvcResult.
     *
     * @param testCase the TestCase in which to add a resultMatcher
     * @param position the position in the test in which to add
     * @param mvcResult the result on which to call the match
     * @throws ConstructionFailedException if the whole construction is not complete
     */
    public static void addResultMatcher(TestCase testCase, int position, VariableReference mvcResult) throws ConstructionFailedException {
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

    /**
     * Create a random specific result matcher and add it to the testCase. (e.g. status())
     * // TODO 2023.02.27 Julien Di Tria This is a very simple implementation that only generates a status() specific result matcher.
     *
     * @param testCase the TestCase in which to add a specific result matcher
     * @param position the position in the test in which to add
     * @return the variable reference to the specific result matcher
     * @throws ConstructionFailedException if the construction is not complete
     */
    private static VariableReference createSpecificResultMatcher(TestCase testCase, int position) throws ConstructionFailedException {
        Method method;
        try {
            method = MockMvcResultMatchers.class.getMethod("status");
        } catch (NoSuchMethodException e) {
            throw new ConstructionFailedException(e.getClass().getName() +" : " + e.getMessage());
        }

        GenericMethod genericMethod = new GenericMethod(method, MockMvcResultMatchers.class);
        VariableReference specificResultMatcher = new VariableReferenceImpl(testCase, genericMethod.getReturnType());
        MethodStatement statement = new MethodStatement(testCase, genericMethod, null, Collections.emptyList(), specificResultMatcher);

        return testCase.addStatement(statement, position);
    }

    /**
     * Get the actual result matcher from the specific one and add it to the testCase.
     * // TODO 2023.02.27 Julien Di Tria This is a very simple implementation that only works for the status() specific result matcher and get the is() matcher.
     *
     * @param testCase the TestCase in which to add a result matcher
     * @param position the position in the test in which to add
     * @param specificResultMatcher the specific result matcher to use to get the actual result matcher
     * @return the variable reference to the result matcher
     * @throws ConstructionFailedException if the construction is not complete
     */
    private static VariableReference getResultMatcher(TestCase testCase, int position, VariableReference specificResultMatcher)
        throws ConstructionFailedException {

        ConstantValue statusValue = new ConstantValue(testCase, GenericClassFactory.get(int.class), HttpStatus.OK.value());

        Method method;
        try {
            method = StatusResultMatchers.class.getMethod("is", int.class);
        } catch (NoSuchMethodException e) {
            throw new ConstructionFailedException(e.getClass().getName() +" : " + e.getMessage());
        }

        GenericMethod genericMethod = new GenericMethod(method, StatusResultMatchers.class);
        VariableReference resultMatcher = new VariableReferenceImpl(testCase, genericMethod.getReturnType());
        MethodStatement statement = new MethodStatement(testCase, genericMethod, specificResultMatcher, Collections.singletonList(statusValue), resultMatcher);

        return testCase.addStatement(statement, position);
    }

    /**
     * Call the match function on the result matcher with the given MvcResult and add it to the testCase.
     *
     * @param testCase the TestCase in which to add a match
     * @param position the position in the test in which to add
     * @param resultMatcher the result matcher to call the match on
     * @param mvcResult the result on which to call the match
     * @return the variable reference to the match
     * @throws ConstructionFailedException if the construction is not complete
     */
    private static VariableReference match(TestCase testCase, int position, VariableReference resultMatcher, VariableReference mvcResult)
        throws ConstructionFailedException {

        Method method;
        try {
            method = ResultMatcher.class.getMethod("match", MvcResult.class);
        } catch (NoSuchMethodException e) {
            throw new ConstructionFailedException(e.getClass().getName() +" : " + e.getMessage());
        }

        GenericMethod genericMethod = new GenericMethod(method, ResultMatcher.class);
        VariableReference match = new VariableReferenceImpl(testCase, genericMethod.getReturnType());
        MethodStatement statement = new MethodStatement(testCase, genericMethod, resultMatcher, Collections.singletonList(mvcResult), match);

        return testCase.addStatement(statement, position);
    }
}
