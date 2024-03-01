package org.evosuite.junit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.evosuite.Properties;
import org.evosuite.junit.writer.LineIndent;
import org.evosuite.junit.writer.TestSuiteWriter;
import org.evosuite.junit.writer.TestSuiteWriterUtils;
import org.evosuite.testcase.execution.ExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.evosuite.junit.writer.TestSuiteWriterUtils.NEWLINE;
import static org.evosuite.junit.writer.TestSuiteWriterUtils.addLine;

public class TestSuite {
    private static final Logger logger = LoggerFactory.getLogger(TestSuite.class);

    // region class data
    private Package pkg;
    private SortedSet<Class<?>> imports = new TreeSet<>(Comparator.comparing(ImportHelper::classComparison));
    private SortedSet<Method> staticMethodImports = new TreeSet<>(Comparator.comparing(ImportHelper::methodComparison));
    private SortedSet<Field> staticFieldImports = new TreeSet<>(Comparator.comparing(ImportHelper::fieldComparison));
    private SortedSet<Annotation> testSuiteClassAnnotations = new TreeSet<>();
    private final String name;
    private ClassExtensions testSuiteClassExtensions = new ClassExtensions();
    private Set<ClassField> classFields = new HashSet<>();
    private Set<Test> testCases = new HashSet<>();
    // endregion

    // region formatting helper
    private final UnitTestAdapter adapter = TestSuiteWriterUtils.getAdapter();
    private LineIndent lineIndent = new LineIndent();
    // endregion

    public TestSuite(String name, ExecutionResult executionResult) {
        this(name, Collections.singletonList(executionResult));
    }

    public TestSuite(String name, Collection<ExecutionResult> executionResults) {
        this.name = name;
        this.testCases.addAll(executionResults.stream().map(Test::new).collect(Collectors.toList()));
    }

    public String toCode(){
        return toCode(lineIndent);
    }

    public String toCode(LineIndent lineIndent) {
        this.lineIndent = lineIndent;
        StringBuilder stringBuilder = new StringBuilder();

        addHeader(stringBuilder);
        addPackage(stringBuilder);
        addImports(stringBuilder);
        addTestSuiteClass(stringBuilder);

        return stringBuilder.toString();
    }

    private void addHeader(StringBuilder stringBuilder) {
        TestSuiteWriter.addEvoSuiteHeader(stringBuilder);
    }

    private void addPackage(StringBuilder stringBuilder) {
        logger.warn("Should use package instead of Properties.CLASS_PREFIX");
//        stringBuilder.append("package ").append(pkg.getName()).append(";").append(NEWLINE);
        stringBuilder.append("package ").append(Properties.CLASS_PREFIX).append(";").append(NEWLINE).append(NEWLINE);
    }

    private void addImports(StringBuilder stringBuilder) {
        // normal imports
        if(!imports.isEmpty()){
            for (String imprt : imports.stream().map(ImportHelper::classComparison).collect(Collectors.toList())) {
                stringBuilder.append("import ").append(imprt).append(";").append(NEWLINE);
            }
            stringBuilder.append(NEWLINE);
        }

        // static imports
        Set<String> staticImports = new TreeSet<>(Comparator.comparing(String::toString));
        staticImports.addAll(staticMethodImports.stream().map(ImportHelper::methodComparison).collect(Collectors.toList()));
        staticImports.addAll(staticFieldImports.stream().map(ImportHelper::fieldComparison).collect(Collectors.toList()));

        if(!staticImports.isEmpty()){
            for (String imprt : staticImports) {
                stringBuilder.append("import static ").append(imprt).append(";").append(NEWLINE);
            }
            stringBuilder.append(NEWLINE);
        }
    }

    private void addTestSuiteClass(StringBuilder stringBuilder){
        addClassAnnotation(stringBuilder);
        addClassDefinition(stringBuilder);
        lineIndent.increase();
        addClassFields(stringBuilder);
        addClassMethods(stringBuilder);
        lineIndent.decrease();
        addFooter(stringBuilder);
    }

    private void addClassAnnotation(StringBuilder stringBuilder) {
        if(!testSuiteClassAnnotations.isEmpty()){
            for (Annotation annotation : testSuiteClassAnnotations){
                stringBuilder.append(annotation.toCode(lineIndent));
            }
            stringBuilder.append(NEWLINE);
        }
    }

    private void addClassDefinition(StringBuilder stringBuilder) {
        stringBuilder.append(adapter.getClassDefinition(name));
        stringBuilder.append(testSuiteClassExtensions.toCode(lineIndent));
        stringBuilder.append(" {").append(NEWLINE);
    }

    private void addClassFields(StringBuilder stringBuilder) {
        if(!classFields.isEmpty()){
            for (ClassField field : classFields){
                stringBuilder.append(field.toCode(lineIndent));
            }
            stringBuilder.append(NEWLINE);
        }
    }

    private void addClassMethods(StringBuilder stringBuilder) {
        if(!testCases.isEmpty()){
            for (Test testCase : testCases){
                stringBuilder.append(testCase.toCode(lineIndent));
            }
            stringBuilder.append(NEWLINE);
        }
    }

    private void addFooter(StringBuilder stringBuilder){
        addLine(stringBuilder, lineIndent, "}");
    }

    //region getters and setters
    public Package getPkg() {
        return pkg;
    }

    public void setPkg(Package pkg) {
        this.pkg = pkg;
    }

    public Set<Class<?>> getImports() {
        return imports;
    }

    public void setImports(Collection<Class<?>> imports) {
        this.imports.clear();
        this.imports.addAll(imports);
    }

    public Set<Method> getStaticMethodImports() {
        return staticMethodImports;
    }

    public void setStaticMethodImports(Collection<Method> staticMethodImports) {
        this.staticMethodImports.clear();
        this.staticMethodImports.addAll(staticMethodImports);
    }

    public Set<Field> getStaticFieldImports() {
        return staticFieldImports;
    }

    public void setStaticFieldImports(Collection<Field> staticFieldImports) {
        this.staticFieldImports.clear();
        this.staticFieldImports.addAll(staticFieldImports);
    }

    public Set<Annotation> getTestSuiteClassAnnotation() {
        return testSuiteClassAnnotations;
    }

    public void setTestSuiteClassAnnotation(Collection<Annotation> testSuiteClassAnnotations) {
        this.testSuiteClassAnnotations.clear();
        this.testSuiteClassAnnotations.addAll(testSuiteClassAnnotations);
    }

    public String getName() {
        return name;
    }

    public ClassExtensions getTestSuiteClassExtensions() {
        return testSuiteClassExtensions;
    }

    public void setTestSuiteClassExtensions(ClassExtensions testSuiteClassExtensions) {
        this.testSuiteClassExtensions = testSuiteClassExtensions;
    }

    public Set<ClassField> getClassFields() {
        return classFields;
    }

    public void setClassFields(Set<ClassField> classFields) {
        this.classFields = classFields;
    }

    public Set<Test> getTestCases() {
        return testCases;
    }

    public void setTestCases(Set<Test> testCases) {
        this.testCases = testCases;
    }
    //endregion


}
