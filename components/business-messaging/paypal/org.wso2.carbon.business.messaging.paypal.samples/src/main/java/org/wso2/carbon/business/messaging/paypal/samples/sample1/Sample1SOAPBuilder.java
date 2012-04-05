/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.business.messaging.paypal.samples.sample1;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.wso2.carbon.business.messaging.paypal.samples.PayloadBuilder;

public class Sample1SOAPBuilder{

    public OMElement createRequestPayload(String version,
            String username, String password, String signature,String detailLevel,String lang) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = factory.createOMNamespace("http://wso2.services.samples",
                "ns");

        OMElement paypalOrderElem = factory.createOMElement("PayPalWebOrder",ns);
        OMElement paypalCredentialElem = factory.createOMElement("PayPalCredentials",ns);
        OMElement paypalGetBalanceElem = factory.createOMElement("PayPalBalance",ns);

        OMElement versionElem = factory.createOMElement("Version", ns);
        OMElement usernameElem = factory.createOMElement("Username", ns);
        OMElement passwordElem = factory.createOMElement("Password", ns);
        OMElement signatureElem = factory.createOMElement("Signature", ns);

        OMElement detailElem = factory.createOMElement("detailLevel", ns);
        OMElement languageElem = factory.createOMElement("language", ns);

        versionElem.setText(version);
        usernameElem.setText(username);
        passwordElem.setText(password);
        signatureElem.setText(signature);

        detailElem.setText(detailLevel);
        languageElem.setText(lang);

        paypalCredentialElem.addChild(usernameElem);
        paypalCredentialElem.addChild(versionElem);
        paypalCredentialElem.addChild(passwordElem);
        paypalCredentialElem.addChild(signatureElem);

        paypalGetBalanceElem.addChild(detailElem);
        paypalGetBalanceElem.addChild(languageElem);


        OMElement customerNameElem = factory.createOMElement("CustomerName", ns);
        customerNameElem.setText("John Smith");
        OMElement customerEmailElem = factory.createOMElement("CustomerEmail", ns);
        customerEmailElem.setText("udayangaw@wso2.com");
        OMElement retailerId = factory.createOMElement("RetailerID", ns);
        retailerId.setText("123239");
        OMElement itemNumber = factory.createOMElement("ItemNumber", ns);
        itemNumber.setText("2344497774VQ");
        OMElement itemName = factory.createOMElement("ItemName", ns);
        itemName.setText("TOM_HAWK_DVD");
        OMElement itemCount = factory.createOMElement("OrderQuantity", ns);
        itemCount.setText("2");

        OMElement reponseElem = factory.createOMElement("response", ns);

        paypalOrderElem.addChild(customerNameElem);
        paypalOrderElem.addChild(customerEmailElem);
        paypalOrderElem.addChild(retailerId);
        paypalOrderElem.addChild(itemNumber);
        paypalOrderElem.addChild(itemName);
        paypalOrderElem.addChild(itemCount);

        paypalOrderElem.addChild(paypalCredentialElem);
        paypalOrderElem.addChild(paypalGetBalanceElem);

        paypalOrderElem.addChild(reponseElem);

        return paypalOrderElem;
    }

}
