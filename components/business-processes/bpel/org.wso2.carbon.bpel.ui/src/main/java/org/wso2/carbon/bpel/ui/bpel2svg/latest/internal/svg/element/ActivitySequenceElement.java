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

import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.activity.ActivityChoice;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.activity.ActivitySequence;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.SVG;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Dimension;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Position;

/**
 * The SVG wrapper for the {@link ActivityChoice}.
 * 
 * @author Gregor Latuske
 */
public class ActivitySequenceElement
	extends ActivityComplexElement<ActivitySequence> {

	/** Serial version UID */
	private static final long serialVersionUID = 795747004970884491L;

	/**
	 * Constructor of ActivityChoiceElement.
	 * 
	 * @param value The associated {@link ActivityChoice}.
	 * @param parent The parent element.
	 */
	protected ActivitySequenceElement(ActivitySequence value, ActivityComplexElement<?> parent) {
		super(value, parent);
	}

	/** {@inheritDoc} */
	@Override
	public SVG getSVG() {
		SVG svg = createSVG();

		// Element has children?
		if (!getChildren().isEmpty()) {

			// Next child position
			Position nextChildPos = createPositionPointer();

			// Arrow source
			Position arrowSource = getCenterPosition();

			// Iterate over children
			for (ActivityElement<?> child : getChildren()) {

				// Modify child position
				Position childPos = nextChildPos.makeCopy();
				childPos.centerX(getDimension(), child.getDimension());
				childPos.appendToY(child.getDimension().getMarginVertical());
				child.setPosition(childPos);

				// Add arrow
				svg.append(createArrow(child.getValue(), arrowSource, child.getTopPosition()));

				// Add SVG segment of the child activity
				svg.append(child.getSVG());

				// Modify next child position & arrow source
				nextChildPos.appendToY(child.getDimension().getHeightWithMargin());
				arrowSource = child.getBottomPosition();
			}

			// Add final arrow
			ActivityElement<?> lastChild = getChildren().get(getChildren().size() - 1);
			svg.append(createArrow(lastChild.getValue(), arrowSource, getBottomPosition()));
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
			if (child.getDimension().getWidthWithMargin() > width) {
				width = child.getDimension().getWidthWithMargin();
			}

			height += child.getDimension().getHeightWithMargin();
		}

		if (getChildren().isEmpty()) {
			width = getDefaultDimension().getWidth();
			height = getDefaultDimension().getHeight();
		} else {
			height += getDefaultDimension().getHeightWithMargin();
		}

		return new Dimension(width, dim.getMarginHorizontal(), height, dim.getMarginVertical());
	}

}
