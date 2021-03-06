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
package org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg;

import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.activity.ActivityRoot;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.element.ActivityRootElement;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Settings;

/**
 * This class is used to generate the SVG.
 */
public final class SVGGenerator {
    private SVGGenerator() {
    }

    /**
     * Generates the SVG.
     *
     * @param root     Root Activity
     * @param settings Settings
     * @return The generated SVG.
     */
    public static SVG generate(ActivityRoot root, Settings settings) {
        ActivityRootElement rootElement = new ActivityRootElement(root, settings);
        return rootElement.getSVG();
    }
}
