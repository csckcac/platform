package org.wso2.carbon.automation.core.utils.frameworkutils.productvariables;

public class ProductVariables {

    private String _hostName;
    private String _httpPort;
    private String _httpsPort;
    private String _webContextRoot;
    private String _nhttpPort;
    private String _nhttpsPort;
    private String _qpidPort;
    private String _backendUrl;

    public String getHostName() {
        return _hostName;
    }

    public String getHttpPort() {
        return _httpPort;
    }

    public String getHttpsPort() {
        return _httpsPort;
    }

    public String getWebContextRoot() {
        return _webContextRoot;
    }

    public String getNhttpPort() {
        return _nhttpPort;
    }

    public String getNhttpsPort() {
        return _nhttpsPort;
    }

    public String getQpidPort() {
        return _qpidPort;
    }

    public String getBackendUrl() {
        return _backendUrl;
    }


    public void setProductVariables(String hostName, String httpPort, String httpsPort,
                                    String webContextRoot, String nhttpPort, String nhttpsPort,
                                    String qpidPort, String backendUrl) {
        _hostName = hostName;
        _httpPort = httpPort;
        _httpsPort = httpsPort;
        _webContextRoot = webContextRoot;
        _qpidPort = qpidPort;
        _nhttpPort = nhttpPort;
        _nhttpsPort = nhttpsPort;
        _backendUrl = backendUrl;
    }

    public void setProductVariables(String hostName, String httpPort, String httpsPort,
                                    String webContextRoot, String nhttpPort, String nhttpsPort,
                                    String backendUrl) {
        _hostName = hostName;
        _httpPort = httpPort;
        _httpsPort = httpsPort;
        _webContextRoot = webContextRoot;
        _nhttpPort = nhttpPort;
        _nhttpsPort = nhttpsPort;
        _backendUrl = backendUrl;
    }

    public void setProductVariables(String hostName, String httpPort, String httpsPort,
                                    String webContextRoot, String qpidPort, String backendUrl) {
        _hostName = hostName;
        _httpPort = httpPort;
        _httpsPort = httpsPort;
        _webContextRoot = webContextRoot;
        _qpidPort = qpidPort;
        _backendUrl = backendUrl;
    }

    public void setProductVariables(String hostName, String httpPort, String httpsPort,
                                    String webContextRoot, String backendUrl) {
        _hostName = hostName;
        _httpPort = httpPort;
        _httpsPort = httpsPort;
        _webContextRoot = webContextRoot;
        _backendUrl = backendUrl;
    }
}