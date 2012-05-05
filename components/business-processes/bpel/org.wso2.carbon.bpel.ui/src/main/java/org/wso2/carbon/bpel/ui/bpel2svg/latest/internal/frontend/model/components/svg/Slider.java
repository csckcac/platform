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
 * This class is the basis of the ProcessModel and the ProcessInstance slider.
 */
public abstract class Slider extends Adjuster<String> {

    /**
     * Constructor of ProcessModelSlider.
     *
     * @param dataModel The associated SVGDataModel.
     */
    public Slider(SVGDataModel dataModel) {
        super(dataModel);

        reset();
    }

    /**
     * Returns the value of maxRange.
     *
     * @return The value of maxRange.
     */
    public abstract int getMaxRange();

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        super.setSelection("0");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSelection(String selection) {
        super.setSelection(selection);

        generateSVG();
    }

}
