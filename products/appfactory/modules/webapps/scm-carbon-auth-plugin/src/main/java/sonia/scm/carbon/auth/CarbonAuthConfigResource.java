/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package sonia.scm.carbon.auth;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;

/**
 *
 */
@Singleton
@Path("config/auth/carbon")
public class CarbonAuthConfigResource {

    /**
     * Constructs ...
     *
     * @param authenticationHandler
     */
    @Inject
    public CarbonAuthConfigResource(CarbonAuthHandler authenticationHandler) {
        this.authenticationHandler = authenticationHandler;
    }

    //~--- get methods ----------------------------------------------------------

    /**
     * Method description
     *
     * @return
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public CarbonAuthConfig getConfig() {
        return authenticationHandler.getConfig();
    }

    //~--- set methods ----------------------------------------------------------

    /**
     * Method description
     *
     * @param uriInfo
     * @param config
     * @return
     * @throws java.io.IOException
     */
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response setConfig(@Context UriInfo uriInfo, CarbonAuthConfig config)
            throws IOException {
        authenticationHandler.setConfig(config);
        authenticationHandler.storeConfig();


        return Response.created(uriInfo.getRequestUri()).build();
    }

    //~--- fields ---------------------------------------------------------------

    /**
     * Field description
     */
    private CarbonAuthHandler authenticationHandler;
}

