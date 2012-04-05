/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.deployment.synchronizer.subversion;

import org.apache.axis2.util.JavaUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tigris.subversion.svnclientadapter.*;
import org.tigris.subversion.svnclientadapter.commandline.CmdLineClientAdapter;
import org.tigris.subversion.svnclientadapter.utils.Depth;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.deployment.synchronizer.ArtifactRepository;
import org.wso2.carbon.deployment.synchronizer.DeploymentSynchronizerException;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Subversion based artifact repository can be used in conjunction with the
 * DeploymentSynchronizer to synchronize a local repository against a remote
 * SVN repository. By default this implementation does not entertain SVN
 * externals but it can be enabled if required. This is based on the Subclipse
 * SVN client adapter which in turns support SVN Kit, Java HL and command line
 * SVN client adapters.
 */
public class SVNBasedArtifactRepository implements ArtifactRepository {

    private static final Log log = LogFactory.getLog(SVNBasedArtifactRepository.class);

    private static final int UNVERSIONED = SVNStatusKind.UNVERSIONED.toInt();
    private static final int MISSING = SVNStatusKind.MISSING.toInt();

    private static final boolean RECURSIVE = true;
    private static final boolean NO_SET_DEPTH = false;

    private SVNUrl svnUrl;
    private ISVNClientAdapter svnClient;

    private boolean ignoreExternals = true;
    private boolean forceUpdate = true;

    public void init(int tenantId) throws DeploymentSynchronizerException {
        ServerConfiguration serverConfig = ServerConfiguration.getInstance();
        String url = serverConfig.getFirstProperty(SVNConstants.SVN_URL);
        if (url == null) {
            handleException("SVN URL must be specified for the SVN based deployment synchronizer");
            return;
        }

        boolean appendTenantId = Boolean.parseBoolean(serverConfig.getFirstProperty(
                SVNConstants.SVN_URL_APPEND_TENANT_ID));
        if (appendTenantId) {
            if (!url.endsWith("/")) {
                url += "/";
            }
            url += tenantId;
        }

        try {
            svnUrl = new SVNUrl(url);
        } catch (MalformedURLException e) {
            handleException("Provided SVN URL is malformed: " + url, e);
        }

        String clientType = serverConfig.getFirstProperty(SVNConstants.SVN_CLIENT);
        if (clientType == null) {
            try {
                clientType = SVNClientAdapterFactory.getPreferredSVNClientType();
            } catch (SVNClientException e) {
                handleException("Error while retrieving the preferred SVN client type", e);
            }
        }

        svnClient = SVNClientAdapterFactory.createSVNClient(clientType);
        String user = serverConfig.getFirstProperty(SVNConstants.SVN_USER);
        if (user != null) {
            svnClient.setUsername(user);
            String password = serverConfig.getFirstProperty(SVNConstants.SVN_PASSWORD);
            svnClient.setPassword(password);
        }

        SVNNotifyListener notifyListener = new SVNNotifyListener();
        svnClient.addNotifyListener(notifyListener);
        svnClient.setProgressListener(notifyListener);
        svnClient.addConflictResolutionCallback(new DefaultSVNConflictResolver());

        String value = serverConfig.getFirstProperty(SVNConstants.SVN_IGNORE_EXTERNALS);
        if (value != null && JavaUtils.isFalseExplicitly(value)) {
            ignoreExternals = false;
        }

        value = serverConfig.getFirstProperty(SVNConstants.SVN_FORCE_UPDATE);
        if (value != null && JavaUtils.isFalseExplicitly(value)) {
            forceUpdate = false;
        }

        checkRemoteDirectory();
    }

    /**
     * Check whether the specified directory exists in the remote SVN repository. If the
     * directory does not exist, attempt to create it.
     *
     * @throws DeploymentSynchronizerException If an error occurs while creating the directory
     */
    private void checkRemoteDirectory() throws DeploymentSynchronizerException {
        try {
            ISVNInfo info = svnClient.getInfo(svnUrl);
            if (info != null && log.isDebugEnabled()) {
                log.debug("Remote directory: " + svnUrl + " exists");
            }
        } catch (SVNClientException ex) {
            if (log.isDebugEnabled()) {
                log.debug("Error while retrieving information from the directory: " + svnUrl, ex);
                log.debug("Attempting to create the directory: " + svnUrl);
            }

            try {
                svnClient.mkdir(svnUrl, true, "Directory creation by deployment synchronizer");
            } catch (SVNClientException e) {
                handleException("Error while attempting to create the directory: " + svnUrl, e);
            }
        }
    }

    private void svnAddFiles(File root) throws SVNClientException {
        ISVNStatus[] status = svnClient.getStatus(root, true, false);
        for (ISVNStatus s : status) {
            if (s.getTextStatus().toInt() == UNVERSIONED) {
                File file = s.getFile();
                String fileName = file.getName();
                if (fileName.startsWith(".") || fileName.startsWith("~") ||
                        fileName.endsWith(".bk")) {
                    continue;
                }

                if (file.isFile()) {
                    svnClient.addFile(file);
                } else {
                    // Do not svn add directories with the recursive option.
                    // That will add child directories and files that we don't want to add.
                    // First add the top level directory only.
                    svnClient.addDirectory(file, false);

                    // Then iterate over the children and add each of them by recursively calling
                    // this method
                    File[] children = file.listFiles(new FileFilter() {
                        public boolean accept(File file) {
                            return !file.getName().equals(".svn");
                        }
                    });

                    for (File child : children) {
                        svnAddFiles(child);
                    }
                }
            }
        }
    }

    public boolean commit(String filePath) throws DeploymentSynchronizerException {
        File root = new File(filePath);
        try {
            svnClient.cleanup(root);
            svnAddFiles(root);
            cleanupDeletedFiles(root);
            ISVNStatus[] status = svnClient.getStatus(root, true, false);
            if (status != null && status.length > 0 && !isAllUnversioned(status)) {
                File[] files = new File[] { root };
                svnClient.commit(files, "Commit initiated by deployment synchronizer", true);
                return true;
            } else {
                log.debug("No changes in the local working copy");
            }
        } catch (SVNClientException e) {
            handleException("Error while committing artifacts to the SVN repository", e);
        }
        return false;
    }

    private boolean isAllUnversioned(ISVNStatus[] status) {
        for (ISVNStatus s : status) {
            if (s.getTextStatus().toInt() != UNVERSIONED) {
                return false;
            }
        }
        return true;
    }

    public boolean checkout(String filePath) throws DeploymentSynchronizerException {
        File root = new File(filePath);
        try {
            cleanupDeletedFiles(root);
            ISVNStatus status = svnClient.getSingleStatus(root);
            if (status != null && status.getTextStatus().toInt() == UNVERSIONED) {
                cleanupUnversionedFiles(root);
                if (svnClient instanceof CmdLineClientAdapter) {
                    // CmdLineClientAdapter does not support all the options
                    svnClient.checkout(svnUrl, root, SVNRevision.HEAD, RECURSIVE);
                } else {
                    svnClient.checkout(svnUrl, root, SVNRevision.HEAD,
                            Depth.infinity, ignoreExternals, forceUpdate);
                }
                return true;
            } else {
                long filesUpdated = -1;
                svnClient.cleanup(root);
                if (svnClient instanceof CmdLineClientAdapter) {
                    // CmdLineClientAdapter does not support all the options
                    filesUpdated = svnClient.update(root, SVNRevision.HEAD, RECURSIVE);
                } else {
                    filesUpdated = svnClient.update(root, SVNRevision.HEAD,
                                                   Depth.infinity, NO_SET_DEPTH,
                                                   ignoreExternals, forceUpdate);
                }
                return filesUpdated > 1;
            }
        } catch (SVNClientException e) {
            handleException("Error while checking out or updating artifacts from the " +
                    "SVN repository", e);
        }
        return false;
    }

    private void cleanupUnversionedFiles(File root) throws SVNClientException {
        ISVNDirEntry[] entries = svnClient.getList(svnUrl, SVNRevision.HEAD, false);
        for (ISVNDirEntry entry : entries) {
            String fileName = entry.getPath();
            SVNNodeKind nodeType = entry.getNodeKind();
            File localFile = new File(root, fileName);
            if (localFile.exists()) {
                ISVNStatus status = svnClient.getSingleStatus(localFile);
                if (status != null && status.getTextStatus().toInt() != UNVERSIONED) {
                    continue;
                }

                if (localFile.isFile() && SVNNodeKind.FILE.equals(nodeType)) {
                    log.info("Unversioned file: " + localFile.getPath() + " will be deleted");
                    if (!localFile.delete()) {
                        log.error("Unable to delete the file: " + localFile.getPath());
                    }
                } else if (localFile.isDirectory() && SVNNodeKind.DIR.equals(nodeType)) {
                    log.info("Unversioned directory: " + localFile.getPath() + " will be deleted");
                    try {
                        FileUtils.deleteDirectory(localFile);
                    } catch (IOException e) {
                        log.error("Error while deleting the directory: " + localFile.getPath(), e);
                    }
                }
            }
        }
    }


    /**
     * Find the files and directories which are in the MISSING state and schedule them
     * for svn delete
     *
     * @param root Root directory of the local working copy
     * @throws SVNClientException If an error occurs in the SVN client
     */
    private void cleanupDeletedFiles(File root) throws SVNClientException {
        ISVNStatus[] status = svnClient.getStatus(root, true, false);
        if (status != null) {
            List<File> deletableFiles = new ArrayList<File>();
            for (ISVNStatus s : status) {
                int statusCode = s.getTextStatus().toInt();
                if (statusCode == MISSING) {
                    if (log.isDebugEnabled()) {
                        log.debug("Scheduling the file: " + s.getPath() + " for SVN delete");
                    }
                    deletableFiles.add(s.getFile());
                }
            }

            if (deletableFiles.size() > 0) {
                svnClient.remove(deletableFiles.toArray(new File[deletableFiles.size()]), true);
            }
        }
    }

    public void initAutoCheckout(boolean useEventing) throws DeploymentSynchronizerException {
        // Nothing to impl
    }

    public void cleanupAutoCheckout() {
        // Nothing to impl
    }

    private void handleException(String msg) throws DeploymentSynchronizerException {
        log.error(msg);
        throw new DeploymentSynchronizerException(msg);
    }

    private void handleException(String msg, Exception e) throws DeploymentSynchronizerException {
        log.error(msg, e);
        throw new DeploymentSynchronizerException(msg, e);
    }
}
