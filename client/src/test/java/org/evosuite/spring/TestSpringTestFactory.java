package org.evosuite.spring;

import com.examples.with.different.packagename.spring.petclinic.owner.OwnerController;
import java.net.URISyntaxException;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.result.StatusResultMatchers;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import static org.junit.Assert.fail;

public class TestSpringTestFactory {
    @Test
    public void testCreateTestCaseForSpringRequestMapping() throws Exception {

        RequestMappingInfo requestMappingInfo =
            RequestMappingInfo
                .paths("/owners/new")
                .methods(RequestMethod.GET)
                .params("lastName=Smith", "firstName=Will")
                .build();
        TestCase test = SpringTestFactory.createTestCaseForRequestMapping(requestMappingInfo);
        System.out.println(test);

        executeEvoSuiteTestCase(test);
    }

    @Test
    public void generatedTest() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder0 = SmockRequestBuilder.request(HttpMethod.GET, "/owners/new");
        String[] stringArray0 = new String[1];
        stringArray0[0] = "Smith";
        mockHttpServletRequestBuilder0.param("lastName", stringArray0);
        String[] stringArray1 = new String[1];
        stringArray1[0] = "Will";
        mockHttpServletRequestBuilder0.param("firstName", stringArray1);
        MockMvc mockMvc0 = SmockMvc.defaultMockMvc();
        ResultActions resultActions0 = mockMvc0.perform(mockHttpServletRequestBuilder0);
        resultActions0.andReturn();
    }

    @Test
    public void testInsertRandomSpringCallFail() throws ConstructionFailedException {
        TestCase testCase = new DefaultTestCase();
        SpringTestFactory.insertRandomSpringCall(testCase, 0);

        System.out.println(testCase.toCode());

        boolean failed = false;
        try {
            executeEvoSuiteTestCase(testCase);
        } catch (AssertionError e) {
            failed = true;
        }
        if(!failed)
            fail("Test should have failed");

    }

    @Test
    public void testInsertRandomSpringCallSuccess() throws ConstructionFailedException {
        TestCase testCase = new DefaultTestCase();
        SpringSupport.setup(OwnerController.class.getName());
        SpringTestFactory.insertRandomSpringCall(testCase, 0);

        System.out.println(testCase.toCode());

        boolean failed = false;
        try {
            executeEvoSuiteTestCase(testCase);
        } catch (AssertionError e) {
            failed = true;
        }
        if(!failed)
            fail("Test should have failed");
    }

//    @Test
//    public void testGeneratedTestSuccess() throws Exception {
//        MockHttpServletRequestBuilder mockHttpServletRequestBuilder0 = SmockRequestBuilder.request(HttpMethod.GET, "/owners/var0");
//        MockMvc mockMvc0;
//        ResultActions resultActions0 = mockMvc0.perform(mockHttpServletRequestBuilder0);
//        MvcResult mvcResult0 = resultActions0.andReturn();
//        StatusResultMatchers statusResultMatchers0 = MockMvcResultMatchers.status();
//        ResultMatcher resultMatcher0 = statusResultMatchers0.is(200);
//        resultMatcher0.match(mvcResult0);
//    }

    private void executeEvoSuiteTestCase(TestCase testCase) {
        TestChromosome c = new TestChromosome();
        c.setTestCase(testCase);
        ExecutionResult executionResult = c.executeForFitnessFunction(new BranchCoverageSuiteFitness());

        System.out.println("test executed in " + executionResult.getExecutionTime() + "ms");
        if (executionResult.getAllThrownExceptions().isEmpty()) {
            System.out.println("no exception");
        } else {
            System.err.println(executionResult.getAllThrownExceptions().size() + " exception(s) during test execution");
            executionResult.getAllThrownExceptions().forEach(e -> e.printStackTrace(System.err));
            fail("Exception(s) thrown during the execution of the test case.");
        }
    }
}
