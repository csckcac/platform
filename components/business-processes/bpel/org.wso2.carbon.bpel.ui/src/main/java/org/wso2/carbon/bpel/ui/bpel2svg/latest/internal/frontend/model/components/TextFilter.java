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
package org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.model.components;

import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.model.SelectionDataModel;

/**
 * This class gives the {@link SelectionDataModel} filtering abilities for different properties of a
 * {@link org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.ProcessModel} or
 * {@link org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.ProcessInstance}.
 */
public class TextFilter
        extends Filter<String> {

    /**
     * Constructor of TextFilter.
     *
     * @param dataModel      The associated SeelctionDataModel.
     * @param filterProperty The filter property.
     */
    public TextFilter(SelectionDataModel<?, ?, ?> dataModel, String filterProperty) {
        super(dataModel, filterProperty);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        setInput("");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean filter(Object object) {
        if (object instanceof String) {

            // Object has to contain the input
            String string = ((String) object).toLowerCase();
            if (!string.contains(getInput().toLowerCase())) {
                return true;
            }
        }

        return false;
    }
}
