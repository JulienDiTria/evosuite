package org.evosuite.spring;

import com.examples.with.different.packagename.ClassWithOverloadedConstructor;
import com.examples.with.different.packagename.spring.petclinic.owner.OwnerController;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SpringClassTestContextTest {

    private static void assertContains(String lookingFor, String lookingIn) {
        if (!lookingIn.contains(lookingFor)) {
            throw new AssertionError("Expected to find: " + lookingFor + " in: " + lookingIn);
        }
    }

    @Test
    public void testWithController() {
        Class<?> klass = OwnerController.class;

        SpringClassTestContext springClassTestContext = new SpringClassTestContext(klass);
        String testSuiteContent = springClassTestContext.createTestSuiteContent();
        String testSuiteName = springClassTestContext.getTestSuiteName();

        assertEquals(klass.getSimpleName() + "_Spring_ESTest", testSuiteName);
        assertContains("@RunWith(SpringRunner.class)", testSuiteContent);
        assertContains("@WebMvcTest(" + klass.getSimpleName() + ".class)", testSuiteContent);
        assertContains("@MockBean", testSuiteContent);
        assertContains("@Autowired", testSuiteContent);
        assertContains("private MockMvc mockMvc;", testSuiteContent);

    }

    @Test
    public void testWithoutController() {
        Class<?> klass = ClassWithOverloadedConstructor.class;

        SpringClassTestContext springClassTestContext = new SpringClassTestContext(klass);
        String testSuiteContent = springClassTestContext.createTestSuiteContent();
        String testSuiteName = springClassTestContext.getTestSuiteName();

        assertEquals(klass.getSimpleName() + "_Spring_ESTest", testSuiteName);
        assertContains("@RunWith(SpringRunner.class)", testSuiteContent);
        assertContains("@WebMvcTest(" + klass.getSimpleName() + ".class)", testSuiteContent);
        assertContains("@Autowired", testSuiteContent);
        assertContains("private MockMvc mockMvc;", testSuiteContent);
    }
}
