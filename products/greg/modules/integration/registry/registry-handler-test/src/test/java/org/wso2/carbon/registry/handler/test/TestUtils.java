package org.wso2.carbon.registry.handler.test;

import junit.framework.Assert;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.integration.core.FrameworkSettings;
import org.wso2.carbon.registry.handler.stub.HandlerManagementServiceStub;

import java.io.File;

public class TestUtils {

    private static final Log log = LogFactory.getLog(TestUtils.class);

    public static String getHandlerResourcePath(String frameworkPath) {
        return frameworkPath + File.separator + ".." + File.separator + ".." + File.separator + ".." +
                File.separator + "src" + File.separator + "test" + File.separator + "java" + File.separator +
                "resources" + File.separator + "sample-handler.xml";
    }

    public static HandlerManagementServiceStub getHandlerManagementServiceStub(String sessionCookie) {
        String serviceURL = null;
        serviceURL = FrameworkSettings.SERVICE_URL + "HandlerManagementService";
        HandlerManagementServiceStub handlerManagementServiceStub = null;
        try {
            handlerManagementServiceStub = new HandlerManagementServiceStub(serviceURL);
            ServiceClient client = handlerManagementServiceStub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);
            handlerManagementServiceStub._getServiceClient().getOptions().setTimeOutInMilliSeconds(600000);
        } catch (AxisFault axisFault) {
            Assert.fail("Unexpected exception thrown");
            axisFault.printStackTrace();
        }
        log.info("handlerManagementServiceStub created");
        return handlerManagementServiceStub;
    }
}
