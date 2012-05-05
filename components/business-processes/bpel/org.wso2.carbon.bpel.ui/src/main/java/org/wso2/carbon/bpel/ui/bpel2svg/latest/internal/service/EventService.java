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
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.activity.ActivityExecEvent;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.service.mapping.ActivityExecStatusMapping;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.wso2.adapter.EventServiceImpl;

import java.util.List;

/**
 * This interface provides methods to access the events stored at the workflow engine.
 */
public interface EventService<M> extends Service, ActivityExecStatusMapping<M> {

    /**
     * Returns all {@link ActivityExecEvent}s stored in the workflow engine to the given
     * {@link ProcessInstance}.
     *
     * @param processInstance The {@link ProcessInstance}, whose {@link ActivityExecEvent}s should be
     *                        retrieved.
     * @return All {@link ActivityExecEvent}s stored in the workflow engine to the given
     *         {@link ProcessInstance}.
     * @throws BPIException If an error occurred while fetching activity execution events
     */
    List<ActivityExecEvent> getActivityExecEvents(ProcessInstance processInstance)
            throws BPIException;

    /**
     * Factory class to create an {@link EventService}.
     */
    class EventServiceFactory implements ServiceFactory<EventService<?>> {

        /** Property name of the {@link EventService}. */
//		private static final String SERVICE_PROPERTY = "dispatcher.service.event";

        /**
         * {@inheritDoc}
         */
        @Override
        public EventService<?> createService() throws BPIException {
            try {
                return new EventServiceImpl();
            } catch (Exception t) {
                throw new BPIException(t);
            }
        }
    }

}
