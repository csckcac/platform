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

import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.model.SelectionDataModel;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.status.Status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class gives the {@link SelectionDataModel} filtering abilities for the status of a
 * {@link org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.ProcessModel} or
 * {@link org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.ProcessInstance}.
 * This class has been adapted for the WSO2 Carbon version to work without JavaServer Faces.
 */
public class StatusFilter
        extends Filter<Status> {

    /**
     * The selectable status values.
     */
    private final List<Status> statues;

    /**
     * Constructor of StatusFilter.
     *
     * @param dataModel      The associated SeelctionDataModel.
     * @param filterProperty The filter property.
     */
    public StatusFilter(SelectionDataModel<?, ?, ?> dataModel, String filterProperty) {
        super(dataModel, filterProperty);
        this.statues = new ArrayList<Status>();
    }

    public void addStatuses(Status[] statues) {
        Collections.addAll(this.statues, statues);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        setInput(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean filter(Object object) {
        if (object instanceof Status) {

            // Statuses are not equal --> add to list
            Status status = (Status) object;
            if (!status.equals(getInput())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the list of selectable statuses.
     *
     * @return The list of selectable statuses.
     */
//	public List<SelectItem> getItems() {
//		List<SelectItem> items = new ArrayList<SelectItem>();
//		items.add(new SelectItem(null, "All"));
//
//		for (Status status : this.statues) {
//			items.add(new SelectItem(status, status.getName()));
//		}
//
//		return items;
//	}

}
