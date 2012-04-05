package org.wso2.carbon.samples.test;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;

import static org.testng.Assert.assertNotNull;

public class BankingServiceTestCase {

    @Test(groups = {"wso2.brs"})
    public void testDeposit() throws AxisFault, XMLStreamException {
        ClientConnectionUtil.waitForPort(9763);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        ServiceClient serviceClient = new ServiceClient();
        Options options = new Options();
        options.setTo(new EndpointReference("http://localhost:9763/services/BankingService"));
        options.setAction("urn:deposit");
        serviceClient.setOptions(options);

        OMElement result = serviceClient.sendReceive(createDepositPayload());
        assertNotNull(result, "Result cannot be null");
    }

    @Test(groups = {"wso2.brs"}, dependsOnMethods = {"testDeposit"})
    protected void testWithdrawal() throws AxisFault, XMLStreamException {
        ClientConnectionUtil.waitForPort(9763);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        ServiceClient serviceClient = new ServiceClient();
        Options options = new Options();
        options.setTo(new EndpointReference("http://localhost:9763/services/BankingService"));
        options.setAction("urn:withDraw");
        serviceClient.setOptions(options);

        OMElement result = serviceClient.sendReceive(createWithdrawPayload());
       // assertNotNull(result, "Result cannot be null");
        if(result != null){
            System.out.println(result.toString());

        }
    }

    private OMElement createDepositPayload() throws XMLStreamException {
        String request = "<p:depositRequest xmlns:p=\"http://brs.carbon.wso2.org\">\n" +
                         "   <!--Zero or more repetitions:-->\n" +
                         "   <p:Deposit>\n" +
                         "      <!--Zero or 1 repetitions:-->\n" +
                         "      <xs:accountNumber xmlns:xs=\"http://banking.samples/xsd\">8425988334v</xs:accountNumber>\n" +
                         "      <!--Zero or 1 repetitions:-->\n" +
                         "      <xs:amount xmlns:xs=\"http://banking.samples/xsd\">2632</xs:amount>\n" +
                         "   </p:Deposit>\n" +
                         "</p:depositRequest>";
        return new StAXOMBuilder(new ByteArrayInputStream(request.getBytes())).getDocumentElement();
    }

    private OMElement createWithdrawPayload() throws XMLStreamException {
        String request = "<p:withDrawRequest xmlns:p=\"http://brs.carbon.wso2.org\">\n" +
                         "   <!--Zero or more repetitions:-->\n" +
                         "   <p:Withdraw>\n" +
                         "      <!--Zero or 1 repetitions:-->\n" +
                         "      <xs:accountNumber xmlns:xs=\"http://banking.samples/xsd\">8425988334v</xs:accountNumber>\n" +
                         "      <!--Zero or 1 repetitions:-->\n" +
                         "      <xs:amount xmlns:xs=\"http://banking.samples/xsd\">2000</xs:amount>\n" +
                         "   </p:Withdraw>\n" +
                         "</p:withDrawRequest>";
        return new StAXOMBuilder(new ByteArrayInputStream(request.getBytes())).getDocumentElement();
    }
}
