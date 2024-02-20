package org.evosuite.testcase.statements;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.generic.GenericAccessibleObject;

public class DeclarationStatement extends AbstractStatement {

    protected DeclarationStatement(TestCase tc, VariableReference retval) throws IllegalArgumentException {
        super(tc, retval);
    }

    protected DeclarationStatement(TestCase tc, Type type) throws IllegalArgumentException {
        super(tc, type);
    }

    @Override
    public Statement copy(TestCase newTestCase, int offset) {
        VariableReference newRetval = retval.copy(newTestCase, offset);
        return new DeclarationStatement(newTestCase, newRetval);
    }

    @Override
    public Throwable execute(Scope scope, PrintStream out)
        throws InvocationTargetException, IllegalArgumentException, IllegalAccessException, InstantiationException {
        return null;
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
        // no op
    }

    @Override
    public boolean same(Statement s) {
        if (this == s)
            return true;
        if (s == null)
            return false;
        if (getClass() != s.getClass())
            return false;

        DeclarationStatement other = (DeclarationStatement) s;
        if (retval == null)
            return other.retval == null;
        else
            return retval.same(other.retval);
    }
}
