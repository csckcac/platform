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
package org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.model.components.svg;

import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.model.SVGDataModel;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Settings;

/**
 * This class is used to set the granularity of the SVG.
 * 
 * @author Gregor Latuske
 */
public class ProcessInstanceSlider
	extends Slider {

	/**
	 * Constructor of ProcessInstanceSlider.
	 * 
	 * @param dataModel The associated SVGDataModel.
	 */
	public ProcessInstanceSlider(SVGDataModel dataModel) {
		super(dataModel);
	}

	/**
	 * Returns the value of maxRange.
	 * 
	 * @return The value of maxRange.
	 */
	public int getMaxRange() {
		return Settings.PI_GRANULARITY;
	}

}
