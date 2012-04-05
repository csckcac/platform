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
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.ProcessModel;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.status.ProcessModelStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class encapsulates the view of the {@link ProcessModel}s.
 * 
 * @author Gregor Latuske
 */
public class ProcessModelDataModel
	extends SelectionDataModel<ProcessInstanceDataModel, List<ProcessModel>, ProcessModel> {

	/** Serial version UID */
	private static final long serialVersionUID = -910492818453091282L;

	/** The loaded {@link ProcessModel}s and their selection status. */
	private Map<ProcessModel, Boolean> internalItems;

    /** The SVGModel to hold the representation of the processModel*/
    private SVGDataModel processModelSVGDataModel;

	/**
	 * Constructor of ProcessModelDataModel.
	 * 
	 * @param child The child data model.
	 */
	public ProcessModelDataModel(ProcessInstanceDataModel child) {
		super(child, "name", "status", ProcessModelStatus.values(), "pid", "name");

		this.internalItems = new HashMap<ProcessModel, Boolean>();

        this.processModelSVGDataModel = new SVGDataModel();
	}

	/** {@inheritDoc} */
	@Override
	public List<ProcessModel> getSelection() {
		List<ProcessModel> processModels = new ArrayList<ProcessModel>();

		for (ProcessModel processModel : this.internalItems.keySet()) {
			if (this.internalItems.get(processModel)) {
				processModels.add(processModel);
			}
		}

		return processModels;
	}

	/** {@inheritDoc} */
	@Override
	public String select() {
		ProcessModel processModel = getItems().getRowData();
        this.internalItems.clear();
        this.internalItems.put(processModel, true);

        //this.internalItems.put(processModel, !isSelected());    //TODO: Here I have changed the impl. Now once we select one process model, all the selections will be removed and add only the selected process

		return "SUCCESS";
	}

	/**
	 * Checks if the current row is selected or not.
	 * 
	 * @return True if the current row is selected.
	 */
	public boolean isSelected() {
		ProcessModel processModel = getItems().getRowData();

		return this.internalItems.get(processModel);
	}

	/** {@inheritDoc} */
	@Override
	protected List<ProcessModel> getInternalItems() {
		return new ArrayList<ProcessModel>(this.internalItems.keySet());
	}

	/** {@inheritDoc} */
	@Override
	protected void setInternalItems(List<ProcessModel> items) {
		this.internalItems.clear();

		for (ProcessModel processModel : items) {
			this.internalItems.put(processModel, true);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void updateChild() {
		List<ProcessInstance> instances = new ArrayList<ProcessInstance>();

		for (ProcessModel model : getSelection()) {
			instances.addAll(model.getProcessInstances());
		}

		getChild().update(instances);
	}

    /** {@inheritDoc} */
	@Override
    public String selectAndNotify() {
        super.selectAndNotify();

        this.processModelSVGDataModel.update(this.getSelection().get(0));

        return "";
    }

    public SVGDataModel getProcessModelSVGDataModel() {
        return this.processModelSVGDataModel;
    }

}
