package org.wso2.carbon.automation.core.utils.frameworkutils.productsetters;

import org.wso2.carbon.automation.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.carbon.automation.core.utils.frameworkutils.EnvironmentSetter;
import org.wso2.carbon.automation.core.utils.frameworkutils.productvariables.ProductVariables;

public class MsSetter extends EnvironmentSetter {
    ProductVariables productVariables = new ProductVariables();

    public ProductVariables getProductVariables() {
        String hostName = null;
        ProductUrlGeneratorUtil productUrlGeneratorUtil = new ProductUrlGeneratorUtil();
        prop = productUrlGeneratorUtil.getStream();
        if (!Boolean.parseBoolean(prop.getProperty("stratos.test"))) {
            hostName = (prop.getProperty("ms.host.name", "localhost"));
        } else {
            hostName = prop.getProperty("ms.service.host.name", "mashup.stratoslive.wso2.com");
        }
        String httpPort = (prop.getProperty("ms.http.port", "9763"));
        String httpsPort = (prop.getProperty("ms.https.port", "9443"));
        String webContextRoot = (prop.getProperty("ms.webContext.root", null));
        productVariables.setProductVariables(hostName, httpPort, httpsPort, webContextRoot, productUrlGeneratorUtil.getBackendUrl(httpsPort, hostName, webContextRoot));
        return productVariables;
    }
}