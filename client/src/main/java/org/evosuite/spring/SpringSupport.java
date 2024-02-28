package org.evosuite.spring;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.evosuite.utils.Randomness;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Global class to set up the Spring context for a given controller if needed. It is used to
 * <li>process the candidate controller to see if it's a spring controller or not</li>
 * <li>analyse the controller and create/hold the mappings between MockMVC#perform and controller calls </li>
 */
public class SpringSupport {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final SpringSupport instance = new SpringSupport();
    private SpringSetupRunner springSetupRunner;
    private MockMvc mockMvc;
    private Map<RequestMappingInfo, HandlerMethod> handlerMethods;
    private SpringClassTestContext springClassTestContext;

    private SpringSupport() {
    }

    private static boolean isHandlerType(Object controller) {
        return RequestMappingHandlerMapping.isHandlerType(controller);
    }

    /**
     * Check if the class is a Spring Controller. If so, analyse it, generate a simple test suite, and execute it to set up the Spring
     * context for the controller.
     * Does nothing otherwise.
     *
     * @param className the name of the class to check
     */
    public static void setup(String className) {
        if (isHandlerType(className)) {
            try {
                // create empty test for spring controller
                Class<?> emptyTestClass = createEmptyTest(className);

                // execute the test with spring runner
                setupSpringRunner(emptyTestClass);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Create a basic test suite for the given controller using the @RunWith(SpringRunner.class) and @WebMvcTest(controller.class)
     * annotations and load it.
     * <p>
     * TODO 16.07.2021 Julien Di Tria:
     *  This methods works while running the tests, but unlikely to work in the real from a Jar application, as there is a bypass of the
     *  classloader by moving the newly compiled file into a "known" path that the classloader will be able to load from.
     * <p>
     *
     * @param controller the controller for which to create the test suite
     * @return a test suite loaded into the classpath
     * @throws IOException if an I/O error occurs,
     */
    private static Class<?> createEmptyTest(Object controller) throws IOException {
        Class<?> clazz = getClassForObject(controller);
        ClassLoader classLoader = logger.getClass().getClassLoader();

        // analyze the controller to create a suitable empty test class
        instance.springClassTestContext = new SpringClassTestContext(clazz);
        String testSuiteContent = instance.springClassTestContext.createTestSuiteContent();

        // create empty test folder
        File tmpRoot = File.createTempFile("EvoSpring" + System.currentTimeMillis(), "");
        tmpRoot.delete();
        tmpRoot.mkdirs();
        tmpRoot.deleteOnExit();

        // create the files for source code and compiled class
        String packageName = clazz.getPackage().getName();
        String packagePath = packageName.replace(".", File.separator);
        String testSuiteName = instance.springClassTestContext.getTestSuiteName();
        String className = packageName + "." + testSuiteName;
        File testFile = new File(tmpRoot, testSuiteName + ".java");
        File compiledFile = new File(tmpRoot, testSuiteName + ".class");
        testFile.deleteOnExit();
        testFile.createNewFile();
        compiledFile.deleteOnExit();

        // write the test suite to the file
        Files.write(testFile.toPath(), testSuiteContent.getBytes());

        // compile the class
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        OutputStream out = new ByteArrayOutputStream();
        OutputStream err = new ByteArrayOutputStream();
        int result = compiler.run(null, out, err, testFile.getPath());
        if (result != 0) {
            logger.warn("Compilation failed with exit code '{}' while trying to compile the file '{}' containing the following code:\n{}",
                result, testFile.getPath(), testSuiteContent);
            logger.warn("Output: {}", out);
            logger.warn("Error: {}", err);
            throw new RuntimeException("Compilation failed with exit code " + result);
        }

        // move file into classpath
        String folderPath = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
        File folder = new File(folderPath, packagePath);
        folder.mkdirs();
        folder.deleteOnExit();
        File compiledFileInClasspath = new File(folder, testSuiteName + ".class");
        compiledFileInClasspath.deleteOnExit();
        Files.move(compiledFile.toPath(), compiledFileInClasspath.toPath(), REPLACE_EXISTING);

        // load class
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Setup the Spring context for the given controller.
     *
     * @param controller the controller for which to setup the Spring context
     */
    private static void setupSpringRunner(Object controller) {
        Class<?> clazz = getClassForObject(controller);
        RunNotifier runNotifier = new RunNotifier();

        try {
            instance.springSetupRunner = new SpringSetupRunner(clazz);
        } catch (InitializationError e) {
            throw new RuntimeException(e);
        }

        instance.springSetupRunner.run(runNotifier);
        instance.mockMvc = instance.springSetupRunner.mockMvc;
        instance.handlerMethods = instance.springSetupRunner.handlerMethods;
        assert (instance.mockMvc != null);
        System.out.println("MockMvc: " + instance.mockMvc);
        System.out.println("SpringSetupRunner executed");
    }
    
    public static MockMvc getMockMvc() {
        return instance.mockMvc;
    }

    /**
     * Get the class for an object.
     *
     * @param object the object for which to find the class
     * @return the class of the object
     */
    public static Class<?> getClassForObject(Object object) {
        Class<?> clazz;
        try {
            clazz = (object instanceof String ? Class.forName((String) object) : object.getClass());
        } catch (ClassNotFoundException e) {
            clazz = null;
            logger.error("Class not found for: {}", object);
        }
        if (clazz == Class.class) {
            clazz = (Class<?>) object;
//            clazz = getClassForObject(((Class<?>) object).getName());
        }
        return clazz;
    }

    public static boolean hasController() {
        return instance.springSetupRunner != null;
    }

    /**
     * Get the handler methods from the handlerMethods after spring context setup is done.
     * <p>
     * If the handlerMethods instance is not set, it will return a mock RequestMappingInfo with a warning.
     *
     * @return a random RequestMappingInfo from the handler methods
     */
    public static RequestMappingInfo getRandomRequestMappingInfo() {
        if (instance.handlerMethods != null) {
            Set<RequestMappingInfo> requestMappingInfos = instance.handlerMethods.keySet();
            requestMappingInfos = requestMappingInfos.stream()
                .filter(requestMappingInfo -> !requestMappingInfo.getMethodsCondition().getMethods().isEmpty())
                .collect(Collectors.toSet());
            if (!requestMappingInfos.isEmpty()) {
                return Randomness.choice(requestMappingInfos);
            }
        }

        return RequestMappingHandlerMapping.getRequestMappingInfo();
    }

    public static void setMockMvc(MockMvc mockMvc) {
        instance.mockMvc = mockMvc;
    }

    public static Collection<Class<?>> getImports() {
        return instance.springClassTestContext.getImports();
    }

    /**
     * Add a spring runner to the given string builder when generating a testSuite into a string to write to file.
     *
     * @param builder the string builder to be used
     */
    public static void addRunner(StringBuilder builder){
        instance.springClassTestContext.addRunner(builder);
    }

    public static void addFields(StringBuilder builder) {
        instance.springClassTestContext.addFields(builder);
    }
}
