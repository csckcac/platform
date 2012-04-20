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
package org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.activity;

import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.ProcessInstance;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.status.ActivityExecStatus;

import java.io.Serializable;
import java.util.Calendar;

/**
 * This class represents the execution data of an activity.
 * 
 * @author Gregor Latuske
 */
public class ActivityExecData
	implements Serializable {

	/** Serial version UID */
	private static final long serialVersionUID = 8976488300937843559L;

	/** The status of the activity. */
	private ActivityExecStatus status;

    /** Activity id of the activity */
    private String activityId;

    /** The start date of the activity (time stamp of START). */
	private Calendar startDate;

	/** The end date of the activity (time stamp of COMPLETED or FAILURE). */
	private Calendar endDate;

	/** The associated process instance. */
	private final ProcessInstance processInstance;

    /**
     * Returns the activity ID
     * @return Activity Id
     */
    public String getActivityId() {
        return activityId;
    }

	/**
	 * Constructor of ActivityExecData.
	 * 
	 * @param activityId Activity ID
     * @param status The status of the activity.
	 * @param processInstance The associated process instance.
	 */
	public ActivityExecData(String activityId, ActivityExecStatus status, ProcessInstance processInstance) {
        this.activityId = activityId;
        this.status = status;
		this.processInstance = processInstance;
	}

	/**
	 * Returns the value of status.
	 * 
	 * @return The value of status.
	 */
	public ActivityExecStatus getStatus() {
		return this.status;
	}

	/**
	 * Set the value of status to status.
	 * 
	 * @param status The new value of status.
	 */
	public void setStatus(ActivityExecStatus status) {
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
	 * Returns the value of endDate.
	 * 
	 * @return The value of endDate.
	 */
	public Calendar getEndDate() {
		return this.endDate;
	}

	/**
	 * Set the value of endDate to endDate.
	 * 
	 * @param endDate The new value of endDate.
	 */
	public void setEndDate(Calendar endDate) {
		this.endDate = endDate;
	}

	/**
	 * Returns the value of processInstance.
	 * 
	 * @return The value of processInstance.
	 */
	public ProcessInstance getProcessInstance() {
		return this.processInstance;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ActivityExecData && getProcessInstance() != null) {

			ActivityExecData data = (ActivityExecData) obj;
			return getStatus().equals(data.getStatus())
				&& getProcessInstance().equals(data.getProcessInstance());
		}

		return super.equals(obj);
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		if (getStatus() != null && getProcessInstance() != null) {
			return getStatus().hashCode() * 13 + getProcessInstance().hashCode() * 17;
		}

		return 0;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "Status: " + getStatus() + ", Process Instance: " + getProcessInstance().getIid();
	}

}
