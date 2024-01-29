package org.evosuite.spring;

import com.examples.with.different.packagename.spring.petclinic.owner.OwnerController;
import com.examples.with.different.packagename.spring.petclinic.owner.OwnerRepository;
import org.evosuite.runtime.ViolatedAssumptionAnswer;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class TestRequestMappingHandlerMapping {

  @Test
  public void testDetectHandlerMethod_FromString() {
    RequestMappingHandlerMapping handlerMapping = new RequestMappingHandlerMapping();
    handlerMapping.detectHandlerMethods("com.examples.with.different.packagename.spring.petclinic.owner.OwnerController");
    System.out.println(handlerMapping);
  }

  @Test
  public void testDetectHandlerMethod_FromObject() {
    OwnerRepository ownerRepository = mock(OwnerRepository.class, new ViolatedAssumptionAnswer());

    RequestMappingHandlerMapping handlerMapping = new RequestMappingHandlerMapping();
    handlerMapping.detectHandlerMethods(new OwnerController(ownerRepository));
    System.out.println(handlerMapping);
  }

  @Test
  public void testDetectHandlerMethodObject() {
    OwnerRepository ownerRepository = mock(OwnerRepository.class, new ViolatedAssumptionAnswer());

    RequestMappingHandlerMapping handlerMapping = new RequestMappingHandlerMapping();
    handlerMapping.detectHandlerMethods(new OwnerController(ownerRepository));
    System.out.println(handlerMapping);
  }
}
