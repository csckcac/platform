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
package org.wso2.carbon.identity.scim.consumer.extensions;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.scim.consumer.utils.BasicAuthUtil;
import org.wso2.carbon.server.admin.privilegedaction.PrivilegedAction;
import org.wso2.carbon.server.admin.privilegedaction.PrivilegedActionException;
import org.wso2.charon.core.client.SCIMClient;
import org.wso2.charon.core.exceptions.BadRequestException;
import org.wso2.charon.core.exceptions.CharonException;
import org.wso2.charon.core.objects.User;
import org.wso2.charon.core.schema.SCIMConstants;

import org.apache.commons.logging.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class AddUserExtension extends AbstractExtension {

    /**
     * for the moment, have some hard coded urls and credentials..
     */
    public static String USER_ENDPOINT = "http://localhost:9788/wso2/scim/Users";
    public static String USER_NAME = "admin";
    public static String PASSWORD = "admini";

    private static Log logger = LogFactory.getLog(AddUserExtension.class.getName());
    private final String EXTENSION_NAME = "addUserExtension";
    private final String SOAP_ACTION = "addUser";
    private final int PRIORITY = 1;

    public void execute(MessageContext messageContext, MessageContext messageContext1)
            throws PrivilegedActionException {
        try {
            //temporary
            logger.info("AddUser Extension was invoked...");
            //extract info from SOAP envelope

            //create User
            SCIMClient scimClient = new SCIMClient();
            User user = scimClient.createUser();
            user.setUserName("hasini");
            user.setPassword("hasini");

            //encode SCIM User
            String encodedUser = scimClient.encodeSCIMObject(user, SCIMConstants.JSON);

            //create client to consume SCIM REST endpoint
            PostMethod postMethod = new PostMethod(USER_ENDPOINT);
            postMethod.addRequestHeader("Authorization",
                                        BasicAuthUtil.getBase64EncodedBasicAuthHeader(USER_NAME, PASSWORD));
            RequestEntity requestEntity = new StringRequestEntity(encodedUser, SCIMConstants.APPLICATION_JSON, null);
            postMethod.setRequestEntity(requestEntity);

            HttpClient client = new HttpClient();
            int responseStatus = client.executeMethod(postMethod);
            System.out.println(responseStatus);
            String response = postMethod.getResponseBodyAsString();
            if (scimClient.evaluateResponseStatus(responseStatus)) {

                scimClient.decodeSCIMResponse(response, SCIMConstants.JSON, SCIMClient.USER);
            } else {
                scimClient.decodeSCIMException(response, SCIMConstants.JSON);
            }

        } catch (UnsupportedEncodingException e) {
            //http client - unsupported encoding
            e.printStackTrace();
        } catch (CharonException e) {
            //error in creating or encoding scim user
            e.printStackTrace();
        } catch (IOException e) {
            //error in invoking http client
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (BadRequestException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public int getPriority() {
        return PRIORITY;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /*public boolean doesHandle(MessageContext messageContext) {
        String method;
        AxisOperation op = messageContext.getOperationContext().getAxisOperation();
        if ((op.getName() != null) && ((method =
                JavaUtils.xmlNameToJavaIdentifier(op.getName().getLocalPart())) != null)) {
            if (method.equals(SOAP_ACTION)) {
                return true;
            }
        }
        return false;
    }
*/

    @Override
    public String getSOAPAction() {
        return SOAP_ACTION;
    }

    public boolean isDisabled() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getExtensionName() {
        return EXTENSION_NAME;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean skipServiceInvocation() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean skipLowerPriorityExtensions() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
