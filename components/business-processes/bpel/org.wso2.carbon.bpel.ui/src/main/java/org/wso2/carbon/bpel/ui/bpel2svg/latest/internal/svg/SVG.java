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
package org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg;

import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.segment.Segment;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Dimension;

/**
 * This class represents the overall output of the SVG (inclusively the dimension).
 * 
 * @author Gregor Latuske
 */
public class SVG {

	/** The dimension of the SVG. */
	private final Dimension dimension;

	/** The output of the SVG as a StringBuffer. */
	private final StringBuffer svg;

	/**
	 * Constructor of SVG.
	 * 
	 * @param dimension The dimension of the SVG.
	 */
	public SVG(Dimension dimension) {
		this.dimension = dimension;
		this.svg = new StringBuffer();
	}

	/**
	 * Appends the {@link SVG} to the overall output.
	 * 
	 * @param svg The {@link SVG}, that should be appended.
	 */
	public void append(SVG svg) {
		this.svg.append(svg.getOutput());
	}

	/**
	 * Appends the {@link Segment} to the overall output.
	 * 
	 * @param segment The {@link Segment}, that should be appended.
	 */
	public void append(Segment segment) {
		this.svg.append(segment.toString());
	}

	/**
	 * Returns the overall output of the SVG as a String.
	 * 
	 * @return The overall output of the SVG as a String.
	 */
	public String getOutput() {
		return this.svg.toString();
	}

	/**
	 * Returns the HTML code needed to display the SVG.
	 * 
	 * @return The HTML code needed to display the SVG.
	 */
	public String getHtml() {
		StringBuffer sb = new StringBuffer();

		sb.append("<object ");
		sb.append("width=\"" + this.dimension.getWidth() + "\" ");
		sb.append("height=\"" + this.dimension.getHeight() + "\" ");
		sb.append("type=\"image/svg+xml\" style=\"margin:auto;\" ");
		sb.append("data=\"./svg?id={0}\">\n");
		sb.append("<param name=\"src\" value=\"./svg\" />\n");
		sb.append("</object>\n");

		return sb.toString();
	}

}
