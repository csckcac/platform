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
import org.wso2.carbon.utils.FileManipulator;

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

    protected Context context;
    protected File webappFile;
    private long lastModifiedTime;
    private Exception faultReason;
    private Map<String, Object> properties = new HashMap<String, Object>();

    public WebApplication(Context context, File webappFile) {
        this.context = context;
        setWebappFile(webappFile);
        setLastModifiedTime(webappFile.lastModified());
    }

    public WebApplication(File webappFile) {
        setWebappFile(webappFile);
        setLastModifiedTime(webappFile.lastModified());
    }

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

    public void setLastModifiedTime(long lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public long getLastModifiedTime() {
        return lastModifiedTime;
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

    public boolean reload() {
        if (context.getAvailable()) {
            context.reload();
            log.info("Reloaded webapp: " + context);
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
        try {
            if (context.getAvailable()) {
                context.stop();
                log.info("Stopped webapp: " + context);
                return true;
            }
        } catch (Exception e) {
            throw new CarbonException("Cannot temporarilly stop webapp " + context, e);
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
        try {
            if (!context.getAvailable()) {
                context.start();
                log.info("Started webapp: " + context);
                return true;
            }
        } catch (Exception e) {
            throw new CarbonException("Cannot start webapp " + context, e);
        }
        return false;
    }

    /**
     * Lazily unload this Web application.
     *
     * @throws CarbonException If an error occurs while lazy unloading
     */
    public void lazyUnload() throws CarbonException {
        try {
            if (context.getAvailable()) {
                // If the following is not done, the Realm will throw a LifecycleException, because
                // Realm.stop is called for each context.
                context.setRealm(null);
                context.stop();
                context.destroy();
                log.info("Unloaded webapp: " + context);
            }
            //if the webapp is stopped above context.getAvailable() becomes false.
            //So to unload stopped webapps this is done.
            else if (LifecycleState.STOPPED.equals(context.getState())) {
                context.setRealm(null);
                context.destroy();
                log.info("Unloaded webapp: " + context);
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
        lazyUnload();
        File webappDir = new File(getAppBase(), context.getBaseName());
        if(TomcatUtil.checkUnpackWars() && webappDir.exists() && !FileManipulator.deleteDir(webappDir)) {
            throw new CarbonException("exploded Webapp directory " + webappDir + " deletion failed");
        }
    }

    /**
     *
     * Completely delete & destroy this Web application
     *
     * @throws CarbonException If an error occurs while undeploying this webapp
     */
    public void delete() throws CarbonException {
        undeploy();
        if (!webappFile.delete()) {
            throw new CarbonException("Webapp file " + webappFile + " deletion failed");
        }
    }

    /**
     * Return a File object representing the "application root" directory
     * for our associated Host.
     *
     * @return The AppBase
     * //TODO - when webapp exploding is supported for stratos, this should return the tenant's webapp dir
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
    public static class Statistics {

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
            return sessionManager.getActiveSessions();
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
}
