package org.wso2.platform.test.core.utils.frameworkutils.productsetters;


import org.wso2.platform.test.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.platform.test.core.utils.frameworkutils.EnvironmentSetter;
import org.wso2.platform.test.core.utils.frameworkutils.productvariables.ProductVariables;

public class BpsSetter extends EnvironmentSetter {
    ProductVariables productVariables = new ProductVariables();
    public ProductVariables getProductVariables() {
        String hostName = null;
        ProductUrlGeneratorUtil productUrlGeneratorUtil = new ProductUrlGeneratorUtil();
        prop = productUrlGeneratorUtil.getStream();
        if (!Boolean.parseBoolean(prop.getProperty("stratos.test"))) {
            hostName = (prop.getProperty("bps.host.name", "localhost"));
        } else {

            hostName = prop.getProperty("bps.service.host.name", "process.stratoslive.wso2.com");
        }
        String httpPort = (prop.getProperty("bps.http.port", "9763"));
        String httpsPort = (prop.getProperty("bps.https.port", "9400"));
        String webContextRoot = (prop.getProperty("bps.webContext.root", null));

        productVariables.setProductVariables(hostName, httpPort, httpsPort, webContextRoot,
                                             productUrlGeneratorUtil.getBackendUrl(httpsPort,hostName,webContextRoot));
        return productVariables;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
