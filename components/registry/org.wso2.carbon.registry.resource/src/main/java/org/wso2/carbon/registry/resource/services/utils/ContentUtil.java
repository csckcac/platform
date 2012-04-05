/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.resource.services.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.common.CommonConstants;
import org.wso2.carbon.registry.common.ResourceData;
import org.wso2.carbon.registry.common.utils.UserUtil;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourcePath;
import org.wso2.carbon.registry.core.config.RemoteConfiguration;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.secure.AuthorizationFailedException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.beans.CollectionContentBean;
import org.wso2.carbon.registry.resource.beans.ContentBean;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

public class ContentUtil {

    private static final Log log = LogFactory.getLog(ContentUtil.class);

    public static CollectionContentBean getCollectionContent(String path,
                                       UserRegistry registry) throws Exception {

        try {
            Resource resource = registry.get(path);
            if (!(resource instanceof Collection)) {
                String msg = "Attempted to get collection content from " +
                        "a non-collection resource " + path;
                log.error(msg);
                throw new RegistryException(msg);
            }

            Collection collection = (Collection) resource;
            String[] childPaths = collection.getChildren();
            CollectionContentBean bean = new CollectionContentBean();
            bean.setChildPaths(childPaths);
            bean.setChildCount(childPaths.length);
            bean.setCollectionTypes(getCollectionTypes());
            if (registry.getRegistryContext() != null) {
                List remoteInstances =  registry.getRegistryContext().
                        getRemoteInstances();
                String[] instances = new String[remoteInstances.size()];
                for(int i=0; i<instances.length; i++) {
                    instances[i] = ((RemoteConfiguration)remoteInstances.get(i)).getId();
                }
                bean.setRemoteInstances(instances);
            }
            ResourcePath resourcePath = new ResourcePath(path);
            bean.setPathWithVersion(resourcePath.getPathWithVersion());
            bean.setVersionView(!resourcePath.isCurrentVersion());

            return bean;

        } catch (Exception e) {
            String msg = "Failed to get content details of the resource " + path +
                    ". Caused by: " + ((e.getCause() instanceof SQLException) ?
                    "" : e.getMessage());
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }

    public static ResourceData[] getResourceData(String[] childPaths,
                                                 UserRegistry registry) throws Exception {

        List <ResourceData> resourceDataList = new ArrayList <ResourceData> ();

        for (String childPath : childPaths) {

            try {
                if (childPath == null || childPath.length() == 0) {
                    continue;
                }
                Resource child = registry.get(childPath);

                ResourceData resourceData = new ResourceData();
                resourceData.setResourcePath(childPath); // + RegistryConstants.VIEW_ACTION);

                String[] parts = childPath.split(RegistryConstants.PATH_SEPARATOR);
                if (parts.length > 0) {
                    resourceData.setName(parts[parts.length - 1]);
                }

                resourceData.setResourceType(child instanceof Collection ?
                        CommonConstants.COLLECTION : CommonConstants.RESOURCE);
                resourceData.setAuthorUserName(child.getAuthorUserName());
                resourceData.setDescription(child.getDescription());
                resourceData.setAverageRating(registry.getAverageRating(child.getPath()));
                Calendar createDateTime = Calendar.getInstance();
                createDateTime.setTime(child.getCreatedTime());
                resourceData.setCreatedOn(createDateTime);
                List mountPoints = child.getPropertyValues("registry.mountpoint");
                List targetPoints = child.getPropertyValues("registry.targetpoint");
//                List paths = child.getPropertyValues("registry.path");
                List actualPaths = child.getPropertyValues("registry.actualpath");
                String user = child.getProperty("registry.user");
                if (child.getProperty("registry.mount") != null) {
                    resourceData.setMounted(true);
                }
                if (child.getProperty("registry.link") != null) {
                    resourceData.setLink(true);

                    if(mountPoints != null && targetPoints != null) {
//                        String mountPoint = (String)mountPoints.get(0);
//                        String targetPoint = (String)targetPoints.get(0);
//                        String tempPath;
//                        if (targetPoint.equals(RegistryConstants.PATH_SEPARATOR) && !childPath.equals(mountPoint)) {
//                            tempPath = ((String)paths.get(0)).substring(mountPoint.length());
//                        } else {
//                            tempPath = targetPoint + ((String)paths.get(0)).substring(mountPoint.length());
//                        }
                        String tempPath = (String)actualPaths.get(0);
                        resourceData.setPutAllowed(
                        UserUtil.isPutAllowed(registry.getUserName(), tempPath, registry));
                        resourceData.setDeleteAllowed(UserUtil.isDeleteAllowed(registry.getUserName(),
                                 tempPath, registry));
                        resourceData.setGetAllowed(UserUtil.isGetAllowed(registry.getUserName(), tempPath, registry));
                        resourceData.setRealPath(tempPath);
                    } else if (user != null) {
                        if (registry.getUserName().equals(user)) {
                            resourceData.setPutAllowed(true);
                            resourceData.setDeleteAllowed(true);
                            resourceData.setGetAllowed(true);
                        } else {
                            resourceData.setPutAllowed(
                        UserUtil.isPutAllowed(registry.getUserName(), childPath, registry));
                            resourceData.setDeleteAllowed(
                        UserUtil.isDeleteAllowed(registry.getUserName(), childPath, registry));
                            resourceData.setGetAllowed(
                        UserUtil.isGetAllowed(registry.getUserName(), childPath, registry));
                        }
                        // Mounted resources should be accessed via the link, and we need not set
                        // the real path.
                    }
                } else {
                    resourceData.setPutAllowed(
                        UserUtil.isPutAllowed(registry.getUserName(), childPath, registry));
                    resourceData.setDeleteAllowed(
                        UserUtil.isDeleteAllowed(registry.getUserName(), childPath, registry));
                    resourceData.setGetAllowed(
                        UserUtil.isGetAllowed(registry.getUserName(), childPath, registry));
                }

                calculateAverageStars(resourceData);

                if(child.getProperty("registry.externalLink") != null) {
                    resourceData.setExternalLink(true);
                }
                if(child.getProperty("registry.absent") != null){
                    resourceData.setAbsent(child.getProperty("registry.absent"));
                }
                resourceDataList.add(resourceData);

            } catch (AuthorizationFailedException ignore) {
                // if we get an auth failed exception while accessing a child, we simply skip it.
                // we are not showing unauthorized resources.
            }
        }

        return resourceDataList.toArray(new ResourceData[resourceDataList.size()]);
    }

    public static ContentBean getContent(String path, UserRegistry registry) throws Exception {

        ResourcePath resourcePath = new ResourcePath(path);
        ContentBean bean = new ContentBean();

        Resource resource = registry.get(path);
        bean.setMediaType(resource.getMediaType());
        bean.setCollection(resource instanceof Collection);
        bean.setLoggedIn(!RegistryConstants.ANONYMOUS_USER.equals(registry.getUserName()));
        bean.setPathWithVersion(resourcePath.getPathWithVersion());
        bean.setAbsent(resource.getProperty("registry.absent"));
        List mountPoints = resource.getPropertyValues("registry.mountpoint");
        List targetPoints = resource.getPropertyValues("registry.targetpoint");
//        List paths = resource.getPropertyValues("registry.path");
        List actualPaths = resource.getPropertyValues("registry.actualpath"); 
        String user = resource.getProperty("registry.user");

        if (resource.getProperty("registry.link") != null) {

            if (mountPoints != null && targetPoints != null) {
//                String mountPoint = (String)mountPoints.get(0);
//                String targetPoint = (String)targetPoints.get(0);
//                String tempPath;
//                if (targetPoint.equals(RegistryConstants.PATH_SEPARATOR) && !childPath.equals(mountPoint)) {
//                    tempPath = ((String)paths.get(0)).substring(mountPoint.length());
//                } else {
//                    tempPath = targetPoint + ((String)paths.get(0)).substring(mountPoint.length());
//                }
                String tempPath = (String)actualPaths.get(0);
                bean.setPutAllowed(
                        UserUtil.isPutAllowed(registry.getUserName(), tempPath, registry));
                bean.setRealPath(tempPath);
            } else if (user != null) {
                if (registry.getUserName().equals(user)) {
                    bean.setPutAllowed(true);
                } else {
                    bean.setPutAllowed(
                        UserUtil.isPutAllowed(registry.getUserName(), path, registry));
                }
                // Mounted resources should be accessed via the link, and we need not set
                // the real path.
            }
        } else {
            boolean putAllowed = UserUtil.isPutAllowed(registry.getUserName(), path, registry);
            bean.setPutAllowed(putAllowed);
        }

        bean.setVersionView(!resourcePath.isCurrentVersion());
        bean.setContentPath(resourcePath.getCompletePath());
        resource.discard();
        
        return bean;
    }

    private static String[] getCollectionTypes() {
        return new String[] {"default", "Axis2 repository", "Synapse repository"};
    }

    private static void calculateAverageStars(ResourceData resourceData) {

        float tempRating = resourceData.getAverageRating() * 1000;
        tempRating = Math.round(tempRating);
        tempRating = tempRating / 1000;
        resourceData.setAverageRating(tempRating);

        float averageRating = resourceData.getAverageRating();
        String[] averageStars = new String[5];

        for (int i = 0; i < 5; i++) {

            if (averageRating >= i + 1) {
                averageStars[i] = "04";

            } else if (averageRating <= i) {
                averageStars[i] = "00";

            } else {

                float fraction = averageRating - i;

                if (fraction <= 0.125) {
                    averageStars[i] = "00";

                } else if (fraction > 0.125 && fraction <= 0.375) {
                    averageStars[i] = "01";

                } else if (fraction > 0.375 && fraction <= 0.625) {
                    averageStars[i] = "02";

                } else if (fraction > 0.625 && fraction <= 0.875) {
                    averageStars[i] = "03";

                } else {
                    averageStars[i] = "04";

                }
            }
        }

        resourceData.setAverageStars(averageStars);
    }
}
