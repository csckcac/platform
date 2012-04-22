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

import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.activity.ActivityFlow;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.SVG;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Dimension;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Position;

import java.util.List;

/**
 * The SVG wrapper for the {@link ActivityFlow}.
 */
public class ActivityFlowElement
        extends ActivityComplexElement<ActivityFlow> {

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = -5097856440647342470L;

    /**
     * The associated grid.
     */
    private Grid grid;

    /**
     * Constructor of ActivityFlowElement.
     *
     * @param value  The associated
     *               {@link org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.activity.ActivityChoice}.
     * @param parent The parent element.
     */
    protected ActivityFlowElement(ActivityFlow value, ActivityComplexElement<?> parent) {
        super(value, parent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SVG getSVG() {
        SVG svg = createSVG();

        // Element has children?
        if (!getChildren().isEmpty()) {

            // Position pointer
            Position nextChildPos = createPositionPointer();

            // Iterate over rows
            for (int i = 0; i < getGrid().getData().size(); i++) {
                List<ActivityElement<?>> row = getGrid().getData().get(i);

                // Centre elements of this row
                int marginLeft = (getDimension().getWidth() - getGrid().getRowWidth(i)) / 2;
                nextChildPos.appendToX(marginLeft);

                // Iterate over columns of the row
                for (ActivityElement<?> child : row) {
                    // Modify child position
                    Position childPos = nextChildPos.makeCopy();
                    childPos.appendToX(child.getDimension().getMarginHorizontal());
                    childPos.appendToY(child.getDimension().getMarginVertical());
                    child.setPosition(childPos);

                    // Add SVG segment of the child activity
                    svg.append(child.getSVG());

                    // Modify position pointer (for each column)
                    nextChildPos.appendToX(child.getDimension().getWidthWithMargin());
                }

                // Modify position pointer (for each row)
                nextChildPos.setX(getPosition().getX());
                nextChildPos.appendToY(getGrid().getRowHeight(i));
            }
        }

        return svg;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Dimension calculateDimension() {
        Dimension dimension = getGrid().getDimension();

        if (getChildren().isEmpty()) {
            dimension.appendToWidth(getDefaultDimension().getWidth());
            dimension.appendToHeight(getDefaultDimension().getHeight());
        } else {
            dimension.appendToHeight(getDefaultDimension().getHeightWithMargin());
        }

        return dimension;
    }

    /**
     * Returns the grid or creates a new, if does not exist.
     *
     * @return The created grid (creates a new, if does not exist).
     */
    private Grid getGrid() {
        if (this.grid == null) {
            this.grid = new Grid(this);
        }

        return this.grid;
    }

}
