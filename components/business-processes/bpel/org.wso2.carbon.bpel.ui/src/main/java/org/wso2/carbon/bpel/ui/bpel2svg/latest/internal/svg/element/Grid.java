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
package org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.element;

import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Dimension;

import java.util.ArrayList;
import java.util.List;

/**
 * The Grid is used to distribute the {@link ActivityElement} in the flow.
 *
 * @author Gregor Latuske
 */
public class Grid {

    /**
     * The associated {@link ActivityFlowElement}.
     */
    private final ActivityFlowElement element;

    /**
     * The data saved in the grid.
     */
    private final List<List<ActivityElement<?>>> data;

    /**
     * Constructor of Grid.
     *
     * @param element The associated {@link ActivityFlowElement}.
     */
    public Grid(ActivityFlowElement element) {
        this.element = element;
        this.data = new ArrayList<List<ActivityElement<?>>>();

        createGrid();
    }

    /**
     * Returns the value of data.
     *
     * @return The value of data.
     */
    public List<List<ActivityElement<?>>> getData() {
        return this.data;
    }

    /**
     * Returns the dimension of the grid.
     *
     * @return The dimension of the grid.
     */
    public Dimension getDimension() {
        Dimension dim = this.element.getDefaultDimension();
        int width = 0;
        int height = 0;

        for (int i = 0; i < this.data.size(); i++) {
            int rowWidth = getRowWidth(i);

            if (rowWidth > width) {
                width = rowWidth;
            }

            height += getRowHeight(i);
        }

        return new Dimension(width, dim.getMarginHorizontal(), height, dim.getMarginVertical());
    }

    /**
     * Returns the width of the row with the given index.
     *
     * @param rowIndex The index of the row.
     * @return The width of the row with the given index.
     */
    public int getRowWidth(int rowIndex) {
        int width = 0;

        for (ActivityElement<?> activityElement : this.data.get(rowIndex)) {
            width += activityElement.getDimension().getWidthWithMargin();
        }

        return width;
    }

    /**
     * Returns the height of the row with the given index.
     *
     * @param rowIndex The index of the row.
     * @return The height of the row with the given index.
     */
    public int getRowHeight(int rowIndex) {
        int height = 0;

        for (ActivityElement<?> activityElement : this.data.get(rowIndex)) {
            if (activityElement.getDimension().getHeightWithMargin() > height) {
                height = activityElement.getDimension().getHeightWithMargin();
            }
        }

        return height;
    }

    /**
     * Creates a flow grid (nxm).
     */
    private void createGrid() {
        for (ActivityElement<?> child : this.element.getChildren()) {
            if (isStartElement(child)) {
                addToRow(child);
                createGrid(child);
            }
        }
    }

    /**
     * Creates a flow grid.
     *
     * @param element The element that target elements should be followed.
     */
    private void createGrid(ActivityElement<?> element) {
        for (LinkElement link : element.getOutgoingLinks()) {
            ActivityElement<?> target = link.getTarget();

            // Use this link to allow progress of the topological sort
            link.use();

            // All incoming links have to be used.
            if (allSourceLinksUsed(target.getIncomingLinks())) {

                // Only add target, if the parent is the element
                if (target.getParent().equals(this.element)) {
                    addToRow(target);
                }

                createGrid(target);
            }
        }
    }

    /**
     * Checks if the element has no incoming links or all incoming links come from elements with an other
     * parent element.
     *
     * @param element The element to check.
     * @return True if the element has no incoming links or all incoming links come from elements with an
     *         other parent element.
     */
    private boolean isStartElement(ActivityElement<?> element) {
        if (element.getIncomingLinks().isEmpty()) {
            return true;
        }

        boolean start = true;
        for (LinkElement link : element.getIncomingLinks()) {
            start = start && !link.getSource().getParent().equals(this.element);
        }

        return start;
    }

    /**
     * Checks if grid contains the element.
     *
     * @param links Link elements
     * @return True if grid contains the element.
     */
    private boolean allSourceLinksUsed(List<LinkElement> links) {
        boolean used = true;
        for (LinkElement link : links) {
            used = used && (link.isUsed() || !link.getSource().getParent().equals(this.element));
        }

        return used;
    }

    /**
     * Adds the given element to a row.
     *
     * @param element The element to add.
     */
    private void addToRow(ActivityElement<?> element) {
        int rowIndex = -1;

        for (LinkElement link : element.getIncomingLinks()) {
            int currentRowIndex = getRowIndex(link.getSource());

            if (currentRowIndex > rowIndex) {
                rowIndex = currentRowIndex;
            }
        }

        // Increment row index
        rowIndex++;

        // Create new row, if not exists
        if (this.data.size() - 1 < rowIndex) {
            this.data.add(new ArrayList<ActivityElement<?>>());
        }

        // Special case: two incoming links
        if (element.getIncomingLinks().size() == 2) {
            addPlaceHolder(element, rowIndex);
        }

        this.data.get(rowIndex).add(element);
    }

    /**
     * Adds place-holders for the special case of two incoming links.
     *
     * @param newElement The given element.
     * @param rowIndex   The row index of the element.
     */
    private void addPlaceHolder(ActivityElement<?> newElement, int rowIndex) {
        int rowIndex1 = getRowIndex(newElement.getIncomingLinks().get(0).getSource());
        int rowIndex2 = getRowIndex(newElement.getIncomingLinks().get(1).getSource());

        // Order rowIndex
        if (rowIndex2 < rowIndex1) {
            int tmpRowIndex = rowIndex1;
            rowIndex1 = rowIndex2;
            rowIndex2 = tmpRowIndex;
        }

        // If there exists links from different rows add place-holder
        if (rowIndex1 != rowIndex2 && rowIndex1 >= 0 && rowIndex2 >= 0) {

            // Calculate maximum column count of all rows from the start index to the row of the new element
            int maxColumnCount = 0;

            for (int i = rowIndex1; i < rowIndex; i++) {
                if (this.data.get(i).size() > maxColumnCount) {
                    maxColumnCount = this.data.get(i).size();
                }
            }

            // Move first element and add place-holders
            int columnCount = this.data.get(rowIndex1).size();
            int placeHolderAmount = maxColumnCount + 1 - columnCount;

            if (placeHolderAmount > 0) {

                // Save element and remove it from the grid.
                ActivityElement<?> activityElement = this.data.get(rowIndex1).get(columnCount - 1);
                this.data.get(rowIndex1).remove(activityElement);

                addPlaceHolders(rowIndex1, placeHolderAmount);

                // Add the element to the list (after the place-holders
                this.data.get(rowIndex1).add(activityElement);
            }

            // Add place-holders in every row between first row index + 1 and row of the new element
            for (int i = rowIndex1 + 1; i <= rowIndex; i++) {

                placeHolderAmount = maxColumnCount - this.data.get(i).size();

                // Additional place-holder for second row index
                if (i == rowIndex2) {
                    placeHolderAmount++;
                }

                addPlaceHolders(i, placeHolderAmount);
            }
        }
    }

    /**
     * Returns the row index of the element in the grid.
     *
     * @param element The given element.
     * @return The row index of the element in the grid.
     */
    private int getRowIndex(ActivityElement<?> element) {
        for (int i = 0; i < this.data.size(); i++) {
            if (this.data.get(i).contains(element)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Adds the given amount of place-holders elements to the given row.
     *
     * @param rowIndex The row index.
     * @param amount   The amount of place-holders.
     */
    private void addPlaceHolders(int rowIndex, int amount) {
        for (int i = 0; i < amount; i++) {
            this.data.get(rowIndex).add(new PlaceHolderElement(this.element));
        }
    }

}
