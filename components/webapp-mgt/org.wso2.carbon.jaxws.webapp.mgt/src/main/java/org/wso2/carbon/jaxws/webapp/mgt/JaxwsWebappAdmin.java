package org.wso2.carbon.jaxws.webapp.mgt;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.webapp.mgt.*;

import javax.activation.DataHandler;
import java.io.File;

public class JaxwsWebappAdmin extends WebappAdmin {

    public WebappsWrapper getPagedWebappsSummary(String webappSearchString,
                                                 String webappState,
                                                 int pageNumber) throws AxisFault {
        return super.getPagedWebappsSummary(webappSearchString, webappState, pageNumber);
    }

    public WebappMetadata getStartedWebapp(String webappFileName) {
        return super.getStartedWebapp(webappFileName);
    }

    public WebappMetadata getStoppedWebapp(String webappFileName) {
        return super.getStoppedWebapp(webappFileName);
    }

    public WebappsWrapper getPagedFaultyWebappsSummary(String webappSearchString,
                                                       int pageNumber) throws AxisFault {
        return super.getPagedFaultyWebappsSummary(webappSearchString, pageNumber);
    }

    public void deleteStartedWebapps(String[] webappFileNames) throws AxisFault {
        super.deleteStartedWebapps(webappFileNames);
    }

    public void deleteStoppedWebapps(String[] webappFileNames) throws AxisFault {
        super.deleteStoppedWebapps(webappFileNames);
    }

    public void deleteFaultyWebapps(String[] webappFileNames) throws AxisFault {
        super.deleteFaultyWebapps(webappFileNames);
    }

    public void deleteWebapp(String webappFileName) throws AxisFault {
        super.deleteWebapp(webappFileName);
    }

    public void deleteAllStartedWebapps() throws AxisFault {
        super.deleteAllStartedWebapps();
    }

    public void deleteAllStoppedWebapps() throws AxisFault {
        super.deleteAllStoppedWebapps();
    }

    public void deleteAllFaultyWebapps() throws AxisFault {
        super.deleteAllFaultyWebapps();
    }

    public void reloadAllWebapps() {
        super.reloadAllWebapps();
    }

    public void reloadWebapps(String[] webappFileNames) {
        super.reloadWebapps(webappFileNames);
    }

    public void stopAllWebapps() throws AxisFault {
        super.stopAllWebapps();
    }

    public void stopWebapps(String[] webappFileNames) throws AxisFault {
        super.stopWebapps(webappFileNames);
    }

    public void startAllWebapps() throws AxisFault {
        super.startAllWebapps();
    }

    public void startWebapps(String[] webappFileNames) throws AxisFault {
        super.startWebapps(webappFileNames);
    }

    public SessionsWrapper getActiveSessions(String webappFileName, int pageNumber) {
        return super.getActiveSessions(webappFileName, pageNumber);
    }

    public void expireSessionsInAllWebapps() {
        super.expireSessionsInAllWebapps();
    }

    public void expireSessionsInWebapps(String[] webappFileNames) {
        super.expireSessionsInWebapps(webappFileNames);
    }

    public void expireSessionsInWebapp(String webappFileName, long maxSessionLifetimeMillis) {
        super.expireSessionsInWebapp(webappFileName, maxSessionLifetimeMillis);
    }

    public void expireSessions(String webappFileName, String[] sessionIDs) throws AxisFault {
        super.expireSessions(webappFileName, sessionIDs);
    }

    public void expireAllSessions(String webappFileName) {
        super.expireAllSessions(webappFileName);
    }

    public boolean uploadWebapp(WebappUploadData[] webappUploadDataList) throws AxisFault {
        return super.uploadWebapp(webappUploadDataList);
    }

    public DataHandler downloadWarFileHandler(String fileName) {
        return super.downloadWarFileHandler(fileName);
    }

    protected String getWebappDeploymentDirPath() {
        return getAxisConfig().getRepository().getPath() + File.separator +
                JaxwsWebappConstants.JAX_WEBAPP_DEPLOYMENT_DIR;
    }

    protected boolean isWebappRelevant(WebApplication webapp) {
        // Check the filter..
        String filterProp = (String) webapp.getProperty(WebappsConstants.WEBAPP_FILTER);
        // If this is a JAX webapp, return true..
        if (filterProp != null &&
                JaxwsWebappConstants.JAX_WEBAPP_FILTER_PROP.equals(filterProp)) {
            return true;
        }
        return false;
    }

}
