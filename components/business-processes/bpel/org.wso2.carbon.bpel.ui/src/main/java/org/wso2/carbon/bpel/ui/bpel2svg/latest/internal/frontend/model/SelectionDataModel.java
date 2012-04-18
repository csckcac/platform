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

import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.model.components.*;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.ProcessItem;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.status.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is the basis for all data models.
 * 
 * @param <V> The type of the values.
 * @param <S> The type of the selection.
 * @author Gregor Latuske
 * @author Jakob Krein
 */
public abstract class SelectionDataModel<C extends AbstractDataModel<?, ?>, U extends List<I>, I extends ProcessItem<?>>
	extends AbstractDataModel<C, U> {

	/** Serial version UID */
	private static final long serialVersionUID = -369515200477782111L;

	/** The value, that should be displayed. */
	private ListDataModel<I> items;

	/** The associated sorter. */
	private final Sorter sorter;

	/** The associated pager. */
	private final Pager pager;

	/** The associated filters. */
	private final List<Filter<?>> filters;

	/** The filter for the id of the process model or process instance. */
	private final TextFilter idFilter;

	/** The filter for the process model name. */
	private final TextFilter nameFilter;

	/** The associated status filter. */
	private final StatusFilter statusFilter;

	/**
	 * Constructor of AbstractDataModel.
	 * 
	 * @param child The child data model.
	 * @param defaultSortProperty The default sort property.
	 * @param statusFilterProperty The name of the status filter property.
	 * @param statues The possible statues of the data model entries.
	 * @param idFilterProperty The name of the id filter property.
	 * @param nameFilterProperty The name of the name filter property.
	 */
	public SelectionDataModel(C child, String defaultSortProperty, String statusFilterProperty,
		Status[] statues, String idFilterProperty, String nameFilterProperty) {
		super(child);

		this.items = new ListDataModel<I>();

		// Create sorter & paper
		this.sorter = new Sorter(this, defaultSortProperty);
		this.pager = new Pager(this);
		this.filters = new ArrayList<Filter<?>>();

		// Add filters
		this.statusFilter = new StatusFilter(this, statusFilterProperty);
        this.statusFilter.addStatuses(statues);
		this.filters.add(this.statusFilter);

		this.idFilter = new TextFilter(this, idFilterProperty);
		this.filters.add(this.idFilter);

		this.nameFilter = new TextFilter(this, nameFilterProperty);
		this.filters.add(this.nameFilter);
	}

	/**
	 * Returns the value of selection.
	 * 
	 * @return The value of selection.
	 */
	public abstract List<I> getSelection();

	/**
	 * Selects the element of the current row.
	 * 
	 * @return "" (empty String)
	 */
	public abstract String select();

	/**
	 * Updates the data of the associated child data model.
	 */
	protected abstract void updateChild();

	/**
	 * Returns the value of internalItems.
	 * 
	 * @return The value of internalItems.
	 */
	protected abstract List<I> getInternalItems();

	/**
	 * Sets the value of internalItems to internalItems.
	 * 
	 * @param items The new value of internalItems.
	 */
	protected abstract void setInternalItems(List<I> items);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(U data) {
		setInternalItems(data);

		// New values --> reset current page & adjust view
		getPager().goToFirstPage();

		// Update child data model
		updateChild();
	}

	/**
	 * Selects the element of the current row and notifies the observers.
	 * 
	 * @return "" (empty String)
	 */
	public String selectAndNotify() {
		select();

		// Update child data model
		updateChild();

		return "";
	}

	/**
	 * Adjust the data model (filtering, sorting, paging).
	 */
	@SuppressWarnings("unchecked")
	public void adjust() {
		List<I> workingEntries = new ArrayList<I>(getInternalItems());

		// Sorting
		this.sorter.adjust((List<ProcessItem<?>>) workingEntries);

		// Filtering
		for (Filter<?> filter : this.filters) {
			filter.adjust((List<ProcessItem<?>>) workingEntries);
		}

		// Paging
		this.pager.adjust((List<ProcessItem<?>>) workingEntries);

		// Create new ListDataModel
		this.items = new ListDataModel<I>(workingEntries);
	}

	/**
	 * Returns the value of items.
	 * 
	 * @return The value of items.
	 */
	public ListDataModel<I> getItems() {
		return this.items;
	}

	/**
	 * Returns the value of sorter.
	 * 
	 * @return The value of sorter.
	 */
	public Sorter getSorter() {
		return this.sorter;
	}

	/**
	 * Returns the value of pager.
	 * 
	 * @return The value of pager.
	 */
	public Pager getPager() {
		return this.pager;
	}

	/**
	 * Returns the value of idFilter.
	 * 
	 * @return The value of idFilter.
	 */
	public TextFilter getIdFilter() {
		return this.idFilter;
	}

	/**
	 * Returns the value of nameFilter.
	 * 
	 * @return The value of nameFilter.
	 */
	public TextFilter getNameFilter() {
		return this.nameFilter;
	}

	/**
	 * Returns the value of statusFilter.
	 * 
	 * @return The value of statusFilter.
	 */
	public StatusFilter getStatusFilter() {
		return this.statusFilter;
	}

	/**
	 * Adds an filter to the list of filters.
	 * 
	 * @param filter The filter to add.
	 */
	protected void addFilter(Filter<?> filter) {
		this.filters.add(filter);
	}

}
