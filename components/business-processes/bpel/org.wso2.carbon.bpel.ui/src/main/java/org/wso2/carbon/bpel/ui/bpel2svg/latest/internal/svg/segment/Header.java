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
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.GlobalSettings;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Settings;

/**
 * This class represents the header segment.
 *
 * @author Gregor Latuske
 */
public class Header
	extends Segment {

	/**
	 * Constructor of Header.
	 *
	 * @param dimension The {@link Dimension} of the header segment.
	 * @param settings The associated {@link Settings}.
	 */
	public Header(Dimension dimension, Settings settings) {
		super();

		GlobalSettings globalSettings = GlobalSettings.getInstance();

		//append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n\n");

		// Import CSS
		append("<?xml-stylesheet href=\"" + globalSettings.getCssPath() + "\" type=\"text/css\"?>\n\n");

		append("<svg ");
		append("xmlns=\"http://www.w3.org/2000/svg\" ");
		append("xmlns:xlink=\"http://www.w3.org/1999/xlink\" ");
		append("version=\"1.2\" baseProfile=\"tiny\" ");
		append("width=\"" + dimension.getWidthWithMargin() + "\" ");
		append("height=\"" + dimension.getHeightWithMargin() + "\">\n");

		
		// Define arrow heads
		String path = "M -5.25,2 L -1.25,0 L -5.25,-2 Z";
		if (settings.isUseCompactMode()) {
			path = "M -3.25,1 L -1.25,0 L -3.25,-1 Z";
		}

		append("<defs>\n");
		append("<marker refX=\"0\" refY=\"0\" orient=\"auto\" id=\"ah\" class=\"arrowhead\">\n");
		append("<path d=\"" + path + "\" class=\"arrowhead\" />\n");
		append("</marker>\n");
		append("<marker refX=\"0\" refY=\"0\" orient=\"auto\" id=\"fah\" class=\"arrowhead\">\n");
		append("<path d=\"" + path + "\" class=\"arrowhead blue-ah\" />\n");
		append("</marker>\n");
		append("<marker refX=\"0\" refY=\"0\" orient=\"auto\" id=\"hah\" class=\"arrowhead\">\n");
		append("<path d=\"" + path + "\" class=\"arrowhead orange-ah\" />\n");
		append("</marker>\n");
		append("</defs>\n\n");

		// Import JavaScripts
        for(String jsPath : globalSettings.getJsPaths()) {
            append("<script type=\"text/ecmascript\" xlink:href=\"" + jsPath + "\" />\n\n");
        }

        append("<div id=\"dcontainer\"></div>\n");

	}
}
