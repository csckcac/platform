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
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.ProcessInstance;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.ProcessItem;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.ProcessModel;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This class gives the {@link SelectionDataModel} sorting abilities.
 * This class has been adapted for the WSO2 Carbon version to work without JavaServer Faces.
 * 
 * @author Gregor Latuske
 * @author Jakob Krein
 */
public class Sorter
	extends Adjuster
	implements Comparator<ProcessItem<?>> {

	/** The sort property. */
	private String property;

	/** Sort ascending? */
	private boolean ascending;

	/**
	 * Constructor of Pager.
	 * 
	 * @param dataModel The associated SeelctionDataModel.
	 * @param property The initial sorting property.
	 */
	public Sorter(SelectionDataModel<?, ?, ?> dataModel, String property) {
		super(dataModel);
		this.property = property;
		this.ascending = true;
	}

	/** {@inheritDoc} */
	@Override
	public void adjust(List<ProcessItem<?>> values) {
		Collections.sort(values, this);
	}

	public void processAction(String event) {
		String param = (String) event;

		if (this.property.equals(param)) {
			this.ascending = !this.ascending;
		} else {
			this.property = param;
			this.ascending = true;
		}

		getDataModel().adjust();
	}

	/** {@inheritDoc} */
	@Override
	public int compare(ProcessItem<?> o1, ProcessItem<?> o2) {
		if (o1 == null || o2 == null) {
			return this.ascending ? -1 : 1;
		}

		// The values of the properties
		Comparable<Object> v1 = null;
		Comparable<Object> v2 = null;

		// Check for same classes
		if (o1 instanceof ProcessModel && o2 instanceof ProcessModel) {
			v1 = invokeMethod(o1, this.property);
			v2 = invokeMethod(o2, this.property);
		} else if (o1 instanceof ProcessInstance && o2 instanceof ProcessInstance) {
			v1 = invokeMethod(o1, this.property);
			v2 = invokeMethod(o2, this.property);
		}

		if (v1 == null || v2 == null) {
			return this.ascending ? -1 : 1;
		} else {
			if (this.ascending) {
				return v1.compareTo(v2);
			} else {
				return v2.compareTo(v1);
			}
		}
	}

}
