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
package org.wso2.carbon.bpel.ui.bpel2svg.latest.wso2.adapter;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.stub.mgt.InstanceManagementServiceStub;
import org.wso2.carbon.bpel.stub.mgt.types.*;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.BPIException;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.ProcessInstance;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.activity.ActivityExecEvent;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.status.ActivityExecStatus;
import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.service.EventService;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the {@link EventService}.
 */
public class EventServiceImpl implements EventService<ActivityStatusType> {

    private static Log log = LogFactory.getLog(EventServiceImpl.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public ActivityExecStatus mapToStatus(ActivityStatusType value) {
        /* compare the value to all possible status values */
        if (value.equals(ActivityStatusType.ENABLED)) {
            return ActivityExecStatus.ENABLED;
        } else if (value.equals(ActivityStatusType.STARTED)) {
            return ActivityExecStatus.STARTED;
        } else if (value.equals(ActivityStatusType.COMPLETED)) {
            return ActivityExecStatus.COMPLETED;
        } else if (value.equals(ActivityStatusType.FAILURE)) {
            return ActivityExecStatus.FAILURE;
            //TODO: What about dead?
        } else if (value.equals(ActivityStatusType.DEAD)) {
            return ActivityExecStatus.FAILURE;

//		} else if (value.equals(ActivityStatusType.)) {
//			return ActivityExecStatus.RECOVERY;
//		} else if (value.equals(ActivityStatusType.)) {
//			return ActivityExecStatus.SKIPPED;
        }

        /* status could not be identified so throw an exception */
        throw new IllegalArgumentException(value.getClass().toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ActivityExecEvent> getActivityExecEvents(
            ProcessInstance processInstance) throws BPIException {

        ArrayList<ActivityExecEvent> activityEvents = new ArrayList<ActivityExecEvent>();

        try {
            /*
            * Get all the information of the instance by calling the WebService. Get a list of
            * activities and events of that instance.
            */
            InstanceInfoWithEventsType instance =
                    getStub().getInstanceInfoWithEvents(Long.valueOf(processInstance.getIid()));

            ScopeInfoWithEventsType rootScope = instance.getRootScope();

            //This will capture all the available event details generated inside root scope.
            activityEvents.addAll(getEventListForScopeIncludingDescendants(processInstance,
                    rootScope));

        } catch (org.wso2.carbon.bpel.stub.mgt.InstanceManagementException e) {
            log.error("An error occurred with the operation \"getInstanceInfoWithEvents\"", e);
        } catch (RemoteException e) {
            log.error("Error occurred in stub.", e);
        }

        return activityEvents;
    }

    /**
     * This will capture all the available event details generated inside the given scope and its descendants
     *
     * @param processInstance is used as a reference in each ActivityExecEvent returned by this method.
     * @param scope           Scope with event info
     * @return a list of events generated for the given scope
     */
    private List<ActivityExecEvent> getEventListForScopeIncludingDescendants(
            ProcessInstance processInstance, ScopeInfoWithEventsType scope) {
        //Initializing the ActivityExecEvent list which will be filled with all the events generated
        // from the given scope and its descendants
        ArrayList<ActivityExecEvent> activityEvents = new ArrayList<ActivityExecEvent>();

        //The activities included in the given scope
        ActivityInfoWithEventsType[] activities =
                scope.getActivitiesWithEvents().getActivityInfoWithEvents();

        if (activities != null) {
            //Fill event info from activities
            for (ActivityInfoWithEventsType activity : activities) {
                EventInfo[] eventInfos = activity.getActivityEventsList().getEventInfo();
                for (EventInfo eventInfo : eventInfos) {

                    ActivityStatusType status = null;

                    // There's probably a better way to do this
                    if (eventInfo.getName().toLowerCase().contains("activityenabledevent")) {
                        status = ActivityStatusType.ENABLED;
                    } else if (eventInfo.getName().toLowerCase().contains("activityexecstartevent")) {
                        status = ActivityStatusType.STARTED;
                    } else if (eventInfo.getName().toLowerCase().contains("activityexecendevent")) {
                        status = ActivityStatusType.COMPLETED;
                    } else if (eventInfo.getName().toLowerCase().contains("activityfailureevent")) {
                        status = ActivityStatusType.FAILURE;
                    }

                    /*
                    * Create an ActivityExecEvent and add it to the list of activities if the event
                    * was an ActivityEvent (status != null)
                    */
                    if (status != null) {
                        ActivityExecEvent instanceEvent = new ActivityExecEvent(
                                activity.getActivityInfo().getName(), activity.getActivityInfo().getAiid(),
                                mapToStatus(status),
                                eventInfo.getTimestamp(),
                                processInstance);
                        activityEvents.add(instanceEvent);
                    }
                }
            }
        }

        //The childScopes for the given scope
        ScopeInfoWithEventsType[] childScopes = scope.getChildrenWithEvents().getChildWithEventsRef();

        //Fill event info from child-scopes recursively
        if (childScopes != null) { //if childScopes is null, i.e. there're no childScopes for the given scope.
            for (ScopeInfoWithEventsType childScope : childScopes) {
                activityEvents.addAll(getEventListForScopeIncludingDescendants(processInstance,
                        childScope));
            }
        }

        return activityEvents;
    }

    /**
     * Creates an {@link InstanceManagementServiceStub} of the process instances management WebService.
     *
     * @return The {@link InstanceManagementServiceStub}
     * @throws RemoteException If stub operation invocation fail
     */
    private InstanceManagementServiceStub getStub() throws RemoteException {
        String instanceMgtService = "InstanceManagementService";
        String serviceURL = AuthenticationManager.getBackendServerURL() + instanceMgtService;

        InstanceManagementServiceStub stub = new InstanceManagementServiceStub(null, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                AuthenticationManager.getCookie());

        return stub;
    }


}
