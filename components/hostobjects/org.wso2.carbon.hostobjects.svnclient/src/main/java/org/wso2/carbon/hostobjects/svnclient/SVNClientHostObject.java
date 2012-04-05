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
import org.tigris.subversion.javahl.Info;
import org.tigris.subversion.javahl.LogMessage;
import org.tigris.subversion.javahl.Revision;
import org.tigris.subversion.javahl.SVNClient;
import org.wso2.carbon.scriptengine.exceptions.ScriptException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * The SVN Client host object allows the mashups to get the information related to a given svn repository.
 * The information can be svn log, svn info, ..
 * This host object can be further extended to handle similar uses.
 */
public class SVNClientHostObject extends ScriptableObject {
    private static final String hostObjectName = "SVNClient";

    private static String svnUrl;
    private static String userName;
    private static String password;
    private NativeObject nativeObject;
    private int logCount = 0;
    private int commitRange = 0;

    private static String[] svnRepoInfo = new String[3];

    private static SVNClient svnClient;

    private Properties properties;

    private static final Log log = LogFactory.getLog(SVNClientHostObject.class);

    public SVNClientHostObject(String url) {
        svnUrl = url;
        svnClient = new org.tigris.subversion.javahl.SVNClient();
    }

    public SVNClientHostObject() {
        svnClient = new org.tigris.subversion.javahl.SVNClient();
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

        SVNClientHostObject svnClientHostObject = new SVNClientHostObject();
        svnClientHostObject.properties = new Properties();

        int length = args.length;
        if (length == 1) {
            svnUrl = (String) args[0];
        } else if (length == 3) {
            svnUrl = (String) args[0];
            userName = (String) args[1];
            password = (String) args[2];
        } else {
            throw new ScriptException("Incorrect number of arguments. Please specify repositoryUrl " +
                    "or repositoryUrl, username, password within the constructListor of SVNClient hostobject.");
        }

        if (svnUrl == null) {
            throw new ScriptException("Invalid SVN Repository. Please recheck the given details of the repository.");
        }

        try {
            URL url = new URL(svnUrl);
        } catch (MalformedURLException e) {
            throw e;
        }
        return svnClientHostObject;
    }


    public String[] jsFunction_getRepositoryInfo() throws MalformedURLException, ClientException {
        getRepositoryInformation();
        if (log.isDebugEnabled()) {
            log.debug("Last modified on: " + svnRepoInfo[0]);
            log.debug("Last committed author: " + svnRepoInfo[1]);
            log.debug("Last Changed Rev: " + svnRepoInfo[2]);
        }
        return svnRepoInfo;
    }

    private static String[] getRepositoryInformation() throws MalformedURLException, ClientException {
        Info svnInfo = svnClient.info(svnUrl);
        Date lastChangedDate = svnInfo.getLastChangedDate();
        svnRepoInfo[0] = lastChangedDate.toString();

        String lastChangedAuthor = svnInfo.getAuthor();
        svnRepoInfo[1] = lastChangedAuthor;

        long lastChangedRev = svnInfo.getLastChangedRevision();
        svnRepoInfo[2] = String.valueOf(lastChangedRev);
        return svnRepoInfo;
    }

    private LogMessage[] getCommitMessages() throws ClientException, MalformedURLException {
        getRepositoryInformation();
        int lastChangedRevisionNumber = Integer.parseInt(svnRepoInfo[2]);
        Revision endRevision = Revision.getInstance(lastChangedRevisionNumber);
        Revision startRevision;
        if (commitRange < 1) {
            startRevision = Revision.getInstance(0);
        } else {
            startRevision = Revision.getInstance(lastChangedRevisionNumber - commitRange);
        }

        return svnClient.logMessages(svnUrl, startRevision, endRevision);
    }


    public String[] jsFunction_getCommitLogs() throws Exception {
        List<String> commitLogs = new ArrayList<String>();
        LogMessage[] logMessages;
        StringBuilder commitLogBuf;
        int count = 1;

        logMessages = getCommitMessages(); //limit only upto the given count
        try {
            for (int i = logMessages.length - 1; i > 0; i--) {
                LogMessage logMessage = logMessages[i];
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
}
