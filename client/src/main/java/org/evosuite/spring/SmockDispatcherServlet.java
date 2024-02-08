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
 * License from repo org/springframework/spring-webmvc/5.1.2.RELEASE/
 * org/springframework/web/servlet/DispatcherServlet.java
 * Code taken and adapted to work with EvoSuite
 */

package org.evosuite.spring;

import java.lang.invoke.MethodHandles;
import javax.servlet.ServletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

public class SmockDispatcherServlet {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public void doDispatch(SmockRequest request, SmockResponse response) throws Exception {
        SmockHandlerExecutionChain mappedHandler = null;
        ModelAndView mv = null;

        // Determine handler for the current request.
        mappedHandler = getHandler(request);
        if (mappedHandler == null) {
            noHandlerFound(request, response);
            return;
        }

        // Determine handler adapter for the current request.
        SmockHandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());

        // Actually invoke the handler.
//        mv = ha.handle(request, response, mappedHandler.getHandler());

        // If the view is null, we assume that the request is handled within the handler itself.
        applyDefaultViewName(request, mv);
    }

    /**
     * Return the HandlerExecutionChain for this request.
     * <p>Tries all handler mappings in order.
     * @param request current HTTP request
     * @return the HandlerExecutionChain, or {@code null} if no handler could be found
     */
    @Nullable
    protected SmockHandlerExecutionChain getHandler(SmockRequest request) throws Exception {
        RequestMappingHandlerMapping mapping = SpringSetup.getRequestMappingHandlerMapping();
        return mapping.getHandler(request);
    }

    /**
     * No handler found -> set appropriate HTTP response status.
     * @param request current HTTP request
     * @param response current HTTP response
     * @throws Exception if preparing the response failed
     */
    protected void noHandlerFound(SmockRequest request, SmockResponse response) throws Exception {
//        logger.warn("No mapping for " + request.getMethod() + " " + getRequestUri(request));
//        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    /**
     * Return the HandlerAdapter for this handler object.
     * @param handler the handler object to find an adapter for
     * @throws ServletException if no HandlerAdapter can be found for the handler. This is a fatal error.
     */
    protected SmockHandlerAdapter getHandlerAdapter(Object handler) throws ServletException {
//        if (this.handlerAdapters != null) {
//            for (SmockHandlerAdapter adapter : this.handlerAdapters) {
//                if (adapter.supports(handler)) {
//                    return adapter;
//                }
//            }
//        }
        throw new ServletException("No adapter for handler [" + handler +
            "]: The DispatcherServlet configuration needs to include a HandlerAdapter that supports this handler");
    }

    /**
     * Do we need view name translation?
     */
    private void applyDefaultViewName(SmockRequest request, @Nullable ModelAndView mv) throws Exception {
        if (mv != null && !mv.hasView()) {
            String defaultViewName = getDefaultViewName(request);
            if (defaultViewName != null) {
                mv.setViewName(defaultViewName);
            }
        }
    }

    /**
     * Translate the supplied request into a default view name.
     * @param request current HTTP servlet request
     * @return the view name (or {@code null} if no default found)
     * @throws Exception if view name translation failed
     */
    @Nullable
    protected String getDefaultViewName(SmockRequest request) throws Exception {
        return null;
//        return (this.viewNameTranslator != null ? this.viewNameTranslator.getViewName(request) : null);
    }
}
