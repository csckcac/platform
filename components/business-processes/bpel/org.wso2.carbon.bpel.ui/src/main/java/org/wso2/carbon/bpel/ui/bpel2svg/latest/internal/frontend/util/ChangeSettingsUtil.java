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

package org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.util;

import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.MainBean;

public class ChangeSettingsUtil {
    public static void selectProcessModel(MainBean bean, int rowIndexOfProcessModel) {
        /* Simulate original AJAX request */
        bean.getPmDataModel().getItems().setRowIndex(rowIndexOfProcessModel);
        bean.getPmDataModel().selectAndNotify();
        bean.setSvgDataModel(bean.getPmDataModel().getProcessModelSVGDataModel()); //TODO: We should be able to update this svgModel internally from selectAndNotify().
    }

    public static void selectProcessInstance(MainBean bean, int rowIndexOfProcessInstance) {
        /* Simulate original AJAX request */
        bean.getPiDataModel().getItems().setRowIndex(rowIndexOfProcessInstance);
        bean.getPiDataModel().selectAndNotify();
    }
}
