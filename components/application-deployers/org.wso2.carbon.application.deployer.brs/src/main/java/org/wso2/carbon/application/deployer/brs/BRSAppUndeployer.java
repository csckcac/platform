package org.wso2.carbon.application.deployer.brs;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.application.deployer.CarbonApplication;
import org.wso2.carbon.application.deployer.config.Artifact;
import org.wso2.carbon.application.deployer.config.CappFile;
import org.wso2.carbon.application.deployer.handler.AppUndeploymentHandler;
import org.wso2.carbon.utils.ArchiveManipulator;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class BRSAppUndeployer implements AppUndeploymentHandler {
    private static final Log log = LogFactory.getLog(BRSAppUndeployer.class);

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
                   log.error(
                           "A BRS must have a single file. But " + files.size() + " files found.");
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

}
