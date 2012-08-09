package org.wso2.carbon.message.processor.test;

import org.testng.annotations.Test;
import org.wso2.carbon.message.processor.stub.MessageProcessorAdminServiceStub;
import org.wso2.carbon.message.store.stub.MessageStoreAdminServiceStub;
import org.wso2.carbon.server.admin.stub.Exception;
import org.wso2.carbon.server.admin.stub.ServerAdminStub;
import org.wso2.esb.integration.ESBIntegrationTestCase;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class MessageProcessorPersistenceTestCase extends ESBIntegrationTestCase {

    private static final String PROCESSOR_NAME = "Processor";
    private static final String MESSAGE_STORE_CONFIG = "<messageStore xmlns=\"http://ws.apache.org/ns/synapse\" name=\"SimpleStore\" />";
    private static final int RESTART_DELAY = 80000;
    private static final String PROCESSOR_CONFIG = "<messageProcessor xmlns=\"http://ws.apache.org/ns/synapse\" " +
                             "class=\"org.apache.synapse.message.processors.forward.ScheduledMessageForwardingProcessor\" " +
                             "name=\"" + PROCESSOR_NAME + "\"  messageStore=\"SimpleStore\"/>";

    public MessageProcessorPersistenceTestCase() {
        super("MessageProcessorAdminService");
    }

    @Test(groups = "wso2.esb", description = "Test addition of Message Processors")
    public void testRestApiAddition() throws RemoteException, Exception, InterruptedException {
        MessageProcessorAdminServiceStub stub = new MessageProcessorAdminServiceStub(getAdminServiceURL());
        MessageStoreAdminServiceStub storeAdminstub = new MessageStoreAdminServiceStub(getAdminServiceURL());
        authenticate(stub);
        authenticate(storeAdminstub);

        storeAdminstub.addMessageStore(MESSAGE_STORE_CONFIG);
        String[] processorNames = stub.getMessageProcessorNames();
        List processorList;
        if (processorNames != null && processorNames.length > 0 && processorNames[0] != null) {
            processorList = Arrays.asList(processorNames);
            if (processorList.contains(PROCESSOR_NAME)) {
                stub.deleteMessageProcessor(PROCESSOR_NAME);
            }
        }
        stub.addMessageProcessor(PROCESSOR_CONFIG);
        ServerAdminStub serverAdmin = new ServerAdminStub(getAdminServiceURL());
        serverAdmin.restart();
        Thread.sleep(RESTART_DELAY);

        processorNames = stub.getMessageProcessorNames();
        if (processorNames != null && processorNames.length > 0 && processorNames[0] != null) {
            processorList = Arrays.asList(processorNames);
            assertTrue(processorList.contains(PROCESSOR_NAME));
        } else{
            assertTrue(false);
        }
        assertEquals(stub.getMessageProcessor(PROCESSOR_NAME).trim(), PROCESSOR_CONFIG);


    }


}
