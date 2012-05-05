/*
   Copyright 2010 Gregor Latuske

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
*/
package org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model;

import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.status.ProcessInstanceStatus;

import java.io.Serializable;
import java.util.Calendar;

/**
 * This class represents an instance of a process model.
 */
public class ProcessInstance
        implements Serializable, ProcessItem<ProcessInstanceStatus> {

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 1165451215587859305L;

    /**
     * The ID of the process instance.
     */
    private final String iid;

    /**
     * The status of the process instance.
     */
    private ProcessInstanceStatus status;

    /**
     * The start date of the process instance.
     */
    private Calendar startDate;

    /**
     * The date of the last activity of the process instance.
     */
    private Calendar lastActivityDate;

    /**
     * The date since the process instance is in error status.
     */
    private Calendar errorSinceDate;

    /**
     * The associated process model.
     */
    private final ProcessModel processModel;

    /**
     * Constructor of ProcessInstance.
     *
     * @param iid            The ID of the process instance.
     * @param status         The status of the process instance.
     * @param startDate      The date of the last activity of the process instance.
     * @param lastActiveDate The date of the last activity of the process instance.
     * @param processModel   The associated process model.
     */
    public ProcessInstance(String iid, ProcessInstanceStatus status, Calendar startDate,
                           Calendar lastActiveDate, ProcessModel processModel) {
        this(iid, status, startDate, lastActiveDate, null, processModel);
    }

    /**
     * Constructor of ProcessInstance.
     *
     * @param iid            The ID of the process instance.
     * @param status         The status of the process instance.
     * @param startDate      The date of the last activity of the process instance.
     * @param lastActiveDate The date of the last activity of the process instance.
     * @param errorSinceDate The date since the process instance is in error status.
     * @param processModel   The associated process model.
     */
    public ProcessInstance(String iid, ProcessInstanceStatus status, Calendar startDate,
                           Calendar lastActiveDate, Calendar errorSinceDate, ProcessModel processModel) {
        this.iid = iid;
        this.status = status;
        this.startDate = startDate;
        this.lastActivityDate = lastActiveDate;
        this.errorSinceDate = errorSinceDate;
        this.processModel = processModel;
    }

    /**
     * Returns the name of the associated {@link ProcessModel}.
     *
     * @return The name of the associated {@link ProcessModel}.
     */
    public String getProcessModelName() {
        String name = "";

        if (this.processModel != null) {
            name = this.processModel.getName();
        }

        return name;
    }

    /**
     * Returns the value of iid.
     *
     * @return The value of iid.
     */
    public String getIid() {
        return this.iid;
    }

    /**
     * Returns the value of status.
     *
     * @return The value of status.
     */
    public ProcessInstanceStatus getStatus() {
        return this.status;
    }

    /**
     * Set the value of status to status.
     *
     * @param status The new value of status.
     */
    public void setStatus(ProcessInstanceStatus status) {
        this.status = status;
    }

    /**
     * Returns the value of startDate.
     *
     * @return The value of startDate.
     */
    public Calendar getStartDate() {
        return this.startDate;
    }

    /**
     * Set the value of startDate to startDate.
     *
     * @param startDate The new value of startDate.
     */
    public void setStartDate(Calendar startDate) {
        this.startDate = startDate;
    }

    /**
     * Returns the value of lastActiveDate.
     *
     * @return The value of lastActiveDate.
     */
    public Calendar getLastActivityDate() {
        return this.lastActivityDate;
    }

    /**
     * Set the value of lastActiveDate to lastActiveDate.
     *
     * @param lastActiveDate The new value of lastActiveDate.
     */
    public void setLastActivityDate(Calendar lastActiveDate) {
        this.lastActivityDate = lastActiveDate;
    }

    /**
     * Returns the value of errorSinceDate.
     *
     * @return The value of errorSinceDate.
     */
    public Calendar getErrorSinceDate() {
        return this.errorSinceDate;
    }

    /**
     * Set the value of errorSinceDate to errorSinceDate.
     *
     * @param errorSinceDate The new value of errorSinceDate.
     */
    public void setErrorSinceDate(Calendar errorSinceDate) {
        this.errorSinceDate = errorSinceDate;
    }

    /**
     * Returns the value of processModel.
     *
     * @return The value of processModel.
     */
    public ProcessModel getProcessModel() {
        return this.processModel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProcessInstance && getIid() != null && getProcessModel() != null) {
            ProcessInstance instance = (ProcessInstance) obj;
            return getIid().equals(instance.getIid()) && getProcessModel().equals(instance.getProcessModel());
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        if (getIid() != null && getProcessModel() != null) {
            return getIid().hashCode() * 13 + getProcessModel().hashCode() * 17;
        }

        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "IID: " + getIid() + ", Status: " + getStatus() + ", Process Model ("
                + getProcessModel().getPid() + ")";
    }
}
