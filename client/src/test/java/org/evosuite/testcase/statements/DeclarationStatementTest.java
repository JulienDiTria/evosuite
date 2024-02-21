package org.evosuite.testcase.statements;

import com.examples.with.different.packagename.ArrayStack;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.statements.numeric.IntPrimitiveStatement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.variable.VariableReferenceImpl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DeclarationStatementTest {
    private DeclarationStatement declarationStatement;
    private TestCase testCase;
    private VariableReference retval;

    @Before
    public void setUp() {
        declarationStatement = null;
        testCase = new DefaultTestCase();
        retval = new VariableReferenceImpl(testCase, ArrayList.class);
    }

    @Test
    public void testConstructor() {
        // null null parameters
        try {
            declarationStatement = new DeclarationStatement(null, null);
            fail("Expecting exception: IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("tc cannot be null", e.getMessage());
        }

        // null non-null parameters
        try {
            declarationStatement = new DeclarationStatement(null, retval);
            fail("Expecting exception: IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("tc cannot be null", e.getMessage());
        }

        // non-null null parameters
        try {
            declarationStatement = new DeclarationStatement(testCase, null);
            fail("Expecting exception: IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("retval cannot be null", e.getMessage());
        }

        // non-null non-null parameters
        declarationStatement = new DeclarationStatement(testCase, retval);
        assertEquals(testCase, declarationStatement.getTestCase());
        assertEquals(retval, declarationStatement.getReturnValue());
        assertEquals(1, declarationStatement.getVariableReferences().size());
        assertNull(declarationStatement.getValue());

    }

    @Test
    public void testValue() {
        declarationStatement = new DeclarationStatement(testCase, retval);

        // default value is null
        assertNull(declarationStatement.getValue());

        // can set a value of same type
        ArrayList<Integer> arrayList = new ArrayList<>();
        declarationStatement.setValue(arrayList);
        assertEquals(arrayList, declarationStatement.getValue());

        // can set a value of super type
        ArrayStack arrayStack = new ArrayStack();
        declarationStatement.setValue(arrayStack);
        assertEquals(arrayStack, declarationStatement.getValue());

        // cannot set a value of sub-type
        retval = new VariableReferenceImpl(testCase, ArrayStack.class);
        declarationStatement = new DeclarationStatement(testCase, retval);
        try {
            declarationStatement.setValue(new ArrayList());
            fail("Expecting exception: ClassCastException");
        } catch (ClassCastException e) {
            assertEquals("trying to assign a value of type 'class java.util.ArrayList' to a variable of type 'class com.examples.with"
                + ".different.packagename.ArrayStack'", e.getMessage());
        }

        // cannot set a value of other type
        try {
            declarationStatement.setValue(new Object());
            fail("Expecting exception: ClassCastException");
        } catch (ClassCastException e) {
            assertEquals("trying to assign a value of type 'class java.lang.Object' to a variable of type 'class com.examples.with"
                + ".different.packagename.ArrayStack'", e.getMessage());
        }

    }

    @Test
    public void testAddToTestCase() {
        declarationStatement = new DeclarationStatement(testCase, retval);
        VariableReference variableReference = testCase.addStatement(declarationStatement);
        assertEquals(retval, variableReference);
        assertEquals(declarationStatement, testCase.getStatement(0));
    }

    @Test
    public void testExecuteNoValue() {
        declarationStatement = new DeclarationStatement(testCase, retval);

        Throwable throwable = null;
        Scope scope = new Scope();
        try {
            throwable = declarationStatement.execute(scope, null);
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
        assertNull(throwable);
    }

    @Test
    public void testExecuteWithValue() throws CodeUnderTestException {
        retval = new VariableReferenceImpl(testCase, ArrayList.class);

        declarationStatement = new DeclarationStatement(testCase, retval);
        ArrayList<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        declarationStatement.setValue(list);

        Throwable throwable = null;
        Scope scope = new Scope();
        try {
            throwable = declarationStatement.execute(scope, null);
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
        assertNull(throwable);
        assertEquals(list, retval.getObject(scope));
    }

    @Test
    public void testGetAccessibleObject() {
        declarationStatement = new DeclarationStatement(testCase, retval);

        assertNull(declarationStatement.getAccessibleObject());
    }

    @Test
    public void testGetUniqueVariableReferences() {
        declarationStatement = new DeclarationStatement(testCase, retval);

        assertNotNull(declarationStatement.getUniqueVariableReferences());
    }

    @Test
    public void testSetValue() {
        declarationStatement = new DeclarationStatement(testCase, retval);

        declarationStatement.setValue(null);
        assertNull(declarationStatement.getValue());
    }

    @Test
    public void testDeclarationStatement() {
        declarationStatement = new DeclarationStatement(testCase, retval);

        assertNull(declarationStatement.getValue());
    }

    @Test
    public void testIsAssignmentStatement() {
        declarationStatement = new DeclarationStatement(testCase, retval);

        assertFalse(declarationStatement.isAssignmentStatement());
    }

    @Test
    public void testReplace() {
        declarationStatement = new DeclarationStatement(testCase, retval);
        VariableReference newVar = new VariableReferenceImpl(testCase, ArrayList.class);
        declarationStatement.replace(retval, newVar);

        // do not simplify the asserts, we need to call equals on the VariableReference themselves
        assertTrue("should have replace retval by newVar", newVar.equals(declarationStatement.getReturnValue()));
        assertFalse("should have replace retval by newVar", retval.equals(declarationStatement.getReturnValue()));
    }

    @Test
    public void testNoReplace() {
        declarationStatement = new DeclarationStatement(testCase, retval);
        VariableReference newVar = new VariableReferenceImpl(testCase, ArrayList.class);
        VariableReference notMatchingVar = new VariableReferenceImpl(testCase, Integer.class);
        declarationStatement.replace(notMatchingVar, newVar);

        // do not simplify the asserts, we need to call equals on the VariableReference themselves
        assertFalse("should not have replace retval by newVar", newVar.equals(declarationStatement.getReturnValue()));
        assertTrue("should not have replace retval by newVar", retval.equals(declarationStatement.getReturnValue()));
    }


    @Test
    public void testSameFromTestCase() {
        declarationStatement = new DeclarationStatement(testCase, retval);
        VariableReference variableReference = testCase.addStatement(declarationStatement);

        Statement other = testCase.getStatement(0);
        assertTrue(declarationStatement.same(other));
    }

    @Test
    public void testSameFromDifferentTestCase() {
        declarationStatement = new DeclarationStatement(testCase, retval);
        testCase.addStatement(declarationStatement);

        TestCase otherTestCase = new DefaultTestCase();
        DeclarationStatement statement2 = new DeclarationStatement(otherTestCase, retval);
        otherTestCase.addStatement(statement2);

        assertTrue(declarationStatement.same(statement2));
    }

    @Test
    public void testSame() {
        declarationStatement = new DeclarationStatement(testCase, retval);
        IntPrimitiveStatement other = new IntPrimitiveStatement(testCase, 12);
        assertFalse(declarationStatement.same(other));

        assertFalse(declarationStatement.same(null));

    }

}
