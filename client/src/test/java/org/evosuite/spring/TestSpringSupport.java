package org.evosuite.spring;

import com.examples.with.different.packagename.spring.petclinic.owner.OwnerController;
import org.junit.Test;

public class TestSpringSupport {

    @Test
    public void test0() {
        SpringSupport.processCandidateController(OwnerController.class);
        System.out.println(SpringSupport.getRequestMappingHandlerMapping());
    }

    @Test
    public void test1() throws Exception {
        SpringSupport.setup(OwnerController.class.getName());
        System.out.println("all good");
    }
}
