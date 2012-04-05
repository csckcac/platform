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
package org.wso2.carbon.policybuilder.ui.fileupload;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Mar 24, 2009
 * Time: 11:41:54 AM
 * To change this template use File | Settings | File Templates.
 */

import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.AxisFault;
import org.wso2.carbon.policybuilder.ui.codegen.UploaderServiceStub;

import javax.activation.DataHandler;
import java.rmi.RemoteException;

public class UploaderServiceClient {

	private UploaderServiceStub stub;
	private DataHandler soapFile;
    private String soapText;

    public UploaderServiceClient(ConfigurationContext ctx, String serviceURL, String cookie) throws AxisFault {
		stub = new UploaderServiceStub(ctx, serviceURL);
		Options options = stub._getServiceClient().getOptions();
		options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
		options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
		//Increase the time out when sending large attachments
		options.setTimeOutInMilliSeconds(100000);
		options.setManageSession(true);
	}

	public void UploadSoapFile(DataHandler soapFile) {
		this.soapFile = soapFile;
	}

    public void uploadSoapText(String soapText){
        this.soapText =soapText;
    }

    public String getPolicyString() throws RemoteException {
		return stub.getString(soapFile);
	}

    public String getPolicyStringFromText() throws RemoteException{

        return stub.getPolicy(soapText);
    }
}
