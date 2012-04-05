package org.wso2.platform.test.core.utils.frameworkutils.productsetters;

import org.wso2.platform.test.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.platform.test.core.utils.frameworkutils.EnvironmentSetter;
import org.wso2.platform.test.core.utils.frameworkutils.productvariables.ProductVariables;

public class DssSetter extends EnvironmentSetter {
    ProductVariables productVariables = new ProductVariables();
    public ProductVariables getProductVariables() {
        String hostName = null;
        ProductUrlGeneratorUtil productUrlGeneratorUtil = new ProductUrlGeneratorUtil();
        prop = productUrlGeneratorUtil.getStream();
        if (!Boolean.parseBoolean(prop.getProperty("stratos.test"))) {
            hostName = (prop.getProperty("dss.host.name", "localhost"));
        } else {
            hostName = prop.getProperty("dss.service.host.name", "data.stratoslive.wso2.com");
        }
        String httpPort = (prop.getProperty("dss.http.port", "9763"));
        String httpsPort = (prop.getProperty("dss.https.port", "9443"));
        String webContextRoot = (prop.getProperty("dss.webContext.root", null));
        productVariables.setProductVariables(hostName, httpPort, httpsPort, webContextRoot, productUrlGeneratorUtil.getBackendUrl(httpsPort,hostName,webContextRoot));
        return productVariables;
    }
}
