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
package org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.status;

/**
 * This class represents the status of the execution of an activity.
 * 
 * @author Gregor Latuske
 */
public enum ActivityExecStatus
	implements Status {

	/**
	 * The activity is outstanding.
	 * <p>
	 * (Or there does not exist any information about the status of the activity.)
	 */
	OUTSTANDING,

	/** The activity was skipped. */
	SKIPPED,

	/** The activity is enabled, but not necessarily ready to execute. */
	ENABLED,

	/** The activity has started execution. */
	STARTED,

	/** The activity is in failure state, recovery required. */
	FAILURE,

	/** The activity is recovering from failure. */
	RECOVERY,

	/** The activity has finished execution. */
	COMPLETED;

	/** {@inheritDoc} */
	public String getName() {
		String name = name().toLowerCase();
		char firstChar = Character.toUpperCase(name.charAt(0));

		return firstChar + name.substring(1);
	}

	/** {@inheritDoc} */
	public String getNameLowerCase() {
		return name().toLowerCase();
	}

	/**
	 * Returns the {@link ActivityExecStatus} corresponding to the given name or <code>null</code> if no
	 * {@link ActivityExecStatus} exists with this name.
	 * 
	 * @param statusName The name of the {@link ActivityExecStatus}.
	 * @return The {@link ActivityExecStatus} corresponding to the given name or <code>null</code> if no
	 *         {@link ActivityExecStatus} exists with this name.
	 */
	public static ActivityExecStatus convert(String statusName) {
		for (ActivityExecStatus status : values()) {
			if (status.name().equalsIgnoreCase(statusName)) {
				return status;
			}
		}

		return null;
	}

}
