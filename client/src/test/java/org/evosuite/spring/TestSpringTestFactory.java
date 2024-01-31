package org.evosuite.spring;

import org.evosuite.Properties;
import org.evosuite.testcase.TestCase;
import org.junit.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;


public class TestSpringTestFactory {
  @Test
  public void testCreateTestCaseForSpringRequestMapping() throws Exception {
    RequestMappingInfo requestMappingInfo =
        RequestMappingInfo
            .paths("/owners/new")
            .methods(RequestMethod.GET)
            .params("lastName=Smith")
            .build();
    TestCase test = SpringTestFactory.createTestCaseForRequestMapping(requestMappingInfo);
    System.out.println(test);
  }
}
