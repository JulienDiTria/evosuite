package org.evosuite.spring;

import java.lang.reflect.Method;
import java.util.HashMap;
import org.evosuite.Properties;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFactory;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.generic.GenericMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

public class SpringTestFactory {

    private static final Logger logger = LoggerFactory.getLogger(SpringTestFactory.class);

    private static final SpringTestFactory singleton = new SpringTestFactory();

    private static final HashMap<TestCase, SpringTestInsertedInfo> insertedInfo = new HashMap<>();


    private class SpringTestInsertedInfo {
        public int mockPerform;
        public int requestBuilder;
        public int resultMatcher;

        public SpringTestInsertedInfo() {
            mockPerform = 0;
            requestBuilder = 0;
            resultMatcher = 0;
        }

        public boolean isNextMockPerform(){
            return mockPerform == 0;
        }

        public boolean isNextRequestBuilder(){
            return requestBuilder == 0 || Randomness.nextBoolean();
        }
    }

    /**
     * Get the singleton reference
     *
     * @return the singleton reference
     */
    public static SpringTestFactory getInstance() {
        return singleton;
    }

    /**
     * Creates a test case for the given request mapping info. The test case contains a request builder that is created using the
     * SmockRequestBuilder class. The request builder is then used to perform the request and the result is returned. The test case doesn't
     * contain any assertions at this stage.
     *
     * @param requestMappingInfo the request mapping info provided by the Spring framework corresponding to that request
     * @return the test case containing the request builder and the result
     */
    public static TestCase createTestCaseForRequestMapping(RequestMappingInfo requestMappingInfo) {
        TestCase tc = new DefaultTestCase();
        VariableReference requestBuilder = addRequestBuilder(tc, requestMappingInfo);
        logger.debug("{}", requestBuilder);
        VariableReference mvcResult = performAndGetResult(tc, requestBuilder);
        return tc;
    }

    public static VariableReference addRequestBuilder(TestCase tc, RequestMappingInfo requestMappingInfo) {
        logger.debug("addRequestBuilder");
        VariableReference requestBuilder = SmockRequestBuilder.createRequestBuilder(tc, requestMappingInfo);
        SmockRequestBuilder.addParamsToRequestBuilder(tc, requestBuilder, requestMappingInfo);
        return requestBuilder;
    }

    /**
     * Performs the request contained by the request builder and return the ResultActions wrapping around the result.
     * <p>
     * SmockMvc smockMvc = New SmockMvc(); ResultActions resultActions = smockMvc.perform(requestBuilder); MvcResult mvcResult =
     * resultActions.andReturn();
     *
     * @param tc             the test case in which the request is performed
     * @param requestBuilder the request builder that contains the request to be performed
     */
    private static VariableReference performAndGetResult(TestCase tc, VariableReference requestBuilder) {
        logger.debug("performAndGetResult");

        // create the smockMVC object
        VariableReference mockMvc = SmockMvc.createMockMvc(tc);

        // call perform and get the result actions
        VariableReference resultActions = SmockMvc.mockPerform(tc, mockMvc, requestBuilder);

        // return the mvcResult from the result actions
        VariableReference mvcResult = SmockResultActions.andReturn(tc, resultActions);
        return mvcResult;
    }

    public static boolean insertRandomSpringCall(TestCase test, int position) {
        return singleton.insertRandomSpringCall(test, position, 0);
    }

    private boolean insertRandomSpringCall(TestCase test, int position, int recursionDepth) {
        SpringTestInsertedInfo info = getInsertedInfo(test);
        try {
            if (addRequestBuilder(test, position, recursionDepth) &&
                addMockPerform(test, position, recursionDepth) &&
                addResultMatcher(test, position, recursionDepth)) {
                return true;
            }
        } catch (ConstructionFailedException e) {
            return false;
        }
        return false;
    }

    /**
     * Get the SpringTestInsertedInfo for the given test case
     *
     * @param test the test case
     * @return the SpringTestInsertedInfo for the given test case
     */
    private SpringTestInsertedInfo getInsertedInfo(TestCase test) {
        return insertedInfo.computeIfAbsent(test, k -> new SpringTestInsertedInfo());
    }

    /**
     * Add a mock perform to the given test case, increasing the inserted info mock perform counter.
     *
     * @param test the test case in which to add the mock perform statement
     * @param position the position in the test case where to add the mock perform statement
     * @param recursionDepth the current recursion depth
     * @return true if the mock perform was added, false otherwise
     * @throws ConstructionFailedException if the max recursion depth is reached
     */
    private boolean addMockPerform(TestCase test, int position, int recursionDepth) throws ConstructionFailedException{
        assertRecursionDepth(recursionDepth);

        int length;

        // add the mock mvc object
        length = test.size();
        VariableReference mockMvc = TestFactory.getInstance().addInjection(test, SpringSupport.getMockMvc(), position, recursionDepth);
        position += (test.size() - length);

        // add the perform statement
        GenericMethod genericMethod = SpringSupport.getMockMvcPerform();
        TestFactory.getInstance().addMethodFor(test, mockMvc, genericMethod, position);

        return true;
    }

    /**
     * Add a request builder to the given test case, increasing the inserted info request builder counter.
     *
     * @param test the test case in which to add the request builder statement
     * @param position the position in the test case where to add the request builder statement
     * @param recursionDepth the current recursion depth
     * @return true if the request builder was added, false otherwise
     * @throws ConstructionFailedException if the max recursion depth is reached
     */
    private boolean addRequestBuilder(TestCase test, int position, int recursionDepth) throws ConstructionFailedException {
        assertRecursionDepth(recursionDepth);

        int length;

        // get a random request mapping info from SpringSupport
        RequestMappingInfo requestMappingInfo = SpringSupport.getRequestMappingInfo();
        if (requestMappingInfo == null) {
            logger.warn("No request mapping info available");
            return false;
        }

        // add the request builder and add the params to it
        length = test.size();
        VariableReference requestBuilder = SmockRequestBuilder.createRequestBuilder(test, position, requestMappingInfo);
        position += (test.size() - length);

        SmockRequestBuilder.addParamsToRequestBuilder(test, position, requestBuilder, requestMappingInfo);
        return true;
    }

    /**
     * Add a result matcher to the given test case, increasing the inserted info result matcher counter.
     *
     * @param test the test case in which to add the result matcher statement
     * @param position the position in the test case where to add the result matcher statement
     * @param recursionDepth the current recursion depth
     * @return true if the result matcher was added, false otherwise
     * @throws ConstructionFailedException if the max recursion depth is reached
     */
    private boolean addResultMatcher(TestCase test, int position, int recursionDepth) throws ConstructionFailedException {
        assertRecursionDepth(recursionDepth);
        return false;
    }

    /**
     * Asserts that the recursion depth is not greater than the max recursion depth
     *
     * @param recursionDepth the current recursion depth
     * @throws ConstructionFailedException if the max recursion depth is reached
     */
    private void assertRecursionDepth(int recursionDepth) throws ConstructionFailedException {
        if (recursionDepth > Properties.MAX_RECURSION) {
            logger.debug("Max recursion depth reached");
            throw new ConstructionFailedException("Max recursion depth reached");
        }
    }
}
