package org.evosuite.testcase.statements;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.variable.VariableReferenceImpl;
import org.evosuite.utils.generic.GenericAccessibleObject;

public class DeclarationStatement extends AbstractStatement {

    private static final long serialVersionUID = 2051431241124468349L;

    private transient Object value = null;
    private transient boolean isInjection = false;

    public DeclarationStatement(TestCase tc, VariableReference retval) throws IllegalArgumentException {
        super(tc, retval);
    }

    Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        if (value != null && !retval.isAssignableFrom(value.getClass())) {
            String message = "trying to assign a value of type '" + value.getClass() + "' to a variable of type '" + retval.getType() + "'";
            throw new ClassCastException(message);
        }
        this.value = value;
        isInjection = true;
    }

    @Override
    public Statement copy(TestCase newTestCase, int offset) {
        VariableReference newRetval = new VariableReferenceImpl(newTestCase, retval.getType());
        DeclarationStatement statement = new DeclarationStatement(newTestCase, newRetval);

        // same value as the original into the new statement, not a problem as used for injection only
        statement.setValue(value);
        return statement;
    }

    @Override
    public Throwable execute(Scope scope, PrintStream out)
        throws InvocationTargetException, IllegalArgumentException, IllegalAccessException, InstantiationException {
        return super.exceptionHandler(new Executer() {
            @Override
            public void execute() throws CodeUnderTestException {
                // no operation, except for setting the value if not null
                if (value != null) {
                    retval.setObject(scope, value);
                }
            }
        });
    }

    @Override
    public GenericAccessibleObject<?> getAccessibleObject() {
        return null;
    }

    @Override
    public List<VariableReference> getUniqueVariableReferences() {
        return new ArrayList<>(getVariableReferences());
    }

    @Override
    public Set<VariableReference> getVariableReferences() {
        Set<VariableReference> vars = new LinkedHashSet<>();
        vars.add(retval);
        return vars;
    }

    @Override
    public boolean isAssignmentStatement() {
        return false;
    }

    @Override
    public void replace(VariableReference oldVar, VariableReference newVar) {
        if (retval.equals(oldVar)) {
            retval = newVar;
        }
    }

    @Override
    public boolean same(Statement s) {
        if (this == s) {
            return true;
        }
        if (s == null) {
            return false;
        }
        if (getClass() != s.getClass()) {
            return false;
        }

        DeclarationStatement other = (DeclarationStatement) s;
        return retval.same(other.retval);
    }

    public boolean isInjection() {
        return isInjection;
    }
}
