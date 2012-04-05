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
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.activity.*;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.SVG;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.segment.ActivityBox;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.segment.Arrow;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.segment.HighlightedArrow;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.segment.Rectangle;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Dimension;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Position;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Settings;

import java.util.ArrayList;
import java.util.List;

/**
 * The SVG wrapper for the {@link ActivityComplex}.
 * 
 * @author Gregor Latuske
 */
public abstract class ActivityComplexElement<T extends ActivityComplex>
	extends ActivityElement<T> {

	/** Serial version UID */
	private static final long serialVersionUID = -9061952581306338295L;

	/** The child elements of this element. */
	private final List<ActivityElement<?>> children;

	/**
	 * Constructor of ActivityComplexElement.
	 * 
	 * @param value The associated {@link ActivityComplex}.
	 * @param parent The parent element.
	 */
	protected ActivityComplexElement(T value, ActivityComplexElement<?> parent) {
		super(value, parent);

		this.children = new ArrayList<ActivityElement<?>>();
		createChildren();
	}

	/**
	 * Constructor of ActivityComplexElement.
	 * 
	 * @param value The associated {@link ProcessModel} or {@link Activity}.
	 * @param settings The associated {@link Settings}.
	 */
	protected ActivityComplexElement(T value, Settings settings) {
		super(value, settings);

		this.children = new ArrayList<ActivityElement<?>>();
		createChildren();
	}

	/**
	 * Returns the {@link Dimension} of the frame of a {@link ActivityComplex}.
	 * 
	 * @return The {@link Dimension} of the frame of a {@link ActivityComplex}.
	 */
	public Dimension getFrameDimension() {
		int width = getDimension().getWidth();
		int marginHorizontal = getDimension().getMarginHorizontal();
		int height = getDimension().getHeight() - getDefaultDimension().getHeight() / 2;
		int marginVertical = getDimension().getMarginVertical();

		return new Dimension(width, marginHorizontal, height, marginVertical);
	}

	/** {@inheritDoc} */
	@Override
	protected Position getLeftPosition() {
		Position pos = getPosition().makeCopy();
		pos.appendToX((getDimension().getWidth() - getDefaultDimension().getWidth()) / 2);
		pos.appendToY(getDefaultDimension().getHeight() / 2);

		return pos;
	}

	/** {@inheritDoc} */
	@Override
	protected Position getRightPosition() {
		Position pos = getPosition().makeCopy();
		pos.appendToX((getDimension().getWidth() - getDefaultDimension().getWidth()) / 2
			+ getDefaultDimension().getWidth());
		pos.appendToY(getDefaultDimension().getHeight() / 2);

		return pos;
	}

	/**
	 * Returns the value of children.
	 * 
	 * @return The value of children.
	 */
	public List<ActivityElement<?>> getChildren() {
		return this.children;
	}

	/**
	 * Creates the SVG and adds the frame and the activity box.
	 * 
	 * @return The created SVG.
	 */
	protected SVG createSVG() {
		SVG svg = new SVG(getDimension());

		// Frame
		if (!getChildren().isEmpty()) {
			Position framePos = getPosition().makeCopy();
			framePos.appendToY(getDefaultDimension().getHeight() / 2);
			svg.append(new Rectangle(getFrameDimension(), framePos, "frame"));
		}

		// Activity box
		Position boxPos = getPosition().makeCopy();
		boxPos.centerX(getDimension(), getDefaultDimension());
		svg.append(new ActivityBox(getValue(), getDefaultDimension(), boxPos, getSettings()));

		return svg;
	}

	/**
	 * Creates the position pointer.
	 * 
	 * @return The position pointer.
	 */
	protected Position createPositionPointer() {
		Position pos = getPosition().makeCopy();
		pos.appendToY(getDefaultDimension().getHeight() + getDefaultDimension().getMarginVertical());

		return pos;
	}

	/**
	 * Creates a new "normal" or highlighted arrow.
	 * 
	 * @param activity The activity to check the highlighting.
	 * @param pos The positions of the arrow.
	 * @return The created arrow.
	 */
	protected Arrow createArrow(Activity activity, Position... pos) {
		if (getSettings().isArrowHighlighted(activity)) {
			return new HighlightedArrow(pos);
		} else {
			return new Arrow(pos);
		}
	}

	/**
	 * Creates the child element.
	 */
	private void createChildren() {
		for (Activity child : getValue().getChildren()) {

			// Create element
			ActivityElement<?> childElement = null;
			if (child instanceof ActivitySimple) {
				childElement = new ActivitySimpleElement((ActivitySimple) child, this);
			} else if (child instanceof ActivityFlow) {
				childElement = new ActivityFlowElement((ActivityFlow) child, this);
			} else if (child instanceof ActivitySequence) {
				childElement = new ActivitySequenceElement((ActivitySequence) child, this);
			} else if (child instanceof ActivityChoice) {
				childElement = new ActivityChoiceElement((ActivityChoice) child, this);
			}

			// Add to children && create links
			if (childElement != null) {
				this.children.add(childElement);
				createLinks(childElement);
			}
		}
	}

	/**
	 * Creates the links of the given element.
	 * 
	 * @param element The given element.
	 */
	private void createLinks(ActivityElement<?> element) {
		for (Link link : element.getValue().getRoot().getLinks()) {

			LinkElement linkElement = new LinkElement(link.getName());

			// Check for new link element
			boolean contains = false;

			for (LinkElement existingLinkElement : getRoot().getLinks()) {
				if (existingLinkElement.equals(linkElement)) {
					linkElement = existingLinkElement;
					contains = true;
				}
			}

			// Add link element if not exists
			if (!contains) {
				getRoot().getLinks().add(linkElement);
			}

			// Set source or target
			if (element.getValue().equals(link.getSource())) {
				linkElement.setSource(element);
			} else if (element.getValue().equals(link.getTarget())) {
				linkElement.setTarget(element);
			}
		}
	}

}
