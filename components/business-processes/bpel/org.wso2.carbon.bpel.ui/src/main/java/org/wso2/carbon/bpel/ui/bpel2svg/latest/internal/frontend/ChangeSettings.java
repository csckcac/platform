/*
   Copyright 2011 Jakob Krein

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

import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.util.ChangeSettingsUtil;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.frontend.util.MainBeanUtil;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.status.ProcessInstanceStatus;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.status.ProcessModelStatus;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.status.Status;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This servlet simply receives the change request from the index.jsp, changes
 * the {@link MainBean} if necessary and redirects to the index.jsp again
 * 
 * @author Jakob Krein
 */
public class ChangeSettings extends HttpServlet {

	private static final long serialVersionUID = -4077634385476488598L;
	
	/** {@inheritDoc} */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {

        String instanceId = req.getParameter("iid");
        if (instanceId == null) {
            throw new ServletException("iid is null");
        }
        String mainBeanId = MainBeanUtil.generateMainBeanId(instanceId);
		
		MainBean bean = (MainBean)req.getSession().getAttribute(mainBeanId);
		
		String selectInstance = req.getParameter("select_instance");
		if(selectInstance != null && !selectInstance.equals("")) {
            ChangeSettingsUtil.selectProcessInstance(bean, Integer.parseInt(selectInstance));
		}
		
		String selectProcessModel = req.getParameter("select_process_model");
		if(selectProcessModel != null && !selectProcessModel.equals("")) {
			ChangeSettingsUtil.selectProcessModel(bean, Integer.parseInt(selectProcessModel));
		}
		
		// Are there any buttons of the highlighting types section pressed?
		String activityTypesHighlightingAdd = req.getParameter("activitytypes_highlighting_add");
		String activityTypesHighlightingRemove = req.getParameter("activitytypes_highlighting_remove");
		String activityTypesHighlightingAddAll = req.getParameter("activitytypes_highlighting_addall");
		String activityTypesHighlightingRemoveAll = req.getParameter("activitytypes_highlighting_removeall");
		
		if(activityTypesHighlightingAdd != null) {
			
			// Get the selection
			String[] activityTypesHighlightingLeft = req.getParameterValues("activitytypes_highlighting_left");
			
			// Simulate the original AJAX request
			if(activityTypesHighlightingLeft != null) {
				bean.getSvgDataModel().getTypeHighlighting().setSelection(activityTypesHighlightingLeft);
				bean.getSvgDataModel().getTypeHighlighting().add();
			}
		} else if(activityTypesHighlightingRemove != null) {
			
			// Get the selection
			String[] activityTypesHighlightingRight = req.getParameterValues("activitytypes_highlighting_right");
			
			// Simulate the original AJAX request
			if(activityTypesHighlightingRight != null) {
				bean.getSvgDataModel().getTypeHighlighting().setSelectionRight(activityTypesHighlightingRight);
				bean.getSvgDataModel().getTypeHighlighting().remove();
			}
		} else if(activityTypesHighlightingAddAll != null) {
			
			// Simulate the original AJAX request
			bean.getSvgDataModel().getTypeHighlighting().addAll();
			
		} else if(activityTypesHighlightingRemoveAll != null) {
			
			// Simulate the original AJAX request
			bean.getSvgDataModel().getTypeHighlighting().removeAll();
			
		}

		
		
		// Are there any buttons of the highlighting names section pressed?
		String activityNamesHighlightingAdd = req.getParameter("activitynames_highlighting_add");
		String activityNamesHighlightingRemove = req.getParameter("activitynames_highlighting_remove");
		String activityNamesHighlightingAddAll = req.getParameter("activitynames_highlighting_addall");
		String activityNamesHighlightingRemoveAll = req.getParameter("activitynames_highlighting_removeall");
		
		if(activityNamesHighlightingAdd != null) {
			
			// Get the selection
			String[] activityNamesHighlightingLeft = req.getParameterValues("activitynames_highlighting_left");
			
			// Simulate the original AJAX request
			if(activityNamesHighlightingLeft != null) {
				bean.getSvgDataModel().getNameHighlighting().setSelection(activityNamesHighlightingLeft);
				bean.getSvgDataModel().getNameHighlighting().add();
			}
		} else if(activityNamesHighlightingRemove != null) {
			
			// Get the selection
			String[] activityNamesHighlightingRight = req.getParameterValues("activitynames_highlighting_right");
			
			// Simulate the original AJAX request
			if(activityNamesHighlightingRight != null) {
				bean.getSvgDataModel().getNameHighlighting().setSelectionRight(activityNamesHighlightingRight);
				bean.getSvgDataModel().getNameHighlighting().remove();
			}
		} else if(activityNamesHighlightingAddAll != null) {
			
			// Simulate the original AJAX request
			bean.getSvgDataModel().getNameHighlighting().addAll();
			
		} else if(activityNamesHighlightingRemoveAll != null) {
			
			// Simulate the original AJAX request
			bean.getSvgDataModel().getNameHighlighting().removeAll();
			
		}
		
		// Are there any buttons of the omitting types section pressed?
		String activityTypesOmittingAdd = req.getParameter("activitytypes_omitting_add");
		String activityTypesOmittingRemove = req.getParameter("activitytypes_omitting_remove");
		String activityTypesOmittingAddAll = req.getParameter("activitytypes_omitting_addall");
		String activityTypesOmittingRemoveAll = req.getParameter("activitytypes_omitting_removeall");
		
		if(activityTypesOmittingAdd != null) {
			
			// Get the selection
			String[] activityTypesOmittingLeft = req.getParameterValues("activitytypes_omitting_left");
			
			// Simulate the original AJAX request
			if(activityTypesOmittingLeft != null) {
				bean.getSvgDataModel().getTypeOmitting().setSelection(activityTypesOmittingLeft);
				bean.getSvgDataModel().getTypeOmitting().add();
			}
		} else if(activityTypesOmittingRemove != null) {
			
			// Get the selection
			String[] activityTypesOmittingRight = req.getParameterValues("activitytypes_omitting_right");
			
			// Simulate the original AJAX request
			if(activityTypesOmittingRight != null) {
				bean.getSvgDataModel().getTypeOmitting().setSelectionRight(activityTypesOmittingRight);
				bean.getSvgDataModel().getTypeOmitting().remove();
			}
		} else if(activityTypesOmittingAddAll != null) {
			
			// Simulate the original AJAX request
			bean.getSvgDataModel().getTypeOmitting().addAll();
			
		} else if(activityTypesOmittingRemoveAll != null) {
			
			// Simulate the original AJAX request
			bean.getSvgDataModel().getTypeOmitting().removeAll();
			
		}

		
		
		// Are there any buttons of the omitting names section pressed?
		String activityNamesOmittingAdd = req.getParameter("activitynames_omitting_add");
		String activityNamesOmittingRemove = req.getParameter("activitynames_omitting_remove");
		String activityNamesOmittingAddAll = req.getParameter("activitynames_omitting_addall");
		String activityNamesOmittingRemoveAll = req.getParameter("activitynames_omitting_removeall");
		
		if(activityNamesOmittingAdd != null) {
			
			// Get the selection
			String[] activityNamesOmittingLeft = req.getParameterValues("activitynames_omitting_left");
			
			// Simulate the original AJAX request
			if(activityNamesOmittingLeft != null) {
				bean.getSvgDataModel().getNameOmitting().setSelection(activityNamesOmittingLeft);
				bean.getSvgDataModel().getNameOmitting().add();
			}
		} else if(activityNamesOmittingRemove != null) {
			
			// Get the selection
			String[] activityNamesOmittingRight = req.getParameterValues("activitynames_omitting_right");
			
			// Simulate the original AJAX request
			if(activityNamesOmittingRight != null) {
				bean.getSvgDataModel().getNameOmitting().setSelectionRight(activityNamesOmittingRight);
				bean.getSvgDataModel().getNameOmitting().remove();
			}
		} else if(activityNamesOmittingAddAll != null) {
			
			// Simulate the original AJAX request
			bean.getSvgDataModel().getNameOmitting().addAll();
			
		} else if(activityNamesOmittingRemoveAll != null) {
			
			// Simulate the original AJAX request
			bean.getSvgDataModel().getNameOmitting().removeAll();
			
		}
		
		/* Has the process model slider changed? */
		String pmSliderChange = req.getParameter("pm_slider_change");
		if(pmSliderChange != null) {
			String pmSlider = req.getParameter("form_svg_pm_slider");
			if(pmSlider != null) {
				bean.getSvgDataModel().getPmSlider().setSelection(pmSlider);
			}
		}
		
		/* Has the process instance slider changed? */
		String piSliderChange = req.getParameter("pi_slider_change");
		if(piSliderChange != null) {
			String piSlider = req.getParameter("form_svg_pi_slider");
			if(piSlider != null) {
				bean.getSvgDataModel().getPiSlider().setSelection(piSlider);
			}
		}
		
		/* Get viewmode information of all the viewmode-enabled tables and boxes */
		String pmTableViewMode = req.getParameter("pm_table_viewmode");
		String piTableViewMode = req.getParameter("pi_table_viewmode");
		String svgViewMode = req.getParameter("svg_viewmode");
		String svgHighlightingTypesViewMode = req.getParameter("svg_highlighting_types_viewmode");
		String svgHighlightingNamesViewMode = req.getParameter("svg_highlighting_names_viewmode");
		String svgOmittingTypesViewMode = req.getParameter("svg_omitting_types_viewmode");
		String svgOmittingNamesViewMode = req.getParameter("svg_omitting_names_viewmode");
		
		/* Toggle viewmode, if viewmode of a certain table or box was added to the request */
		if(pmTableViewMode != null) {
			bean.getPmDataModel().getViewMode().processAction();
		} else if(piTableViewMode != null) {
			bean.getPiDataModel().getViewMode().processAction();
		} else if(svgViewMode != null) {
			bean.getSvgDataModel().getViewMode().processAction();
		} else if(svgHighlightingTypesViewMode != null) {
			bean.getSvgDataModel().getTypeHighlighting().getViewMode().processAction();
		} else if(svgHighlightingNamesViewMode != null) {
			bean.getSvgDataModel().getNameHighlighting().getViewMode().processAction();
		} else if(svgOmittingTypesViewMode != null) {
			bean.getSvgDataModel().getTypeOmitting().getViewMode().processAction();
		} else if(svgOmittingNamesViewMode != null) {
			bean.getSvgDataModel().getNameOmitting().getViewMode().processAction();
		}
		
		/* Refresh the page (update the information in the bean) */
		String refresh = req.getParameter("refresh");
		if(refresh != null) {
			bean.refresh();
		}
		
		/* Is there a request for process model table page change */
		String pmFirstPage = req.getParameter("pm_first_page");
		String pmPreviousPage = req.getParameter("pm_previous_page");
		String pmPage = req.getParameter("pm_page");
		String pmNextPage = req.getParameter("pm_next_page");
		String pmLastPage = req.getParameter("pm_last_page");
		
		if(pmFirstPage != null) {
			bean.getPmDataModel().getPager().processAction("first");
		} else if(pmPreviousPage != null) {
			bean.getPmDataModel().getPager().processAction("previous");
		} else if(pmPage != null) {
			int newPage = Integer.parseInt(pmPage);
			if(newPage != bean.getPmDataModel().getPager().getCurrentPage()) {
				bean.getPmDataModel().getPager().setCurrentPage(newPage);
			}
		} else if(pmNextPage != null) {
			bean.getPmDataModel().getPager().processAction("next");
		} else if(pmLastPage != null) {
			bean.getPmDataModel().getPager().processAction("last");
		}
		
		/* Is there a request for process instance table page change */
		String piFirstPage = req.getParameter("pi_first_page");
		String piPreviousPage = req.getParameter("pi_previous_page");
		String piPage = req.getParameter("pi_page");
		String piNextPage = req.getParameter("pi_next_page");
		String piLastPage = req.getParameter("pi_last_page");
		
		if(piFirstPage != null) {
			bean.getPiDataModel().getPager().processAction("first");
		} else if(piPreviousPage != null) {
			bean.getPiDataModel().getPager().processAction("previous");
		} else if(piPage != null) {
			int newPage = Integer.parseInt(piPage);
			if(newPage != bean.getPiDataModel().getPager().getCurrentPage()) {
				bean.getPiDataModel().getPager().setCurrentPage(newPage);
			}
		} else if(piNextPage != null) {
			bean.getPiDataModel().getPager().processAction("next");
		} else if(piLastPage != null) {
			bean.getPiDataModel().getPager().processAction("last");
		}
		
		/* Check if number of items per page are changed for the process model table */
		String pmItemCount = req.getParameter("pm_item_count");
		if(pmItemCount != null && Integer.valueOf(pmItemCount) != bean.getPmDataModel().getPager().getMaximum()) {
			bean.getPmDataModel().getPager().setMaximum(Integer.valueOf(pmItemCount));
		}
		
		/* Check if number of items per page are changed for the process instance table */
		String piItemCount = req.getParameter("pi_item_count");
		if(piItemCount != null && Integer.valueOf(piItemCount) != bean.getPiDataModel().getPager().getMaximum()) {
			bean.getPiDataModel().getPager().setMaximum(Integer.valueOf(piItemCount));
		}
		
		/* Sort process instance table according to parameter */
		String piSort = req.getParameter("pi_sort");
		if(piSort != null) {
			bean.getPiDataModel().getSorter().processAction(piSort);
		}
		
		/* Sort process model table according to parameter */
		String pmSort = req.getParameter("pm_sort");
		if(pmSort != null) {
			bean.getPmDataModel().getSorter().processAction(pmSort);
		}
		
		/* Filter process instance table according to parameter */
		String piFilterProcessModel = req.getParameter("pi_filter_process_model");
		String piFilterIid = req.getParameter("pi_filter_iid");
		String piFilterStatus = req.getParameter("pi_filter_status");
		
		/* Process Model Name filter for process instance table */
		String piFilterProcessModelOld = bean.getPiDataModel().getNameFilter().getInput();
		if((piFilterProcessModelOld == null && piFilterProcessModel != null && !piFilterProcessModel.equals("")) ||
		   (piFilterProcessModelOld != null && piFilterProcessModel != null && !piFilterProcessModel.equals(piFilterProcessModelOld))) {
			bean.getPiDataModel().getNameFilter().setInput(piFilterProcessModel);
		}
		
		/* Process Instance ID filter for process instance table */
		String piFilterIidOld = bean.getPiDataModel().getIdFilter().getInput();
		if((piFilterIidOld == null && piFilterIid != null && !piFilterIid.equals("")) ||
		   (piFilterIidOld != null && piFilterIid != null && !piFilterIid.equals(piFilterIidOld))) {
			bean.getPiDataModel().getIdFilter().setInput(piFilterIid);
		}
		
		/* Process Instance Status filter for process instance table */
		Status piFilterStatusOld = bean.getPiDataModel().getStatusFilter().getInput();
		if((piFilterStatusOld == null && piFilterStatus != null && !piFilterStatus.equals("")) ||
		   (piFilterStatusOld != null && piFilterStatus != null && !piFilterStatus.equalsIgnoreCase(piFilterStatusOld.getName()))) {
			bean.getPiDataModel().getStatusFilter().setInput(ProcessInstanceStatus.convert(piFilterStatus));
		}

		
		/* Filter process model table according to parameter */
		String pmFilterPid = req.getParameter("pm_filter_pid");
		String pmFilterName = req.getParameter("pm_filter_name");
		String pmFilterStatus = req.getParameter("pm_filter_status");
		
		/* Process Model ID filter for process model table */
		String pmFilterPidOld = bean.getPmDataModel().getIdFilter().getInput();
		if((pmFilterPidOld == null && pmFilterPid != null && !pmFilterPid.equals("")) ||
		   (pmFilterPidOld != null && pmFilterPid != null && !pmFilterPid.equals(pmFilterPidOld))) {
			bean.getPmDataModel().getIdFilter().setInput(pmFilterPid);
		}
		
		/* Process Model Name filter for process model table */
		String pmFilterNameOld = bean.getPmDataModel().getNameFilter().getInput();
		if((pmFilterNameOld == null && pmFilterName != null && !pmFilterName.equals("")) ||
		   (pmFilterNameOld != null && pmFilterName != null && !pmFilterName.equals(pmFilterNameOld))) {
			bean.getPmDataModel().getNameFilter().setInput(pmFilterName);
		}

		/* Process Model Status filter for process instance table */
		Status pmFilterStatusOld = bean.getPmDataModel().getStatusFilter().getInput();
		if((pmFilterStatusOld == null && pmFilterStatus != null && !pmFilterStatus.equals("")) ||
		   (pmFilterStatusOld != null && pmFilterStatus != null && !pmFilterStatus.equalsIgnoreCase(pmFilterStatusOld.getName()))) {
			bean.getPmDataModel().getStatusFilter().setInput(ProcessModelStatus.convert(pmFilterStatus));
		}
		
		
		/* Reset filter of process instance table according to parameter */
		String piFilterReset = req.getParameter("pi_filter_reset");
		if(piFilterReset != null) {
			if(piFilterReset.equals("processModelName")) {
				bean.getPiDataModel().getNameFilter().processAction();
			} else if(piFilterReset.equals("iid")) {
				bean.getPiDataModel().getIdFilter().processAction();
			} else if(piFilterReset.equals("status")) {
				bean.getPiDataModel().getStatusFilter().processAction();
			}
		}

		/* Reset filter of process model table according to parameter */
		String pmFilterReset = req.getParameter("pm_filter_reset");
		if(pmFilterReset != null) {
			if(pmFilterReset.equals("name")) {
				bean.getPmDataModel().getNameFilter().processAction();
			} else if(pmFilterReset.equals("pid")) {
				bean.getPmDataModel().getIdFilter().processAction();
			} else if(pmFilterReset.equals("status")) {
				bean.getPmDataModel().getStatusFilter().processAction();
			}
		}

        String redirectUrl = req.getParameter("redirect_url");

		/* Redirect to jsp */
		String urlWithSessionID = resp.encodeRedirectURL(redirectUrl);
		//resp.sendRedirect(urlWithSessionID);

//		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/carbon/bpi/index.jsp");
//		dispatcher.forward(req, resp);
	}
	
	/** {@inheritDoc} */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {
		doGet(req, resp);
	}
	

}
