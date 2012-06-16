package org.wso2.carbon.eventbridge.restapi.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.eventbridge.commons.Event;
import org.wso2.carbon.eventbridge.commons.EventStreamDefinition;
import org.wso2.carbon.eventbridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.eventbridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.eventbridge.commons.utils.EventConverterUtils;
import org.wso2.carbon.eventbridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.eventbridge.core.exception.StreamDefinitionNotFoundException;
import org.wso2.carbon.eventbridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.eventbridge.restapi.internal.Utils;
import org.wso2.carbon.eventbridge.restapi.jaxb.NextVersion;
import org.wso2.carbon.eventbridge.restapi.utils.RESTUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
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
            @PathParam("eventStream") String eventStream,
            @PathParam("version") String version, String request, @HeaderParam("authorize") String authHeader) {

        try {
            String streamId = Utils.getEventBridge().getStreamId(RESTUtils.extractAuthHeaders(authHeader),
                    eventStream,
                    version);
            List<Event> eventList = EventConverterUtils.convertFromJson(request, streamId);
//            Utils.getEventBridge().publish(RESTUtils.extractAuthHeaders(authHeader), eventList);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (StreamDefinitionNotFoundException e) {
            throw new WebApplicationException(e);
        } catch (StreamDefinitionStoreException e) {
            throw new WebApplicationException(e);
        }

    }

    @POST
    @Path("/{eventStream}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public NextVersion defineEvent(@PathParam("eventStream") String eventStream,@HeaderParam("authorize") String authHeader,
                            String request, @Context UriInfo uriInfo) {
        try {
            EventStreamDefinition eventStreamDefinition = EventDefinitionConverterUtils.convertFromJson(request);
            Utils.getEventBridge().saveStreamDefinition(RESTUtils.extractAuthHeaders(authHeader), eventStreamDefinition);
            return new NextVersion(uriInfo.getPath() + eventStreamDefinition.getVersion());
        } catch (MalformedStreamDefinitionException e) {
            throw new WebApplicationException(e);
        } catch (DifferentStreamDefinitionAlreadyDefinedException e) {
            throw new WebApplicationException(e);
        } catch (StreamDefinitionStoreException e) {
            throw new WebApplicationException(e);
        }

    }

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_PLAIN)
    public String getSampleText() {
        return "BAM REST API is now live! \n";
    }


}
