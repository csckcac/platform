package org.wso2.carbon.automation.core.utils.frameworkutils.productsetters;

import org.wso2.carbon.automation.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.carbon.automation.core.utils.frameworkutils.EnvironmentSetter;
import org.wso2.carbon.automation.core.utils.frameworkutils.productvariables.ProductVariables;


public class AsSetter extends EnvironmentSetter {
    ProductVariables productVariables = new ProductVariables();

    public ProductVariables getProductVariables() {
        ProductUrlGeneratorUtil productUrlGeneratorUtil = new ProductUrlGeneratorUtil();
        prop = productUrlGeneratorUtil.getStream();

        String hostName = null;
        if (!Boolean.parseBoolean(prop.getProperty("stratos.test"))) {
            hostName = (prop.getProperty("as.host.name", "localhost"));
        } else {
            hostName = prop.getProperty("app.service.host.name", "appserver.stratoslive.wso2.com");
        }
        String httpPort = (prop.getProperty("as.http.port", "9764"));
        String httpsPort = (prop.getProperty("as.https.port", "9444"));
        String webContextRoot = (prop.getProperty("as.webContext.root", null));
        productVariables.setProductVariables
                (hostName, httpPort, httpsPort, webContextRoot,
                 productUrlGeneratorUtil.getBackendUrl(httpsPort, hostName, webContextRoot));
        return productVariables;
    }
}
