package org.wso2.carbon.appfactory.maven.build;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.BuildDriver;
import org.wso2.carbon.appfactory.core.BuildDriverListener;

public class Maven2BuildDriver implements BuildDriver {

	private static final Log log = LogFactory.getLog(Maven2BuildDriver.class);
	
	@Override
	public void buildArtifact(String applicationId, String version, String revision,
	                          BuildDriverListener listener) throws AppFactoryException {

		File workDir = AppFactoryUtil.getApplicationWorkDirectory(applicationId, version, revision);
		String pomFilePath = workDir.getAbsolutePath() + File.separator + "pom.xml";
		File pomFile = new File(pomFilePath);
		if (!pomFile.exists()) {
			handleException("pom.xml file not found at " + pomFilePath);
		}
		executeMavenGoal(workDir.getAbsolutePath());
		String targetDirPath = workDir.getAbsolutePath() + File.separator + "target";
		File targetDir = new File(targetDirPath);
		if (!targetDir.exists()) {
			handleException("Application build failure.");
			listener.onBuildFailure(applicationId, version, revision, targetDir);
		}
		listener.onBuildSuccessful(applicationId, version, revision, targetDir);
	}

	// TODO : Run in a thread pool
	private void executeMavenGoal(String applicationPath) throws AppFactoryException {
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
			// TODO: need to get the build error, exception back to the user.
			if (result.getExecutionException() == null) {
				if (result.getExitCode() != 0) {
					request.setOffline(true);
					result = invoker.execute(request);
					if (result.getExitCode() == 0) {
					} else {
						final String errorMessage =
						                            "No maven Application found at " +
						                                    applicationPath;
						handleException(errorMessage);
					}
				}
			}
		} catch (MavenInvocationException e) {
			handleException("Maven invocation failed with error:" + e.getLocalizedMessage(), e);
		}
	}

	private void handleException(String msg, Exception e) throws AppFactoryException {
		log.error(msg, e);
		throw new AppFactoryException(msg, e);
	}

	private void handleException(String msg) throws AppFactoryException {
		log.error(msg);
		throw new AppFactoryException(msg);
	}

}
