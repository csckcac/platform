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
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.service.mapping.ProcessInstanceStatusMapping;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.wso2.adapter.ProcessInstanceServiceImpl;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.wso2.service.InstanceNotFoundException;

import java.util.List;

/**
 * This interface provides methods to access the {@link ProcessInstance}s stored at the workflow engine.
 * 
 * @author Gregor Latuske
 */
public interface ProcessInstanceService<M>
	extends Service, ProcessInstanceStatusMapping<M> {

    /**
     * Returns the ProcessInstance for the given instance ID.
     *
     * @param instanceId
     * @return
     * @throws org.wso2.carbon.bpel.ui.bpel2svg.latest.wso2.service.InstanceNotFoundException
     *
     */
    public ProcessInstance getProcessInstance(String instanceId) throws InstanceNotFoundException;

	/**
	 * Returns all {@link ProcessInstance}s stored in the workflow engine to the given {@link ProcessModel}s.
	 *
	 * @param processModels The {@link ProcessModel}s whose {@link ProcessInstance}s should be retrieved.
	 * @return All {@link ProcessInstance}s stored in the workflow engine to the given {@link ProcessModel}s.
	 * @throws BPIException
	 */
	public List<ProcessInstance> getProcessInstances(List<ProcessModel> processModels) throws BPIException;

	/**
	 * Returns all {@link ProcessInstance}s stored in the workflow engine to the given {@link ProcessModel}.
	 * 
	 * @param processModel The {@link ProcessModel} whose {@link ProcessInstance}s should be retrieved.
	 * @return All {@link ProcessInstance}s stored in the workflow engine to the given {@link ProcessModel}.
	 * @throws BPIException
	 */
	public List<ProcessInstance> getProcessInstances(ProcessModel processModel) throws BPIException;

	/**
	 * Factory class to create a {@link ProcessInstanceService}.
	 * 
	 * @author Gregor Latuske
	 */
	class ProcessInstanceServiceFactory
		implements ServiceFactory<ProcessInstanceService<?>> {

		/** {@inheritDoc} */
		@Override
		public ProcessInstanceService<?> createService() throws BPIException {
			try {
                return new ProcessInstanceServiceImpl();
			} catch (Throwable t) {
				throw new BPIException(t);
			}
		}
	}

}
