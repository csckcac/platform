package org.wso2.carbon.automation.core.utils.frameworkutils.productsetters;

import org.wso2.carbon.automation.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.carbon.automation.core.utils.frameworkutils.EnvironmentSetter;
import org.wso2.carbon.automation.core.utils.frameworkutils.productvariables.ProductVariables;

public class BamSetter extends EnvironmentSetter {
    ProductVariables productVariables = new ProductVariables();

    public ProductVariables getProductVariables() {
        String hostName = null;
        ProductUrlGeneratorUtil productUrlGeneratorUtil = new ProductUrlGeneratorUtil();
        prop = productUrlGeneratorUtil.getStream();
        if (!Boolean.parseBoolean(prop.getProperty("stratos.test"))) {
            hostName = (prop.getProperty("bam.host.name", "localhost"));
        } else {
            hostName = prop.getProperty("bam.service.host.name", "monitor.stratoslive.wso2.com");
        }
        String httpPort = (prop.getProperty("bam.http.port", "9770"));
        String httpsPort = (prop.getProperty("bam.https.port", "9450"));
        String webContextRoot = (prop.getProperty("bam.webContext.root", null));
        productVariables.setProductVariables(hostName, httpPort, httpsPort, webContextRoot, productUrlGeneratorUtil.getBackendUrl(httpsPort, hostName, webContextRoot));
        return productVariables;
    }
}
