package org.wso2.carbon.rest.api.test;

import org.testng.annotations.Test;
import org.wso2.carbon.rest.api.stub.RestApiAdminAPIException;
import org.wso2.carbon.rest.api.stub.RestApiAdminStub;
import org.wso2.carbon.rest.api.stub.types.carbon.APIData;
import org.wso2.carbon.server.admin.stub.*;
import org.wso2.carbon.server.admin.stub.Exception;
import org.wso2.esb.integration.ESBIntegrationTestCase;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertTrue;

public class RestAPIPersistenceTestCase extends ESBIntegrationTestCase {

    private static final String API_NAME = "RestAPITest";

    public RestAPIPersistenceTestCase() {
        super("RestApiAdmin");
    }

    @Test(groups = "wso2.esb", description = "Test addition of an Rest API")
    public void testRestApiAddition() throws RemoteException, RestApiAdminAPIException, Exception, InterruptedException {
        RestApiAdminStub stub = new RestApiAdminStub(getAdminServiceURL());
        authenticate(stub);

        String[] apiNames = stub.getApiNames();
        List apiList;
        if (apiNames != null && apiNames.length > 0 && apiNames[0] != null) {
            apiList = Arrays.asList(apiNames);
            if (apiList.contains(API_NAME)) {
                assertTrue(stub.deleteApi(API_NAME));
            }
        }

        APIData apiData = stub.getApiByName("<api name=\"" + API_NAME + "\" context=\"/stockquote\">" + "<resource methods=\"GET\"/></api>");
        assertTrue(stub.addApi(apiData));

        ServerAdminStub serverAdmin = new ServerAdminStub(getAdminServiceURL());
        serverAdmin.restart();
        Thread.sleep(1000);

        apiNames = stub.getApiNames();
        if (apiNames != null && apiNames.length > 0 && apiNames[0] != null) {
            apiList = Arrays.asList(apiNames);
            assertTrue(apiList.contains(API_NAME));
        }



    }



}
