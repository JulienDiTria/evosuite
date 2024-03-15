package org.evosuite.spring;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.evosuite.junit.ClassField;
import org.evosuite.junit.EvoAnnotation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.repository.Repository;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.data.repository.RepositoryDefinition;

public class SpringClassTestContext {

    private static final Logger logger = LoggerFactory.getLogger(SpringClassTestContext.class);

    /**
     * The new line character
     */
    private static final String NEWLINE = System.lineSeparator();

    /**
     * The suffix to be added to the test suite name
     */
    private static final String SUFFIX = "_Spring_ESTest";

    /**
     * The class analyzed by the test context
     */
    private final Class<?> klass;

    /**
     * The name of the test suite
     */
    private final String testSuiteName;

    /**
     * The set of mock beans to be used in the test context
     */
    private final Set<Class<?>> mockBeans;

    /**
     * The set of imports to be used in the test context
     */
    private final SortedSet<Class<?>> imports;

    /**
     * Constructor, runs the analysis of the class directly
     *
     * @param klass the class to be analyzed (should be a Spring Controller)
     */
    public SpringClassTestContext(Class<?> klass) {
        this.klass = klass;
        mockBeans = new HashSet<>();
        imports = new TreeSet<>(Comparator.comparing(Class::getCanonicalName));
        testSuiteName = klass.getSimpleName() + SUFFIX;

        setup();
        analyzeClass();
    }

    /**
     * Set up some basic imports
     */
    private void setup() {
        // setup imports with basic imports needed for Spring
        imports.add(Test.class);
        imports.add(RunWith.class);
        imports.add(Autowired.class);
        imports.add(WebMvcTest.class);
        imports.add(SpringRunner.class);
        imports.add(MockBean.class);
        imports.add(MockMvc.class);
    }

    /**
     * Analyze the class to find the mock beans
     */
    private void analyzeClass() {

        // Analyze the constructors
        Constructor<?>[] constructors = klass.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            analyzeConstructor(constructor);
        }

        // TODO 13.03.2023 Julien Di Tria: Add all fields with @Autowired annotation to the imports

        // Add all mock beans to the imports, only if package is different
        Package pkg = klass.getPackage();
        mockBeans.stream().filter(mockBean -> !mockBean.getPackage().equals(pkg)).forEach(imports::add);
    }

    /**
     * Analyze a constructor to find the mock beans
     *
     * @param constructor the constructor to be analyzed
     */
    private void analyzeConstructor(Constructor<?> constructor) {
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        for (Class<?> parameterType : parameterTypes) {
            // check if the parameter is a bean that can come from spring using MockBean
            if (isRepository(parameterType)) {
                mockBeans.add(parameterType);
            }
        }
    }

    /** Check if the parameter is a bean that is a spring repository.
     * <p>
     * This is simplified from {@link org.springframework.data.repository.config.RepositoryComponentProvider#RepositoryComponentProvider(java.lang.Iterable, org.springframework.beans.factory.support.BeanDefinitionRegistry)}
     *
     * @param parameterType the parameter type to be checked
     * @return true if the parameter is a repository interface, false otherwise
     */
    private static boolean isRepository(Class<?> parameterType) {
        boolean isInterface = parameterType.isInterface();
        boolean isRepository = Repository.class.isAssignableFrom(parameterType);
        boolean hasRepositoryAnnotation = AnnotationUtils.findAnnotation(parameterType, RepositoryDefinition.class) != null;
        return isInterface && (isRepository || hasRepositoryAnnotation);
    }

    public List<EvoAnnotation> getClassAnnotations() {
        EvoAnnotation annotation = new EvoAnnotation("@RunWith(SpringRunner.class)");
        annotation.add("@WebMvcTest(" + klass.getSimpleName() + ".class)");

        return new ArrayList<>(Collections.singletonList(annotation));
    }

    public List<ClassField> getClassFields() {
        List<ClassField> fields = new ArrayList<>();
        fields.add(new ClassField(MockMvc.class, Collections.singletonList(new EvoAnnotation("@Autowired"))));
        fields.addAll(mockBeans.stream()
            .map(mockBean -> new ClassField(mockBean,Collections.singletonList(new EvoAnnotation("@MockBean"))))
            .collect(Collectors.toList()));
        return fields;
    }

    /**
     * Create the content of the test suite, including package, imports, annotations, class definition, fields and an empty test method
     *
     * @return the content of the test suite
     */
    public String createTestSuiteContent() {
        StringBuilder sb = new StringBuilder();

        addPackage(sb);
        addImports(sb);
        addRunner(sb);
        addTestSuiteDefinition(sb);

        return sb.toString();
    }

    /**
     * Add the package to the content (content is hold in the string builder)
     *
     * @param sb the string builder to be used
     */
    private void addPackage(StringBuilder sb) {
        sb.append("package ");
        sb.append(klass.getPackage().getName());
        sb.append(";");
        sb.append(NEWLINE);
        sb.append(NEWLINE);
    }

    /**
     * Add the imports to the content (content is hold in the string builder)
     *
     * @param sb the string builder to be used
     */
    private void addImports(StringBuilder sb) {
        for (Class<?> im : imports) {
            sb.append("import ");
            sb.append(im.getCanonicalName());
            sb.append(";");
            sb.append(NEWLINE);
        }

        sb.append(NEWLINE);
    }

    /**
     * Add the test suite annotations to the content to run the test with SpringRunner and WebMvcTest
     *
     * @param sb the string builder to be used
     */
    void addRunner(StringBuilder sb) {
        sb.append("@RunWith(SpringRunner.class)");
        sb.append(NEWLINE);
        sb.append("@WebMvcTest(");
        sb.append(klass.getSimpleName());
        sb.append(".class)");
        sb.append(NEWLINE);
    }

    /**
     * Add the test suite definition to the content (content is hold in the string builder)
     *
     * @param sb the string builder to be used
     */
    private void addTestSuiteDefinition(StringBuilder sb) {
        // add class definition
        sb.append("public class ");
        sb.append(testSuiteName);
        sb.append(" {");
        sb.append(NEWLINE);
        sb.append(NEWLINE);

        addFields(sb);
        addSimpleTestMethod(sb);

        sb.append("}");
    }

    /**
     * Add the fields (autowired and mockbeans) to the content (content is hold in the string builder)
     *
     * @param sb the string builder to be used
     */
    void addFields(StringBuilder sb) {
        sb.append("    @Autowired").append(NEWLINE);
        sb.append("    private MockMvc mockMvc0;").append(NEWLINE);
        sb.append(NEWLINE);

        for (Class<?> mockBean : mockBeans) {
            String name = mockBean.getSimpleName();
            name = name.substring(0, 1).toLowerCase() + name.substring(1);
            sb.append("    @MockBean").append(NEWLINE);
            sb.append("    private ").append(mockBean.getSimpleName()).append(" ").append(name).append(";").append(NEWLINE);
            sb.append(NEWLINE);
        }
    }

    /**
     * Add a simple test method to the content (content is hold in the string builder)
     *
     * @param sb the string builder to be used
     */
    private void addSimpleTestMethod(StringBuilder sb) {
        sb.append("    @Test");
        sb.append(NEWLINE);
        sb.append("    public void emptyTestOK() {");
        sb.append(NEWLINE);
        sb.append("        System.out.println(\"emptyTestOK\");");
        sb.append(NEWLINE);
        sb.append("        assert(true);");
        sb.append(NEWLINE);
        sb.append("    }");
        sb.append(NEWLINE);
    }

    /**
     * Get the name of the test suite
     *
     * @return the name of the test suite
     */
    public String getTestSuiteName() {
        return testSuiteName;
    }

    /**
     * Get the set of imports
     *
     * @return the set of imports
     */
    public Set<Class<?>> getImports() {
        return imports;
    }

}
