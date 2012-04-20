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

import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.GlobalSettings;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Position;

import static org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Dimension.getDateImageDimension;

/**
 * This class represents a date image segment.
 *
 * @author Gregor Latuske
 */
public class DateImage
	extends Image {

	/**
	 * Constructor of DateImage.
	 *
	 * @param name The name of the image.
	 * @param position The position of the image.
	 * @param enabled Is the image enabled?
	 */
	public DateImage(String name, Position position, boolean enabled) {
		super(GlobalSettings.getInstance().getDateImagePath(), name, GlobalSettings.getInstance()
			.getDateImageExtension(), getDateImageDimension(), position, enabled);
	}

}
