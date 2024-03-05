/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package org.evosuite.junit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.evosuite.Properties;
import org.evosuite.junit.writer.TestSuiteWriterUtils;
import org.evosuite.runtime.vnet.NonFunctionalRequirementExtension;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCodeVisitor;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;
import java.util.Map;

import static org.evosuite.junit.writer.TestSuiteWriterUtils.NEWLINE;

/**
 * Used to adapt the internal representations of test suites to JUnit 5 test cases
 */
public class JUnit5TestAdapter implements UnitTestAdapter {

    @Override
    public Class<?> testAnnotation() {
        return org.junit.jupiter.api.Test.class;
    }

    @Override
    public Class<?> beforeAll() {
        return org.junit.jupiter.api.BeforeAll.class;
    }

    @Override
    public Class<?> beforeEach() {
        return org.junit.jupiter.api.BeforeEach.class;
    }

    @Override
    public Class<?> afterAll() {
        return org.junit.jupiter.api.AfterAll.class;
    }

    @Override
    public Class<?> afterEach() {
        return org.junit.jupiter.api.AfterEach.class;
    }

    private String getJUnitTestShortName() {
        if (Properties.ECLIPSE_PLUGIN) {
            String res = "";
            if (Properties.TARGET_CLASS.equals("EvoSuiteTest"))
                res = org.evosuite.annotations.EvoSuiteTest.class.getName();
            else
                res = "EvoSuiteTest";
            res += " (checked = false)";
            return res;
        } else {
            if (Properties.TARGET_CLASS.equals("Test"))
                return "org.junit.jupiter.api.Test";
            else
                return "Test";
        }
    }

    /* (non-Javadoc)
     * @see org.evosuite.junit.UnitTestAdapter#getImports()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getImports() {
        ArrayList<String> imports = new ArrayList<>();
        if ((Properties.ECLIPSE_PLUGIN) && (!Properties.TARGET_CLASS.equals("EvoSuiteTest")))
            imports.add(org.evosuite.annotations.EvoSuiteTest.class.getName());
        if (!Properties.TARGET_CLASS.equals("Test"))
            imports.add("org.junit.jupiter.api.Test");
        imports.add("import org.junit.jupiter.api.Timeout");
        imports.add("import java.util.concurrent.TimeUnit");

        return imports;
    }

    /* (non-Javadoc)
     * @see org.evosuite.junit.UnitTestAdapter#getImports()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getStaticImports() {
        return Collections.singletonList("org.junit.jupiter.api.Assertions.*");
    }

    /* (non-Javadoc)
     * @see org.evosuite.junit.UnitTestAdapter#getClassDefinition(java.lang.String)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClassDefinition(String testName) {
        return "public class " + testName;
    }


    public String getTimeoutAnnotation() {
        return "@Timeout(value = " + (Properties.TIMEOUT + 1000) + " , unit = TimeUnit.MILLISECONDS)";
    }

    /* (non-Javadoc)
     * @see org.evosuite.junit.UnitTestAdapter#getMethodDefinition(java.lang.String)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMethodDefinition(String testName) {
        //TODO remove once JUnit is fixed. See comments in Scaffolding regarding Timeout rule
        return "@" + getJUnitTestShortName() + NEWLINE + getTimeoutAnnotation()
                + NEWLINE + "public void " + testName + "()";
    }

    /* (non-Javadoc)
     * @see org.evosuite.junit.UnitTestAdapter#getSuite(java.util.List)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSuite(List<String> suites) {
        throw new UnsupportedOperationException("getSuite is not supported in JUNIT 5");
    }

    /* (non-Javadoc)
     * @see org.evosuite.junit.UnitTestAdapter#getTestString(org.evosuite.testcase.TestCase, java.util.Map)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTestString(int id, TestCase test, Map<Integer, Throwable> exceptions) {
        return test.toCode(exceptions);
    }

    /* (non-Javadoc)
     * @see org.evosuite.junit.UnitTestAdapter#getTestString(int, org.evosuite.testcase.TestCase, java.util.Map, org.evosuite.testcase.TestCodeVisitor)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTestString(int id, TestCase test,
                                Map<Integer, Throwable> exceptions, TestCodeVisitor visitor) {
        visitor.setExceptions(exceptions);
        test.accept(visitor);
        visitor.clearExceptions();
        return visitor.getCode();
    }

    @Override
    public void addNFR(StringBuilder builder) {
        builder.append(TestSuiteWriterUtils.METHOD_SPACE);
        builder.append("@").append(RegisterExtension.class.getCanonicalName()).append("\n");
        builder.append(TestSuiteWriterUtils.METHOD_SPACE);
        builder.append("public ").append(NonFunctionalRequirementExtension.class.getName()).append(" nfr = new ").append(NonFunctionalRequirementExtension.class.getName()).append("();\n\n");
    }
}
