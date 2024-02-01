package org.evosuite.spring;

import org.evosuite.Properties;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.junit.Test;
import org.springframework.web.bind.annotation.RequestMapping;
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
    System.out.println(executionResult.getExecutionTime());
  }
}
