/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.wso2.carbon.appfactory.svn.repository.mgt.impl;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.shared.invoker.*;
import org.tigris.subversion.svnclientadapter.*;
import org.tigris.subversion.svnclientadapter.commandline.CmdLineClientAdapter;
import org.tigris.subversion.svnclientadapter.commandline.CmdLineClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.javahl.JhlClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.svnkit.SvnKitClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.utils.Depth;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.svn.repository.mgt.RepositoryMgtException;
import org.wso2.carbon.appfactory.svn.repository.mgt.beans.Permission;
import org.wso2.carbon.appfactory.svn.repository.mgt.beans.PermissionType;
import org.wso2.carbon.appfactory.svn.repository.mgt.beans.Repository;
import org.wso2.carbon.appfactory.svn.repository.mgt.util.Util;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * SCM-manager specific repository manager implementation
 */
public class SCMManagerBasedRepositoryManager extends AbstractRepositoryManager {
    private static final Log log = LogFactory.getLog(SCMManagerBasedRepositoryManager.class);
    //rest URIs
    private static final String REST_BASE_URI = "/scm/api/rest";
    private static final String REST_CREATE_REPOSITORY_URI = "/repositories";
    private static final String REST_GET_REPOSITORY_URI = "/repositories/svn/";

    //repositories xml elements
    private static final String REPOSITORY_XML_ROOT_ELEMENT = "repositories";
    private static final String REPOSITORY_NAME_ELEMENT = "name";
    private static final String REPOSITORY_TYPE_ELEMENT = "type";
    private static final String REPOSITORY_URL_ELEMENT = "url";

    //permission xml elements
    private static final String PERMISSION_XML_ROOT_ELEMENT = "permissions";
    private static final String PERMISSION_TYPE_ELEMENT = "type";
    private static final String PERMISSION_NAME_ELEMENT = "name";
    private static final String PERMISSION_GROUP_PERMISSION_ELEMENT = "groupPermission";
    private AppFactoryConfiguration configuration;
    private ISVNClientAdapter svnClient;
    private String clientType;
    private static AppFactoryConfiguration appFactoryConfiguration = Util.getConfiguration();

    @Override
    public String createRepository(String applicationKey) throws RepositoryMgtException {

        HttpClient client = getClient(configuration);
        PostMethod post = new PostMethod(getServerURL(configuration) + REST_BASE_URI +
                REST_CREATE_REPOSITORY_URI);
        Repository repository = new Repository();
        repository.setName(applicationKey);
        repository.setType("svn");

        Permission permission = new Permission();
        permission.setGroupPermission(true);
        permission.setName(applicationKey);
        permission.setType(PermissionType.WRITE);
        ArrayList<Permission> permissions = new ArrayList<Permission>();
        permissions.add(permission);

        repository.setPermissions(permissions);


        post.setRequestEntity(new ByteArrayRequestEntity(getRepositoryAsString(repository)));
        post.setDoAuthentication(true);
        post.addRequestHeader("Content-Type", "application/xml;charset=UTF-8");

        String url = null;
        try {
            client.executeMethod(post);
        } catch (IOException e) {
            String msg = "Error while invoking the web service";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        } finally {
            HttpConnectionManager manager = client.getHttpConnectionManager();
            if (manager instanceof SimpleHttpConnectionManager) {
                ((SimpleHttpConnectionManager) manager).shutdown();
            }
        }
        if (post.getStatusCode() == HttpStatus.SC_CREATED) {
            url = getURL(applicationKey);
        } else {
            String msg = "Repository creation is failed for " + applicationKey + " server returned status " +
                    post.getStatusText();
            log.error(msg);
            throw new RepositoryMgtException(msg);
        }
        return url;
    }

    private byte[] getRepositoryAsString(Repository repo) throws RepositoryMgtException {
        OMFactory factory = OMAbstractFactory.getOMFactory();

        OMElement repository = factory.createOMElement(REPOSITORY_XML_ROOT_ELEMENT, null);

        OMElement name = factory.createOMElement(REPOSITORY_NAME_ELEMENT, null);
        name.setText(repo.getName());
        repository.addChild(name);
        OMElement type = factory.createOMElement(REPOSITORY_TYPE_ELEMENT, null);
        type.setText(repo.getType());
        repository.addChild(type);

        for (Permission perm : repo.getPermissions()) {


            OMElement permission = factory.createOMElement(PERMISSION_XML_ROOT_ELEMENT, null);
            OMElement groupPermission = factory.createOMElement(PERMISSION_GROUP_PERMISSION_ELEMENT, null);
            groupPermission.setText(String.valueOf(perm.getGroupPermission()));
            permission.addChild(groupPermission);

            OMElement permName = factory.createOMElement(PERMISSION_NAME_ELEMENT, null);
            permName.setText(perm.getName());
            OMElement permType = factory.createOMElement(PERMISSION_TYPE_ELEMENT, null);
            permType.setText(perm.getType().toString());

            permission.addChild(permName);
            permission.addChild(permType);
            repository.addChild(permission);
        }
        StringWriter writer = new StringWriter();
        try {
            repository.serialize(writer);
        } catch (XMLStreamException e) {
            String msg = "Error while serializing the payload";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                String msg = "Error while closing the reader";
                log.error(msg, e);

            }
        }

        return writer.toString().getBytes();


    }

    @Override
    public String getURL(String applicationKey) throws RepositoryMgtException {

        HttpClient client = getClient(configuration);
        GetMethod get = new GetMethod(getServerURL(configuration) + REST_BASE_URI + REST_GET_REPOSITORY_URI
                + applicationKey);
        get.setDoAuthentication(true);
        get.addRequestHeader("Content-Type", "application/xml;charset=UTF-8");
        String repository = null;
        try {
            client.executeMethod(get);
            repository = getRepositoryFromStream(get.getResponseBodyAsStream()).getUrl();
        } catch (IOException e) {
            String msg = "Error while invoking the service";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        } finally {
            HttpConnectionManager manager = client.getHttpConnectionManager();
            if (manager instanceof SimpleHttpConnectionManager) {
                ((SimpleHttpConnectionManager) manager).shutdown();
            }
        }

        return repository;
    }

    @Override
    public void setConfig(AppFactoryConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public AppFactoryConfiguration getConfig() {
        return configuration;
    }

    private Repository getRepositoryFromStream(InputStream responseBodyAsStream)
            throws RepositoryMgtException {
        XMLInputFactory xif = XMLInputFactory.newInstance();
        XMLStreamReader reader = null;
        Repository repository = new Repository();
        try {
            reader = xif.createXMLStreamReader(responseBodyAsStream);
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            OMElement rootElement = builder.getDocumentElement();
            if (REPOSITORY_XML_ROOT_ELEMENT.equals(rootElement.getLocalName())) {
                Iterator elements = rootElement.getChildElements();
                while (elements.hasNext()) {
                    Object object = elements.next();
                    if (object instanceof OMElement) {
                        OMElement element = (OMElement) object;
                        if (REPOSITORY_URL_ELEMENT.equals(element.getLocalName())) {
                            repository.setUrl(element.getText());
                            break;
                        }
                    }

                }
            } else {
                String msg = "In the payload no repository information is found";
                log.error(msg);
                throw new RepositoryMgtException(msg);
            }
        } catch (XMLStreamException e) {
            String msg = "Error while reading the stream";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (XMLStreamException e) {
                String msg = "Error while serializing the payload";
                log.error(msg, e);

            }
        }


        return repository;
    }


    /*
        setup SVNClientAdapterFactory
    */
    private void setUp() {
        try {
            try {
                SvnKitClientAdapterFactory.setup();
                System.out.print("SVN Kit client adapter initialized");
            } catch (Throwable t) {
                System.out.print("Unable to initialize the SVN Kit client adapter - Required jars " + "may be missing");
            }
            //setup CmdLineClientAdapterFactory - which is the default
            CmdLineClientAdapterFactory.setup();

            clientType = SVNClientAdapterFactory.getPreferredSVNClientType();
            svnClient = SVNClientAdapterFactory.createSVNClient(clientType);

            // providing the credentials
            svnClient.setUsername(AppFactoryConstants.SCM_ADMIN_NAME);
            svnClient.setPassword(AppFactoryConstants.SCM_ADMIN_NAME);

            log.info("Command line client adapter initialized");

        } catch (SVNClientException e) {
            log.error("Unable to initialize the command line client adapter" + e);
        }
    }

    /*
     Create a new directory in the svn
    */
    public void createDirectory(String url, String commitMessage) {
        setUp();
        try {
            SVNUrl svnUrl = null;
            try {
                svnUrl = new SVNUrl(url);
            } catch (MalformedURLException e) {
                log.error("Malformed url" + e);
            }
            svnClient.mkdir(svnUrl, commitMessage);
        } catch (SVNClientException e) {
            log.error(e);
        }

    }

    /*
      svn copy operation
    */
    public void svnCopy(String sourceUrl, String destinationUrl, String commitMessage, SVNRevision rev) {
        setUp();
        rev = SVNRevision.HEAD; //last revision is used
        try {
            SVNUrl svnUrl = null;
            SVNUrl copyURL = null;
            try {
                // construct the svnURL
                svnUrl = new SVNUrl(sourceUrl);
                copyURL = new SVNUrl(destinationUrl);
            } catch (MalformedURLException e) {
                log.error("Malformed url" + e);
            }
            svnClient.copy(svnUrl, copyURL, commitMessage, rev);
        } catch (SVNClientException e) {
            log.error(e);
        }

    }

    /*
      svn move operation
    */
    public void svnMove(String sourceUrl, String destinationUrl, String commitMessage, SVNRevision rev) {
        setUp();
        // Get the current revision
        rev = SVNRevision.HEAD; //last revision is used
        try {
            SVNUrl svnUrl = null;
            SVNUrl copyURL = null;
            try {
                // construct the svnURL
                svnUrl = new SVNUrl(sourceUrl);
                copyURL = new SVNUrl(destinationUrl);
            } catch (MalformedURLException e) {
                log.error("Malformed url" + e);
            }
            svnClient.move(svnUrl, copyURL, commitMessage, rev);
        } catch (SVNClientException e) {
            log.error(e);
        }

    }

    public void initSVNClient() throws SCMManagerExceptions {
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
            svnClient.setUsername(appFactoryConfiguration.getFirstProperty(
                    AppFactoryConstants.SCM_ADMIN_NAME));
            svnClient.setPassword(appFactoryConfiguration.getFirstProperty(
                    AppFactoryConstants.SCM_ADMIN_PASSWORD));
        } catch (SVNClientException e) {
            throw new SCMManagerExceptions("Client type can not be defined.");
        }

        if (svnClient == null) {
            throw new SCMManagerExceptions("Failed to instantiate svn client.");
        }
    }

    /**
     * Check out from given url
     *
     * @param applicationSvnUrl - application svn url location
     * @param applicationId     - application id
     * @return application check out path.
     * @throws SCMManagerExceptions
     *
     */
    public String checkoutApplication(String applicationSvnUrl, String applicationId, String svnRevision)
            throws SCMManagerExceptions {
        File checkoutDirectory = createApplicationCheckoutDirectory(applicationId);
        initSVNClient();

        SVNUrl svnUrl = null;
        try {
            svnUrl = new SVNUrl(applicationSvnUrl);
        } catch (MalformedURLException e) {
            handleException("SVN URL of application is malformed.", e);
        }

        try {
            if (svnRevision != null && !"".equals(svnRevision)) {
                SVNRevision revision = SVNRevision.getRevision(svnRevision);

                if (svnClient instanceof CmdLineClientAdapter) {
                    // CmdLineClientAdapter does not support all the options
                    svnClient.checkout(svnUrl, checkoutDirectory, revision, true);
                } else {
                    svnClient.checkout(svnUrl, checkoutDirectory, revision,
                            Depth.infinity, true, true);
                }
            } else {
                throw new SCMManagerExceptions("SVN revision number is null or empty");
            }
        } catch (SVNClientException e) {
            handleException("Failed to checkout code from SVN URL:" + svnUrl, e);
        } catch (ParseException e) {
            handleException("SVN revision: " + svnRevision + " is not valid ", e);
        }
        return checkoutDirectory.getAbsolutePath();
    }

    /**
     * Build the application from given location
     *
     * @param sourcePath - source location
     * @throws SCMManagerExceptions
     *
     */
    public void buildApplication(String sourcePath) throws SCMManagerExceptions {
        String pomFilePath = sourcePath + File.separator + "pom.xml";
        File pomFile = new File(pomFilePath);
        if (!pomFile.exists()) {
            handleException("pom.xml file not found at " + pomFilePath);
        }
        executeMavenGoal(sourcePath);
        String targetDirPath = sourcePath + File.separator + "target";
        File targetDir = new File(targetDirPath);
        if (!targetDir.exists()) {
            handleException("Application build failure.");
        }
    }

    public File createApplicationCheckoutDirectory(String applicationName)
            throws SCMManagerExceptions {
        File tempDir = new File(CarbonUtils.getTmpDir() + File.separator + applicationName);
        if (!tempDir.exists()) {
            boolean directoriesCreated = tempDir.mkdirs();
            if (!directoriesCreated) {
                handleException("Failed to create directory path:" + tempDir.getAbsolutePath());
            }
        }
        return tempDir;
    }


    private void handleException(String msg) throws SCMManagerExceptions {
        log.error(msg);
        throw new SCMManagerExceptions(msg);
    }

    private void handleException(String msg, Exception e) throws SCMManagerExceptions {
        log.error(msg, e);
        throw new SCMManagerExceptions(msg, e);
    }

    public boolean executeMavenGoal(String applicationPath)
            throws SCMManagerExceptions {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setShowErrors(true);

        request.setPomFile(new File(applicationPath + File.separator + "pom.xml"));

        List<String> goals = new ArrayList<String>();
        goals.add("clean");
        goals.add("install");

        request.setGoals(goals);
        Invoker invoker = new DefaultInvoker();
        InvocationOutputHandler outputHandler = new SystemOutHandler();
        invoker.setErrorHandler(outputHandler);

        try {
            InvocationResult result = invoker.execute(request);
            //Todo: need to get the build error, exception back to the user.
            if (result.getExecutionException() == null) {
                if (result.getExitCode() != 0) {
                    request.setOffline(true);
                    result = invoker.execute(request);
                    if (result.getExitCode() == 0) {
                        return true;
                    } else {
                        final String errorMessage = "No maven Application found at "
                                + applicationPath;
                        handleException(errorMessage);
                    }
                }
                return true;
            }
        } catch (MavenInvocationException e) {
            handleException("Maven invocation failed with error:" + e.getLocalizedMessage(), e);
        }
        return false;
    }

    public void cleanApplicationDir(String applicationPath) {
        File application = new File(applicationPath);
        try {
            FileUtils.deleteDirectory(application);
        } catch (IOException ignore) {
            log.warn("Failed to clean up application at path:" + applicationPath);
        }
    }

    public String getAdminUsername(String applicationId) {
        return appFactoryConfiguration.getFirstProperty(
                AppFactoryConstants.SERVER_ADMIN_NAME) + "@" + applicationId;
    }




}
