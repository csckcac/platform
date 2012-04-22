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
package org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings;

/**
 * This class represents the position of a
 * {@link org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.element.ActivityElement} in the coordinates.
 */
public class Position {

    /**
     * The position of the element on the x axis.
     */
    private int x;

    /**
     * The position of the element on the y axis.
     */
    private int y;

    /**
     * Constructor of SVGDimension.
     */
    public Position() {
        this.x = 0;
        this.y = 0;
    }

    /**
     * Constructor of SVGDimension.
     *
     * @param x The position of the element on the x axis.
     * @param y The position of the element on the y axis.
     */
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Calculates the distance to the given position.
     * <p/>
     * The factor is used to compensate the difference between width and height of an activity element.
     * <p/>
     * E.g.: (10,10) & (20,15) --> abs(10 - 20) + abs(10 - 15) * factor --> 10 + 5 * factor
     *
     * @param position The given position.
     * @param factor   Factor to be multiplied?
     * @return The distance to the given position.
     */
    public int calculateDistance(Position position, int factor) {
        int distance = 0;

        distance += Math.abs(getX() - position.getX());
        distance += Math.abs(getY() - position.getY()) * factor;

        return distance;
    }

    /**
     * similar to clone()
     */
    public Position makeCopy() {
        return new Position(getX(), getY());
    }

    /**
     * Returns the value of x.
     *
     * @return The value of x.
     */
    public int getX() {
        return this.x;
    }

    /**
     * Set the value of x to x.
     *
     * @param x The new value of x.
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Appends the given value to the position on the x axis.
     *
     * @param x The value, that should be append to the position on the x axis.
     */
    public void appendToX(int x) {
        setX(getX() + x);
    }

    /**
     * Sets the x value to the center of both {@link Dimension}s, so the second element will be in middle of
     * the first.
     *
     * @param dim1 The {@link Dimension} of the outer element.
     * @param dim2 The {@link Dimension} of the inner element.
     */
    public void centerX(Dimension dim1, Dimension dim2) {
        appendToX((dim1.getWidth() - dim2.getWidthWithMargin()) / 2 + dim2.getMarginHorizontal());
    }

    /**
     * Returns the value of y.
     *
     * @return The value of y.
     */
    public int getY() {
        return this.y;
    }

    /**
     * Set the value of y to y.
     *
     * @param y The new value of y.
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * Appends the given value to the position on the y axis.
     *
     * @param y The value, that should be append to the position on the y axis.
     */
    public void appendToY(int y) {
        setY(getY() + y);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "X: " + getX() + ", Y: " + getY();
    }

}
