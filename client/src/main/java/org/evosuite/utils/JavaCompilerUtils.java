package org.evosuite.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.apache.commons.io.FileUtils;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.runtime.util.JarPathing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaCompilerUtils {

    private static final Logger logger = LoggerFactory.getLogger(JavaCompilerUtils.class);

    public JavaCompilerUtils() {
    }

    private static String defaultClassPath() {
        String evosuiteCP = ClassPathHandler.getInstance().getEvoSuiteClassPath();
        if (JarPathing.containsAPathingJar(evosuiteCP)) {
            evosuiteCP = JarPathing.expandPathingJars(evosuiteCP);
        }

        String targetProjectCP = ClassPathHandler.getInstance().getTargetProjectClasspath();
        if (JarPathing.containsAPathingJar(targetProjectCP)) {
            targetProjectCP = JarPathing.expandPathingJars(targetProjectCP);
        }

        return targetProjectCP + File.pathSeparator + evosuiteCP;
    }

    public boolean compile(List<File> toCompile) {
        //try to compile the test cases
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            logger.error("No Java compiler is available");
            return false;
        }

        // set up the file manager and compilation units
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        Locale locale = Locale.getDefault();
        Charset charset = StandardCharsets.UTF_8;
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, locale, charset);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(toCompile);

        // set up the compiler task and run it
        String classpath = defaultClassPath();
        List<String> optionList = new ArrayList<>(Arrays.asList("-classpath", classpath));
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics,
            optionList, null, compilationUnits);
        boolean compiled = task.call();
        try {
            fileManager.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // check results
        if (!compiled) {
            logger.error("Compilation failed on compilation units: {}", compilationUnits);
            logger.error("Classpath: {}", classpath);

            for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
                if (diagnostic.getMessage(null).startsWith("error while writing")) {
                    logger.error("Error is due to file permissions, ignoring...");
                    return true;
                }
                logger.error("Diagnostic: {} : {} ", diagnostic.getMessage(null), diagnostic.getLineNumber());
            }

            StringBuilder buffer = new StringBuilder();
            for (JavaFileObject sourceFile : compilationUnits) {
                List<String> lines = null;
                try {
                    lines = FileUtils.readLines(new File(sourceFile.toUri().getPath()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                buffer.append(compilationUnits.iterator().next().toString()).append("\n");

                for (int i = 0; i < lines.size(); i++) {
                    buffer.append(i + 1).append(": ").append(lines.get(i)).append("\n");
                }
            }
            logger.error("{}", buffer);
            return false;
        }
        return true;
    }
}
