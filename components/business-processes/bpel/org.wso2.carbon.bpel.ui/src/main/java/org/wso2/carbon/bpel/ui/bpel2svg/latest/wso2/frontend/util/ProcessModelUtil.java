/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpel.ui.bpel2svg.latest.wso2.frontend.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.MainBean;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.BPIException;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.ProcessModel;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.service.BPIService;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.SVG;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Settings;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.wso2.service.ProcessNotFoundException;

import java.util.List;

public class ProcessModelUtil {
    private static Log log = LogFactory.getLog(ProcessModelUtil.class);

    public static SVG generateSVGForProcess(MainBean bean, String processID) throws BPIException {
        ProcessModel model = getProcessModelFromProcessID(bean, processID);
        setRowIndexForProcess(bean, model);
        return generateSVGForProcessModel(model);
    }

    private static ProcessModel getProcessModelFromProcessID(MainBean bean, String processID) {
        for (ProcessModel model : bean.getPmDataModel().getItems().getItems()) {
            if (model.getPid().equals(processID)) {
                return model;
            }
        }
        throw new ProcessNotFoundException("ProcessModel was not found for process-id:" + processID);
    }

    private static SVG generateSVGForProcessModel(ProcessModel processModel) throws BPIException {
        try {
            BPIService service = new BPIService.BPIServiceFactory().createService();
            SVG svg = service.getSVG(processModel, new Settings());
            return svg;
        } catch (BPIException e) {
            log.error("Error occurred while SVG generation.", e);
            throw e;
        }
    }

    /**
     * Returns the rowIndex from the ListDataModel for the given processID
     * @param bean
     * @param processModel
     * @return
     */
    private static int getRowIndexForProcessModel(MainBean bean, ProcessModel processModel) {
        List<ProcessModel> processModelList = bean.getPmDataModel().getItems().getItems();
        for (int i =0; i < processModelList.size(); i++) {
            if (processModelList.get(i).getPid().equals(processModel.getPid())) {
                return i;
            }
        }

        throw new ProcessNotFoundException("ProcessModel:" + processModel.getPid() + " is not found in MainBean.");
    }

    /**
     * MainBean internally keep a ListDataModel (i.e. bean.getPmDataModel().getItems()) for store processes
     * and a rawIndex to keep the current selected process.
     * This method, deduce the rawIndex based on ProcessModel sequence in ListDataModel and set it.
     * After setting the rowIndex, we need to notify the observers as well using "bean.getPmDataModel().selectAndNotify()"
      * @param bean
     * @param processModel
     */
    private static void setRowIndexForProcess(MainBean bean, ProcessModel processModel) {
        int rowIndex = getRowIndexForProcessModel(bean, processModel);
        bean.getPmDataModel().getItems().setRowIndex(rowIndex);
        bean.getPmDataModel().selectAndNotify();
    }

    public static int getRowIndexForProcessModelID(MainBean bean, String processID) {
        ProcessModel processModel = getProcessModelFromProcessID(bean, processID);
        return getRowIndexForProcessModel(bean, processModel);
    }
}
