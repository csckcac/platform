package org.wso2.carbon.appfactory.application.deployment.service;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.maven.shared.invoker.SystemOutHandler;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.tigris.subversion.svnclientadapter.commandline.CmdLineClientAdapter;
import org.tigris.subversion.svnclientadapter.commandline.CmdLineClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.javahl.JhlClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.svnkit.SvnKitClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.utils.Depth;
import org.wso2.carbon.appfactory.application.deployment.service.internal.AppFactoryConfigurationHolder;
import org.wso2.carbon.appfactory.application.deployment.service.internal.ApplicationUploadClient;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.application.mgt.stub.upload.types.carbon.UploadedFileItem;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.utils.CarbonUtils;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class ApplicationDeploymentService extends AbstractAdmin {
    private static final Log log = LogFactory.getLog(ApplicationDeploymentService.class);
    private static AppFactoryConfiguration appFactoryConfiguration = AppFactoryConfigurationHolder.getInstance().getAppFactoryConfiguration();
    private ISVNClientAdapter svnClient;


    private void initSVNClient() throws ApplicationDeploymentExceptions {
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
            throw new ApplicationDeploymentExceptions("Client type can not be defined.");
        }

        if (svnClient == null) {
            throw new ApplicationDeploymentExceptions("Failed to instantiate svn client.");
        }
    }

    /**
     * Check out from given url
     *
     * @param applicationSvnUrl - application svn url location
     * @param applicationId     - application id
     * @return application check out path.
     * @throws ApplicationDeploymentExceptions
     *
     */
    private String checkoutApplication(String applicationSvnUrl, String applicationId)
            throws ApplicationDeploymentExceptions {
        File checkoutDirectory = createApplicationCheckoutDirectory(applicationId);
        initSVNClient();

        SVNUrl svnUrl = null;
        try {
            svnUrl = new SVNUrl(applicationSvnUrl);
        } catch (MalformedURLException e) {
            handleException("SVN URL of application is malformed.", e);
        }

        try {
            if (svnClient instanceof CmdLineClientAdapter) {
                // CmdLineClientAdapter does not support all the options
                svnClient.checkout(svnUrl, checkoutDirectory, SVNRevision.HEAD, true);
            } else {
                svnClient.checkout(svnUrl, checkoutDirectory, SVNRevision.HEAD,
                                   Depth.infinity, true, true);
            }
        } catch (SVNClientException e) {
            handleException("Failed to checkout code from SVN URL:" + svnUrl, e);
        }
        return checkoutDirectory.getAbsolutePath();
    }

    /**
     * Build the application from given location
     *
     * @param sourcePath - source location
     * @throws ApplicationDeploymentExceptions
     *
     */
    private void buildApplication(String sourcePath) throws ApplicationDeploymentExceptions {
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

    private File createApplicationCheckoutDirectory(String applicationName)
            throws ApplicationDeploymentExceptions {
        File tempDir = new File(CarbonUtils.getTmpDir() + File.separator + applicationName);
        if (!tempDir.exists()) {
            boolean directoriesCreated = tempDir.mkdirs();
            if (!directoriesCreated) {
                handleException("Failed to create directory path:" + tempDir.getAbsolutePath());
            }
        }
        return tempDir;
    }


    private void handleException(String msg) throws ApplicationDeploymentExceptions {
        log.error(msg);
        throw new ApplicationDeploymentExceptions(msg);
    }

    private void handleException(String msg, Exception e) throws ApplicationDeploymentExceptions {
        log.error(msg, e);
        throw new ApplicationDeploymentExceptions(msg, e);
    }

    //TODO car files deployment???
    public Application[] deployApplication(String applicationSvnUrl, String applicationId,
                                           String stage) throws ApplicationDeploymentExceptions {

        String key = new StringBuilder(AppFactoryConstants.DEPLOYMENT_STAGES).append(".").
                append(stage).append(".").append(AppFactoryConstants.DEPLOYMENT_URL).toString();
        String[] deploymentServerUrls = appFactoryConfiguration.getProperties(key);

        if (deploymentServerUrls.length == 0) {
            handleException("No deployment paths are configured for stage:" + stage);
        }

        String checkoutPath = checkoutApplication(applicationSvnUrl, applicationId);
        try {
            buildApplication(checkoutPath);

            File targetArtifacts = new File(checkoutPath + File.separator + "target");

            String[] fileExtension = {"car"};
            List<File> artifactFiles = (List<File>) FileUtils.listFiles(targetArtifacts, fileExtension, false);

            List<Application> applications = new ArrayList<Application>();

            for (File deployArtifact : artifactFiles) {
                applications.add(new Application(deployArtifact.getName(), FileUtils.sizeOf(deployArtifact)));

                // upload artifact to given deployment servers
                for (String deploymentServerUrl : deploymentServerUrls) {
                    uploadArtifact(applicationId, deployArtifact, deploymentServerUrl);
                }
            }

            return applications.toArray(new Application[applications.size()]);
        } finally {
            cleanApplicationDir(checkoutPath);
        }
    }

    private void uploadArtifact(String applicationId, File deployArtifact,
                                String deploymentServerUrl) throws ApplicationDeploymentExceptions {
        ApplicationUploadClient applicationUploadClient = new ApplicationUploadClient(deploymentServerUrl);

        UploadedFileItem uploadedFileItem = new UploadedFileItem();

        DataHandler dataHandler = new DataHandler(new FileDataSource(deployArtifact));
        uploadedFileItem.setDataHandler(dataHandler);
        uploadedFileItem.setFileName(deployArtifact.getName());
        uploadedFileItem.setFileType("jar");

        UploadedFileItem[] uploadedFileItems = {uploadedFileItem};

        String remoteIp = null;
        try {
            URL deploymentURL = new URL(deploymentServerUrl);
            remoteIp = deploymentURL.getHost();
        } catch (MalformedURLException e) {
            handleException("Deployment server url is malformed.");
        }

        try {
            if (applicationUploadClient.authenticate(getAdminUsername(applicationId),
                                                     appFactoryConfiguration.getFirstProperty(
                                                             AppFactoryConstants.SERVER_ADMIN_PASSWORD), remoteIp)) {
                applicationUploadClient.uploadCarbonApp(uploadedFileItems);
                log.info(deployArtifact.getName() + " is successfully uploaded.");
            } else {
                handleException("Failed to login to " + remoteIp + " to deploy artifact:" + deployArtifact.getName());
            }
        } catch (Exception e) {
            handleException("Failed to upload the artifact:" + deployArtifact + " of application:" +
                            applicationId + " to deployment location:" + deploymentServerUrl);
        }
    }


    private boolean executeMavenGoal(String applicationPath)
            throws ApplicationDeploymentExceptions {
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

    private void cleanApplicationDir(String applicationPath) {
        File application = new File(applicationPath);
        try {
            FileUtils.deleteDirectory(application);
        } catch (IOException ignore) {
            log.warn("Failed to clean up application at path:" + applicationPath);
        }
    }

    private String getAdminUsername(String applicationId) {
        return appFactoryConfiguration.getFirstProperty(
                AppFactoryConstants.SERVER_ADMIN_NAME) + "@" + applicationId;
    }

}