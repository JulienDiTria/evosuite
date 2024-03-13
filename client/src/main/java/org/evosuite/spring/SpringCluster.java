package org.evosuite.spring;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.evosuite.setup.TestClusterUtils;
import org.evosuite.utils.generic.GenericAccessibleObject;
import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.generic.GenericClassImpl;
import org.evosuite.utils.generic.GenericMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.ContentResultMatchers;
import org.springframework.test.web.servlet.result.CookieResultMatchers;
import org.springframework.test.web.servlet.result.FlashAttributeResultMatchers;
import org.springframework.test.web.servlet.result.HandlerResultMatchers;
import org.springframework.test.web.servlet.result.HeaderResultMatchers;
import org.springframework.test.web.servlet.result.JsonPathResultMatchers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.result.ModelResultMatchers;
import org.springframework.test.web.servlet.result.PrintingResultHandler;
import org.springframework.test.web.servlet.result.RequestResultMatchers;
import org.springframework.test.web.servlet.result.StatusResultMatchers;
import org.springframework.test.web.servlet.result.ViewResultMatchers;
import org.springframework.test.web.servlet.result.XpathResultMatchers;

/**
 * Singleton class that holds information about generators for SSpring related classes (ResultMatchers, etc.)
 */
public class SpringCluster {

    protected static final Logger logger = LoggerFactory.getLogger(SpringCluster.class);

    /**
     * Singleton instance
     */
    private static final SpringCluster instance = new SpringCluster();

    /**
     * Map of classes to their generators
     */
    private static final Map<GenericClass<?>, Set<GenericAccessibleObject<?>>> generators = new LinkedHashMap<>();

    private SpringCluster() {
    }

    public static SpringCluster getInstance() {
        return instance;
    }

    public void initialize() {
        findRequestBuilderGenerators();
        findResultMatcherGenerators();
        findGlobalResultMatcherGenerators();
    }

    public void findGlobalResultMatcherGenerators() {
        Set<Class<?>> lookForClasses = new HashSet<>(Arrays.asList(
            ContentResultMatchers.class, CookieResultMatchers.class, FlashAttributeResultMatchers.class,
            HandlerResultMatchers.class, HeaderResultMatchers.class, JsonPathResultMatchers.class,
            ModelResultMatchers.class, PrintingResultHandler.class, RequestResultMatchers.class,
            StatusResultMatchers.class, ViewResultMatchers.class, XpathResultMatchers.class));
        findGeneratorForClassesInClass(lookForClasses, MockMvcResultMatchers.class);
    }

    public void findResultMatcherGenerators() {
        Set<Class<?>> lookInClasses = new HashSet<>(Arrays.asList(
            ContentResultMatchers.class, CookieResultMatchers.class, FlashAttributeResultMatchers.class,
            HandlerResultMatchers.class, HeaderResultMatchers.class, JsonPathResultMatchers.class,
            ModelResultMatchers.class, PrintingResultHandler.class, RequestResultMatchers.class,
            StatusResultMatchers.class, ViewResultMatchers.class, XpathResultMatchers.class));
        findGeneratorForClassInClasses(ResultMatcher.class, lookInClasses);
    }

    public void findRequestBuilderGenerators() {
        Set<Class<?>> lookInClasses = new HashSet<>(Arrays.asList(MockMvcRequestBuilders.class, MockHttpServletRequestBuilder.class));
        findGeneratorForClassInClasses(MockHttpServletRequestBuilder.class, lookInClasses);
    }

    public void findGeneratorForClassesInClasses(Collection<Class<?>> lookForClasses, Collection<Class<?>> lookInClasses) {
        lookForClasses.forEach(
            lookForClass -> lookInClasses.forEach(lookInClass -> findGeneratorForClassInClass(lookForClass, lookInClass)));
    }

    public void findGeneratorForClassInClasses(Class<?> lookForClass, Collection<Class<?>> lookInClasses) {
        lookInClasses.forEach(lookInClass -> findGeneratorForClassInClass(lookForClass, lookInClass));
    }

    public void findGeneratorForClassesInClass(Collection<Class<?>> lookForClasses, Class<?> lookInClass) {
        lookForClasses.forEach(lookForClass -> findGeneratorForClassInClass(lookForClass, lookInClass));
    }

    public void findGeneratorForClassInClass(Class<?> lookForKlass, Class<?> lookInKlass) {
        Set<GenericAccessibleObject<?>> newGenerators = new HashSet<>();
        for (Method method : TestClusterUtils.getMethods(lookInKlass)) {
            GenericMethod genericMethod = new GenericMethod(method, lookInKlass);
            if (genericMethod.isPublic() && lookForKlass == genericMethod.getReturnType()) {
                newGenerators.add(genericMethod);
            }
        }
        addGenerators(new GenericClassImpl(lookForKlass), newGenerators);
    }

    public void addGenerators(GenericClass<?> targetClass, Set<GenericAccessibleObject<?>> targetGenerators) {
        Set<GenericAccessibleObject<?>> currentGenerators = generators.getOrDefault(targetClass, new LinkedHashSet<>());
        currentGenerators.addAll(targetGenerators);
        if (!currentGenerators.isEmpty()) {
            generators.put(targetClass, currentGenerators);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SpringCluster: {\n");
        sb.append("generators: \n");
        sb.append(generators.entrySet().stream().map(entry -> {
            String key = entry.getKey().toString();
            String value = entry.getValue().stream().map(GenericAccessibleObject::toString).collect(Collectors.joining("\n\t"));
            return key + " -> \n\t" + value;
        }).collect(Collectors.joining("\n\n")));
        sb.append("}");
        return sb.toString();
    }
}
