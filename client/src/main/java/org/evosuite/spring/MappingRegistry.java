package org.evosuite.spring;

import java.util.Set;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;


public class MappingRegistry extends AbstractMappingRegistry<RequestMappingInfo> {

    /**
     * Get the URL path patterns associated with this {@link RequestMappingInfo}.
     */
    @Override
    protected Set<String> getMappingPathPatterns(RequestMappingInfo info) {
        return info.getPatternsCondition().getPatterns();
    }

}
