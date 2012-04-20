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

import static org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Dimension.getTypeImageDimension;

/**
 * This class represents a type image segment.
 *
 * @author Gregor Latuske
 */
public class TypeImage
	extends Image {

	/**
	 * Constructor of TypeImage.
	 *
	 * @param name The name of the image.
	 * @param position The {@link Position} of the image.
	 */
	public TypeImage(String name, Position position) {
		super(GlobalSettings.getInstance().getTypeImagePath(), name, GlobalSettings.getInstance()
			.getTypeImageExtension(), getTypeImageDimension(), position, true);
	}

}
