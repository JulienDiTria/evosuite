package org.evosuite.junit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.evosuite.Properties;
import org.evosuite.junit.writer.LineIndent;
import org.evosuite.junit.writer.Scaffolding;
import org.evosuite.junit.writer.TestSuiteWriterUtils;
import org.evosuite.testcase.TestCodeVisitor;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.ExecutionResult;

import static org.evosuite.junit.writer.TestSuiteWriterUtils.BLOCK_SPACE;
import static org.evosuite.junit.writer.TestSuiteWriterUtils.INNER_BLOCK_SPACE;
import static org.evosuite.junit.writer.TestSuiteWriterUtils.INNER_INNER_BLOCK_SPACE;
import static org.evosuite.junit.writer.TestSuiteWriterUtils.INNER_INNER_INNER_BLOCK_SPACE;
import static org.evosuite.junit.writer.TestSuiteWriterUtils.NEWLINE;
import static org.evosuite.junit.writer.TestSuiteWriterUtils.addLine;
import static org.evosuite.junit.writer.TestSuiteWriterUtils.addMultiLines;

public class Test {

    // region data for constructing
    private final ExecutionResult executionResult;
    // endregion

    // region helpers
    private Package pkg;
    private Set<Class<?>> imports = new TreeSet<>(Comparator.comparing(ImportHelper::classComparison));
    private Set<Method> staticMethodImports = new TreeSet<>(Comparator.comparing(ImportHelper::methodComparison));
    private Set<Field> staticFieldImports = new TreeSet<>(Comparator.comparing(ImportHelper::fieldComparison));
    // endregion

    // region formatting helper
    private final TestCodeVisitor visitor = new TestCodeVisitor();
    private final UnitTestAdapter adapter = TestSuiteWriterUtils.getAdapter();
    private LineIndent lineIndent;
    // endregion

    // region class data
    private List<String> comments = new ArrayList<>();
    private SortedSet<MethodAnnotation> testAnnotation = new TreeSet<>();
    private String name;
    // endregion

    public Test(ExecutionResult executionResult){
        this(executionResult, new LineIndent(), "test");
    }

    public Test(ExecutionResult executionResult, LineIndent lineIndent, String name){
        this.executionResult = executionResult;
        this.name = name;
    }

    public String toCode() {
        return toCode(new LineIndent());
    }

    public String toCode(LineIndent lineIndent){
        this.lineIndent = lineIndent;

        StringBuilder stringBuilder = new StringBuilder();

        addComments(stringBuilder);
        addAnnotations(stringBuilder);
        addMethod(stringBuilder);

        return stringBuilder.toString();
    }

    private void addComments(StringBuilder stringBuilder) {
        for (String comment : comments) {
            stringBuilder.append(lineIndent).append("// ").append(comment).append(NEWLINE);
        }
    }

    private void addAnnotations(StringBuilder stringBuilder) {
        for (MethodAnnotation annotation : testAnnotation) {
            addLine(stringBuilder, lineIndent, annotation.toCode());
        }
    }

    private void addMethod(StringBuilder stringBuilder) {
        addMethodDefinition(stringBuilder);
        lineIndent.increase();
        addBody(stringBuilder);
        lineIndent.decrease();
        addFooter(stringBuilder);
    }

    private void addMethodDefinition(StringBuilder stringBuilder) {
        String methodDefinitionBuilder = adapter.getMethodDefinition(name) + " throws Throwable {" + NEWLINE;
        addMultiLines(stringBuilder, lineIndent, methodDefinitionBuilder);
    }

    private void addBody(StringBuilder stringBuilder) {
        chopTestToFirstException();
        addPreSecurityException(stringBuilder);
        addTest(stringBuilder);
        addPostSecurityException(stringBuilder);
    }

    private void addPreSecurityException(StringBuilder stringBuilder) {
        if (executionResult.hasSecurityException()) {
            lineIndent.increase();
            stringBuilder.append(lineIndent);
            stringBuilder.append("Future<?> future = " + Scaffolding.EXECUTOR_SERVICE
                + ".submit(new Runnable(){ ");
            stringBuilder.append(NEWLINE);

            lineIndent.increase(4);
            stringBuilder.append(lineIndent);
            stringBuilder.append("@Override public void run() { ");
            stringBuilder.append(NEWLINE);
            Set<Class<?>> exceptions = executionResult.test.getDeclaredExceptions();
            if (!exceptions.isEmpty()) {
                lineIndent.increase();
                stringBuilder.append(lineIndent);
                stringBuilder.append("try {");
                stringBuilder.append(NEWLINE);
            }
            lineIndent.increase();
        }
    }

    private void addTest(StringBuilder stringBuilder) {
        for (String line : adapter.getTestString(0, executionResult.test, executionResult.exposeExceptionMapping(), visitor).split("\\r"
            + "?\\n")) {
            stringBuilder.append(lineIndent);
            stringBuilder.append(line);
            stringBuilder.append(NEWLINE);
        }
    }

    private void addPostSecurityException(StringBuilder stringBuilder) {
        if (executionResult.hasSecurityException()) {
            Set<Class<?>> exceptions = executionResult.test.getDeclaredExceptions();
            if (!exceptions.isEmpty()) {
                stringBuilder.append(lineIndent.decrease());
                stringBuilder.append("} catch(Throwable t) {");
                stringBuilder.append(NEWLINE);

                stringBuilder.append(lineIndent.increase());
                stringBuilder.append("// Need to catch declared exceptions");
                stringBuilder.append(NEWLINE);

                stringBuilder.append(lineIndent.decrease());
                stringBuilder.append("}");
                stringBuilder.append(NEWLINE);
            }

            stringBuilder.append(lineIndent.decrease());
            stringBuilder.append("}"); //closing run(){
            stringBuilder.append(NEWLINE);

            stringBuilder.append(lineIndent.decrease());
            stringBuilder.append("});"); //closing submit
            stringBuilder.append(NEWLINE);

            long time = Properties.TIMEOUT + 1000; // we add one second just to be sure, that to avoid issues with test cases taking exactly TIMEOUT ms
            stringBuilder.append(lineIndent);
            stringBuilder.append("future.get(").append(time).append(", TimeUnit.MILLISECONDS);");
            stringBuilder.append(NEWLINE);
        }
    }

    private void addFooter(StringBuilder stringBuilder) {
        stringBuilder.append(lineIndent).append("}").append(NEWLINE);
    }

    // region helper to prepare test
    private void chopTestToFirstException() {
        Integer pos = executionResult.getFirstPositionOfThrownException();
        if (pos != null) {
            if (executionResult.getExceptionThrownAtPosition(pos) instanceof CodeUnderTestException) {
                executionResult.test.chop(pos);
            } else {
                executionResult.test.chop(pos + 1);
            }
        }
    }
    // endregion

    // region getters and setters
    public LineIndent getLineIndent() {
        return lineIndent;
    }

    public void setLineIndent(LineIndent lineIndent) {
        this.lineIndent = lineIndent;
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }

    public SortedSet<MethodAnnotation> getTestAnnotation() {
        return testAnnotation;
    }

    public void setTestAnnotation(SortedSet<MethodAnnotation> testAnnotation) {
        this.testAnnotation = testAnnotation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    // end region
}
