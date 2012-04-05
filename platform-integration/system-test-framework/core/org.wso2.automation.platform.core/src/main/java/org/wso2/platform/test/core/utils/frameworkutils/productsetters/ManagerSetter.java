package org.wso2.platform.test.core.utils.frameworkutils.productsetters;

import org.wso2.platform.test.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.platform.test.core.utils.frameworkutils.EnvironmentSetter;
import org.wso2.platform.test.core.utils.frameworkutils.productvariables.ProductVariables;

public class ManagerSetter extends EnvironmentSetter {
    ProductVariables productVariables = new ProductVariables();
    public ProductVariables getProductVariables() {
        String hostName = null;
        ProductUrlGeneratorUtil productUrlGeneratorUtil = new ProductUrlGeneratorUtil();
        prop = productUrlGeneratorUtil.getStream();
        if (!Boolean.parseBoolean(prop.getProperty("stratos.test"))) {
            hostName = (prop.getProperty("manager.host.name", null));
        } else {
            hostName = prop.getProperty("manager.service.host.name", "stratoslive.wso2.com");
        }
        String httpPort = (prop.getProperty("manager.http.port", "9763"));
        String httpsPort = (prop.getProperty("manager.https.port", "9443"));
        String webContextRoot = (prop.getProperty("manager.webContext.root", null));
        productVariables.setProductVariables(hostName, httpPort, httpsPort, webContextRoot, productUrlGeneratorUtil.getBackendUrl(httpsPort,hostName,webContextRoot));
        return productVariables;
    }
}
