package org.evosuite.spring.proxySpring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.ProxySpringApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.boot.test.context.ReactiveWebMergedContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.web.SpringBootMockServletContext;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.web.reactive.context.GenericReactiveWebApplicationContext;
import org.springframework.boot.web.servlet.support.ServletContextApplicationContextInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.SpringVersion;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.support.AbstractContextLoader;
import org.springframework.test.context.support.AnnotationConfigContextLoaderUtils;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.context.web.WebMergedContextConfiguration;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.GenericWebApplicationContext;

public class ProxySpringBootContextLoader extends AbstractContextLoader {

    private static final Logger logger = LoggerFactory.getLogger(ProxySpringBootContextLoader.class);

    public ApplicationContext loadContext(MergedContextConfiguration config) throws Exception {
        logger.warn("loadContext - pre");
        Class<?>[] configClasses = config.getClasses();
        String[] configLocations = config.getLocations();
        Assert.state(
            !ObjectUtils.isEmpty(configClasses)
                || !ObjectUtils.isEmpty(configLocations),
            () -> "No configuration classes "
                + "or locations found in @SpringApplicationConfiguration. "
                + "For default configuration detection to work you need "
                + "Spring 4.0.3 or better (found " + SpringVersion.getVersion()
                + ").");

        logger.warn("loadContext - 1");

        ProxySpringApplication application = getProxySpringApplication();
        application.setMainApplicationClass(config.getTestClass());
        application.addPrimarySources(Arrays.asList(configClasses));
        application.getSources().addAll(Arrays.asList(configLocations));
        ConfigurableEnvironment environment = getEnvironment();
        if (!ObjectUtils.isEmpty(config.getActiveProfiles())) {
            setActiveProfiles(environment, config.getActiveProfiles());
        }
        logger.warn("loadContext - 2");
        ResourceLoader resourceLoader = (application.getResourceLoader() != null)
            ? application.getResourceLoader()
            : new DefaultResourceLoader(getClass().getClassLoader());
        TestPropertySourceUtils.addPropertiesFilesToEnvironment(environment,
            resourceLoader, config.getPropertySourceLocations());
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(environment,
            getInlinedProperties(config));
        logger.warn("loadContext - 3");
        application.setEnvironment(environment);
        List<ApplicationContextInitializer<?>> initializers = getInitializers(config,
            application);
        logger.warn("loadContext - if");
        if (config instanceof WebMergedContextConfiguration) {
            logger.warn("loadContext - if WebMergedContextConfiguration");
            application.setWebApplicationType(WebApplicationType.SERVLET);
            if (!isEmbeddedWebEnvironment(config)) {
                new WebConfigurer().configure(config, application, initializers);
            }
        } else if (config instanceof ReactiveWebMergedContextConfiguration) {
            logger.warn("loadContext - if ReactiveWebMergedContextConfiguration");
            application.setWebApplicationType(WebApplicationType.REACTIVE);
            if (!isEmbeddedWebEnvironment(config)) {
                new ReactiveWebConfigurer().configure(application);
            }
        } else {
            logger.warn("loadContext - if WebApplicationType.NONE");
            application.setWebApplicationType(WebApplicationType.NONE);
        }
        application.setInitializers(initializers);

        logger.warn("loadContext - pre run - application {}", application.getClass());
        System.err.println("ProxySpringBootContextLoader loadContext - pre");
        try {
            // TODO 2024.03.12 Julien Di Tria : understand why this call to application.run() doesn't happen correctly and suddenly stops
            //  the RMI client jvm without logs
//            ConfigurableApplicationContext applicationContext = application.run();
            ConfigurableApplicationContext applicationContext = runnn(application);
            System.err.println("ProxySpringBootContextLoader loadContext - aft");
            logger.warn("loadContext - aft");
            return applicationContext;
        } catch (Throwable exception) {
            logger.error("Error when running the spring application", exception);
            throw new RuntimeException(exception);
        }
    }

    private ConfigurableApplicationContext runnn(ProxySpringApplication application){
        logger.warn("runnn - pre");
        ConfigurableApplicationContext applicationContext = application.run();
        logger.warn("runnn - aft");
        return applicationContext;
    }

    /**
     * Builds new {@link org.springframework.boot.ProxySpringApplication} instance. You can override this method to add custom behavior
     *
     * @return {@link org.springframework.boot.ProxySpringApplication} instance
     */
    protected ProxySpringApplication getProxySpringApplication() {
        return new ProxySpringApplication();
    }

    /**
     * Builds new {@link org.springframework.boot.SpringApplication} instance. You can override this method to add custom behavior
     *
     * @return {@link org.springframework.boot.SpringApplication} instance
     */
    protected SpringApplication getSpringApplication() {
        return new SpringApplication();
    }

    /**
     * Builds a new {@link ConfigurableEnvironment} instance. You can override this method to return something other than
     * {@link StandardEnvironment} if necessary.
     *
     * @return a {@link ConfigurableEnvironment} instance
     */
    protected ConfigurableEnvironment getEnvironment() {
        return new StandardEnvironment();
    }

    private void setActiveProfiles(ConfigurableEnvironment environment,
        String[] profiles) {
        TestPropertyValues
            .of("spring.profiles.active="
                + StringUtils.arrayToCommaDelimitedString(profiles))
            .applyTo(environment);
    }

    protected String[] getInlinedProperties(MergedContextConfiguration config) {
        ArrayList<String> properties = new ArrayList<>();
        // JMX bean names will clash if the same bean is used in multiple contexts
        disableJmx(properties);
        properties.addAll(Arrays.asList(config.getPropertySourceProperties()));
        if (!isEmbeddedWebEnvironment(config) && !hasCustomServerPort(properties)) {
            properties.add("server.port=-1");
        }
        return StringUtils.toStringArray(properties);
    }

    private void disableJmx(List<String> properties) {
        properties.add("spring.jmx.enabled=false");
    }

    private boolean hasCustomServerPort(List<String> properties) {
        Binder binder = new Binder(convertToConfigurationPropertySource(properties));
        return binder.bind("server.port", Bindable.of(String.class)).isBound();
    }

    private ConfigurationPropertySource convertToConfigurationPropertySource(
        List<String> properties) {
        return new MapConfigurationPropertySource(TestPropertySourceUtils
            .convertInlinedPropertiesToMap(StringUtils.toStringArray(properties)));
    }

    /**
     * Return the {@link ApplicationContextInitializer initializers} that will be applied to the context. By default this method will adapt
     * {@link ContextCustomizer context customizers}, add {@link SpringApplication#getInitializers() application initializers} and add
     * {@link MergedContextConfiguration#getContextInitializerClasses() initializers specified on the test}.
     *
     * @param config      the source context configuration
     * @param application the application instance
     * @return the initializers to apply
     * @since 2.0.0
     */
    protected List<ApplicationContextInitializer<?>> getInitializers(
        MergedContextConfiguration config, SpringApplication application) {
        List<ApplicationContextInitializer<?>> initializers = new ArrayList<>();
        for (ContextCustomizer contextCustomizer : config.getContextCustomizers()) {
            initializers.add(new ContextCustomizerAdapter(contextCustomizer, config));
        }
        initializers.addAll(application.getInitializers());
        for (Class<? extends ApplicationContextInitializer<?>> initializerClass : config
            .getContextInitializerClasses()) {
            initializers.add(BeanUtils.instantiateClass(initializerClass));
        }
        if (config.getParent() != null) {
            initializers.add(new ParentContextApplicationContextInitializer(
                config.getParentApplicationContext()));
        }
        return initializers;
    }

    private boolean isEmbeddedWebEnvironment(MergedContextConfiguration config) {
        SpringBootTest annotation = AnnotatedElementUtils
            .findMergedAnnotation(config.getTestClass(), SpringBootTest.class);
        return annotation != null && annotation.webEnvironment().isEmbedded();
    }

    @Override
    public void processContextConfiguration(ContextConfigurationAttributes configAttributes) {
        super.processContextConfiguration(configAttributes);
        if (!configAttributes.hasResources()) {
            Class<?>[] defaultConfigClasses = detectDefaultConfigurationClasses(
                configAttributes.getDeclaringClass());
            configAttributes.setClasses(defaultConfigClasses);
        }
    }

    /**
     * Detect the default configuration classes for the supplied test class. By default simply delegates to
     * {@link AnnotationConfigContextLoaderUtils#detectDefaultConfigurationClasses}.
     *
     * @param declaringClass the test class that declared {@code @ContextConfiguration}
     * @return an array of default configuration classes, potentially empty but never {@code null}
     * @see AnnotationConfigContextLoaderUtils
     */
    protected Class<?>[] detectDefaultConfigurationClasses(Class<?> declaringClass) {
        return AnnotationConfigContextLoaderUtils
            .detectDefaultConfigurationClasses(declaringClass);
    }

    @Override
    public ApplicationContext loadContext(String... locations) throws Exception {
        throw new UnsupportedOperationException("SpringApplicationContextLoader "
            + "does not support the loadContext(String...) method");
    }

    @Override
    protected String[] getResourceSuffixes() {
        return new String[] {"-context.xml", "Context.groovy"};
    }

    @Override
    protected String getResourceSuffix() {
        throw new IllegalStateException();
    }

    /**
     * Inner class to configure {@link WebMergedContextConfiguration}.
     */
    private static class WebConfigurer {

        private static final Class<GenericWebApplicationContext> WEB_CONTEXT_CLASS = GenericWebApplicationContext.class;

        void configure(MergedContextConfiguration configuration,
            SpringApplication application,
            List<ApplicationContextInitializer<?>> initializers) {
            WebMergedContextConfiguration webConfiguration = (WebMergedContextConfiguration) configuration;
            addMockServletContext(initializers, webConfiguration);
            application.setApplicationContextClass(WEB_CONTEXT_CLASS);
        }

        private void addMockServletContext(
            List<ApplicationContextInitializer<?>> initializers,
            WebMergedContextConfiguration webConfiguration) {
            SpringBootMockServletContext servletContext = new SpringBootMockServletContext(
                webConfiguration.getResourceBasePath());
            initializers.add(0, new ServletContextApplicationContextInitializer(
                servletContext, true));
        }

    }

    /**
     * Inner class to configure {@link ReactiveWebMergedContextConfiguration}.
     */
    private static class ReactiveWebConfigurer {

        private static final Class<GenericReactiveWebApplicationContext> WEB_CONTEXT_CLASS = GenericReactiveWebApplicationContext.class;

        void configure(SpringApplication application) {
            application.setApplicationContextClass(WEB_CONTEXT_CLASS);
        }

    }

    /**
     * Adapts a {@link ContextCustomizer} to a {@link ApplicationContextInitializer} so that it can be triggered via
     * {@link SpringApplication}.
     */
    private static class ContextCustomizerAdapter
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        private final ContextCustomizer contextCustomizer;

        private final MergedContextConfiguration config;

        ContextCustomizerAdapter(ContextCustomizer contextCustomizer,
            MergedContextConfiguration config) {
            this.contextCustomizer = contextCustomizer;
            this.config = config;
        }

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            this.contextCustomizer.customizeContext(applicationContext, this.config);
        }

    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    private static class ParentContextApplicationContextInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        private final ApplicationContext parent;

        ParentContextApplicationContextInitializer(ApplicationContext parent) {
            this.parent = parent;
        }

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            applicationContext.setParent(this.parent);
        }

    }

}
