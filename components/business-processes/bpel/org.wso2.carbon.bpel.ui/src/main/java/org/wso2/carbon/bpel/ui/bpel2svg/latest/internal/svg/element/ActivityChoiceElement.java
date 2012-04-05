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

import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.ProcessModel;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.activity.Activity;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.activity.ActivityChoice;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.activity.ActivitySequence;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.SVG;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.segment.Arrow;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Dimension;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Position;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Settings;

/**
 * The SVG wrapper for the {@link ActivitySequence}.
 *
 * @author Gregor Latuske
 */
public class ActivityChoiceElement
	extends ActivityComplexElement<ActivityChoice> {

	/** Serial version UID */
	private static final long serialVersionUID = -7494125003695682759L;

	/**
	 * Constructor of ActivitySequenceElement.
	 *
	 * @param value The associated {@link ActivitySequence}.
	 * @param parent The parent element.
	 */
	protected ActivityChoiceElement(ActivityChoice value,
		ActivityComplexElement<?> parent) {
		super(value, parent);
	}

	/**
	 * Constructor of ActivitySequenceElement.
	 *
	 * @param value The associated {@link ProcessModel} or {@link Activity}.
	 * @param settings The associated {@link Settings}.
	 */
	protected ActivityChoiceElement(ActivityChoice value, Settings settings) {
		super(value, settings);
	}

	/** {@inheritDoc} */
	@Override
	public SVG getSVG() {
		SVG svg = createSVG();

		// Element has children?
		if (!getChildren().isEmpty()) {

			// Next child position
			Position nextChildPos = createPositionPointer();

			// Iterate over children
			for (ActivityElement<?> child : getChildren()) {

				// Modify child position
				Position childPos = nextChildPos.makeCopy();
				childPos.appendToX(child.getDimension().getMarginHorizontal());
				childPos.appendToY(child.getDimension().getMarginVertical());
				child.setPosition(childPos);

				// Add top arrow
				svg.append(createTopArrow(child));

				// Add SVG segment of the child activity
				svg.append(child.getSVG());

				// Add top arrow
				svg.append(createBottomArrow(child));

				// Modify position pointer
				nextChildPos.appendToX(child.getDimension().getWidthWithMargin());
			}
		}

		return svg;
	}

	/** {@inheritDoc} */
	@Override
	protected Dimension calculateDimension() {
		Dimension dim = getDefaultDimension();
		int width = 0;
		int height = 0;

		for (ActivityElement<?> child : getChildren()) {
			width += child.getDimension().getWidthWithMargin();

			if (child.getDimension().getHeightWithMargin() > height) {
				height = child.getDimension().getHeightWithMargin();
			}
		}

		if (getChildren().isEmpty()) {
			width = getDefaultDimension().getWidth();
			height = getDefaultDimension().getHeight();
		} else {
			height += getDefaultDimension().getHeightWithMargin();
		}

		return new Dimension(width, dim.getMarginHorizontal(), height, dim.getMarginVertical());
	}

	/**
	 * Creates an arrow between the centre of this element and the top of the child element.
	 *
	 * @param child The given child.
	 * @return An arrow between the centre of this element and the top of the child element.
	 */
	private Arrow createTopArrow(ActivityElement<?> child) {
		Position pos1 = getCenterPosition().makeCopy();
		pos1.appendToY(getDimension().getMarginVertical());

		Position pos2 = child.getTopPosition().makeCopy();
		pos2.appendToY(-child.getDimension().getMarginVertical());
		
		return createArrow(child.getValue(), getCenterPosition(), pos1, pos2, child.getTopPosition());
	}

	/**
	 * Creates an arrow between the bottom of the child element and the bottom of this element.
	 *
	 * @param child The given child.
	 * @return An arrow between the bottom of the child element and the bottom of this element.
	 */
	private Arrow createBottomArrow(ActivityElement<?> child) {
		Position pos2 = getBottomPosition().makeCopy();
		pos2.appendToY(-getDimension().getMarginVertical());

		Position pos1 = pos2.makeCopy();
		pos1.setX(child.getBottomPosition().getX());

		return createArrow(child.getValue(), child.getBottomPosition(), pos1, pos2, getBottomPosition());
	}

}
