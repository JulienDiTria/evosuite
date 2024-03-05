package org.evosuite.spring;

import com.examples.with.different.packagename.spring.petclinic.owner.OwnerController;
import org.evosuite.TestSuiteGenerator;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.junit.JUnitAnalyzer;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Test;

public class TestSpringSupport {

    @Test
    public void test0() {
        SpringSupport.setup(OwnerController.class.getName());
        System.out.println("all good");
    }

    @Test
    public void test1() throws ConstructionFailedException {
        TestCase testCase = new DefaultTestCase();
        SpringSupport.setup(OwnerController.class.getName());
        SpringTestFactory.insertRandomSpringCall(testCase, 0);

        TestSuiteChromosome testSuite = new TestSuiteChromosome();
        testSuite.addTest(testCase);
        JUnitAnalyzer.removeTestsThatDoNotCompile(testSuite.getTests());

//        TestSuiteGenerator.writeJUnitTestsAndCreateResult(testSuite);
    }
}
