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

import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.ProcessInstance;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.status.ProcessInstanceStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * This class encapsulates the view of the {@link ProcessInstance}s.
 * 
 * @author Gregor Latuske
 */
public class ProcessInstanceDataModel
	extends SelectionDataModel<SVGDataModel, List<ProcessInstance>, ProcessInstance> {

	/** Serial version UID */
	private static final long serialVersionUID = 4633844681663989246L;

	/** The loaded {@link ProcessInstance}s. */
	private List<ProcessInstance> internalItems;

	/** The selected process instance. */
	private ProcessInstance selection;

	/**
	 * Constructor of ProcessInstanceDataModel.
	 * 
	 * @param child The child data model.
	 */
	public ProcessInstanceDataModel(SVGDataModel child) {
		super(child, "processModelName", "status", ProcessInstanceStatus.values(), "iid", "processModelName");

		this.internalItems = new ArrayList<ProcessInstance>();
	}

	/** {@inheritDoc} */
	@Override
	public List<ProcessInstance> getSelection() {
		List<ProcessInstance> instances = new ArrayList<ProcessInstance>();
		instances.add(this.selection);

		return instances;
	}

	/** {@inheritDoc} */
	@Override
	public String select() {
		this.selection = (ProcessInstance) getItems().getRowData();

		return "SUCCESS";
	}

	/** {@inheritDoc} */
	@Override
	protected List<ProcessInstance> getInternalItems() {
		return this.internalItems;
	}

	/** {@inheritDoc} */
	@Override
	protected void setInternalItems(List<ProcessInstance> items) {
		this.internalItems = items;
		this.selection = null;
	}

	/** {@inheritDoc} */
	@Override
	protected void updateChild() {
		getChild().update(getSelection().get(0));
	}

}
