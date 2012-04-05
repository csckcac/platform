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
 * This class gives the {@link SelectionDataModel} paging abilities.
 * This class has been adapted for the WSO2 Carbon version to work without JavaServer Faces.
 * 
 * @author Gregor Latuske
 * @author Jakob Krein
 */
public class Pager
	extends Adjuster {

	/** The possible pages. */
	private static final String[] PAGES = new String[] {"first", "previous", "next", "last"};

	/** The current number of not filtered items. */
	private int count;

	/** The maximum number of items displayed at once. */
	private int maximum;

	/** The page currently displayed. */
	private int currentPage;

	/**
	 * Constructor of Pager.
	 */
	public Pager(SelectionDataModel<?, ?, ?> dataModel) {
		super(dataModel);
		this.count = 0;
		this.maximum = 10;
		this.currentPage = 1;
	}

	/** {@inheritDoc} */
	@Override
	public void adjust(List<ProcessItem<?>> values) {
		this.count = values.size();

		// Clone list and update values
		List<ProcessItem<?>> temp = new ArrayList<ProcessItem<?>>(values);
		values.clear();
		values.addAll(temp.subList(getRangStart() - 1, getRangEnd()));
	}

	public void processAction(String event) {
		String param = event;

		if (param.equalsIgnoreCase(PAGES[0])) {
			goToFirstPage();
		} else if (param.equalsIgnoreCase(PAGES[1])) {
			setCurrentPage(getCurrentPage() - 1);
		} else if (param.equalsIgnoreCase(PAGES[2])) {
			setCurrentPage(getCurrentPage() + 1);
		} else if (param.equalsIgnoreCase(PAGES[3])) {
			setCurrentPage(getLastPage());
		}
	}

	/**
	 * Returns the start index of the item rang.
	 * 
	 * @return The start index of the item rang.
	 */
	public int getRangStart() {
		return (this.currentPage - 1) * this.maximum + 1;
	}

	/**
	 * Returns the end index of the item rang.
	 * 
	 * @return The end index of the item rang.
	 */
	public int getRangEnd() {
		int end = getRangStart() + this.maximum - 1;

		if (end > getCount()) {
			end = getCount();
		}

		return end;
	}

	/**
	 * Returns the value of itemCount.
	 * 
	 * @return The value of itemCount.
	 */
	public int getCount() {
		return this.count;
	}

	/**
	 * Returns the value of maximum.
	 * 
	 * @return The value of maximum.
	 */
	public int getMaximum() {
		return this.maximum;
	}

	/**
	 * Sets the value of maxItemCount to maximum.
	 * 
	 * @param maximum The new value of maximum.
	 */
	public void setMaximum(int maximum) {
		if (maximum > 0) {
			this.maximum = maximum;
			goToFirstPage();
		}
	}

	/**
	 * Returns the value of currentPage.
	 * 
	 * @return The value of currentPage.
	 */
	public int getCurrentPage() {
		return this.currentPage;
	}

	/**
	 * Sets the value of current to currentPage.
	 * 
	 * @param current The new value of currentPage.
	 */
	public void setCurrentPage(int currentPage) {
		if (currentPage > 0 && currentPage <= getLastPage()) {
			this.currentPage = currentPage;
			getDataModel().adjust();
		}
	}

	/**
	 * Sets the current page to first one.
	 */
	public void goToFirstPage() {
		this.currentPage = 1;
		getDataModel().adjust();
	}

	/**
	 * Returns the number of the last page.
	 * 
	 * @return The number of the last page.
	 */
	public int getLastPage() {
		double page = (double) this.count / this.maximum;
		return (int) Math.ceil(page);
	}

}
