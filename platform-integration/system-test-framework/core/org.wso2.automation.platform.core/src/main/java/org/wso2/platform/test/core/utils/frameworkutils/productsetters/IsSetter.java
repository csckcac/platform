package org.wso2.platform.test.core.utils.frameworkutils.productsetters;

import org.wso2.platform.test.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.platform.test.core.utils.frameworkutils.EnvironmentSetter;
import org.wso2.platform.test.core.utils.frameworkutils.productvariables.ProductVariables;

public class IsSetter extends EnvironmentSetter {
    ProductVariables productVariables = new ProductVariables();
    public ProductVariables getProductVariables() {
        String hostName = null;
        ProductUrlGeneratorUtil productUrlGeneratorUtil = new ProductUrlGeneratorUtil();
        prop = productUrlGeneratorUtil.getStream();
        if (!Boolean.parseBoolean(prop.getProperty("stratos.test"))) {
            hostName = (prop.getProperty("is.host.name", "localhost"));
        } else {
            hostName = prop.getProperty("is.service.host.name", "identity.stratoslive.wso2.com");
        }
        String httpPort = (prop.getProperty("is.http.port", "9763"));
        String httpsPort = (prop.getProperty("is.https.port", "9443"));
        String webContextRoot = (prop.getProperty("is.webContext.root", null));
        productVariables.setProductVariables(hostName, httpPort, httpsPort, webContextRoot, productUrlGeneratorUtil.getBackendUrl(httpsPort,hostName,webContextRoot));
        return productVariables;
    }
}
