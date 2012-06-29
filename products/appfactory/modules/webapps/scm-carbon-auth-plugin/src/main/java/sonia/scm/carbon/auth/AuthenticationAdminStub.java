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
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.SOAPBinding;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class AuthenticationAdminStub {
    private String epr;


    public void setEpr(String epr) {
        this.epr = epr;
    }


    public String login(String userName, String password, String remoteAddress) throws Exception {

        QName serviceName;
        QName portName;
        Service service;
        Dispatch<SOAPMessage> dispatch;
        BindingProvider provider;
        SOAPElement operation;
        SOAPMessage response;
        SOAPMessage request;
        SOAPPart part;
        SOAPEnvelope env;
        SOAPBody body;
        SOAPElement user;
        SOAPElement pwd;

        SOAPElement remoteAddr;

        serviceName = new QName("http://authentication.services.core.carbon.wso2.org",
                                "AuthenticationAdmin");
        portName = new QName("http://authentication.services.core.carbon.wso2.org",
                             "AuthenticationAdminHttpsSoap11Endpoint");

        service = Service.create(serviceName);
        service.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, epr);

        dispatch = service.createDispatch(portName, SOAPMessage.class, Service.Mode.MESSAGE);

        provider = dispatch;

        MessageFactory mf = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);

        request = mf.createMessage();
        part = request.getSOAPPart();

        env = part.getEnvelope();
        body = env.getBody();

        operation = body.addChildElement("login", "ns",
                                         "http://authentication.services.core.carbon.wso2.org");

        user = operation.addChildElement("username");
        user.addTextNode(userName);

        pwd = operation.addChildElement("password");
        pwd.addTextNode(password);

        remoteAddr = operation.addChildElement("remoteAddress");
        remoteAddr.addTextNode(remoteAddress);

        request.saveChanges();

        response = dispatch.invoke(request);

        if ("true".equals(response.getSOAPBody().getFirstChild().getTextContent())) {
            List cookieList;
            Map respHeaders;
            Map<String, Object> headers;

            headers = provider.getResponseContext();
            respHeaders = (Map) headers.get(MessageContext.HTTP_RESPONSE_HEADERS);
            // This is the JSESSIONID cookie.
            cookieList = (List) respHeaders.get("Set-cookie");
            return (String) cookieList.get(0);
        } else {
            return null;
        }

    }
}
