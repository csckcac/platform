package org.wso2.carbon.automation.core.utils.frameworkutils.productsetters;

import org.wso2.carbon.automation.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.carbon.automation.core.utils.frameworkutils.EnvironmentSetter;
import org.wso2.carbon.automation.core.utils.frameworkutils.productvariables.ProductVariables;

public class MbSetter extends EnvironmentSetter {
    ProductVariables productVariables = new ProductVariables();

    public ProductVariables getProductVariables() {
        String hostName = null;
        ProductUrlGeneratorUtil productUrlGeneratorUtil = new ProductUrlGeneratorUtil();
        prop = productUrlGeneratorUtil.getStream();
        if (!Boolean.parseBoolean(prop.getProperty("stratos.test"))) {
            hostName = (prop.getProperty("mb.host.name", "localhost"));
        } else {
            hostName = prop.getProperty("mb.service.host.name", "messaging.stratoslive.wso2.com");
        }
        String httpPort = (prop.getProperty("mb.http.port", "9763"));
        String httpsPort = (prop.getProperty("mb.https.port", "9443"));
        String webContextRoot = (prop.getProperty("mb.webContext.root", null));
        String qpidPort = prop.getProperty("mb.qpid.port", "5672");
        productVariables.setProductVariables(hostName, httpPort, httpsPort, webContextRoot, qpidPort, productUrlGeneratorUtil.getBackendUrl(httpsPort, hostName, webContextRoot));
        return productVariables;
    }
}
