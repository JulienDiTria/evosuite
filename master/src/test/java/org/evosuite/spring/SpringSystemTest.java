package org.evosuite.spring;

import com.examples.with.different.packagename.spring.petclinic.owner.OwnerCtrler;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Test;

public class SpringSystemTest extends SystemTestBase {

    @Test
    public void testSpringController() throws IOException {
        Properties.ALGORITHM = Properties.Algorithm.MONOTONIC_GA;
        Properties.JUNIT_TESTS = true;

        EvoSuite evosuite = new EvoSuite();

        String targetClass = OwnerCtrler.class.getCanonicalName();

        Properties.TARGET_CLASS = targetClass;
        Properties.SEARCH_BUDGET = 100000;
        ClassPathHandler.getInstance().addElementToTargetProjectClassPath("/Users/julien.ditria/.m2/repository/org/springframework/spring-webmvc/5.1.2.RELEASE/spring-webmvc-5.1.2.RELEASE.jar");
        ClassPathHandler.getInstance().addElementToTargetProjectClassPath("/Users/julien.ditria/.m2/repository/org/springframework/spring-web/5.1.2.RELEASE/spring-web-5.1.2.RELEASE.jar");

        String[] command = new String[] {"-generateSuite", "-class", targetClass};
        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<TestSuiteChromosome> ga = getGAFromResult(result);
        TestSuiteChromosome best = ga.getBestIndividual();
        System.out.println("EvolvedTestSuite:\n" + best);

    }
}
