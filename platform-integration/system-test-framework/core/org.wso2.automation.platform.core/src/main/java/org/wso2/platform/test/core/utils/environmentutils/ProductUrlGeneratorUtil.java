package org.wso2.platform.test.core.utils.environmentutils;

import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ProductUrlGeneratorUtil {

    public static final Properties prop = new Properties();

    static {
        setStream();
    }

    public static Properties setStream() {
        try {
            InputStream inputStream = new FileInputStream
                    (ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + File.separator +
                     "framework.properties");
            if (inputStream != null) {
                prop.load(inputStream);
                return prop;
            }
            inputStream.close();
        } catch (IOException ignored) {
        }

        return null;
    }

    public Properties getStream() {
        return prop;
    }

    public String getHttpServiceURL(String httpPort, String hostName,
                                    FrameworkProperties frameworkProperties, UserInfo userInfo) {
        if (frameworkProperties.getEnvironmentSettings().is_runningOnStratos()) {
            return getHttpServiceURLOfStratos(httpPort, hostName, frameworkProperties, userInfo);
        } else {
            return getHttpServiceURLOfProduct(httpPort, hostName, frameworkProperties);
        }
    }

    private String getHttpServiceURLOfProduct(String httpPort, String hostName,
                                              FrameworkProperties frameworkProperties) {
        String serviceURL = null;
        boolean webContextEnabled = frameworkProperties.getEnvironmentSettings().isEnableCarbonWebContext();
        boolean portEnabled = frameworkProperties.getEnvironmentSettings().isEnablePort();
        String webContextRoot = frameworkProperties.getProductVariables().getWebContextRoot();

        if (portEnabled && webContextEnabled) {
            if (webContextRoot != null && httpPort != null) {
                serviceURL = "http://" + hostName + ":" + httpPort + "/" + webContextRoot + "/" + "services";
            } else if (webContextRoot == null && httpPort != null) {
                serviceURL = "http://" + hostName + ":" + httpPort + "/" + "services";
            } else if (webContextRoot == null) {
                serviceURL = "http://" + hostName + "/" + "services/";
            } else {
                serviceURL = "http://" + hostName + "/" + webContextRoot + "/" + "services";
            }
        } else if (!portEnabled && webContextEnabled) {
            serviceURL = "http://" + hostName + "/" + webContextRoot + "/" + "services";
        } else if (portEnabled && !webContextEnabled) {
            serviceURL = "http://" + hostName + ":" + httpPort + "/" + "services";
        } else {
            serviceURL = "http://" + hostName + "/" + "services";
        }
        return serviceURL;

    }

    private String getHttpServiceURLOfStratos(String httpPort, String hostName,
                                              FrameworkProperties frameworkProperties,
                                              UserInfo info) {
        String serviceURL = null;
        boolean webContextEnabled = frameworkProperties.getEnvironmentSettings().isEnableCarbonWebContext();
        boolean portEnabled = frameworkProperties.getEnvironmentSettings().isEnablePort();
        String webContextRoot = frameworkProperties.getProductVariables().getWebContextRoot();
        String superTenantID = "0";
        String tenantDomain;

        if (info.getUserId().equals(superTenantID)) { /*skip the domain if user is super admin */
            tenantDomain = null;
        } else {
            tenantDomain = info.getUserName().split("@")[1];
        }

        if (portEnabled && webContextEnabled) {
            if (webContextRoot != null && httpPort != null) {
                serviceURL = "http://" + hostName + ":" + httpPort + "/" + webContextRoot + "/" + "services/t/" + tenantDomain;
            } else if (webContextRoot == null && httpPort != null) {
                serviceURL = "http://" + hostName + ":" + httpPort + "/" + "services/t/" + tenantDomain;
            } else if (webContextRoot == null) {
                serviceURL = "http://" + hostName + "/" + "/services/t/" + tenantDomain;
            } else {
                serviceURL = "http://" + hostName + "/" + webContextRoot + "/" + "services/t/" + tenantDomain;
            }
        } else if (!portEnabled && webContextEnabled) {
            serviceURL = "http://" + hostName + "/" + webContextRoot + "/" + "services/t/" + tenantDomain;
        } else if (portEnabled && !webContextEnabled) {
            serviceURL = "http://" + hostName + ":" + httpPort + "/" + "services/t/" + tenantDomain;
        } else {
            serviceURL = "http://" + hostName + "/" + "services/t/" + tenantDomain;
        }
        return serviceURL;
    }


    public String getBackendUrl(String httpsPort, String hostName, String webContextRoot) {
        String backendUrl = null;
        boolean webContextEnabled = Boolean.parseBoolean(prop.getProperty("carbon.web.context.enable"));
        boolean portEnabled = Boolean.parseBoolean(prop.getProperty("port.enable"));

        if (portEnabled && webContextEnabled) {
            if (webContextRoot != null && httpsPort != null) {
                backendUrl = "https://" + hostName + ":" + httpsPort + "/" + webContextRoot + "/" + "services/";
            } else if (webContextRoot == null && httpsPort != null) {
                backendUrl = "https://" + hostName + ":" + httpsPort + "/" + "services/";
            } else if (webContextRoot == null) {
                backendUrl = "https://" + hostName + "/" + "services/";
            } else {
                backendUrl = "https://" + hostName + "/" + webContextRoot + "/" + "services/";
            }
        } else if (!portEnabled && webContextEnabled) {
            backendUrl = "https://" + hostName + "/" + webContextRoot + "/" + "services/";
        } else if (portEnabled && !webContextEnabled) {
            backendUrl = "https://" + hostName + ":" + httpsPort + "/" + "services/";
        } else {
            backendUrl = "https://" + hostName + "/" + "services/";
        }
        return backendUrl;
    }

    public String getWebappURL(String httpPort, String hostName,
                               FrameworkProperties frameworkProperties, UserInfo user) {
        String webAppURL = null;
        boolean portEnabled = frameworkProperties.getEnvironmentSettings().isEnablePort();

        if (frameworkProperties.getEnvironmentSettings().is_runningOnStratos()) {
            if (portEnabled && httpPort != null) {
                webAppURL = "http://" + hostName + ":" + httpPort + "/t/" + user.getUserName().split("@")[1] + "/webapps";
            } else {
                webAppURL = "http://" + hostName + "/t/" + user.getUserName().split("@")[1] + "/webapps";
            }
        } else {
            if (portEnabled && httpPort != null) {
                webAppURL = "http://" + hostName + ":" + httpPort;
            } else {
                webAppURL = "http://" + hostName;
            }
        }
        return webAppURL;
    }


    public String getServiceHomeURL(String productName) {
        String indexURL;
        FrameworkProperties properties = FrameworkFactory.getFrameworkProperties(productName);
        boolean webContextEnabled = Boolean.parseBoolean(prop.getProperty("carbon.web.context.enable"));
        boolean portEnabled = Boolean.parseBoolean(prop.getProperty("port.enable"));
        String webContextRoot = properties.getProductVariables().getWebContextRoot();
        String httpsPort = properties.getProductVariables().getHttpsPort();
        String hostName = properties.getProductVariables().getHostName();

        if (portEnabled && webContextEnabled) {
            if (webContextRoot != null && httpsPort != null) {
                indexURL = "https://" + hostName + ":" + httpsPort + "/" + webContextRoot + "/" +
                           "home" + "/" + "index.html";
            } else if (webContextRoot == null && httpsPort != null) {
                indexURL = "https://" + hostName + ":" + httpsPort + "/" + "home" + "/" + "index.html";
            } else if (webContextRoot == null) {
                indexURL = "https://" + hostName + "/" + "home" + "/" + "index.html";
            } else {
                indexURL = "https://" + hostName + "/" + webContextRoot + "/" + "home" + "/" + "index.html";
            }
        } else if (!portEnabled && webContextEnabled) {
            indexURL = "https://" + hostName + "/" + webContextRoot + "/" + "home" + "/" + "index.html";
        } else if (portEnabled && !webContextEnabled) {
            indexURL = "https://" + hostName + ":" + httpsPort + "/" + "home" + "/" + "index.html";
        } else {
            indexURL = "https://" + hostName + "/" + "home" + "/" + "index.html";
        }
        return indexURL;
    }

    public static String getRemoteRegistryURLOfProducts(String httpsPort, String hostName,
                                                        String webContextRoot) {
        String remoteRegistryURL;
        boolean webContextEnabled = Boolean.parseBoolean(prop.getProperty("carbon.web.context.enable"));
        boolean portEnabled = Boolean.parseBoolean(prop.getProperty("port.enable"));

        if (portEnabled && webContextEnabled) {
            if (webContextRoot != null && httpsPort != null) {
                remoteRegistryURL = "https://" + hostName + ":" + httpsPort + "/" + webContextRoot + "/" + "registry/";
            } else if (webContextRoot == null && httpsPort != null) {
                remoteRegistryURL = "https://" + hostName + ":" + httpsPort + "/" + "registry/";
            } else if (webContextRoot == null) {
                remoteRegistryURL = "https://" + hostName + "/" + "services/";
            } else {
                remoteRegistryURL = "https://" + hostName + "/" + webContextRoot + "/" + "registry/";
            }
        } else if (!portEnabled && webContextEnabled) {
            remoteRegistryURL = "https://" + hostName + "/" + webContextRoot + "/" + "registry/";
        } else if (portEnabled && !webContextEnabled) {
            remoteRegistryURL = "https://" + hostName + ":" + httpsPort + "/" + "registry/";
        } else {
            remoteRegistryURL = "https://" + hostName + "/" + "registry/";
        }
        return remoteRegistryURL;
    }

    public static String getRemoteRegistryURLOfStratos(String httpsPort, String hostName,
                                                       FrameworkProperties frameworkProperties,
                                                       UserInfo info) {
        String remoteRegistryURL;
        boolean webContextEnabled = frameworkProperties.getEnvironmentSettings().isEnableCarbonWebContext();
        boolean portEnabled = frameworkProperties.getEnvironmentSettings().isEnablePort();
        String webContextRoot = frameworkProperties.getProductVariables().getWebContextRoot();
        String superTenantID = "0";
        String tenantDomain;

        if (info.getUserId().equals(superTenantID)) { /*skip the domain if user is super admin */
            tenantDomain = null;
        } else {
            tenantDomain = info.getUserName().split("@")[1];
        }

        if (portEnabled && webContextEnabled) {
            if (webContextRoot != null && httpsPort != null) {
                remoteRegistryURL = "https://" + hostName + ":" + httpsPort + "/" + webContextRoot + "/t/" + tenantDomain + "/registry/";
            } else if (webContextRoot == null && httpsPort != null) {
                remoteRegistryURL = "https://" + hostName + ":" + httpsPort + "/" + "t/" + tenantDomain + "/registry/";
            } else if (webContextRoot == null) {
                remoteRegistryURL = "https://" + hostName + "/" + "t/" + tenantDomain + "/registry";
            } else {
                remoteRegistryURL = "https://" + hostName + "/" + webContextRoot + "/" + "t/" + tenantDomain + "/registry/";
            }
        } else if (!portEnabled && webContextEnabled) {
            remoteRegistryURL = "https://" + hostName + "/" + webContextRoot + "/" + "t/" + tenantDomain + "/registry/";
        } else if (portEnabled && !webContextEnabled) {
            remoteRegistryURL = "https://" + hostName + ":" + httpsPort + "/" + "t/" + tenantDomain + "/registry/";
        } else {
            remoteRegistryURL = "https://" + hostName + "/" + "t/" + tenantDomain + "/registry/";
        }
        return remoteRegistryURL;
    }
}