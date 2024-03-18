package org.evosuite.spring;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.evosuite.junit.ClassField;
import org.evosuite.junit.EvoAnnotation;
import org.evosuite.utils.JavaCompilerUtils;
import org.evosuite.utils.Randomness;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

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
                logger.warn("setup - creating simple test suite for '{}'", className);

                // create simple test for spring controller
                Class<?> simpleTestSuite = createSimpleTestSuite(className);
                
                logger.warn("setup - class loaded '{}'", simpleTestSuite.getName());

                // execute the test with spring runner
                setupSpringRunner(simpleTestSuite);
                logger.warn("setup - spring runner loaded");

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
    private static Class<?> createSimpleTestSuite(Object controller) throws IOException {
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
        JavaCompilerUtils compiler = new JavaCompilerUtils();
        boolean compilationOk = compiler.compile(Collections.singletonList(testFile));
        if (!compilationOk) {
            logger.warn("createSimpleTestSuite - Compilation failed while trying to compile the file '{}' containing the following code:\n{}",
                testFile.getPath(), testSuiteContent);
            throw new RuntimeException("Compilation failed for file " + testFile.getPath());
        }

        // move file into classpath
        // TODO 2023.03.04 Julien Di Tria : use the test directory instead of java source directory
        String folderPath = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
        File folder = new File(folderPath, packagePath);
        folder.mkdirs();
        folder.deleteOnExit();
        File compiledFileInClasspath = new File(folder, testSuiteName + ".class");
        compiledFileInClasspath.deleteOnExit();
        logger.warn("createSimpleTestSuite - Moving file '{}' to '{}'", compiledFile.getPath(), compiledFileInClasspath.getPath());
        Files.move(compiledFile.toPath(), compiledFileInClasspath.toPath(), REPLACE_EXISTING);

        // load class
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            logger.warn("createSimpleTestSuite - Could not load className '{}' created from file '{}'", className, compiledFileInClasspath.getPath());
            throw new RuntimeException(e);
        }
    }

    private static int compile(File toCompile) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        OutputStream out = new ByteArrayOutputStream();
        OutputStream err = new ByteArrayOutputStream();
        int result = compiler.run(null, out, err, toCompile.getPath());
        if (result != 0) {
            logger.warn("Compilation failed with exit code '{}' while trying to compile the file '{}'",
                result, toCompile.getPath());
            logger.warn("Output: {}", out);
            logger.warn("Error: {}", err);
        }
        return result;
    }

    /**
     * Setup the Spring context for the given controller.
     *
     * @param controller the controller for which to setup the Spring context
     */
    private static void setupSpringRunner(Object controller) {
        Class<?> clazz = getClassForObject(controller);

        try {
            instance.springSetupRunner = new SpringSetupRunner(clazz);
        } catch (InitializationError e) {
            logger.error("setupSpringRunner - could not initialize SpringSetupRunner for class '{}'", clazz.getName());
            throw new RuntimeException(e);
        }

        logger.warn("setupSpringRunner - object springSetupRunner {}", instance.springSetupRunner);

        try {
            CountDownLatch latch = new CountDownLatch(1);
            RunNotifier runNotifier = new RunNotifier();
            SpringRunListener listener = new SpringRunListener(latch);
            runNotifier.addListener(listener);
            instance.springSetupRunner.run(runNotifier);
            boolean noTimeout = latch.await(10, TimeUnit.SECONDS);
            if (!noTimeout) {
                logger.warn("setupSpringRunner - timeout");
                throw new RuntimeException("Timeout while setting up Spring context for class " + clazz.getName());
            }
            else {
                logger.info("setupSpringRunner - done");
            }
        } catch (InterruptedException e) {
            logger.error("setupSpringRunner - interrupted", e);
            throw new RuntimeException(e);
        }

        instance.mockMvc = instance.springSetupRunner.mockMvc;
        instance.handlerMethods = instance.springSetupRunner.handlerMethods;
        if (instance.mockMvc == null) {
            logger.error("setupSpringRunner - MockMvc is null for class '{}'", clazz.getName());
            throw new RuntimeException("MockMvc is null for class " + clazz.getName());
        }
        logger.warn("setupSpringRunner - MockMvc: {}", instance.mockMvc);
        logger.warn("setupSpringRunner - done");
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
    public static Map.Entry<RequestMappingInfo, HandlerMethod> getRandomRequestMappingInfo() {
        if (instance.handlerMethods != null) {

            Set<Map.Entry<RequestMappingInfo,HandlerMethod>> kvs_requestMappingInfo_handler = instance.handlerMethods.entrySet();
            kvs_requestMappingInfo_handler = kvs_requestMappingInfo_handler.stream()
                .filter(kv -> !kv.getKey().getMethodsCondition().getMethods().isEmpty())
                .collect(Collectors.toSet());
            if (!kvs_requestMappingInfo_handler.isEmpty()) {
                return Randomness.choice(kvs_requestMappingInfo_handler);
            }
        }

        HashMap<RequestMappingInfo, HandlerMethod> singleEntry = new HashMap<>();
        singleEntry.put(RequestMappingHandlerMapping.getRequestMappingInfo(), null);
        return singleEntry.entrySet().iterator().next();
    }

    public static void setMockMvc(MockMvc mockMvc) {
        instance.mockMvc = mockMvc;
    }

    public static Collection<Class<?>> getImports() {
        if (instance.springClassTestContext == null) {
            logger.warn("No springClassTestContext found to getImports, likely to not be a spring controller.");
            return new ArrayList<>();
        }
        return instance.springClassTestContext.getImports();
    }

    /**
     * Add a spring runner to the given string builder when generating a testSuite into a string to write to file.
     *
     * @param builder the string builder to be used
     */
    public static void addRunner(StringBuilder builder){
        if (instance.springClassTestContext != null) {
            instance.springClassTestContext.addRunner(builder);
        }
        else {
            logger.warn("No springClassTestContext found to add runner to the test suite");
        }
    }

    public static void addFields(StringBuilder builder) {
        if (instance.springClassTestContext != null) {
            instance.springClassTestContext.addFields(builder);
        }
        else {
            logger.warn("addFields : No springClassTestContext found to add fields to the test suite");
        }
    }

    public static List<EvoAnnotation> getClassAnnotations() {
        if (instance.springClassTestContext != null) {
            return instance.springClassTestContext.getClassAnnotations();
        }
        else {
            logger.warn("getClassAnnotations : No springClassTestContext found to add class level annotation to the test suite");
            return Collections.emptyList();
        }
    }

    public static Collection<ClassField> getClassFields() {
        if (instance.springClassTestContext != null) {
            return instance.springClassTestContext.getClassFields();
        }
        else {
            logger.warn("getClassFields : No springClassTestContext found to add class level fields to the test suite");
            return Collections.emptyList();
        }
    }
}
