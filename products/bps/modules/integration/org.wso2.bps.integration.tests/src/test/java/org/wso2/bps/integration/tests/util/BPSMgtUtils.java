/*
 *
 *   Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.bps.integration.tests.util;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.fail;

public class BPSMgtUtils {

    private static final Log log = LogFactory.getLog(BPSMgtUtils.class);

    private static List<String> listInstances(
            InstanceManagementServiceStub instanceManagementServiceStub,
            int expectedInstanceCount) throws InstanceManagementException, RemoteException {
        return listInstances(instanceManagementServiceStub, expectedInstanceCount, null);
    }


    public static List<String> listInstances(
            InstanceManagementServiceStub instanceManagementServiceStub,
            int expectedInstanceCount,
            String processId)
            throws InstanceManagementException, RemoteException {
        List<String> instanceIds = new ArrayList<String>();
        if (instanceManagementServiceStub == null) {
            fail("Instance management service stub is null");
        }


        String filter;
        if (processId == null) {
            filter = " ";
        } else {
            filter = "pid=" + processId;
        }
        PaginatedInstanceList paginatedInstanceList = instanceManagementServiceStub.
                getPaginatedInstanceList(filter,
                        "-started",
                        300,
                        0);
        boolean instanceFound = false;
        if (paginatedInstanceList.isInstanceSpecified() &&
                paginatedInstanceList.getInstance().length == expectedInstanceCount) {
            instanceFound = true;
            for (LimitedInstanceInfoType instance : paginatedInstanceList.getInstance()) {
                instanceIds.add(instance.getIid());
                log.info("ProcessId: " + instance.getPid() +
                        "\nInstanceId: " + instance.getIid() +
                        "\nStarted: " + instance.getDateStarted().getTime() +
                        "\nState: " + instance.getStatus() +
                        "\nLast-Active: " + instance.getDateLastActive().getTime() + "\n");
            }
        } else if (!paginatedInstanceList.isInstanceSpecified() && expectedInstanceCount == 0) {
            log.info("No instances found as expected");
            instanceFound = true;
        }
        assertFalse(!instanceFound, "Expected instance count " + expectedInstanceCount +
                " is not there in the server");


        return instanceIds;
    }

    public static void deleteInstances(InstanceManagementServiceStub instanceManagementServiceStub,
                                       int count)
            throws InstanceManagementException, RemoteException {
        if (instanceManagementServiceStub == null) {
            fail("Instance management service stub is null");
        }


        log.info("Deleting all the instances");
        int instanceCount = instanceManagementServiceStub.
                deleteInstances(" ", true);
        assertFalse(instanceCount != count, "Instance deletion failed!, deleted instance count is " + instanceCount +
                " where it should be 1");

        listInstances(instanceManagementServiceStub, 0);

    }

    public static void getInstanceInfo(InstanceManagementServiceStub instanceManagementServiceStub,
                                       String status, String variableName, String expectedVarValue,
                                       List<String> instanceIds) {
        if (instanceManagementServiceStub == null) {
            fail("Instance management service stub is null");
        }

        try {

            boolean variableFound = false;

            for (String iid : instanceIds) {
                InstanceInfoType instanceInfo = instanceManagementServiceStub.
                        getInstanceInfo(Long.parseLong(iid));
                if (status != null) {
                    log.info("Validating instance status, expected: " + status +
                            " actual: " + instanceInfo.getStatus());
                    assertFalse(!instanceInfo.getStatus().getValue().equals(status.toUpperCase()), "Status of instance " + iid + " is not equal to " + status +
                            " but " + instanceInfo.getStatus().getValue());
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
                                        fail("Incorrect Test Result: " + varValue +
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

            assertFalse(!variableFound, variableName + " variable not found");
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
            fail(axisFault.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public static void performAction(InstanceManagementServiceStub instanceManagementServiceStub,
                                     String instanceId, InstanceOperation operation,
                                     List<String> instanceIds) {
        switch (operation) {
            case SUSPEND:
                try {
                    instanceManagementServiceStub.suspendInstance(Long.parseLong(instanceId));
                    getInstanceInfo(instanceManagementServiceStub, "SUSPENDED", null, null,
                            instanceIds);
                } catch (Exception e) {
                    e.printStackTrace();
                    fail(e.getMessage());
                }
                break;
            case RESUME:
                try {
                    instanceManagementServiceStub.resumeInstance(Long.parseLong(instanceId));
                    getInstanceInfo(instanceManagementServiceStub, "ACTIVE", null, null,
                            instanceIds);
                } catch (Exception e) {
                    e.printStackTrace();
                    fail(e.getMessage());
                }
                break;
            case TERMINATE:
                try {
                    instanceManagementServiceStub.terminateInstance(Long.parseLong(instanceId));
                    getInstanceInfo(instanceManagementServiceStub, "TERMINATED", null, null,
                            instanceIds);
                } catch (Exception e) {
                    e.printStackTrace();
                    fail(e.getMessage());
                }
                break;
        }
    }

    public enum InstanceOperation {
        SUSPEND,
        RESUME,
        TERMINATE
    }
}