package org.evosuite.spring;

import com.examples.with.different.packagename.spring.petclinic.owner.OwnerController;
import com.examples.with.different.packagename.spring.petclinic.owner.OwnerRepository;
import org.evosuite.runtime.ViolatedAssumptionAnswer;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class TestRequestMappingHandlerMapping {

  @Test
  public void testIsHandler() {
    OwnerRepository ownerRepository = mock(OwnerRepository.class, new ViolatedAssumptionAnswer());

    assertTrue(RequestMappingHandlerMapping.isHandlerType("com.examples.with.different.packagename.spring.petclinic.owner.OwnerController"));
    assertTrue(RequestMappingHandlerMapping.isHandlerType(new OwnerController(ownerRepository)));
    assertTrue(RequestMappingHandlerMapping.isHandlerType("org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController"));
  }
}
