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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.databinding.types.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.TaskOperationsStub;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TComment;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TSimpleQueryInput;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TTaskAbstract;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TTaskAuthorisationParams;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TTaskSimpleQueryResultSet;
import org.wso2.carbon.humantask.ui.constants.HumanTaskUIConstants;

import javax.xml.stream.XMLStreamException;
import java.rmi.RemoteException;

/**
 * The client service class to call the back end taskOperationsService.
 */
public class HumanTaskClientAPIServiceClient {

    /**
     * Class logger
     */
    private static Log log = LogFactory.getLog(HumanTaskPackageManagementServiceClient.class);

    /**
     * Task Operations stub.
     */
    private TaskOperationsStub stub;

    private HumanTaskClientAPIServiceClient() {
    }

    /**
     * The class constructor.
     *
     * @param cookie           :
     * @param backendServerURL : The back end server URL.
     * @param configContext    : The axis configuration context.
     * @throws org.apache.axis2.AxisFault : If the client creation fails.
     */
    public HumanTaskClientAPIServiceClient(
            String cookie,
            String backendServerURL,
            ConfigurationContext configContext) throws AxisFault {
        String serviceURL = backendServerURL +
                            HumanTaskUIConstants.SERVICE_NAMES.TASK_OPERATIONS_SERVICE;
        stub = new TaskOperationsStub(configContext, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    /**
     * Lists the tasks matching the provided simple query object.
     *
     * @param queryInput : The simple query object with the filtering criteria.
     * @return : The result set
     * @throws java.rmi.RemoteException :
     * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault
     *                                  :
     * @throws org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault
     *                                  :
     */
    public TTaskSimpleQueryResultSet taskListQuery(TSimpleQueryInput queryInput)
            throws RemoteException, IllegalArgumentFault, IllegalStateFault {
        try {
            return stub.simpleQuery(queryInput);
        } catch (RemoteException e) {
            log.error("Error occurred while performing taskListQuery operation", e);
            throw e;
        } catch (IllegalStateFault illegalStateFault) {
            log.error("Error occurred while performing taskListQuery operation", illegalStateFault);
            throw illegalStateFault;
        } catch (IllegalArgumentFault illegalArgumentFault) {
            log.error("Error occurred while performing taskListQuery operation", illegalArgumentFault);
            throw illegalArgumentFault;
        }
    }

    /**
     * Load task data for the give task id.
     *
     * @param taskId :
     * @return :
     * @throws RemoteException    :
     * @throws IllegalAccessFault :
     */
    public TTaskAbstract loadTask(URI taskId) throws RemoteException, IllegalAccessFault {

        try {
            return stub.loadTask(taskId);
        } catch (RemoteException e) {
            log.error("Error occurred while performing loadTask operation", e);
            throw e;
        } catch (IllegalAccessFault illegalAccessFault) {
            log.error("Error occurred while performing loadTask operation", illegalAccessFault);
            throw illegalAccessFault;
        }
    }

    /**
     * Task complete operation.
     *
     * @param taskId  : The task id to be completed.
     * @param payLoad : The payload.
     * @throws RemoteException       :
     * @throws IllegalAccessFault    :
     * @throws IllegalArgumentFault  :
     * @throws IllegalStateFault     :
     * @throws IllegalOperationFault :
     * @throws XMLStreamException    :
     */
    public void complete(URI taskId, String payLoad)
            throws RemoteException, IllegalAccessFault, IllegalArgumentFault, IllegalStateFault,
                   IllegalOperationFault, XMLStreamException {
        try {
            stub.complete(taskId, payLoad);
        } catch (RemoteException e) {
            log.error("Error occurred while performing complete operation", e);
            throw e;
        } catch (IllegalAccessFault illegalAccessFault) {
            log.error("Error occurred while performing complete operation", illegalAccessFault);
            throw illegalAccessFault;
        } catch (IllegalArgumentFault e) {
            log.error("Error occurred while performing complete operation", e);
            throw e;
        } catch (IllegalStateFault e) {
            log.error("Error occurred while performing complete operation", e);
            throw e;
        } catch (IllegalOperationFault e) {
            log.error("Error occurred while performing complete operation", e);
            throw e;
        }
    }

    /**
     * Loads the task input.
     *
     * @param taskId : The id of the task/.
     * @return : The task input OMElement.
     * @throws RemoteException        :
     * @throws IllegalStateFault      :
     * @throws IllegalOperationFault:
     * @throws IllegalAccessFault:
     * @throws IllegalArgumentFault:
     * @throws javax.xml.stream.XMLStreamException
     *                                :
     */
    public OMElement loadTaskInput(URI taskId)
            throws RemoteException, IllegalStateFault, IllegalOperationFault, IllegalAccessFault,
                   IllegalArgumentFault, XMLStreamException {
        try {
            String input = (String) stub.getInput(taskId, null);
            return AXIOMUtil.stringToOM(input);
        } catch (RemoteException e) {
            log.error("Error occurred while performing loadTaskInput operation", e);
            throw e;
        } catch (IllegalStateFault e) {
            log.error("Error occurred while performing loadTaskInput operation", e);
            throw e;
        } catch (IllegalOperationFault e) {
            log.error("Error occurred while performing loadTaskInput operation", e);
            throw e;
        } catch (IllegalArgumentFault e) {
            log.error("Error occurred while performing loadTaskInput operation", e);
            throw e;
        } catch (IllegalAccessFault e) {
            log.error("Error occurred while performing loadTaskInput operation", e);
            throw e;
        } catch (XMLStreamException e) {
            log.error("Error occurred while performing loadTaskInput operation", e);
            throw e;
        }
    }


    /**
     * Loads the task output.
     *
     * @param taskId : The id of the task/.
     * @return : The task input OMElement.
     * @throws RemoteException        :
     * @throws IllegalStateFault      :
     * @throws IllegalOperationFault:
     * @throws IllegalAccessFault:
     * @throws IllegalArgumentFault:
     * @throws javax.xml.stream.XMLStreamException
     *                                :
     */
    public OMElement loadTaskOutput(URI taskId)
            throws RemoteException, IllegalStateFault, IllegalOperationFault, IllegalAccessFault,
                   IllegalArgumentFault, XMLStreamException {
        try {
            String output = (String) stub.getOutput(taskId, null);
            return AXIOMUtil.stringToOM(output);
        } catch (RemoteException e) {
            log.error("Error occurred while performing loadTaskOutput operation", e);
            throw e;
        } catch (IllegalStateFault e) {
            log.error("Error occurred while performing loadTaskOutput operation", e);
            throw e;
        } catch (IllegalOperationFault e) {
            log.error("Error occurred while performing loadTaskOutput operation", e);
            throw e;
        } catch (IllegalArgumentFault e) {
            log.error("Error occurred while performing loadTaskOutput operation", e);
            throw e;
        } catch (IllegalAccessFault e) {
            log.error("Error occurred while performing loadTaskOutput operation", e);
            throw e;
        } catch (XMLStreamException e) {
            log.error("Error occurred while performing loadTaskOutput operation", e);
            throw e;
        }
    }

    /**
     * Claim task operation.
     *
     * @param taskId : The ID of the task to be claimed.
     * @throws IllegalArgumentFault  :
     * @throws IllegalAccessFault    :
     * @throws IllegalStateFault     :
     * @throws RemoteException       :
     * @throws IllegalOperationFault :
     */
    public void claim(URI taskId) throws IllegalArgumentFault, IllegalAccessFault,
                                         IllegalStateFault, RemoteException, IllegalOperationFault {
        try {
            stub.claim(taskId);
        } catch (RemoteException e) {
            log.error("Error occurred while performing claim operation", e);
            throw e;
        } catch (IllegalStateFault e) {
            log.error("Error occurred while performing claim operation", e);
            throw e;
        } catch (IllegalOperationFault e) {
            log.error("Error occurred while performing claim operation", e);
            throw e;
        } catch (IllegalArgumentFault e) {
            log.error("Error occurred while performing claim operation", e);
            throw e;
        } catch (IllegalAccessFault e) {
            log.error("Error occurred while performing claim operation", e);
            throw e;
        }
    }

    /**
     * Loads the task authorisation parameters for UI functionality.
     *
     * @param taskId : The task Id.
     * @return : The task authorisation parameters.
     * @throws RemoteException      :
     * @throws IllegalStateFault    :
     * @throws IllegalArgumentFault :
     */
    public TTaskAuthorisationParams getTaskParams(URI taskId)
            throws RemoteException, IllegalStateFault, IllegalArgumentFault {

        try {
            return stub.loadAuthorisationParams(taskId);
        } catch (RemoteException e) {
            log.error("Error occurred while performing getTaskParams operation", e);
            throw e;
        } catch (IllegalStateFault e) {
            log.error("Error occurred while performing getTaskParams operation", e);
            throw e;
        } catch (IllegalArgumentFault e) {
            log.error("Error occurred while performing getTaskParams operation", e);
            throw e;
        }
    }

    public void start(URI taskId)
            throws RemoteException, IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
                   IllegalAccessFault {
        try {
            stub.start(taskId);
        } catch (RemoteException e) {
            log.error("Error occurred while performing start operation", e);
            throw e;
        } catch (IllegalStateFault e) {
            log.error("Error occurred while performing start operation", e);
            throw e;
        } catch (IllegalOperationFault e) {
            log.error("Error occurred while performing start operation", e);
            throw e;
        } catch (IllegalArgumentFault e) {
            log.error("Error occurred while performing start operation", e);
            throw e;
        } catch (IllegalAccessFault e) {
            log.error("Error occurred while performing start operation", e);
            throw e;
        }
    }

    /**
     * Stop task.
     *
     * @param taskId : The task Id.
     * @throws RemoteException       :
     * @throws IllegalStateFault     :
     * @throws IllegalOperationFault :
     * @throws IllegalArgumentFault  :
     * @throws IllegalAccessFault    :
     */
    public void stop(URI taskId)
            throws RemoteException, IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
                   IllegalAccessFault {
        try {
            stub.stop(taskId);
        } catch (RemoteException e) {
            log.error("Error occurred while performing stop operation", e);
            throw e;
        } catch (IllegalStateFault e) {
            log.error("Error occurred while performing stop operation", e);
            throw e;
        } catch (IllegalOperationFault e) {
            log.error("Error occurred while performing stop operation", e);
            throw e;
        } catch (IllegalArgumentFault e) {
            log.error("Error occurred while performing stop operation", e);
            throw e;
        } catch (IllegalAccessFault e) {
            log.error("Error occurred while performing stop operation", e);
            throw e;
        }
    }

    /**
     * Release task.
     *
     * @param taskId : The task id.
     * @throws RemoteException       :
     * @throws IllegalStateFault     :
     * @throws IllegalOperationFault :
     * @throws IllegalArgumentFault  :
     * @throws IllegalAccessFault    :
     */
    public void release(URI taskId)
            throws RemoteException, IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
                   IllegalAccessFault {
        try {
            stub.release(taskId);
        } catch (RemoteException e) {
            log.error("Error occurred while performing release operation", e);
            throw e;
        } catch (IllegalStateFault e) {
            log.error("Error occurred while performing release operation", e);
            throw e;
        } catch (IllegalOperationFault e) {
            log.error("Error occurred while performing release operation", e);
            throw e;
        } catch (IllegalArgumentFault e) {
            log.error("Error occurred while performing release operation", e);
            throw e;
        } catch (IllegalAccessFault e) {
            log.error("Error occurred while performing release operation", e);
            throw e;
        }
    }

    /**
     * Gets the list of comments associated with a given task.
     *
     * @param taskId : The task id.
     * @return : The comments of the task.
     * @throws RemoteException       :
     * @throws IllegalStateFault:
     * @throws IllegalOperationFault :
     * @throws IllegalArgumentFault  :
     * @throws IllegalAccessFault    :
     */
    public TComment[] getComments(URI taskId)
            throws RemoteException, IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
                   IllegalAccessFault {
        try {
            return stub.getComments(taskId);
        } catch (RemoteException e) {
            log.error("Error occurred while performing get comments operation", e);
            throw e;
        } catch (IllegalStateFault e) {
            log.error("Error occurred while performing get comments operation", e);
            throw e;
        } catch (IllegalOperationFault e) {
            log.error("Error occurred while performing get comments operation", e);
            throw e;
        } catch (IllegalArgumentFault e) {
            log.error("Error occurred while performing get comments operation", e);
            throw e;
        } catch (IllegalAccessFault e) {
            log.error("Error occurred while performing get comments operation", e);
            throw e;
        }
    }

    /**
     * Add the given comment string to the task.
     *
     * @param taskId      : The id of the task.
     * @param commentText : The comment text.
     * @return : The id of the persisted comment.
     * @throws RemoteException       :
     * @throws IllegalStateFault     :
     * @throws IllegalOperationFault :
     * @throws IllegalArgumentFault  :
     * @throws IllegalAccessFault    :
     */
    public URI addComment(URI taskId, String commentText)
            throws RemoteException, IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
                   IllegalAccessFault {
        try {
            return stub.addComment(taskId, commentText);
        } catch (RemoteException e) {
            log.error("Error occurred while performing add comment operation", e);
            throw e;
        } catch (IllegalStateFault e) {
            log.error("Error occurred while performing add comment operation", e);
            throw e;
        } catch (IllegalOperationFault e) {
            log.error("Error occurred while performing add comment operation", e);
            throw e;
        } catch (IllegalArgumentFault e) {
            log.error("Error occurred while performing add comment operation", e);
            throw e;
        } catch (IllegalAccessFault e) {
            log.error("Error occurred while performing add comment operation", e);
            throw e;
        }
    }

    /**
     * Delete comment client operation.
     *
     * @param taskId    : The task which the comment belongs to.
     * @param commentId : The comment to be deleted.
     * @throws RemoteException       :
     * @throws IllegalStateFault     :
     * @throws IllegalOperationFault :
     * @throws IllegalArgumentFault  :
     * @throws IllegalAccessFault    :
     */
    public void deleteComment(URI taskId, URI commentId)
            throws RemoteException, IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
                   IllegalAccessFault {

        try {
            stub.deleteComment(taskId, commentId);
        } catch (RemoteException e) {
            log.error("Error occurred while performing delete comment operation", e);
            throw e;
        } catch (IllegalStateFault e) {
            log.error("Error occurred while performing delete comment  operation", e);
            throw e;
        } catch (IllegalOperationFault e) {
            log.error("Error occurred while performing delete comment  operation", e);
            throw e;
        } catch (IllegalArgumentFault e) {
            log.error("Error occurred while performing delete comment  operation", e);
            throw e;
        } catch (IllegalAccessFault e) {
            log.error("Error occurred while performing delete comment  operation", e);
            throw e;
        }
    }

    /**
     * Suspend task operation.
     * @param taskId : The task to be suspended.
     * @throws RemoteException :
     * @throws IllegalStateFault :
     * @throws IllegalOperationFault :
     * @throws IllegalArgumentFault :
     * @throws IllegalAccessFault :
     */
    public void suspend(URI taskId)
            throws RemoteException, IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
                   IllegalAccessFault {
        try {
            stub.suspend(taskId);
        } catch (RemoteException e) {
            log.error("Error occurred while performing suspend operation", e);
            throw e;
        } catch (IllegalStateFault e) {
            log.error("Error occurred while performing suspend operation", e);
            throw e;
        } catch (IllegalOperationFault e) {
            log.error("Error occurred while performing suspend operation", e);
            throw e;
        } catch (IllegalArgumentFault e) {
            log.error("Error occurred while performing suspend operation", e);
            throw e;
        } catch (IllegalAccessFault e) {
            log.error("Error occurred while performing suspend operation", e);
            throw e;
        }
    }

    /**
     * Resume task operation.
     *
     * @param taskId : The task id.
     * @throws RemoteException :
     * @throws IllegalStateFault :
     * @throws IllegalOperationFault :
     * @throws IllegalArgumentFault :
     * @throws IllegalAccessFault :
     */
    public void resume(URI taskId)
            throws RemoteException, IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
                   IllegalAccessFault {
        try {
            stub.resume(taskId);
        } catch (RemoteException e) {
            log.error("Error occurred while performing resume operation", e);
            throw e;
        } catch (IllegalStateFault e) {
            log.error("Error occurred while performing resume operation", e);
            throw e;
        } catch (IllegalOperationFault e) {
            log.error("Error occurred while performing resume operation", e);
            throw e;
        } catch (IllegalArgumentFault e) {
            log.error("Error occurred while performing resume operation", e);
            throw e;
        } catch (IllegalAccessFault e) {
            log.error("Error occurred while performing resume operation", e);
            throw e;
        }
    }
}
