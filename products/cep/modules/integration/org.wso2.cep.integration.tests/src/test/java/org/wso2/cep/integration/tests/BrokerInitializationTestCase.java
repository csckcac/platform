package org.wso2.cep.integration.tests;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.brokermanager.stub.BrokerManagerAdminServiceBrokerManagerAdminServiceExceptionException;
import org.wso2.carbon.brokermanager.stub.BrokerManagerAdminServiceStub;
import org.wso2.carbon.brokermanager.stub.types.BrokerConfigurationDetails;
import org.wso2.carbon.brokermanager.stub.types.BrokerProperty;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;

import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Check whether the BrokerManager properly initializes the Brokers
 */
public class BrokerInitializationTestCase {

    private LoginLogoutUtil util = new LoginLogoutUtil();
    private BrokerManagerAdminServiceStub brokerManagerAdminServiceStub;

    //                          private String sessionCookie = null;
    @BeforeClass(groups = {"wso2.cep"})
    public void login() throws Exception {
        ClientConnectionUtil.waitForPort(9443);
        String loggedInSessionCookie = util.login();
        brokerManagerAdminServiceStub =
                new BrokerManagerAdminServiceStub("https://localhost:9443/services/BrokerManagerAdminService");
        ServiceClient client = brokerManagerAdminServiceStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                            loggedInSessionCookie);
    }

    @AfterClass(groups = {"wso2.cep"})
    public void logout() throws Exception {
        ClientConnectionUtil.waitForPort(9443);
        util.logout();
    }

    @Test(groups = {"wso2.cep"})
    public void brokerInitTest()
            throws BrokerManagerAdminServiceBrokerManagerAdminServiceExceptionException,
                   RemoteException {


        String[] brokerNames = brokerManagerAdminServiceStub.getBrokerNames();

        ArrayList<BrokerProperty> localBrokerPropertyArrayList = null;
        ArrayList<BrokerProperty> wsEventBrokerPropertyArrayList = null;
        ArrayList<BrokerProperty> jmsBrokerPropertyArrayList = null;


        for (String brokerName : brokerNames) {
            if (brokerName.equalsIgnoreCase("local")) {
                BrokerProperty[] brokerProperties = brokerManagerAdminServiceStub.getBrokerProperties(brokerName);
                BrokerProperty[] localBrokerProperties = new BrokerProperty[2];
                localBrokerPropertyArrayList = new ArrayList<BrokerProperty>();

                localBrokerProperties[0] = new BrokerProperty();
                localBrokerProperties[0].setKey("name");
                localBrokerProperties[0].setValue("localBroker");
                localBrokerProperties[0].setRequired(true);
                localBrokerProperties[0].setDisplayName("Broker Name");
                localBrokerProperties[0].setSecured(false);
                localBrokerPropertyArrayList.add(localBrokerProperties[0]);

                localBrokerProperties[1] = new BrokerProperty();
                localBrokerProperties[1].setKey("type");
                localBrokerProperties[1].setValue("local");
                localBrokerProperties[1].setRequired(true);
                localBrokerProperties[1].setDisplayName("Broker Type");
                localBrokerProperties[1].setSecured(false);
                localBrokerPropertyArrayList.add(localBrokerProperties[1]);


            } else if (brokerName.equalsIgnoreCase("ws-event")) {
                BrokerProperty[] brokerProperties = brokerManagerAdminServiceStub.getBrokerProperties(brokerName);
                if (brokerProperties != null) {
                    wsEventBrokerPropertyArrayList = new ArrayList<BrokerProperty>();
                    for (BrokerProperty wsEventBrokerProperty : brokerProperties) {
                        if (wsEventBrokerProperty.getKey().equals("uri")) {
                            wsEventBrokerProperty.setValue("https://localhost:9443/services/EventBrokerService");
                            wsEventBrokerPropertyArrayList.add(wsEventBrokerProperty);
                        } else if (wsEventBrokerProperty.getKey().equals("username")) {
                            wsEventBrokerProperty.setValue("admin");
                            wsEventBrokerPropertyArrayList.add(wsEventBrokerProperty);
                        } else if (wsEventBrokerProperty.getKey().equals("password")) {
                            wsEventBrokerProperty.setValue("admin");
                            wsEventBrokerPropertyArrayList.add(wsEventBrokerProperty);
                        }
                    }
                    BrokerProperty[] wsEventBrokerProperties = new BrokerProperty[2];

                    wsEventBrokerProperties[0] = new BrokerProperty();
                    wsEventBrokerProperties[0].setKey("name");
                    wsEventBrokerProperties[0].setValue("wsEventBroker");
                    wsEventBrokerProperties[0].setRequired(true);
                    wsEventBrokerProperties[0].setDisplayName("Broker Name");
                    wsEventBrokerProperties[0].setSecured(false);

                    wsEventBrokerPropertyArrayList.add(wsEventBrokerProperties[0]);


                    wsEventBrokerProperties[1] = new BrokerProperty();
                    wsEventBrokerProperties[1].setKey("type");
                    wsEventBrokerProperties[1].setValue("ws-event");
                    wsEventBrokerProperties[1].setRequired(true);
                    wsEventBrokerProperties[1].setDisplayName("Broker Type");
                    wsEventBrokerProperties[1].setSecured(false);
                    wsEventBrokerPropertyArrayList.add(wsEventBrokerProperties[1]);
                }
            }
        }


        if (localBrokerPropertyArrayList != null) {
            BrokerProperty[] localBrokerProperties = new BrokerProperty[localBrokerPropertyArrayList.size()];
            localBrokerPropertyArrayList.toArray(localBrokerProperties);
            brokerManagerAdminServiceStub.addBrokerConfiguration("localBroker", "local", localBrokerProperties);
        }

        if (wsEventBrokerPropertyArrayList != null) {
            BrokerProperty[] wsEventBrokerProperties = new BrokerProperty[wsEventBrokerPropertyArrayList.size()];
            wsEventBrokerPropertyArrayList.toArray(wsEventBrokerProperties);
            brokerManagerAdminServiceStub.addBrokerConfiguration("wsEventBroker", "ws-event", wsEventBrokerProperties);
        }



        BrokerConfigurationDetails[] brokerConfigurationDetailList = brokerManagerAdminServiceStub.getAllBrokerConfigurationNamesAndTypes();
        for (BrokerConfigurationDetails brokerConfigurationDetails : brokerConfigurationDetailList) {
            String brokerName = brokerConfigurationDetails.getBrokerName();
            if ("wsEventBroker".equals(brokerName)) {
                Assert.assertEquals("ws-event", brokerConfigurationDetails.getBrokerType());
                BrokerProperty[] createdBrokerProperties = brokerConfigurationDetails.getBrokerProperties();
                for (BrokerProperty brokerProperty : createdBrokerProperties) {
                    String brokerPropertyKey = brokerProperty.getKey();
                    if ("username".equals(brokerPropertyKey)) {
                        Assert.assertNull(brokerProperty.getDisplayName());
                        Assert.assertEquals("admin", brokerProperty.getValue());
                        Assert.assertFalse(brokerProperty.getRequired());
                        Assert.assertFalse(brokerProperty.getSecured());
                    } else if ("name".equals(brokerPropertyKey)) {
                        Assert.assertNull(brokerProperty.getDisplayName());
                        Assert.assertEquals("wsEventBroker", brokerProperty.getValue());
                        Assert.assertFalse(brokerProperty.getRequired());
                        Assert.assertFalse(brokerProperty.getSecured());
                    } else if ("password".equals(brokerPropertyKey)) {
                        Assert.assertNull(brokerProperty.getDisplayName());
                        Assert.assertEquals("admin", brokerProperty.getValue());
                        Assert.assertFalse(brokerProperty.getRequired());
                        Assert.assertFalse(brokerProperty.getSecured());
                    } else if ("type".equals(brokerPropertyKey)) {
                        Assert.assertNull(brokerProperty.getDisplayName());
                        Assert.assertEquals("ws-event", brokerProperty.getValue());
                        Assert.assertFalse(brokerProperty.getRequired());
                        Assert.assertFalse(brokerProperty.getSecured());
                    } else if ("uri".equals(brokerPropertyKey)) {
                        Assert.assertNull(brokerProperty.getDisplayName());
                        Assert.assertEquals("https://localhost:9443/services/EventBrokerService", brokerProperty.getValue());
                        Assert.assertFalse(brokerProperty.getRequired());
                        Assert.assertFalse(brokerProperty.getSecured());
                    } else {
                        Assert.fail("Broker Property Key");
                    }
                }
            } else if ("localBroker".equals(brokerName)) {
                Assert.assertEquals("local", brokerConfigurationDetails.getBrokerType());
                BrokerProperty[] createdBrokerProperties = brokerConfigurationDetails.getBrokerProperties();
                for (BrokerProperty brokerProperty : createdBrokerProperties) {
                    String brokerPropertyKey = brokerProperty.getKey();
                    if ("name".equals(brokerPropertyKey)) {
                        Assert.assertNull(brokerProperty.getDisplayName());
                        Assert.assertEquals("localBroker", brokerProperty.getValue());
                        Assert.assertFalse(brokerProperty.getRequired());
                        Assert.assertFalse(brokerProperty.getSecured());
                    } else if ("type".equals(brokerPropertyKey)) {
                        Assert.assertNull(brokerProperty.getDisplayName());
                        Assert.assertEquals("local", brokerProperty.getValue());
                        Assert.assertFalse(brokerProperty.getRequired());
                        Assert.assertFalse(brokerProperty.getSecured());
                    } else {
                        Assert.fail("Broker Property Key");
                    }
                }
            } else {
                Assert.fail("Unknown broker name");
            }
        }
    }
}
