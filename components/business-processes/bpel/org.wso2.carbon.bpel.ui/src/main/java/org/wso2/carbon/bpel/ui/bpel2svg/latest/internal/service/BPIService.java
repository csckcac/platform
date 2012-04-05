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
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.SVG;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.svg.settings.Settings;

import java.util.List;

/**
 * This interface provides methods to access the data from the workflow engine. It wraps the methods of the
 * services or aggregates the data returned from the services.
 * 
 * @author Gregor Latuske
 */
public interface BPIService
	extends Service {

    /**
     * Returns the ProcessModel for a given instance ID
     *
     * @param instanceID
     * @return
     * @throws BPIException
     */
    public ProcessModel getProcessModelFromInstance(String instanceID) throws BPIException;

    /**
     * Returns the ProcessModel for a given id
     *
     *
     * @param instanceID
     * @return ProcessModel
     * @throws BPIException
     */
    public String getProcessModelIDFromInstance(String instanceID) throws BPIException;

	/**
	 * Returns all {@link ProcessModel}s stored in the workflow engine and their {@link ProcessInstance}s.
	 * <p>
	 * The list is sorted by the name of the {@link ProcessInstance} in descending order.
	 * <p>
	 * This method also parses the BPEL files and caches the output.
	 * 
	 * @return All {@link ProcessModel}s stored in the workflow engine and their {@link ProcessInstance}s.
	 * @throws BPIException
	 */
	public List<ProcessModel> getProcessModels() throws BPIException;

	/**
	 * Returns the {@link ProcessModel} with the given ID and its {@link ProcessInstance}s or
	 * <code>null</code>. *
	 * <p>
	 * This method also parses the BPEL file and caches  the output.
	 * 
	 * @param pid The ID of the {@link ProcessModel}.
	 * @return The {@link ProcessModel} with the given ID its {@link ProcessInstance}s <code>null</code>.
	 * @throws BPIException
	 */
	public ProcessModel getProcessModel(String pid) throws BPIException;

	/**
	 * Returns the generated {@link SVG} of the {@link ProcessModel}.
	 * 
	 * @param processModel The {@link ProcessModel}, whose graph should be generated.
	 * @param settings The settings associated with the current session.
	 * @return The generated {@link SVG} of the {@link ProcessModel}.
	 * @throws BPIException
	 */
	public SVG getSVG(ProcessModel processModel, Settings settings) throws BPIException;

	/**
	 * Returns the generated {@link SVG} of the {@link ProcessModel} with the execution data of the
	 * {@link ProcessInstance}.
	 * <p>
	 * 1) Retrieve the execution data from the workflow engine.
	 * <p>
	 * 2) Generate the SVG
	 * 
	 * @param processInstance The {@link ProcessInstance} with the associated {@link ProcessModel},whose graph
	 *        should be generated.
	 * @param settings The settings associated with the current session.
	 * @return The generated {@link SVG} of the {@link ProcessModel} with the execution data of the
	 *         {@link ProcessInstance}.
	 * @throws BPIException
	 */
	public SVG getSVG(ProcessInstance processInstance, Settings settings) throws BPIException;

	/**
	 * Factory class to create a {@link BPIService}.
	 * 
	 * @author Gregor Latuske
	 */
	public class BPIServiceFactory
		implements ServiceFactory<BPIService> {

		/** {@inheritDoc} */
		@Override
		public BPIService createService() throws BPIException {
			return new BPIServiceImpl();
		}
	}
}
