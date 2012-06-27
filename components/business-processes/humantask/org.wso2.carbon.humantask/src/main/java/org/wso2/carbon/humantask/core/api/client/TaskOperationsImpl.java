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
import org.wso2.carbon.humantask.client.api.*;
import org.wso2.carbon.humantask.client.api.types.*;
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
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskIllegalAccessException;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskIllegalArgumentException;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskIllegalOperationException;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskIllegalStateException;
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
import java.util.List;
import java.util.concurrent.Callable;

/**
 * The implementation of the WS Human Task API Operations.
 */
public class TaskOperationsImpl extends AbstractAdmin
        implements HumanTaskClientAPIAdminSkeletonInterface {

    private static Log log = LogFactory.getLog(TaskOperationsImpl.class);

    @Override
    public TTaskSimpleQueryResultSet simpleQuery(final TSimpleQueryInput tSimpleQueryInput)
            throws IllegalStateFault, IllegalArgumentFault {

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
        } catch (HumanTaskIllegalStateException ex) {
            log.error(ex);
            throw new IllegalStateFault(ex);
        } catch (Exception ex) {
            log.error(ex);
            throw new IllegalArgumentFault(ex);
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
    public void stop(final URI taskId) throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            validateTaskId(taskId);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            HumanTaskCommand stop = new Stop(getCaller(), new Long(taskId.toString()));
                            stop.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    @Override
    public TBatchResponse[] batchComplete(URI[] taskIds, OMElement o) {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }

    @Override
    public void resume(final URI taskId)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            validateTaskId(taskId);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            HumanTaskCommand resumeCommand =
                                    new Resume(getCaller(), new Long(taskId.toString()));
                            resumeCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    @Override
    public QName[] getRenderingTypes(URI taskId) throws IllegalArgumentFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return new QName[0];
    }

    @Override
    public void setTaskCompletionDeadlineExpression(URI taskId, NCName ncName, String s)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        validateTaskId(taskId);
        handleUnsupportedOperation();
    }

    @Override
    public void setOutput(URI taskIdURI, NCName ncName, OMElement o)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskId = validateTaskId(taskIdURI);
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
            handleException(ex);
        }
    }

    @Override
    public TTaskOperations getTaskOperations(URI taskId)
            throws IllegalOperationFault, IllegalArgumentFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        validateTaskId(taskId);
        handleUnsupportedOperation();
        return null;
    }

    @Override
    public TBatchResponse[] batchRelease(URI[] taskId) {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }

    @Override
    public TTaskDetails getTaskDetails(URI taskId) throws IllegalArgumentFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        validateTaskId(taskId);
        handleUnsupportedOperation();
        return null;
    }

    @Override
    public void forward(URI taskId, TOrganizationalEntity tOrganizationalEntity)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        validateTaskId(taskId);
        handleUnsupportedOperation();
    }

    @Override
    public boolean isSubtask(URI taskId)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        validateTaskId(taskId);
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        // As we do not support sub task with this release.
        return false;
    }

    @Override
    public void suspend(final URI taskId)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            validateTaskId(taskId);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            HumanTaskCommand suspendCommand =
                                    new Suspend(getCaller(), new Long(taskId.toString()));
                            suspendCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    @Override
    public TUser[] getAssignableUserList(URI taskIdURI) throws IllegalStateFault, IllegalArgumentFault {
        final int tenantId = CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId();
                CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(tenantId);
        try {
            final Long taskId = validateTaskId(taskIdURI);
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
        } catch (HumanTaskIllegalStateException ex) {
            log.error(ex);
            throw new IllegalStateFault(ex);
        } catch (Exception ex) {
            log.error(ex);
            throw new IllegalArgumentFault(ex);
        }
    }

    @Override
    public void updateComment(final URI taskIdURI, final URI commentId, final String s)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskId = validateTaskId(taskIdURI);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            UpdateComment updateCommentCommand =
                                    new UpdateComment(getCaller(), taskId, new Long(commentId.toString()), s);
                            updateCommentCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    @Override
    public TTaskAbstract loadTask(URI taskIdURI) throws IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskId = validateTaskId(taskIdURI);
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
            log.error(ex);
            throw new IllegalAccessFault(ex);
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
    public void skip(URI taskIdURI) throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskId = validateTaskId(taskIdURI);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            Skip skipCommand = new Skip(getCaller(), taskId);
                            skipCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    @Override
    public TBatchResponse[] batchFail(URI[] taskIds, TFault tFault) {
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
    public void start(final URI taskId)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            validateTaskId(taskId);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            Start startCommand = new Start(getCaller(), new Long(taskId.toString()));
                            startCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    @Override
    public void fail(final URI taskIdURI, final TFault tFault)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskID = validateTaskId(taskIdURI);

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
            handleException(ex);
        }
    }

    @Override
    public void activate(URI taskIdURI)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskID = validateTaskId(taskIdURI);

            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            Activate activateCommand = new Activate(getCaller(), taskID);
                            activateCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    @Override
    public URI addComment(final URI taskIdURI, final String commentString)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            validateTaskId(taskIdURI);
            Validate.notEmpty(commentString, "The comment string cannot be empty");
            return HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<URI>() {
                        public URI call() throws Exception {
                            AddComment addComment = new AddComment(getCaller(), new Long(taskIdURI.toString()), commentString);
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
            handleException(ex);
        }
        return null;
    }

    @Override
    public void deleteComment(final URI taskIdURI, final URI commentId)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskId = validateTaskId(taskIdURI);

            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            DeleteComment deleteComment =
                                    new DeleteComment(getCaller(), taskId, new Long(commentId.toString()));
                            deleteComment.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    @Override
    public void delegate(final URI taskId, final TOrganizationalEntity delegatee)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            RecipientNotAllowedException, IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            validateTaskId(taskId);
            if (delegatee == null) {
                throw new IllegalArgumentFault("The delegatee cannot be null!");
            }
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            List<OrganizationalEntityDAO> orgEntities = TransformerUtils.
                                    transformOrganizationalEntityList(delegatee);
                            if (orgEntities.size() > 1) {
                                throw new IllegalArgumentFault("There can be only 1 delegatee of type user!");
                            }

                            Delegate delegateCommand = new Delegate(getCaller(),
                                    new Long(taskId.toString()), orgEntities.get(0));
                            delegateCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    @Override
    public TComment[] getComments(URI taskIdURI)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskId = validateTaskId(taskIdURI);
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
            handleException(ex);
        }
        return null;
    }

    @Override
    public TTaskInstanceData getTaskInstanceData(URI taskId, String s,
                                                 TRenderingTypes[] tRenderingTypeses)
            throws IllegalOperationFault, IllegalArgumentFault, IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return null;
    }

    @Override
    public TTaskDetails getParentTask(URI taskId)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return null;
    }

    @Override
    public TBatchResponse[] batchResume(URI[] taskIds) {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }

    @Override
    public TBatchResponse[] batchRemove(URI[] taskIds) {
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
            handleException(ex);
        }
        return false;
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
                            return daoConn.getTask(taskId).getAttachments();
                        }
                    });
            return TransformerUtils.transformAttachments(attachmentList);
        } catch (Exception ex) {
            log.error(ex);
            throw new IllegalAccessFault(ex);
        }
    }

    @Override
    public void remove(URI taskId)
            throws IllegalOperationFault, IllegalArgumentFault, IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long notificationId = validateTaskId(taskId);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            Remove removeCommand = new Remove(getCaller(), notificationId);
                            removeCommand.execute();
                            return null;
                        }
                    });
        } catch (HumanTaskIllegalOperationException ex) {
            log.error(ex);
            throw new IllegalOperationFault(ex);
        } catch (HumanTaskIllegalAccessException ex) {
            log.error(ex);
            throw new IllegalAccessFault(ex);
        } catch (Exception ex) {
            log.error(ex);
            throw new IllegalArgumentFault(ex);
        }
    }

    @Override
    public TBatchResponse[] batchStart(URI[] taskIds) {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }

    @Override
    public URI instantiateSubtask(URI taskId, String s)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return null;
    }

    @Override
    public TTaskAuthorisationParams loadAuthorisationParams(URI taskIdURI)
            throws IllegalStateFault, IllegalArgumentFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskId = validateTaskId(taskIdURI);
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
        } catch (HumanTaskIllegalArgumentException ex) {
            log.error(ex);
            throw new IllegalArgumentFault(ex);
        } catch (Exception ex) {
            log.error(ex);
            throw new IllegalStateFault(ex);
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
    public TTaskEvents loadTaskEvents(URI taskIdURI)
            throws IllegalArgumentFault, IllegalStateFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskId = validateTaskId(taskIdURI);
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
        } catch (HumanTaskIllegalArgumentException ex) {
            log.error(ex);
            throw new IllegalArgumentFault(ex);
        } catch (Exception ex) {
            log.error(ex);
            throw new IllegalStateFault(ex);
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
    public Object getInput(final URI taskIdURI, final NCName inputIdentifier)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            validateTaskId(taskIdURI);
            return HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            Long taskId = validateTaskId(taskIdURI);
                            String partName = "";
                            if (inputIdentifier != null) {
                                partName = inputIdentifier.toString().trim();
                            }

                            GetInput getInput = new GetInput(getCaller(), taskId, partName);
                            getInput.execute();
                            Node input = getInput.getInputElement();
                            return DOMUtils.domToString(input);
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }

    @Override
    public TBatchResponse[] batchSkip(URI[] taskIds) {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }

    @Override
    public void complete(final URI taskIdURI, final String outputStr)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskId = validateTaskId(taskIdURI);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            Element output = DOMUtils.stringToDOM(outputStr);
                            Complete completeCommand = new Complete(getCaller(), taskId, output);
                            completeCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    @Override
    public boolean hasSubtasks(URI taskId)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        validateTaskId(taskId);
        return false;
    }

    @Override
    public TBatchResponse[] batchActivate(URI[] taskIds) {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }

    @Override
    public void claim(final URI taskIdURI)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskId = validateTaskId(taskIdURI);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            HumanTaskCommand claim = new Claim(getCaller(), taskId);
                            claim.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
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
    public void setFault(final URI taskIdURI, final TFault tFault)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskId = validateTaskId(taskIdURI);
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
            handleException(ex);
        }
    }

    @Override
    public void suspendUntil(URI taskId, TTime suspendUntilTime)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        validateTaskId(taskId);
        handleUnsupportedOperation();
    }

    @Override
    public void setTaskStartDurationExpression(URI taskId, NCName ncName, String s)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        validateTaskId(taskId);
        handleUnsupportedOperation();
    }

    @Override
    public String getTaskDescription(final URI taskIdURI, final String contentTypeStr) throws IllegalArgumentFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskId = validateTaskId(taskIdURI);
            return HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<String>() {
                        public String call() throws Exception {
                            String contentType;
                            if (StringUtils.isNotEmpty(contentTypeStr)) {
                                contentType = contentTypeStr;
                            } else {
                                contentType = "text/plain";
                            }

                            GetTaskDescription taskDescriptionCommand =
                                    new GetTaskDescription(getCaller(), taskId, contentType);
                            taskDescriptionCommand.execute();
                            return taskDescriptionCommand.getTaskDescription();
                        }
                    });
        } catch (HumanTaskIllegalArgumentException ex) {
            log.error(ex);
            throw new IllegalArgumentFault(ex);
        } catch (Exception ex) {
            log.error(ex);
            throw new IllegalArgumentFault(ex);
        }
    }

    @Override
    public void deleteAttachment(URI taskId, URI attachmentId)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        validateTaskId(taskId);
        handleUnsupportedOperation();
    }

    @Override
    public void nominate(final URI taskIdURI, final TOrganizationalEntity nomineeOrgEntity)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskId = validateTaskId(taskIdURI);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            List<OrganizationalEntityDAO> nominees = TransformerUtils.
                                    transformOrganizationalEntityList(nomineeOrgEntity);
                            Nominate nominateCommand = new Nominate(getCaller(), taskId, nominees);
                            nominateCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    @Override
    public void deleteOutput(final URI taskIdURI)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskId = validateTaskId(taskIdURI);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            DeleteOutput deleteOutput = new DeleteOutput(getCaller(), taskId);
                            deleteOutput.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    @Override
    public TBatchResponse[] batchForward(URI[] taskIds, TOrganizationalEntity tOrganizationalEntity) {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }

    @Override
    public TBatchResponse[] batchSuspend(URI[] taskIds) {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }

    @Override
    public TTaskDetails[] getSubtasks(URI taskId)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        validateTaskId(taskId);
        handleUnsupportedOperation();
        return new TTaskDetails[0];
    }

    @Override
    public void deleteFault(final URI taskIdURI)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskId = validateTaskId(taskIdURI);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            DeleteFault deleteFaultCommand = new DeleteFault(getCaller(), taskId);
                            deleteFaultCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    @Override
    public Object getOutput(final URI taskIdURI, final NCName partNCName)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskId = validateTaskId(taskIdURI);
            return HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            String partName = "";
                            if (partNCName != null) {
                                partName = partNCName.toString().trim();
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
            handleException(ex);
        }
        return null;
    }

    @Override
    public void release(final URI taskId)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            validateTaskId(taskId);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            HumanTaskCommand releaseCommand =
                                    new Release(getCaller(), new Long(taskId.toString()));
                            releaseCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    @Override
    public TFault getFault(URI taskIdURI)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        try {
            final Long taskId = validateTaskId(taskIdURI);
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
            handleException(ex);
        }

        return null;
    }

    @Override
    public void setPriority(final URI taskIdURI, final TPriority tPriority)
            throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());

        if (tPriority.getTPriority().intValue() < 1 || tPriority.getTPriority().intValue() > 10) {
            log.warn("The priority value should be between 1 and 10. " +
                     "Hence ignoring the provided priority :" + tPriority.getTPriority());
        }

        try {
            final Long taskId = validateTaskId(taskIdURI);
            HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().getScheduler().
                    execTransaction(new Callable<Object>() {
                        public Object call() throws Exception {
                            int newPriority = tPriority.getTPriority().intValue();
                            SetPriority setPriorityCommand =
                                    new SetPriority(getCaller(), taskId, newPriority);
                            setPriorityCommand.execute();
                            return null;
                        }
                    });
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    @Override
    public TBatchResponse[] batchSuspendUntil(URI[] taskIds, TTime suspendUntilTime) {
        CarbonContextHolder.getThreadLocalCarbonContextHolder().setTenantId(CarbonContextHolder.
                getCurrentCarbonContextHolder().getTenantId());
        handleUnsupportedOperation();
        return new TBatchResponse[0];
    }

    @Override
    public URI getParentTaskIdentifier(URI taskId)
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


    private TUser[] getUserListForRole(String roleName, int tenantId, String actualOwnerUserName)
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


    private void handleException(Exception ex) throws IllegalStateFault, IllegalOperationFault, IllegalArgumentFault,
            IllegalAccessFault {
        log.error(ex);

        if(ex instanceof HumanTaskIllegalAccessException) {
            throw new IllegalAccessFault(ex);
        } else if(ex instanceof HumanTaskIllegalArgumentException) {
            throw new  IllegalArgumentFault(ex);
        } else if (ex instanceof HumanTaskIllegalOperationException) {
            throw new IllegalOperationFault(ex);
        } else if (ex instanceof HumanTaskIllegalStateException) {
            throw new  IllegalStateFault(ex);
        }  else {
            throw new IllegalStateFault(ex);
        }
    }
}