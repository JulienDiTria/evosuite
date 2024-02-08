package org.evosuite.spring;

import com.examples.with.different.packagename.spring.petclinic.owner.OwnerController;
import com.examples.with.different.packagename.spring.petclinic.owner.OwnerControllerTest;
import org.junit.Test;

public class TestSpringSetup {

    @Test
    public void test0() {
        SpringSetup.processCandidateController(OwnerController.class);
        System.out.println(SpringSetup.getRequestMappingHandlerMapping());
    }

    @Test
    public void test1() throws Exception {
        SpringSetup.setup(OwnerControllerTest.class);
        System.out.println("all good");
    }
}
