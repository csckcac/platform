/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.salesforce.webapp.servlet;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class SessionFilter implements Filter {
    private List<String> urlList;   

    public void init(FilterConfig filterConfig) throws ServletException {
        String urls = filterConfig.getInitParameter("avoid-urls");
        StringTokenizer tokenizer = new StringTokenizer(urls, ",");
        urlList = new ArrayList<String>();
        while (tokenizer.hasMoreTokens()) {
            urlList.add(tokenizer.nextToken());
        }
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String url = request.getServletPath();
        boolean allowedRequest = false;
        if (urlList.contains(url)) {
            allowedRequest = true;
        }
        if (!allowedRequest) {
            HttpSession session = request.getSession();
            if (session == null) {
                response.sendRedirect("login.jsp?error=Invalid Session. Please login again!");
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    public void destroy() {

    }
}
