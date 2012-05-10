package org.wso2.carbon.artifact.deployment.service;

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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class ProjectDeploymentImpl implements ProjectDeployingService {
    private static final Log log = LogFactory.getLog(ProjectDeploymentImpl.class);
    private static final String tempDirPath = "TEMP_DIR";
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
     * @param svnBaseUrl - base svn url location
     * @param projectId  - svn project name, this name is used with base url to construct the svn path
     * @return project check out path.
     * @throws ProjectDeploymentExceptions
     */
    private String checkoutProject(String svnBaseUrl, String projectId)
            throws ProjectDeploymentExceptions {
        File root = createProjectCheckoutDirectory(projectId);
        initSVNClient();

        SVNUrl svnUrl = null;
        try {
            svnUrl = new SVNUrl(svnBaseUrl + "/" + projectId);
        } catch (MalformedURLException e) {
            handleException("SVN URL of project is malformed.", e);
        }

        try {
            if (svnClient instanceof CmdLineClientAdapter) {
                // CmdLineClientAdapter does not support all the options
                svnClient.checkout(svnUrl, root, SVNRevision.HEAD, true);
            } else {
                svnClient.checkout(svnUrl, root, SVNRevision.HEAD,
                                   Depth.infinity, true, true);
            }
        } catch (SVNClientException e) {
            handleException("Failed to checkout code from SVN URL:" + svnUrl, e);
        }
        return root.getAbsolutePath();
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


    public Artifact[] deployProject(String projectId, String artifactType,
                                    String stage) throws ProjectDeploymentExceptions {
        ProjectDeploymentConfiguration configuration = ProjectDeploymentConfigBuilder.createDeploymentConfiguration();

        List<String> deploymentPaths = configuration.getDeploymentServerLocations(stage);

        if (deploymentPaths.isEmpty()) {
            handleException("No deployment paths are configured for stage:" + stage);
        }

        if (configuration.getSvnBaseURL() == null) {
            handleException("Base svn path is not configured.");
        }

        String checkoutPath = checkoutProject(configuration.getSvnBaseURL(), projectId);
        try {
            buildProject(checkoutPath);

            File targetArtifacts = new File(checkoutPath + File.separator + "target");

            createDeploymentDirs(deploymentPaths);

            String[] fileExtension = {artifactType.toLowerCase()};
            List<File> artifactFiles = (List<File>) FileUtils.listFiles(targetArtifacts, fileExtension, false);

            List<Artifact> artifacts = new ArrayList<Artifact>();

            for (File deployArtifact : artifactFiles) {
                artifacts.add(new Artifact(deployArtifact.getName(), FileUtils.sizeOf(deployArtifact)));
                for (String deploymentPath : deploymentPaths) {
                    File deployDir = new File(deploymentPath);
                    try {
                        FileUtils.copyFileToDirectory(deployArtifact, deployDir);
                    } catch (IOException e) {
                        handleException("Failed to copy artifact:" + deployArtifact.getName() + " to deploy directory:" + deploymentPath);
                    }
                }
            }

            return artifacts.toArray(new Artifact[artifacts.size()]);
        } finally {
            cleanProjectDir(checkoutPath);
        }
    }

    private void createDeploymentDirs(List<String> paths) throws ProjectDeploymentExceptions {
        for (String path : paths) {
            File file = new File(path);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    handleException("Failed to create directory structure:" + path + " to deploy artifacts");
                }
            }
        }
    }

    private boolean executeMavenGoal(String projectPath) throws ProjectDeploymentExceptions {
        if (System.getProperty("maven.home") == null) {
            throw new ProjectDeploymentExceptions("System property 'maven.home' is not set.");
        }
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

}