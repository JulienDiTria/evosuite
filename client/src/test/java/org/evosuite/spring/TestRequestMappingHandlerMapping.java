package org.evosuite.spring;

import com.examples.with.different.packagename.spring.petclinic.owner.OwnerController;
import com.examples.with.different.packagename.spring.petclinic.owner.OwnerRepository;
import org.evosuite.runtime.ViolatedAssumptionAnswer;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class TestRequestMappingHandlerMapping {

  @Test
  public void testDetectHandlerMethods_FromString() {
    RequestMappingHandlerMapping handlerMapping = new RequestMappingHandlerMapping();
    handlerMapping.processCandidateController("com.examples.with.different.packagename.spring.petclinic.owner.OwnerController");
    System.out.println(handlerMapping);
  }

  @Test
  public void testDetectHandlerMethods_FromObject() {
    OwnerRepository ownerRepository = mock(OwnerRepository.class, new ViolatedAssumptionAnswer());

    RequestMappingHandlerMapping handlerMapping = new RequestMappingHandlerMapping();
    handlerMapping.processCandidateController(new OwnerController(ownerRepository));
    System.out.println(handlerMapping);
  }

  @Test
  public void testDetectHandlerMethods() {
    OwnerRepository ownerRepository = mock(OwnerRepository.class, new ViolatedAssumptionAnswer());

    RequestMappingHandlerMapping handlerMapping = new RequestMappingHandlerMapping();
    handlerMapping.processCandidateController(new OwnerController(ownerRepository));
    String out1 = handlerMapping.toString();

    handlerMapping = new RequestMappingHandlerMapping();
    handlerMapping.processCandidateController("com.examples.with.different.packagename.spring.petclinic.owner.OwnerController");
    String out2 = handlerMapping.toString();

    Assert.assertEquals("both output are note equal", out1, out2);
  }

  @Test
  public void testDetectHandlerMethods_ErrorController() {
    RequestMappingHandlerMapping handlerMapping = new RequestMappingHandlerMapping();
    handlerMapping.processCandidateController("org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController");
    System.out.println(handlerMapping);
  }
}
