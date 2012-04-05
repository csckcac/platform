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
package org.wso2.carbon.governance.generic.ui.utils;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.generic.ui.clients.ManageGenericArtifactServiceClient;
import org.wso2.carbon.governance.services.ui.utils.AddServiceUIGenerator;
import org.wso2.carbon.registry.common.ui.UIException;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class ManageGenericArtifactUtil {
    private static final Log log = LogFactory.getLog(ManageGenericArtifactUtil.class);

    public static String addArtifactContent(
            OMElement info, HttpServletRequest request, ServletConfig config, HttpSession session,
            String dataName, String dataNamespace) throws UIException {

        try {
            ManageGenericArtifactServiceClient
                    serviceClient = new ManageGenericArtifactServiceClient(config, session);
            OMElement filledService = new AddServiceUIGenerator(
                    dataName, dataNamespace).getDataFromUI(info, request);
            String operation = request.getParameter("add_edit_operation");
            if (operation != null) {
                if (operation.equals("add")) {
                    return serviceClient.addArtifact(request.getParameter("key"),
                            filledService.toString(),
                            request.getParameter("lifecycleAttribute"));
                } else if (operation.equals("edit")) {
                    return serviceClient.editArtifact(request.getParameter("key"),
                            filledService.toString(),
                            request.getParameter("lifecycleAttribute"));
                }
            }
            return null;
        } catch (Exception e) {
            String msg = "Failed to add/edit artifact details. " + e.getMessage();
            log.error(msg, e);
            throw new UIException(msg, e);
        }
    }
}
