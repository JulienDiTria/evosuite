package org.evosuite.testcase.statements;

import com.examples.with.different.packagename.spring.petclinic.owner.OwnerController;
import java.util.ArrayList;
import java.util.List;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.spring.SmockRequestBuilder;
import org.evosuite.spring.SmockResultActions;
import org.evosuite.spring.SpringSupport;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.variable.VariableReferenceImpl;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

public class MockMvcStatementTest {

    static MockMvc mockMvc;

    @BeforeClass
    public static void init() {
        SpringSupport.setup(OwnerController.class.getName());
        mockMvc = SpringSupport.getMockMvc();
    }

    @Test
    public void testExecute() throws Exception {
        TestCase tc = new DefaultTestCase();
        VariableReference requestBuilder = SmockRequestBuilder.createGetRequestBuilder(tc, "/owners");

        List<VariableReference> parameters = new ArrayList<>();
        parameters.add(requestBuilder);
        VariableReference resultActions = new VariableReferenceImpl(tc, ResultActions.class);
        MockMvcStatement statement = MockMvcStatement.builder(mockMvc, tc, parameters, resultActions);
        tc.addStatement(statement);

        VariableReference mvcResult = SmockResultActions.smockAndReturn(tc, resultActions);

        TestChromosome c = new TestChromosome();
        c.setTestCase(tc);
        ExecutionResult executionResult = c.executeForFitnessFunction(new BranchCoverageSuiteFitness());

        System.out.println("test executed in " + executionResult.getExecutionTime() + "ms");
        System.err.println("exception: " + executionResult.getAllThrownExceptions());
        System.out.println(tc.toCode());

        /*
        Object[] objectArray0 = new Object[1];
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder0 = MockMvcRequestBuilders.get("/owners", objectArray0);
        ResultActions resultActions0 = mockMvc0.perform(mockHttpServletRequestBuilder0);
        resultActions0.andReturn();
         */
    }

}
