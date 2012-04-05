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
package org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.model;

import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.model.components.ViewMode;

import java.io.Serializable;

/**
 * This class is the basis for all data models.
 * 
 * @author Gregor Latuske
 */
public abstract class AbstractDataModel<C extends AbstractDataModel<?, ?>, U> /*implements Serializable*/ {

	/** Serial version UID */
	private static final long serialVersionUID = -5400679683329201972L;

	/** The child data model. */
	private final C child;

	/** The view mode of the data model (maximise & minimise). */
	private final ViewMode viewMode;

	/**
	 * Constructor of AbstractDataModel.
	 * 
	 * @param child The child data model.
	 */
	public AbstractDataModel(C child) {
		this.child = child;
		this.viewMode = new ViewMode();
	}

	/**
	 * Updates the data model (the internal values) and notifies the observers.
	 * 
	 * @param data The new data for the data model.
	 */
	public abstract void update(U data);

	/**
	 * Returns the value of childDataModel.
	 * 
	 * @return The value of childDataModel.
	 */
	public C getChild() {
		return child;
	}

	/**
	 * Returns the value of viewMode.
	 * 
	 * @return The value of viewMode.
	 */
	public ViewMode getViewMode() {
		return this.viewMode;
	}

}
