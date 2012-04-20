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
package org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.model.components.svg;

import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.model.SVGDataModel;

import java.util.*;

/**
 * This class is the basis for all adjusting functions of activities in the SVG.
 * This class has been adapted for the WSO2 Carbon version to work without JavaServer Faces.
 * 
 * @author Gregor Latuske
 * @author Jakob Krein
 */
public class ActivityAdjuster extends Adjuster<String[]> {

	/** The selectable items on the left side. */
	private Set<String> itemsLeft;

	/** The selected item(s) on the right side. */
	private List<String> selectionRight;

	/** The selectable items on the right side. */
	private Set<String> itemsRight;

	/**
	 * Constructor of Adjuster.
	 * 
	 * @param dataModel The associated SVGDataModel.
	 */
	public ActivityAdjuster(SVGDataModel dataModel) {
		super(dataModel);
        selectionRight = new ArrayList<String>();
    }

	/** {@inheritDoc} */
	@Override
	public void reset() {
		//getViewMode().setMaximised(false); //TODO: Removed this property set, as it setMaximise to false indirectly, once we explicitly set setMaximise property to true
		setSelection(new String[] {});
		setSelectionRight(new String[] {});
	}

	/**
	 * Processes the 'add' AJAX event.
	 */
	public void add() {
		for (String selection : getSelection()) {
			this.itemsRight.add(selection);
			this.itemsLeft.remove(selection);
		}

		generateSVG();
	}

	/**
	 * Processes the 'remove' AJAX event.
	 */
	public void remove() {
		for (String selection : getSelectionRight()) {
			this.itemsLeft.add(selection);
			this.itemsRight.remove(selection);
		}

		generateSVG();
	}

	/**
	 * Processes the 'addAll' AJAX event.
	 */
	public void addAll() {
		this.itemsRight.addAll(this.itemsLeft);
		this.itemsLeft.clear();

		generateSVG();
	}

	/**
	 * Processes the 'removeAll' AJAX event.
	 */
	public void removeAll() {
		this.itemsLeft.addAll(this.itemsRight);
		this.itemsRight.clear();

		generateSVG();
	}

	/**
	 * Sets the value of itemsLeft to items and the value of itemsRigth of empty set.
	 * 
	 * @param items The new value of itemsLeft.
	 */
	public void setItems(Set<String> items) {
		this.itemsLeft = new TreeSet<String>(items);
		this.itemsRight = new TreeSet<String>();
	}

	/**
	 * Returns the list of selectable items on the left side.
	 * 
	 * @return The list of selectable items on the left side.
	 */
	public List<SelectItem> getItemsLeft() {
		List<SelectItem> items = new ArrayList<SelectItem>();

		for (String string : this.itemsLeft) {
			items.add(new SelectItem(string));
		}

		return items;
	}

	/**
	 * Returns the list of selectable items on the right side.
	 * 
	 * @return The list of selectable items on the right side.
	 */
	public List<SelectItem> getItemsRight() {
		List<SelectItem> items = new ArrayList<SelectItem>();

		for (String string : this.itemsRight) {
			items.add(new SelectItem(string));
		}

		return items;
	}

	/**
	 * Returns the values that should be highlighted or removed.
	 * 
	 * @return The values that should be highlighted or removed.
	 */
	public Set<String> getValues() {
		return this.itemsRight;
	}

	/**
	 * Returns the value of selectionRight.
	 * 
	 * @return The value of selectionRight.
	 */
	public String[] getSelectionRight() {
		return (String[]) this.selectionRight.toArray();
	}

	/**
	 * Sets the value of selectionRight to selectionRight.
	 * 
	 * @param selectionRight The new value of selectionRight.
	 */
	public void setSelectionRight(String[] selectionRight) {
        Collections.addAll(this.selectionRight, selectionRight);
	}

}
