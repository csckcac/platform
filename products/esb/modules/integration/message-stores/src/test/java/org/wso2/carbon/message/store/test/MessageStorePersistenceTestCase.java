package org.wso2.carbon.message.store.test;

import org.testng.annotations.Test;
import org.wso2.carbon.message.store.stub.MessageStoreAdminServiceStub;
import org.wso2.carbon.rest.api.stub.RestApiAdminAPIException;
import org.wso2.carbon.rest.api.stub.RestApiAdminStub;
import org.wso2.carbon.rest.api.stub.types.carbon.APIData;
import org.wso2.carbon.server.admin.stub.*;
import org.wso2.carbon.server.admin.stub.Exception;
import org.wso2.esb.integration.ESBIntegrationTestCase;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class MessageStorePersistenceTestCase extends ESBIntegrationTestCase {

    private static final String STORE_NAME = "SimpleStore";
    private static final String MESSAGE_STORE_CONFIG = "<messageStore xmlns=\"http://ws.apache.org/ns/synapse\" name=\""+ STORE_NAME +"\"/>";
    private static final int RESTART_DELAY = 800000;

    public MessageStorePersistenceTestCase() {
        super("MessageStoreAdminService");
    }

    @Test(groups = "wso2.esb", description = "Test addition of an Message Store")
    public void testRestApiAddition() throws RemoteException, Exception, InterruptedException {
        MessageStoreAdminServiceStub stub = new MessageStoreAdminServiceStub(getAdminServiceURL());
        authenticate(stub);

        String[] messageStoreNames = stub.getMessageStoreNames();
        List storeList;
        if (messageStoreNames != null && messageStoreNames.length > 0 && messageStoreNames[0] != null) {
            storeList = Arrays.asList(messageStoreNames);
            if (storeList.contains(STORE_NAME)) {
                stub.deleteMessageStore(STORE_NAME);
            }
        }

        stub.addMessageStore(MESSAGE_STORE_CONFIG);

        ServerAdminStub serverAdmin = new ServerAdminStub(getAdminServiceURL());
        serverAdmin.restart();
        Thread.sleep(RESTART_DELAY);

        //CHECK if message store exists after server restart
        messageStoreNames = stub.getMessageStoreNames();
        if (messageStoreNames != null && messageStoreNames.length > 0 && messageStoreNames[0] != null) {
            storeList = Arrays.asList(messageStoreNames);
            assertTrue(storeList.contains(STORE_NAME));
        } else{
            assertTrue(false);
        }

        String storeConfig = stub.getMessageStore(STORE_NAME);
        assertEquals(storeConfig.trim(), MESSAGE_STORE_CONFIG);
    }



}
