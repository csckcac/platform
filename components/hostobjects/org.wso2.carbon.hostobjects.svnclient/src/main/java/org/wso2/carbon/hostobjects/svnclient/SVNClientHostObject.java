/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.hostobjects.svnclient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.*;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.svnclientadapter.*;
import org.tigris.subversion.svnclientadapter.commandline.CmdLineClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.javahl.JhlClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.svnkit.SvnKitClientAdapterFactory;
import org.wso2.carbon.scriptengine.exceptions.ScriptException;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The SVN Client host object allows the mashups to get the information related to a given svn repository.
 * The information can be svn log, svn info, ..
 * This host object can be further extended to handle similar uses.
 */

public class SVNClientHostObject extends ScriptableObject {
    private static final String hostObjectName = "SVNClient";

    private SVNUrl svnUrl;
    /*   private  String userName;
 private  String password;*/

    private int logCount = 0;
    private int commitRange = 0;

    private String[] svnRepoInfo = new String[3];

    private ISVNClientAdapter svnClient;

    private static final Log log = LogFactory.getLog(SVNClientHostObject.class);

    private void initSVNClient(String userName, String password) throws ScriptException {
        try {
            SvnKitClientAdapterFactory.setup();
            log.debug("SVN Kit client adapter initialized");
        } catch (Throwable t) {
            log.debug("Unable to initialize the SVN Kit client adapter - Required jars " +
                    "may be missing", t);
        }

        try {
            JhlClientAdapterFactory.setup();
            log.debug("Java HL client adapter initialized");
        } catch (Throwable t) {
            log.debug("Unable to initialize the Java HL client adapter - Required jars " +
                    " or the native libraries may be missing", t);
        }

        try {
            CmdLineClientAdapterFactory.setup();
            log.debug("Command line client adapter initialized");
        } catch (Throwable t) {
            log.debug("Unable to initialize the command line client adapter - SVN command " +
                    "line tools may be missing", t);
        }


        String clientType;
        try {
            clientType = SVNClientAdapterFactory.getPreferredSVNClientType();
            svnClient = SVNClientAdapterFactory.createSVNClient(clientType);
            if ((userName != null) && (password != null)) {
                svnClient.setUsername(userName);
                svnClient.setPassword(password);
            }
        } catch (SVNClientException e) {
            throw new ScriptException("Client type can not be defined.");
        }

        if (svnClient == null) {
            throw new ScriptException("Failed to instantiate svn client.");
        }
    }


    public SVNClientHostObject(String userName, String password) throws ScriptException {
        try {
            initSVNClient(userName, password);
        } catch (ScriptException e) {
            String msg="Failed to initiate svn client";
            log.error(msg,e);
            throw new ScriptException(msg,e);
        }
    }

    public SVNClientHostObject() throws ScriptException {
        try {
            initSVNClient(null, null);
        } catch (ScriptException e) {
            String msg="Failed to initiate svn client";
            log.error(msg,e);
            throw new ScriptException(msg,e);
        }
    }

    /**
     * <p>The count of the logs that are considered</p>
     * <pre>
     *         svnClient.params = {commitRange: 1000, logCount:20};
     *
     * </pre>
     */
    public void jsSet_params(Object object) throws ScriptException {
        if (object instanceof NativeObject) {
            NativeObject nativeObject;
            nativeObject = (NativeObject) object;

            Object commitRangeObj = ScriptableObject.getProperty(nativeObject, "commitRange");
            Object logCountObj = ScriptableObject.getProperty(nativeObject, "logCount");
            if (commitRangeObj instanceof Integer) {
                commitRange = (Integer) commitRangeObj;
            } else {
                throw new ScriptException("commitRange field needs to be an integer.");
            }
            if (logCountObj instanceof Integer) {
                logCount = (Integer) logCountObj;
            } else {
                throw new ScriptException("logCount field needs to be an integer.");
            }
        } else {
            throw new ScriptException("Invalid parameter");
        }
    }

    public Integer jsGet_params() throws ScriptException {
        return logCount;
    }


    /**
     * <p>
     * The SVNClient Object has two different constructors. Choose one depending on your configuration
     * and your needs.
     * <p/>
     * 1. The first constructor takes no parameters and uses configuration information specified in the
     * configuration files. Using a configuration such as this is useful if you want to use the default
     * It also reduces the hassle of having to key in
     * the configuration details each time you need a new email object.
     * <p/>
     * var svnclient = new SVNClient();
     * <p/>
     * 2. The second constructor, unlike the first, requires the user to provide the svn repo location
     * each time he creates a new svnclient object.  The benefit is that no server configuration
     * is needed and you can use diffent accounts whenever you need. The configuration details
     * should be given as follows:
     * <p/>
     * var svnclient = new SVNClient("http://svn.wso2.org/repos/wso2/trunk/graphite/components/stratos/status-monitor");
     * // SVN Repository URL
     * <p/>
     * 3. The third is a slight variant of the second. It requires the svn user credentials to be specified:
     * (repository url, committer user name, and password)
     * <p/>
     * var svnclient = new SVNClient("http://svn.wso2.org/repos/wso2/trunk/graphite/components/stratos/status-monitor",
     * admin, root);
     * </p>
     */
    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj,
                                           boolean inNewExpr) throws ScriptException, MalformedURLException {
        String svnUrlL, userNameL = null, passwordL = null;
        int length = args.length;
        if (length == 1) {
            svnUrlL = (String) args[0];
        } else if (length == 3) {
            svnUrlL = (String) args[0];
            userNameL = (String) args[1];
            passwordL = (String) args[2];
        } else {
            throw new ScriptException("Incorrect number of arguments. Please specify repositoryUrl " +
                    "or repositoryUrl, username, password within the constructListor of SVNClient hostobject.");
        }

        if (svnUrlL == null) {
            throw new ScriptException("Invalid SVN Repository. Please recheck the given details of the repository.");
        }
        SVNUrl url;
        try {
            url = new SVNUrl(svnUrlL);
        } catch (MalformedURLException e) {
            log.error("SVN URL is malformed",e);
            throw e;
        }
        SVNClientHostObject svnClientHostObject = new SVNClientHostObject(userNameL, passwordL);
        svnClientHostObject.setSvnUrl(url);


        return svnClientHostObject;
    }


    public String[] jsFunction_getRepositoryInfo() throws MalformedURLException, ClientException, ScriptException {
        getRepositoryInformation();
        if (log.isDebugEnabled()) {
            log.debug("Last modified on: " + svnRepoInfo[0]);
            log.debug("Last committed author: " + svnRepoInfo[1]);
            log.debug("Last Changed Rev: " + svnRepoInfo[2]);
        }
        return svnRepoInfo;
    }

    private String[] getRepositoryInformation() throws MalformedURLException, ClientException, ScriptException {
        ISVNInfo svnInfo;
        try {
            svnInfo = svnClient.getInfo(svnUrl);
        } catch (SVNClientException e) {
            String msg="Failed to get svn info from "+svnUrl;
            log.error(msg,e);
            throw new ScriptException(msg,e);
        }
        Date lastChangedDate = svnInfo.getLastChangedDate();
        svnRepoInfo[0] = lastChangedDate.toString();

        String lastChangedAuthor = svnInfo.getLastCommitAuthor();
        svnRepoInfo[1] = lastChangedAuthor;

        SVNRevision.Number lastChangedRev = svnInfo.getLastChangedRevision();
        svnRepoInfo[2] = String.valueOf(lastChangedRev);
        return svnRepoInfo;
    }

    private ISVNLogMessage[] getCommitMessages() throws ClientException, MalformedURLException, ScriptException {
        getRepositoryInformation();

        SVNRevision endRevision;
        try {
            endRevision = SVNRevision.getRevision(svnRepoInfo[2]);
        } catch (ParseException e) {
            String msg="Failed to get svn revision from "+svnUrl;
            log.error(msg,e);
            throw new ScriptException(msg,e);
        }
        SVNRevision startRevision = null;
        if (commitRange < 1) {
            try {
                startRevision = SVNRevision.getRevision("0");
            } catch (ParseException e) {
                String msg="Failed to get svn revision from "+svnUrl;
                log.error(msg,e);
                throw new ScriptException(msg,e);
            }
        } else {
            try {
                System.out.println(startRevision);
                startRevision = SVNRevision.getRevision(Integer.toString(Integer.parseInt(svnRepoInfo[2]) - commitRange));
                System.out.println(startRevision);
            } catch (ParseException e) {
                String msg="Failed to get svn revision from "+svnUrl;
                log.error(msg,e);
                throw new ScriptException(msg,e);
            }
        }
        ISVNLogMessage logMessages[];
        try {
            logMessages = svnClient.getLogMessages(svnUrl, startRevision, endRevision);

        } catch (SVNClientException e) {
            String msg="Failed to get svn log messages from "+svnUrl;
            log.error(msg,e);
            throw new ScriptException(msg,e);
        }
        return logMessages;
    }


    public String[] jsFunction_getCommitLogs() throws Exception {
        List<String> commitLogs = new ArrayList<String>();
        ISVNLogMessage[] logMessages;
        StringBuilder commitLogBuf;
        int count = 1;

        logMessages = getCommitMessages(); //limit only upto the given count
        try {
            for (int i = logMessages.length - 1; i > 0; i--) {
                ISVNLogMessage logMessage = logMessages[i];
                commitLogBuf = new StringBuilder();
                commitLogBuf.append(logMessage.getRevision()).append(" | ").append(logMessage.getAuthor()).append(" | ").
                        append(logMessage.getDate()).append("\n").append(" | ").append(logMessage.getMessage());
                String msg = commitLogBuf.toString() + "\n";
                if (log.isDebugEnabled()) {
                    log.debug(msg);
                }
                commitLogs.add(msg);
                count++;
                if (count > logCount) {
                    break;
                }
            }
        } catch (Exception e) {
            String msg = "Error in getting the commit logs";
            log.error(msg);
            throw new Exception(msg, e);
        }
        return commitLogs.toArray(new String[(commitLogs.size())]);
    }

    @Override
    public String getClassName() {
        return hostObjectName;
    }

    public void setSvnUrl(SVNUrl svnUrl) {
        this.svnUrl = svnUrl;
    }

    public int getLogCount() {
        return logCount;
    }

    public void setLogCount(int logCount) {
        this.logCount = logCount;
    }

    public int getCommitRange() {
        return commitRange;
    }

    public void setCommitRange(int commitRange) {
        this.commitRange = commitRange;
    }
}
