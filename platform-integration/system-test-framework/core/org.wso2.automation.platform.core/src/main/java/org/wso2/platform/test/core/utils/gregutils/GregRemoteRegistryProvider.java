package org.wso2.platform.test.core.utils.gregutils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkProperties;

import java.net.MalformedURLException;
import java.net.URL;


public class GregRemoteRegistryProvider {
    private static final Log log = LogFactory.getLog(GregRemoteRegistryProvider.class);
    public RemoteRegistry registry;

    public RemoteRegistry getRegistry(int userId)
            throws MalformedURLException, RegistryException {
        String registryURL;
        //tenant details
        UserInfo userDetails = UserListCsvReader.getUserInfo(userId);
        String username = userDetails.getUserName();
        String password = userDetails.getPassword();
        EnvironmentBuilder env = new EnvironmentBuilder();
        FrameworkProperties properties = FrameworkFactory.getFrameworkProperties(ProductConstant.GREG_SERVER_NAME);
        if (env.getFrameworkSettings().getEnvironmentSettings().is_runningOnStratos()) {
            registryURL = ProductUrlGeneratorUtil.getRemoteRegistryURLOfStratos(properties.getProductVariables().
                    getHttpsPort(), properties.getProductVariables().getHostName(), properties, userDetails);
        } else {
            registryURL = ProductUrlGeneratorUtil.getRemoteRegistryURLOfProducts(properties.getProductVariables().getHttpsPort(),
                                                                                 properties.getProductVariables().getHostName(),
                                                                                 properties.getProductVariables().getWebContextRoot());
        }

        log.info("Remote Registry URL" + registryURL);

        try {
            registry = new RemoteRegistry(new URL(registryURL), username, password);
        } catch (RegistryException e) {
            log.error("Registry API RegistryException thrown :" + e);
            throw new RegistryException("Registry API RegistryException thrown :" + e);
        } catch (MalformedURLException e) {
            log.error("Registry API MalformedURLException thrown :" + e);
            throw new MalformedURLException("Registry API MalformedURLException thrown :" + e);

        }
        return registry;
    }


}
