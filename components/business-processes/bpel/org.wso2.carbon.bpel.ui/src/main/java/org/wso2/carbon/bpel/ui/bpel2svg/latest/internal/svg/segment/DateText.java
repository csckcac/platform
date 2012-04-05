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

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * This class represents a date text segment.
 *
 * @author Gregor Latuske
 */
public class DateText
	extends Segment {

	/**
	 * Constructor of DateText.
	 *
	 * @param calendar The date to display.
	 * @param position The position of the text.
	 */
	public DateText(Calendar calendar, Position position) {
		super();

		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yy, HH:mm:ss");
		String text = (calendar == null) ? "-" : format.format(calendar.getTime());

		append("<text class=\"date\" ");
		append("x=\"" + position.getX() + "\" y=\"" + position.getY() + "\">");
		append(text + "</text>\n");
	}

}
