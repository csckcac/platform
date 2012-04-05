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
package org.wso2.carbon.business.messaging.paypal.test.core;

import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.business.messaging.paypal.mediator.ui.*;
import org.wso2.carbon.business.messaging.paypal.test.util.PaypalTestUtil;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CompactBuilderTest extends BaseTestCase {


    public void testBuildConfiguration() throws XMLStreamException {
        PaypalMediator mediator = PaypalTestUtil.
                buildMediator("compact",PaypalTestUtil.getConfiguration("paypal_config1.xml"));
        //validate top level data
        assertEquals(mediator.getTagLocalName(), "paypal");
        assertEquals(mediator.getAxis2xml(), null);
        assertEquals(mediator.getClientRepository(), null);
        assertEquals(mediator.getClientRepository(), null);
        //validate request credentials
        RequestCredential credentials = mediator.getRequestCredential();

        HashMap map = new HashMap();
        List<Input> credInputsList = new ArrayList<Input>();
        //map credentials to input object to reuse validation code
        Input usernameCred = new Input();
        usernameCred.setName("username");
        usernameCred.setSourceValue(credentials.getUsernameValue());
        usernameCred.setSourceXPath(credentials.getUsernameXPath());
        /*insert test value*/
        Input testInputUserName = new Input();
        testInputUserName.setSourceValue("uswick_1291532762_biz_api1.wso2.com");
        testInputUserName.setName(usernameCred.getName());
        map.put(usernameCred.getName(), testInputUserName);
        credInputsList.add(usernameCred);

        Input passwordCred = new Input();
        passwordCred.setName("password");
        passwordCred.setSourceValue(credentials.getPasswordValue());
        passwordCred.setSourceXPath(credentials.getPasswordXPath());
        /*insert test value*/
        Input testInputPassword = new Input();
        testInputPassword.setSourceValue("1291532800");
        testInputPassword.setName(passwordCred.getName());
        map.put(passwordCred.getName(), testInputPassword);
        credInputsList.add(passwordCred);

        Input signCred = new Input();
        signCred.setName("signature");
        signCred.setSourceValue(credentials.getSignatureValue());
        signCred.setSourceXPath(credentials.getSignatureXPath());
        /*insert test value*/
        Input testInputSign = new Input();
        testInputSign.setSourceValue("AQU0e5vuZCvSg-XJploSa.sGUDlpATwhZYmAXFUIuNss83luEA0voic8");
        testInputSign.setName(signCred.getName());
        map.put(signCred.getName(), testInputSign);
        credInputsList.add(signCred);

        //validate credentials
        PaypalTestUtil.validateCredentialInputs(map, credInputsList);
        validateOptionalRequestCredentialsForNull(credentials);

        Operation operation = mediator.getOperation();
        //we validate operations
        validateOperation(operation, "GetBalance", "US", "2.0");

        map = new HashMap();

        Input detailInput = new Input();
        detailInput.setName("DetailLevel");
        detailInput.setSourceValue("full");
        map.put(detailInput.getName(), detailInput);

        Input errorLang = new Input();
        errorLang.setName("ErrorLanguage");
        errorLang.setSourceValue("en");
        map.put(errorLang.getName(), errorLang);

        List<Input> inputs = operation.getInputs();
        PaypalTestUtil.validateSimpleInputs(map, inputs);
    }

    public void testBuildConfiguration2() throws XMLStreamException, JaxenException {
        PaypalMediator mediator = PaypalTestUtil.buildMediator("compact",
                                                               PaypalTestUtil.getConfiguration("paypal_config2.xml"));
        //validate top level data
        assertEquals(mediator.getTagLocalName(), "paypal");
        assertEquals(mediator.getAxis2xml(), null);
        assertEquals(mediator.getClientRepository(), null);
        assertEquals(mediator.getClientRepository(), null);
        //validate request credentials
        RequestCredential credentials = mediator.getRequestCredential();

        HashMap map = new HashMap();
        List<Input> credInputsList = new ArrayList<Input>();
        //map credentials to input object to reuse validation code
        Input usernameCred = new Input();
        usernameCred.setName("username");
        usernameCred.setSourceValue(credentials.getUsernameValue());
        usernameCred.setSourceXPath(credentials.getUsernameXPath());
        /*insert test value*/
        Input testInputUserName = new Input();
        SynapseXPath usernameXpathExpr = null;
        //create xpath expr
        usernameXpathExpr = new SynapseXPath("//ns2:WebRequest/ns2:user");
        usernameXpathExpr.addNamespace("ns2", "http://wso2.org/sample");
        testInputUserName.setSourceXPath(usernameXpathExpr);
        testInputUserName.setName(usernameCred.getName());
        map.put(usernameCred.getName(), testInputUserName);
        credInputsList.add(usernameCred);

        Input passwordCred = new Input();
        passwordCred.setName("password");
        passwordCred.setSourceValue(credentials.getPasswordValue());
        passwordCred.setSourceXPath(credentials.getPasswordXPath());
        /*insert test value*/
        Input testInputPassword = new Input();
        SynapseXPath passwordXpathExpr = new SynapseXPath("//ns27:WebRequest/ns27:pass");
        passwordXpathExpr.addNamespace("ns27", "http://wso2.org/sample");
        testInputPassword.setSourceXPath(passwordXpathExpr);
        testInputPassword.setName(passwordCred.getName());
        map.put(passwordCred.getName(), testInputPassword);
        credInputsList.add(passwordCred);

        Input signCred = new Input();
        signCred.setName("signature");
        signCred.setSourceValue(credentials.getSignatureValue());
        signCred.setSourceXPath(credentials.getSignatureXPath());
        /*insert test value*/
        Input testInputSign = new Input();
        testInputSign.setSourceValue("AQU0e5vuZCvSg-XJploSa.sGUDlpATwhZYmAXFUIuNss83luEA0voic8");
        testInputSign.setName(signCred.getName());
        map.put(signCred.getName(), testInputSign);
        credInputsList.add(signCred);

        //validate credentials
        PaypalTestUtil.validateCredentialInputs(map, credInputsList);
        validateOptionalRequestCredentialsForNull(credentials);

        Operation operation = mediator.getOperation();
        //we validate operations
        validateOperation(operation, "GetBalance", "US", "2.0");

        map = new HashMap();

        Input detailInput = new Input();
        detailInput.setName("DetailLevel");
        detailInput.setSourceValue("full");
        map.put(detailInput.getName(), detailInput);

        Input errorLang = new Input();
        errorLang.setName("ErrorLanguage");
        errorLang.setSourceValue("en");
        map.put(errorLang.getName(), errorLang);

        List<Input> inputs = operation.getInputs();
        PaypalTestUtil.validateSimpleInputs(map, inputs);
    }

    public void testBuildConfiguration3() throws XMLStreamException, JaxenException {
        PaypalMediator mediator = PaypalTestUtil.buildMediator("compact",
                                                               PaypalTestUtil.getConfiguration("paypal_config3.xml"));
        Operation operation = mediator.getOperation();
        //we validate operations
        validateOperation(operation, "GetBalance", "US", "2.0");

        HashMap map = new HashMap();

        Input detailInput = new Input();
        detailInput.setName("DetailLevel");
        SynapseXPath detailXpathExpr = new SynapseXPath("//ns27:WebRequest/ns27:detail");
        detailXpathExpr.addNamespace("ns27", "http://wso2.org/sample");
        detailInput.setSourceXPath(detailXpathExpr);
        map.put(detailInput.getName(), detailInput);

        Input errorLang = new Input();
        errorLang.setName("ErrorLanguage");
        SynapseXPath errorXpathExpr = new SynapseXPath("//ns2:WebRequest/ns2:errorlang");
        errorXpathExpr.addNamespace("ns2", "http://wso2.org/sample");
        errorLang.setSourceXPath(errorXpathExpr);
        map.put(errorLang.getName(), errorLang);

        List<Input> inputs = operation.getInputs();
        PaypalTestUtil.validateSimpleInputs(map, inputs);
    }

    private void validateOperation(Operation operation, String opName, String currency, String version) {
        assertEquals(operation.getName(), opName);
        assertEquals(operation.getCurrency(), currency);
//        assertEquals(operation.getVersion(),version);
    }

    private void validateOptionalRequestCredentialsForNull(RequestCredential credentials) {
        assertTrue(credentials.getAppIdValue() == null);
        assertTrue(credentials.getAppIdXPath() == null);
        assertTrue(credentials.getAuthCertValue() == null);
        assertTrue(credentials.getAuthCertXPath() == null);
        assertTrue(credentials.getAuthTokenValue() == null);
        assertTrue(credentials.getAuthTokenXPath() == null);
        assertTrue(credentials.getDevIdValue() == null);
        assertTrue(credentials.getDevIdXPath() == null);
        assertTrue(credentials.getHardExpirationValue() == null);
        assertTrue(credentials.getHardExpirationXPath() == null);
        assertTrue(credentials.getSubjectValue() == null);
        assertTrue(credentials.getSubjectXPath() == null);
    }

}
