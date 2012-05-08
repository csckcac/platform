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

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.stub.mgt.InstanceManagementException;
import org.wso2.carbon.bpel.stub.mgt.InstanceManagementServiceStub;
import org.wso2.carbon.bpel.stub.mgt.types.InstanceInfoType;
import org.wso2.carbon.bpel.stub.mgt.types.LimitedInstanceInfoType;
import org.wso2.carbon.bpel.stub.mgt.types.PaginatedInstanceList;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.BPIException;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.ProcessInstance;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.ProcessModel;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.status.ProcessInstanceStatus;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.service.ProcessInstanceService;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.service.ProcessModelService;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.wso2.service.InstanceNotFoundException;
import org.wso2.carbon.bpel.ui.clients.InstanceManagementServiceClient;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the {@link ProcessInstanceService}.
 */
public class ProcessInstanceServiceImpl implements ProcessInstanceService<String> {

    private static Log log = LogFactory.getLog(ProcessInstanceServiceImpl.class);

    public ProcessInstance getProcessInstance(String instanceId) {
        try {
            InstanceInfoType instanceBean = getInstanceInfo(Long.parseLong(instanceId));

            //Retrieved the ProcessModel from the ProcessID
            ProcessModelService pmService =
                    new ProcessModelService.ProcessModelServiceFactory().createService();
            ProcessModel model = pmService.getProcessModel(instanceBean.getPid());

            return new ProcessInstance(instanceBean.getIid(),
                    mapToStatus(instanceBean.getStatus().toString()), instanceBean.getDateStarted(),
                    instanceBean.getDateLastActive(), model);

        } catch (Exception e) {
            throw new InstanceNotFoundException("Process instance could not found for id:" +
                    instanceId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProcessInstanceStatus mapToStatus(String value) {
        /* Compare the value to all possible status values */
        for (ProcessInstanceStatus status : ProcessInstanceStatus.values()) {
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
    public List<ProcessInstance> getProcessInstances(List<ProcessModel> processModels)
            throws BPIException {

        ArrayList<ProcessInstance> processInstances = new ArrayList<ProcessInstance>();

        try {

            int currentPage = 0;
            int maxPage;
            do {
                /* Retrieve the list of process instances */
                PaginatedInstanceList instanceList = getPaginatedInstanceList("", "", 200, currentPage);
                LimitedInstanceInfoType[] instances = instanceList.getInstance();
                maxPage = instanceList.getPages();

                if (instances != null) {
                    /* Transform each process instance into an internal representation of the instance */
                    for (LimitedInstanceInfoType instance : instances) {
                        for (ProcessModel processModel : processModels) {
                            if (processModel.getPid().equals(instance.getPid())) {

                                /*
                                                * We need this WebService-call for every instance to get the dateErrorSince-value.
                                                * The rest of the instance-info is available without this call (maybe avoidable in
                                                * future releases)
                                                */
                                InstanceInfoType instanceInfo = getInstanceInfo(Long.parseLong(instance.getIid()));

                                /* Create an the internal representation */
                                ProcessInstance processInstance = new ProcessInstance(
                                        instance.getIid(),
                                        mapToStatus(instance.getStatus().toString()),
                                        instance.getDateStarted(),
                                        instance.getDateLastActive(),
                                        instanceInfo.getDateErrorSince(),
                                        processModel);

                                /* Add the process instance to the list of process instances */
                                processInstances.add(processInstance);
                            }
                        }
                    }
                }
                currentPage++;

            } while (currentPage < maxPage);

        } catch (Exception e) {
            log.error("An error occurred while communication with the back end.", e);
        }

        return processInstances;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ProcessInstance> getProcessInstances(ProcessModel processModel)
            throws BPIException {

        ArrayList<ProcessModel> processModels = new ArrayList<ProcessModel>();
        processModels.add(processModel);

        return getProcessInstances(processModels);
    }

    /**
     * Retrieves the process instance list by delegating the request to the {@link InstanceManagementServiceStub}
     *
     * @param filter Only instances that bypass the filter are retrieved
     * @param order  The order of the list
     * @param limit  The limit of instances that should be retrieved
     * @param page   The number of the page that should be retrieved
     * @return A list of process instances
     * @throws RemoteException If stub operation invocation fail
     * @throws org.wso2.carbon.bpel.stub.mgt.InstanceManagementException
     *                         If error occurred while
     *                         reading the process list from the backend
     */
    private PaginatedInstanceList getPaginatedInstanceList(String filter, String order, int limit,
                                                           int page)
            throws Exception {
        /* Call WebService */
        return getClient().getPaginatedInstanceList(filter, order, limit, page);
    }

    /**
     * Retrieves information about an instance by delegating the request to the {@link InstanceManagementServiceStub}
     *
     * @param iid The id of the instance
     * @return An {@link InstanceInfoType} that holds information about the instance
     * @throws RemoteException If stub operation invocation fail
     * @throws org.wso2.carbon.bpel.stub.mgt.InstanceManagementException
     *                         If error occurred while
     *                         reading the process list from the backend
     */
    private InstanceInfoType getInstanceInfo(long iid) throws Exception {
        /* Call WebService */
        return getClient().getInstanceInfo(iid);
    }

    /**
     * Creates an {@link InstanceManagementServiceClient} of the process instances management WebService.
     *
     * @return The {@link InstanceManagementServiceClient}
     * @throws AxisFault If stub creation operation fails
     */
    private InstanceManagementServiceClient getClient() throws AxisFault {
        InstanceManagementServiceClient client = new InstanceManagementServiceClient(AuthenticationManager.getCookie
                (), AuthenticationManager.getBackendServerURL(), AuthenticationManager.getConfigContext());
        return client;
    }

}
