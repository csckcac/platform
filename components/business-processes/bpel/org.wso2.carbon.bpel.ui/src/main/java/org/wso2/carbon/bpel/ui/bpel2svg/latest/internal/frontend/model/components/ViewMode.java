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
package org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.model.components;

/**
 * This class is used to minimise or maximise the window of the data model.
 * This class has been adapted for the WSO2 Carbon version to work without JavaServer Faces.
 * 
 * @author Gregor Latuske
 * @author Jakob Krein
 */
public class ViewMode {

	/** Is the view maximised? */
	private boolean maximised;

	/**
	 * Constructor of ViewMode.
	 */
	public ViewMode() {
		this(true);
	}

	/**
	 * Constructor of ViewMode.
	 * 
	 * @param maximised Is the view maximised?
	 */
	public ViewMode(boolean maximised) {
        setMaximised(maximised);
	}

	/**
	 * Returns the value of maximised.
	 * 
	 * @return The value of maximised.
	 */
	public boolean isMaximised() {
		return this.maximised;
	}

	/**
	 * Sets the value of maximised to maximised.
	 * 
	 * @param maximised The new value of maximised.
	 */
	public void setMaximised(boolean maximised) {
		this.maximised = maximised;
	}

	public void processAction()  {
        setMaximised(!this.maximised);
	}

}
