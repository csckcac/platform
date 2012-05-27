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

package org.wso2.carbon.registry.search.services.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.secure.AuthorizationFailedException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.common.ResourceData;
import org.wso2.carbon.registry.common.utils.CommonUtil;
import org.wso2.carbon.registry.search.beans.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.beans.CustomSearchParameterBean;

import java.util.*;

public class AdvancedSearchResultsBeanPopulator {

    public static final Log log = LogFactory.getLog(AdvancedSearchResultsBeanPopulator.class);


    public static AdvancedSearchResultsBean populate(Registry configSystemRegistry, UserRegistry registry,
                                                     CustomSearchParameterBean propertyNameValues) {

        AdvancedSearchResultsBean advancedSearchResultsBean = new AdvancedSearchResultsBean();

        try {

            String[] childPaths =
                    getQueryResult(configSystemRegistry, registry, propertyNameValues.getParameterValues());

            String[][] tempPropValues = propertyNameValues.getParameterValues();

            for (int i = 0; i < tempPropValues.length; i++) {
                if (tempPropValues[i][0].equals("resourcePath")) {
                    String s = tempPropValues[i][1];
                    if (childPaths == null || childPaths.length == 0 && tempPropValues[i][0].indexOf("%") == -1) {
                        tempPropValues[i][1] = tempPropValues[i][1] + "%";
                        String[] s2 = getQueryResult(configSystemRegistry, registry, tempPropValues);
                        tempPropValues[i][1] = tempPropValues[i][1] + "/%";
                        String[] s1 = getQueryResult(configSystemRegistry, registry, tempPropValues);

                        if (s2 != null && s2.length > 0) {
                            Set<String> result = new HashSet<String>();
                            result.addAll(Arrays.asList(s2));
                            if (s1 != null && s1.length > 0) {
                                result.removeAll(Arrays.asList(s1));
                            }
                            childPaths = result.toArray(new String[result.size()]);
                        }
                    }
                    break;
                }
            }
            int resultSize = childPaths.length;

            List<ResourceData> resourceDataList = new ArrayList<ResourceData>();
            for (int i = 0; i < resultSize; i++) {

                try {
                    Resource child = registry.get(childPaths[i]);

                    if("true".equals(child.getProperty("registry.absent"))) {
                        continue;
                    }
                    ResourceData resourceData = new ResourceData();
                    resourceData.setResourcePath(childPaths[i]);

                    if (childPaths[i] != null) {
                        if (RegistryConstants.ROOT_PATH.equals(childPaths[i])) {
                            resourceData.setName("root");
                        } else {
                            String[] parts = childPaths[i].split(RegistryConstants.PATH_SEPARATOR);
                            resourceData.setName(parts[parts.length - 1]);
                        }
                    }


                    resourceData.setResourceType(child instanceof Collection ?
                            "collection" : "resource");
                    resourceData.setAuthorUserName(child.getAuthorUserName());
                    resourceData.setDescription(child.getDescription());
                    resourceData.setAverageRating(registry.getAverageRating(child.getPath()));
                    Calendar createdDateTime = Calendar.getInstance();
                    createdDateTime.setTime(child.getCreatedTime());
                    resourceData.setCreatedOn(createdDateTime);
                    CommonUtil.populateAverageStars(resourceData);

                    child.discard();

                    resourceDataList.add(resourceData);

                } catch (AuthorizationFailedException e) {
                    // do not show unauthorized resource in search results.
                }

            }

            advancedSearchResultsBean.setResourceDataList(resourceDataList.toArray(new ResourceData[resourceDataList.size()]));

        } catch (RegistryException e) {

            String msg = "Failed to get advanced search results. " + e.getMessage();
            advancedSearchResultsBean.setErrorMessage(msg);
        }
        catch (Exception e) {
            log.error("An error occurred while obtaining search results", e);
        }

        return advancedSearchResultsBean;
    }

    private static String[] getQueryResult(Registry configSystemRegistry, UserRegistry registry,
                                           String[][] propertyNameValues) throws Exception {

        AdvancedResourceQuery query = new AdvancedResourceQuery();

        Map<String, String> customValues = new HashMap<String, String>();

        for (int i = 0; i < propertyNameValues.length; i++) {
            if (propertyNameValues[i][0].equals("resourcePath")) {
                query.setResourceName(propertyNameValues[i][1]);
            } else if (propertyNameValues[i][0].equals("author")) {
                query.setAuthorName(propertyNameValues[i][1]);
            } else if (propertyNameValues[i][0].equals("updater")) {
                query.setUpdaterName(propertyNameValues[i][1]);
            } else if (propertyNameValues[i][0].equals("createdAfter")) {
                query.setCreatedAfter(CommonUtil.computeDate(propertyNameValues[i][1]));
            } else if (propertyNameValues[i][0].equals("createdBefore")) {
                query.setCreatedBefore(addOneDay(CommonUtil.computeDate(propertyNameValues[i][1])));
            } else if (propertyNameValues[i][0].equals("updatedAfter")) {
                query.setUpdatedAfter(CommonUtil.computeDate(propertyNameValues[i][1]));
            } else if (propertyNameValues[i][0].equals("updatedBefore")) {
                query.setUpdatedBefore(addOneDay(CommonUtil.computeDate(propertyNameValues[i][1])));
            } else if (propertyNameValues[i][0].equals("commentWords")) {
                query.setCommentWords(propertyNameValues[i][1]);
            } else if (propertyNameValues[i][0].equals("associationType")) {
                query.setAssociationType(propertyNameValues[i][1]);
            } else if (propertyNameValues[i][0].equals("associationDest")) {
                query.setAssociationDest(propertyNameValues[i][1]);
            } else if (propertyNameValues[i][0].equals("tags")) {
                query.setTags(propertyNameValues[i][1]);
            } else if (propertyNameValues[i][0].equals("propertyName")) {
                query.setPropertyName(propertyNameValues[i][1]);
            } else if (propertyNameValues[i][0].equals("leftPropertyValue")) {
                query.setLeftPropertyValue(propertyNameValues[i][1]);
            } else if (propertyNameValues[i][0].equals("rightPropertyValue")) {
                query.setRightPropertyValue(propertyNameValues[i][1]);   
            } else if (propertyNameValues[i][0].equals("authorNameNegate")) {
                query.setAuthorNameNegate(propertyNameValues[i][1]);
            } else if (propertyNameValues[i][0].equals("updaterNameNegate")) {
                query.setUpdaterNameNegate(propertyNameValues[i][1]);
            } else if (propertyNameValues[i][0].equals("createdRangeNegate")) {
                query.setCreatedRangeNegate(propertyNameValues[i][1]);
            } else if (propertyNameValues[i][0].equals("updatedRangeNegate")) {
                query.setUpdatedRangeNegate(propertyNameValues[i][1]);
            } else if (propertyNameValues[i][0].equals("leftOp")) {
                query.setLeftOp(propertyNameValues[i][1]);
            } else if (propertyNameValues[i][0].equals("rightOp")) {
                query.setRightOp(propertyNameValues[i][1]);    
            } else if (propertyNameValues[i][0].equals("content")) {
                query.setContent(propertyNameValues[i][1]);
            } else if (propertyNameValues[i][0].equals("mediaType")) {
                query.setMediaType(propertyNameValues[i][1]);
            } else if (propertyNameValues[i][0].equals("mediaTypeNegate")) {
                query.setMediaTypeNegate(propertyNameValues[i][1]);
            } else {

                customValues.put(propertyNameValues[i][0], propertyNameValues[i][1]);
            }
        }        

        boolean first = true, noCustomSearch = true;
        Set<String> s = new HashSet<String>();

        for (Map.Entry<String, String> entry : customValues.entrySet()) {
            if (!entry.getValue().equals("")) {
                Map<String, String> temp = new HashMap();
                temp.put(entry.getKey(), entry.getValue());

                query.setCustomSearchValues(temp);
                Resource qResults = query.execute(configSystemRegistry, registry);

                if (((String[]) qResults.getContent()).length > 0) {
                    if (first) {
                        s.addAll(Arrays.asList((String[]) qResults.getContent()));
                        first = false;
                    } else {
                        s.retainAll(Arrays.asList((String[]) qResults.getContent()));
                    }
                } else {
                    s.clear();
                    return new String[0];
                }


                noCustomSearch = false;
            }
        }

        if (noCustomSearch) {
            query.setCustomSearchValues(customValues);
            Resource qResults = query.execute(configSystemRegistry, registry);

            return (String[]) qResults.getContent();
        }

        String[] ret = new String[s.size()];
        ret = s.toArray(ret);

        return ret;
    }

    private static Date addOneDay(Date date) {
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTime();
    }
}
