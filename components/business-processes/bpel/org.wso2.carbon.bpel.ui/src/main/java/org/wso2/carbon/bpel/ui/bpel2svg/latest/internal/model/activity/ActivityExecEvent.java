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
 * This class represents the execution event of an activity.
 * 
 * @author Gregor Latuske
 */
public class ActivityExecEvent
	implements Serializable {

	/** Serial version UID */
	private static final long serialVersionUID = -7996583904358363531L;

	/** The name of the associated activity. */
	private final String name;

    /** The id of the associated activity */
    private final String activityId;

	/** The status of the activity (respectively the type of the event). */
	private final ActivityExecStatus status;

	/** The date of the event. */
	private final Calendar date;

	/** The associated process instance. */
	private final ProcessInstance processInstance;

	/**
	 * Constructor of Activity.
	 *
	 * @param name The name of the associated activity.
	 * @param status The status of the activity (respectively the type of the event).
	 * @param date The date of the event.
	 * @param processInstance The associated process instance.
	 */
	public ActivityExecEvent(String name, String activityId, ActivityExecStatus status, Calendar date,
		ProcessInstance processInstance) {
		this.name = name;
        this.activityId = activityId;
		this.status = status;
		this.date = date;
		this.processInstance = processInstance;
	}

    /**
     * Returns id of the associated activity
     * @return
     */
    public String getActivityId() {
        return activityId;
    }

	/**
	 * Returns the value of name.
	 *
	 * @return The value of name.
	 */
	public String getName() {
		return this.name;
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
	 * Returns the value of date.
	 * 
	 * @return The value of date.
	 */
	public Calendar getDate() {
		return this.date;
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
		if (obj != null && obj instanceof ActivityExecEvent && getName() != null && getStatus() != null
			&& getProcessInstance() != null) {

			ActivityExecEvent evt = (ActivityExecEvent) obj;
			return getName().equals(evt.getName()) && getStatus().equals(evt.getStatus())
				&& getProcessInstance().equals(evt.getProcessInstance());
		}

		return false;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		if (getName() != null && getStatus() != null && getProcessInstance() != null) {
			return getName().hashCode() * 7 + getStatus().hashCode() * 13 + getProcessInstance().hashCode()
				* 17;
		}

		return 0;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "Name: " + getName() + ", Status: " + getStatus() + ", Date: " + getDate().getTime()
			+ ", Process Instance: " + getProcessInstance().getIid();
	}

}
