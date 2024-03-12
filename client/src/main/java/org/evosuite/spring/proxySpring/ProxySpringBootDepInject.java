package org.evosuite.spring.proxySpring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionEvaluationReport;
import org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportMessage;
import org.springframework.boot.test.autoconfigure.SpringBootDependencyInjectionTestExecutionListener;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.test.context.CacheAwareContextLoaderDelegate;
import org.springframework.test.context.ContextLoader;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.SmartContextLoader;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.cache.ContextCache;
import org.springframework.util.Assert;

import static org.evosuite.spring.SpringSetupRunner.getFieldValue;
import static org.springframework.test.context.support.DependencyInjectionTestExecutionListener.REINJECT_DEPENDENCIES_ATTRIBUTE;

public class ProxySpringBootDepInject implements TestExecutionListener, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(ProxySpringBootDepInject.class);

    private final SpringBootDependencyInjectionTestExecutionListener testExecutionListener;
    private final String name;

    public ProxySpringBootDepInject(SpringBootDependencyInjectionTestExecutionListener testExecutionListener) {
        this.testExecutionListener = testExecutionListener;
        this.name = testExecutionListener.getClass().getSimpleName();
    }

    @Override
    public final int getOrder() {
        return 2000;
    }

    @Override
    public void prepareTestInstance(TestContext testContext) throws Exception {
        logger.warn("prepareTestInstance - pre");
        try {
            prepareTestInstanceInternal(testContext);
            logger.warn("prepareTestInstance - no error");
        } catch (Exception ex) {
            logger.error("prepareTestInstance - error pre", ex);
            outputConditionEvaluationReport(testContext);
            logger.error("prepareTestInstance - error aft", ex);
            throw ex;
        }
        logger.warn("prepareTestInstance - aft");
    }

    private void outputConditionEvaluationReport(TestContext testContext) {
        logger.warn("outputConditionEvaluationReport - pre");
        try {
            ApplicationContext context = testContext.getApplicationContext();
            logger.warn("outputConditionEvaluationReport - 1");
            if (context instanceof ConfigurableApplicationContext) {
                logger.warn("outputConditionEvaluationReport - 1.1");
                ConditionEvaluationReport report =
                    ConditionEvaluationReport.get(((ConfigurableApplicationContext) context).getBeanFactory());
                logger.warn("outputConditionEvaluationReport - 1.2");
                System.err.println(new ConditionEvaluationReportMessage(report));
                logger.error(String.valueOf(new ConditionEvaluationReportMessage(report)));
                logger.warn("outputConditionEvaluationReport - 1.3");
            }
            logger.warn("outputConditionEvaluationReport - 2");
        } catch (Exception ex) {
            // Allow original failure to be reported
            logger.error("outputConditionEvaluationReport - error ignored", ex);
        }
        logger.warn("outputConditionEvaluationReport - aft");
    }

    public void prepareTestInstanceInternal(TestContext testContext) throws Exception {
        logger.warn("prepareTestInstanceInternal - Performing dependency injection for test context [{}].", testContext);
        injectDependencies(testContext);
        logger.warn("prepareTestInstanceInternal - done.");
    }

    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        logger.warn("beforeTestMethod - pre");

        if (Boolean.TRUE.equals(testContext.getAttribute(REINJECT_DEPENDENCIES_ATTRIBUTE))) {
            logger.debug("Reinjecting dependencies for test context [{}].", testContext);
            injectDependencies(testContext);
            logger.warn("beforeTestMethod - after dependencies injected.");
        }
        logger.warn("beforeTestMethod - aft");
    }

    protected void injectDependencies(TestContext testContext) throws Exception {
        logger.warn("injectDependencies - pre");
        Object bean = testContext.getTestInstance();
        logger.warn("injectDependencies - 1");
        Class<?> clazz = testContext.getTestClass();
        logger.warn("injectDependencies - 2");
        ApplicationContext context;
        try {
            logger.warn("injectDependencies - 2 - try");
            CacheAwareContextLoaderDelegate cacheAwareContextLoaderDelegate = getFieldValue(testContext, "cacheAwareContextLoaderDelegate");
            logger.warn("injectDependencies - 2 - try - 1 - cacheAwareContextLoaderDelegate: {}", cacheAwareContextLoaderDelegate);
            ContextCache contextCache = getFieldValue(cacheAwareContextLoaderDelegate, "contextCache");
            logger.warn("injectDependencies - 2 - try - 1.1 - contextCache: {}", contextCache);
            MergedContextConfiguration mergedContextConfiguration = getFieldValue(testContext, "mergedContextConfiguration");
            logger.warn("injectDependencies - 2 - try - 2 - mergedContextConfiguration: {}", mergedContextConfiguration);
            context = loadContext(contextCache, mergedContextConfiguration);
//            context = cacheAwareContextLoaderDelegate.loadContext(mergedContextConfiguration);
//            context = testContext.getApplicationContext();
            logger.warn("injectDependencies - 2 - try done");
        } catch (IllegalStateException e) {
            logger.error("injectDependencies - 2 - try error", e);
            throw new RuntimeException(e);
        }
        logger.warn("injectDependencies - 2.1");
        AutowireCapableBeanFactory beanFactory = context.getAutowireCapableBeanFactory();
        logger.warn("injectDependencies - 3");
        beanFactory.autowireBeanProperties(bean, AutowireCapableBeanFactory.AUTOWIRE_NO, false);
        logger.warn("injectDependencies - 4");
        beanFactory.initializeBean(bean, clazz.getName() + AutowireCapableBeanFactory.ORIGINAL_INSTANCE_SUFFIX);
        logger.warn("injectDependencies - 5");
        testContext.removeAttribute(REINJECT_DEPENDENCIES_ATTRIBUTE);
        logger.warn("injectDependencies - aft");
    }

    private ApplicationContext loadContext(ContextCache contextCache, MergedContextConfiguration mergedContextConfiguration) {
        logger.warn("loadContext - pre sync");
        synchronized (this) {
            logger.warn("loadContext - pre");
            ApplicationContext context = contextCache.get(mergedContextConfiguration);
            logger.warn("loadContext - context: {}", context);
            if (context == null) {
                logger.warn("loadContext - context null");
                try {
                    logger.warn("loadContext - loading internal");
                    context = loadContextInternal(mergedContextConfiguration);
                    logger.warn("Storing ApplicationContext in cache under key [{}]", mergedContextConfiguration);
                    contextCache.put(mergedContextConfiguration, context);
                    logger.warn("loadContext - cache saved");
                } catch (Exception ex) {
                    logger.error("loadContext - Failed to load ApplicationContext", ex);
                    throw new IllegalStateException("Failed to load ApplicationContext", ex);
                }
            } else {
                logger.warn("loadContext - Retrieved ApplicationContext from cache with key [{}]", mergedContextConfiguration);
            }
            contextCache.logStatistics();
            return context;
        }
    }

    protected ApplicationContext loadContextInternal(MergedContextConfiguration mergedContextConfiguration) throws Exception {
        logger.warn("loadContextInternal - pre");

        ContextLoader contextLoader = mergedContextConfiguration.getContextLoader();
        logger.warn("loadContextInternal - 1");

        Assert.notNull(contextLoader, "Cannot load an ApplicationContext with a NULL 'contextLoader'. " +
            "Consider annotating your test class with @ContextConfiguration or @ContextHierarchy.");

        ApplicationContext applicationContext;
        logger.warn("loadContextInternal - 2");

        if (contextLoader instanceof SmartContextLoader) {
            logger.warn("loadContextInternal - 2 - SmartContextLoader - pre");

            SmartContextLoader smartContextLoader = (SmartContextLoader) contextLoader;
            logger.warn("loadContextInternal - 2 - SmartContextLoader - 1 - smartContextLoader: {}, class {}", smartContextLoader,
                smartContextLoader.getClass());

            if (smartContextLoader instanceof SpringBootContextLoader) {
                logger.warn("loadContextInternal - 2 - SmartContextLoader - 1.1 - SpringBootContextLoader");
                ProxySpringBootContextLoader proxySpringBootContextLoader = new ProxySpringBootContextLoader();
                applicationContext = proxySpringBootContextLoader.loadContext(mergedContextConfiguration);
            } else {
                logger.warn("loadContextInternal - 2 - SmartContextLoader - 1.2 - NOT SpringBootContextLoader");
                applicationContext = smartContextLoader.loadContext(mergedContextConfiguration);
            }

            logger.warn("loadContextInternal - 2 - SmartContextLoader - aft");
        } else {
            logger.warn("loadContextInternal - 2 - else - pre");
            String[] locations = mergedContextConfiguration.getLocations();
            logger.warn("loadContextInternal - 2 - else - 1");

            Assert.notNull(locations, "Cannot load an ApplicationContext with a NULL 'locations' array. " +
                "Consider annotating your test class with @ContextConfiguration or @ContextHierarchy.");
            logger.warn("loadContextInternal - 2 - else - 2");
            applicationContext = contextLoader.loadContext(locations);
            logger.warn("loadContextInternal - 2 - else - aft");
        }

        logger.warn("loadContextInternal - aft");
        return applicationContext;
    }
}
