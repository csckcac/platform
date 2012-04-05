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
package org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.element;

import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.activity.ActivitySimple;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.SVG;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.segment.ActivityBox;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Dimension;

/**
 * The SVG wrapper for the {@link ActivitySimple}.
 *
 * @author Gregor Latuske
 */
public class ActivitySimpleElement
	extends ActivityElement<ActivitySimple> {

	/** Serial version UID */
	private static final long serialVersionUID = 8907001384835603101L;

	/**
	 * Constructor of ActivitySimpleElement.
	 *
	 * @param value The associated {@link ActivitySimple}.
	 * @param parent The parent element.
	 */
	protected ActivitySimpleElement(ActivitySimple value, ActivityComplexElement<?> parent) {
		super(value, parent);
	}

	/** {@inheritDoc} */
	@Override
	public SVG getSVG() {
		SVG svg = new SVG(getDimension());
		svg.append(new ActivityBox(getValue(), getDimension(), getPosition(), getSettings()));

		return svg;
	}

	/** {@inheritDoc} */
	@Override
	protected Dimension calculateDimension() {
		return getDefaultDimension();
	}

}
