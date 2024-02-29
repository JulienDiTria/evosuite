package org.evosuite.testsuite;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.evosuite.Properties;
import org.evosuite.testcase.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.evosuite.junit.writer.TestSuiteWriterUtils.NEWLINE;

public class TestSuite {
    private static final Logger logger = LoggerFactory.getLogger(TestSuite.class);

    private Package pkg;
    private Set<Class<?>> imports = new TreeSet<>(Comparator.comparing(TestSuite::classComparison));
    private Set<Method> staticMethodImports = new TreeSet<>(Comparator.comparing(TestSuite::methodComparison));
    private Set<Field> staticFieldImports = new TreeSet<>(Comparator.comparing(TestSuite::fieldComparison));
    private TestSuiteClassAnnotation testSuiteClassAnnotation;
    private String name;
    private TestSuiteClassExtension testSuiteClassExtension;
    private Set<Field> classFields = new HashSet<>();
    private Set<TestCase> testCases = new HashSet<>();

    public TestSuite() {

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        addHeader(stringBuilder);
        addPackage(stringBuilder);
        addImports(stringBuilder);

        return stringBuilder.toString();
    }

    private void addHeader(StringBuilder stringBuilder) {

    }

    private void addPackage(StringBuilder stringBuilder) {
        stringBuilder.append("package ").append(pkg.getName());
        stringBuilder.append(";").append(NEWLINE);
    }

    private void addImports(StringBuilder stringBuilder) {
        // normal imports
        if(!imports.isEmpty()){
            for (String imprt : imports.stream().map(TestSuite::classComparison).collect(Collectors.toList())) {
                stringBuilder.append("import ").append(imprt);
                stringBuilder.append(";").append(NEWLINE);
            }
            stringBuilder.append(NEWLINE);
        }

        // static imports
        Set<String> staticImports = new TreeSet<>(Comparator.comparing(String::toString));
        staticImports.addAll(staticMethodImports.stream().map(TestSuite::methodComparison).collect(Collectors.toList()));
        staticImports.addAll(staticFieldImports.stream().map(TestSuite::fieldComparison).collect(Collectors.toList()));

        if(!staticImports.isEmpty()){
            for (String imprt : staticImports) {
                stringBuilder.append("import static ");
                stringBuilder.append(imprt);
                stringBuilder.append(";");
                stringBuilder.append(NEWLINE);
            }
            stringBuilder.append(NEWLINE);
        }
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

    public void setImports(Set<Class<?>> imports) {
        this.imports = imports;
    }

    public Set<Method> getStaticMethodImports() {
        return staticMethodImports;
    }

    public void setStaticMethodImports(Set<Method> staticMethodImports) {
        this.staticMethodImports = staticMethodImports;
    }

    public Set<Field> getStaticFieldImports() {
        return staticFieldImports;
    }

    public void setStaticFieldImports(Set<Field> staticFieldImports) {
        this.staticFieldImports = staticFieldImports;
    }

    public TestSuiteClassAnnotation getTestSuiteClassAnnotation() {
        return testSuiteClassAnnotation;
    }

    public void setTestSuiteClassAnnotation(TestSuiteClassAnnotation testSuiteClassAnnotation) {
        this.testSuiteClassAnnotation = testSuiteClassAnnotation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TestSuiteClassExtension getTestSuiteClassExtension() {
        return testSuiteClassExtension;
    }

    public void setTestSuiteClassExtension(TestSuiteClassExtension testSuiteClassExtension) {
        this.testSuiteClassExtension = testSuiteClassExtension;
    }

    public Set<Field> getClassFields() {
        return classFields;
    }

    public void setClassFields(Set<Field> classFields) {
        this.classFields = classFields;
    }

    public Set<TestCase> getTestCases() {
        return testCases;
    }

    public void setTestCases(Set<TestCase> testCases) {
        this.testCases = testCases;
    }
    //endregion

    //region comparators

    private static String classComparison(Class<?> klass){
        return klass.getCanonicalName();
    }

    private static String methodComparison(Method method){
        return method.getDeclaringClass().getTypeName() + '.' + method.getName();
    }

    private static String fieldComparison(Field field){
        return field.getDeclaringClass().getTypeName() + '.' + field.getName();
    }

    //endregion
}
