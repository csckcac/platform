/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.bpel.ui.bpel2svg.latest.wso2.svg.segment;

import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.segment.Segment;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Dimension;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.GlobalSettings;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Position;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.wso2.svg.javascript.JSFunction;

import static org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Dimension.STATUS_BASED_ACTION_IMAGE;

public abstract class Button extends Segment {

    public Button(String path, String name, String extension, Dimension dimension, Position position, JSFunction jsFunction) {
        super();
        generateButton(path, name, extension, dimension, position, jsFunction);
    }

    public Button(String name, JSFunction jsFunction, Position buttonPos) {
        this(GlobalSettings.getInstance().getStatusBasedActionImagePath(), name, GlobalSettings.getInstance().getStatusBasedActionImageExtension(), STATUS_BASED_ACTION_IMAGE(), buttonPos, jsFunction);
    }

    /**
     * The default implementation generates and image.
     * @param path
     * @param name
     * @param extension
     * @param dimension
     * @param position
     * @param jsFunction - there should be a js function which should be visible to the svg generated.
     */
    protected void generateButton(String path, String name, String extension, Dimension dimension, Position position, JSFunction jsFunction) {
        // XLink is used to show tool tip (title element does not work yet)
		append("<a onmouseup=\"" + jsFunction.generateJSFunctionSignature() + "\" xlink:title=\"" + name + "\">\n");
		append("\t<image xlink:href=\"" + geButtonImage(path, name, extension) + "\" ");
		append("\twidth=\"" + dimension.getWidth() + "\" ");
		append("\theight=\"" + dimension.getHeight() + "\" ");
		append("\tx=\"" + position.getX() + "\" ");
		append("\ty=\"" + position.getY() + "\" >\n");
		append("\t\t<title role=\"tooltip\">" + name + "</title>\n");
		append("\t</image>\n");
		append("</a>\n");
    }

    //TODO: This is a code duplication with Image.getImage. Need to move them to a util class
    private String geButtonImage(String path, String name, String extension) {
        StringBuffer sb = new StringBuffer();
		sb.append(path);
		sb.append(name.toLowerCase());
		sb.append(extension);

		return sb.toString();
    }
}
