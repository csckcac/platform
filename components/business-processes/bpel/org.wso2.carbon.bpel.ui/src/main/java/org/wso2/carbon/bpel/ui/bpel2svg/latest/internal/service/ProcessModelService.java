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
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.ProcessModel;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.service.mapping.ProcessModelStatusMapping;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.wso2.adapter.ProcessModelServiceImpl;

import java.util.List;

/**
 * This interface provides methods to access the {@link ProcessModel}s stored at the workflow engine.
 *
 * @author Gregor Latuske
 */
public interface ProcessModelService<M> extends Service, ProcessModelStatusMapping<M> {

    /**
     * Returns all {@link ProcessModel}s stored in the workflow engine.
     *
     * @return All {@link ProcessModel}s stored in the workflow engine.
     * @throws BPIException If an error occurred while fetching the process models
     */
    List<ProcessModel> getProcessModels() throws BPIException;

    /**
     * Returns the {@link ProcessModel} with the given ID or <code>null</code>.
     *
     * @param pid The ID of the {@link ProcessModel}.
     * @return The {@link ProcessModel} with the given ID <code>null</code>.
     * @throws BPIException If an error occurred while fetching the process model
     */
    ProcessModel getProcessModel(String pid) throws BPIException;

    /**
     * Factory class to create a {@link ProcessModelService}.
     *
     * @author Gregor Latuske
     */
    class ProcessModelServiceFactory implements ServiceFactory<ProcessModelService<?>> {

        /**
         * {@inheritDoc}
         */
        @Override
        public ProcessModelService<?> createService() throws BPIException {
            try {
                return new ProcessModelServiceImpl();
            } catch (Exception t) {
                throw new BPIException(t);
            }
        }
    }

}
