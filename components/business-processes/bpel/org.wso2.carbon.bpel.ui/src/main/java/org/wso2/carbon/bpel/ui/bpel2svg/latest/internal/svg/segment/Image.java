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

/**
 * This class represents an image segment.
 *
 * @author Gregor Latuske
 */
public abstract class Image
	extends Segment {

	/**
	 * Constructor of Image.
	 *
	 * @param path The path to the image.
	 * @param name The name of the image.
	 * @param extension The file extension of the image.
	 * @param dimension The {@link Dimension} of the image.
	 * @param position The {@link Position} of the image.
	 * @param enabled Is the image enabled?
	 */
	public Image(String path, String name, String extension, Dimension dimension, Position position,
		boolean enabled) {
		super();

		float opacity = enabled ? 1.0f : 0.33f;

		// XLink is used to show tool tip (title element does not work yet)
		append("<a xlink:title=\"" + name + "\">\n");
		append("\t<image xlink:href=\"" + getImage(path, name, extension) + "\" ");
		append("\twidth=\"" + dimension.getWidth() + "\" ");
		append("\theight=\"" + dimension.getHeight() + "\" ");
		append("\tx=\"" + position.getX() + "\" ");
		append("\ty=\"" + position.getY() + "\" ");
		append("\topacity=\"" + opacity + "\">\n");
		append("\t\t<title role=\"tooltip\">" + name + "</title>\n");
		append("\t</image>\n");
		append("</a>\n");
	}

	/**
	 * Returns the complete path to the image.
	 *
	 * @param path The path to the image.
	 * @param name The name of the image.
	 * @param extension The file extension of the image.
	 * @return The complete path to the image.
	 */
	private String getImage(String path, String name, String extension) {
		StringBuffer sb = new StringBuffer();
		sb.append(path);
		sb.append(name.toLowerCase());
		sb.append(extension);

		return sb.toString();
	}

}
