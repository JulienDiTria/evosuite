package org.evosuite.spring;

import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

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

        TestChromosome c = new TestChromosome();
        c.setTestCase(test);
        ExecutionResult executionResult = c.executeForFitnessFunction(new BranchCoverageSuiteFitness());

        System.out.println("test executed in " + executionResult.getExecutionTime() + "ms");
        if (executionResult.getAllThrownExceptions().isEmpty()) {
            System.out.println("no exception");
        } else {
            executionResult.getAllThrownExceptions().forEach(e -> System.out.println("exception" + e.toString()));
        }
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
}
