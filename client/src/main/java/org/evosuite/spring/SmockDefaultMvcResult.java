package org.evosuite.spring;

public class SmockDefaultMvcResult implements SmockMvcResult {

  private final SmockRequest mockRequest;
  private final SmockResponse mockResponse;

  public SmockDefaultMvcResult(SmockRequest request, SmockResponse response) {
    this.mockRequest = request;
    this.mockResponse = response;
  }

  @Override
  public SmockRequest getRequest() {
    return this.mockRequest;
  }

  @Override
  public SmockResponse getResponse() {
    return this.mockResponse;
  }
}
