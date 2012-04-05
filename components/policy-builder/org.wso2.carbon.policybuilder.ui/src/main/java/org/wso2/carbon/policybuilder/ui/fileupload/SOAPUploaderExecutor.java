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
 * Time: 11:24:06 AM
 * To change this template use File | Settings | File Templates.
 */

import org.wso2.carbon.ui.transports.fileupload.AbstractFileUploadExecutor;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.ui.CarbonUIMessage;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.FileItemData;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class SOAPUploaderExecutor extends AbstractFileUploadExecutor {

	private static Log log = LogFactory.getLog(SOAPUploaderExecutor.class);

	public boolean execute(HttpServletRequest request, HttpServletResponse response) throws CarbonException, IOException {
		//response.setContentType("text/xml; charset=utf-8");
		response.setContentType("text/plain; charset=utf-8");
        String action = request.getParameter("action")   ;
        boolean isSoapText ="soapTextAction".equals(action);
        System.out.println("Action = " + action);

        PrintWriter out = response.getWriter();
		String webContext = (String) request.getAttribute(CarbonConstants.WEB_CONTEXT);
		String serverURL = (String) request.getAttribute(CarbonConstants.SERVER_URL);
		String cookie = (String) request.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
		if (fileItemsMap == null || fileItemsMap.isEmpty()) {
			String msg = "File uploading failed. No files are specified";
			log.error(msg);
			//CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request,
				//	response, "../" + webContext + "/axis1/index.jsp");
		}

		//Creating the stub to call the back-end service
		UploaderServiceClient uploaderClient = new UploaderServiceClient(
				configurationContext, serverURL + "UploaderService", cookie);
		String msg,output="";
		try {
            if(!isSoapText){
                boolean hasFile = false;
                for (Object o : fileItemsMap.keySet()) {
                    String fieldName = (String) o;
                    FileItemData fileItemData = fileItemsMap.get(fieldName);
                    String fileName = getFileName(fileItemData.getFileItem().getName());
                    if (fieldName.indexOf("soapFile") > -1) {
                        uploaderClient.UploadSoapFile(fileItemData.getDataHandler());
                        hasFile = true;
                        break;
                    }
                }
                if (!hasFile) {
                    throw new Exception("no file type found for uploading");
                }

                //Uploading files to back-end service
                output = uploaderClient.getPolicyString();
                if (log.isDebugEnabled()) {
                    log.info("Output :\n" + output);
                }
                //System.out.println("Output :\n" + output);
                msg = "Files have been uploaded "
                        + "successfully. Please refresh this page in a while to see"
                        + "the status of the created Axis1 service \n";
                msg += output;
            }
            else{
                   for (Object o : formFieldsMap.keySet()) {
                    String keyName = (String) o;
                    String value = formFieldsMap.get(keyName);
                    //String fileName = getFileName(fileItemData.getFileItem().getName());
                    if (keyName!=null && keyName.indexOf("soapText") > -1) {
                        if (log.isDebugEnabled()) {
                            log.debug("SOAP TEXT FOUND");
                        }
                        //System.out.println("SOAP TEXT FOUND");
                        String soapText = value;
                        if (log.isDebugEnabled()) {
                            log.debug("SOAP TEXT : "+ soapText );
                        }

                        uploaderClient.uploadSoapText(soapText);
                        output = uploaderClient.getPolicyStringFromText();
                        break;
                        }
                    }


                 

            }
            out.write(output);
			out.flush();
			return true;
		} catch (Exception e) {
			msg = "File upload failed. " + e.getMessage();
			log.error(msg);
			//CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request,
					//response, "../" + webContext + "/policybuilder/index.jsp");
		}
		return false;
	}
}
