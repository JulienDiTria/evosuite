package org.evosuite.spring;

import org.junit.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

public class TestSpringTestFactory {
  @Test
  public void testCreateTestCaseForSpringRequestMapping() throws Exception {
    RequestMappingInfo requestMappingInfo = RequestMappingInfo.paths("/owners/new").build();
    SpringTestFactory.createTestCaseForRequestMapping(requestMappingInfo);
  }
}
