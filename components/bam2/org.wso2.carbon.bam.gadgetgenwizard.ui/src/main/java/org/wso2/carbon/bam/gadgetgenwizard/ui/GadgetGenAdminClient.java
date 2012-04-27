package org.wso2.carbon.bam.gadgetgenwizard.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.bam.gadgetgenwizard.stub.GadgetGenAdminServiceStub;
import org.wso2.carbon.bam.gadgetgenwizard.stub.types.WSMap;
import org.wso2.carbon.bam.gadgetgenwizard.stub.types.WSMapElement;

import javax.servlet.http.HttpSession;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
public class GadgetGenAdminClient {

    private GadgetGenAdminServiceStub stub;

       private String GADGETGEN_ADMIN_SERVICE_URL = "GadgetGenAdminService";

       public GadgetGenAdminClient(String cookie, String backendServerURL,
                                  ConfigurationContext configCtx) throws AxisFault {
           String serviceURL = backendServerURL + GADGETGEN_ADMIN_SERVICE_URL;
           stub = new GadgetGenAdminServiceStub(configCtx, serviceURL);
           ServiceClient client = stub._getServiceClient();
           Options option = client.getOptions();
           option.setManageSession(true);
           option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
       }


    public void generateGraph(WSMap map) throws RemoteException {
        stub.createGadget(map);
    }

    public WSMap constructWSMap(HttpSession session, List<String> sessionAttrKey) {
        List<WSMapElement> sessionValues = new ArrayList<WSMapElement>();
        for (String key : sessionAttrKey) {
            WSMapElement wsMapElement = new WSMapElement();
            wsMapElement.setKey(key);
            wsMapElement.setValue(((String[]) session.getAttribute(key))[0]);
            sessionValues.add(wsMapElement);
        }
        WSMap wsMap = new WSMap();
        wsMap.setWsMapElements(sessionValues.toArray(new WSMapElement[sessionValues.size()]));

        return wsMap;
    }


}
