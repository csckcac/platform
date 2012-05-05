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

import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.activity.Activity;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.SVG;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Dimension;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.GlobalSettings;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Position;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Settings;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the basis of all {@link ActivityElement}s.
 */
public abstract class ActivityElement<T extends Activity> /*implements Serializable*/ {

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The associated SVG element.
     */
    private final T value;

    /**
     * The associated {@link Settings}.
     */
    private final Settings settings;

    /**
     * The parent element.
     */
    private final ActivityComplexElement<?> parent;

    /**
     * The associated {@link ActivityRootElement}.
     */
    private final ActivityRootElement root;

    /**
     * The dimension of the element.
     */
    private Dimension dimension;

    /**
     * The position of the element.
     */
    private Position position;

    /**
     * Constructor of SVGElement.
     *
     * @param value  The associated
     *               {@link org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.ProcessModel} or
     *               {@link Activity}.
     * @param parent The parent element.
     */
    protected ActivityElement(T value, ActivityComplexElement<?> parent) {
        this(value, parent, parent.getSettings());
    }

    /**
     * Constructor of SVGElement.
     *
     * @param value    The associated
     *                 {@link org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.ProcessModel} or
     *                 {@link Activity}.
     * @param settings The associated {@link Settings}.
     */
    protected ActivityElement(T value, Settings settings) {
        this(value, null, settings);
    }

    /**
     * Constructor of SVGElement.
     *
     * @param value    The associated
     *                 {@link org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.ProcessModel} or
     *                 {@link Activity}.
     * @param parent   The parent {@link ActivityElement}.
     * @param settings The associated {@link Settings}.
     */
    private ActivityElement(T value, ActivityComplexElement<?> parent, Settings settings) {
        this.value = value;
        this.settings = settings;
        this.parent = parent;

        // Set root
        if (this instanceof ActivityRootElement) {
            this.root = (ActivityRootElement) this;
        } else {
            this.root = parent.getRoot();
        }

        this.position = new Position();
    }

    /**
     * Returns the SVG part of the element as a String.
     *
     * @return The SVG part of the element as a String.
     */
    public abstract SVG getSVG();

    /**
     * Calculates the dimension of the element.
     *
     * @return The dimension of the element.
     */
    protected abstract Dimension calculateDimension();

    /**
     * Returns the value of value.
     *
     * @return The value of value.
     */
    public T getValue() {
        return this.value;
    }

    /**
     * Returns the value of settings.
     *
     * @return The value of settings.
     */
    public Settings getSettings() {
        return this.settings;
    }

    /**
     * Returns the value of parent.
     *
     * @return The value of parent.
     */
    public ActivityComplexElement<?> getParent() {
        return this.parent;
    }

    /**
     * Returns the value of root.
     *
     * @return The value of root.
     */
    public ActivityRootElement getRoot() {
        return this.root;
    }

    /**
     * Returns the links this element is the source of the edge.
     *
     * @return The links this element is the source of the edge.
     */
    public List<LinkElement> getOutgoingLinks() {
        List<LinkElement> links = new ArrayList<LinkElement>();

        for (LinkElement link : this.root.getLinks()) {
            if (equals(link.getSource()) && link.getTarget() != null) {
                links.add(link);
            }
        }

        return links;
    }

    /**
     * Returns the links this element is the target of the edge.
     *
     * @return The links this element is the target of the edge.
     */
    public List<LinkElement> getIncomingLinks() {
        List<LinkElement> links = new ArrayList<LinkElement>();

        for (LinkElement link : this.root.getLinks()) {
            if (equals(link.getTarget()) && link.getSource() != null) {
                links.add(link);
            }
        }

        return links;
    }

    /**
     * Returns the value of dimension.
     *
     * @return The value of dimension.
     */
    public Dimension getDimension() {
        if (this.dimension == null) {
            this.dimension = calculateDimension();
        }

        return this.dimension;
    }

    /**
     * Returns the default {@link Dimension} for the selected view mode.
     *
     * @return The default {@link Dimension} for the selected view mode.
     */
    public Dimension getDefaultDimension() {
        if (getSettings().isUseCompactMode()) {
            return GlobalSettings.getInstance().getCompactMode();
        }

        return GlobalSettings.getInstance().getFullMode();
    }

    /**
     * Returns the nearest position to the element for outgoing links.
     *
     * @param neighbour The neighbour whose nearest position should be found.
     * @return The nearest position to the element for outgoing links.
     */
    public Position getClosestOutgoingPosition(ActivityElement<?> neighbour) {
        return getClosestPosition(neighbour.getTopPosition(), getBottomPosition());
    }

    /**
     * Returns the nearest position to the element for incoming links.
     *
     * @param neighbour The neighbour whose nearest position should be found.
     * @return The nearest position to the element for incoming links.
     */
    public Position getClosestIncomingPosition(ActivityElement<?> neighbour) {
        return getClosestPosition(neighbour.getBottomPosition(), getTopPosition());
    }

    /**
     * Returns the nearest position to the element.
     *
     * @param neighbourPosition The position of the neighbour.
     * @param defaultPosition   The default position.
     * @return The nearest position to the element.
     */
    private Position getClosestPosition(Position neighbourPosition, Position defaultPosition) {
        List<Position> positions = new ArrayList<Position>();
        positions.add(getLeftPosition());
        positions.add(getRightPosition());

        int factor = getDefaultDimension().getWidth() / getDefaultDimension().getHeight();

        Position pos = defaultPosition;
        int distance = defaultPosition.calculateDistance(neighbourPosition, factor);

        // Iterate over positions and find closest
        for (Position postn : positions) {
            int currentDistance = postn.calculateDistance(neighbourPosition, factor);

            if (currentDistance < distance) {
                pos = postn;
                distance = currentDistance;
            }
        }

        return pos;
    }

    /**
     * Returns the source position for outgoing edges.
     *
     * @return The source position for outgoing edges.
     */
    public Position getTopPosition() {
        Position pos = getPosition().makeCopy();
        pos.appendToX(getDimension().getWidth() / 2);

        return pos;
    }

    /**
     * Returns the target position for incoming edges.
     *
     * @return The target position for incoming edges.
     */
    public Position getBottomPosition() {
        Position pos = getPosition().makeCopy();
        pos.appendToX(getDimension().getWidth() / 2);
        pos.appendToY(getDimension().getHeight());

        return pos;
    }

    /**
     * Returns the source position for outgoing edges inside the activity.
     *
     * @return The source position for outgoing edges inside the activity.
     */
    public Position getCenterPosition() {
        Position pos = getPosition().makeCopy();
        pos.appendToX(getDimension().getWidth() / 2);
        pos.appendToY(getDefaultDimension().getHeight());

        return pos;
    }

    /**
     * Returns the target/sour position on the left side.
     *
     * @return The target/sour position on the left side.
     */
    protected Position getLeftPosition() {
        Position pos = getPosition().makeCopy();
        pos.appendToY(getDimension().getHeight() / 2);

        return pos;
    }

    /**
     * Returns the target/sour position on the right side.
     *
     * @return The target/sour position on the right side.
     */
    protected Position getRightPosition() {
        Position pos = getPosition().makeCopy();
        pos.appendToX(getDimension().getWidth());
        pos.appendToY(getDimension().getHeight() / 2);

        return pos;
    }

    /**
     * Returns the value of position.
     * <p/>
     * If the position was not created before, it will be created now.
     *
     * @return The value of position.
     */
    public Position getPosition() {
        return this.position;
    }

    /**
     * Set the value of position to position.
     *
     * @param position The new value of position.
     */
    public void setPosition(Position position) {
        this.position = position;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ActivityElement<?> && getValue() != null) {
            ActivityElement<?> element = (ActivityElement<?>) obj;
            return getValue().equals(element.getValue());
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        if (getValue() == null) {
            return 0;
        }

        return getValue().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Value: " + getValue() + ", Parent (" + getParent().getValue() + "), Position ("
                + getPosition() + ")";
    }

}
