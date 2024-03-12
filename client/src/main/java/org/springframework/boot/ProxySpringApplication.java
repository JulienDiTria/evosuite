/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.StopWatch;

import static org.evosuite.spring.SpringSetupRunner.getSuperFieldValue;

public class ProxySpringApplication extends SpringApplication {

    private static final Logger logger = LoggerFactory.getLogger(ProxySpringApplication.class);

    /**
     * Run the Spring application, creating and refreshing a new {@link ApplicationContext}.
     *
     * @param args the application arguments (usually passed from a Java main method)
     * @return a running {@link ApplicationContext}
     */
    @Override
    public ConfigurableApplicationContext run(String... args) {
        System.err.println("ProxySpringApplication.run");
        logger.warn("run - pre");
        String argsString = Arrays.asList(args).toString();
        logger.warn(String.format("run - pre args [%s]", argsString));
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        ConfigurableApplicationContext context = null;
        Collection<SpringBootExceptionReporter> exceptionReporters = new ArrayList<>();
        invokePrivateSuperMethod("configureHeadlessProperty", null, null);
//        configureHeadlessProperty();

        logger.warn("run - 1");
        SpringApplicationRunListeners listeners = (SpringApplicationRunListeners) invokePrivateSuperMethod("getRunListeners",
            new Class[] {String[].class},
            new Object[] {args});
//        SpringApplicationRunListeners listeners = getRunListeners(args);
        logger.warn("run - 2");

        listeners.starting();
        try {
            logger.warn("run - 3 - try - pre");

            ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
            System.out.println("run - 3 - try - 1");
            logger.warn("run - 3 - try - 1");

            ConfigurableEnvironment environment = (ConfigurableEnvironment) invokePrivateSuperMethod("prepareEnvironment",
                new Class[] {SpringApplicationRunListeners.class, ApplicationArguments.class},
                new Object[] {listeners, applicationArguments});
//            ConfigurableEnvironment environment = prepareEnvironment(listeners, applicationArguments);
            logger.warn("run - 3 - try - 2");
            invokePrivateSuperMethod("configureIgnoreBeanInfo", new Class[] {ConfigurableEnvironment.class}, new Object[] {environment});
//            configureIgnoreBeanInfo(environment);
            logger.warn("run - 3 - try - 3");
            Banner printedBanner =
                (Banner) invokePrivateSuperMethod("printBanner", new Class[] {ConfigurableEnvironment.class}, new Object[] {environment});
//            Banner printedBanner = printBanner(environment);
            logger.warn("run - 3 - try - 4");
            context = createApplicationContext();

            exceptionReporters = (ArrayList<SpringBootExceptionReporter>) invokePrivateSuperMethod("getSpringFactoriesInstances",
                new Class[] {Class.class, Class[].class, Object[].class},
                new Object[] {SpringBootExceptionReporter.class, new Class[] {ConfigurableApplicationContext.class},
                    new Object[] {context}});
//            exceptionReporters = getSpringFactoriesInstances(
//                SpringBootExceptionReporter.class,
//                new Class[] { ConfigurableApplicationContext.class }, context);

            logger.warn("run - 3 - try - 5");
            invokePrivateSuperMethod("prepareContext",
                new Class[] {ConfigurableApplicationContext.class, ConfigurableEnvironment.class, SpringApplicationRunListeners.class,
                    ApplicationArguments.class, Banner.class},
                new Object[] {context, environment, listeners, applicationArguments, printedBanner});
//            prepareContext(context, environment, listeners, applicationArguments, printedBanner);

            logger.warn("run - 3 - try - 6");
            invokePrivateSuperMethod("refreshContext", new Class[] {ConfigurableApplicationContext.class}, new Object[] {context});
//            refreshContext(context);
            logger.warn("run - 3 - try - 7");
            afterRefresh(context, applicationArguments);
            logger.warn("run - 3 - try - 8");
            stopWatch.stop();
            logger.warn("run - 3 - try - 9");
            if (getSuperFieldValue(this, "logStartupInfo")) {
//          if(this.logStartupInfo) {
                new StartupInfoLogger(getSuperFieldValue(this, "mainApplicationClass"))
//                new StartupInfoLogger(this.mainApplicationClass)
                    .logStarted(getApplicationLog(), stopWatch);
            }
            logger.warn("run - 3 - try - 10");
            listeners.started(context);

            logger.warn("run - 3 - try - 11");
            invokePrivateSuperMethod("callRunners", new Class[] {ApplicationContext.class, ApplicationArguments.class},
                new Object[] {context, applicationArguments});
//            callRunners(context, applicationArguments);
            logger.warn("run - 3 - try - aft");
        } catch (Throwable ex) {
            logger.error("run - 3 - catch ", ex);
            invokePrivateSuperMethod("handleRunFailure", new Class[] {ConfigurableApplicationContext.class, Throwable.class,
                Collection.class, SpringApplicationRunListeners.class}, new Object[] {context, ex, exceptionReporters, listeners});
//            handleRunFailure(context, ex, exceptionReporters, listeners);
            logger.warn("run - 3 - catch - rethrow");
            throw new IllegalStateException(ex);
        }

        try {
            logger.warn("run - 4 - try - pre");
            listeners.running(context);
            logger.warn("run - 4 - try - aft");
        } catch (Throwable ex) {
            logger.error("run - 4 - catch ", ex);
            invokePrivateSuperMethod("handleRunFailure", new Class[] {ConfigurableApplicationContext.class, Throwable.class,
                Collection.class, SpringApplicationRunListeners.class}, new Object[] {context, ex, exceptionReporters, null});
//            handleRunFailure(context, ex, exceptionReporters, null);
            logger.warn("run - 4 - catch - rethrow");
            throw new IllegalStateException(ex);
        }

        logger.warn("run - aft");
        return context;
    }

    Object invokePrivateSuperMethod(String methodName, Class<?>[] parameterTypes, Object[] args) {
        try {
            Method method = SpringApplication.class.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method.invoke(this, args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            logger.error("Error invoking private method: " + methodName, e);
            throw new RuntimeException(e);
        }
    }

}
