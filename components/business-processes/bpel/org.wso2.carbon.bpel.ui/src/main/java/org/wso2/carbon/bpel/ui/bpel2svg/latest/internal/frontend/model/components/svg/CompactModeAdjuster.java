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

/**
 * This class is used to disable or enabled the compact mode.
 * 
 * @author Gregor Latuske
 */
public class CompactModeAdjuster
	extends Adjuster<Boolean> {

	/**
	 * Constructor of ViewModeAdjuster.
	 * 
	 * @param dataModel The associated SVGDataModel.
	 */
	public CompactModeAdjuster(SVGDataModel dataModel) {
		super(dataModel);
	}
	
	/** {@inheritDoc} */
	@Override
	public void reset() {
		super.setSelection(false);
	}

	/** {@inheritDoc} */
	@Override
	public void setSelection(Boolean selection) {
		super.setSelection(selection);

		generateSVG();
	}

}
