package org.wso2.carbon.appfactory.artifact.deployment.service;

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
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.application.mgt.stub.upload.types.carbon.UploadedFileItem;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class ProjectDeploymentImpl implements ProjectDeployingService {
    private static final Log log = LogFactory.getLog(ProjectDeploymentImpl.class);
    private static String tempDirPath = System.getProperty("java.io.tmpdir").endsWith(
            File.separator) ? System.getProperty("java.io.tmpdir") + "checkOutCode" :
                                        System.getProperty("java.io.tmpdir") + File.separator + "checkOutCode";
    private ISVNClientAdapter svnClient;


    private void initSVNClient() throws ProjectDeploymentExceptions {
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
//            svnClient.setUsername();
        } catch (SVNClientException e) {
            throw new ProjectDeploymentExceptions("Client type can not be defined.");
        }

        if (svnClient == null) {
            throw new ProjectDeploymentExceptions("Failed to instantiate svn client.");
        }
    }

    /**
     * Check out from given url
     *
     * @param projectSvnUrl - project svn url location
     * @param projectId     - project id
     * @return project check out path.
     * @throws ProjectDeploymentExceptions
     */
    private String checkoutProject(String projectSvnUrl, String projectId)
            throws ProjectDeploymentExceptions {
        File checkoutDirectory = createProjectCheckoutDirectory(projectId);
        initSVNClient();

        SVNUrl svnUrl = null;
        try {
            svnUrl = new SVNUrl(projectSvnUrl);
        } catch (MalformedURLException e) {
            handleException("SVN URL of project is malformed.", e);
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
     * Build the project from given location
     *
     * @param sourcePath - source location
     * @throws ProjectDeploymentExceptions
     */
    private void buildProject(String sourcePath) throws ProjectDeploymentExceptions {
        String pomFilePath = sourcePath + File.separator + "pom.xml";
        File pomFile = new File(pomFilePath);
        if (!pomFile.exists()) {
            handleException("pom.xml file not found at " + pomFilePath);
        }
        executeMavenGoal(sourcePath);
        String targetDirPath = sourcePath + File.separator + "target";
        File targetDir = new File(targetDirPath);
        if (!targetDir.exists()) {
            handleException("Project build failure.");
        }
    }

    private File createProjectCheckoutDirectory(String projectName)
            throws ProjectDeploymentExceptions {
        File tempDir = new File(tempDirPath + File.separator + projectName);
        if (!tempDir.exists()) {
            boolean directoriesCreated = tempDir.mkdirs();
            if (!directoriesCreated) {
                handleException("Failed to create directory path:" + tempDir.getAbsolutePath());
            }
        }
        return tempDir;
    }


    private void handleException(String msg) throws ProjectDeploymentExceptions {
        log.error(msg);
        throw new ProjectDeploymentExceptions(msg);
    }

    private void handleException(String msg, Exception e) throws ProjectDeploymentExceptions {
        log.error(msg, e);
        throw new ProjectDeploymentExceptions(msg, e);
    }


    public Artifact[] deployProject(String projectSvnUrl, String projectId, String artifactType,
                                    String stage) throws ProjectDeploymentExceptions {
        AppFactoryConfiguration configuration = AppFactoryConfigurationHolder.getInstance().getAppFactoryConfiguration();

        List<String> deploymentServerUrls = configuration.getDeploymentServerUrls(stage);

        if (deploymentServerUrls.isEmpty()) {
            handleException("No deployment paths are configured for stage:" + stage);
        }

        String checkoutPath = checkoutProject(projectSvnUrl, projectId);
        try {
            buildProject(checkoutPath);

            File targetArtifacts = new File(checkoutPath + File.separator + "target");

            String[] fileExtension = {artifactType.toLowerCase()};
            List<File> artifactFiles = (List<File>) FileUtils.listFiles(targetArtifacts, fileExtension, false);

            List<Artifact> artifacts = new ArrayList<Artifact>();

            for (File deployArtifact : artifactFiles) {
                artifacts.add(new Artifact(deployArtifact.getName(), FileUtils.sizeOf(deployArtifact)));

                // upload artifact to given deployment servers
                for (String deploymentServerUrl : deploymentServerUrls) {
                    uploadArtifact(projectId, deployArtifact, deploymentServerUrl);
                }
            }

            return artifacts.toArray(new Artifact[artifacts.size()]);
        } finally {
            cleanProjectDir(checkoutPath);
        }
    }

    private void uploadArtifact(String projectId, File deployArtifact,
                                String deploymentServerUrl) throws ProjectDeploymentExceptions {
        ArtifactUploadClient artifactUploadClient = new ArtifactUploadClient(deploymentServerUrl);

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
            if (artifactUploadClient.authenticate(getAdminUsername(projectId), getAdminPassword(), remoteIp)) {
                artifactUploadClient.uploadCarbonApp(uploadedFileItems);
            }
        } catch (Exception e) {
            handleException("Failed to upload the artifact:" + deployArtifact + " of project:" + projectId + " to deployment location:" + deploymentServerUrl);
        }
    }


    private boolean executeMavenGoal(String projectPath) throws ProjectDeploymentExceptions {
       /* if (System.getProperty("maven.home") == null) {
            throw new ProjectDeploymentExceptions("System property 'maven.home' is not set.");
        }*/
        InvocationRequest request = new DefaultInvocationRequest();
        request.setShowErrors(true);

        request.setPomFile(new File(projectPath + File.separator + "pom.xml"));

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
                        final String errorMessage = "No maven Project found at "
                                                    + projectPath;
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

    private void cleanProjectDir(String projectPath) {
        File project = new File(projectPath);
        try {
            FileUtils.deleteDirectory(project);
        } catch (IOException ignore) {
            log.warn("Failed to clean up project at path:" + projectPath);
        }
    }

    private String getAdminUsername(String projectId) {
        String adminUsername = AppFactoryConfigurationHolder.getInstance().getAppFactoryConfiguration().getAdminUserName();
        return adminUsername + "@" + projectId;
    }

    private String getAdminPassword() {
        return AppFactoryConfigurationHolder.getInstance().getAppFactoryConfiguration().getAdminPassword();
    }

}