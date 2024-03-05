package org.evosuite.junit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.evosuite.Properties;
import org.evosuite.junit.writer.LineIndent;
import org.evosuite.junit.writer.TestSuiteWriter;
import org.evosuite.junit.writer.TestSuiteWriterUtils;
import org.evosuite.spring.SpringSupport;
import org.evosuite.testcase.execution.ExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.evosuite.junit.writer.TestSuiteWriterUtils.NEWLINE;
import static org.evosuite.junit.writer.TestSuiteWriterUtils.addLine;
import static org.evosuite.junit.writer.TestSuiteWriterUtils.addMultiLines;

public class TestSuite {
    private static final Logger logger = LoggerFactory.getLogger(TestSuite.class);

    // region class data
    private Package pkg;
    private final SortedSet<String> imports = new TreeSet<>();
    private final SortedSet<String> staticImports = new TreeSet<>();
    private final List<EvoAnnotation> testSuiteClassEvoAnnotations = new ArrayList<>();
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

        analyze();
    }

    private void analyze() {
        // check if using Spring runner or EvoSuite runner
        analyzeTestSuiteClassAnnotations();

        // analyze the adapter
        imports.addAll(adapter.getImports());
        staticImports.addAll(adapter.getStaticImports());

        // analyze the tests
        for (Test test : testCases) {
            imports.addAll(test.getImports());
            staticImports.addAll(test.getStaticImports());
            classFields.addAll(test.getClassFields());
        }
    }

    private void analyzeTestSuiteClassAnnotations() {
        if(testCases.stream().allMatch(test -> test.getExecutionResult().test.usesSpring())){
            testSuiteClassEvoAnnotations.addAll(SpringSupport.getClassAnnotations());
        }
        else if (TestSuiteWriterUtils.needToUseAgent() && !Properties.NO_RUNTIME_DEPENDENCY) {
            testSuiteClassEvoAnnotations.addAll(TestSuiteWriter.getEvoRunnerAnnotation());
        }
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
        StringBuilder headerBuilder = new StringBuilder();
        TestSuiteWriter.addEvoSuiteHeader(headerBuilder);
        String header = headerBuilder.toString();

        addMultiLines(stringBuilder, lineIndent, header);
    }

    private void addPackage(StringBuilder stringBuilder) {
        logger.warn("Should use package instead of Properties.CLASS_PREFIX");
//        stringBuilder.append("package ").append(pkg.getName()).append(";").append(NEWLINE);
        stringBuilder.append(lineIndent).append("package ").append(Properties.CLASS_PREFIX).append(";").append(NEWLINE).append(NEWLINE);
    }

    private void addImports(StringBuilder stringBuilder) {
        // normal imports
        if(!imports.isEmpty()){
            for (String imprt : imports) {
                stringBuilder.append(lineIndent).append("import ").append(imprt).append(";").append(NEWLINE);
            }
            stringBuilder.append(NEWLINE);
        }

        // static imports
        if(!staticImports.isEmpty()){
            for (String imprt : staticImports) {
                stringBuilder.append(lineIndent).append("import static ").append(imprt).append(";").append(NEWLINE);
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
        if(!testSuiteClassEvoAnnotations.isEmpty()){
            for (EvoAnnotation evoAnnotation : testSuiteClassEvoAnnotations){
                stringBuilder.append(evoAnnotation.toCode(lineIndent));
            }
            stringBuilder.append(NEWLINE);
        }
    }

    private void addClassDefinition(StringBuilder stringBuilder) {
        stringBuilder.append(lineIndent);
        stringBuilder.append(adapter.getClassDefinition(name));
        stringBuilder.append(testSuiteClassExtensions.toCode());
        stringBuilder.append(" {").append(NEWLINE).append(NEWLINE);
    }

    private void addClassFields(StringBuilder stringBuilder) {
        if(!classFields.isEmpty()){
            for (ClassField field : classFields){
                stringBuilder.append(field.toCode(lineIndent));
            }
            stringBuilder.append(lineIndent).append(NEWLINE);
        }
    }

    private void addClassMethods(StringBuilder stringBuilder) {
        if(!testCases.isEmpty()){
            boolean first = true;
            for (Test testCase : testCases){
                if(first){
                    first = false;
                } else {
                    addLine(stringBuilder, lineIndent, "");
                }
                stringBuilder.append(testCase.toCode(lineIndent));
            }
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

    public Set<String> getImports() {
        return imports;
    }

    public void setImports(Collection<String> imports) {
        this.imports.clear();
        this.imports.addAll(imports);
    }

    public Set<String> getStaticImports() {
        return staticImports;
    }

    public void setStaticMethodImports(Collection<String> staticImports) {
        this.staticImports.clear();
        this.staticImports.addAll(staticImports);
    }

    public List<EvoAnnotation> getTestSuiteClassAnnotation() {
        return testSuiteClassEvoAnnotations;
    }

    public void setTestSuiteClassAnnotation(List<EvoAnnotation> testSuiteClassEvoAnnotations) {
        this.testSuiteClassEvoAnnotations.clear();
        this.testSuiteClassEvoAnnotations.addAll(testSuiteClassEvoAnnotations);
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
