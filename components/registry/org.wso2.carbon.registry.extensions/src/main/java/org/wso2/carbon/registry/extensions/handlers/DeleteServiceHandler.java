/*
 * Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/


package org.wso2.carbon.registry.extensions.handlers;

import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

/**
 * This handler implementation customizes the delete process of a service with versioning in the repository.
 * 
 */
public class DeleteServiceHandler extends Handler {

    @Override
    public void delete(RequestContext requestContext) throws RegistryException {

        Registry registry = requestContext.getRegistry();

        String patchVersionPath = requestContext.getResource().getParentPath();
        String minorVersionPath = RegistryUtils.getParentPath(patchVersionPath);
        String majorVersionPath =  RegistryUtils.getParentPath(minorVersionPath);


        Resource minorVersionNode = registry.get(minorVersionPath);

        if(minorVersionNode instanceof Collection) {

            Collection minorVersionNodeCol = (Collection) minorVersionNode;
            if (minorVersionNodeCol.getChildren().length > 2) {

                registry.delete(patchVersionPath);
            } else {

                Resource majorVersionPathNode = registry.get(majorVersionPath);
                if (majorVersionPathNode instanceof Collection) {

                    Collection majorVersionPathNodeCol = (Collection) majorVersionPathNode;
                    if (majorVersionPathNodeCol.getChildren().length > 2) {

                        registry.delete(minorVersionPath);
                    } else if (majorVersionPathNodeCol.getChildren().length == 2) {

                        registry.delete(majorVersionPath);
                        if(((Collection) registry.get(RegistryUtils.getParentPath(majorVersionPath))).getChildren().length == 0) {
                            registry.delete(RegistryUtils.getParentPath(majorVersionPath));
                        }
                    }
                } else {
                     throw new RegistryException("Unable to delete. Collection is expected for major version.");
                }
            }
        } else {
            throw new RegistryException("Unable to delete. Collection is expected for minor version.");
        }

        requestContext.setProcessingComplete(true);
    }
}