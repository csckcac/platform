/*
* Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.carbon.humantask.ui.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.humantask.stub.mgt.HumanTaskPackageManagementStub;
import org.wso2.carbon.humantask.stub.mgt.PackageManagementException;
import org.wso2.carbon.humantask.stub.mgt.types.DeployedTaskDefinitionsPaginated;
import org.wso2.carbon.humantask.stub.mgt.types.Task_type0;
import org.wso2.carbon.humantask.stub.mgt.types.UndeployStatus_type0;
import org.wso2.carbon.humantask.ui.constants.HumanTaskUIConstants;

import java.rmi.RemoteException;

/**
 * Human task package management client to call the HumanTask package management service stub.
 */
public class HumanTaskPackageManagementServiceClient {

    /**
     * Class logger
     */
    private static Log log = LogFactory.getLog(HumanTaskPackageManagementServiceClient.class);

    /**
     * The stub
     */
    private HumanTaskPackageManagementStub stub;

    /**
     * @param cookie           :
     * @param backendServerURL : The back end server URL.
     * @param configContext    : The axis configuration context.
     * @throws AxisFault : If the client creation fails.
     */
    public HumanTaskPackageManagementServiceClient(
            String cookie,
            String backendServerURL,
            ConfigurationContext configContext) throws AxisFault {
        String serviceURL = backendServerURL +
                            HumanTaskUIConstants.SERVICE_NAMES.HUMANTASK_MANAGEMENT_SERVICE;
        stub = new HumanTaskPackageManagementStub(configContext, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    /**
     * Returns the deployed package list for the given page number.
     *
     * @param page : The page number
     * @return : The packages list
     * @throws RemoteException            :
     * @throws PackageManagementException :
     */
    /*public DeployedPackagesPaginated getPaginatedPackageList(int page)
            throws RemoteException, PackageManagementException {
        try {
            return stub.listDeployedPackagesPaginated(page);
        } catch (RemoteException e) {
            log.error("listDeployedPackagesPaginated operation invocation failed.", e);
            throw e;
        } catch (PackageManagementException e) {
            log.error("listDeployedPackagesPaginated operation failed.", e);
            throw e;
        }
    } */

    /**
     * Returns the deployed package list for the given page number.
     *
     * @param page : The page number
     * @return : The task definitions list
     * @throws RemoteException            :
     * @throws PackageManagementException :
     */
    public DeployedTaskDefinitionsPaginated getPaginatedTaskDefinitions(int page)
            throws RemoteException, PackageManagementException {
        try {
            return stub.listDeployedTaskDefinitionsPaginated(page);
        } catch (RemoteException e) {
            log.error("listDeployedTaskDefinitionsPaginated operation invocation failed.", e);
            throw e;
        } catch (PackageManagementException e) {
            log.error("listDeployedTaskDefinitionsPaginated operation failed.", e);
            throw e;
        }
    }

    /**
     * Undeploys the given package name.
     *
     * @param packageName : The name of the package to be undeployed.
     * @return : The undeploy status.
     * @throws RemoteException            :
     * @throws PackageManagementException :
     */
    public UndeployStatus_type0 unDeployPackage(String packageName)
            throws RemoteException, PackageManagementException {
        try {
            return stub.undeployHumanTaskPackage(packageName);
        } catch (RemoteException e) {
            log.error("listDeployedPackagesPaginated operation invocation failed.", e);
            throw e;
        } catch (PackageManagementException e) {
            log.error("listDeployedPackagesPaginated operation failed.", e);
            throw e;
        }
    }

    /**
     * Get a list of tasks available in a given package.
     *
     * @param packageName : The package name
     * @return : The task array.
     * @throws RemoteException            :
     * @throws PackageManagementException :
     */
    public Task_type0[] listTasksInPackage(String packageName)
            throws RemoteException, PackageManagementException {
        try {
            return stub.listTasksInPackage(packageName);
        } catch (RemoteException e) {
            log.error("listDeployedPackagesPaginated operation invocation failed.", e);
            throw e;
        } catch (PackageManagementException e) {
            log.error("listDeployedPackagesPaginated operation failed.", e);
            throw e;
        }
    }
}
