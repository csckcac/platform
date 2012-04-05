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
package org.wso2.carbon.identity.sso.saml.builders;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.StatusMessage;
import org.opensaml.saml2.core.impl.ResponseBuilder;
import org.opensaml.saml2.core.impl.StatusBuilder;
import org.opensaml.saml2.core.impl.StatusCodeBuilder;
import org.opensaml.saml2.core.impl.StatusMessageBuilder;
import org.wso2.carbon.identity.sso.saml.util.SAMLSSOUtil;

public class ErrorResponseBuilder {

    private static Log log = LogFactory.getLog(ErrorResponseBuilder.class);
    private Response response;

    //Do the bootstrap first
    static {
        SAMLSSOUtil.doBootstrap();
    }


    public ErrorResponseBuilder() {
        ResponseBuilder responseBuilder = new ResponseBuilder();
        this.response = responseBuilder.buildObject();
    }

    /**
     * Build the error response
     *
     * @param inResponseToID
     * @param status
     * @param statMsg
     * @return
     */
    public Response buildResponse(String inResponseToID, String status, String statMsg) {
        response.setIssuer(SAMLSSOUtil.getIssuer());
        response.setStatus(buildStatus(status, statMsg));
        response.setVersion(SAMLVersion.VERSION_20);
        response.setID(SAMLSSOUtil.createID());
        response.setInResponseTo(inResponseToID);
        response.setIssueInstant(new DateTime());
        return response;
    }

    /**
     * Build the status for Response
     *
     * @param status
     * @param statMsg
     * @return
     */
    private Status buildStatus(String status, String statMsg) {

        Status stat = new StatusBuilder().buildObject();

        //Set the status code
        StatusCode statCode = new StatusCodeBuilder().buildObject();
        statCode.setValue(status);
        stat.setStatusCode(statCode);

        //Set the status Message
        if (statMsg != null) {
            StatusMessage statMesssage = new StatusMessageBuilder().buildObject();
            statMesssage.setMessage(statMsg);
            stat.setStatusMessage(statMesssage);
        }

        return stat;
    }

}
