package org.wso2.carbon.eventbridge.restapi.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.eventbridge.commons.Event;
import org.wso2.carbon.eventbridge.commons.EventStreamDefinition;
import org.wso2.carbon.eventbridge.commons.exception.*;
import org.wso2.carbon.eventbridge.commons.utils.EventConverterUtils;
import org.wso2.carbon.eventbridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.eventbridge.core.EventConverter;
import org.wso2.carbon.eventbridge.core.exception.StreamDefinitionNotFoundException;
import org.wso2.carbon.eventbridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.eventbridge.core.EventStreamTypeHolder;
import org.wso2.carbon.eventbridge.restapi.internal.Utils;
import org.wso2.carbon.eventbridge.restapi.utils.RESTUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@Path("/")
public class EventResource {

    private static Log log = LogFactory.getLog(EventResource.class);

    /*
    JSON string expected:
    [
      {
       "event" : [val1, val2 ....] ,
       "meta" : [val1, val2 ....] ,
       "correlation" : [val1, val2 ....]
      }
     ,
      {
       "event" : [val1, val2 ....] ,
       "meta" : [val1, val2 ....] ,
       "correlation" : [val1, val2 ....]
      }
     , ....
    ]
     */
    @POST
    @Path("/{eventStream}/{version}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response publishEvent(
            @PathParam("eventStream") String eventStreamName,
            @PathParam("version") String version, String requestBody,
            @Context HttpServletRequest request) {

        try {
            final String eventStreamId =
                    Utils.getEventBridgeReceiver().findEventStreamId(RESTUtils.getSessionId(request), eventStreamName
                            , version);
            Utils.getEventBridgeReceiver().publish(requestBody, RESTUtils.getSessionId(request),
                    new EventConverter() {
                        @Override
                        public List<Event> toEventList(Object jsonEvents,
                                                       EventStreamTypeHolder eventStreamTypeHolder) {
                            return EventConverterUtils.convertFromJson((String) jsonEvents, eventStreamId );
                        }
                    });
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (UndefinedEventTypeException e) {
            throw new WebApplicationException(e);
        } catch (SessionTimeoutException e) {
            throw new WebApplicationException(e);
        } catch (NoStreamDefinitionExistException e) {
            throw new WebApplicationException(e);
        }


    }

    @POST
    @Path("/{eventStream}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveEventStreamDefn(@PathParam("eventStream") String eventStream,
                                   String requestBody, @Context HttpServletRequest request) {
        try {
            EventStreamDefinition eventStreamDefinition = EventDefinitionConverterUtils.convertFromJson(requestBody);
            Utils.getEventBridgeReceiver().saveEventStreamDefinition(RESTUtils.getSessionId(request),
                    eventStreamDefinition);
            return Response.status(Response.Status.ACCEPTED).build();

        } catch (MalformedStreamDefinitionException e) {
            throw new WebApplicationException(e);
        } catch (DifferentStreamDefinitionAlreadyDefinedException e) {
            throw new WebApplicationException(e);
        } catch (StreamDefinitionStoreException e) {
            throw new WebApplicationException(e);
        } catch (SessionTimeoutException e) {
            throw new WebApplicationException(e);
        }

    }

    @GET
    @Path("/{eventStream}/{version}")
    public Response getStreamDefinition(
                @PathParam("eventStream") String eventStreamName,
                @PathParam("version") String version,
                @Context HttpServletRequest request) {

            try {
                Utils.getEventBridgeReceiver().getEventStreamDefinition(RESTUtils.getSessionId(request), eventStreamName, version);
                return Response.status(Response.Status.ACCEPTED).build();

            } catch (SessionTimeoutException e) {
                throw new WebApplicationException(e);
            } catch (StreamDefinitionStoreException e) {
                throw new WebApplicationException(e);
            } catch (StreamDefinitionNotFoundException e) {
                throw new WebApplicationException(e);
            }


    }

    @GET
    @Path("/")
    public String getAllStreamDefinitions(
                @Context HttpServletRequest request) {

            try {
                List<EventStreamDefinition> allEventStreamDefinitions =
                        Utils.getEventBridgeReceiver().getAllEventStreamDefinitions(RESTUtils.getSessionId(request));
                return EventDefinitionConverterUtils.convertToJson(allEventStreamDefinitions);

            } catch (SessionTimeoutException e) {
                throw new WebApplicationException(e);
            }


    }






}
