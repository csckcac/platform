/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.redirector.servlet.ui.filters;

import org.wso2.carbon.stratos.common.constants.StratosConstants;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class DocPageFilter implements Filter {
    ServletContext context;
    public void init(FilterConfig filterConfig) throws ServletException {
        context = filterConfig.getServletContext();
    }

    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse, FilterChain filterChain) throws
            IOException, ServletException {
        if (!(servletRequest instanceof HttpServletRequest)) {
            // no filtering
            return;
        }
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        if (request.getAttribute(StratosConstants.TENANT_SPECIFIC_URL_RESOLVED) != null) {
            // if the tenant specifc url filter is passed, we are not calling the login.jsp filter
            return;
        }

        String url = request.getRequestURI();
        // use the name admin in place of admin
        url = url.replaceAll("admin/docs/userguide.html", "tenant-dashboard/docs/userguide.html");
        RequestDispatcher requestDispatcher =
                request.getRequestDispatcher(url);
        requestDispatcher.forward(request, servletResponse);
    }

    public void destroy() {
        // nothing to destory
    }
}
