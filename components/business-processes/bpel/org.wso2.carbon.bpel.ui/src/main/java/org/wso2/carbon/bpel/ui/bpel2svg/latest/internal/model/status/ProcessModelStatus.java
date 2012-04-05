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
 * This class represents the status of a process model.
 *
 * @author Gregor Latuske
 */
public enum ProcessModelStatus
	implements Status {

	/** The Process is accepting new requests. */
	ACTIVE,

	/** The Process is not accepting new requests. */
	RETIRED;

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
	 * Returns the {@link ProcessModelStatus} corresponding to the given name or <code>null</code> if no
	 * {@link ProcessModelStatus} exists with this name.
	 *
	 * @param statusName The name of the {@link ProcessModelStatus}.
	 * @return The {@link ProcessModelStatus} corresponding to the given name or <code>null</code> if no
	 *         {@link ProcessModelStatus} exists with this name.
	 */
	public static ProcessModelStatus convert(String statusName) {
		for (ProcessModelStatus status : values()) {
			if (status.name().equalsIgnoreCase(statusName)) {
				return status;
			}
		}

		return null;
	}

}
