package org.wso2.carbon.application.deployer.brs.internal;

import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.application.deployer.AppDeployerConstants;
import org.wso2.carbon.application.deployer.AppDeployerUtils;
import org.wso2.carbon.application.deployer.Feature;
import org.wso2.carbon.application.deployer.brs.BRSAppDeployer;
import org.wso2.carbon.application.deployer.brs.BRSAppUndeployer;
import org.wso2.carbon.application.deployer.service.ApplicationManagerService;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

/**
 * @scr.component name="application.deployer.brs" immediate="true"
 * @scr.reference name="application.manager"
 * interface="org.wso2.carbon.application.deployer.service.ApplicationManagerService"
 * cardinality="1..1" policy="dynamic" bind="setAppManager" unbind="unsetAppManager"
 */
public class BRSAppDeployerDSComponent {

    private static Log log = LogFactory.getLog(BRSAppDeployerDSComponent.class);

       private static ApplicationManagerService applicationManager;
       private static HashMap<String, List<Feature>> requiredFeatures;

       private BRSAppDeployer brsDeployer = null;
       private BRSAppUndeployer brsUndeployer = null;

       protected void activate(ComponentContext ctxt) {
           try {
               //register brs deployer and undeployer in the ApplicationManager
               brsDeployer = new BRSAppDeployer();
               brsUndeployer = new BRSAppUndeployer();
               applicationManager.registerDeploymentHandler(brsDeployer);
               applicationManager.registerUndeploymentHandler(brsUndeployer);

               // read required-features.xml
               URL reqFeaturesResource = ctxt.getBundleContext().getBundle()
                       .getResource(AppDeployerConstants.REQ_FEATURES_XML);
               if (reqFeaturesResource != null) {
                   InputStream xmlStream = reqFeaturesResource.openStream();
                   requiredFeatures = AppDeployerUtils
                           .readRequiredFeaturs(new StAXOMBuilder(xmlStream).getDocumentElement());
               }
           } catch (Throwable e) {
               log.error("Failed to activate BRS Application Deployer", e);
           }
       }

       protected void deactivate(ComponentContext ctxt) {
           //unregister deployer and undeployer in the ApplicationManager
           applicationManager.unregisterDeploymentHandler(brsDeployer);
           applicationManager.unregisterUndeploymentHandler(brsUndeployer);
       }

       protected void setAppManager(ApplicationManagerService appManager) {
           applicationManager = appManager;
       }

       protected void unsetAppManager(ApplicationManagerService appManager) {
           applicationManager = null;
       }

       public static HashMap<String, List<Feature>> getRequiredFeatures() {
           return requiredFeatures;
       }

}
