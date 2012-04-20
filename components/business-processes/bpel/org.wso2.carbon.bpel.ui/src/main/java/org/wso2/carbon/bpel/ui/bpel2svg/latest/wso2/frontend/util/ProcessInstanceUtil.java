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

import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.MainBean;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.ProcessInstance;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.wso2.service.InstanceNotFoundException;

import java.util.List;

public final class ProcessInstanceUtil {

    private ProcessInstanceUtil() {
    }

//    public static SVG generateSVGForProcessInstance(MainBean bean, int rowIndex) throws BPIException {
//        ProcessInstance processInstance = getProcessInstanceFromRowIndex(bean, rowIndex);
//        return generateSVGForProcessInstance(processInstance);
//    }
//
//    public static SVG generateSVGForProcessInstance(MainBean bean, String instanceID) throws BPIException {
//        ProcessInstance instance = getProcessInstanceFromInstanceID(bean, instanceID);
//        setRowIndexForInstance(bean, instance);
//        return generateSVGForProcessInstance(instance);
//    }

//    /**
//     * MainBean internally keep a ListDataModel (i.e. bean.getPiDataModel().getItems()) for store processes instances
//     * and a rawIndex to keep the current selected process instance.
//     * This method, deduce the rawIndex based on ProcessInstance sequence in ListDataModel and set it.
//     * After setting the rowIndex, we need to notify the observers as well using "bean.getPiDataModel().selectAndNotify()"
//     * @param bean MainBean
//     * @param instance Process Instance
//     */
//    private static void setRowIndexForInstance(MainBean bean, ProcessInstance instance) {
//        int rowIndex = getRowIndexForProcessInstance(bean, instance);
//        bean.getPiDataModel().getItems().setRowIndex(rowIndex);
//        bean.getPiDataModel().selectAndNotify();
//    }

    public static int getRowIndexForProcessInstanceID(MainBean bean, String instanceID) {
        ProcessInstance instance = getProcessInstanceFromInstanceID(bean, instanceID);
        return getRowIndexForProcessInstance(bean, instance);
    }

    private static int getRowIndexForProcessInstance(MainBean bean, ProcessInstance instance) {
        List<ProcessInstance> processInstanceList = bean.getPiDataModel().getItems().getItems();
        for (int i = 0; i < processInstanceList.size(); i++) {
            if (processInstanceList.get(i).getIid().equals(instance.getIid())) {
                return i;
            }
        }

        throw new InstanceNotFoundException("ProcessInstance:" + instance.getIid() + " is not found in MainBean.");
    }

//    private static ProcessInstance getProcessInstanceFromRowIndex(MainBean bean, int rowIndex) {
//        ListDataModel<ProcessInstance> instanceList = bean.getPiDataModel().getItems();
//        ProcessInstance processInstance = instanceList.getItems().get(rowIndex);
//
//        return processInstance;
//    }

//    private static String getProcessInstanceIDFromRowIndex(MainBean bean, int rowIndex) {
//        return getProcessInstanceFromRowIndex(bean, rowIndex).getIid();
//    }
//
//    private static SVG generateSVGForProcessInstance(ProcessInstance instance) throws BPIException {
//        try {
//            BPIService service = new BPIService.BPIServiceFactory().createService();
//            return service.getSVG(instance, new Settings());
//        } catch (BPIException e) {
//            log.error("Error occurred while SVG generation.", e);
//            throw e;
//        }
//    }

    private static ProcessInstance getProcessInstanceFromInstanceID(MainBean bean, String instanceID) {
        for (ProcessInstance instance : bean.getPiDataModel().getItems().getItems()) {
            if (instance.getIid().equals(instanceID)) {
                return instance;
            }
        }
        throw new InstanceNotFoundException("Instance was not found for instance-id:" + instanceID);
    }
}
