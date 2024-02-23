/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * License from repo org/springframework/spring-webmvc/5.1.2.RELEASE
 * org/springframework/web/servlet/handler/AbstractHandlerMethodMapping.MappingRegistry
 * Code taken and adapted to work with EvoSuite
 */

package org.evosuite.spring;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.lang.Nullable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

public abstract class AbstractMappingRegistry<T> {

    private final Map<T, HandlerMethod> mappingLookup = new LinkedHashMap<>();
    private final MultiValueMap<String, T> urlLookup = new LinkedMultiValueMap<>();


    public void register(T mapping, Class<?> handler, Method method) {
        this.mappingLookup.put(mapping, new HandlerMethod(handler, method));
        List<String> directUrls = getDirectUrls(mapping);
        for (String url : directUrls) {
            this.urlLookup.add(url, mapping);
        }
    }

    public List<T> getMappings(){
        return new ArrayList<>(this.mappingLookup.keySet());
    }

    /**
     * Return matches for the given URL path. Not thread-safe.
     */
    @Nullable
    public List<T> getMappingsByUrl(String urlPath) {
        return this.urlLookup.get(urlPath);
    }

    private List<String> getDirectUrls(T mapping) {
        List<String> urls = new ArrayList<>(1);
        urls.addAll(getMappingPathPatterns(mapping));
        return urls;
    }

    /**
     * Get the URL path patterns associated with this {@link RequestMappingInfo}.
     */
    protected abstract Set<String> getMappingPathPatterns(T info);

    @Override
    public String toString() {
        return this.mappingLookup.entrySet().stream()
            .map(entry -> entry.getKey().toString() + " -> " + entry.getValue().getMethod().toString())
            .collect(Collectors.joining(",\n ", "[", "]"));
    }


}
