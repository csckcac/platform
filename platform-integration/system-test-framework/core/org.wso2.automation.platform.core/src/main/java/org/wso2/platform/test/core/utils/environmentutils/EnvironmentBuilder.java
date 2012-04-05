package org.wso2.platform.test.core.utils.environmentutils;

import org.wso2.carbon.admin.service.AdminServiceAuthentication;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.frameworkutils.EnvironmentSetter;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkProperties;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkSettings;
import org.wso2.platform.test.core.utils.frameworkutils.productvariables.ProductVariables;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class EnvironmentBuilder {
    protected EnvironmentVariables as;
    protected EnvironmentVariables esb;
    protected EnvironmentVariables is;
    protected EnvironmentVariables bps;
    protected EnvironmentVariables dss;
    protected EnvironmentVariables greg;
    protected EnvironmentVariables bam;
    protected EnvironmentVariables brs;
    protected EnvironmentVariables cep;
    protected EnvironmentVariables gs;
    protected EnvironmentVariables mb;
    protected EnvironmentVariables ms;
    protected EnvironmentVariables manager;
    protected EnvironmentVariables clusterNode;
    protected List<EnvironmentVariables> clusterList = new LinkedList<EnvironmentVariables>();
    protected Map<String,EnvironmentVariables> clusterMap=new HashMap();
    private String sessionCookie;
    private String backEndUrl;
    private String serviceUrl;
    private String serverHostName;
    private String httpPort;
    private String webAppURL;
    private EnvironmentVariables productVariables;

    public EnvironmentBuilder() {
    }

    public EnvironmentBuilder as(int userId) {
        productVariables = new EnvironmentVariables();
        FrameworkProperties frameworkProperties =
                FrameworkFactory.getFrameworkProperties(ProductConstant.APP_SERVER_NAME);
        ProductVariables asSetter = frameworkProperties.getProductVariables();
        AdminServiceAuthentication adminServiceAuthentication;
        backEndUrl = asSetter.getBackendUrl();
        serverHostName = asSetter.getHostName();
        httpPort = asSetter.getHttpPort();
        adminServiceAuthentication = new AdminServiceAuthentication(backEndUrl);
        productVariables = loginSetupAs(userId, adminServiceAuthentication, frameworkProperties, asSetter);
        this.as = productVariables;
        return this;
    }

    public EnvironmentBuilder esb(int userId) {
        productVariables = new EnvironmentVariables();
        FrameworkProperties frameworkProperties =
                FrameworkFactory.getFrameworkProperties(ProductConstant.ESB_SERVER_NAME);
        ProductVariables esbSetter = frameworkProperties.getProductVariables();
        AdminServiceAuthentication adminServiceAuthentication;
        backEndUrl = esbSetter.getBackendUrl();
        serverHostName = esbSetter.getHostName();
        httpPort = esbSetter.getHttpPort();
        adminServiceAuthentication = new AdminServiceAuthentication(backEndUrl);
        productVariables = loginSetup(userId, httpPort, adminServiceAuthentication, frameworkProperties, esbSetter);
        this.esb = productVariables;
        return this;
    }

    public EnvironmentBuilder is(int userId) {
        productVariables = new EnvironmentVariables();
        AdminServiceAuthentication adminServiceAuthentication;
        FrameworkProperties frameworkProperties =
                FrameworkFactory.getFrameworkProperties(ProductConstant.IS_SERVER_NAME);
        ProductVariables isSetter = frameworkProperties.getProductVariables();
        backEndUrl = isSetter.getBackendUrl();
        serverHostName = isSetter.getHostName();
        httpPort = isSetter.getHttpPort();
        adminServiceAuthentication = new AdminServiceAuthentication(backEndUrl);
        productVariables = loginSetup(userId, httpPort, adminServiceAuthentication, frameworkProperties, isSetter);
        this.is = productVariables;
        return this;
    }

    public EnvironmentBuilder bps(int userId) {
        productVariables = new EnvironmentVariables();
        AdminServiceAuthentication adminServiceAuthentication;
        FrameworkProperties frameworkProperties =
                FrameworkFactory.getFrameworkProperties(ProductConstant.BPS_SERVER_NAME);
        ProductVariables bpsSetter = frameworkProperties.getProductVariables();
        backEndUrl = bpsSetter.getBackendUrl();
        serverHostName = bpsSetter.getHostName();
        httpPort = bpsSetter.getHttpPort();
        adminServiceAuthentication = new AdminServiceAuthentication(backEndUrl);
        productVariables = loginSetup(userId, httpPort, adminServiceAuthentication, frameworkProperties, bpsSetter);
        this.bps = productVariables;
        return this;
    }

    public EnvironmentBuilder dss(int userId) {
        productVariables = new EnvironmentVariables();
        AdminServiceAuthentication adminServiceAuthentication;
        FrameworkProperties frameworkProperties =
                FrameworkFactory.getFrameworkProperties(ProductConstant.DSS_SERVER_NAME);
        ProductVariables dssSetter = frameworkProperties.getProductVariables();
        backEndUrl = dssSetter.getBackendUrl();
        serverHostName = dssSetter.getHostName();
        httpPort = dssSetter.getHttpPort();
        adminServiceAuthentication = new AdminServiceAuthentication(backEndUrl);
        productVariables = loginSetup(userId, httpPort, adminServiceAuthentication, frameworkProperties, dssSetter);
        this.dss = productVariables;
        return this;
    }

    public EnvironmentBuilder greg(int tenent) {
        productVariables = new EnvironmentVariables();
        AdminServiceAuthentication adminServiceAuthentication;
        FrameworkProperties frameworkProperties =
                FrameworkFactory.getFrameworkProperties(ProductConstant.GREG_SERVER_NAME);
        ProductVariables gregSetter = frameworkProperties.getProductVariables();
        backEndUrl = gregSetter.getBackendUrl();
        serverHostName = gregSetter.getHostName();
        httpPort = gregSetter.getHttpPort();
        adminServiceAuthentication = new AdminServiceAuthentication(backEndUrl);
        productVariables = loginSetup(tenent, httpPort, adminServiceAuthentication, frameworkProperties, gregSetter);
        this.greg = productVariables;
        return this;
    }

    public EnvironmentBuilder bam(int tenent) {
        productVariables = new EnvironmentVariables();
        AdminServiceAuthentication adminServiceAuthentication;
        FrameworkProperties frameworkProperties =
                FrameworkFactory.getFrameworkProperties(ProductConstant.BAM_SERVER_NAME);
        ProductVariables bamSetter = frameworkProperties.getProductVariables();
        backEndUrl = bamSetter.getBackendUrl();
        serverHostName = bamSetter.getHostName();
        httpPort = bamSetter.getHttpPort();
        adminServiceAuthentication = new AdminServiceAuthentication(backEndUrl);
        productVariables = loginSetup(tenent, httpPort, adminServiceAuthentication, frameworkProperties, bamSetter);
        this.bam = productVariables;
        return this;
    }


    public EnvironmentBuilder brs(int tenent) {
        productVariables = new EnvironmentVariables();
        AdminServiceAuthentication adminServiceAuthentication;
        FrameworkProperties frameworkProperties =
                FrameworkFactory.getFrameworkProperties(ProductConstant.BRS_SERVER_NAME);
        ProductVariables brsSetter = frameworkProperties.getProductVariables();
        backEndUrl = brsSetter.getBackendUrl();
        serverHostName = brsSetter.getHostName();
        httpPort = brsSetter.getHttpPort();
        adminServiceAuthentication = new AdminServiceAuthentication(backEndUrl);
        productVariables = loginSetup(tenent, httpPort, adminServiceAuthentication, frameworkProperties, brsSetter);
        this.brs = productVariables;
        return this;
    }

    public EnvironmentBuilder cep(int tenent) {
        productVariables = new EnvironmentVariables();
        AdminServiceAuthentication adminServiceAuthentication;
        FrameworkProperties frameworkProperties =
                FrameworkFactory.getFrameworkProperties(ProductConstant.CEP_SERVER_NAME);
        ProductVariables cepSetter = frameworkProperties.getProductVariables();
        backEndUrl = cepSetter.getBackendUrl();
        serverHostName = cepSetter.getHostName();
        httpPort = cepSetter.getHttpPort();
        adminServiceAuthentication = new AdminServiceAuthentication(backEndUrl);
        productVariables = loginSetup(tenent, httpPort, adminServiceAuthentication, frameworkProperties, cepSetter);
        this.cep = productVariables;
        return this;
    }

    public EnvironmentBuilder gs(int tenent) {
        productVariables = new EnvironmentVariables();
        AdminServiceAuthentication adminServiceAuthentication;
        FrameworkProperties frameworkProperties =
                FrameworkFactory.getFrameworkProperties(ProductConstant.GS_SERVER_NAME);
        ProductVariables gsSetter = frameworkProperties.getProductVariables();
        backEndUrl = gsSetter.getBackendUrl();
        serverHostName = gsSetter.getHostName();
        httpPort = gsSetter.getHttpPort();
        adminServiceAuthentication = new AdminServiceAuthentication(backEndUrl);
        productVariables = loginSetup(tenent, httpPort, adminServiceAuthentication, frameworkProperties, gsSetter);
        this.gs = productVariables;
        return this;
    }

    public EnvironmentBuilder ms(int tenent) {
        productVariables = new EnvironmentVariables();
        AdminServiceAuthentication adminServiceAuthentication;
        FrameworkProperties frameworkProperties =
                FrameworkFactory.getFrameworkProperties(ProductConstant.MS_SERVER_NAME);
        ProductVariables msSetter = frameworkProperties.getProductVariables();
        backEndUrl = msSetter.getBackendUrl();
        serverHostName = msSetter.getHostName();
        httpPort = msSetter.getHttpPort();
        adminServiceAuthentication = new AdminServiceAuthentication(backEndUrl);
        productVariables = loginSetup(tenent, httpPort, adminServiceAuthentication, frameworkProperties, msSetter);
        this.ms = productVariables;
        return this;
    }

    public EnvironmentBuilder mb(int tenent) {
        productVariables = new EnvironmentVariables();
        AdminServiceAuthentication adminServiceAuthentication;
        FrameworkProperties frameworkProperties =
                FrameworkFactory.getFrameworkProperties(ProductConstant.MB_SERVER_NAME);
        ProductVariables mbSetter = frameworkProperties.getProductVariables();
        backEndUrl = mbSetter.getBackendUrl();
        serverHostName = mbSetter.getHostName();
        httpPort = mbSetter.getHttpPort();
        adminServiceAuthentication = new AdminServiceAuthentication(backEndUrl);
        productVariables = loginSetup(tenent, httpPort, adminServiceAuthentication, frameworkProperties, mbSetter);
        this.mb = productVariables;
        return this;
    }


    public EnvironmentBuilder manager(int tenent) {
        productVariables = new EnvironmentVariables();
        AdminServiceAuthentication adminServiceAuthentication;
        FrameworkProperties frameworkProperties =
                FrameworkFactory.getFrameworkProperties(ProductConstant.MANAGER_SERVER_NAME);
        ProductVariables managerSetter = frameworkProperties.getProductVariables();
        backEndUrl = managerSetter.getBackendUrl();
        serverHostName = managerSetter.getHostName();
        httpPort = managerSetter.getHttpPort();
        adminServiceAuthentication = new AdminServiceAuthentication(backEndUrl);
        productVariables = loginSetup(tenent, httpPort, adminServiceAuthentication, frameworkProperties, managerSetter);
        this.manager = productVariables;
        return this;
    }

    public EnvironmentBuilder clusterNode(String node,int tenent)
    {
        ClusterReader reader = new ClusterReader();

            reader.getProductName(node);
        productVariables = new EnvironmentVariables();
        AdminServiceAuthentication adminServiceAuthentication;
        FrameworkProperties frameworkProperties =
                FrameworkFactory.getClusterProperties(node);
        ProductVariables clusterSetter = frameworkProperties.getProductVariables();
        backEndUrl = clusterSetter.getBackendUrl();
        serverHostName = clusterSetter.getHostName();
        httpPort = clusterSetter.getHttpPort();
        adminServiceAuthentication = new AdminServiceAuthentication(backEndUrl);
        productVariables = loginSetup(tenent, httpPort, adminServiceAuthentication, frameworkProperties, clusterSetter);
        this.clusterNode = productVariables;
        this.clusterList.add(this.clusterNode);
        this.clusterMap.put(node,this.clusterNode);
        return this;
    }



    private EnvironmentVariables loginSetup(int tenent, String httpPort,
                                            AdminServiceAuthentication adminServiceAuthentication,
                                            FrameworkProperties frameworkProperties,
                                            ProductVariables productSetter) {
        UserInfo userInfo = UserListCsvReader.getUserInfo(tenent);
        sessionCookie = adminServiceAuthentication.login(userInfo.getUserName(),
                                                         userInfo.getPassword(), serverHostName);
        serviceUrl = getServiceURL(frameworkProperties, productSetter, userInfo);
        productVariables.setEnvironment(sessionCookie, backEndUrl, serviceUrl, userInfo,
                                        adminServiceAuthentication, productSetter);
        return productVariables;
    }

    private EnvironmentVariables loginSetupAs(int user,
                                              AdminServiceAuthentication adminServiceAuthentication,
                                              FrameworkProperties frameworkProperties,
                                              ProductVariables productSetter) {
        UserInfo userInfo = UserListCsvReader.getUserInfo(user);
        sessionCookie = adminServiceAuthentication.login(userInfo.getUserName(),
                                                         userInfo.getPassword(), serverHostName);
        serviceUrl = getServiceURL(frameworkProperties, productSetter, userInfo);
        webAppURL = new ProductUrlGeneratorUtil().
                getWebappURL(httpPort, serverHostName, frameworkProperties, userInfo);
        productVariables.setEnvironment(sessionCookie, backEndUrl, serviceUrl, webAppURL,
                                        userInfo, adminServiceAuthentication, productSetter);
        return productVariables;
    }

    private String getServiceURL(FrameworkProperties frameworkProperties,
                                 ProductVariables productSetter, UserInfo userInfo) {
        String generatedServiceURL = null;
        if (productSetter.getNhttpPort() != null) { //if port is nhttp port
            generatedServiceURL = new ProductUrlGeneratorUtil().
                    getHttpServiceURL(productSetter.getNhttpPort(), serverHostName, frameworkProperties, userInfo);
        } else {
            generatedServiceURL = new ProductUrlGeneratorUtil().
                    getHttpServiceURL(httpPort, serverHostName, frameworkProperties, userInfo);
        }
        return generatedServiceURL;
    }


    public FrameworkSettings getFrameworkSettings() {
        FrameworkSettings frameworkSettings = new FrameworkSettings();
        EnvironmentSetter setter = new EnvironmentSetter();
        frameworkSettings.setFrameworkSettings(setter.getDataSource(), setter.getEnvironmentSettings(),
                                               setter.getEnvironmentVariables(), setter.getSelenium(),
                                               setter.getRavana(), setter.getDashboardVariables());
        return frameworkSettings;
    }

    public ManageEnvironment build() {
        return new ManageEnvironment(this);
    }
}
