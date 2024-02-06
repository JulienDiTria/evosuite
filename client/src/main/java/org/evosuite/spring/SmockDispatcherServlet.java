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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

public class SmockDispatcherServlet {

    public void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HandlerExecutionChain mappedHandler = null;
        ModelAndView mv = null;

        // Determine handler for the current request.
        mappedHandler = getHandler(request);
        if (mappedHandler == null) {
            noHandlerFound(request, response);
            return;
        }

        // Determine handler adapter for the current request.
        HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());

        // Actually invoke the handler.
        mv = ha.handle(request, response, mappedHandler.getHandler());

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
    protected HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
        RequestMappingHandlerMapping mapping = SpringSetup.getRequestMappingHandlerMapping();
        return mapping.getHandler(request);
    }

    /**
     * No handler found -> set appropriate HTTP response status.
     * @param request current HTTP request
     * @param response current HTTP response
     * @throws Exception if preparing the response failed
     */
    protected void noHandlerFound(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (pageNotFoundLogger.isWarnEnabled()) {
            pageNotFoundLogger.warn("No mapping for " + request.getMethod() + " " + getRequestUri(request));
        }
        if (this.throwExceptionIfNoHandlerFound) {
            throw new NoHandlerFoundException(request.getMethod(), getRequestUri(request),
                new ServletServerHttpRequest(request).getHeaders());
        }
        else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Return the HandlerAdapter for this handler object.
     * @param handler the handler object to find an adapter for
     * @throws ServletException if no HandlerAdapter can be found for the handler. This is a fatal error.
     */
    protected HandlerAdapter getHandlerAdapter(Object handler) throws ServletException {
        if (this.handlerAdapters != null) {
            for (HandlerAdapter adapter : this.handlerAdapters) {
                if (adapter.supports(handler)) {
                    return adapter;
                }
            }
        }
        throw new ServletException("No adapter for handler [" + handler +
            "]: The DispatcherServlet configuration needs to include a HandlerAdapter that supports this handler");
    }

    /**
     * Do we need view name translation?
     */
    private void applyDefaultViewName(HttpServletRequest request, @Nullable ModelAndView mv) throws Exception {
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
    protected String getDefaultViewName(HttpServletRequest request) throws Exception {
        return (this.viewNameTranslator != null ? this.viewNameTranslator.getViewName(request) : null);
    }
}
