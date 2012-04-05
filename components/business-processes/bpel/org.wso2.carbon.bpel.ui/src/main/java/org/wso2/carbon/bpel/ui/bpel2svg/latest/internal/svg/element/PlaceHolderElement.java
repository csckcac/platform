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

import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.SVG;

/**
 * A place holder element.
 *
 * @author Gregor Latuske
 */
public class PlaceHolderElement
	extends ActivitySimpleElement {

	/** Serial version UID */
	private static final long serialVersionUID = -690450012557658310L;

	/**
	 * Constructor of PlaceHolderElement.
	 *
	 * @param parent The parent element.
	 */
	protected PlaceHolderElement(ActivityComplexElement<?> parent) {
		super(null, parent);
	}

	/** {@inheritDoc} */
	@Override
	public SVG getSVG() {
		return new SVG(getDimension());
	}

}
