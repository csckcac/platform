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

/**
 * This exception encapsulates all other used exceptions.
 *
 * @author Gregor Latuske
 */
public class BPIException
	extends Exception {

	/** Serial version UID */
	private static final long serialVersionUID = 8141521328665159527L;

	/**
	 * Constructor of DefaultException.
	 *
	 * @param throwable The {@link Exception} to encapsulate.
	 */
	public BPIException(Throwable throwable) {
		super(throwable);
	}

    public BPIException(String cause, Throwable throwable) {
		super(cause, throwable);
	}

}
