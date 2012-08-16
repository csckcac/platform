/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
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
package org.wso2.carbon.webapp.mgt;

import org.apache.catalina.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.context.ApplicationContext;
import org.wso2.carbon.utils.FileManipulator;
import org.wso2.carbon.utils.multitenancy.CarbonApplicationContextHolder;
import org.wso2.carbon.webapp.mgt.utils.GhostWebappDeployerUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a Tomcat Web Application
 */
@SuppressWarnings("unused")
public class WebApplication {
    private static final Log log = LogFactory.getLog(WebApplication.class);

    /**
     * Key for the enable SAAS context parameter
     */
    public static final String ENABLE_SAAS = "carbon.enable.saas";

    private Context context;
    private File webappFile;
    private long lastModifiedTime;
    private Exception faultReason;
    private String state;
    private Map<String, Object> properties = new HashMap<String, Object>();
    private TomcatGenericWebappsDeployer tomcatGenericWebappsDeployer;

    // We need this variable to use in the Statistics inner class which is static
    private static boolean isThisGhost = false;

    public WebApplication(TomcatGenericWebappsDeployer tomcatGenericWebappsDeployer, Context context, File webappFile) {
        this.tomcatGenericWebappsDeployer = tomcatGenericWebappsDeployer;
        this.context = context;
        setWebappFile(webappFile);
        setLastModifiedTime(webappFile.lastModified());
    }

    /*public WebApplication(File webappFile) {
        setWebappFile(webappFile);
        setLastModifiedTime(webappFile.lastModified());
    }*/

    /**
     * Set ServletContext parameters for this webapp
     *
     * @param parameters ServletContext params for this webapp
     */
    public void setServletContextParameters(List<WebContextParameter> parameters) {
        for (WebContextParameter parameter : parameters) {
            context.getServletContext().setInitParameter(parameter.getName(),
                    parameter.getValue()); // context-param in web.xml
        }
    }

    public File getWebappFile() {
        return webappFile;
    }

    public void setWebappFile(File webappFile) {
        this.webappFile = webappFile;
    }

    public String getContextName() {
        return context.getName();
    }

    public String getDisplayName() {
        return context.getDisplayName();
    }

    public void setDisplayName(String name) {
        context.setDisplayName(name);
    }

    public void setLastModifiedTime(long lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public long getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public Exception getFaultReason() {
        return faultReason;
    }

    public void setFaultReason(Exception faultReason) {
        this.faultReason = faultReason;
    }

    public void setProperty(String key, Object value) {
        if (key != null) {
            properties.put(key, value);
        }
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public TomcatGenericWebappsDeployer getTomcatGenericWebappsDeployer() {
        return tomcatGenericWebappsDeployer;
    }

    public boolean reload() {
        try {
            //reload the context of Host
            handleHotUpdateToHost("reload");
        } catch (CarbonException e) {
            log.error("error while reloading context for the hosts", e);
        }
        //reload the context of WebApplication
        return reload(context);
    }

    private boolean reload(Context contextOfWepap) {
        if (contextOfWepap.getAvailable()) {
            contextOfWepap.reload();
            log.info("Reloaded webapp: " + contextOfWepap);
            return true;
        }
        return false;
    }

    /**
     * Temporarilly stop this Web application. When a request is made to a stopped webapp resource,
     * an HTTP 503 (Temporarilly Unavailable) will be returned.
     *
     * @return true - If hte webapp was successfully stopped
     * @throws CarbonException If an error occurs while stopping this webapp
     */
    public boolean stop() throws CarbonException {
        //stop the context of Host
        handleHotUpdateToHost("stop");
        //stop the context of WebApplication
        return stop(this.context);

    }

    private boolean stop(Context contextOfWepap) throws CarbonException {
        try {
            if (contextOfWepap.getAvailable()) {
                contextOfWepap.stop();
                this.setState("Stopped");
                log.info("Stopped webapp: " + contextOfWepap);
                return true;
            }
        } catch (Exception e) {
            throw new CarbonException("Cannot temporarilly stop webapp " + contextOfWepap, e);
        }
        return false;
    }

    /**
     * Start a webapp which was stopped
     *
     * @return true - if the webapp was successfully started
     * @throws CarbonException If an error occurs while starting this webapp
     */
    public boolean start() throws CarbonException {
        //start context of the Host
        handleHotUpdateToHost("start");
        //start the context of WebApplication
        return start(this.context);
    }

    private boolean start(Context contextOfWepap) throws CarbonException {
        try {
            if (!contextOfWepap.getAvailable()) {
                contextOfWepap.start();
                this.setState("Started");
                log.info("Started webapp: " + contextOfWepap);
                return true;
            }
        } catch (Exception e) {
            throw new CarbonException("Cannot start webapp " + contextOfWepap, e);
        }
        return false;
    }

    /**
     * Lazily unload this Web application.
     *
     * @throws CarbonException If an error occurs while lazy unloading
     */
    public void lazyUnload() throws CarbonException {
        //lozyunload the context of Host
        handleHotUpdateToHost("lazyUnload");
        //lazyunload the context of WebApplication
        lazyUnload(this.context);
    }

    private void lazyUnload(Context contextOfWepap) throws CarbonException {
        try {
            if (contextOfWepap.getAvailable()) {
                // If the following is not done, the Realm will throw a LifecycleException, because
                // Realm.stop is called for each context.
                contextOfWepap.setRealm(null);
                contextOfWepap.stop();
                contextOfWepap.destroy();
                log.info("Unloaded webapp: " + contextOfWepap);
            }
            //if the webapp is stopped above context.getAvailable() becomes false.
            //So to unload stopped webapps this is done.
            else if (LifecycleState.STOPPED.equals(contextOfWepap.getState())) {
                contextOfWepap.setRealm(null);
                contextOfWepap.destroy();
                log.info("Unloaded webapp: " + contextOfWepap);
            }
        } catch (Exception e) {
            throw new CarbonException("Cannot lazy unload webapp " + context, e);
        }
    }

    /**
     * Completely delete & destroy this Web application. When a request is made to an undeployed
     * webapp resource, an HTTP 404 (Not Found) will be returned.
     *
     * @throws CarbonException If an error occurs while undeploying this webapp
     */
    public void undeploy() throws CarbonException {
        CarbonApplicationContextHolder currentCarbonAppContextHolder =
                CarbonApplicationContextHolder.getThreadLocalCarbonApplicationContextHolder();
        currentCarbonAppContextHolder.startApplicationFlow();
        currentCarbonAppContextHolder.setApplicationName(TomcatUtil.
                getApplicationNameFromContext(this.context.getBaseName()));
        //lazyunload the context of WebApplication
        lazyUnload();
        File webappDir;
        if (webappFile.getAbsolutePath().endsWith(".war")) {
            String filePath = webappFile.getAbsolutePath();
            webappDir = new File(filePath.substring(0, filePath.lastIndexOf('.')));
        } else {
            webappDir = webappFile;
        }
        // Delete the exploded dir of war based webapps upon undeploy. But omit deleting
        // directory based webapps.
        if (TomcatUtil.checkUnpackWars() && webappDir.exists() && !webappFile.isDirectory() &&
                !FileManipulator.deleteDir(webappDir)) {
            throw new CarbonException("exploded Webapp directory " + webappDir + " deletion failed");
        }
        currentCarbonAppContextHolder.endApplicationFlow();
    }

    /**
     * Doing the start, stop, reload and lazy unload of webapps inside all hosts
     * respectively when getting request.
     *
     * @param nameOfOperation the operation to be performed in oder to hot update the host
     * @throws CarbonException if errors occurs when hot update the host
     */
    private void handleHotUpdateToHost(String nameOfOperation) throws CarbonException {
        if (DataHolder.getHotUpdateService() != null) {
            List<String> mappings = ApplicationContext.getCurrentApplicationContext().
                    getUrlMappingsPerApplication(this.context.getName());
            Engine engine = DataHolder.getCarbonTomcatService().getTomcat().getEngine();
            Context hostContext;
            Host host;
            for (String hostName : mappings) {
                host = (Host) engine.findChild(hostName);
                if(host != null) {
                    hostContext = (Context)host.findChild("/");
                    if(hostContext != null) {
                        if (nameOfOperation.equalsIgnoreCase("start")) {
                            start(hostContext);
                        } else if (nameOfOperation.equalsIgnoreCase("stop")) {
                            stop(hostContext);
                        } else if (nameOfOperation.equalsIgnoreCase("reload")) {
                            reload(hostContext);
                        } else if (nameOfOperation.equalsIgnoreCase("lazyunload")) {
                            lazyUnload(hostContext);
                            DataHolder.getHotUpdateService().removeHost(hostName);
                        } else if (nameOfOperation.equalsIgnoreCase("delete")) {
                            DataHolder.getHotUpdateService().deleteHost(hostName);
                        }
                    }

                }

            }

        }
    }

    /**
     * Completely delete & destroy this Web application
     *
     * @throws CarbonException If an error occurs while undeploying this webapp
     */
    public void delete() throws CarbonException {
        handleHotUpdateToHost("delete");
        undeploy();
        if (webappFile.isFile() && !webappFile.delete()) {
            throw new CarbonException("Webapp file " + webappFile + " deletion failed");
        } else if (webappFile.isDirectory() && !FileManipulator.deleteDir(webappFile)) {
            throw new CarbonException("Webapp Directory " + webappFile + " deletion failed");
        }
    }

    /**
     * Return a File object representing the "application root" directory
     * for our associated Host.
     *
     * @return The AppBase
     *         //TODO - when webapp exploding is supported for stratos, this should return the tenant's webapp dir
     */
    protected File getAppBase() {
        File appBase = null;
        File file = new File(DataHolder.getCarbonTomcatService().getTomcat().getHost().getAppBase());
        /*if (!file.isAbsolute()) {
            file = new File(System.getProperty("catalina.base"),
                            host.getAppBase());
        }*/
        try {
            appBase = file.getCanonicalFile();
        } catch (IOException e) {
            appBase = file;
        }
        return appBase;
    }

    /**
     * Given a context path, get the config file name.
     *
     * @param path
     * @return
     */
    private String getConfigFile(String path) {
        String basename;
        if (path.equals("")) {
            basename = "ROOT";
        } else {
            basename = path.substring(1).replace('/', '#');
        }
        return basename;
    }

    private File getConfigBase() {
        File configBase = new File(System.getProperty("catalina.base"), "conf");
        Container container = context;
        Container host = null;
        Container engine = null;
        while (container != null) {
            if (container instanceof Host) {
                host = container;
            }
            if (container instanceof Engine) {
                engine = container;
            }
            container = container.getParent();
        }
        if (engine != null) {
            configBase = new File(configBase, engine.getName());
        }
        if (host != null) {
            configBase = new File(configBase, host.getName());
        }
        return configBase;
    }

    /**
     * Given a context path, get the config file name.
     *
     * @param path
     */
    private String getDocBase(String path) {
        String basename;
        if (path.equals("")) {
            basename = "ROOT";
        } else {
            basename = path.substring(1);
        }
        return basename;
    }

    /**
     * Expire all sessions of this webapp
     */
    public void expireAllSessions() {
        Session[] sessions = context.getManager().findSessions();
        for (Session session : sessions) {
            session.expire();
        }
    }

    /**
     * Expire sessions whose lifetime is greater than or equal to <code>maxLifetimeMillis</code>
     *
     * @param maxLifetimeMillis The maximum session lifetime in milliseconds
     */
    public void expireSessions(long maxLifetimeMillis) {
        Session[] sessions = context.getManager().findSessions();
        for (Session session : sessions) {
            if ((System.currentTimeMillis() - session.getCreationTime()) >= maxLifetimeMillis) {
                session.expire();
            }
        }
    }

    /**
     * Expire all specified sessions
     *
     * @param sessionIDs The IDs of the sessions to be expired
     * @throws CarbonException If an error occurs while getting the session
     */
    public void expireSessions(String[] sessionIDs) throws CarbonException {
        Manager manager = context.getManager();
        try {
            for (String sessionID : sessionIDs) {
                manager.findSession(sessionID).expire();
            }
        } catch (IOException e) {
            throw new CarbonException("Cannot expire sessions", e);
        }
    }

    /**
     * Get all the current sessions for this webapp
     *
     * @return the current sessions for this webapp
     */
    public List<HttpSession> getSessions() {
        Session[] tomcatSessions = context.getManager().findSessions();
        List<HttpSession> sessions = new ArrayList<HttpSession>();
        for (Session tomcatSession : tomcatSessions) {
            sessions.add(new HttpSession(tomcatSession));
        }
        return sessions;
    }

    /**
     * Get the session corresponding to the <code>sessionId</code>
     *
     * @param sessionId The session ID
     * @return the session corresponding to the <code>sessionId</code>
     * @throws CarbonException If an error occurs while retrieving the session
     */
    public HttpSession getSession(String sessionId) throws CarbonException {
        Session session;
        try {
            session = context.getManager().findSession(sessionId);
        } catch (IOException e) {
            throw new CarbonException("Cannot find session " + sessionId, e);
        }
        if (session != null) {
            return new HttpSession(session);
        }
        return null;
    }

    public Statistics getStatistics() {
        return new Statistics(context.getManager());
    }

    @Override
    public String toString() {
        return context + ".File[" + webappFile.getAbsolutePath() + "]";
    }

    /**
     * Represents statistics corresponding to this webapp
     */
    public final static class Statistics {

        /**
         * The Tomcat Session Manager
         */
        private Manager sessionManager;

        private Statistics(Manager sessionManager) {
            this.sessionManager = sessionManager;
        }

        public int getMaxActiveSessions() {
            return sessionManager.getMaxActive();
        }

        public int getMaxSessionInactivityInterval() {
            return sessionManager.getMaxInactiveInterval();
        }

        public int getMaxSessionLifetime() {
            return sessionManager.getSessionMaxAliveTime();
        }

        public int getAvgSessionLifetime() {
            return sessionManager.getSessionAverageAliveTime();
        }

        public int getRejectedSessions() {
            return sessionManager.getRejectedSessions();
        }

        public int getActiveSessions() {
            if (GhostWebappDeployerUtils.isGhostOn()) {
                //If this webapp is in ghost from then we return 0
                if (isThisGhost || sessionManager == null) {
                    return 0;
                }
                return sessionManager.getActiveSessions();
            } else {
                return sessionManager.getActiveSessions();
            }
        }

        public long getExpiredSessions() {
            return sessionManager.getExpiredSessions();
        }
    }

    /**
     * Represents an HTTP session
     */
    public static class HttpSession {
        private Session tomcatSession;

        public HttpSession(Session tomcatSession) {
            this.tomcatSession = tomcatSession;
        }

        public String getSessionId() {
            return tomcatSession.getId();
        }

        public String getAuthType() {
            return tomcatSession.getAuthType();
        }

        public long getCreationTime() {
            return tomcatSession.getCreationTime();
        }

        public long getLastAccessedTime() {
            return tomcatSession.getLastAccessedTime();
        }

        public long getMaxInactiveInterval() {
            return tomcatSession.getMaxInactiveInterval();
        }

        public void expire() {
            tomcatSession.expire();
        }
    }

    /**
     * This method will return the Catalina Context for a particular webapp
     *
     * @return Context
     */
    public Context getContext() {
        return this.context;
    }

    /**
     * We need this method in ghost mode because, this will set a static variable which indicates
     * whether this Webapp is a ghost. We need a static variable to use it in the Statistics inner
     * class, which is a static class.
     *
     * @param isThisGhost boolean parameter to set the static variable
     */
    public void setIsGhostWebapp(boolean isThisGhost) {
        this.isThisGhost = isThisGhost;
    }
}
