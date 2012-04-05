/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.jaggery.app.mgt;

import org.apache.catalina.*;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.deploy.ErrorPage;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.deploy.SecurityCollection;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.utils.multitenancy.CarbonContextHolder;
import org.wso2.carbon.webapp.mgt.CarbonTomcatSessionManager;
import org.wso2.carbon.webapp.mgt.DataHolder;
import org.wso2.carbon.webapp.mgt.WebContextParameter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.management.ManagementPermission;
import java.util.List;
import java.util.Map;

/**
 * This deployer is responsible for deploying/undeploying/updating those Jaggery apps.
 */

public class TomcatJaggeryWebappsDeployer {

    private static Log log = LogFactory.getLog(TomcatJaggeryWebappsDeployer.class);

    private String webContextPrefix;
    private int tenantId;
    private String tenantDomain;

    private JaggeryApplicationsHolder webappsHolder;

    /**
     * Constructor
     *
     * @param webContextPrefix The Web context prefix
     * @param tenantId         The tenant ID of the tenant to whom this deployer belongs to
     * @param tenantDomain     The tenant domain of the tenant to whom this deployer belongs to
     * @param webappsHolder    JaggeryApplicationsHolder
     */
    public TomcatJaggeryWebappsDeployer(String webContextPrefix,
                                        int tenantId,
                                        String tenantDomain,
                                        JaggeryApplicationsHolder webappsHolder) {
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
     * Deploy Jaggery app
     *
     * @param webappFile                The Jaggery app file to be deployed
     * @param webContextParams          context-params for this Jaggery app
     * @param applicationEventListeners Application event listeners
     * @param servletParameters         Jaggery servlet params
     * @param servletMappingParameters  jaggery servletmappings
     * @param securityConstraint        restrict jaggery conf
     * @throws CarbonException If a deployment error occurs
     */
    @SuppressWarnings(value = "unused")
    public void deploy(File webappFile,
                       List<WebContextParameter> webContextParams,
                       List<Object> applicationEventListeners,
                       List<ServletParameter> servletParameters,
                       List<ServletMappingParameter> servletMappingParameters,
                       SecurityConstraint securityConstraint) throws CarbonException {
        CarbonContextHolder currentCarbonContextHolder =
                CarbonContextHolder.getCurrentCarbonContextHolder();
        currentCarbonContextHolder.startTenantFlow();
        try {
            currentCarbonContextHolder.setTenantId(tenantId);
            currentCarbonContextHolder.setTenantDomain(tenantDomain);
            long lastModifiedTime = webappFile.lastModified();
            long configLastModified = 0;
            if (JaggeryDeploymentUtil.getConfig(webappFile) != null) {
                configLastModified = JaggeryDeploymentUtil.getConfig(webappFile).lastModified();
            }

            //long configLastModified = webappFile.
            JaggeryApplication deployedWebapp =
                    webappsHolder.getStartedWebapps().get(webappFile.getName());
            JaggeryApplication undeployedWebapp =
                    webappsHolder.getStoppedWebapps().get(webappFile.getName());
            JaggeryApplication faultyWebapp =
                    webappsHolder.getFaultyWebapps().get(webappFile.getName());
            if (deployedWebapp == null && faultyWebapp == null && undeployedWebapp == null) {
                handleHotDeployment(webappFile, webContextParams, applicationEventListeners,
                        servletParameters, servletMappingParameters, securityConstraint);
            } else if (deployedWebapp != null &&
                    deployedWebapp.getLastModifiedTime() != lastModifiedTime &&
                    (configLastModified != 0 && deployedWebapp.getConfigDirLastModifiedTime() != configLastModified)) {
                /*handleHotUpdate(deployedWebapp, webContextParams, applicationEventListeners,
                        servletParameters, servletMappingParameters, securityConstraint);*/
                undeploy(deployedWebapp);
                handleHotDeployment(webappFile, webContextParams, applicationEventListeners,
                        servletParameters, servletMappingParameters, securityConstraint);
            } else if (faultyWebapp != null &&
                    faultyWebapp.getLastModifiedTime() != lastModifiedTime) {
                handleHotDeployment(webappFile, webContextParams, applicationEventListeners,
                        servletParameters, servletMappingParameters, securityConstraint);
            }
        } finally {
            currentCarbonContextHolder.endTenantFlow();
        }
    }

    /**
     * Hot deployment of a Jaggery app. i.e., deploy a Jaggery app that has newly become available.
     *
     * @param webapp                    The Jaggery app WAR or directory that needs to be deployed
     * @param webContextParams          ServletContext params for this webapp
     * @param applicationEventListeners Application event listeners
     * @param servletParameters         web.xml servlet entries
     * @param servletMappingParameters  web.xml servlet mapping entries
     * @param securityConstraint        to restrinct the config file
     * @throws CarbonException If an error occurs during deployment
     */
    private void handleHotDeployment(File webapp, List<WebContextParameter> webContextParams,
                                     List<Object> applicationEventListeners,
                                     List<ServletParameter> servletParameters,
                                     List<ServletMappingParameter> servletMappingParameters, SecurityConstraint securityConstraint)
            throws CarbonException {
        String filename = webapp.getName();
        if (webapp.isDirectory()) {
            handleExplodedWebappDeployment(webapp, webContextParams, applicationEventListeners,
                    servletParameters, servletMappingParameters, securityConstraint);
        } else if (filename.endsWith(".zip")) {
            synchronized (this) {
                String appPath = webapp.getAbsolutePath().substring(0, webapp.getAbsolutePath().indexOf(".zip"));
                try {
                    JaggeryDeploymentUtil.unZip(new FileInputStream(webapp), appPath);
                    if (!webapp.delete()) {
                        throw new CarbonException(appPath + "could not be deleted");
                    }
                } catch (FileNotFoundException e) {
                    throw new CarbonException(e);
                }
                File unzippedWebapp = new File(appPath);
                handleExplodedWebappDeployment(unzippedWebapp, webContextParams, applicationEventListeners,
                        servletParameters, servletMappingParameters, securityConstraint);
            }
        } else if (filename.endsWith(".war")) {
            handleWarWebappDeployment(webapp, webContextParams, applicationEventListeners,
                    servletParameters, servletMappingParameters, securityConstraint);
        }
    }

    /**
     * Handle the deployment of a an archive Jaggery app. i.e., a WAR
     *
     * @param webappWAR                 The WAR Jaggery app file
     * @param webContextParams          ServletContext params for this webapp
     * @param applicationEventListeners Application event listeners
     * @param servletParameters         web.xml servlet entries
     * @param servletMappingParameters  web.xml servlet mapping entries
     * @param securityConstraint        to restrict config file
     * @throws CarbonException If a deployment error occurs
     */
    private void handleWarWebappDeployment(File webappWAR,
                                           List<WebContextParameter> webContextParams,
                                           List<Object> applicationEventListeners,
                                           List<ServletParameter> servletParameters,
                                           List<ServletMappingParameter> servletMappingParameters, SecurityConstraint securityConstraint)
            throws CarbonException {
        String filename = webappWAR.getName();
        String warContext = "";
        if (filename.equals("ROOT.war")) {  // FIXME: This is not working for some reason!
            if (webContextPrefix != null && !webContextPrefix.endsWith("/")) {
                warContext = "/";
            }
        } else {
            warContext = filename.substring(0, filename.indexOf(".zip"));
        }
        if (!warContext.equals("/") && webContextPrefix.length() == 0) {
            webContextPrefix = "/";
        }
        handleWebappDeployment(webappWAR, webContextPrefix + warContext,
                webContextParams, applicationEventListeners,
                servletParameters, servletMappingParameters, securityConstraint);
    }

    /**
     * Handle the deployment of a an exploded Jaggery app. i.e., a Jaggery app deployed as a directory
     * & not an archive
     *
     * @param webappDir                 The exploded Jaggery app directory
     * @param webContextParams          ServletContext params for this webapp
     * @param applicationEventListeners Application event listeners
     * @param servletParameters         web.xml servlet entries
     * @param servletMappingParameters  web.xml servlet mapping entries
     * @param securityConstraint        to restrict config file
     * @throws CarbonException If a deployment error occurs
     */
    private void handleExplodedWebappDeployment(File webappDir,
                                                List<WebContextParameter> webContextParams,
                                                List<Object> applicationEventListeners,
                                                List<ServletParameter> servletParameters,
                                                List<ServletMappingParameter> servletMappingParameters, SecurityConstraint securityConstraint)
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
                webContextParams, applicationEventListeners,
                servletParameters, servletMappingParameters, securityConstraint);
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
     * Hot update an existing Jaggery app. i.e., reload or redeploy a Jaggery app archive which has been
     * updated
     *
     * @param jaggeryApplication        The Jaggery app which needs to be hot updated
     * @param webContextParams          ServletContext params for this Jaggery app
     * @param applicationEventListeners Application event listeners
     * @param servletParameters         jaggery servlet params
     * @param servletMappingParameters  jaggery servlet mappings
     * @param securityConstraint        to restrict config file
     * @throws CarbonException If a deployment error occurs
     */
/*    private void handleHotUpdate(JaggeryApplication jaggeryApplication,
                                 List<WebContextParameter> webContextParams,
                                 List<Object> applicationEventListeners,
                                 List<ServletParameter> servletParameters,
                                 List<ServletMappingParameter> servletMappingParameters, SecurityConstraint securityConstraint) throws CarbonException {
        File webappFile = jaggeryApplication.getWebappFile();
        if (webappFile.isDirectory()) {  // webapp deployed as an exploded directory
            jaggeryApplication.reload();
            jaggeryApplication.setServletContextParameters(webContextParams);
            jaggeryApplication.setLastModifiedTime(webappFile.lastModified());
        } else { // webapp deployed from WAR
            // NOTE: context.reload() does not work for webapps deployed from WARs. Hence, we
            // need to undeploy & redeploy.
            // See http://tomcat.apache.org/tomcat-5.5-doc/manager-howto.html#Reload An Existing Application
            undeploy(jaggeryApplication);
            handleWarWebappDeployment(webappFile, webContextParams, applicationEventListeners,
                    servletParameters, servletMappingParameters, securityConstraint);
        }
        log.info("Redeployed Jaggery App: " + jaggeryApplication);
    }
    */

    /**
     * Handle undeployment.
     *
     * @param webappFile The Jaggery app file to be undeployed
     * @throws CarbonException If an error occurs while undeploying Jaggery app
     */
    @SuppressWarnings(value = "unused")
    public void undeploy(File webappFile) throws CarbonException {
        CarbonContextHolder currentCarbonContextHolder =
                CarbonContextHolder.getCurrentCarbonContextHolder();
        currentCarbonContextHolder.startTenantFlow();
        try {
            currentCarbonContextHolder.setTenantId(tenantId);
            currentCarbonContextHolder.setTenantDomain(tenantDomain);
            Map deployedWebapps = webappsHolder.getStartedWebapps();
            Map stoppedWebapps = webappsHolder.getStoppedWebapps();
            String fileName = webappFile.getName();

            if (deployedWebapps.containsKey(fileName)) {
                undeploy((JaggeryApplication) deployedWebapps.get(fileName));
            }
            //also checking the stopped webapps.
            else if (stoppedWebapps.containsKey(fileName)) {
                undeploy((JaggeryApplication) stoppedWebapps.get(fileName));
            }

            clearFaultyWebapp(fileName);
        } finally {
            currentCarbonContextHolder.endTenantFlow();
        }
    }

    /**
     * Handle undeployment.
     *
     * @param webappFile The Jaggery app file to be undeployed
     * @throws CarbonException If an error occurs while lazy unloading
     */
    @SuppressWarnings(value = "unused")
    public void lazyUnload(File webappFile) throws CarbonException {
        CarbonContextHolder currentCarbonContextHolder =
                CarbonContextHolder.getCurrentCarbonContextHolder();
        currentCarbonContextHolder.startTenantFlow();
        try {
            currentCarbonContextHolder.setTenantId(tenantId);
            currentCarbonContextHolder.setTenantDomain(tenantDomain);
            Map deployedWebapps = webappsHolder.getStartedWebapps();
            String fileName = webappFile.getName();
            if (deployedWebapps.containsKey(fileName)) {
                ((JaggeryApplication) deployedWebapps.get(fileName)).lazyUnload();
            }

            clearFaultyWebapp(fileName);
        } finally {
            currentCarbonContextHolder.endTenantFlow();
        }
    }

    private void clearFaultyWebapp(String fileName) {
        Map faultyWebapps = webappsHolder.getFaultyWebapps();
        if (faultyWebapps.containsKey(fileName)) {
            JaggeryApplication faultyWebapp = (JaggeryApplication) faultyWebapps.get(fileName);
            faultyWebapps.remove(fileName);
            log.info("Removed faulty webapp " + faultyWebapp);
        }
    }

    /**
     * Undeploy a Jaggery app
     *
     * @param webapp The Jaggery app being undeployed
     * @throws CarbonException If an error occurs while undeploying
     */
    private void undeploy(JaggeryApplication webapp) throws CarbonException {
        webappsHolder.undeployWebapp(webapp);
        log.info("Undeployed Jaggery App: " + webapp);
    }

    /**
     * Deployment procedure of Jaggery apps
     *
     * @param webappFile                The Jaggery app file to be deployed
     * @param contextStr                jaggery app context string
     * @param webContextParams          context-params for this Jaggery app
     * @param applicationEventListeners Application event listeners
     * @param servletParameters         web.xml servlet entries
     * @param servletMappingParameters  web.xml servlet mapping entries
     * @param securityConstraint        to restrict config file
     * @throws CarbonException If a deployment error occurs
     */
    private void handleWebappDeployment(File webappFile, String contextStr,
                                        List<WebContextParameter> webContextParams,
                                        List<Object> applicationEventListeners,
                                        List<ServletParameter> servletParameters,
                                        List<ServletMappingParameter> servletMappingParameters, SecurityConstraint securityConstraint) throws CarbonException {

        String filename = webappFile.getName();
        try {
            JSONObject jaggeryConfigObj = readJaggeryConfig(webappFile);

            Tomcat tomcat = DataHolder.getCarbonTomcatService().getTomcat();

            Context context =
                    DataHolder.getCarbonTomcatService().addWebApp(contextStr, webappFile.getAbsolutePath(),
                            new JaggeryConfListener(tomcat, servletParameters, servletMappingParameters, jaggeryConfigObj, securityConstraint));


            context.setManager(new CarbonTomcatSessionManager(tenantId)); // TODO: Must use a clusterable manager such as BackupManager
            context.setReloadable(true);

            JaggeryApplication webapp = new JaggeryApplication(context, webappFile);
            webapp.setServletParameters(servletParameters);
            webapp.setServletMappingParameters(servletMappingParameters);
            webapp.setServletContextParameters(webContextParams);
            webappsHolder.getStartedWebapps().put(filename, webapp);
            webappsHolder.getFaultyWebapps().remove(filename);
            registerApplicationEventListeners(applicationEventListeners, context);
            log.info("Deployed Jaggery App: " + webapp);
        } catch (Throwable e) {
            //catching a Throwable here to avoid web-apps crashing the server during startup
            StandardContext context = new StandardContext();
            context.setName(webappFile.getName());
            JaggeryApplication webapp = new JaggeryApplication(context, webappFile);
            String msg = "Error while deploying webapp: " + webapp;
            log.error(msg, e);
            webapp.setFaultReason(new Exception(msg, e));
            webappsHolder.getFaultyWebapps().put(filename, webapp);
            webappsHolder.getStartedWebapps().remove(filename);
            throw new CarbonException(msg, e);
        }
    }

    public static class JaggeryConfListener implements LifecycleListener {
        private List<ServletParameter> servletParameters;
        private List<ServletMappingParameter> servletMappingParameters;
        private JSONObject jaggeryConfig;
        private Tomcat tomcat;
        private SecurityConstraint securityConstraint;

        public JaggeryConfListener(Tomcat tomcat, List<ServletParameter> servletParameters,
                                   List<ServletMappingParameter> servletMappingParameters,
                                   JSONObject jaggeryConfig, SecurityConstraint securityConstraint) {
            this.servletParameters = servletParameters;
            this.servletMappingParameters = servletMappingParameters;
            this.jaggeryConfig = jaggeryConfig;
            this.tomcat = tomcat;
            this.securityConstraint = securityConstraint;
        }

        @Override
        public void lifecycleEvent(LifecycleEvent event) {
            if (Lifecycle.BEFORE_START_EVENT.equals(event.getType())) {
                initJaggeryappDefaults((Context) event.getLifecycle(), this.tomcat,
                        this.jaggeryConfig, this.servletParameters, this.servletMappingParameters, this.securityConstraint);
            }
        }
    }

    public static void initJaggeryappDefaults(Context ctx, Tomcat tomcat,
                                              JSONObject jaggeryConfig, List<ServletParameter> servletParameters,
                                              List<ServletMappingParameter> servletMappingParameters, SecurityConstraint securityConstraint) {

        for (ServletParameter servletParameter : servletParameters) {
            if (servletParameter.getServletName() != null && servletParameter.getServletClass() != null) {
                Wrapper servletWrapper = tomcat.addServlet(
                        ctx.getName(), servletParameter.getServletName(), servletParameter.getServletClass());

                if (servletParameter.getInitParams() != null) {
                    for (Map.Entry<String, String> entry : servletParameter.getInitParams().entrySet()) {
                        servletWrapper.addInitParameter(entry.getKey(), entry.getValue());
                    }
                }

                if (servletParameter.getLoadOnStartup() != 0) {
                    servletWrapper.setLoadOnStartup(servletParameter.getLoadOnStartup());
                }
            }
        }

        for (ServletMappingParameter servletMappingParameter : servletMappingParameters) {
            if (servletMappingParameter.getServletName() != null && servletMappingParameter.getUrlPattern() != null) {
                ctx.addServletMapping(servletMappingParameter.getUrlPattern(), servletMappingParameter.getServletName());
            }
        }

        ctx.addConstraint(securityConstraint);

        //jaggery conf params if null conf is not available
        if (jaggeryConfig != null) {
            addWelcomeFiles(ctx, jaggeryConfig);
            addErrorPages(ctx, jaggeryConfig);
            addSecurityConstraints(ctx, jaggeryConfig);
            setLoginConfig(ctx, jaggeryConfig);
            addSecurityRoles(ctx, jaggeryConfig);
        }
    }

    private JSONObject readJaggeryConfig(File f) throws IOException {

        File confFile = new File(f.getAbsolutePath() + File.separator + JaggeryConstants.JAGGERY_CONF_FILE);

        if (!confFile.exists()) {
            return null;
        }

        String jsonString = "";
        if (!confFile.isDirectory()) {
            FileInputStream fis = new FileInputStream(confFile);
            StringWriter writer = new StringWriter();
            IOUtils.copy(fis, writer, null);
            jsonString = writer.toString();

        }

        return (JSONObject) JSONValue.parse(jsonString);
    }

    private static void addErrorPages(Context context, JSONObject obj) {
        JSONArray arr = (JSONArray) obj.get(JaggeryConstants.JaggeryConfigParams.ERROR_PAGES);
        if (arr != null) {
            for (Object anArr : arr) {
                ErrorPage errPage = new ErrorPage();
                JSONObject o = (JSONObject) anArr;
                errPage.setErrorCode((String) o.get(JaggeryConstants.JaggeryConfigParams.ERROR_CODE));
                errPage.setLocation((String) o.get(JaggeryConstants.JaggeryConfigParams.LOCATION));

                context.addErrorPage(errPage);
            }
        }
    }

    private static void setLoginConfig(Context context, JSONObject obj) {
        JSONObject loginObj = (JSONObject) obj.get(JaggeryConstants.JaggeryConfigParams.LOGIN_CONFIG);
        if (loginObj != null) {
            if (loginObj.get(JaggeryConstants.JaggeryConfigParams.AUTH_METHOD).equals(JaggeryConstants.JaggeryConfigParams.AUTH_METHOD_FORM)) {
                LoginConfig loginConfig = new LoginConfig();
                loginConfig.setAuthMethod(JaggeryConstants.JaggeryConfigParams.AUTH_METHOD_FORM);
                loginConfig.setLoginPage((String) ((JSONObject) loginObj.get(JaggeryConstants.JaggeryConfigParams.FORM_LOGIN_CONFIG)).get(JaggeryConstants.JaggeryConfigParams.FORM_LOGIN_PAGE));
                loginConfig.setErrorPage((String) ((JSONObject) loginObj.get(JaggeryConstants.JaggeryConfigParams.FORM_LOGIN_CONFIG)).get(JaggeryConstants.JaggeryConfigParams.FORM_ERROR_PAGE));
                context.setLoginConfig(loginConfig);

            } else if (loginObj.get(JaggeryConstants.JaggeryConfigParams.AUTH_METHOD).equals(JaggeryConstants.JaggeryConfigParams.AUTH_METHOD_BASIC)) {
                LoginConfig loginConfig = new LoginConfig();
                loginConfig.setAuthMethod(JaggeryConstants.JaggeryConfigParams.AUTH_METHOD_BASIC);
                context.setLoginConfig(loginConfig);

            }
        }
    }

    private static void addSecurityConstraints(Context context, JSONObject obj) {
        JSONArray arr = (JSONArray) obj.get(JaggeryConstants.JaggeryConfigParams.SECURITY_CONSTRAINTS);
        if (arr != null) {
            for (Object anArr : arr) {
                JSONObject o = (JSONObject) anArr;
                SecurityConstraint securityConstraint = new SecurityConstraint();
                if (((JSONObject) o.get(JaggeryConstants.JaggeryConfigParams.SECURITY_CONSTRAINT)).get(JaggeryConstants.JaggeryConfigParams.WEB_RESOURCE_COLLECTION) != null) {
                    JSONObject resCollection = (JSONObject) ((JSONObject) o.get(JaggeryConstants.JaggeryConfigParams.SECURITY_CONSTRAINT)).get(JaggeryConstants.JaggeryConfigParams.WEB_RESOURCE_COLLECTION);
                    SecurityCollection secCollection = new SecurityCollection();
                    secCollection.setName((String) resCollection.get(JaggeryConstants.JaggeryConfigParams.WEB_RES_NAME));

                    JSONArray arrPattern = (JSONArray) resCollection.get(JaggeryConstants.JaggeryConfigParams.URL_PATTERNS);
                    for (Object anArrPattern : arrPattern) {
                        secCollection.addPattern((String) anArrPattern);
                    }

                    JSONArray methods = (JSONArray) resCollection.get(JaggeryConstants.JaggeryConfigParams.HTTP_METHODS);
                    if (methods != null) {
                        for (Object method : methods) {
                            secCollection.addMethod((String) method);
                        }
                    }

                    securityConstraint.addCollection(secCollection);
                }

                if (((JSONObject) o.get(JaggeryConstants.JaggeryConfigParams.SECURITY_CONSTRAINT)).get(JaggeryConstants.JaggeryConfigParams.AUTH_ROLES) != null) {
                    JSONArray roles = (JSONArray) ((JSONObject) o.get(JaggeryConstants.JaggeryConfigParams.SECURITY_CONSTRAINT)).get(JaggeryConstants.JaggeryConfigParams.AUTH_ROLES);
                    for (Object role : roles) {
                        securityConstraint.addAuthRole((String) role);
                    }
                    securityConstraint.setAuthConstraint(true);
                }

                context.addConstraint(securityConstraint);
            }
        }
    }

    private static void addSecurityRoles(Context context, JSONObject obj) {
        JSONArray arr = (JSONArray) obj.get(JaggeryConstants.JaggeryConfigParams.SECURITY_ROLES);
        if (arr != null) {
            for (Object role : arr) {
                context.addSecurityRole((String) role);
            }
        }
    }

    private static void addWelcomeFiles(Context context, JSONObject obj) {
        JSONArray arr = (JSONArray) obj.get(JaggeryConstants.JaggeryConfigParams.WELCOME_FILES);
        if (arr != null) {
            for (Object role : arr) {
                context.addWelcomeFile((String) role);
            }
        }
    }
}

