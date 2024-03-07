package org.evosuite.spring;

import com.examples.with.different.packagename.ClassWithOverloadedConstructor;
import com.examples.with.different.packagename.spring.petclinic.owner.OwnerController;
import com.examples.with.different.packagename.spring.petclinic.owner.PetController;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SpringClassTestContextTest {

    private static void assertContains(String lookingFor, String lookingIn) {
        if (!lookingIn.contains(lookingFor)) {
            throw new AssertionError("Expected to find: " + lookingFor + " in: " + lookingIn);
        }
    }

    private static void assertContains(String lookingFor, String lookingIn, int times) {
        int count = StringUtils.countMatches(lookingIn, lookingFor);
        if (count != times) {
            throw new AssertionError("Expected to find " + times + " occurrences of: " + lookingFor + " in: " + lookingIn + " but found "
                + "only" + count);
        }
    }

    @Test
    public void testWithOwnerController() {
        Class<?> klass = OwnerController.class;

        SpringClassTestContext springClassTestContext = new SpringClassTestContext(klass);
        String testSuiteContent = springClassTestContext.createTestSuiteContent();
        String testSuiteName = springClassTestContext.getTestSuiteName();

        assertEquals(klass.getSimpleName() + "_Spring_ESTest", testSuiteName);
        assertContains("@RunWith(SpringRunner.class)", testSuiteContent);
        assertContains("@WebMvcTest(" + klass.getSimpleName() + ".class)", testSuiteContent);
        assertContains("@MockBean", testSuiteContent, 1);
        assertContains("@Autowired", testSuiteContent);
        assertContains("private MockMvc mockMvc0;", testSuiteContent);
        System.out.println(testSuiteContent);
    }

    @Test
    public void testWithPetController() {
        Class<?> klass = PetController.class;

        SpringClassTestContext springClassTestContext = new SpringClassTestContext(klass);
        String testSuiteContent = springClassTestContext.createTestSuiteContent();
        String testSuiteName = springClassTestContext.getTestSuiteName();

        assertEquals(klass.getSimpleName() + "_Spring_ESTest", testSuiteName);
        assertContains("@RunWith(SpringRunner.class)", testSuiteContent);
        assertContains("@WebMvcTest(" + klass.getSimpleName() + ".class)", testSuiteContent);
        assertContains("@MockBean", testSuiteContent, 2);
        assertContains("@Autowired", testSuiteContent);
        assertContains("private MockMvc mockMvc0;", testSuiteContent);
        System.out.println(testSuiteContent);
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
        assertContains("@MockBean", testSuiteContent, 0);
        assertContains("@Autowired", testSuiteContent);
        assertContains("private MockMvc mockMvc0;", testSuiteContent);
        System.out.println(testSuiteContent);
    }
}
