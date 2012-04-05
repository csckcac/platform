/*
   Copyright 2011 Jakob Krein

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
 */
package org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.model.components.svg;

/**
 * This class simulates the behavior of the original class of the JavaServer Faces
 * implementation (javax.faces.model.SelectItem).
 *  
 * @author Jakob Krein
 */
public class SelectItem {

	/** The value property of the item */
	private Object value;
	
	/** The label property of the item */
	private String label;
	
	/** The disabled property of the item */
	private boolean disabled;
	
	/**
	 * Construct a new {@link SelectItem}
	 */
    /*@Deprecated
	public SelectItem() {
		this(null);
	}*/

	/**
	 * Construct a new {@link SelectItem} with the specified value
	 * 
	 * @param value The value of the item
	 */
	public SelectItem(java.lang.Object value) {
		this(value, value.toString());
	}
	
	/**
	 * Construct a new {@link SelectItem} with the specified value and label
	 * 
	 * @param value The value of the item
	 * @param label The label of the item
	 */
	public SelectItem(Object value, String label) {
		this(value, label, false);
	}

	/**
	 * Construct a new {@link SelectItem} with the specified value, label and
	 * the status of the item (enabled/disabled)
	 * 
	 * @param value The value of the item
	 * @param label The label of the item
	 * @param disabled Holds the value of the status of the item
	 */
	public SelectItem(Object value, String label, boolean disabled) {
		this.value = value;
		this.label = label;
		this.disabled = disabled;
	}

	/**
	 * Returns the status of the item
	 * 
	 * @return Status of the item
	 */
	public boolean isDisabled() {
		return disabled;
	}

	/**
	 * Sets the status of the item to disabled or enabled
	 * 
	 * @param disabled Status of the item
	 */
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	/**
	 * Returns the label of the item
	 * 
	 * @return Label of the item
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label of the item
	 * 
	 * @param label The label of the item
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Returns the value of the item
	 * 
	 * @return The value of the item
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Sets the value of the item
	 * 
	 * @param value The value of the item
	 */
	public void setValue(Object value) {
		this.value = value;
	}
	
}
