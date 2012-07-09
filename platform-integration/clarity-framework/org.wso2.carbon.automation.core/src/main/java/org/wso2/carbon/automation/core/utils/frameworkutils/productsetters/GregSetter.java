package org.wso2.carbon.automation.core.utils.frameworkutils.productsetters;

import org.wso2.carbon.automation.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.carbon.automation.core.utils.frameworkutils.EnvironmentSetter;
import org.wso2.carbon.automation.core.utils.frameworkutils.productvariables.ProductVariables;

public class GregSetter extends EnvironmentSetter {
    ProductVariables productVariables = new ProductVariables();

    public ProductVariables getProductVariables() {
        String hostName = null;
        ProductUrlGeneratorUtil productUrlGeneratorUtil = new ProductUrlGeneratorUtil();
        prop = productUrlGeneratorUtil.getStream();
        if (!Boolean.parseBoolean(prop.getProperty("stratos.test"))) {
            hostName = (prop.getProperty("greg.host.name", "localhost"));
        } else {
            hostName = prop.getProperty("greg.service.host.name", "governance.stratoslive.wso2.com");
        }
        String httpPort = (prop.getProperty("greg.http.port", "9763"));
        String httpsPort = (prop.getProperty("greg.https.port", "9400"));
        String webContextRoot = (prop.getProperty("greg.webContext.root", null));
        productVariables.setProductVariables(hostName, httpPort, httpsPort, webContextRoot, productUrlGeneratorUtil.getBackendUrl(httpsPort, hostName, webContextRoot));
        return productVariables;
    }
}
