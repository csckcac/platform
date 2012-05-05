/*
   Copyright 2011 Jakob Krein

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
 */
package org.wso2.carbon.bpel.ui.bpel2svg.latest.wso2.adapter;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.stub.mgt.ProcessManagementException;
import org.wso2.carbon.bpel.stub.mgt.ProcessManagementServiceStub;
import org.wso2.carbon.bpel.stub.mgt.types.LimitedProcessInfoType;
import org.wso2.carbon.bpel.stub.mgt.types.PaginatedProcessInfoList;
import org.wso2.carbon.bpel.stub.mgt.types.ProcessInfoType;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.BPIException;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.ProcessModel;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.status.ProcessModelStatus;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.service.ProcessModelService;

import javax.xml.namespace.QName;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the {@link ProcessModelService}.
 */
public class ProcessModelServiceImpl implements ProcessModelService<String> {

    private static Log log = LogFactory.getLog(ProcessModelServiceImpl.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public ProcessModelStatus mapToStatus(String value) {
        /* Compare the value to all possible status values */
        for (ProcessModelStatus status : ProcessModelStatus.values()) {
            if (status.name().equalsIgnoreCase(value)) {

                /* Return the mapped status */
                return status;
            }
        }

        /* Status could not be identified so throw an exception */
        throw new IllegalArgumentException(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ProcessModel> getProcessModels() throws BPIException {

        PaginatedProcessInfoList processList;
        ArrayList<ProcessModel> processModels = new ArrayList<ProcessModel>();

        try {
            /* Retrieve the list of process models */
            processList = getPaginatedProcessList("name}}* namespace=*", "deployed name", 0);
        } catch (ProcessManagementException e) {
            throw new BPIException("Error occurred in back-end service while getting the paginated " +
                    "process list.", e);
        } catch (RemoteException e) {
            throw new BPIException("Error occurred during communication with back-end while getting " +
                    "the paginated process list.", e);
        }

        /* Transform each process model into an internal representation of the model */
        LimitedProcessInfoType[] processes = processList.getProcessInfo();
        if (processes != null) {
            for (LimitedProcessInfoType process : processes) {

                ProcessInfoType processInfo;
                try {
                    /* Get additional information for the current process model by calling the WebService */
                    processInfo = getProcessInfo(process.getPid());

                    /* Create the internal representation */
                    ProcessModel processModel = new ProcessModel(
                            process.getPid(),
                            processInfo.getDefinitionInfo().getProcessName().toString(),
                            process.getVersion(),
                            mapToStatus(process.getStatus().getValue()),
                            processInfo.getDefinitionInfo().getDefinition().getExtraElement().toString());

                    /* Add the process model to the list of process models */
                    processModels.add(processModel);
                } catch (Exception e) {
                    log.error("Error occurred in back-end service.", e);
                }
            }
        } else {
            log.error("NULL process list", new NullPointerException("NULL process list"));
        }

        return processModels;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProcessModel getProcessModel(String pid) throws BPIException {
        ProcessModel processModel = null;
        ProcessInfoType processInfo;

        try {
            /* Get additional information for the process model by calling the WebService */
            processInfo = getProcessInfo(pid);

            /* Create and internal representation */
            processModel = new ProcessModel(
                    pid,
                    processInfo.getDefinitionInfo().getProcessName().toString(),
                    processInfo.getVersion(),
                    mapToStatus(processInfo.getStatus().getValue()),
                    processInfo.getDefinitionInfo().getDefinition().getExtraElement().toString());
        } catch (ProcessManagementException e) {
            log.error("Error occurred in back-end service.", e);
        } catch (RemoteException e) {
            log.error("Error occurred during communication with back-end.", e);
        }


        return processModel;
    }

    /**
     * Retrieves the process model list by delegating the request to the {@link ProcessManagementServiceStub}
     *
     * @param filter     Only models that bypass the filter are retrieved
     * @param orderBy    The order of the list
     * @param pageNumber The number of the page that should be retrieved
     * @return A list of process models
     * @throws RemoteException If stub operation invocation fail
     * @throws org.wso2.carbon.bpel.stub.mgt.ProcessManagementException
     *                         If error occurred while
     *                         reading the process list from the backend
     */
    private PaginatedProcessInfoList getPaginatedProcessList(String filter, String orderBy,
                                                             int pageNumber)
            throws RemoteException, ProcessManagementException {
        /* Call Web-Service */
        return getStub().getPaginatedProcessList(filter, orderBy, pageNumber);
    }

    /**
     * Retrieves information about a process model by delegating the request to the
     * {@link ProcessManagementServiceStub}
     *
     * @param pid The id of the process model
     * @return A {@link ProcessInfoType} that holds information about the process model
     * @throws RemoteException If stub operation invocation fail
     * @throws org.wso2.carbon.bpel.stub.mgt.ProcessManagementException
     *                         If error occurred while
     *                         reading the process list from the backend
     */
    private ProcessInfoType getProcessInfo(String pid)
            throws RemoteException, ProcessManagementException {
        /* Call Web-Service */
        return getStub().getProcessInfo(QName.valueOf(pid));
    }

    /**
     * Creates a {@link ProcessManagementServiceStub} of the process management WebService.
     *
     * @return The {@link ProcessManagementServiceStub}
     * @throws RemoteException If stub operation invocation fail
     */
    private ProcessManagementServiceStub getStub() throws RemoteException {
        String processMgtService = "ProcessManagementService";
        String serviceURL = AuthenticationManager.getBackendServerURL() + processMgtService;

        ProcessManagementServiceStub stub = new ProcessManagementServiceStub(null, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                AuthenticationManager.getCookie());

        return stub;
    }

}
