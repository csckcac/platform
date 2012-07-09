package org.wso2.carbon.automation.core.utils.frameworkutils.productsetters;

import org.wso2.carbon.automation.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.carbon.automation.core.utils.frameworkutils.EnvironmentSetter;
import org.wso2.carbon.automation.core.utils.frameworkutils.productvariables.ProductVariables;

public class BrsSetter extends EnvironmentSetter {
    ProductVariables productVariables = new ProductVariables();

    public ProductVariables getProductVariables() {
        String hostName = null;
        ProductUrlGeneratorUtil productUrlGeneratorUtil = new ProductUrlGeneratorUtil();
        prop = productUrlGeneratorUtil.getStream();
        if (!Boolean.parseBoolean(prop.getProperty("stratos.test"))) {
            hostName = (prop.getProperty("brs.host.name", "localhost"));
        } else {
            hostName = prop.getProperty("brs.service.host.name", "rule.stratoslive.wso2.com");
        }
        String httpPort = (prop.getProperty("brs.http.port", "9770"));
        String httpsPort = (prop.getProperty("brs.https.port", "9450"));
        String webContextRoot = (prop.getProperty("brs.webContext.root", null));
        productVariables.setProductVariables(hostName, httpPort, httpsPort, webContextRoot,
                                             productUrlGeneratorUtil.getBackendUrl
                                                     (httpsPort, hostName, webContextRoot));
        return productVariables;
    }
}
