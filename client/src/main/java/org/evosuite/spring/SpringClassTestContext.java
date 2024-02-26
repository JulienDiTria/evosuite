package org.evosuite.spring;

import java.lang.reflect.Constructor;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.repository.Repository;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

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
            if (Repository.class.isAssignableFrom(parameterType)) {
                mockBeans.add(parameterType);
            }
        }
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
        addTestSuiteAnnotations(sb);
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
     * Add the test suite annotations to the content (content is hold in the string builder)
     *
     * @param sb the string builder to be used
     */
    private void addTestSuiteAnnotations(StringBuilder sb) {
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
    private void addFields(StringBuilder sb) {
        sb.append("    @Autowired");
        sb.append(NEWLINE);
        sb.append("    private MockMvc mockMvc;");
        sb.append(NEWLINE);
        sb.append(NEWLINE);

        for (Class<?> mockBean : mockBeans) {
            String name = mockBean.getSimpleName();
            name = name.substring(0, 1).toLowerCase() + name.substring(1);
            sb.append("    @MockBean");
            sb.append(NEWLINE);
            sb.append("    private ");
            sb.append(mockBean.getSimpleName());
            sb.append(" ");
            sb.append(name);
            sb.append(";");
            sb.append(NEWLINE);
        }

        if (!mockBeans.isEmpty()) {
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
}
