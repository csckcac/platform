/*
 *  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.entitlement.filter;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.carbon.identity.entitlement.filter.callback.BasicAuthCallBackHandler;
import org.wso2.carbon.identity.entitlement.filter.callback.EntitlementFilterCallBackHandler;
import org.wso2.carbon.identity.entitlement.filter.exception.EntitlementFilterException;
import org.wso2.carbon.identity.entitlement.proxy.PDPConfig;
import org.wso2.carbon.identity.entitlement.proxy.PDPProxy;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;


public class EntitlementFilter implements Filter {

    private static final Log log = LogFactory.getLog(EntitlementFilter.class);

    private String domainID ="EntitlementFilter";
    private String remoteServiceUserName;
    private String remoteServicePassword;
    private String remoteServiceHost;
    private String remoteServicePort;
    private String transportType;
    private String subjectScope;
    private String subjectAttributeName;
    private String decisionCaching;
    private String authRedirectURL;

    private FilterConfig filterConfig = null;
    private PDPProxy pClient;
    private int maxCacheEntries;

    @Override
    /**
     * In this init method the required attributes are taken from web.xml, if there are not provided they will be set to default.
     * authRedirectURL attribute have to provided
     */
    public void init(FilterConfig filterConfig) throws EntitlementFilterException {

        this.filterConfig = filterConfig;

        remoteServiceUserName = filterConfig.getServletContext().getInitParameter(EntitlementConstants.USER);
        remoteServicePassword = filterConfig.getServletContext().getInitParameter(EntitlementConstants.PASSWORD);
        remoteServiceHost = filterConfig.getServletContext().getInitParameter(EntitlementConstants.HOST);
        remoteServicePort = filterConfig.getServletContext().getInitParameter(EntitlementConstants.PORT);
        transportType = filterConfig.getServletContext().getInitParameter(EntitlementConstants.TRANSPORT);
        if(transportType==null){
            transportType=EntitlementConstants.defaultTransportType;
        }
        subjectScope = filterConfig.getServletContext().getInitParameter(EntitlementConstants.SUBJECT_SCOPE);
        if(subjectScope==null){
           subjectScope=EntitlementConstants.defaultSubjectScope;
        }
        subjectAttributeName = filterConfig.getServletContext().getInitParameter(EntitlementConstants.SUBJECT_ATTRIBUTE_NAME);
        decisionCaching = filterConfig.getInitParameter(EntitlementConstants.DECISION_CACHING);
        if(decisionCaching==null){
            decisionCaching=EntitlementConstants.defaultDecisionCaching;
        }
        maxCacheEntries = Integer.parseInt(filterConfig.getInitParameter(EntitlementConstants.MAX_CACHE_ENTRIES));
        if(filterConfig.getInitParameter(EntitlementConstants.MAX_CACHE_ENTRIES)==null){
            maxCacheEntries=Integer.parseInt(EntitlementConstants.defaultMaxCacheEntries);
        }

        //This Attribute is Mandatory So have to be specified in the web.xml
        authRedirectURL = filterConfig.getInitParameter(EntitlementConstants.AUTH_REDIRECT_URL);

        //Initializing the PDP Proxy
        //If you are not using a WSO2 product please uncomment these lines to use provided keystore
        //System.setProperty("javax.net.ssl.trustStore","wso2carbon.jks");
        //System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");

        pClient= PDPProxy.getInstance();
        Map<String, String[]> config=new HashMap<String, String[]>();
        String tempArr[]={"https://"+remoteServiceHost+":"+remoteServicePort+"/services/"};
        config.put("EntitlementFilter",tempArr) ;
        PDPConfig pConfig=new PDPConfig(remoteServiceUserName,remoteServicePassword,config,"EntitlementFilter",transportType,"enable".equals(decisionCaching),maxCacheEntries);
        pConfig.setAppToPDPMap(config);

        try {
            pClient.init(pConfig);
        } catch (Exception e) {
            log.error("Error while initializing the PDP Proxy" + e);
            throw new EntitlementFilterException("Error while initializing the PDP Proxy", e);

        }


    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws EntitlementFilterException {

        String decision = EntitlementConstants.DENY;
        String userName;
        String action;
        String resource;
        String[] env = new String[0];

        userName = findUserName((HttpServletRequest) servletRequest, subjectScope, subjectAttributeName);
        resource = findResource((HttpServletRequest) servletRequest);
        action = findAction((HttpServletRequest) servletRequest);

        if(((HttpServletRequest) servletRequest).getRequestURI().contains("/updateCacheAuth.do")) {

            try {
                decision=pClient.getActualDecisionByAttributes(userName, resource, action, env,domainID);
            } catch (Exception e) {
                log.error("Error while Making the Decision " , e);
            }

        } else {
            try {
                decision=pClient.getActualDecisionByAttributes(userName, resource, action, env,domainID);
            } catch (Exception e) {
                e.printStackTrace();
                throw new EntitlementFilterException("Exception while making the decision : " + e);
            }
        }
        System.out.println("Entitlement Decision for User :"+userName+" is :"+decision);
        completeAuthorization(decision, servletRequest, servletResponse, filterConfig, filterChain);

    }

    @Override
    public void destroy() {
        decisionCaching = null;
    }

    private String findUserName(HttpServletRequest request, String subjectScope,
                                String subjectAttributeName) throws EntitlementFilterException {
        String subject;
        if (subjectScope.equals(EntitlementConstants.SESSION)) {
            subject = (String) request.getSession(false).getAttribute(subjectAttributeName);
        } else if (subjectScope.equals(EntitlementConstants.REQUEST_PARAM)) {
            subject = request.getParameter(subjectAttributeName);
        } else if (subjectScope.equals(EntitlementConstants.REQUEST_ATTIBUTE)) {
            subject = (String) request.getAttribute(subjectAttributeName);
        } else if (subjectScope.equals(EntitlementConstants.Basic_Auth)) {
            EntitlementFilterCallBackHandler callBackHandler = new BasicAuthCallBackHandler(request);
            subject=callBackHandler.getUserName();
        } else {
            log.error(subjectScope + " is an invalid"
                      + " configuration for subjectScope parameter in web.xml. Valid configurations are"
                      + " \'" + EntitlementConstants.REQUEST_PARAM + "\', " + EntitlementConstants.REQUEST_ATTIBUTE + "\' and \'"
                      + EntitlementConstants.SESSION + "\'");

            throw new EntitlementFilterException(subjectScope + " is an invalid"
                                                 + " configuration for subjectScope parameter in web.xml. Valid configurations are"
                                                 + " \'" + EntitlementConstants.REQUEST_PARAM + "\', " + EntitlementConstants.REQUEST_ATTIBUTE + "\' and \'"
                                                 + EntitlementConstants.SESSION + "\'");
        }
        if (subject == null || subject.equals("null")) {
            log.error("Username not provided in " + subjectScope);
            throw new EntitlementFilterException("Username not provided in " + subjectScope);
        };
        return subject;
    }

    private String findResource(HttpServletRequest request) {
        return request.getRequestURI();
    }

    private String findAction(HttpServletRequest request) {
        return request.getMethod();
    }

    private void completeAuthorization(String decision, ServletRequest servletRequest,
                                       ServletResponse servletResponse, FilterConfig filterConfig,
                                       FilterChain filterChain)
            throws EntitlementFilterException {
        try {
            if (decision.equals(EntitlementConstants.PERMIT)) {
                if (((HttpServletRequest) servletRequest).getRequestURI().contains("/updateCacheAuth.do")) {
                    try {
                        init(filterConfig);
                    } catch (EntitlementFilterException e) {
                        throw new EntitlementFilterException("Error while updating PEP cache", e);
                    }
                    log.info("PEP cache has been updated");
                    servletResponse.getWriter().print("PEP cache has been updated");
                } else {
                    filterChain.doFilter(servletRequest, servletResponse);
                }
            } else if (decision.equals(EntitlementConstants.DENY)) {
                log.info("User not authorized to perform the action");
                servletRequest.getRequestDispatcher(authRedirectURL)
                        .forward(servletRequest, servletResponse);
            } else if (decision.equals(EntitlementConstants.NOT_APPLICABLE)) {
                log.info("No applicable policies found");
                servletRequest.getRequestDispatcher(authRedirectURL)
                        .forward(servletRequest, servletResponse);
            } else {
                log.error("Unrecognized decision returned from PDP");
                servletRequest.getRequestDispatcher(authRedirectURL)
                        .forward(servletRequest, servletResponse);
            }
        } catch (Exception e) {
            log.error("Error occurred while completing authorization", e);
            throw new EntitlementFilterException("Error occurred while completing authorization", e);
        }
    }

}
