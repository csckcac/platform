package org.wso2.platform.test.core.utils.frameworkutils.productsetters;

import org.wso2.platform.test.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.platform.test.core.utils.frameworkutils.EnvironmentSetter;
import org.wso2.platform.test.core.utils.frameworkutils.productvariables.ProductVariables;

public class EsbSetter extends EnvironmentSetter {
    ProductVariables productVariables = new ProductVariables();

    public ProductVariables getProductVariables() {
        String hostName = null;
        String nHttpPort = null;
        String nHttpsPort = null;
        ProductUrlGeneratorUtil productUrlGeneratorUtil = new ProductUrlGeneratorUtil();
        prop = productUrlGeneratorUtil.getStream();
        if (!Boolean.parseBoolean(prop.getProperty("stratos.test"))) {
            hostName = (prop.getProperty("esb.host.name", "localhost"));
            nHttpPort = (prop.getProperty("esb.nhttp.port", "8280"));
            nHttpsPort = (prop.getProperty("esb.nhttps.port", "8243"));
        } else {
            hostName = prop.getProperty("esb.service.host.name", "esb.stratoslive.wso2.com");
            nHttpPort = (prop.getProperty("esb.nhttp.port", "8280"));
            nHttpsPort = (prop.getProperty("esb.nhttps.port", "8243"));
        }
        String httpPort = (prop.getProperty("esb.http.port", "9765"));
        String httpsPort = (prop.getProperty("esb.https.port", "9445"));
        String webContextRoot = (prop.getProperty("esb.webContext.root", null));
        productVariables.setProductVariables(hostName, httpPort, httpsPort, webContextRoot,
                                             nHttpPort, nHttpsPort,
                                             productUrlGeneratorUtil.getBackendUrl(httpsPort, hostName, webContextRoot));
        return productVariables;
    }

}
