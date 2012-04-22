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
package org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.model.ProcessInstanceDataModel;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.model.ProcessModelDataModel;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.model.SVGDataModel;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.BPIException;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.service.BPIService;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.service.BPIService.BPIServiceFactory;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * This bean delivers the list of process models their instances and the graph with the status information of
 * the selected process instance.
 */
public class MainBean /*implements Serializable*/ {

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 4909256204216744458L;

    /**
     * Name of the bean.
     */
    public static final String NAME = "main";

    /**
     * The logger instance.
     */
    private static final Log LOG = LogFactory.getLog(MainBean.class);

    /**
     * The data model for the
     * {@link org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.ProcessModel}s.
     */
    protected ProcessModelDataModel pmDataModel;

    /**
     * The data model for the
     * {@link org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.ProcessInstance}s.
     */
    protected ProcessInstanceDataModel piDataModel;

    /**
     * The data model for the
     * {@link org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.SVG} of the selected
     * {@link org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.ProcessInstance}.
     */
    protected SVGDataModel svgDataModel;  //TODO: Previously this was final. But to change this dynamically I removed the final. Need to review this and add a proper fix.

    /**
     * The map with the SVG data models.
     */
    protected Map<String, SVGDataModel> svgDataModelMap;

    protected MainBean() throws ClassNotFoundException, URISyntaxException, BPIException {
        this.svgDataModelMap = new HashMap<String, SVGDataModel>();

        this.svgDataModel = new SVGDataModel();
        this.piDataModel = new ProcessInstanceDataModel(this.svgDataModel);
        this.pmDataModel = new ProcessModelDataModel(this.piDataModel);

        // Initial refresh
        refresh();
    }

    /**
     * Refreshes the data of the DataModels.
     *
     * @return "" (empty String)
     */
    public String refresh() {
        try {
            BPIService service = new BPIServiceFactory().createService();
            this.pmDataModel.update(service.getProcessModels());
        } catch (BPIException exception) {
            LOG.error("Could not retrieve process data.", exception);
        }

        return "";
    }

    /**
     * Returns the value of pmDataModel.
     *
     * @return The value of pmDataModel.
     */
    public ProcessModelDataModel getPmDataModel() {
        return this.pmDataModel;
    }

    /**
     * Returns the value of piDataModel.
     *
     * @return The value of piDataModel.
     */
    public ProcessInstanceDataModel getPiDataModel() {
        return this.piDataModel;
    }

    /**
     * Returns the value of svgDataModel.
     *
     * @return The value of svgDataModel.
     */
    public SVGDataModel getSvgDataModel() {
        return this.svgDataModel;
    }

    public void setSvgDataModel(SVGDataModel svgDataModel) {
        this.svgDataModel = svgDataModel;
    }

    /**
     * Returns the SVG data model to the given id.
     *
     * @param id The given id.
     * @return The SVG data model to the given id.
     */
    public SVGDataModel getSvgDataModel(String id) {
        // Return stored SVG data model or create new
        SVGDataModel svgDataMdl = this.svgDataModelMap.get(id);

        // Create new SVG data model
        if (svgDataMdl == null && id != null) {
            svgDataMdl = new SVGDataModel();
            svgDataMdl.update(this.piDataModel.getSelection().get(0));

            this.svgDataModelMap.put(id, svgDataMdl);

            //Here the SVG model will be cloned from this.svgDataModel and re-copied to the svgDataModelMap
            if (this.svgDataModel.getSvg() != null) {
                if (this.svgDataModel.getProcessInstance() != null) {       //i.e. svgModel was generated from a process instance
                    if (this.svgDataModel.getProcessInstance().getIid().equals(id)) {         //TODO: denis@wso2.com added this block to avoid the consistency between this.svgDataModel and this.svgDataModelMap
                        this.svgDataModelMap.put(id, this.svgDataModel);
                    }
                } else {   //i.e. svgModel was generated from a process model
                    this.svgDataModelMap.put(id, this.svgDataModel);
                }
            }
        }

        return this.svgDataModelMap.get(id);
    }
}
