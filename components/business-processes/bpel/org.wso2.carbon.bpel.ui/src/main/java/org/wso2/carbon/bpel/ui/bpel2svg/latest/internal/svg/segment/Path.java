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
 * This class represents a path segment.
 *
 * @author Gregor Latuske
 */
public class Path
	extends Segment {

	/**
	 * Constructor of Path.
	 *
	 * @param pos1 The source position of the path segment.
	 * @param pos2 The target position of the path segment.
	 */
	public Path(Position position1, Position position2) {
		super();

		append("<path d=\"M " + position1.getX() + "," + position1.getY() + " ");
		append("L " + position2.getX() + "," + position2.getY() + "\" class=\"frame\"/>\n");
	}

}
