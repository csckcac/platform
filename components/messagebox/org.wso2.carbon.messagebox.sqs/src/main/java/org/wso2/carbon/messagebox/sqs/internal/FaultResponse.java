/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.messagebox.sqs.internal;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.wso2.carbon.messagebox.MessageBoxException;
import org.wso2.carbon.messagebox.sqs.internal.util.Utils;

public class FaultResponse {
    private String faultCode;
    private String faultString;
    private String requestedId;


    public FaultResponse(
            MessageBoxException messageBoxException, String requestedId) {
        this.requestedId = requestedId;
        this.faultCode = messageBoxException.getMessage();
        if (faultCode != null) {
            this.faultString = Utils.getSQSErrorCodeDescriptionMap().get(faultCode);
        }
    }

    public FaultResponse(String requestedId, String faultCode, String faultString) {
        this.requestedId = requestedId;
        this.faultCode = faultCode;
        this.faultString = faultString;
    }

    /**
     * <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
     * <soap:Body>
     * <soap:Fault>
     * <soap:faultcode>InvalidParameterValue</soap:faultcode>
     * <soap:faultstring>Value (quename_nonalpha) for parameter QueueName is invalid
     * Must be an alphanumeric String of 1 to 80 in length
     * </soap:faultstring>
     * <soap:detail>
     * <aws:RequestId xmlns:aws="http://webservices.amazon.com/AWSFault/2005-15-09">
     * 42d59b56-7407-4c4a-be0f-4c88daeea257
     * </aws:RequestId>
     * </soap:detail>
     * </soap:Fault>
     * </soap:Body>
     * </soap:Envelope>
     */

    public AxisFault createAxisFault() {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace awsFaultNs = factory.createOMNamespace(
                "http://webservices.amazon.com/AWSFault/2005-15-09", "aws");
        OMElement requestIdElement = factory.createOMElement("RequestId", awsFaultNs);
        requestIdElement.setText(requestedId);
        AxisFault axisFault = new AxisFault(faultString);
        axisFault.setDetail(requestIdElement);
        axisFault.setFaultCode(faultCode);
        return axisFault;
    }
}
