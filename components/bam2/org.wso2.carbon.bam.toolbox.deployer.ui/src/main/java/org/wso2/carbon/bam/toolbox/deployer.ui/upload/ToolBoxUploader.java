package org.wso2.carbon.bam.toolbox.deployer.ui.upload;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.bam.toolbox.deployer.stub.BAMToolboxDepolyerServiceBAMToolboxDeploymentExceptionException;
import org.wso2.carbon.bam.toolbox.deployer.ui.client.*;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.ui.transports.fileupload.AbstractFileUploadExecutor;
import org.wso2.carbon.utils.FileItemData;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
public class ToolBoxUploader extends AbstractFileUploadExecutor {

    @Override
    public boolean execute(HttpServletRequest request, HttpServletResponse response) throws CarbonException, IOException {

        String webContext = (String) request.getAttribute(CarbonConstants.WEB_CONTEXT);

        HttpSession session = request.getSession();
        String serverURL = CarbonUIUtil.getServerURL(session.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) session.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        BAMToolBoxDeployerClient  client = new BAMToolBoxDeployerClient (cookie, serverURL, configContext);

        List<FileItemData> toolbox;
        Map<String, ArrayList<FileItemData>> fileItemsMap = getFileItemsMap();

        toolbox = fileItemsMap.get("toolbox");
        FileItemData uploadedTool = toolbox.get(0);

        try {
            if (client.uploadToolBox(uploadedTool.getDataHandler(), uploadedTool.getFileItem().getName())){
              response.sendRedirect("../" + webContext + "/bam-toolbox/listbar.jsp?success=true" );
            }
            else {
                response.sendRedirect("../" + webContext + "/bam-toolbox/uploadbar.jsp?success=false&message=" +
                        "Error while deploying toolbox!");
            }
            return true;
        } catch (BAMToolboxDepolyerServiceBAMToolboxDeploymentExceptionException e) {
            response.sendRedirect("../" + webContext +"/bam-toolbox/uploadbar.jsp?success=false&message="
                    +e.getFaultMessage().getBAMToolboxDeploymentException().getMessage());
            return false;
        }
    }
}
