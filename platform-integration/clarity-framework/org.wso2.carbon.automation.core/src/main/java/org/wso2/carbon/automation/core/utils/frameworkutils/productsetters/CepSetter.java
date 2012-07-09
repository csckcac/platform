package org.wso2.carbon.automation.core.utils.frameworkutils.productsetters;

import org.wso2.carbon.automation.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.carbon.automation.core.utils.frameworkutils.EnvironmentSetter;
import org.wso2.carbon.automation.core.utils.frameworkutils.productvariables.ProductVariables;

public class CepSetter extends EnvironmentSetter {
    ProductVariables productVariables = new ProductVariables();

    public ProductVariables getProductVariables() {
        String hostName = null;
        ProductUrlGeneratorUtil productUrlGeneratorUtil = new ProductUrlGeneratorUtil();
        prop = productUrlGeneratorUtil.getStream();
        if (!Boolean.parseBoolean(prop.getProperty("stratos.test"))) {
            hostName = (prop.getProperty("cep.host.name", "localhost"));
        } else {
            hostName = prop.getProperty("cep.service.host.name", "process.stratoslive.wso2.com");
        }
        String httpPort = (prop.getProperty("cep.http.port", "9763"));
        String httpsPort = (prop.getProperty("cep.https.port", "9443"));
        String webContextRoot = (prop.getProperty("cep.webContext.root", null));
        String qpidPort = prop.getProperty("cep.qpid.port", "5672");
        productVariables.setProductVariables(hostName, httpPort, httpsPort, webContextRoot, qpidPort, productUrlGeneratorUtil.getBackendUrl(httpsPort, hostName, webContextRoot));
        return productVariables;
    }
}
