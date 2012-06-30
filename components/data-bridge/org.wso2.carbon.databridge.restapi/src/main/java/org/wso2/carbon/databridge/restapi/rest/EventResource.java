package org.wso2.carbon.databridge.restapi.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.*;
import org.wso2.carbon.databridge.commons.utils.EventConverterUtils;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.databridge.core.EventConverter;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionNotFoundException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.databridge.core.StreamTypeHolder;
import org.wso2.carbon.databridge.restapi.internal.Utils;
import org.wso2.carbon.databridge.restapi.utils.RESTUtils;

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
    @Path("/{stream}/{version}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response publishEvent(
            @PathParam("stream") String streamName,
            @PathParam("version") String version, String requestBody,
            @Context HttpServletRequest request) {

        try {
            final String streamId =
                    Utils.getDataBridgeReceiver().findStreamId(RESTUtils.getSessionId(request), streamName
                            , version);
            Utils.getDataBridgeReceiver().publish(requestBody, RESTUtils.getSessionId(request),
                    new EventConverter() {
                        @Override
                        public List<Event> toEventList(Object jsonEvents,
                                                       StreamTypeHolder streamTypeHolder) {
                            return EventConverterUtils.convertFromJson((String) jsonEvents, streamId );
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
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveStreamDefn(String requestBody, @Context HttpServletRequest request) {
        try {
            StreamDefinition streamDefinition = EventDefinitionConverterUtils.convertFromJson(requestBody);
            Utils.getDataBridgeReceiver().saveStreamDefinition(RESTUtils.getSessionId(request),
                    streamDefinition);
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
    @Path("/{stream}/{version}")
    public Response getStreamDefinition(
                @PathParam("stream") String streamName,
                @PathParam("version") String version,
                @Context HttpServletRequest request) {

            try {
                StreamDefinition streamDefinition = Utils.getDataBridgeReceiver()
                        .getStreamDefinition(RESTUtils.getSessionId(request), streamName, version);
                String json = EventDefinitionConverterUtils.convertToJson(streamDefinition);
                return Response.ok(json, MediaType.APPLICATION_JSON).build();

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
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllStreamDefinitions(
                @Context HttpServletRequest request) {

            try {
                List<StreamDefinition> allStreamDefinitions =
                        Utils.getDataBridgeReceiver().getAllStreamDefinitions(RESTUtils.getSessionId(request));
                String json = EventDefinitionConverterUtils.convertToJson(allStreamDefinitions);
                return Response.ok(json, MediaType.APPLICATION_JSON).build();

            } catch (SessionTimeoutException e) {
                throw new WebApplicationException(e);
            }
    }
}
