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
package org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.segment;

import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Dimension;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Position;

import java.awt.*;

/**
 * This class represents a rectangle segment.
 *
 * @author Gregor Latuske
 */
public class Rectangle
	extends Segment {

	/**
	 * Constructor of Rectangle.
	 *
	 * @param dimension The {@link Dimension} of the rectangle.
	 * @param position The {@link Position} of the rectangle.
	 * @param cssClass The CSS class of the rectangle.
	 */
	public Rectangle(Dimension dimension, Position position, String cssClass) {
		this(dimension, position, cssClass, null);
	}

	/**
	 * Constructor of Rectangle.
	 *
	 * @param dimension The {@link Dimension} of the rectangle.
	 * @param position The {@link Position} of the rectangle.
	 * @param cssClass The CSS class of the rectangle.
	 * @param color The background colour of the rectangle.
	 */
	public Rectangle(Dimension dimension, Position position, String cssClass, Color color) {
		super();

		generateRectangle(dimension, position, cssClass, color);
	}

    protected void generateRectangle(Dimension dimension, Position position, String cssClass, Color color) {
        append("<rect class=\"" + cssClass + "\" rx=\"10\" ry=\"10\" ");
		append("width=\"" + dimension.getWidth() + "\" ");
		append("height=\"" + dimension.getHeight() + "\" ");
		append("x=\"" + position.getX() + "\" ");
		append("y=\"" + position.getY() + "\" ");

		if (color != null) {
			String colorString = "";

			if (color.getRed() <= 0xf) {
				colorString += "0";
			}
			colorString += Integer.toHexString(color.getRed());

			if (color.getGreen() <= 0xf) {
				colorString += "0";
			}
			colorString += Integer.toHexString(color.getGreen());

			if (color.getBlue() <= 0xf) {
				colorString += "0";
			}
			colorString += Integer.toHexString(color.getBlue());

			append("style=\"fill: #" + colorString + "\" ");
		}

		append("/>\n\n");
    }

}
