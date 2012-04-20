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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.model.SelectionDataModel;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.ProcessItem;

import java.lang.reflect.Method;
import java.util.List;

/**
 * This class is the basis for the paging, sorting and filtering abilities of the {@link SelectionDataModel}.
 * This class has been adapted for the WSO2 Carbon version to work without JavaServer Faces.
 * 
 * @author Gregor Latuske
 * @author Jakob Krein
 */
public abstract class Adjuster {

	/** The logger instance. */
	private static final Log LOG = LogFactory.getLog(Adjuster.class);

	/** The associated SelectionDataModel. */
	private final SelectionDataModel<?, ?, ?> dataModel;

	/**
	 * Constructor of Adjuster.
	 * 
	 * @param dataModel The associated SeelctionDataModel.
	 */
	public Adjuster(SelectionDataModel<?, ?, ?> dataModel) {
		this.dataModel = dataModel;
	}

	/**
	 * Adjust the list of values.
	 * 
	 * @param values ProcessItems
	 */
	public abstract void adjust(List<ProcessItem<?>> values);

	/**
	 * Returns the value of dataModel.
	 * 
	 * @return The value of dataModel.
	 */
	protected SelectionDataModel<?, ?, ?> getDataModel() {
		return this.dataModel;
	}


	/**
	 * Invokes the method with the given name and returns the result.
	 * <p>
	 * If something went wrong during the execution <code>null</code> will be returned.
	 * 
	 * @param object The object that provides the method.
	 * @param property Property
     * @return The result of the invocation or <code>null</code>, if something went wrong during the
	 *         execution.
	 */
	protected Comparable<Object> invokeMethod(Object object, String property) {
		Comparable<Object> value = null;
        String tProperty = property;
		// Create getter method
		if (!tProperty.startsWith("get") && !tProperty.startsWith("is")) {
			tProperty = "get" + tProperty.substring(0, 1).toUpperCase() + tProperty.substring(1);
		}

		// Invoke method
		try {
			Method method = object.getClass().getMethod(tProperty);
			value = (Comparable<Object>) method.invoke(object);
		} catch (Exception exception) {
			LOG.error("Could not invoke method (" + property + ") of class (" + object.getClass() + ").",
				exception);
		}

		return value;
	}

}
