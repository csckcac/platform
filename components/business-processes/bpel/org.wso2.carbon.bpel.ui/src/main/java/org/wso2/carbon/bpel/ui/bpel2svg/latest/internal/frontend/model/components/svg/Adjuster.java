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
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.model.components.ViewMode;

/**
 * This class is the basis for all justing functions of the SVG.
 * 
 * @author Gregor Latuske
 */
public abstract class Adjuster<S> {

	/** The view mode of the data model (maximise & minimise). */
	private final ViewMode viewMode;

	/** The selected item(s). */
	private S selection;

	/** The associated SVGDataModel. */
	private final SVGDataModel dataModel;

	/**
	 * Constructor of Adjuster.
	 * 
	 * @param dataModel The associated SVGDataModel.
	 */
	public Adjuster(SVGDataModel dataModel) {
		this.viewMode = new ViewMode(false);
		this.dataModel = dataModel;

		reset();
	}

	/**
	 * Resets the component.
	 */
	public abstract void reset();

	/**
	 * Returns the value of viewMode.
	 * 
	 * @return The value of viewMode.
	 */
	public ViewMode getViewMode() {
		return this.viewMode;
	}

	/**
	 * Returns the value of selection.
	 * 
	 * @return The value of selection.
	 */
	public S getSelection() {
		return this.selection;
	}

	/**
	 * Sets the value of leftSelection to selection.
	 * 
	 * @param leftSelection The new value of selection.
	 */
	public void setSelection(S selection) {
		this.selection = selection;
	}

	/**
	 * Generates the SVG output.
	 */
	protected void generateSVG() {
		this.dataModel.updateSVG();
	}

}
