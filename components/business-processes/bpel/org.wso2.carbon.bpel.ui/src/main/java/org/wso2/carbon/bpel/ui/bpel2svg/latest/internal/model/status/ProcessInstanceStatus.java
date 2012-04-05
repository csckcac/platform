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
 * This class represents the status of a process instance.
 * 
 * @author Gregor Latuske
 */
public enum ProcessInstanceStatus
	implements Status {

	/** The process instance is active. */
	ACTIVE,

	/** The process instance has been suspended. */
	SUSPENDED,

	/** The process instance has completed. */
	COMPLETED,

	/** The process instance was terminated. */
	TERMINATED,

	/** The process instance failed (with a fault). */
	FAILED,

	/** The process instance is erroneous. */
	ERROR;

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
	 * Returns the {@link ProcessInstanceStatus} corresponding to the given name or <code>null</code> if no
	 * {@link ProcessInstanceStatus} exists with this name.
	 * 
	 * @param statusName The name of the {@link ProcessInstanceStatus}.
	 * @return The {@link ProcessInstanceStatus} corresponding to the given name or <code>null</code> if no
	 *         {@link ProcessInstanceStatus} exists with this name.
	 */
	public static ProcessInstanceStatus convert(String statusName) {
		for (ProcessInstanceStatus status : values()) {
			if (status.name().equalsIgnoreCase(statusName)) {
				return status;
			}
		}

		return null;
	}

}
