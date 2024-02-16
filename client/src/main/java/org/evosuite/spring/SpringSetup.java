package org.evosuite.spring;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.nio.file.Files;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.apache.commons.io.FileUtils;
import org.evosuite.TestGenerationContext;
import org.evosuite.instrumentation.InstrumentingClassLoader;
import org.evosuite.instrumentation.NonInstrumentingClassLoader;
import org.evosuite.utils.FileIOUtils;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.web.servlet.MockMvc;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SpringSetup {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final SpringSetup instance = new SpringSetup();
    private final RequestMappingHandlerMapping requestMappingHandlerMapping = new RequestMappingHandlerMapping();
    private SpringSetupRunner springSetupRunner;

    private SpringSetup() {
    }

    /**
     * Process the candidate controller which consists of
     * <li>scanning the class for request mapping annotations</li>
     * <li>registering the handler methods</li>
     *
     * @param controller the candidate controller
     */
    public static void processCandidateController(Object controller) {
        instance.requestMappingHandlerMapping.processCandidateController(controller);
    }

    public static boolean isHandlerType(Object controller) {
        return instance.requestMappingHandlerMapping.isHandlerType(controller);
    }

    public static RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        return instance.requestMappingHandlerMapping;
    }


    public static void setup(String className) {
        processCandidateController(className);
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
     *  TODO 16.07.2021 Julien Di Tria:
     *   For now, this method uses a template of the test class, but ideally will need to analyze the controller to create a suitable
     *   empty test class.
     *
     * @param controller the controller for which to create the test suite
     * @return a test suite loaded into the classpath
     * @throws IOException if an I/O error occurs,
     */
    private static Class<?> createEmptyTest(Object controller) throws IOException {
        Class<?> clazz = getClassForObject(controller);
        ClassLoader classLoader = logger.getClass().getClassLoader();

        // create empty test folder
        File tmpRoot = File.createTempFile("EvoSpring" + System.currentTimeMillis(), "");
        tmpRoot.delete();
        tmpRoot.mkdirs();
        tmpRoot.deleteOnExit();

        // create empty test class
        String packageName = clazz.getPackage().getName();
        String packagePath = packageName.replace(".", File.separator);
        String testSuiteName = clazz.getSimpleName() + "MyTest";
        String className = packageName + "." + testSuiteName;
        File testFile = new File(tmpRoot, testSuiteName + ".java");
        File compiledFile = new File(tmpRoot, testSuiteName + ".class");
        testFile.deleteOnExit();
        testFile.createNewFile();
        compiledFile.deleteOnExit();

        // copy from template
        String templateName = "/Users/julien.ditria/github/JulienDiTria/evosuite/client/src/test/java/com/examples/with/different"
            + "/packagename/spring/petclinic/owner/OwnerControllerSimpleTest.java";

        File templateFile = new File(templateName);

        try {
            String content = new String(Files.readAllBytes(templateFile.toPath()));
            content = content.replace("OwnerControllerSimpleTest", testSuiteName);
            Files.write(testFile.toPath(), content.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // compile the class
        // TODO 16.02.2023 Julien Di Tria
        //  Check the return value of the compiler, and throw an exception if the compilation fails
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null, null, null, testFile.getPath());

        // move file into classpath
        // TODO 16.02.2023 Julien Di Tria
        //  Find another way to find where to put the file, as this is not a safe way to do it, maybe use the classpath of the CUT
        File folder =
            new File(TestGenerationContext.getInstance().getClassLoaderForSUT().getClass().getProtectionDomain().getCodeSource().getLocation().getPath(), packagePath);
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
    public static void setupSpringRunner(Object controller) {
        Class<?> clazz = getClassForObject(controller);
        RunNotifier runNotifier = new RunNotifier();

        try {
            instance.springSetupRunner = new SpringSetupRunner(clazz);
        } catch (InitializationError e) {
            throw new RuntimeException(e);
        }
        instance.springSetupRunner.run(runNotifier);

        Object testInstance = instance.springSetupRunner.testInstance;
        MockMvc mockMvc = getFieldValue(testInstance, "mockMvc");

//        try {
//            mockMvc.perform(get("/owners")
//                    .param("lastName", "Smith"))
//                .andExpect(status().isOk());
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }

        System.out.println("MockMvc: " + mockMvc);
        System.out.println("SpringSetupRunner executed");
    }

    static <T> T getFieldValue(Object holder, String fieldName) {
        Field field = null;
        Object fieldValue = null;
        try {
            field = holder.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            fieldValue = field.get(holder);
            return (T) fieldValue;
        } catch (NoSuchFieldException | IllegalAccessException | ClassCastException e) {
            throw new RuntimeException(e);
        }
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
}
