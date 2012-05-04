package org.wso2.platform.test.core.utils.frameworkutils.productsetters;

import org.wso2.platform.test.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.platform.test.core.utils.frameworkutils.EnvironmentSetter;
import org.wso2.platform.test.core.utils.frameworkutils.productvariables.ProductVariables;

public class GsSetter  extends EnvironmentSetter {
    ProductVariables productVariables = new ProductVariables();
    public ProductVariables getProductVariables() {
        String hostName = null;
        ProductUrlGeneratorUtil productUrlGeneratorUtil = new ProductUrlGeneratorUtil();
        prop = productUrlGeneratorUtil.getStream();
        if (!Boolean.parseBoolean(prop.getProperty("stratos.test"))) {
            hostName = (prop.getProperty("gs.host.name", "localhost"));
        } else {
            hostName = prop.getProperty("gs.service.host.name", "gadget.stratoslive.wso2.com");
        }
        String httpPort = (prop.getProperty("gs.http.port", "9763"));
        String httpsPort = (prop.getProperty("gs.https.port", "9443"));
        String webContextRoot = (prop.getProperty("gs.webContext.root", null));
        productVariables.setProductVariables(hostName, httpPort, httpsPort, webContextRoot, productUrlGeneratorUtil.getBackendUrl(httpsPort,hostName,webContextRoot));
        return productVariables;
    }
}