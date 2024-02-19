package org.evosuite.testcase.statements;

import com.examples.with.different.packagename.spring.petclinic.owner.OwnerController;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.spring.SmockRequestBuilder;
import org.evosuite.spring.SpringSetup;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.variable.VariableReferenceImpl;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class MockMvcStatementTest {

    static MockMvc mockMvc;

    @BeforeClass
    public static void init() {
        SpringSetup.setup(OwnerController.class.getName());
        mockMvc = SpringSetup.getMockMvc();
    }

    @Test
    public void testExecute() throws Exception {
        TestCase tc = new DefaultTestCase();
        VariableReference requestBuilder = SmockRequestBuilder.createGetRequestBuilder(tc, "/owners");

        List<VariableReference> parameters = new ArrayList<>();
        parameters.add(requestBuilder);
        VariableReference retVal = new VariableReferenceImpl(tc, ResultActions.class);
        MockMvcStatement statement = MockMvcStatement.builder(mockMvc, tc, parameters, retVal);
        tc.addStatement(statement);

        TestChromosome c = new TestChromosome();
        c.setTestCase(tc);
        ExecutionResult executionResult = c.executeForFitnessFunction(new BranchCoverageSuiteFitness());

        System.out.println("test executed in " + executionResult.getExecutionTime() + "ms");
        System.err.println("exception: " + executionResult.getAllThrownExceptions());
        System.out.println(tc.toCode());
    }

}
