/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.rest.api.ui.client;

import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rest.api.stub.RestApiAdminStub;
import org.wso2.carbon.rest.api.stub.types.carbon.APIData;

public class RestApiAdminClient {
	
	private static final Log log = LogFactory.getLog(RestApiAdminClient.class);
	
	private static final String BUNDLE = "org.wso2.carbon.rest.api.ui.i18n.Resources";
    
	private ResourceBundle bundle;

	private RestApiAdminStub stub;
	
	public RestApiAdminClient(ConfigurationContext configCtx, String backendServerURL,
            String cookie, Locale locale) throws AxisFault {
		
		bundle = ResourceBundle.getBundle(BUNDLE, locale);
        String serviceURL = backendServerURL + "RestApiAdmin";
        stub = new RestApiAdminStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setTimeOutInMilliSeconds(15 * 60 * 1000);
        options.setProperty(HTTPConstants.SO_TIMEOUT, 15 * 60 * 1000);
        options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, 15 * 60 * 1000);
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
	}
	
	public String[] getApiNames() throws AxisFault{
		try {
			return stub.getApiNames();
		} catch (Exception e) {
			handleException(bundle.getString("unable.to.get.declared.apis"), e);
		}
		return null;
	}
	
	public APIData getApiByNane(String apiName) throws AxisFault{
		try {
			return stub.getApiByName(apiName);
		} catch (Exception e) {
			handleException(bundle.getString("failed.to.find.api"), e);
		}
		return null;
	}
	
	public void deleteApi(String apiName) throws AxisFault{
		try {
			stub.deleteApi(apiName);
		} catch (Exception e) {
			handleException(bundle.getString("could.not.delete.api"), e);
		}
	}
	
	public void addApi(APIData apiData) throws AxisFault{
		try {
			stub.addApi(apiData);
		} catch (Exception e) {
			handleException(bundle.getString("could.not.add.api"), e);
		}
	}
	
	public void updateApi(APIData apiData) throws AxisFault{
		try {
			stub.updateApi(apiData.getName(), apiData);
		} catch (Exception e) {
			handleException(bundle.getString("could.not.update.api"), e);
		}
	}
	
	public String[] getDefinedSequences() throws AxisFault{
		try {
			String[] sequences = stub.getSequences();
			if(sequences != null && sequences.length != 0){
				Arrays.sort(sequences);
			}
			return sequences;
		} catch (Exception e) {
			handleException(bundle.getString("could.not.get.sequences"), e);
		}
		return null;
	}
	
	public String getApiSource(APIData apiData) throws AxisFault{
		try {
			return stub.getApiSource(apiData);
		} catch (Exception e) {
			handleException(bundle.getString("could.not.get.api.source"), e);
		}
		return null;
	}
	
	public void addApiFromString(String apiData) throws AxisFault{
		try {
			stub.addApiFromString(apiData);
		} catch (Exception e) {
			handleException(bundle.getString("could.not.add.api"), e);
		}
	}
	
	public void updateApiFromString(String apiName, String apiData) throws AxisFault{
		try {
			stub.updateApiFromString(apiName, apiData);
		} catch (Exception e) {
			handleException(bundle.getString("could.not.update.api"), e);
		}
	}
	
	private void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }
	

}
