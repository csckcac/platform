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

import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Position;

/**
 * This class represents a text segment.
 */
public class Text extends Segment {

    /**
     * Constructor of Text.
     *
     * @param text       The text that should be displayed.
     * @param position   The {@link Position} of the text.
     * @param cssClass   The CSS class of the text.
     * @param attributes Additional attributes.
     */
    public Text(String text, Position position, String cssClass, String... attributes) {
        this(text, -1, position, cssClass, attributes);
    }

    /**
     * Constructor of SVGText.
     *
     * @param title      The text that should be displayed.
     * @param maxLength  The maximum length of the text (-1 stands for full lenght)
     * @param position   The {@link Position} of the text.
     * @param cssClass   The CSS class of the text.
     * @param attributes Additional attributes.
     */
    public Text(String title, int maxLength, Position position, String cssClass, String... attributes) {
        String text = title;
        // Cut title
        if (maxLength >= 0 && text.length() > maxLength) {
            text = text.substring(0, maxLength) + "...";
        }

        // XLink is used to show tool tip (text element does not work yet)
        append("<a xlink:title=\"" + title + "\">\n");
        append("<text class=\"" + cssClass + "\" ");
        append("x=\"" + position.getX() + "\" y=\"" + position.getY() + "\"");

        // Append additional attributes
        for (String attribute : attributes) {
            append(" " + attribute);
        }

        append(">" + text + "\n");
        append("\t<title>" + title + "</title>\n");
        append("</text>\n");
        append("</a>\n");
    }

}
