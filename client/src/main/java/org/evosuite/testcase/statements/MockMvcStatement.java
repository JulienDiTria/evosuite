package org.evosuite.testcase.statements;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.variable.VariableReferenceImpl;
import org.evosuite.utils.generic.GenericMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

public class MockMvcStatement extends MethodStatement {

    protected static final Logger logger = LoggerFactory.getLogger(MockMvcStatement.class);

    private final MockMvc mockMvc;

    public static MockMvcStatement builder(MockMvc mockMvc, TestCase tc, List<VariableReference> parameters, VariableReference retVal) {
        if (mockMvc == null) {
            throw new IllegalArgumentException("MockMvc cannot be null");
        }

        Method performMethod;
        try {
            performMethod = MockMvc.class.getMethod("perform", RequestBuilder.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        GenericMethod method = new GenericMethod(performMethod, MockMvc.class);
        VariableReference callee = new VariableReferenceImpl(tc, MockMvc.class);

        return new MockMvcStatement(mockMvc, tc, method, callee, parameters, retVal);
    }

    private MockMvcStatement(MockMvc mockMvc, TestCase tc, GenericMethod method,
        VariableReference callee, List<VariableReference> parameters, VariableReference retVal)
        throws IllegalArgumentException {
        super(tc, method, callee, parameters, retVal);
        this.mockMvc = mockMvc;
    }

    @Override
    public Throwable execute(final Scope scope, PrintStream out)
        throws InvocationTargetException, IllegalArgumentException,
        IllegalAccessException, InstantiationException {
        logger.trace("Executing method " + method.getName());
        Throwable exceptionThrown = null;

        try {
            return super.exceptionHandler(new MethodStatementExecuter(mockMvc, null, scope));
        } catch (InvocationTargetException e) {
            exceptionThrown = e.getCause();
            logger.debug("Exception thrown in method {}: {}", method.getName(),
                exceptionThrown);
        }
        return exceptionThrown;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {

        retval.getStPosition();
        for (VariableReference v : parameters) {
            v.getStPosition();
        }
        return true;
    }
}
