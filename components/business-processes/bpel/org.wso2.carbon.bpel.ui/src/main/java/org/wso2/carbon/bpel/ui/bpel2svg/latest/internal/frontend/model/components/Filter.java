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
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.ProcessItem;

import java.util.ArrayList;
import java.util.List;

/**
 * This class gives the {@link SelectionDataModel} filtering abilities.
 * This class has been adapted for the WSO2 Carbon version to work without JavaServer Faces.
 * 
 * @param <T> The type of input.
 * @author Gregor Latuske
 * @author Jakob Krein
 */
public abstract class Filter<T>
	extends Adjuster {

	/** The filter property. */
	private final String filterProperty;

	/** The selected status. */
	private T input;

	/**
	 * Constructor of Filter.
	 * 
	 * @param dataModel The associated SeelctionDataModel.
	 * @param filterProperty The filter property.
	 */
	public Filter(SelectionDataModel<?, ?, ?> dataModel, String filterProperty) {
		super(dataModel);
		this.filterProperty = filterProperty;
	}

	/**
	 * Resets the filter input.
	 */
	public abstract void reset();

	/**
	 * Filters a single entry from the list if the method returns true.
	 * 
	 * @param object The object to filter.
	 * @return True if the entry should be filtered from the list.
	 */
	protected abstract boolean filter(Object object);

	public void processAction() {
		reset();
	}

	/** {@inheritDoc} */
	@Override
	public void adjust(List<ProcessItem<?>> values) {
		List<ProcessItem<?>> valuesToRemove = new ArrayList<ProcessItem<?>>();

		// No filter --> nothing to delete
		if (getInput() != null) {
			for (ProcessItem<?> value : values) {

				// Invoke getter method
				Object object = invokeMethod(value, this.filterProperty);

				// Object matches filter --> delete from values
				if (object != null && filter(object)) {
					valuesToRemove.add(value);
				}
			}
		}

		values.removeAll(valuesToRemove);
	}

	/**
	 * Returns the value of input.
	 * 
	 * @return The value of input.
	 */
	public T getInput() {
		return this.input;
	}

	/**
	 * Sets the value of input to input.
	 * 
	 * @param input The new value of input.
	 */
	public void setInput(T input) {
		this.input = input;

		getDataModel().getPager().goToFirstPage();
	}

}
