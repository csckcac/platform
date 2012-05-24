package org.wso2.carbon.application.deployer.brs;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.application.deployer.AppDeployerUtils;
import org.wso2.carbon.application.deployer.CarbonApplication;
import org.wso2.carbon.application.deployer.brs.internal.BRSAppDeployerDSComponent;
import org.wso2.carbon.application.deployer.config.Artifact;
import org.wso2.carbon.application.deployer.config.CappFile;
import org.wso2.carbon.application.deployer.handler.AppDeploymentHandler;
import org.wso2.carbon.utils.ArchiveManipulator;
import org.wso2.carbon.utils.FileManipulator;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;


public class BRSAppDeployer implements AppDeploymentHandler {

    private static final Log log = LogFactory.getLog(BRSAppDeployer.class);

    public static final String BRS_TYPE = "service/rule";
    public static final String BRS_DIR = "ruleservices";

    private Map<String, Boolean> acceptanceList = null;

    /**
     * Check the artifact type and if it is a Rule, call the Rule Axis2 deployer
     *
     * @param carbonApp  - CarbonApplication instance to check for BRS artifacts
     * @param axisConfig - AxisConfiguration of the current tenant
     */
    public void deployArtifacts(CarbonApplication carbonApp, AxisConfiguration axisConfig) {
        List<Artifact.Dependency> artifacts =
                carbonApp.getAppConfig().getApplicationArtifact().getDependencies();

        String repo = axisConfig.getRepository().getPath();

        String artifactPath, destPath;
        for (Artifact.Dependency dep : artifacts) {
            Artifact artifact = dep.getArtifact();
            if (artifact == null) {
                continue;
            }

            String artifactName = artifact.getName();
            if (!isAccepted(artifact.getType())) {
                log.warn("Can't deploy artifact : " + artifactName + " of type : " +
                        artifact.getType() + ". Required features are not installed in the system");
                continue;
            }

            if (BRS_TYPE.equals(artifact.getType())) {
                destPath = repo + BRS_DIR;
            } else {
                continue;
            }

            List<CappFile> files = artifact.getFiles();
            if (files.size() != 1) {
                log.error("BRS must have a single file to " + "be deployed. But " + files.size() +
                        " files found.");
                continue;
            }
            String fileName = artifact.getFiles().get(0).getName();
            artifactPath = artifact.getExtractedPath() + File.separator + fileName;
            try {
                FileManipulator.copyFileToDir(new File(artifactPath), new File(destPath));
            } catch (IOException e) {
                log.error("Unable to copy the BRS : " + artifactName, e);
            }
        }
    }

    /**
     * Check the artifact type and if it is a Gadget, delete the file from the Gadget deployment hot
     * folder
     *
     * @param carbonApp  - CarbonApplication instance to check for Gadget artifacts
     * @param axisConfig - AxisConfiguration of the current tenant
     */
    public void undeployArtifacts(CarbonApplication carbonApp, AxisConfiguration axisConfig) {

        List<Artifact.Dependency> artifacts =
                carbonApp.getAppConfig().getApplicationArtifact().getDependencies();
        ArchiveManipulator archiveManipulator = new ArchiveManipulator();

        String repo = axisConfig.getRepository().getPath();
        String artifactPath, destPath;
        for (Artifact.Dependency dep : artifacts) {
            Artifact artifact = dep.getArtifact();
            if (artifact == null) {
                continue;
            }
            if (BRSAppDeployer.BRS_TYPE.equals(artifact.getType())) {
                destPath = repo + File.separator + BRSAppDeployer.BRS_DIR;
            } else {
                continue;
            }

            List<CappFile> files = artifact.getFiles();
            if (files.size() != 1) {
                log.error("A BRS must have a single file. But " + files.size() + " files found.");
                continue;
            }
            String fileName = artifact.getFiles().get(0).getName();
            artifactPath = artifact.getExtractedPath() + File.separator + fileName;
            File artifactInRepo;
            try {
                String[] filesInZip = archiveManipulator.check(artifactPath);
                File jsFile = null;
                for (String file : filesInZip) {
                    String artifactRepoPath = destPath + File.separator + file;
                    if (file.indexOf("/") == -1) {
                        String extension = file.substring(file.indexOf(".") + 1);
                        if ("js".equals(extension)) {
                            jsFile = new File(destPath + File.separator + file);
                        } else {
                            artifactInRepo = new File(artifactRepoPath);
                            if (artifactInRepo.exists() && artifactInRepo.delete()) {
                                log.warn("Couldn't delete BRS artifact file : " + artifactPath);
                            }
                        }
                    }
                }
                if (jsFile != null && jsFile.exists() && !jsFile.delete()) {
                    log.warn("Couldn't delete BRS artifact file : " + artifactPath);
                }
            } catch (IOException e) {
                log.error("Error reading the content of the artifact : " + artifact.getName(), e);
            }
        }
    }

    /**
     * Check whether a particular artifact type can be accepted for deployment. If the type doesn't
     * exist in the acceptance list, we assume that it doesn't require any special features to be
     * installed in the system. Therefore, that type is accepted. If the type exists in the
     * acceptance list, the acceptance value is returned.
     *
     * @param serviceType - service type to be checked
     * @return true if all features are there or entry is null. else false
     */
    private boolean isAccepted(String serviceType) {
        if (acceptanceList == null) {
            acceptanceList = AppDeployerUtils
                    .buildAcceptanceList(BRSAppDeployerDSComponent.getRequiredFeatures());
        }
        Boolean acceptance = acceptanceList.get(serviceType);
        return (acceptance == null || acceptance);
    }

}
