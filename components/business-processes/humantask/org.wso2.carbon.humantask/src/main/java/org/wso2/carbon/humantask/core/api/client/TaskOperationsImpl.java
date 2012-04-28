/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.humantask.core.api.client;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.databinding.types.NCName;
import org.apache.axis2.databinding.types.URI;
import org.apache.axis2.databinding.utils.ConverterUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.humantask.client.api.IllegalAccessFault;
import org.wso2.carbon.humantask.client.api.IllegalArgumentFault;
import org.wso2.carbon.humantask.client.api.IllegalOperationFault;
import org.wso2.carbon.humantask.client.api.IllegalStateFault;
import org.wso2.carbon.humantask.client.api.RecipientNotAllowedException;
import org.wso2.carbon.humantask.client.api.TBatchResponse;
import org.wso2.carbon.humantask.client.api.TaskOperationsSkeletonInterface;
import org.wso2.carbon.humantask.client.api.types.TAttachment;
import org.wso2.carbon.humantask.client.api.types.TAttachmentInfo;
import org.wso2.carbon.humantask.client.api.types.TComment;
import org.wso2.carbon.humantask.client.api.types.TFault;
import org.wso2.carbon.humantask.client.api.types.TOrganizationalEntity;
import org.wso2.carbon.humantask.client.api.types.TPriority;
import org.wso2.carbon.humantask.client.api.types.TRenderingTypes;
import org.wso2.carbon.humantask.client.api.types.TSimpleQueryInput;
import org.wso2.carbon.humantask.client.api.types.TStatus;
import org.wso2.carbon.humantask.client.api.types.TTaskAbstract;
import org.wso2.carbon.humantask.client.api.types.TTaskAuthorisationParams;
import org.wso2.carbon.humantask.client.api.types.TTaskDetails;
import org.wso2.carbon.humantask.client.api.types.TTaskEventType;
import org.wso2.carbon.humantask.client.api.types.TTaskEvents;
import org.wso2.carbon.humantask.client.api.types.TTaskHistoryFilter;
import org.wso2.carbon.humantask.client.api.types.TTaskInstanceData;
import org.wso2.carbon.humantask.client.api.types.TTaskOperations;
import org.wso2.carbon.humantask.client.api.types.TTaskQueryResultSet;
import org.wso2.carbon.humantask.client.api.types.TTaskSimpleQueryResultSet;
import org.wso2.carbon.humantask.client.api.types.TTime;
import org.wso2.carbon.humantask.client.api.types.TUser;
import org.wso2.carbon.humantask.core.HumanTaskConstants;
import org.wso2.carbon.humantask.core.dao.*;
import org.wso2.carbon.humantask.core.engine.HumanTaskCommand;
import org.wso2.carbon.humantask.core.engine.HumanTaskEngine;
import org.wso2.carbon.humantask.core.engine.HumanTaskException;
import org.wso2.carbon.humantask.core.engine.PeopleQueryEvaluator;
import org.wso2.carbon.humantask.core.engine.commands.Activate;
import org.wso2.carbon.humantask.core.engine.commands.AddComment;
import org.wso2.carbon.humantask.core.engine.commands.Claim;
import org.wso2.carbon.humantask.core.engine.commands.Complete;
import org.wso2.carbon.humantask.core.engine.commands.Delegate;
import org.wso2.carbon.humantask.core.engine.commands.DeleteComment;
import org.wso2.carbon.humantask.core.engine.commands.DeleteFault;
import org.wso2.carbon.humantask.core.engine.commands.DeleteOutput;
import org.wso2.carbon.humantask.core.engine.commands.Fail;
import org.wso2.carbon.humantask.core.engine.commands.GetComments;
import org.wso2.carbon.humantask.core.engine.commands.GetFault;
import org.wso2.carbon.humantask.core.engine.commands.GetInput;
import org.wso2.carbon.humantask.core.engine.commands.GetOutput;
import org.wso2.carbon.humantask.core.engine.commands.GetTaskDescription;
import org.wso2.carbon.humantask.core.engine.commands.Nominate;
import org.wso2.carbon.humantask.core.engine.commands.Release;
import org.wso2.carbon.humantask.core.engine.commands.Remove;
import org.wso2.carbon.humantask.core.engine.commands.Resume;
import org.wso2.carbon.humantask.core.engine.commands.SetFault;
import org.wso2.carbon.humantask.core.engine.commands.SetOutput;
import org.wso2.carbon.humantask.core.engine.commands.SetPriority;
import org.wso2.carbon.humantask.core.engine.commands.Skip;
import org.wso2.carbon.humantask.core.engine.commands.Start;
import org.wso2.carbon.humantask.core.engine.commands.Stop;
import org.wso2.carbon.humantask.core.engine.commands.Suspend;
import org.wso2.carbon.humantask.core.engine.commands.UpdateComment;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskRuntimeException;
import org.wso2.carbon.humantask.core.engine.util.CommonTaskUtil;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;
import org.wso2.carbon.humantask.core.utils.DOMUtils;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.utils.multitenancy.CarbonContextHolder;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * The implementation of the WS Human Task API Operations.
 */
public class TaskOperationsImpl extends AbstractAdmin implements TaskOperationsSkeletonInterface {

    private static Log log = LogFactory.getLog(TaskOperationsImpl.class);

    @Override
    public TTaskSimpleQueryResultSet simpleQuery(final TSimpleQueryInput tSimpleQueryInput)
            throws IllegalStateFault, IllegalArgumentFault {

        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());

        try {
            List<TaskDAO> matchingTasks = HumanTaskServiceComponent.getHumanTaskServer().
                    getTaskEngine().getScheduler().execTransaction(new Callable<List<TaskDAO>>() {
                public List<TaskDAO> call() throws Exception {
                    HumanTaskDAOConnection daoConn = HumanTaskServiceComponent.getHumanTaskServer().
                            getDaoConnectionFactory().getConnection();
                    SimpleQueryCriteria queryCriteria = TransformerUtils.
                            transformSimpleTaskQuery(tSimpleQueryInput);
                    queryCriteria.setCallerTenantId(CarbonContext.getCurrentContext().getTenantId());
                    queryCriteria.setCaller(getCaller());
                    return daoConn.searchTasks(queryCriteria);
                }
            });

            int pageNumber = tSimpleQueryInput.getPageNumber();

            if (pageNumber < 0 || pageNumber == Integer.MAX_VALUE) {
                pageNumber = 0;
            }

            int startIndexOfCurrentPage = pageNumber * HumanTaskConstants.ITEMS_PER_PAGE;
            int endIndexOfCurrentPage = (pageNumber + 1) * HumanTaskConstants.ITEMS_PER_PAGE;
            int taskListSize = matchingTasks.size();
            int pages = (int) Math.ceil((double) taskListSize / HumanTaskConstants.ITEMS_PER_PAGE);

            TaskDAO[] instanceArray =
                    matchingTasks.toArray(new TaskDAO[taskListSize]);
            TTaskSimpleQueryResultSet resultSet = new TTaskSimpleQueryResultSet();
            resultSet.setPages(pages);
            for (int i = startIndexOfCurrentPage;
                 (i < endIndexOfCurrentPage && i < taskListSize); i++) {
                resultSet.addRow(TransformerUtils.transformToSimpleQueryRow(instanceArray[i]));
            }
            return resultSet;
        } catch (Exception ex) {
            String errMsg = "simpleQuery operation failed";
            log.error(errMsg, ex);
            throw new IllegalStateFault(errMsg, ex);
        }
    }

    @Override
    public TBatchResponse[] batchStop(URI[] uris) {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }

	private void handleUnsupportedOperation() {
        throw new UnsupportedOperationException("This operation is not currently supported in " +
                "this version of WSO2 BPS.");
    }

    @Override
    public TTaskAbstract[] getMyTaskAbstracts(String s, String s1, String s2, TStatus[] tStatuses,
                                              String s3, String s4, String s5, int i, int i1)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return new TTaskAbstract[0];
    }

    @Override
    public void stop(final URI uri) throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            validateTaskId(uri);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            HumanTaskCommand stop = new Stop(getCaller(), new Long(uri.toString()));
                            stop.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            String errMsg = "stop operation failed";
            log.error(errMsg, ex);
            throw new IllegalStateFault(errMsg, ex);
        }
    }

    @Override
    public TBatchResponse[] batchComplete(URI[] uris, OMElement o) {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }

    @Override
    public void resume(final URI uri)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            validateTaskId(uri);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            HumanTaskCommand resumeCommand =
                                    new Resume(getCaller(), new Long(uri.toString()));
                            resumeCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            String errMsg = "resume operation failed";
            log.error(errMsg, ex);
            if (ex instanceof IllegalArgumentFault) {
                throw (IllegalArgumentFault) ex;
            } else if (ex instanceof IllegalOperationFault) {
                throw (IllegalOperationFault) ex;
            } else if (ex instanceof IllegalStateFault) {
                throw (IllegalStateFault) ex;
            } else if (ex instanceof IllegalAccessFault) {
                throw (IllegalAccessFault) ex;
            } else {
                throw new IllegalStateFault(errMsg, ex);
            }
        }
    }

    @Override
    public QName[] getRenderingTypes(URI uri) throws IllegalArgumentFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return new QName[0];
    }

    @Override
    public void setTaskCompletionDeadlineExpression(URI uri, NCName ncName, String s)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        validateTaskId(uri);
        handleUnsupportedOperation();
    }

    @Override
    public void setOutput(URI uri, NCName ncName, OMElement o)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskId = validateTaskId(uri);
            if (ncName != null && o != null) {
                final String outputName = ncName.toString();
                final Element outputData = DOMUtils.getElementFromObject(o);
                HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                        execTransaction(new Callable<Object>() {
                            public Object call() throws Exception {
                                SetOutput setOutputCommand =
                                        new SetOutput(getCaller(), taskId, outputName, outputData);
                                setOutputCommand.execute();
                                return null;
                            }
                        });

            } else {
                throw new IllegalArgumentFault("The output data cannot be empty!");
            }
        } catch (Exception ex) {
            String errMsg = "setOutput operation failed";
            log.error(errMsg, ex);
            if (ex instanceof IllegalArgumentFault) {
                throw (IllegalArgumentFault) ex;
            } else if (ex instanceof IllegalOperationFault) {
                throw (IllegalOperationFault) ex;
            } else if (ex instanceof IllegalStateFault) {
                throw (IllegalStateFault) ex;
            } else if (ex instanceof IllegalAccessFault) {
                throw (IllegalAccessFault) ex;
            } else {
                throw new IllegalStateFault(errMsg, ex);
            }
        }
    }

    @Override
    public TTaskOperations getTaskOperations(URI uri)
            throws IllegalOperationFault, IllegalArgumentFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        validateTaskId(uri);
        handleUnsupportedOperation();
        return null;
    }

    @Override
    public TBatchResponse[] batchRelease(URI[] uris) {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }

    @Override
    public TTaskDetails getTaskDetails(URI uri) throws IllegalArgumentFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        validateTaskId(uri);
        handleUnsupportedOperation();
        return null;
    }

    @Override
    public void forward(URI uri, TOrganizationalEntity tOrganizationalEntity)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        validateTaskId(uri);
        handleUnsupportedOperation();
    }

    @Override
    public boolean isSubtask(URI uri)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        validateTaskId(uri);
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        // As we do not support sub task with this release.
        return false;
    }

    @Override
    public void suspend(final URI uri)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            validateTaskId(uri);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            HumanTaskCommand suspendCommand =
                                    new Suspend(getCaller(), new Long(uri.toString()));
                            suspendCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            String errMsg = "suspend operation failed";
            log.error(errMsg, ex);
            if (ex instanceof IllegalArgumentFault) {
                throw (IllegalArgumentFault) ex;
            } else if (ex instanceof IllegalOperationFault) {
                throw (IllegalOperationFault) ex;
            } else if (ex instanceof IllegalStateFault) {
                throw (IllegalStateFault) ex;
            } else if (ex instanceof IllegalAccessFault) {
                throw (IllegalAccessFault) ex;
            } else {
                throw new IllegalStateFault(errMsg, ex);
            }
        }
    }

    @Override
    public TUser[] getAssignableUserList(URI uri) throws IllegalStateFault, IllegalArgumentFault {
        final Integer tenantId = CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId();
                CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(tenantId);
        try {
            final Long taskId = validateTaskId(uri);
            return HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<TUser[]>() {
                        public TUser[] call() throws Exception {
                            HumanTaskEngine engine =
                                    HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine();
                            HumanTaskDAOConnection daoConn =
                                    engine.getDaoConnectionFactory().getConnection();
                            TaskDAO task = daoConn.getTask(taskId);
                            String roleName = CommonTaskUtil.getPotentialOwnerRoleName(task);
                            String actualOwnerUserName = null;
                            OrganizationalEntityDAO actualOwner =
                                    CommonTaskUtil.getUserEntityForRole(task,GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);
                            if (actualOwner != null) {
                                actualOwnerUserName = actualOwner.getName();
                            }

                            return getUserListForRole(roleName, tenantId, actualOwnerUserName);
                        }
                    });
        } catch (Exception ex) {
            String errMsg = "loadAuthorisationParams operation failed";
            log.error(errMsg, ex);
            if (ex instanceof IllegalArgumentFault) {
                throw (IllegalArgumentFault) ex;
            } else if (ex instanceof IllegalStateFault) {
                throw (IllegalStateFault) ex;
            } else {
                throw new IllegalStateFault(errMsg, ex);
            }
        }
    }

    @Override
    public void updateComment(final URI uri, final URI uri1, final String s)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskId = validateTaskId(uri);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            UpdateComment updateCommentCommand =
                                    new UpdateComment(getCaller(), taskId, new Long(uri1.toString()), s);
                            updateCommentCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            String errMsg = "updateComment operation failed";
            log.error(errMsg, ex);
            if (ex instanceof IllegalArgumentFault) {
                throw (IllegalArgumentFault) ex;
            } else if (ex instanceof IllegalOperationFault) {
                throw (IllegalOperationFault) ex;
            } else if (ex instanceof IllegalStateFault) {
                throw (IllegalStateFault) ex;
            } else if (ex instanceof IllegalAccessFault) {
                throw (IllegalAccessFault) ex;
            } else {
                throw new IllegalStateFault(errMsg, ex);
            }
        }
    }

    @Override
    public TTaskAbstract loadTask(URI uri) throws IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskId = validateTaskId(uri);
            TaskDAO task = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<TaskDAO>() {
                        public TaskDAO call() throws Exception {
                            HumanTaskEngine engine = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine();
                            HumanTaskDAOConnection daoConn = engine.getDaoConnectionFactory().getConnection();
                            return daoConn.getTask(taskId);
                        }
                    });
            return TransformerUtils.transformTask(task, getCaller());
        }  catch (Exception ex) {
            String errMsg = "loadTask operation failed";
            log.error(errMsg, ex);
            if (ex instanceof IllegalAccessFault) {
                throw (IllegalAccessFault) ex;
            } else {
                throw new IllegalAccessFault(errMsg, ex);
            }
        }
    }

    @Override
    public TTaskDetails[] getMyTaskDetails(String s, String s1, String s2, TStatus[] tStatuses,
                                           String s3, String s4, String s5, int i, int i1)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return new TTaskDetails[0];
    }

    @Override
    public TBatchResponse[] batchNominate(URI[] uris) {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }

    @Override
    public URI[] getSubtaskIdentifiers(URI uri)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return new URI[0];
    }

    @Override
    public String getOutcome(URI uri) throws IllegalOperationFault, IllegalArgumentFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        validateTaskId(uri);
        handleUnsupportedOperation();
        return null;
    }

    @Override
    public Object getRendering(URI uri, QName qName) throws IllegalArgumentFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        validateTaskId(uri);
        handleUnsupportedOperation();
        return null;
    }

    @Override
    public void skip(URI uri) throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskId = validateTaskId(uri);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            Skip skipCommand = new Skip(getCaller(), taskId);
                            skipCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            String errMsg = "skip operation failed";
            log.error(errMsg, ex);
            if (ex instanceof IllegalArgumentFault) {
                throw (IllegalArgumentFault) ex;
            } else if (ex instanceof IllegalOperationFault) {
                throw (IllegalOperationFault) ex;
            } else if (ex instanceof IllegalStateFault) {
                throw (IllegalStateFault) ex;
            } else if (ex instanceof IllegalAccessFault) {
                throw (IllegalAccessFault) ex;
            } else {
                throw new IllegalStateFault(errMsg, ex);
            }
        }
    }

    @Override
    public TBatchResponse[] batchFail(URI[] uris, TFault tFault) {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }

    @Override
    public void setTaskCompletionDurationExpression(URI uri, NCName ncName, String s)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
    }

    @Override
    public void start(final URI uri)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            validateTaskId(uri);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            Start startCommand = new Start(getCaller(), new Long(uri.toString()));
                            startCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            String errMsg = "start operation failed";
            log.error(errMsg, ex);
            if (ex instanceof IllegalArgumentFault) {
                throw (IllegalArgumentFault) ex;
            } else if (ex instanceof IllegalOperationFault) {
                throw (IllegalOperationFault) ex;
            } else if (ex instanceof IllegalStateFault) {
                throw (IllegalStateFault) ex;
            } else if (ex instanceof IllegalAccessFault) {
                throw (IllegalAccessFault) ex;
            } else {
                throw new IllegalStateFault(errMsg, ex);
            }
        }
    }

    @Override
    public void fail(final URI uri, final TFault tFault)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskID = validateTaskId(uri);

            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            String faultName = null;
                            Element faultData = null;
                            if (tFault != null) {
                                faultName = tFault.getFaultName().toString();
                                faultData = DOMUtils.getElementFromObject(tFault.getFaultData());
                            }
                            Fail failCommand = new Fail(getCaller(), taskID, faultName, faultData);
                            failCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            String errMsg = "fail operation failed";
            log.error(errMsg, ex);
            if (ex instanceof IllegalArgumentFault) {
                throw (IllegalArgumentFault) ex;
            } else if (ex instanceof IllegalOperationFault) {
                throw (IllegalOperationFault) ex;
            } else if (ex instanceof IllegalStateFault) {
                throw (IllegalStateFault) ex;
            } else if (ex instanceof IllegalAccessFault) {
                throw (IllegalAccessFault) ex;
            } else {
                throw new IllegalStateFault(errMsg, ex);
            }
        }
    }

    @Override
    public void activate(URI uri)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskID = validateTaskId(uri);

            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            Activate activateCommand = new Activate(getCaller(), taskID);
                            activateCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            String errMsg = "activate operation failed";
            log.error(errMsg, ex);
            if (ex instanceof IllegalArgumentFault) {
                throw (IllegalArgumentFault) ex;
            } else if (ex instanceof IllegalOperationFault) {
                throw (IllegalOperationFault) ex;
            } else if (ex instanceof IllegalStateFault) {
                throw (IllegalStateFault) ex;
            } else if (ex instanceof IllegalAccessFault) {
                throw (IllegalAccessFault) ex;
            } else {
                throw new IllegalStateFault(errMsg, ex);
            }
        }
    }

    @Override
    public URI addComment(final URI uri, final String s)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            validateTaskId(uri);
            Validate.notEmpty(s, "The comment string cannot be empty");
            return HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<URI>() {
                        public URI call() throws Exception {
                            AddComment addComment = new AddComment(getCaller(), new Long(uri.toString()), s);
                            addComment.execute();
                            CommentDAO persisted = addComment.getPersistedComment();
                            if (persisted != null) {
                                return ConverterUtil.convertToURI(persisted.getId().toString());
                            } else {
                                throw new IllegalStateFault("The persisted comment is null. " +
                                        "See error log for more details.");
                            }
                        }
                    });
        } catch (Exception ex) {
            String errMsg = "addComment operation failed";
            log.error(errMsg, ex);
            if (ex instanceof IllegalArgumentFault) {
                throw (IllegalArgumentFault) ex;
            } else if (ex instanceof IllegalOperationFault) {
                throw (IllegalOperationFault) ex;
            } else if (ex instanceof IllegalStateFault) {
                throw (IllegalStateFault) ex;
            } else if (ex instanceof IllegalAccessFault) {
                throw (IllegalAccessFault) ex;
            } else {
                throw new IllegalStateFault(errMsg, ex);
            }
        }
    }

    @Override
    public void deleteComment(final URI uri, final URI uri1)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskId = validateTaskId(uri);

            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            DeleteComment deleteComment =
                                    new DeleteComment(getCaller(), taskId, new Long(uri1.toString()));
                            deleteComment.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            String errMsg = "deleteComment operation failed";
            log.error(errMsg, ex);
            if (ex instanceof IllegalArgumentFault) {
                throw (IllegalArgumentFault) ex;
            } else if (ex instanceof IllegalOperationFault) {
                throw (IllegalOperationFault) ex;
            } else if (ex instanceof IllegalStateFault) {
                throw (IllegalStateFault) ex;
            } else if (ex instanceof IllegalAccessFault) {
                throw (IllegalAccessFault) ex;
            } else {
                throw new IllegalStateFault(errMsg, ex);
            }
        }
    }

    @Override
    public void delegate(final URI uri, final TOrganizationalEntity tOrganizationalEntity)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            RecipientNotAllowedException, IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            validateTaskId(uri);
            if (tOrganizationalEntity == null) {
                throw new IllegalArgumentFault("The delegatee cannot be null!");
            }
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            List<OrganizationalEntityDAO> orgEntities = TransformerUtils.
                                    transformOrganizationalEntityList(tOrganizationalEntity);
                            if (orgEntities.size() > 1) {
                                throw new IllegalArgumentFault("There can be only 1 delegatee of type user!");
                            }

                            Delegate delegateCommand = new Delegate(getCaller(),
                                    new Long(uri.toString()), orgEntities.get(0));
                            delegateCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            String errMsg = "delegate operation failed";
            log.error(errMsg, ex);
            if (ex instanceof IllegalArgumentFault) {
                throw (IllegalArgumentFault) ex;
            } else if (ex instanceof IllegalOperationFault) {
                throw (IllegalOperationFault) ex;
            } else if (ex instanceof IllegalStateFault) {
                throw (IllegalStateFault) ex;
            } else if (ex instanceof IllegalAccessFault) {
                throw (IllegalAccessFault) ex;
            } else {
                throw new IllegalStateFault(errMsg, ex);
            }
        }
    }

    @Override
    public TComment[] getComments(URI uri)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskId = validateTaskId(uri);
            return HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<TComment[]>() {
                        public TComment[] call() throws Exception {
                            GetComments getComments = new GetComments(getCaller(), taskId);
                            getComments.execute();
                            List<TComment> comments =
                                    TransformerUtils.transformComments(getComments.getComments());
                            return comments.toArray(new TComment[comments.size()]);
                        }
                    });
        } catch (Exception ex) {
            String errMsg = "getComments operation failed";
            log.error(errMsg, ex);
            if (ex instanceof IllegalArgumentFault) {
                throw (IllegalArgumentFault) ex;
            } else if (ex instanceof IllegalOperationFault) {
                throw (IllegalOperationFault) ex;
            } else if (ex instanceof IllegalStateFault) {
                throw (IllegalStateFault) ex;
            } else if (ex instanceof IllegalAccessFault) {
                throw (IllegalAccessFault) ex;
            } else {
                throw new IllegalStateFault(errMsg, ex);
            }
        }
    }

    @Override
    public TTaskInstanceData getTaskInstanceData(URI uri, String s,
                                                 TRenderingTypes[] tRenderingTypeses)
            throws IllegalOperationFault, IllegalArgumentFault, IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return null;
    }

    @Override
    public TTaskDetails getParentTask(URI uri)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return null;
    }

    @Override
    public TBatchResponse[] batchResume(URI[] uris) {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }

    @Override
    public TBatchResponse[] batchRemove(URI[] uris) {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }

    @Override
    public TAttachment[] getAttachment(URI taskIdentifier, URI attachmentIdentifier)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return new TAttachment[0];
    }

    @Override
    public boolean addAttachment(URI taskIdentifier, String name, String accessType, String contentType, Object attachment)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
                   IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        final Long taskId = validateTaskId(taskIdentifier);
        final String attachmentID = (String) attachment;

        try {
            Boolean isAdded = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine()
                        .getScheduler().execTransaction(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    HumanTaskEngine engine = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine();
                    HumanTaskDAOConnection daoConn = engine.getDaoConnectionFactory().getConnection();
                    TaskDAO taskDAO = daoConn.getTask(taskId);
                    try {
                        boolean isAdded = taskDAO.addAttachment(TransformerUtils.generateAttachmentDAOFromID(taskDAO,
                                                                                                    attachmentID));

                        if (!isAdded) {
                            throw new HumanTaskException("Attachment with id: " + attachmentID +  "was not associated " +
                                                         "task with id:" + taskId);
                        }

                        return isAdded;
                    } catch (HumanTaskException ex) {
                        String errMsg = "getAttachmentInfos operation failed. Reason: ";
                        log.error(errMsg + ex.getLocalizedMessage(), ex);
                        throw ex;
                    }
                }
            });
            return isAdded;
        } catch (Exception ex) {
            String errMsg = "addAttachment operation failed";
            log.error(errMsg, ex);
            if (ex instanceof IllegalAccessFault) {
                throw (IllegalAccessFault) ex;
            } else {
                throw new IllegalAccessFault(errMsg, ex);
            }
        }
    }

    @Override
    public TAttachmentInfo[] getAttachmentInfos(final URI taskIdentifier) throws IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());

        final Long taskId = validateTaskId(taskIdentifier);
        try {
            List<AttachmentDAO> attachmentList = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine()
                    .getScheduler().
                    execTransaction(new Callable<List<AttachmentDAO>>() {
                        public List<AttachmentDAO> call() throws Exception {
                            HumanTaskEngine engine = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine();
                            HumanTaskDAOConnection daoConn = engine.getDaoConnectionFactory().getConnection();
                            log.warn("Here we can re-use the loadTask method in the same class. But then we have to " +
                                     "depend on the DTO defined by WSDL, not the back end DAOs. So if we can refactor" +
                                     " the loadTask method, we can reuse it and avoid the db transaction take in this" +
                                     " method. Else this method cost two DB transactions.");
                            return daoConn.getTask(taskId).getAttachments();
                        }
                    });
            return TransformerUtils.transformAttachments(attachmentList);
        } catch (Exception ex) {
            String errMsg = "getAttachmentInfos operation failed";
            log.error(errMsg, ex);
            if (ex instanceof IllegalAccessFault) {
                throw (IllegalAccessFault) ex;
            } else {
                throw new IllegalAccessFault(errMsg, ex);
            }
        }
    }

    @Override
    public void remove(URI uri)
            throws IllegalOperationFault, IllegalArgumentFault, IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long notificationId = validateTaskId(uri);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            Remove removeCommand = new Remove(getCaller(), notificationId);
                            removeCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            String errMsg = "remove operation failed";
            log.error(errMsg, ex);
            if (ex instanceof IllegalArgumentFault) {
                throw (IllegalArgumentFault) ex;
            } else if (ex instanceof IllegalOperationFault) {
                throw (IllegalOperationFault) ex;
            } else if (ex instanceof IllegalAccessFault) {
                throw (IllegalAccessFault) ex;
            } else {
                throw new IllegalAccessFault(errMsg, ex);
            }
        }
    }

    @Override
    public TBatchResponse[] batchStart(URI[] uris) {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }

    @Override
    public URI instantiateSubtask(URI uri, String s)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return null;
    }

    @Override
    public TTaskAuthorisationParams loadAuthorisationParams(URI uri)
            throws IllegalStateFault, IllegalArgumentFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskId = validateTaskId(uri);
            return HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<TTaskAuthorisationParams>() {
                        public TTaskAuthorisationParams call() throws Exception {
                            HumanTaskEngine engine =
                                    HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine();
                            HumanTaskDAOConnection daoConn =
                                    engine.getDaoConnectionFactory().getConnection();
                            TaskDAO task = daoConn.getTask(taskId);
                            return TransformerUtils.transformTaskAuthorization(task, getCaller());
                        }
                    });
        } catch (Exception ex) {
            String errMsg = "loadAuthorisationParams operation failed";
            log.error(errMsg, ex);
            if (ex instanceof IllegalArgumentFault) {
                throw (IllegalArgumentFault) ex;
            } else if (ex instanceof IllegalStateFault) {
                throw (IllegalStateFault) ex;
            } else {
                throw new IllegalStateFault(errMsg, ex);
            }
        }
    }

    @Override
    public TTaskEventType[] getTaskHistory(URI uri, TTaskHistoryFilter tTaskHistoryFilter, int i,
                                           int i1, boolean b)
            throws IllegalOperationFault, IllegalArgumentFault, IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return new TTaskEventType[0];
    }

    @Override
    public void setTaskStartDeadlineExpression(URI uri, NCName ncName, String s)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
    }

    @Override
    public TTaskEvents loadTaskEvents(URI uri) throws IllegalArgumentFault, IllegalStateFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskId = validateTaskId(uri);
            return HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<TTaskEvents>() {
                        public TTaskEvents call() throws Exception {
                            HumanTaskEngine engine =
                                    HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine();
                            HumanTaskDAOConnection daoConn =
                                    engine.getDaoConnectionFactory().getConnection();
                            TaskDAO task = daoConn.getTask(taskId);
                            return TransformerUtils.transformTaskEvents(task, getCaller());
                        }
                    });
        } catch (Exception ex) {
            String errMsg = "loadAuthorisationParams operation failed";
            log.error(errMsg, ex);
            if (ex instanceof IllegalArgumentFault) {
                throw (IllegalArgumentFault) ex;
            } else if (ex instanceof IllegalStateFault) {
                throw (IllegalStateFault) ex;
            } else {
                throw new IllegalStateFault(errMsg, ex);
            }
        }
    }

    @Override
    public TBatchResponse[] batchDelegate(URI[] uris, TOrganizationalEntity tOrganizationalEntity) {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }

    @Override
    public TBatchResponse[] batchSetGenericHumanRole(URI[] uris, String s,
                                                     TOrganizationalEntity tOrganizationalEntity) {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }

    @Override
    public void setGenericHumanRole(URI uri, String s, TOrganizationalEntity tOrganizationalEntity)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        validateTaskId(uri);
        handleUnsupportedOperation();
    }

    @Override
    public Object getInput(final URI uri, final NCName ncName)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            validateTaskId(uri);
            return HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            Long taskId = validateTaskId(uri);
                            String partName = "";
                            if (ncName != null) {
                                partName = ncName.toString().trim();
                            }

                            GetInput getInput = new GetInput(getCaller(), taskId, partName);
                            getInput.execute();
                            Node input = getInput.getInputElement().getFirstChild().getFirstChild();
                            return DOMUtils.domToString(input);
                        }
                    });
        } catch (Exception ex) {
            String errMsg = "getInput operation failed";
            log.error(errMsg, ex);
            if (ex instanceof IllegalArgumentFault) {
                throw (IllegalArgumentFault) ex;
            } else if (ex instanceof IllegalOperationFault) {
                throw (IllegalOperationFault) ex;
            } else if (ex instanceof IllegalStateFault) {
                throw (IllegalStateFault) ex;
            } else if (ex instanceof IllegalAccessFault) {
                throw (IllegalAccessFault) ex;
            } else {
                throw new IllegalStateFault(errMsg, ex);
            }
        }
    }

    @Override
    public TBatchResponse[] batchSkip(URI[] uris) {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }

    @Override
    public void complete(final URI uri, final String o)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskId = validateTaskId(uri);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            Element output = DOMUtils.stringToDOM(o);
                            Complete completeCommand = new Complete(getCaller(), taskId, output);
                            completeCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            String errMsg = "complete operation failed";
            log.error(errMsg, ex);
            if (ex instanceof IllegalArgumentFault) {
                throw (IllegalArgumentFault) ex;
            } else if (ex instanceof IllegalOperationFault) {
                throw (IllegalOperationFault) ex;
            } else if (ex instanceof IllegalStateFault) {
                throw (IllegalStateFault) ex;
            } else if (ex instanceof IllegalAccessFault) {
                throw (IllegalAccessFault) ex;
            } else {
                throw new IllegalStateFault(errMsg, ex);
            }
        }
    }

    @Override
    public boolean hasSubtasks(URI uri)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        validateTaskId(uri);
        return false;
    }

    @Override
    public TBatchResponse[] batchActivate(URI[] uris) {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }

    @Override
    public void claim(final URI uri)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskId = validateTaskId(uri);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            HumanTaskCommand claim = new Claim(getCaller(), taskId);
                            claim.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            String errMsg = "claim operation failed";
            log.error(errMsg, ex);
            if (ex instanceof IllegalArgumentFault) {
                throw (IllegalArgumentFault) ex;
            } else if (ex instanceof IllegalOperationFault) {
                throw (IllegalOperationFault) ex;
            } else if (ex instanceof IllegalStateFault) {
                throw (IllegalStateFault) ex;
            } else if (ex instanceof IllegalAccessFault) {
                throw (IllegalAccessFault) ex;
            } else {
                throw new IllegalStateFault(errMsg, ex);
            }
        }
    }

    @Override
    public TTaskQueryResultSet query(String s, String s1, String s2, int i, int i1)
            throws IllegalStateFault, IllegalArgumentFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return null;
    }

    @Override
    public TBatchResponse[] batchClaim(URI[] uris) {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }

    @Override
    public TBatchResponse[] batchSetPriority(URI[] uris, TPriority tPriority) {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }

    @Override
    public void setFault(final URI uri, final TFault tFault)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskId = validateTaskId(uri);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            String faultName = null;
                            Element faultData = null;
                            if (tFault != null) {
                                faultName = tFault.getFaultName().toString();
                                faultData = DOMUtils.getElementFromObject(tFault.getFaultData());
                            }
                            SetFault setFault = new SetFault(getCaller(), taskId, faultName, faultData);
                            setFault.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            String errMsg = "setFault operation failed";
            log.error(errMsg, ex);
            if (ex instanceof IllegalArgumentFault) {
                throw (IllegalArgumentFault) ex;
            } else if (ex instanceof IllegalOperationFault) {
                throw (IllegalOperationFault) ex;
            } else if (ex instanceof IllegalStateFault) {
                throw (IllegalStateFault) ex;
            } else if (ex instanceof IllegalAccessFault) {
                throw (IllegalAccessFault) ex;
            } else {
                throw new IllegalStateFault(errMsg, ex);
            }
        }
    }

    @Override
    public void suspendUntil(URI uri, TTime tTime)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        validateTaskId(uri);
        handleUnsupportedOperation();
    }

    @Override
    public void setTaskStartDurationExpression(URI uri, NCName ncName, String s)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        validateTaskId(uri);
        handleUnsupportedOperation();
    }

    @Override
    public String getTaskDescription(final URI uri, final String s) throws IllegalArgumentFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskId = validateTaskId(uri);
            return HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<String>() {
                        public String call() throws Exception {
                            String contentType;
                            if (StringUtils.isNotEmpty(s)) {
                                contentType = s;
                            } else {
                                contentType = "text/plain";
                            }

                            GetTaskDescription taskDescriptionCommand =
                                    new GetTaskDescription(getCaller(), taskId, contentType);
                            taskDescriptionCommand.execute();
                            return taskDescriptionCommand.getTaskDescription();
                        }
                    });
        } catch (Exception ex) {
            String errMsg = "getTaskDescription operation failed";
            log.error(errMsg, ex);
            if (ex instanceof IllegalArgumentFault) {
                throw (IllegalArgumentFault) ex;
            } else {
                throw new IllegalArgumentFault(errMsg, ex);
            }
        }
    }

    @Override
    public void deleteAttachment(URI uri, URI uri1)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        validateTaskId(uri);
        handleUnsupportedOperation();
    }

    @Override
    public void nominate(final URI uri, final TOrganizationalEntity tOrganizationalEntity)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskId = validateTaskId(uri);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            List<OrganizationalEntityDAO> nominees = TransformerUtils.
                                    transformOrganizationalEntityList(tOrganizationalEntity);
                            Nominate nominateCommand = new Nominate(getCaller(), taskId, nominees);
                            nominateCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            String errMsg = "nominate operation failed";
            log.error(errMsg, ex);
            if (ex instanceof IllegalArgumentFault) {
                throw (IllegalArgumentFault) ex;
            } else if (ex instanceof IllegalOperationFault) {
                throw (IllegalOperationFault) ex;
            } else if (ex instanceof IllegalStateFault) {
                throw (IllegalStateFault) ex;
            } else if (ex instanceof IllegalAccessFault) {
                throw (IllegalAccessFault) ex;
            } else {
                throw new IllegalStateFault(errMsg, ex);
            }
        }
    }

    @Override
    public void deleteOutput(final URI uri)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskId = validateTaskId(uri);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            DeleteOutput deleteOutput = new DeleteOutput(getCaller(), taskId);
                            deleteOutput.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            String errMsg = "deleteOutput operation failed";
            log.error(errMsg, ex);
            if (ex instanceof IllegalArgumentFault) {
                throw (IllegalArgumentFault) ex;
            } else if (ex instanceof IllegalOperationFault) {
                throw (IllegalOperationFault) ex;
            } else if (ex instanceof IllegalStateFault) {
                throw (IllegalStateFault) ex;
            } else if (ex instanceof IllegalAccessFault) {
                throw (IllegalAccessFault) ex;
            } else {
                throw new IllegalStateFault(errMsg, ex);
            }
        }
    }

    @Override
    public TBatchResponse[] batchForward(URI[] uris, TOrganizationalEntity tOrganizationalEntity) {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }

    @Override
    public TBatchResponse[] batchSuspend(URI[] uris) {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }

    @Override
    public TTaskDetails[] getSubtasks(URI uri)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        validateTaskId(uri);
        handleUnsupportedOperation();
        return new TTaskDetails[0];
    }

    @Override
    public void deleteFault(final URI uri)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskId = validateTaskId(uri);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            DeleteFault deleteFaultCommand = new DeleteFault(getCaller(), taskId);
                            deleteFaultCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            String errMsg = "deleteFault operation failed";
            log.error(errMsg, ex);
            if (ex instanceof IllegalArgumentFault) {
                throw (IllegalArgumentFault) ex;
            } else if (ex instanceof IllegalOperationFault) {
                throw (IllegalOperationFault) ex;
            } else if (ex instanceof IllegalStateFault) {
                throw (IllegalStateFault) ex;
            } else if (ex instanceof IllegalAccessFault) {
                throw (IllegalAccessFault) ex;
            } else {
                throw new IllegalStateFault(errMsg, ex);
            }
        }
    }

    @Override
    public Object getOutput(final URI uri, final NCName ncName)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskId = validateTaskId(uri);
            return HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            String partName = "";
                            if (ncName != null) {
                                partName = ncName.toString().trim();
                            }
                            GetOutput getOutputCommand = new GetOutput(getCaller(), taskId, partName);
                            getOutputCommand.execute();
                            Node outputElement = getOutputCommand.getOutputData().getFirstChild();
                            try {
                                return DOMUtils.domToString(outputElement);
                            } catch (Exception e) {
                                log.error("Error occurred when converting the output to OMElement",
                                        e);
                                throw new IllegalStateFault("Error occurred when converting the " +
                                        "output to OMElement", e);
                            }
                        }
                    });
        } catch (Exception ex) {
            String errMsg = "getOutput operation failed";
            log.error(errMsg, ex);
            if (ex instanceof IllegalArgumentFault) {
                throw (IllegalArgumentFault) ex;
            } else if (ex instanceof IllegalOperationFault) {
                throw (IllegalOperationFault) ex;
            } else if (ex instanceof IllegalStateFault) {
                throw (IllegalStateFault) ex;
            } else if (ex instanceof IllegalAccessFault) {
                throw (IllegalAccessFault) ex;
            } else {
                throw new IllegalStateFault(errMsg, ex);
            }
        }
    }

    @Override
    public void release(final URI uri)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            validateTaskId(uri);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            HumanTaskCommand releaseCommand =
                                    new Release(getCaller(), new Long(uri.toString()));
                            releaseCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            String errMsg = "release operation failed";
            log.error(errMsg, ex);
            if (ex instanceof IllegalArgumentFault) {
                throw (IllegalArgumentFault) ex;
            } else if (ex instanceof IllegalOperationFault) {
                throw (IllegalOperationFault) ex;
            } else if (ex instanceof IllegalStateFault) {
                throw (IllegalStateFault) ex;
            } else if (ex instanceof IllegalAccessFault) {
                throw (IllegalAccessFault) ex;
            } else {
                throw new IllegalStateFault(errMsg, ex);
            }
        }
    }

    @Override
    public TFault getFault(URI uri)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskId = validateTaskId(uri);
            return HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<TFault>() {
                        public TFault call() throws Exception {
                            GetFault getFault = new GetFault(getCaller(), taskId);
                            getFault.execute();
                            TFault fault = new TFault();
                            if (getFault.getFaultData() != null &&
                                    StringUtils.isNotEmpty(getFault.getFaultName())) {
                                fault.setFaultName(new NCName(getFault.getFaultName()));
                                fault.setFaultData(DOMUtils.domToString(getFault.getFaultData()));
                            }
                            return fault;
                        }
                    });
        } catch (Exception ex) {
            String errMsg = "getFault operation failed";
            log.error(errMsg, ex);
            if (ex instanceof IllegalArgumentFault) {
                throw (IllegalArgumentFault) ex;
            } else if (ex instanceof IllegalOperationFault) {
                throw (IllegalOperationFault) ex;
            } else if (ex instanceof IllegalStateFault) {
                throw (IllegalStateFault) ex;
            } else if (ex instanceof IllegalAccessFault) {
                throw (IllegalAccessFault) ex;
            } else {
                throw new IllegalStateFault(errMsg, ex);
            }
        }
    }

    @Override
    public void setPriority(final URI uri, final TPriority tPriority)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskId = validateTaskId(uri);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            Integer newPriotity = tPriority.getTPriority().intValue();
                            SetPriority setPriorityCommand =
                                    new SetPriority(getCaller(), taskId, newPriotity);
                            setPriorityCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            String errMsg = "setPriority operation failed";
            log.error(errMsg, ex);
            if (ex instanceof IllegalArgumentFault) {
                throw (IllegalArgumentFault) ex;
            } else if (ex instanceof IllegalOperationFault) {
                throw (IllegalOperationFault) ex;
            } else if (ex instanceof IllegalStateFault) {
                throw (IllegalStateFault) ex;
            } else if (ex instanceof IllegalAccessFault) {
                throw (IllegalAccessFault) ex;
            } else {
                throw new IllegalStateFault(errMsg, ex);
            }
        }
    }

    @Override
    public TBatchResponse[] batchSuspendUntil(URI[] uris, TTime tTime) {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }

    @Override
    public URI getParentTaskIdentifier(URI uri)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return null;

    }

    // Validates the provided task ID URI and returns it in case of a valid ID.
    private Long validateTaskId(URI taskId) {
        if (taskId == null || StringUtils.isEmpty(taskId.toString())) {
            throw new IllegalArgumentException("The task id cannot be null or empty");
        }

        try {
            return Long.valueOf(taskId.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The task id must be a number", e);
        }
    }

    private String getCaller() {
        // TODO - remove hard coded user name value once moved to task view page.
        String userName = "admin";

        PeopleQueryEvaluator pqe = HumanTaskServiceComponent.getHumanTaskServer().
                getTaskEngine().getPeopleQueryEvaluator();

        if (StringUtils.isNotEmpty(pqe.getLoggedInUser())) {
            userName = pqe.getLoggedInUser();
        }

        // We cannot perform any task operation without resolving the user name of the currently
        // logged in user.
        if (StringUtils.isEmpty(userName)) {
            throw new HumanTaskRuntimeException("Cannot determine the user name of the user " +
                    "performing the task operation!");
        }

        return userName;
    }


    private TUser[] getUserListForRole(String roleName, Integer tenantId, String actualOwnerUserName)
            throws RegistryException, UserStoreException {
        TUser[] userList = new TUser[0];

        RegistryService registryService = HumanTaskServiceComponent.getRegistryService();
        if(registryService != null && registryService.getUserRealm(tenantId) != null) {
            UserRealm userRealm = registryService.getUserRealm(tenantId);
            String[] assignableUserNameList = userRealm.getUserStoreManager().getUserListOfRole(roleName);
            if(assignableUserNameList != null) {
                userList = new TUser[assignableUserNameList.length];
                for(int i= 0 ; i < assignableUserNameList.length ; i++) {
                    TUser user  = new TUser();
                    user.setTUser(assignableUserNameList[i]);
                    if(StringUtils.isEmpty(actualOwnerUserName)) {
                        userList[i] = user;
                    } else if(StringUtils.isNotEmpty(actualOwnerUserName) &&
                              !actualOwnerUserName.equals(assignableUserNameList[i])) {
                        userList[i] = user;
                    }
                }
            }
        } else {
            log.warn("Cannot load User Realm for Tenant Id: " + tenantId);
        }
        return userList;
    }
}