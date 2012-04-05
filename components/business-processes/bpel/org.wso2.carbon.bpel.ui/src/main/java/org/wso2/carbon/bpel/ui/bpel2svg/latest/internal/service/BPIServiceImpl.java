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
package org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.service;

import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.BPIException;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.ProcessInstance;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.ProcessModel;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.activity.ActivityExecData;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.activity.ActivityExecEvent;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.status.ActivityExecStatus;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.service.EventService.EventServiceFactory;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.service.ProcessInstanceService.ProcessInstanceServiceFactory;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.service.ProcessModelService.ProcessModelServiceFactory;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.service.bpel.BPELParser;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.SVG;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.SVGGenerator;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Settings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The implementation of the {@link BPIService}.
 * 
 * @author Gregor Latuske
 */
class BPIServiceImpl
	implements BPIService {

    /** {@inheritDoc} */
    public String getProcessModelIDFromInstance(String instanceID) throws BPIException {
        ProcessInstanceService<?> piService = new ProcessInstanceServiceFactory().createService();

        ProcessModel process = piService.getProcessInstance(instanceID).getProcessModel();

        return process.getPid();
    }

    public ProcessModel getProcessModelFromInstance(String instanceID) throws BPIException {
        try {
            String processModelID = getProcessModelIDFromInstance(instanceID);
            ProcessModel model = getProcessModel(processModelID);
            return model;
        } catch (BPIException e) {
            throw e;
        }
    }

	/** {@inheritDoc} */
	public List<ProcessModel> getProcessModels() throws BPIException {
		try {
			ProcessModelService<?> pmService = new ProcessModelServiceFactory().createService();
			ProcessInstanceService<?> piService = new ProcessInstanceServiceFactory().createService();

			List<ProcessModel> processModels = pmService.getProcessModels();
			List<ProcessInstance> processInstances = piService.getProcessInstances(processModels);
			
			for (ProcessModel processModel : processModels) {
				for (ProcessInstance processInstance : processInstances) {
					// Add process instance to adequate process model
					if (processModel.equals(processInstance.getProcessModel())) {
						processModel.getProcessInstances().add(processInstance);
					}
				}
				
				// Parse process model
				processModel.setActivityRoot(BPELParser.parse(processModel));
			}

			return processModels;
		} catch (Throwable throwable) {
			throw handelThrowable(throwable);
		}
	}

	/** {@inheritDoc} */
	public ProcessModel getProcessModel(String pid) throws BPIException {
		try {
			ProcessModelService<?> pmService = new ProcessModelServiceFactory().createService();
			ProcessInstanceService<?> piService = new ProcessInstanceServiceFactory().createService();

			ProcessModel processModel = pmService.getProcessModel(pid);

			// Load process instances
			processModel.setProcessInstances(piService.getProcessInstances(processModel));

			// Parse process model
			processModel.setActivityRoot(BPELParser.parse(processModel));

			return processModel;
		} catch (Throwable throwable) {
			throw handelThrowable(throwable);
		}
	}

	/** {@inheritDoc} */
	@Override
	public SVG getSVG(ProcessModel processModel, Settings settings) throws BPIException {
		return getSVG(processModel, null, settings);
	}

	/** {@inheritDoc} */
	@Override
	public SVG getSVG(ProcessInstance processInstance, Settings settings) throws BPIException {
		return getSVG(processInstance.getProcessModel(), processInstance, settings);
	}

	/**
	 * Provides the logic of the methods getSVG(ProcessModel) and getSVG(ProcessInstance).
	 * 
	 * @param processModel The {@link ProcessModel}, whose graph should be generated.
	 * @param processInstance The {@link ProcessInstance} with the associated {@link ProcessModel},whose graph
	 *        should be generated.
	 * @param settings The settings associated with the current session.
	 * @throws BPIException
	 */
	private SVG getSVG(ProcessModel processModel, ProcessInstance processInstance, Settings settings)
		throws BPIException {
		try {
			// Retrieve the execution data, if a process instance is given
			if (processInstance != null) {
				settings.setProcessInstance(processInstance);
				settings.setActivityExecData(getActivityExecData(processInstance));
			}

			// Finally generate the SVG
			return SVGGenerator.generate(processModel.getActivityRoot(), settings);
		} catch (Throwable throwable) {
			throw handelThrowable(throwable);
		}
	}

	/**
	 * Retrieves a map of {@link ActivityExecData} and the according activity name.
	 * <p>
	 * 1) The {@link ActivityExecEvent}s will be fetched from the workflow engine.
	 * <p>
	 * 2) The collected {@link ActivityExecEvent} will be aggregated to a {@link ActivityExecData}.
	 * 
	 * @param processInstance The {@link processInstance}, that {@link ActivityExecData} should be retrieved.
	 * @return A map of {@link ActivityExecData} and the according activity name.
	 * @throws BPIException
	 */
	private Map<String, ActivityExecData> getActivityExecData(ProcessInstance processInstance)
		throws BPIException {

		EventService<?> service = new EventServiceFactory().createService();
		Map<String, ActivityExecData> map = new HashMap<String, ActivityExecData>();

		// Load events and iterate over them
		List<ActivityExecEvent> events = service.getActivityExecEvents(processInstance);
		for (ActivityExecEvent event : events) {

			// Check whether execution data to the activity name was already saved
			ActivityExecData execData = map.get(event.getName());
			if (execData == null) {

				// Create new execution data and put it into the map
				execData = new ActivityExecData(event.getActivityId(), event.getStatus(), processInstance);
				execData.setStartDate(event.getDate());

				map.put(event.getName(), execData);
			} else {

				// Check whether the event is in status order before or after the saved data
				int compareTo = event.getStatus().compareTo(execData.getStatus());
				if (compareTo < 0 && event.getDate().before(execData.getStartDate())) {
					execData.setStartDate(event.getDate());
				} else if (compareTo > 0) {
					execData.setStatus(event.getStatus());

					// Only target status set end date
					if (event.getStatus().equals(ActivityExecStatus.FAILURE)
						|| event.getStatus().equals(ActivityExecStatus.COMPLETED)) {
						execData.setEndDate(event.getDate());
					} else if (event.getStatus().equals(ActivityExecStatus.RECOVERY)) {
						execData.setEndDate(null);
					}
				}
			}
		}

		return map;
	}

	/**
	 * Wraps a {@link Throwable} to a {@link BPIException}.
	 * 
	 * @param throwable The {@link Throwable} to warp.
	 * @return The wrapped {@link Throwable} as a {@link BPIException}.
	 */
	private BPIException handelThrowable(Throwable throwable) {
		if (throwable instanceof BPIException) {
			return (BPIException) throwable;
		}
		return new BPIException(throwable);
	}

}
