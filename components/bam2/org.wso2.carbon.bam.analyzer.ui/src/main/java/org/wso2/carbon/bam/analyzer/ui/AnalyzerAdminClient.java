package org.wso2.carbon.bam.analyzer.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.bam.analyzer.stub.AnalyzerAdminServiceAnalyzerException;
import org.wso2.carbon.bam.analyzer.stub.AnalyzerAdminServiceStub;
import org.wso2.carbon.bam.analyzer.stub.service.types.ConnectionDTO;

import java.rmi.RemoteException;

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
public class AnalyzerAdminClient {

    private AnalyzerAdminServiceStub stub;

    private String ANALYZER_ADMIN_SERVICE_URL = "AnalyzerAdminService";

    public AnalyzerAdminClient(String cookie, String backendServerURL,
                               ConfigurationContext configCtx) throws AxisFault {
        String serviceURL = backendServerURL + ANALYZER_ADMIN_SERVICE_URL;
        stub = new AnalyzerAdminServiceStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public boolean addAnalyzer(String analyzerXML) throws AxisFault {
        try {
            return stub.addTask(analyzerXML);
        } catch (RemoteException e) {
            throw new AxisFault("Cannot add analyzer configuration", e);
        } catch (AnalyzerAdminServiceAnalyzerException e) {
            throw new AxisFault("Cannot add analyzer configuration", e);
        }
    }

    public boolean editAnalyer(String analyzerXML) throws AxisFault {
        try {
            return stub.editTask(analyzerXML);
        } catch (RemoteException e) {
            throw new AxisFault("Cannot edit analyzer configuration", e);
        } catch (AnalyzerAdminServiceAnalyzerException e) {
            throw new AxisFault("Cannot edit analyzer configuration", e);
        }
    }

    public boolean deleteAnalyzer(String analyzerXML) throws AxisFault {
        try {
            return stub.deleteTask(analyzerXML);
        } catch (RemoteException e) {
            throw new AxisFault("Cannot delete analyzer configuration", e);
        } catch (AnalyzerAdminServiceAnalyzerException e) {
            throw new AxisFault("Cannot delete analyzer configuration", e);
        }
    }

    public String[] getAnalyzerXMLs() throws AxisFault {
        try {
            return stub.getAnalyzerXMLs();
        } catch (RemoteException e) {
            throw new AxisFault("Cannot delete analyzer configuration", e);
        } catch (AnalyzerAdminServiceAnalyzerException e) {
            throw new AxisFault("Cannot delete analyzer configuration", e);
        }
    }

    public String getAnalyzerXML(String seqName) throws AxisFault {
        try {
            return stub.getAnalyzerXML(seqName);
        } catch (RemoteException e) {
            throw new AxisFault("Cannot delete analyzer configuration", e);
        } catch (AnalyzerAdminServiceAnalyzerException e) {
            throw new AxisFault("Cannot delete analyzer configuration", e);
        }
    }

    public boolean editIndexConfiguration(String configurationXML) throws AxisFault {
        try {
            return stub.resetIndexConfiguration(configurationXML);
        } catch (RemoteException e) {
            throw new AxisFault("Cannot edit analyzer configuration", e);
        } catch (AnalyzerAdminServiceAnalyzerException e) {
            throw new AxisFault("Cannot edit analyzer configuration", e);
        }
    }

}
