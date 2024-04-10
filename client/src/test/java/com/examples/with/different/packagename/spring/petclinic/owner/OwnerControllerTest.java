package com.examples.with.different.packagename.spring.petclinic.owner;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.result.StatusResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Ignore
@RunWith(SpringRunner.class)
@WebMvcTest(OwnerController.class)
public class OwnerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OwnerRepository owners;

    @Test
    public void testMultiline() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/owners");
        requestBuilder = requestBuilder.param("lastName", "Smith");

        ResultActions resultActions = mockMvc.perform(requestBuilder);
        MvcResult mvcResult = resultActions.andReturn();

        StatusResultMatchers matcher = MockMvcResultMatchers.status();
        ResultMatcher resultMatcher = matcher.isOk();
        resultMatcher.match(mvcResult);
    }

    @Test
    public void testMultilineExact() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/owners");
        requestBuilder = requestBuilder.param("lastName", "Smith");

        ResultActions resultActions = mockMvc.perform(requestBuilder);

        StatusResultMatchers matcher = MockMvcResultMatchers.status();
        ResultMatcher resultMatcher = matcher.isOk();
        resultActions.andExpect(resultMatcher);
    }

    @Test
    public void testParamRequestBuilder() throws Exception {
        String paramName = "lastName";
        String paramValue = "Smith";
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/owners");
        requestBuilder = requestBuilder.param(paramName, paramValue);

        ResultActions resultActions = mockMvc.perform(requestBuilder);

        StatusResultMatchers matcher = MockMvcResultMatchers.status();
        ResultMatcher resultMatcher = matcher.isOk();
        resultActions.andExpect(resultMatcher);
    }

}
