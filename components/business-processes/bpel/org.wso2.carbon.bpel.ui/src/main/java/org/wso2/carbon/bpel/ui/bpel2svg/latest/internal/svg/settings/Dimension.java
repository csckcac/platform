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
 * This class represents the width and the height of an
 * {@link org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.element.ActivityElement}.
 */
public class Dimension {

    /**
     * The width of the element in pixels.
     */
    private int width;

    /**
     * The height of the element in pixels.
     */
    private int height;

    /**
     * The left and right margin of the element in pixels.
     */
    private int marginHorizontal;

    /**
     * The top and bottom margin of the element in pixels.
     */
    private int marginVertical;

    /**
     * Returns the {@link Dimension} of a type image.
     *
     * @return The {@link Dimension} of a type image.
     */
    public static Dimension getTypeImageDimension() {
        return GlobalSettings.getInstance().getTypeImageSize();
    }

    /**
     * Returns the {@link Dimension} of a status image.
     *
     * @return The {@link Dimension} of a status image.
     */
    public static Dimension getStatusImageDimension() {
        return GlobalSettings.getInstance().getStatusImageSize();
    }

    /**
     * Returns the {@link Dimension} of a date image.
     *
     * @return The {@link Dimension} of a date image.
     */
    public static Dimension getDateImageDimension() {
        return GlobalSettings.getInstance().getDateImageSize();
    }

    public static Dimension getStatusBasedActionImageDimension() {
        return GlobalSettings.getInstance().getStatusBasedActionImageSize();
    }

    /**
     * Constructor of SVGDimension.
     *
     * @param width            The width of the element.
     * @param marginHorizontal The left and right margin of the element.
     * @param height           The height of the element.
     * @param marginVertical   The top and bottom margin of the element.
     */
    public Dimension(int width, int marginHorizontal, int height, int marginVertical) {
        this.width = width;
        this.marginHorizontal = marginHorizontal;
        this.height = height;
        this.marginVertical = marginVertical;
    }

    /**
     * Returns the value of width.
     *
     * @return The value of width.
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * Returns the value of width + marginHorizontal * 2.
     *
     * @return The value of width + marginHorizontal.
     */
    public int getWidthWithMargin() {
        return getWidth() + getMarginHorizontal() * 2;
    }

    /**
     * Adds the value to the width.
     *
     * @param width The value that should be added to the width.
     */
    public void appendToWidth(int width) {
        this.width += width;
    }

    /**
     * Returns the value of height.
     *
     * @return The value of height.
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * Returns the value of height + marginVertical * 2.
     *
     * @return The value of height + marginVertical * 2.
     */
    public int getHeightWithMargin() {
        return getHeight() + getMarginVertical() * 2;
    }

    /**
     * Adds the value to the height.
     *
     * @param height The value that should be added to the height.
     */
    public void appendToHeight(int height) {
        this.height += height;
    }

    /**
     * Returns the value of marginHorizontal.
     *
     * @return The value of marginHorizontal.
     */
    public int getMarginHorizontal() {
        return this.marginHorizontal;
    }

    /**
     * Returns the value of marginVertical.
     *
     * @return The value of marginVertical.
     */
    public int getMarginVertical() {
        return this.marginVertical;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Width: " + getWidth() + ", Height: " + getHeight() + ", Margin: " + getMarginHorizontal()
                + " " + getMarginVertical();
    }

}
