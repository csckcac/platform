/**
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.mashup.jsservices.custom.ui;

import org.apache.axis2.description.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axiom.om.OMException;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.core.util.MIMEType2FileExtensionMap;
import org.wso2.carbon.mashup.utils.MashupConstants;
import org.wso2.carbon.mashup.jsservices.custom.ui.internal.CustomUIServiceComponent;
import org.wso2.carbon.mashup.utils.MashupUtils;
import org.wso2.carbon.utils.deployment.GhostDeployerUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.File;

public class CustomUIServletFilter implements Filter {
    private static Log log = LogFactory.getLog(CustomUIServletFilter.class);

    /**
     * This init method will be invoked at MainServlet. This will be called at
     * the startup time as well as when the system is re-starting.
     *
     * @param filterConfig - FilterConfig
     * @throws ServletException
     */
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        //We jump in only if this is a HTTP Get request
        if ((httpServletRequest.getMethod().equalsIgnoreCase("get"))
                && (httpServletRequest.getQueryString() == null)) {

            String contextRoot = CustomUIServiceComponent.getCarbonContextRoot();
            if (!contextRoot.endsWith("/")) {
                contextRoot = contextRoot + "/";
            }
            String serviceRoot = CustomUIServiceComponent.getConfigurationContext().getServicePath();
            String fullRoot = contextRoot + serviceRoot;
            String requestURI = ((HttpServletRequest) request).getRequestURI();

            //Ignore if the requestURI does not start with service's root
            if (requestURI.startsWith(fullRoot, 0)) {

                //check whether the request is for a tenant
                String servicePath = requestURI.substring(fullRoot.length());
                //subPath can be either /t/foo.com/admin/digit2image... or /admin/digit2image...
                // we need to check whether t refers to tenant root or a dir within jsservices
                // i.e. it can be /t/digit2image...
                int tIndex = servicePath.indexOf("/t/");
                String requestPath = servicePath;
                String tenant = null;
                if (tIndex != -1) {
                    tenant = MultitenantUtils.getTenantDomainFromRequestURL(servicePath);
                    requestPath = servicePath.substring(tIndex + tenant.length() + 4);
                } else {
                    requestPath = requestPath.substring(1);
                }

                // rip off the contextroot & the service root from the request URI
                // Eg: '/service/admin/digit2image/digit2image/images/1.gif' will be
                // ripped off to 'admin/digit2image/digit2image/images/1.gif'

                String[] requestParts = requestPath.split("/");

                // js services are deployed in the 3rd level of the service heirarchy.
                // Eg: jsservices/admin/digit2image/digit2image.js
                if (requestParts.length >= 2) {

                    // if this is a js services, then the services name should be like admin/foo/bar
                    String serviceName = requestParts[0] + "/" + requestParts[1];
                    AxisService axisService;
                    if(tenant != null) {
                        axisService = TenantAxisUtils.getTenantAxisConfiguration(tenant,
                                                CustomUIServiceComponent.getConfigurationContext()).
                                getServiceForActivation(serviceName);
                         if(axisService != null ) {
                            Parameter ghostParam = axisService.getParameter(CarbonConstants.GHOST_SERVICE_PARAM);
                            if (ghostParam != null && "true".equals(ghostParam.getValue())) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Request is heading to a ghost service. Deploying the actual service.");
                                }

                                // now we have to deploy the actual service
                                // remove the ghost service and deploy the actual service
                                axisService = GhostDeployerUtils.deployActualService(TenantAxisUtils.getTenantAxisConfiguration(tenant,
                                        CustomUIServiceComponent.getConfigurationContext()), axisService);
                            }
                        }
                    } else {
                        axisService = CustomUIServiceComponent.getConfigurationContext().
                                getAxisConfiguration().getServiceForActivation(serviceName);
                    }
                    //Serve the custom UI only if the service is deployed and active.
                    if (axisService != null && MashupConstants.JS_SERVICE.equals(
                            axisService.getParameterValue("serviceType"))) {

                        if (!axisService.isActive()) {
                            OutputStream outputStream = httpServletResponse.getOutputStream();
                            httpServletResponse.setContentType("text/html");
                            httpServletResponse.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                            outputStream
                                    .write(("<h4>Service " + serviceName +
                                            " is inactive. Cannot display <em>web resources</em>.</h4>")
                                            .getBytes());
                            outputStream.flush();
                            return;
                        }
                        //We assume that a file name will have '.'
                        int indexOfDot = requestPath.lastIndexOf(".");
                        boolean isFile = false;

                        try {
                            if (indexOfDot != -1) {
                                String subString = requestPath.substring(indexOfDot);
                                isFile = subString.matches("\\.(.)*");
                                // admin/hello/hello.endpoint/images/test.gif  = requestPath
                                //.gif = subString
                                // Checking weather the request is of the form serviceName.endpointName if so its a false allarm
                                if (requestPath.length() > indexOfDot + 1) {
                                    int indexOfSlash = subString.indexOf("/");
                                    if (indexOfSlash != -1) {
                                        String endpointName = subString.substring(1, indexOfSlash);
                                        if (axisService.getEndpoint(endpointName) != null) {
                                            isFile = subString.substring(indexOfDot + 1).matches("\\.(.)*");
                                        }
                                    }
                                }

                            }

                            //Retrieve the resources folder. JSDeployer make sure to put this parameter.
                            Object resourceFolderObject = axisService.getParameterValue(MashupConstants.RESOURCES_FOLDER);
                            File serviceResourceFolder = null;
                            if (resourceFolderObject != null && resourceFolderObject instanceof File) {
                                serviceResourceFolder = (File) resourceFolderObject;
                            }

                            File file;
                            if (requestPath.equals(serviceName + "/")) {
                                if (serviceResourceFolder != null) {
                                    //Serves the '{serviceFileName.resources}/www/index.html' or the 'index.htm' page.
                                    serviceResourceFolder = (File) resourceFolderObject;

                                    file = new File(serviceResourceFolder + "/www/" + "index.html");
                                    if (!file.exists()) {
                                        file = new File(serviceResourceFolder + "/www/" + "index.htm");
                                    }
                                    if (file.exists()) {
                                        response.setContentType("text/html");
                                        MashupUtils.writeFile(httpServletResponse, file);
                                        return;
                                    }
                                }
                                //Redirect to the ?tryit if an 'index.html' or a 'index.htm' page is not available in
                                //the '{serviceFileName.resources}/www' folder.

                                //As we redirect the request to 'serviceEpr/' even when the request was made to 'serviceEpr',
                                //we can guarantee the existence of '/' at the end of the URI.
                                requestURI = requestURI.substring(0, requestURI.length() - 1);
                                httpServletResponse.sendRedirect(requestURI + "?tryit");
                                return;
                            }
                            if (requestPath.equals(serviceName)) {
                                //requests to the 'serviceEpr' will be redirected to the 'serviceEpr/'
                                //This is to deal with the issues that arises due to the relative path javascript imports
                                httpServletResponse.sendRedirect(requestURI + "/");
                                return;
                            }

                            //The request can be to a file in the '{serviceFileName.resources}/www' folder or a subfolder.
                            //For an example for a request to http://localhost:9762/services/version/images/logo.gif will
                            // be served with version.resources/www/images/logo.gif
                            if (isFile) {
                                if (serviceResourceFolder != null) {
                                    serviceResourceFolder = (File) resourceFolderObject;

                                    String subPath = requestPath.substring(requestPath.indexOf(serviceName) +
                                            serviceName.length() + 1);
                                    file = new File(serviceResourceFolder.getAbsolutePath() + "/www/"
                                            + subPath);
                                    if (file.exists()) {
                                        MIMEType2FileExtensionMap map = CustomUIServiceComponent.getExtensionMap();
                                        if (map != null) {
                                            response.setContentType(map.getMIMEType(file));
                                        }
                                        MashupUtils.writeFile(httpServletResponse, file);
                                        return;
                                    }
                                }
                                //We throw 'resource not found' error if file or the folder does not exist
                                httpServletResponse.sendError(404);
                                return;
                            }
                        } catch (OMException e) {
                            log.error(e);
                        } catch (Exception e) {
                            log.error(e);
                        }
                    }
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    public void destroy() {
    }
}
