/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package sonia.scm.carbon.auth;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.SOAPBinding;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
public class UserAdminStub {
    private String userAdminEPR;

    public String getUserAdminEPR() {
        return userAdminEPR;
    }

    public void setUserAdminEPR(String userAdminEPR) {
        this.userAdminEPR = userAdminEPR;
    }

    public String getRolesXMLOfUser(String userName, String adminCookie) throws SOAPException {
        QName serviceName;
        QName portName;
        Service service;
        Dispatch<SOAPMessage> dispatch;
        BindingProvider provider;
        SOAPElement operation;
        Map<String, Object> headers;
        Map<String, List<String>> reqHeaders;
        List<String> cookieList;
        SOAPMessage response;
        SOAPMessage request;
        SOAPPart part;
        SOAPEnvelope env;
        SOAPBody body;
        SOAPElement user;

        serviceName = new QName("http://mgt.user.carbon.wso2.org", "UserAdmin");
        portName = new QName("http://mgt.user.carbon.wso2.org", "UserAdminHttpsSoap11Endpoint");

        service = Service.create(serviceName);
        service.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, getUserAdminEPR());

        dispatch = service.createDispatch(portName, SOAPMessage.class, Service.Mode.MESSAGE);
        provider = dispatch;

        MessageFactory mf = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);

        request = mf.createMessage();
        part = request.getSOAPPart();
        env = part.getEnvelope();
        body = env.getBody();

        // We are going to get all the roles belong to the user "admin"
        operation = body.addChildElement("getRolesOfUser", "ns",
                                         "http://mgt.user.carbon.wso2.org");

        user = operation.addChildElement("userName");
        user.addTextNode(userName);

        request.saveChanges();


        headers = provider.getRequestContext();
        reqHeaders = (Map<String, List<String>>) headers.get(MessageContext.HTTP_REQUEST_HEADERS);

        if (reqHeaders == null) {
            reqHeaders = new HashMap<String, List<String>>();
        }

        cookieList = new ArrayList<String>();


        cookieList.add(adminCookie);


        reqHeaders.put("Cookie", cookieList);

        headers.put(MessageContext.HTTP_REQUEST_HEADERS, reqHeaders);
        response = dispatch.invoke(request);

        return (response.getSOAPBody().getFirstChild().getTextContent());


    }

}
