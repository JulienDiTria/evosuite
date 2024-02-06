package org.evosuite.spring;

import com.examples.with.different.packagename.spring.petclinic.owner.OwnerController;
import org.junit.Test;

public class TestSpringSetup {

    @Test
    public void test0() {
        SpringSetup springSetup0 = new SpringSetup();
        springSetup0.processCandidateController(OwnerController.class);
        System.out.println(springSetup0.requestMappingHandlerMapping);
    }
}
