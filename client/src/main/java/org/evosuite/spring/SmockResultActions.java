package org.evosuite.spring;

public class SmockResultActions {

  SmockMvcResult mvcResult;

  public SmockResultActions(SmockMvcResult mvcResult) {
    this.mvcResult = mvcResult;
  }

  public SmockResultActions andExpect(SmockResultMatcher matcher) throws Exception {
    matcher.match(mvcResult);
    return this;
  }

  public SmockResultActions andDo(SmockResultHandler handler) throws Exception {
    handler.handle(mvcResult);
    return this;
  }

  public SmockMvcResult andReturn() {
    return mvcResult;
  }
}
