/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.automation.api.clients.business.processes;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.api.clients.utils.AuthenticateStub;
import org.wso2.carbon.bpel.stub.mgt.InstanceManagementException;
import org.wso2.carbon.bpel.stub.mgt.InstanceManagementServiceStub;
import org.wso2.carbon.bpel.stub.mgt.types.InstanceInfoType;
import org.wso2.carbon.bpel.stub.mgt.types.LimitedInstanceInfoType;
import org.wso2.carbon.bpel.stub.mgt.types.PaginatedInstanceList;
import org.wso2.carbon.bpel.stub.mgt.types.VariableInfoType;
import org.wso2.carbon.bpel.stub.mgt.types.Variables_type0;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;


public class BpelInstanceManagerClient {
    String ServiceEndPoint = null;
    String SessionCookie = null;
    private static final Log log = LogFactory.getLog(BpelInstanceManagerClient.class);

    InstanceManagementServiceStub instanceManagementServiceStub = null;

    /**
     * Registers the text to display in a tool tip.   The text
     * displays when the cursor lingers over the component.
     *
     * @param serviceEndPoint the string to display.  If the text is null,
     *                        the tool tip is turned off for this component.
     */
    public BpelInstanceManagerClient(String serviceEndPoint, String sessionCookie) {
        this.ServiceEndPoint = serviceEndPoint;
        this.SessionCookie = sessionCookie;
    }

    private InstanceManagementServiceStub setInstanceManagementStub() throws AxisFault {
        final String packageMgtServiceUrl = ServiceEndPoint + "InstanceManagementService";

        InstanceManagementServiceStub instanceManagementStub = null;
        instanceManagementStub = new InstanceManagementServiceStub(packageMgtServiceUrl);
        AuthenticateStub.authenticateStub(SessionCookie, instanceManagementStub);
        return instanceManagementStub;
    }

    public PaginatedInstanceList listAllInstances()
            throws InstanceManagementException, RemoteException {

        instanceManagementServiceStub = this.setInstanceManagementStub();

        PaginatedInstanceList paginatedInstanceList = null;
        /** The filter set on the is not filtering appropriate services as the filter is ment . it is require to filer the service manually**/
        paginatedInstanceList = instanceManagementServiceStub.getPaginatedInstanceList("", "", 300, 0);
        return paginatedInstanceList;
    }

    public PaginatedInstanceList filterPageInstances(String processId)
            throws InstanceManagementException, RemoteException {

        instanceManagementServiceStub = this.setInstanceManagementStub();

        PaginatedInstanceList paginatedInstanceList = null;
        PaginatedInstanceList filteredInstanceList = null;
        /** The filter set on the is not filtering appropriate services as the filter is ment . it is require to filer the service manually**/
        paginatedInstanceList = instanceManagementServiceStub.getPaginatedInstanceList("", "", 300, 0);
        filteredInstanceList = new PaginatedInstanceList();
        if (paginatedInstanceList.isInstanceSpecified()) {
            for (LimitedInstanceInfoType instance : paginatedInstanceList.getInstance()) {
                if (instance.getPid().toString().contains(processId)) {
                    filteredInstanceList.addInstance(instance);
                }
            }
        }
        return filteredInstanceList;
    }


    public List<String> listInstances(String processId)
            throws RemoteException, InstanceManagementException {
        List<String> instanceIds = new ArrayList<String>();

        instanceManagementServiceStub = this.setInstanceManagementStub();
        /** The filter set on the is not filtering appropriate services as the filter is ment . it is require to filer the service manually**/
        PaginatedInstanceList paginatedInstanceList = instanceManagementServiceStub.getPaginatedInstanceList("", "", 300, 0);
        if (paginatedInstanceList.isInstanceSpecified()) {
            for (LimitedInstanceInfoType instance : paginatedInstanceList.getInstance()) {
                instanceIds.add(instance.getIid());
                log.info("ProcessId: " + instance.getPid() +
                         "\nInstanceId: " + instance.getIid() +
                         "\nStarted: " + instance.getDateStarted().getTime() +
                         "\nState: " + instance.getStatus() +
                         "\nLast-Active: " + instance.getDateLastActive().getTime() + "\n");
            }
        }
        return instanceIds;
    }

    public void deleteInstance(String instanceId)
            throws org.wso2.carbon.bpel.stub.mgt.InstanceManagementException, RemoteException {
        String instanceFilter = "IID=" + instanceId;
        instanceManagementServiceStub = this.setInstanceManagementStub();
        log.info("Deleting all the instances");
        int instanceCount = instanceManagementServiceStub.
                deleteInstances(instanceFilter, true);
    }


    public void clearInstancesOfProcess(String processId)
            throws InstanceManagementException, RemoteException {
        PaginatedInstanceList instanceList = filterPageInstances(processId);
        for (LimitedInstanceInfoType instanceInfo : instanceList.getInstance()) {
            deleteInstance(instanceInfo.getIid());
        }
    }

    public InstanceInfoType getInstanceInfo(String instanceId)
            throws RemoteException, InstanceManagementException {
        instanceManagementServiceStub = this.setInstanceManagementStub();
        InstanceInfoType instanceInfo = null;
        instanceInfo = instanceManagementServiceStub.
                getInstanceInfo(Long.parseLong(instanceId));
        return instanceInfo;
    }

    public boolean assertInstanceInfo(String status, String variableName, String expectedVarValue,
                                      List<String> instanceIds)
            throws RemoteException, InstanceManagementException {
        instanceManagementServiceStub = this.setInstanceManagementStub();
        boolean variableFound = false;
        for (String iid : instanceIds) {
            InstanceInfoType instanceInfo = instanceManagementServiceStub.
                    getInstanceInfo(Long.parseLong(iid));
            if (status != null) {
                log.info("Validating instance status, expected: " + status +
                         " actual: " + instanceInfo.getStatus());
            }
            if (variableName == null) {
                variableFound = true;
            } else {
                Variables_type0 variables = instanceInfo.getRootScope().getVariables();
                VariableInfoType[] variableList = variables.getVariableInfo();
                for (VariableInfoType variable : variableList) {
                    String varName = variable.getSelf().getName();
                    String varValue = null;
                    for (OMElement varElement : variable.getValue().getExtraElement()) {
                        if (varValue == null) {
                            varValue = varElement.toString();
                        } else {
                            varValue += varElement.toString();
                        }

                        if (variableName != null && expectedVarValue != null) {
                            if (varName.equals(variableName)) {
                                if (varValue.contains(expectedVarValue)) {
                                    variableFound = true;
                                } else {
                                    log.info("Incorrect Test Result: " + varValue +
                                             " Expected" + expectedVarValue + "in the result");
                                }
                            }
                        } else {
                            variableFound = true;
                        }
                        log.info("Variable name: " + varName + "\nVariable Value: " +
                                 varValue);
                    }
                }
            }
        }
        return variableFound;
    }


    public void performAction(String instanceId, InstanceOperation operation)
            throws RemoteException, InstanceManagementException {
        instanceManagementServiceStub = this.setInstanceManagementStub();
        switch (operation) {
            case SUSPEND:
                instanceManagementServiceStub.suspendInstance(Long.parseLong(instanceId));
                break;
            case RESUME:
                instanceManagementServiceStub.resumeInstance(Long.parseLong(instanceId));
                break;
            case TERMINATE:
                instanceManagementServiceStub.terminateInstance(Long.parseLong(instanceId));
                break;
        }
    }

    public static enum InstanceOperation {
        SUSPEND,
        RESUME,
        TERMINATE
    }

}
