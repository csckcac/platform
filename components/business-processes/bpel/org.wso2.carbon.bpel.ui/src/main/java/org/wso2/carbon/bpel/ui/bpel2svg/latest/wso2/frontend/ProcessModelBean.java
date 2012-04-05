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

package org.wso2.carbon.bpel.ui.bpel2svg.latest.wso2.frontend;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.MainBean;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.model.ProcessInstanceDataModel;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.model.ProcessModelDataModel;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.model.SVGDataModel;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.BPIException;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.ProcessModel;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.service.BPIService;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This Class is used for maintain process model based Beans
 */
public class ProcessModelBean extends MainBean {
    /** The logger instance. */
	private static final Log log = LogFactory.getLog(ProcessModelBean.class);

    public ProcessModelBean(String processID) throws ClassNotFoundException, URISyntaxException, BPIException {
        this.svgDataModelMap = new HashMap<String, SVGDataModel>();

		this.svgDataModel = new SVGDataModel();
		this.piDataModel = new ProcessInstanceDataModel(this.svgDataModel);
		this.pmDataModel = new ProcessModelDataModel(this.piDataModel);

		// Initial refresh
		refresh(processID);
    }

    private String refresh(String processID) {
        try {
            BPIService service = new BPIService.BPIServiceFactory().createService();

            //Prepare the List required for pmDataModel.update
            ProcessModel model = service.getProcessModel(processID);
            List<ProcessModel> modelList = new ArrayList<ProcessModel>();
            modelList.add(model);

            this.pmDataModel.update(modelList);
        } catch (BPIException e) {
            log.error("Could not update process data model.", e);
        }

        return "";
    }
}
