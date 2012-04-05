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
package org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.model;

import java.util.ArrayList;
import java.util.List;


/**
 * This class simulates the behavior of the original class of the JavaServer Faces
 * implementation (javax.faces.model.ListDataModel).
 * 
 * @author Jakob Krein
 *
 * @param <I> Type of items
 */
public class ListDataModel<I> {
	
	/** The index of the selected item */
	private int rowIndex = -1;
	
	/** The list that holds the items */
	private List<I> items;
	
	/**
	 * Returns the number of items
	 * 
	 * @return Number of items
	 */
	public int getRowCount() {
		return items.size();
	}
	
	/**
	 * Construct a new {@link ListDataModel}
	 */
	public ListDataModel() {
		this.items = new ArrayList<I>();
	}
	
	/**
	 * Construct a new {@link ListDataModel} with the specified items
	 * 
	 * @param items The items for initialization
	 */
	public ListDataModel(List<I> items) {
		this.items = items;
	}
	
	/**
	 * Returns the currently selected row
	 * 
	 * @return The currently selected row
	 */
	public I getRowData() {
		if((rowIndex < 0) || (rowIndex > items.size() - 1)) {
			return null;
		}
		return items.get(rowIndex);
	}
	
	/**
	 * Sets the index of the row to be selected
	 * 
	 * @param rowIndex The index of the row to be selected
	 */
	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}
	
	/**
	 * Returns the index of the currently selected row
	 * 
	 * @return Index of the currently selected row
	 */
	public int getRowIndex() {
		return rowIndex;
	}
	
	/**
	 * Sets the list of items to the specified new list of items
	 * 
	 * @param items New list of items
	 */
	public void setItems(List<I> items) {
		this.items = items;
	}
	
	/**
	 * Returns the list of items
	 * 
	 * @return The list of items
	 */
	public List<I> getItems() {
		return items;
	}
	
}
