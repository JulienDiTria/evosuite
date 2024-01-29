package org.evosuite.spring;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class SmockRequestBuilder {

//      private static HTTP

      private final String method;

      private final URI url;

      private final MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

      public SmockRequestBuilder(HttpMethod httpMethod, String url) throws URISyntaxException {
            this(httpMethod.name(), new URI(url));
      }

      private SmockRequestBuilder(String method, URI url) {
            Assert.notNull(method, "'httpMethod' is required");
            Assert.notNull(url, "'url' is required");
            this.method = method;
            this.url = url;
      }

      SmockRequest buildRequest() throws Exception {
            SmockRequest request = new SmockRequest();
//            request.setMethod(this.method);
//            request.setUrl(this.url);
//            request.setParameters(this.parameters);
//            request.();
            return request;
      }

      public SmockRequestBuilder param(String name, String... values) {
            addToMultiValueMap(this.parameters, name, values);
            return this;
      }

      private static <T> void addToMultiValueMap(MultiValueMap<String, T> map, String name, T[] values) {
            Assert.hasLength(name, "'name' must not be empty");
            Assert.notEmpty(values, "'values' must not be empty");
            for (T value : values) {
                  map.add(name, value);
            }
      }
}
