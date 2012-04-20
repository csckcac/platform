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

import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.activity.ActivityRoot;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.SVG;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.segment.*;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Dimension;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.GlobalSettings;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Position;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Settings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The SVG wrapper for the {@link ActivityRoot}.
 * 
 * @author Gregor Latuske
 */
public class ActivityRootElement
	extends ActivityChoiceElement {

	/** Serial version UID */
	private static final long serialVersionUID = 6482318063986674863L;

	/** The set of links assigned to all flow activity. */
	private Set<LinkElement> links;

	/**
	 * Constructor of ActivityRootElement.
	 * 
	 * @param value The associated {@link ActivityRoot}.
	 * @param parent The parent element.
	 */
	public ActivityRootElement(ActivityRoot value, Settings settings) {
		super(value, settings);

		// Remove omitted activities
		omitElements(this);
	}

	/** {@inheritDoc} */
	@Override
	public SVG getSVG() {
		SVG svg = new SVG(getDimension());

		// Header
		svg.append(new Header(getDimension(), getSettings()));

		// Start icon
		Position iconPos1 = new Position();
		iconPos1.setX((getDimension().getWidthWithMargin() - getDimension().getWidth()) / 2);
		iconPos1.setY(getDimension().getMarginVertical());
		svg.append(new TypeImage("start", iconPos1));

		// Positions pointer
		Position pos = new Position(0, getDimension().getHeightWithMargin());

		// Iterate over child activities
		for (ActivityElement<?> child : getChildren()) {

			// Modify child position
			Position childPos = pos.makeCopy();
			childPos.appendToX(child.getDimension().getMarginHorizontal());
			childPos.appendToY(child.getDimension().getMarginVertical());
			child.setPosition(childPos);

			// Add arrows
			if (getSettings().isHighlighted(child.getValue())) {
				svg.append(new HighlightedArrow(getTopPosition(), child.getTopPosition()));
				svg.append(new HighlightedArrow(child.getBottomPosition(), getBottomPosition()));
			} else {
				svg.append(new Arrow(getTopPosition(), child.getTopPosition()));
				svg.append(new Arrow(child.getBottomPosition(), getBottomPosition()));
			}

			// Add SVG segment of the child activity
			svg.append(child.getSVG());

			// Modify position pointer
			pos.appendToX(child.getDimension().getWidthWithMargin());
		}

		// Add flow arrows
		for (LinkElement link : getLinks()) {
			ActivityElement<?> source = link.getSource();
			ActivityElement<?> target = link.getTarget();

			Position pos1 = source.getClosestOutgoingPosition(target);
			Position pos2 = target.getClosestIncomingPosition(source);
			
			if (getSettings().isArrowHighlighted(target.getValue())) {
				svg.append(new HighlightedArrow(pos1, pos2));
			} else {
				svg.append(new FlowArrow(pos1, pos2));
			}
		}

		// Add additional arrow, if there exists no child activities
		if (getChildren().isEmpty()) {
			svg.append(new Arrow(getTopPosition(), getBottomPosition()));
		}

		// End icon
		Position iconPos2 = iconPos1.makeCopy();
		iconPos2.setY(getDimension().getHeightWithMargin() - getDimension().getHeight() - iconPos2.getY());
		svg.append(new TypeImage("end", iconPos2));

		// Footer
		svg.append(new Footer());

		return svg;
	}

	/** {@inheritDoc} */
	@Override
	public Position getTopPosition() {
		Position pos = new Position();
		pos.setX(getDimension().getWidthWithMargin() / 2);
		pos.setY(getDimension().getHeight() + getDimension().getMarginVertical());

		return pos;
	}

	/** {@inheritDoc} */
	@Override
	public Position getBottomPosition() {
		Position pos = new Position();
		pos.setX(getDimension().getWidthWithMargin() / 2);
		pos.setY(getDimension().getHeightWithMargin() - getDimension().getHeight()
			- getDimension().getMarginVertical());

		return pos;
	}

	/**
	 * Returns the value of links.
	 * 
	 * @return The value of links.
	 */
	public Set<LinkElement> getLinks() {
		if (this.links == null) {
			this.links = new HashSet<LinkElement>();
		}

		return this.links;
	}

	/** {@inheritDoc} */
	@Override
	protected Dimension calculateDimension() {
		int width = 0;
		int height = 0;

		for (ActivityElement<?> child : getChildren()) {
			Dimension dim = child.getDimension();

			// Calculate width
			width += dim.getWidthWithMargin();

			// Calculate height
			if (dim.getHeightWithMargin() > height) {
				height = dim.getHeightWithMargin();
			}
		}

		// Adjust width
		if (getChildren().isEmpty()) {
			width = GlobalSettings.getInstance().getTypeImageSize().getWidthWithMargin();
		}

		// Adjust height
		height += GlobalSettings.getInstance().getTypeImageSize().getHeightWithMargin() * 2;

		return new Dimension(width, 0, height, 0);
	}

	/**
	 * Check the given elements for omission.
	 * 
	 * @param elements The elements to check.
	 */
	private void omitElements(ActivityComplexElement<?> element) {
		List<ActivityElement<?>> omittedElements = new ArrayList<ActivityElement<?>>();

		for (ActivityElement<?> child : element.getChildren()) {

			// Add element to omitted elements, if omitted
			if (getSettings().isOmitted(child.getValue())) {
				omittedElements.add(child);
			}

			// Check child elements
			if (child instanceof ActivityComplexElement<?>) {
				omitElements((ActivityComplexElement<?>) child);
			}
		}

		// Iterate over omitted elements
		for (ActivityElement<?> omittedElement : omittedElements) {

			// Create new links n X m (n - in coming, m - out going)
			for (LinkElement in : omittedElement.getIncomingLinks()) {
				for (LinkElement out : omittedElement.getOutgoingLinks()) {

					String name = in.getName() + " " + out.getName();
					getLinks().add(new LinkElement(name, in.getSource(), out.getTarget()));
				}
			}

			// Remove old links
			getLinks().removeAll(omittedElement.getIncomingLinks());
			getLinks().removeAll(omittedElement.getOutgoingLinks());
		}

		// Remove omitted elements
		element.getChildren().removeAll(omittedElements);
	}
}
