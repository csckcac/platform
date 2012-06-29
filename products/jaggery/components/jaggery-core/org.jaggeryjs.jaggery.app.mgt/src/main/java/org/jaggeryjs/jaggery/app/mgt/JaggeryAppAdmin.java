/*
 *  Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.jaggeryjs.jaggery.app.mgt;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.utils.*;
import org.wso2.carbon.webapp.mgt.*;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import java.io.*;
import java.net.SocketException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * The Admin service for managing webapps
 */
@SuppressWarnings("unused")
public class JaggeryAppAdmin extends AbstractAdmin {

    private static final int BYTE_BUFFER_SIZE = 8192;

    private static final Log log = LogFactory.getLog(
            JaggeryAppAdmin.class);

    public JaggeryAppAdmin() {
    }

    public JaggeryAppAdmin(AxisConfiguration axisConfig) throws Exception {
        super(axisConfig);
    }

    /**
     * Get a page of started webapps
     *
     * @param webappSearchString Search string
     * @param webappState        State of the webapp.
     *                           Can be either JaggeryAppsConstants.WebappState.STARTED or
     *                           JaggeryAppsConstants.WebappState.STOPPED
     * @param pageNumber         The pageNumber of the page to be fetched
     * @return WebappsWrapper
     * @throws org.apache.axis2.AxisFault
     */
    public WebappsWrapper getPagedWebappsSummary(String webappSearchString,
                                                 String webappState,
                                                 int pageNumber) throws AxisFault {
        if (webappState.equalsIgnoreCase(JaggeryConstants.WebappState.STARTED)) {
            return getPagedWebapps(pageNumber, getStartedWebapps(webappSearchString));
        } else if (webappState.equalsIgnoreCase(JaggeryConstants.WebappState.STOPPED)) {
            return getPagedWebapps(pageNumber, getStoppedWebapps(webappSearchString));
        } else {
            throw new AxisFault("Invalid webapp state: ", webappState);
        }
    }

    /**
     * Get the details of a deplyed webapp
     *
     * @param webappFileName
     * @return
     */
    public WebappMetadata getStartedWebapp(String webappFileName) {
        WebApplicationsHolder holder = getWebappsHolder();
        WebApplication webapp = holder.getStartedWebapps().get(webappFileName);
        WebappMetadata webappMetadata = null;
        if (webapp != null) {
            webappMetadata = getWebapp(webapp);
            webappMetadata.setStarted(true);
        }
        return webappMetadata;
    }

    /**
     * Get the details of an stopped webapp
     *
     * @param webappFileName
     * @return
     */
    public WebappMetadata getStoppedWebapp(String webappFileName) {
        WebApplicationsHolder holder = getWebappsHolder();
        WebApplication webapp = holder.getStoppedWebapps().get(webappFileName);
        WebappMetadata webappMetadata = null;
        if (webapp != null) {
            webappMetadata = getWebapp(webapp);
            webappMetadata.setStarted(false);
        }
        return webappMetadata;
    }

    private WebappMetadata getWebapp(WebApplication webapp) {
        WebappMetadata webappMetadata;
        webappMetadata = new WebappMetadata();
        webappMetadata.setDisplayName(webapp.getDisplayName());
        webappMetadata.setContext(webapp.getContextName());
        webappMetadata.setLastModifiedTime(webapp.getLastModifiedTime());
        webappMetadata.setWebappFile(webapp.getWebappFile().getName());
        webappMetadata.setState(webapp.getState());

        WebApplication.Statistics statistics = webapp.getStatistics();
        WebappStatistics stats = new WebappStatistics();
        stats.setActiveSessions(statistics.getActiveSessions());
        stats.setAvgSessionLifetime(statistics.getAvgSessionLifetime());
        stats.setExpiredSessions(statistics.getExpiredSessions());
        stats.setMaxActiveSessions(statistics.getMaxActiveSessions());
        stats.setMaxSessionInactivityInterval(statistics.getMaxSessionInactivityInterval());
        stats.setMaxSessionLifetime(statistics.getMaxSessionLifetime());
        stats.setRejectedSessions(statistics.getRejectedSessions());

        webappMetadata.setStatistics(stats);
        return webappMetadata;
    }

    /**
     * @param webappSearchString
     * @param pageNumber
     * @return
     * @throws org.apache.axis2.AxisFault
     */
    public WebappsWrapper getPagedFaultyWebappsSummary(String webappSearchString,
                                                       int pageNumber) throws AxisFault {
        return getPagedWebapps(pageNumber, getFaultyWebapps(webappSearchString));
    }

    private WebappsWrapper getPagedWebapps(int pageNumber, List<WebappMetadata> webapps) {
        WebApplicationsHolder webappsHolder = getWebappsHolder();
        WebappsWrapper webappsWrapper = getWebappsWrapper(webappsHolder, webapps);
        try {
            webappsWrapper.setHostName(NetworkUtils.getLocalHostname());
        } catch (SocketException e) {
            log.error("Error occurred while getting local hostname", e);
        }

        if (getConfigContext().getAxisConfiguration().getTransportIn("http") != null) {
            int httpProxyPort = CarbonUtils.getTransportProxyPort(getConfigContext(), "http");
            if (httpProxyPort != -1) {
                webappsWrapper.setHttpPort(httpProxyPort);
            } else {
                int httpPort = CarbonUtils.getTransportPort(getConfigContext(), "http");
                webappsWrapper.setHttpPort(httpPort);
            }
        }

        if (getConfigContext().getAxisConfiguration().getTransportIn("https") != null) {
            int httpsProxyPort = CarbonUtils.getTransportProxyPort(getConfigContext(), "https");
            if (httpsProxyPort != -1) {
                webappsWrapper.setHttpsPort(httpsProxyPort);
            } else {
                int httpsPort = CarbonUtils.getTransportPort(getConfigContext(), "https");
                webappsWrapper.setHttpsPort(httpsPort);
            }
        }


        sortWebapps(webapps);
        DataPaginator.doPaging(pageNumber, webapps, webappsWrapper);
        return webappsWrapper;
    }

    private void sortWebapps(List<WebappMetadata> webapps) {
        if (webapps.size() > 0) {
            Collections.sort(webapps, new Comparator<WebappMetadata>() {
                public int compare(WebappMetadata arg0, WebappMetadata arg1) {
                    return arg0.getContext().compareToIgnoreCase(arg1.getContext());
                }
            });
        }
    }

    private List<WebappMetadata> getStartedWebapps(String webappsSearchString) {
        return getWebapps(getWebappsHolder().getStartedWebapps().values(), webappsSearchString);
    }

    private List<WebappMetadata> getStoppedWebapps(String webappsSearchString) {
        return getWebapps(getWebappsHolder().getStoppedWebapps().values(), webappsSearchString);
    }

    private List<WebappMetadata> getWebapps(Collection allWebapps,
                                            String webappsSearchString) {
        List<WebappMetadata> webapps = new ArrayList<WebappMetadata>();
        for (Object webapp : allWebapps) {
            if (!doesWebappSatisfySearchString((WebApplication) webapp, webappsSearchString)) {
                continue;
            }
            WebappMetadata webappMetadata = new WebappMetadata();
            webappMetadata.setDisplayName(((WebApplication) webapp).getDisplayName());
            webappMetadata.setContext(((WebApplication) webapp).getContextName());
            webappMetadata.setLastModifiedTime(((WebApplication) webapp).getLastModifiedTime());
            webappMetadata.setWebappFile(((WebApplication) webapp).getWebappFile().getName());
            webappMetadata.setState(((WebApplication) webapp).getState());
            WebappStatistics statistics = new WebappStatistics();
            statistics.setActiveSessions(((WebApplication) webapp).getStatistics().getActiveSessions());
            webappMetadata.setStatistics(statistics);

            webapps.add(webappMetadata);
        }
        return webapps;
    }

    private List<WebappMetadata> getFaultyWebapps(String webappsSearchString) {
        WebApplicationsHolder webappsHolder = getWebappsHolder();
        if (webappsHolder == null) {
            return null;
        }
        List<WebappMetadata> webapps = new ArrayList<WebappMetadata>();
        for (WebApplication webapp : webappsHolder.getFaultyWebapps().values()) {
            if (!doesWebappSatisfySearchString(webapp, webappsSearchString)) {
                continue;
            }
            WebappMetadata webappMetadata = new WebappMetadata();
            webappMetadata.setContext(webapp.getContextName());
            webappMetadata.setLastModifiedTime(webapp.getLastModifiedTime());
            webappMetadata.setWebappFile(webapp.getWebappFile().getName());
            webappMetadata.setStarted(false); //TODO
            webappMetadata.setRunning(false); //TODO
            webappMetadata.setFaulty(true);

            // Set the fault reason
            StringWriter sw = new StringWriter();
            webapp.getFaultReason().printStackTrace(new PrintWriter(sw));
            String faultException = sw.toString();
            webappMetadata.setFaultException(faultException);

            webapps.add(webappMetadata);
        }
        return webapps;
    }

    private boolean doesWebappSatisfySearchString(WebApplication webapp,
                                                  String searchString) {
        return searchString == null || searchString.trim().length() == 0 ||
                webapp.getContextName().toLowerCase(Locale.getDefault()).contains(searchString.toLowerCase(Locale.getDefault()));
    }

    private WebApplicationsHolder getWebappsHolder() {
        return (WebApplicationsHolder) getConfigContext().
                getProperty(CarbonConstants.WEB_APPLICATIONS_HOLDER);
    }

    private WebappsWrapper getWebappsWrapper(WebApplicationsHolder webappsHolder,
                                             List<WebappMetadata> webapps) {
        WebappsWrapper webappsWrapper = new WebappsWrapper();
        webappsWrapper.setWebapps(webapps.toArray(new WebappMetadata[webapps.size()]));
        webappsWrapper.setNumberOfCorrectWebapps(webappsHolder.getStartedWebapps().size());
        webappsWrapper.setNumberOfFaultyWebapps(webappsHolder.getFaultyWebapps().size());
        return webappsWrapper;
    }

    /**
     * Delete a set of started webapps
     *
     * @param webappFileNames The names of the webapp files to be deleted
     * @throws org.apache.axis2.AxisFault If an error occurs while deleting a webapp
     */
    public void deleteStartedWebapps(String[] webappFileNames) throws AxisFault {
        deleteWebapps(webappFileNames, getWebappsHolder().getStartedWebapps());
    }

    /**
     * Delete a set of stopped webapps
     *
     * @param webappFileNames The names of the webapp files to be deleted
     * @throws org.apache.axis2.AxisFault If an error occurs while deleting a webapp
     */
    public void deleteStoppedWebapps(String[] webappFileNames) throws AxisFault {
        deleteWebapps(webappFileNames, getWebappsHolder().getStoppedWebapps());
    }

    /**
     * Delete a set of faulty webapps
     *
     * @param webappFileNames The names of the webapp files to be deleted
     * @throws org.apache.axis2.AxisFault If an error occurs while deleting a webapp
     */
    public void deleteFaultyWebapps(String[] webappFileNames) throws AxisFault {
        deleteWebapps(webappFileNames, getWebappsHolder().getFaultyWebapps());
    }

    /**
     * Delete a single webapp which can be in any state; started, stopped or faulty. This method
     * will search the webapp in all lists and delete it if found.
     *
     * @param webappFileName - name of the file to be deleted
     * @throws org.apache.axis2.AxisFault - If an error occurs while deleting the webapp
     */
    public void deleteWebapp(String webappFileName) throws AxisFault {
        WebApplicationsHolder holder = getWebappsHolder();
        if (holder.getStartedWebapps().get(webappFileName) != null) {
            deleteStartedWebapps(new String[]{webappFileName});
        } else if (holder.getStoppedWebapps().get(webappFileName) != null) {
            deleteStoppedWebapps(new String[]{webappFileName});
        } else if (holder.getFaultyWebapps().get(webappFileName) != null) {
            deleteFaultyWebapps(new String[]{webappFileName});
        }
    }

    private void deleteWebapps(String[] webappFileNames,
                               Map<String, WebApplication> webapps) throws AxisFault {
        for (String webappFileName : webappFileNames) {
            WebApplication webapp = webapps.get(webappFileName);
            try {
                webapps.remove(webappFileName);
                webapp.delete();
            } catch (CarbonException e) {
                handleException("Could not delete webapp " + webapp, e);
            }
        }
    }

//    private void undeployWebapps(String[] webappFileNames,
//                                 Map<String, JaggeryApplication> webapps) throws AxisFault {
//        for (String webappFileName : webappFileNames) {
//            JaggeryApplication webapp = webapps.get(webappFileName);
//            try {
//                webapp.undeploy();
//                webapps.remove(webappFileName);
//            } catch (CarbonException e) {
//                handleException("Could not delete webapp " + webapp, e);
//            }
//        }
//    }

    /**
     * Delete all started webapps
     *
     * @throws org.apache.axis2.AxisFault If an error occurs while deleting a webapp
     */
    public void deleteAllStartedWebapps() throws AxisFault {
        deleteAllWebapps(getWebappsHolder().getStartedWebapps());
    }

    /**
     * Delete all stopped webapps
     *
     * @throws org.apache.axis2.AxisFault If an error occurs while deleting a webapp
     */
    public void deleteAllStoppedWebapps() throws AxisFault {
        deleteAllWebapps(getWebappsHolder().getStoppedWebapps());
    }

    /**
     * Delete all faulty webapps
     *
     * @throws org.apache.axis2.AxisFault If an error occurs while deleting a webapp
     */
    public void deleteAllFaultyWebapps() throws AxisFault {
        deleteAllWebapps(getWebappsHolder().getFaultyWebapps());
    }

    private void deleteAllWebapps(Map<String, WebApplication> webapps) throws AxisFault {
        for (WebApplication webapp : webapps.values()) {
            try {
                webapp.delete();
            } catch (CarbonException e) {
                handleException("Could not delete started webapp " + webapp, e);
            }
        }
        webapps.clear();
    }

    /**
     * Reload all webapps
     */
    public void reloadAllWebapps() {
        Map<String, WebApplication> startedWebapps = getWebappsHolder().getStartedWebapps();
        for (WebApplication webapp : startedWebapps.values()) {
            webapp.reload();
        }
    }

    /**
     * Reload a set of webapps
     *
     * @param webappFileNames The file names of the webapps to be reloaded
     */
    public void reloadWebapps(String[] webappFileNames) {
        for (String webappFileName : webappFileNames) {
            getWebappsHolder().getStartedWebapps().get(webappFileName).reload();
        }
    }

    /**
     * Undeploy all webapps
     *
     * @throws org.apache.axis2.AxisFault If an error occurs while undeploying
     */
    public void stopAllWebapps() throws AxisFault {
        Map<String, WebApplication> startedWebapps = getWebappsHolder().getStartedWebapps();
        for (WebApplication webapp : startedWebapps.values()) {
            try {
                webapp.stop();
            } catch (CarbonException e) {
                handleException("Error occurred while undeploying all webapps", e);
            }
        }
        startedWebapps.clear();
    }

    /**
     * Undeploy a set of webapps
     *
     * @param webappFileNames The file names of the webapps to be stopped
     * @throws org.apache.axis2.AxisFault If an error occurs while undeploying
     */
    public void stopWebapps(String[] webappFileNames) throws AxisFault {
        WebApplicationsHolder webappsHolder = getWebappsHolder();
        Map<String, WebApplication> startedWebapps = webappsHolder.getStartedWebapps();
        for (String webappFileName : webappFileNames) {
            try {
                WebApplication webapp = startedWebapps.get(webappFileName);
                webappsHolder.stopWebapp(webapp);
            } catch (CarbonException e) {
                handleException("Error occurred while undeploying webapps", e);
            }
        }
    }

    /**
     * Redeploy all webapps
     *
     * @throws org.apache.axis2.AxisFault If an error occurs while restarting webapps
     */
    public void startAllWebapps() throws AxisFault {
        Map<String, WebApplication> stoppedWebapps = getWebappsHolder().getStoppedWebapps();
//        Deployer webappDeployer =
//                ((DeploymentEngine) getAxisConfig().getConfigurator()).getDeployer(JaggeryConstants
//                        .WEBAPP_DEPLOYMENT_FOLDER, JaggeryConstants.WEBAPP_EXTENSION);
        for (WebApplication webapp : stoppedWebapps.values()) {
            startWebapp(stoppedWebapps, webapp);
        }
        stoppedWebapps.clear();
    }

    /**
     * Redeploy a set of webapps
     *
     * @param webappFileNames The file names of the webapps to be restarted
     * @throws org.apache.axis2.AxisFault If a deployment error occurs
     */
    public void startWebapps(String[] webappFileNames) throws AxisFault {
        WebApplicationsHolder webappsHolder = getWebappsHolder();
        Map<String, WebApplication> stoppedWebapps = webappsHolder.getStoppedWebapps();
//        Deployer webappDeployer =
//                ((DeploymentEngine) getAxisConfig().getConfigurator()).getDeployer(JaggeryConstants
//                        .WEBAPP_DEPLOYMENT_FOLDER, JaggeryConstants.WEBAPP_EXTENSION);
        for (String webappFileName : webappFileNames) {
            WebApplication webapp = stoppedWebapps.get(webappFileName);
            startWebapp(stoppedWebapps, webapp);
        }
    }

    private void startWebapp(Map<String, WebApplication> stoppedWebapps,
                             WebApplication webapp) throws AxisFault {
        try {
            boolean started = webapp.start();
            if (started) {
                String webappFileName = webapp.getWebappFile().getName();
                stoppedWebapps.remove(webappFileName);
                WebApplicationsHolder webappsHolder = getWebappsHolder();
                Map<String, WebApplication> startedWebapps = webappsHolder.getStartedWebapps();
                startedWebapps.put(webappFileName, webapp);
            }
        } catch (CarbonException e) {
            String msg = "Cannot start webapp " + webapp;
            log.error(msg, e);
            throw new AxisFault(msg, e);
        }
    }

    /**
     * Get all active sessions of a webapp
     *
     * @param webappFileName The names of the webapp file
     * @param pageNumber     The number of the page to fetch, starting with 0
     * @return The session array
     */
    public SessionsWrapper getActiveSessions(String webappFileName, int pageNumber) {
        WebApplication webapp = getWebappsHolder().getStartedWebapps().get(webappFileName);
        List<SessionMetadata> sessionMetadataList = new ArrayList<SessionMetadata>();
        int numOfActiveSessions = 0;
        if (webapp != null) {
            List<WebApplication.HttpSession> sessions = webapp.getSessions();
            numOfActiveSessions = sessions.size();
            for (WebApplication.HttpSession session : sessions) {
                sessionMetadataList.add(new SessionMetadata(session));
            }
        }
        sortSessions(sessionMetadataList);
        SessionsWrapper sessionsWrapper = new SessionsWrapper(sessionMetadataList);
        DataPaginator.doPaging(pageNumber, sessionMetadataList, sessionsWrapper);
        sessionsWrapper.setWebappFileName(webappFileName);
        sessionsWrapper.setNumberOfActiveSessions(numOfActiveSessions);
        return sessionsWrapper;
    }

    private void sortSessions(List<SessionMetadata> sessions) {
        if (sessions.size() > 0) {
            Collections.sort(sessions, new Comparator<SessionMetadata>() {
                public int compare(SessionMetadata arg0, SessionMetadata arg1) {
                    return (int) (arg0.getLastAccessedTime() - arg1.getLastAccessedTime());
                }
            });
        }
    }

    /**
     * Expire all sessions in all webapps
     */
    public void expireSessionsInAllWebapps() {
        Map<String, WebApplication> webapps = getWebappsHolder().getStartedWebapps();
        for (WebApplication webapp : webapps.values()) {
            webapp.expireAllSessions();
        }
    }

    /**
     * Expire all sessions in specified webapps
     *
     * @param webappFileNames The file names of the webapps whose sessions should be expired
     */
    public void expireSessionsInWebapps(String[] webappFileNames) {
        Map<String, WebApplication> webapps = getWebappsHolder().getStartedWebapps();
        for (String webappFileName : webappFileNames) {
            WebApplication webapp = webapps.get(webappFileName);
            webapp.expireAllSessions();
        }
    }

    /**
     * Expire all sessions in the specified webapp which has a
     * lifetime >= <code>maxSessionLifetimeMillis</code>
     *
     * @param webappFileName           The file name of the webapp whose sessions should be expired
     * @param maxSessionLifetimeMillis The max allowed lifetime for the sessions
     */
    public void expireSessionsInWebapp(String webappFileName, long maxSessionLifetimeMillis) {
        Map<String, WebApplication> webapps = getWebappsHolder().getStartedWebapps();
        WebApplication webapp = webapps.get(webappFileName);
        webapp.expireSessions(maxSessionLifetimeMillis);
    }

    /**
     * Expire a given session in a webapp
     *
     * @param webappFileName The file name of the webapp whose sessions should be expired
     * @param sessionIDs     Array of session IDs
     * @throws org.apache.axis2.AxisFault If an error occurs while retrieving sessions
     */
    public void expireSessions(String webappFileName, String[] sessionIDs) throws AxisFault {
        Map<String, WebApplication> webapps = getWebappsHolder().getStartedWebapps();
        WebApplication webapp = webapps.get(webappFileName);
        try {
            webapp.expireSessions(sessionIDs);
        } catch (CarbonException e) {
            handleException("Cannot expire specified sessions in webapp " + webappFileName, e);
        }
    }

    /**
     * Expire a given session in a webapp
     *
     * @param webappFileName The file name of the webapp whose sessions should be expired
     */
    public void expireAllSessions(String webappFileName) {
        Map<String, WebApplication> webapps = getWebappsHolder().getStartedWebapps();
        WebApplication webapp = webapps.get(webappFileName);
        webapp.expireAllSessions();
    }

    /**
     * Upload a webapp
     *
     * @param webappUploadDataList Array of data representing the webapps that are to be uploaded
     * @return true - if upload was successful
     * @throws org.apache.axis2.AxisFault If an error occurrs while uploading
     */
    public boolean uploadWebapp(WebappUploadData[] webappUploadDataList) throws AxisFault {

        AxisConfiguration axisConfig = getAxisConfig();
        String repoPath = axisConfig.getRepository().getPath();

        repoPath = (repoPath.endsWith(File.separator)) ? repoPath = repoPath.substring(0, repoPath.length() - 1) : repoPath;

        String jaggeryAppsPath = repoPath + File.separator +
                JaggeryConstants.WEBAPP_DEPLOYMENT_FOLDER;
        if (!repoPath.endsWith("/deployment/server")) {
            jaggeryAppsPath = repoPath + File.separator +
                    JaggeryConstants.WEBAPP_DEPLOYMENT_FOLDER_IN_JAGGERY;
        }

        for (WebappUploadData uploadData : webappUploadDataList) {
            String fName = uploadData.getFileName();
            if (fName.contains(".")) {
                fName = fName.split("\\.")[0];
            }

            File webappsDir = new File(jaggeryAppsPath + File.separator + fName);
            File jaggeryAppsFile = new File(jaggeryAppsPath);
            if (webappsDir.exists()) {
                String msg = "Jaggery app with the same name already exists";
                log.error(msg);
                throw new AxisFault(msg);
            } else if (!jaggeryAppsFile.exists()) {
                //if deployment directory is not there we create it
                if (!jaggeryAppsFile.mkdir()) {
                    String error = "Jaggery deployment directory not found and cannot be created when uploading";
                    log.error(error);
                    throw new AxisFault(error);
                }
            }

            ArchiveManipulator archiveManipulator = new ArchiveManipulator();
            try {
                archiveManipulator.extractFromStream(uploadData.getDataHandler().getInputStream(),
                        jaggeryAppsPath + File.separator + fName);
            } catch (IOException e) {
                log.error("Could not unzip the Jaggery App Archive", e);
                throw new AxisFault(e.getMessage(), e);
            }

        }
        return true;
    }

    private void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }

    /**
     * Downloads the webapp archive (.war) file
     *
     * @param fileName name of the .war that needs to be downloaded
     * @return the corresponding data handler of the .war that needs to be downloaded
     */
    public DataHandler downloadWarFileHandler(String fileName) {

        String repoPath = getAxisConfig().getRepository().getPath();
        String jaggeryAppsPath = repoPath + File.separator +
                JaggeryConstants.WEBAPP_DEPLOYMENT_FOLDER + File.separator + fileName;
        if (!repoPath.endsWith("/deployment/server/")) {
            jaggeryAppsPath = repoPath + File.separator +
                    JaggeryConstants.WEBAPP_DEPLOYMENT_FOLDER_IN_JAGGERY + File.separator + fileName;
        }

        File webAppFile = new File(jaggeryAppsPath);
        DataHandler handler = null;

        if (webAppFile.isDirectory()) {
            String zipTo = "tmp" + File.separator + fileName + ".zip";
            File fDownload = new File(zipTo);
            ArchiveManipulator archiveManipulator = new ArchiveManipulator();
            synchronized (this) {
                try {
                    archiveManipulator.archiveDir(zipTo, webAppFile.getAbsolutePath());
                    FileDataSource datasource = new FileDataSource(fDownload);
                    handler = new DataHandler(datasource);
                } catch (IOException e) {
                    log.error("Error downloading WAR file.", e);
                }
            }
        } else {
            FileDataSource datasource = new FileDataSource(new File(repoPath));
            handler = new DataHandler(datasource);
        }
        return handler;
    }

}