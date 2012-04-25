/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.webapp.mgt;

import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.core.StandardContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.utils.multitenancy.CarbonContextHolder;

import java.io.File;
import java.lang.management.ManagementPermission;
import java.util.List;
import java.util.Map;

/**
 * This deployer is responsible for deploying/undeploying/updating those webapps.
 *
 * @see org.wso2.carbon.tomcat
 */
@SuppressWarnings("unused")
public class TomcatGenericWebappsDeployer {

    private static Log log = LogFactory.getLog(TomcatGenericWebappsDeployer.class);

    private String webContextPrefix;
    private int tenantId;
    private String tenantDomain;

    private WebApplicationsHolder webappsHolder;

    /**
     * Constructor
     *
     * @param webContextPrefix The Web context prefix
     * @param tenantId The tenant ID of the tenant to whom this deployer belongs to
     * @param tenantDomain  The tenant domain of the tenant to whom this deployer belongs to
     * @param webappsHolder    WebApplicationsHolder
     */
    public TomcatGenericWebappsDeployer(String webContextPrefix,
                                        int tenantId,
                                        String tenantDomain,
                                        WebApplicationsHolder webappsHolder) {
        SecurityManager secMan = System.getSecurityManager();
        if (secMan != null) {
            secMan.checkPermission(new ManagementPermission("control"));
        }
        this.tenantId = tenantId;
        this.tenantDomain = tenantDomain;
        this.webContextPrefix = webContextPrefix;
        this.webappsHolder = webappsHolder;
    }

    /**
     * Deploy webapps
     *
     * @param webappFile The webapp file to be deployed
     * @param webContextParams  context-params for this webapp
     * @param applicationEventListeners Application event listeners
     * @throws CarbonException If a deployment error occurs
     */
    public void deploy(File webappFile,
                       List<WebContextParameter> webContextParams,
                       List<Object> applicationEventListeners) throws CarbonException {
        CarbonContextHolder currentCarbonContextHolder =
                CarbonContextHolder.getCurrentCarbonContextHolder();
        currentCarbonContextHolder.startTenantFlow();
        try {
            currentCarbonContextHolder.setTenantId(tenantId);
            currentCarbonContextHolder.setTenantDomain(tenantDomain);
            long lastModifiedTime = webappFile.lastModified();
            WebApplication deployedWebapp =
                    webappsHolder.getStartedWebapps().get(webappFile.getName());
            WebApplication undeployedWebapp =
                    webappsHolder.getStoppedWebapps().get(webappFile.getName());
            WebApplication faultyWebapp =
                    webappsHolder.getFaultyWebapps().get(webappFile.getName());
            if (deployedWebapp == null && faultyWebapp == null && undeployedWebapp == null) {
                handleHotDeployment(webappFile, webContextParams, applicationEventListeners);
            } else if (deployedWebapp != null &&
                    deployedWebapp.getLastModifiedTime() != lastModifiedTime) {
                handleHotUpdate(deployedWebapp, webContextParams, applicationEventListeners);
            } else if (faultyWebapp != null &&
                    faultyWebapp.getLastModifiedTime() != lastModifiedTime) {
                handleHotDeployment(webappFile, webContextParams, applicationEventListeners);
            }
        } finally {
            currentCarbonContextHolder.endTenantFlow();
        }
    }

    /**
     * Hot deploy a webapp. i.e., deploy a webapp that has newly become available.
     *
     * @param webapp The webapp WAR or directory that needs to be deployed
     * @param webContextParams ServletContext params for this webapp
     * @param applicationEventListeners Application event listeners
     * @throws CarbonException If an error occurs during deployment
     */
    private void handleHotDeployment(File webapp, List<WebContextParameter> webContextParams,
                                     List<Object> applicationEventListeners)
            throws CarbonException {
        String filename = webapp.getName();
        if (webapp.isDirectory()) {
            handleExplodedWebappDeployment(webapp, webContextParams, applicationEventListeners);
        } else if (filename.endsWith(".war")) {
            handleWarWebappDeployment(webapp, webContextParams, applicationEventListeners);
        }
    }

    /**
     * Handle the deployment of a an archive webapp. i.e., a WAR
     *
     * @param webappWAR The WAR webapp file
     * @param webContextParams ServletContext params for this webapp
     * @param applicationEventListeners Application event listeners
     * @throws CarbonException If a deployment error occurs
     */
    private void handleWarWebappDeployment(File webappWAR,
                                           List<WebContextParameter> webContextParams,
                                           List<Object> applicationEventListeners)
            throws CarbonException {
        String filename = webappWAR.getName();
        String warContext = "";
        if (filename.equals("ROOT.war")) {  // FIXME: This is not working for some reason!
            if (webContextPrefix != null && !webContextPrefix.endsWith("/")) {
                warContext = "/";
            }
        } else {
            warContext = filename.substring(0, filename.indexOf(".war"));
        }
        if (!warContext.equals("/") && webContextPrefix.length() == 0) {
            webContextPrefix = "/";
        }
        handleWebappDeployment(webappWAR, webContextPrefix + warContext,
                webContextParams, applicationEventListeners);
    }

    /**
     * Handle the deployment of a an exploded webapp. i.e., a webapp deployed as a directory
     * & not an archive
     *
     * @param webappDir The exploded webapp directory
     * @param webContextParams ServletContext params for this webapp
     * @param applicationEventListeners Application event listeners
     * @throws CarbonException If a deployment error occurs
     */
    private void handleExplodedWebappDeployment(File webappDir,
                                                List<WebContextParameter> webContextParams,
                                                List<Object> applicationEventListeners)
            throws CarbonException {
        String filename = webappDir.getName();
        String warContext = "";
        if (filename.equals("ROOT")) {
            if (webContextPrefix != null && !webContextPrefix.endsWith("/")) {
                warContext = "/";
            }
        } else {
            warContext = filename;
        }
        if (!warContext.equals("/") && webContextPrefix.length() == 0) {
            webContextPrefix = "/";
        }
        handleWebappDeployment(webappDir, webContextPrefix + warContext,
                                webContextParams, applicationEventListeners);
    }

    private void handleWebappDeployment(File webappFile, String contextStr,
                                        List<WebContextParameter> webContextParams,
                                        List<Object> applicationEventListeners) throws CarbonException {
        String filename = webappFile.getName();
        try {
            Context context =
                    DataHolder.getCarbonTomcatService().addWebApp(contextStr, webappFile.getAbsolutePath());
            //deploying web app for url-mapper
            if (DataHolder.getHotUpdateService() != null) {
                List<String> hostNames = DataHolder.getHotUpdateService().getMappigsPerWebapp(contextStr);
                for (String hostName : hostNames) {
                    Host host = (Host) DataHolder.getCarbonTomcatService().
                            getTomcat().getEngine().findChild(hostName);
                    Context contextForHost =
                            DataHolder.getCarbonTomcatService().addWebApp(host, "/", webappFile.getAbsolutePath());
                }
            }
            context.setManager(new CarbonTomcatSessionManager(tenantId)); // TODO: Must use a clusterable manager such as BackupManager
            context.setReloadable(false);
            WebApplication webapp = new WebApplication(context, webappFile);
            webapp.setServletContextParameters(webContextParams);
            webappsHolder.getStartedWebapps().put(filename, webapp);
            webappsHolder.getFaultyWebapps().remove(filename);
            registerApplicationEventListeners(applicationEventListeners, context);
            /*ErrorPage page = new ErrorPage();
            page.setErrorCode(503);
            page.setLocation("/503.jsp");
            context.addErrorPage(page);*/
            log.info("Deployed webapp: " + webapp);
        } catch (Throwable e) {
            //catching a Throwable here to avoid web-apps crashing the server during startup
            StandardContext context = new StandardContext();
            context.setName(webappFile.getName());
            WebApplication webapp = new WebApplication(context, webappFile);
            String msg = "Error while deploying webapp: " + webapp;
            log.error(msg, e);
            webapp.setFaultReason(new Exception(msg, e));
            webappsHolder.getFaultyWebapps().put(filename, webapp);
            webappsHolder.getStartedWebapps().remove(filename);
            throw new CarbonException(msg, e);
        }
    }

    private void registerApplicationEventListeners(List<Object> applicationEventListeners,
                                                   Context context) {
        Object[] originalEventListeners = context.getApplicationEventListeners();
        Object[] newEventListeners = new Object[originalEventListeners.length + applicationEventListeners.size()];
        if (originalEventListeners.length != 0) {
            System.arraycopy(originalEventListeners, 0, newEventListeners, 0, originalEventListeners.length);
            int i = originalEventListeners.length;
            for (Object eventListener : applicationEventListeners) {
                newEventListeners[i++] = eventListener;
            }
        } else {
            newEventListeners =
                    applicationEventListeners.toArray(new Object[applicationEventListeners.size()]);
        }
        context.setApplicationEventListeners(newEventListeners);
    }

    /**
     * Hot update an existing webapp. i.e., reload or redeploy a webapp archive which has been
     * updated
     *
     * @param webApplication The webapp which needs to be hot updated
     * @param webContextParams ServletContext params for this webapp
     * @param applicationEventListeners Application event listeners
     * @throws CarbonException If a deployment error occurs
     */
    private void handleHotUpdate(WebApplication webApplication,
                                 List<WebContextParameter> webContextParams,
                                 List<Object> applicationEventListeners) throws CarbonException {
        File webappFile = webApplication.getWebappFile();
        if (webappFile.isDirectory()) {  // webapp deployed as an exploded directory
            webApplication.reload();
            webApplication.setServletContextParameters(webContextParams);
            webApplication.setLastModifiedTime(webappFile.lastModified());
        } else { // webapp deployed from WAR
            // NOTE: context.reload() does not work for webapps deployed from WARs. Hence, we 
            // need to undeploy & redeploy.
            // See http://tomcat.apache.org/tomcat-5.5-doc/manager-howto.html#Reload An Existing Application
            undeploy(webApplication);
            handleWarWebappDeployment(webappFile, webContextParams, applicationEventListeners);
        }
        log.info("Redeployed webapp: " + webApplication);
    }

    /**
     * Handle undeployment.
     *
     * @param webappFile The webapp file to be undeployed
     * @throws CarbonException If an error occurs while undeploying webapp
     */
    public void undeploy(File webappFile) throws CarbonException {
        CarbonContextHolder currentCarbonContextHolder =
                CarbonContextHolder.getCurrentCarbonContextHolder();
        currentCarbonContextHolder.startTenantFlow();
        try {
            currentCarbonContextHolder.setTenantId(tenantId);
            currentCarbonContextHolder.setTenantDomain(tenantDomain);
            Map<String, WebApplication> deployedWebapps = webappsHolder.getStartedWebapps();
            Map<String, WebApplication> stoppedWebapps = webappsHolder.getStoppedWebapps();
            String fileName = webappFile.getName();
            if (deployedWebapps.containsKey(fileName)) {
                undeploy(deployedWebapps.get(fileName));
            }
            //also checking the stopped webapps.
            else if (stoppedWebapps.containsKey(fileName)) {
                undeploy(stoppedWebapps.get(fileName));
            }

            clearFaultyWebapp(fileName);
        } finally {
            currentCarbonContextHolder.endTenantFlow();
        }
    }

    /**
     * Handle undeployment.
     *
     * @param webappFile The webapp file to be undeployed
     * @throws CarbonException If an error occurs while lazy unloading
     */
    public void lazyUnload(File webappFile) throws CarbonException {
        CarbonContextHolder currentCarbonContextHolder =
                CarbonContextHolder.getCurrentCarbonContextHolder();
        currentCarbonContextHolder.startTenantFlow();
        try {
            currentCarbonContextHolder.setTenantId(tenantId);
            currentCarbonContextHolder.setTenantDomain(tenantDomain);
            Map<String, WebApplication> deployedWebapps = webappsHolder.getStartedWebapps();
            String fileName = webappFile.getName();
            if (deployedWebapps.containsKey(fileName)) {
                deployedWebapps.get(fileName).lazyUnload();
            }

            clearFaultyWebapp(fileName);
        } finally {
            currentCarbonContextHolder.endTenantFlow();
        }
    }

    private void clearFaultyWebapp(String fileName) {
        Map<String, WebApplication> faultyWebapps = webappsHolder.getFaultyWebapps();
        if (faultyWebapps.containsKey(fileName)) {
            WebApplication faultyWebapp = faultyWebapps.get(fileName);
            faultyWebapps.remove(fileName);
            log.info("Removed faulty webapp " + faultyWebapp);
        }
    }

    /**
     * Undeploy a webapp
     *
     * @param webapp The webapp being undeployed
     * @throws CarbonException If an error occurs while undeploying
     */
    private void undeploy(WebApplication webapp) throws CarbonException {
        webappsHolder.undeployWebapp(webapp);
        log.info("Undeployed webapp: " + webapp);
    }
}
