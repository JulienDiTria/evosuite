package org.evosuite.spring;


public class SmockMvc {
  public SmockResultActions perform(SmockRequestBuilder requestBuilder) throws Exception {

    // build the request
    SmockRequest request = requestBuilder.buildRequest();

    // execute the request
    SmockResponse response = request.execute();

    // put the response into a mvc result
    final SmockMvcResult mvcResult = new SmockDefaultMvcResult(request, response);

    // wrap around the mvc result
    return new SmockResultActions(mvcResult);
  }
}
