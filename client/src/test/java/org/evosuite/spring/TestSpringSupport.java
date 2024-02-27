package org.evosuite.spring;

import com.examples.with.different.packagename.spring.petclinic.owner.OwnerController;
import org.junit.Test;

public class TestSpringSupport {

    @Test
    public void test0() {
        SpringSupport.setup(OwnerController.class.getName());
        System.out.println("all good");
    }
}
