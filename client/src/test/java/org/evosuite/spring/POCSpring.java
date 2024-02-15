package org.evosuite.spring;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.StatusResultMatchers;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class POCSpring {

    @Test
    public void testPOC() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get("/owners");
        requestBuilder = requestBuilder.param("lastName", "Smith");

        ResultActions resultActions = new ResultActions() {
            @Override
            public ResultActions andExpect(ResultMatcher matcher) throws Exception {
                return null;
            }

            @Override
            public ResultActions andDo(ResultHandler handler) throws Exception {
                return null;
            }

            @Override
            public MvcResult andReturn() {
                return new MvcResult() {
                    @Override
                    public MockHttpServletRequest getRequest() {
                        return null;
                    }

                    @Override
                    public MockHttpServletResponse getResponse() {
                        return new MockHttpServletResponse();
                    }

                    @Override
                    public Object getHandler() {
                        return null;
                    }

                    @Override
                    public HandlerInterceptor[] getInterceptors() {
                        return new HandlerInterceptor[0];
                    }

                    @Override
                    public ModelAndView getModelAndView() {
                        return null;
                    }

                    @Override
                    public Exception getResolvedException() {
                        return null;
                    }

                    @Override
                    public FlashMap getFlashMap() {
                        return null;
                    }

                    @Override
                    public Object getAsyncResult() {
                        return null;
                    }

                    @Override
                    public Object getAsyncResult(long timeToWait) {
                        return null;
                    }
                };
            }
        };
        MvcResult result = resultActions.andReturn();

        StatusResultMatchers status = status();
        ResultMatcher ok = status.isOk();
        ok.match(result);
    }
}
