/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.hostobjects;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.dto.UserApplicationAPIUsage;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.DocumentationType;
import org.wso2.carbon.apimgt.api.model.DuplicateAPIException;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.utils.APINameComparator;
import org.wso2.carbon.apimgt.usage.client.APIUsageStatisticsClient;
import org.wso2.carbon.apimgt.usage.client.dto.*;
import org.wso2.carbon.apimgt.usage.client.dto.APIUsageDTO;
import org.wso2.carbon.apimgt.usage.client.dto.APIVersionLastAccessTimeDTO;
import org.wso2.carbon.apimgt.usage.client.dto.APIVersionUsageDTO;
import org.wso2.carbon.apimgt.usage.client.dto.PerUserAPIUsageDTO;
import org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException;
import org.wso2.carbon.hostobjects.file.FileHostObject;
import org.wso2.carbon.hostobjects.web.RequestHostObject;
import org.wso2.carbon.scriptengine.exceptions.ScriptException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class APIProviderHostObject extends ScriptableObject {
    
    private static final Log log = LogFactory.getLog(APIProviderHostObject.class);

    private String username;
    
    private APIProvider apiProvider;

    public String getClassName() {
        return "APIProvider";
    }

    // The zero-argument constructor used for create instances for runtime
    public APIProviderHostObject() throws APIManagementException {
        apiProvider = APIManagerFactory.getInstance().getAPIProvider();
        //apiProvider = APIManagerFactory.getInstance().getAPIProvider();
    }

    public APIProviderHostObject(String loggedUser) throws APIManagementException {
        username = loggedUser;
        apiProvider = APIManagerFactory.getInstance().getAPIProvider(loggedUser);
    }
    
    public String getUsername() {
        return username;
    }

    public static Scriptable jsConstructor(Context cx, Object[] args, Function Obj,
                                           boolean inNewExpr)
            throws ScriptException, APIManagementException {

        int length = args.length;
        if (length == 1) {
            String username = (String) args[0];
            return new APIProviderHostObject(username);
        }
        return new APIProviderHostObject();
    }

    public APIProvider getApiProvider() {
        return apiProvider;
    }

    private static APIProvider getAPIProvider(Scriptable thisObj) {
        return ((APIProviderHostObject) thisObj).getApiProvider();
    }

    public static boolean jsFunction_login(Context cx, Scriptable thisObj,
                                           Object[] args, Function funObj) throws ScriptException {
        return true;
    }

    public static NativeArray jsFunction_getAPIUsageHostTest(String APIname, String serverURL)
            throws ScriptException {
        NativeArray myn = new NativeArray(0L);
        String[] usage = {"1.0.0", "10", "2.2.1", "40"};

        for (int i = 0; i < usage.length; i++) {
            myn.put(i, myn, usage[i]);
        }
        return myn;
    }

    /**
     * This method is to functionality of add a new API in API-Provider
     *
     * @param cx      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return true if the API was added successfully               
     * @throws ScriptException Wrapped exception by org.wso2.carbon.scriptengine.exceptions.ScriptException
     */
    public static boolean jsFunction_addAPI(Context cx, Scriptable thisObj,
                                            Object[] args,
                                            Function funObj) throws ScriptException {
        if (args.length == 0) {
            throw new ScriptException("Invalid number of input parameters.");
        }
        
        boolean success = false;
        NativeObject apiData = (NativeObject) args[0];
        String provider = (String) apiData.get("provider", apiData);
        String name = (String) apiData.get("apiName", apiData);
        String version = (String) apiData.get("version", apiData);
        String description = (String) apiData.get("description", apiData);
        String endpoint = (String) apiData.get("endpoint", apiData);
        String sandboxUrl = (String) apiData.get("sandbox", apiData);
        if ("".equals(sandboxUrl)) {
            sandboxUrl = null;
        }
        String wsdl = (String) apiData.get("wsdl", apiData);
        String tags = (String) apiData.get("tags", apiData);

        Set<String> tag = new HashSet<String>();
        if (tags.indexOf(",") >= 0) {
            String[] userTag = tags.split(",");
            tag.addAll(Arrays.asList(userTag).subList(0, tags.split(",").length));
        } else {
            tag.add(tags);
        }

        String tier = (String) apiData.get("tier", apiData);
        FileHostObject fileHostObject = (FileHostObject) apiData.get("imageUrl", apiData);
        String contextVal = (String) apiData.get("context", apiData);
        String context = contextVal.startsWith("/") ? contextVal : ("/" + contextVal);

        HttpServletRequest req = ((RequestHostObject) apiData.get("request", apiData)).getHttpServletRequest();
        NativeArray uriTemplateArr = (NativeArray) apiData.get("uriTemplateArr", apiData);

        APIIdentifier apiId = new APIIdentifier(provider, name, version);
        APIProvider apiProvider = getAPIProvider(thisObj);
        try {
            if (apiProvider.isAPIAvailable(apiId)) {
                throw new ScriptException("Failed saving the new API due to an API already exists " +
                                          " with same name: " + name + " and version: "
                                          + version + " for the provider: " + provider);
            }
            
            API api = new API(apiId);
            NativeArray uriMethodArr = (NativeArray) apiData.get("uriMethodArr", apiData);
            if (uriTemplateArr.getLength() == uriMethodArr.getLength()) {
                Set<URITemplate> uriTemplates = new HashSet<URITemplate>();
                for (int i = 0; i < uriTemplateArr.getLength(); i++) {
                    URITemplate templates = new URITemplate();
                    templates.setUriTemplate((String) uriTemplateArr.get(i, uriTemplateArr));
                    String uriMethods = (String) uriMethodArr.get(i, uriMethodArr);
                    String[] uriMethodArray = uriMethods.split(",");
                    for (String anUriMethod : uriMethodArray) {
                        templates.addMethod(anUriMethod);
                    }
                    templates.setResourceURI(endpoint);
                    templates.setResourceSandboxURI(sandboxUrl);
                    uriTemplates.add(templates);
                }
                api.setUriTemplates(uriTemplates);
            }

            api.setDescription(description);
            api.setWsdlUrl(wsdl);
            api.setLastUpdated(new Date());
            api.setUrl(endpoint);
            api.setSandboxUrl(sandboxUrl);
            api.addTags(tag);
            
            Set<Tier> availableTier = new HashSet<Tier>();
            String[] tierNames = tier.split(",");
            for (String tierName : tierNames) {
                availableTier.add(new Tier(tierName));
            }
            api.addAvailableTiers(availableTier);
            api.setStatus(APIStatus.CREATED);
            api.setContext(context);

            checkFileSize(fileHostObject);
            apiProvider.addAPI(api);

            if (fileHostObject != null) {
                api.setThumbnailUrl(apiProvider.addIcon(apiId, fileHostObject.getInputStream(),
                        fileHostObject.getJavaScriptFile().getContentType()));
                apiProvider.updateAPI(api);
            }
            success = true;

        } catch (APIManagementException e) {
            log.error("Error while adding the API: " + name + "-" + version, e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return success;

    }

    public static boolean jsFunction_updateAPI(Context cx, Scriptable thisObj,
                                               Object[] args,
                                               Function funObj) throws ScriptException {

        if (args.length == 0) {
            throw new ScriptException("Invalid number of input parameters.");
        }

        NativeObject apiData = (NativeObject) args[0];
        boolean success = false;
        String provider = (String) apiData.get("provider", apiData);
        String name = (String) apiData.get("apiName", apiData);
        String version = (String) apiData.get("version", apiData);
        String description = (String) apiData.get("description", apiData);
        FileHostObject fileHostObject = (FileHostObject) apiData.get("imageUrl", apiData);
        String endpoint = (String) apiData.get("endpoint", apiData);
        String sandboxUrl = (String) apiData.get("sandbox", apiData);
        if ("".equals(sandboxUrl)) {
            sandboxUrl = null;
        }
        String wsdl = (String) apiData.get("wsdl", apiData);
        String tags = (String) apiData.get("tags", apiData);
        Set<String> tag = new HashSet<String>();
        if (tags.indexOf(",") >= 0) {
            String[] userTag = tags.split(",");
            tag.addAll(Arrays.asList(userTag).subList(0, tags.split(",").length));
        } else {
            tag.add(tags);
        }
        APIIdentifier oldApiId = new APIIdentifier(provider, name, version);
        APIProvider apiProvider = getAPIProvider(thisObj);
        try {
            API oldApi = apiProvider.getAPI(oldApiId);
            HttpServletRequest req = ((RequestHostObject) apiData.get("request", apiData)).getHttpServletRequest();

            String tier = (String) apiData.get("tier", apiData);
            String contextVal = (String) apiData.get("context", apiData);
            String context = contextVal.startsWith("/") ? contextVal : ("/" + contextVal);

            APIIdentifier apiId = new APIIdentifier(provider, name, version);
            API api = new API(apiId);

            NativeArray uriTemplateArr = (NativeArray) apiData.get("uriTemplateArr", apiData);
            NativeArray uriMethodArr = (NativeArray) apiData.get("uriMethodArr", apiData);

            if (uriTemplateArr.getLength() == uriMethodArr.getLength()) {
                Set<URITemplate> uriTemplates = new HashSet<URITemplate>();

                for (int i = 0; i < uriTemplateArr.getLength(); i++) {
                    URITemplate templates = new URITemplate();
                    String templateVal = (String) uriTemplateArr.get(i, uriTemplateArr);
                    String template = templateVal.startsWith("/") ? templateVal : ("/" + templateVal);
                    templates.setUriTemplate(template);
                    String uriMethods=(String) uriMethodArr.get(i, uriMethodArr);
                    String[] uriMethodArray = uriMethods.split(",");
                    for (String anUriMethod : uriMethodArray) {
                        templates.addMethod(anUriMethod);
                    }
                    templates.setResourceURI(endpoint);
                    templates.setResourceSandboxURI(sandboxUrl);
                    uriTemplates.add(templates);
                }
                api.setUriTemplates(uriTemplates);
            }

            api.setDescription(description);
            api.setLastUpdated(new Date());
            api.setUrl(endpoint);
            api.setSandboxUrl(sandboxUrl);
            api.addTags(tag);
            api.setContext(context);

            Set<Tier> availableTier = new HashSet<Tier>();
            String[] tierNames = tier.split(",");
            for (String tierName : tierNames) {
                availableTier.add(new Tier(tierName));
            }
            api.addAvailableTiers(availableTier);

            api.setStatus(oldApi.getStatus());
            api.setWsdlUrl(wsdl);
            api.setLastUpdated(new Date());
            checkFileSize(fileHostObject);

            if (fileHostObject != null) {
                api.setThumbnailUrl(apiProvider.addIcon(apiId, fileHostObject.getInputStream(),
                        fileHostObject.getJavaScriptFile().getContentType()));
            } else if (oldApi.getThumbnailUrl() != null) {
                // retain the previously uploaded image
                api.setThumbnailUrl(oldApi.getThumbnailUrl());
            }
            apiProvider.updateAPI(api);
            success = true;
        } catch (APIManagementException e) {
            log.error("Error while updating the API: " + name + "-" + version, e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return success;
    }

    public static boolean jsFunction_updateAPIStatus(Context cx, Scriptable thisObj,
                                               Object[] args,
                                               Function funObj) throws ScriptException {
        if (args.length == 0) {
            throw new ScriptException("Invalid number of input parameters.");
        }

        NativeObject apiData = (NativeObject) args[0];
        boolean success = false;
        String provider = (String) apiData.get("provider", apiData);
        String name = (String) apiData.get("apiName", apiData);
        String version = (String) apiData.get("version", apiData);
        String status = (String) apiData.get("status", apiData);
        boolean publishToGateway = Boolean.parseBoolean((String) apiData.get("publishToGateway", apiData));

        try {
            APIProvider apiProvider = getAPIProvider(thisObj);
            APIIdentifier apiId = new APIIdentifier(provider, name, version);
            API api = apiProvider.getAPI(apiId);
            apiProvider.changeAPIStatus(api, getApiStatus(status),
                    ((APIProviderHostObject) thisObj).getUsername(), publishToGateway);
            success = true;
        } catch (APIManagementException e) {
            log.error("Error while updating API status", e);
        }
        return success;
    }

    private static void checkFileSize(FileHostObject fileHostObject)
            throws ScriptException, APIManagementException {
        if (fileHostObject != null) {
            long length = fileHostObject.getJavaScriptFile().getLength();
            if (length / 1024.0 > 1024) {
                throw new APIManagementException("Image file exceeds the maximum limit of 1MB");
            }
        }
    }

    /**
     * This method is to functionality of getting an existing API to API-Provider based
     *
     * @param cx      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return a native array
     * @throws ScriptException Wrapped exception by org.wso2.carbon.scriptengine.exceptions.ScriptException
     */

    public static NativeArray jsFunction_getAPI(Context cx, Scriptable thisObj,
                                                Object[] args,
                                                Function funObj) throws ScriptException {
        NativeArray myn = new NativeArray(0);

        if (args.length != 3 || !isStringValues(args)) {
            throw new ScriptException("Invalid number of parameters or their types.");
        }
        String providerName = args[0].toString();
        String apiName = args[1].toString();
        String version = args[2].toString();

        APIIdentifier apiId = new APIIdentifier(providerName, apiName, version);
        APIProvider apiProvider = getAPIProvider(thisObj);
        try {
            API api = apiProvider.getAPI(apiId);
            Set<URITemplate> uriTemplates = api.getUriTemplates();
            myn.put(0, myn, checkValue(api.getId().getApiName()));
            myn.put(1, myn, checkValue(api.getDescription()));
            myn.put(2, myn, checkValue(api.getUrl()));
            myn.put(3, myn, checkValue(api.getWsdlUrl()));
            myn.put(4, myn, checkValue(api.getId().getVersion()));
            StringBuffer tagsSet = new StringBuffer("");
            for (int k = 0; k < api.getTags().toArray().length; k++) {
                tagsSet.append(api.getTags().toArray()[k].toString());
                if (k != api.getTags().toArray().length - 1) {
                    tagsSet.append(",");
                }
            }
            myn.put(5, myn, checkValue(tagsSet.toString()));
            StringBuffer tiersSet = new StringBuffer("");
            Set<Tier> tierSet = api.getAvailableTiers();
            Iterator it = tierSet.iterator();
            int j = 0;
            while (it.hasNext()) {
                Object tierObject = it.next();
                Tier tier = (Tier) tierObject;
                tiersSet.append(tier.getName());
                if (j != tierSet.size() - 1) {
                    tiersSet.append(",");
                }
                j++;
            }

            myn.put(6, myn, checkValue(tiersSet.toString()));
            myn.put(7, myn, checkValue(api.getStatus().toString()));
            myn.put(8, myn, api.getThumbnailUrl());
            myn.put(9, myn, api.getContext());
            myn.put(10, myn, checkValue(api.getLastUpdated().toString()));
            myn.put(11, myn, getSubscriberCount(apiId, thisObj));

            if (uriTemplates.size() != 0) {
                NativeArray uriTempArr = new NativeArray(uriTemplates.size());
                Iterator i = uriTemplates.iterator();
                List<NativeArray> uriTemplatesArr = new ArrayList<NativeArray>();
                while (i.hasNext()) {
                    List<String> utArr = new ArrayList<String>();
                    URITemplate ut = (URITemplate) i.next();
                    utArr.add(ut.getUriTemplate());
                    utArr.add(ut.getMethodsAsString().replaceAll("\\s",","));;

                    NativeArray utNArr = new NativeArray(utArr.size());
                    for (int p = 0; p < utArr.size(); p++) {
                        utNArr.put(p, utNArr, utArr.get(p));
                    }
                    uriTemplatesArr.add(utNArr);
                }

                for (int c = 0; c < uriTemplatesArr.size(); c++) {
                    uriTempArr.put(c, uriTempArr, uriTemplatesArr.get(c));
                }

                myn.put(12, myn, uriTempArr);
                myn.put(13, myn, checkValue(api.getSandboxUrl()));
            }
        } catch (APIManagementException e) {
            log.error("Error from registry while getting API information for the api: " + apiName + "-" + version, e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return myn;
    }

    public static NativeArray jsFunction_getSubscriberCountByAPIs(Context cx, Scriptable thisObj,
                                                Object[] args,
                                                Function funObj) throws ScriptException {
        NativeArray myn = new NativeArray(0);
        String providerName = null;
        APIProvider apiProvider = getAPIProvider(thisObj);
        try {
            if (args.length == 0) {
                throw new ScriptException("Invalid number of input parameters.");
            }
            providerName = (String) args[0];
            if (providerName != null) {
                List<API> apiSet = apiProvider.getAPIsByProvider(providerName);
                Map<String,Long> subscriptions = new TreeMap<String,Long>();
                for (API api : apiSet) {
                    if (api.getStatus() == APIStatus.CREATED) {
                        continue;
                    }                    
                    long count = apiProvider.getAPISubscriptionCountByAPI(api.getId());
                    Long currentCount = subscriptions.get(api.getId().getApiName());
                    if (currentCount != null) {
                        subscriptions.put(api.getId().getApiName(), currentCount + count);
                    } else {
                        subscriptions.put(api.getId().getApiName(), count);
                    }
                }
                
                int i = 0;
                for (Map.Entry<String,Long> entry : subscriptions.entrySet()) {
                    NativeObject row = new NativeObject();
                    row.put("apiName", row, entry.getKey());
                    row.put("count", row, entry.getValue().longValue());
                    myn.put(i, myn, row);
                    i++;
                }                                
            }
        } catch (APIManagementException e) {
            log.error("Error from registry while getting subscribers of the provider: " + providerName, e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return myn;        
    }

    public static NativeArray jsFunction_getTiers(Context cx, Scriptable thisObj,
                                                                         Object[] args,
                                                                         Function funObj) throws ScriptException {
        NativeArray myn = new NativeArray(0);
        APIProvider apiProvider = getAPIProvider(thisObj);
        try {
            Set<Tier> tiers = apiProvider.getTiers();
            int i = 0;
            for (Tier tier : tiers) {
                NativeObject row = new NativeObject();
                row.put("tierName", row, tier.getName());
                myn.put(i, myn, row);
                i++;
            }
        } catch (APIManagementException e) {
            log.error("Error from registry while getting available tiers", e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return myn;
    }

    public static NativeArray jsFunction_getSubscriberCountByAPIVersions(Context cx, Scriptable thisObj,
                                                                  Object[] args,
                                                                  Function funObj) throws ScriptException {
        NativeArray myn = new NativeArray(0);
        String providerName = null;
        String apiName = null;
        APIProvider apiProvider = getAPIProvider(thisObj);
        try {
            if (args.length == 0 || args.length == 1) {
                throw new ScriptException("Invalid number of input parameters.");
            }
            providerName = (String) args[0];
            apiName = (String) args[1];
            if (providerName != null && apiName != null) {
                List<API> apiSet = apiProvider.getAPIsByProvider(providerName);
                Map<String,Long> subscriptions = new TreeMap<String,Long>();
                for (API api : apiSet) {
                    if (!api.getId().getApiName().equals(apiName) || api.getStatus() == APIStatus.CREATED) {
                        continue;
                    }
                    long count = apiProvider.getAPISubscriptionCountByAPI(api.getId());
                    subscriptions.put(api.getId().getVersion(), count);
                }

                int i = 0;
                for (Map.Entry<String,Long> entry : subscriptions.entrySet()) {
                    NativeObject row = new NativeObject();
                    row.put("apiVersion", row, entry.getKey());
                    row.put("count", row, entry.getValue().longValue());
                    myn.put(i, myn, row);
                    i++;
                }
            }
        } catch (APIManagementException e) {
            log.error("Error from registry while getting subscribers of the " +
                    "provider: " + providerName + " and API: " + apiName, e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return myn;
    }
    
    private static int getSubscriberCount(APIIdentifier apiId, Scriptable thisObj) throws APIManagementException {
        APIProvider apiProvider = getAPIProvider(thisObj);
        Set<Subscriber> subs = apiProvider.getSubscribersOfAPI(apiId);
        Set<String> subscriberNames = new HashSet<String>();
        for (Subscriber sub : subs) {
            subscriberNames.add(sub.getName());
        }
        return subscriberNames.size();
    }

    /**
     * This method is to functionality of getting all the APIs stored
     *
     * @param cx      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return a native array
     * @throws ScriptException Wrapped exception by org.wso2.carbon.scriptengine.exceptions.ScriptException
     */
    public static NativeArray jsFunction_getAllAPIs(Context cx, Scriptable thisObj,
                                                           Object[] args,
                                                           Function funObj) throws ScriptException {
        NativeArray myn = new NativeArray(0);
        APIProvider apiProvider = getAPIProvider(thisObj);
        try {
            List<API> apiList = apiProvider.getAllAPIs();
            Iterator it = apiList.iterator();
            int i = 0;
            while (it.hasNext()) {
                NativeObject row = new NativeObject();
                Object apiObject = it.next();
                API api = (API) apiObject;
                APIIdentifier apiIdentifier = api.getId();
                row.put("apiName", row, apiIdentifier.getApiName());
                row.put("version", row, apiIdentifier.getVersion());
                row.put("provider", row, apiIdentifier.getProviderName());
                row.put("status", row, checkValue(api.getStatus().toString()));
                row.put("thumb", row, api.getThumbnailUrl());
                row.put("subs", row, getSubscriberCount(apiIdentifier, thisObj));
                myn.put(i, myn, row);
                i++;
            }
        } catch (APIManagementException e) {
            log.error("Error from registry while getting the APIs", e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }        
        return myn;
    }

    /**
     * This method is to functionality of getting all the APIs stored per provider
     *
     * @param cx      Rhino context
     * @param thisObj Scriptable object
     * @param args    Passing arguments
     * @param funObj  Function object
     * @return a native array
     * @throws ScriptException Wrapped exception by org.wso2.carbon.scriptengine.exceptions.ScriptException
     */
    public static NativeArray jsFunction_getAPIsByProvider(Context cx, Scriptable thisObj,
                                                           Object[] args,
                                                           Function funObj) throws ScriptException {
        NativeArray myn = new NativeArray(0);
        if (args.length == 0) {
            throw new ScriptException("Invalid number of parameters.");
        }
        String providerName = (String) args[0];
        if (providerName != null) {
            APIProvider apiProvider = getAPIProvider(thisObj);
            try {
                List<API> apiList = apiProvider.getAPIsByProvider(providerName);
                Iterator it = apiList.iterator();
                int i = 0;
                while (it.hasNext()) {
                    NativeObject row = new NativeObject();
                    Object apiObject = it.next();
                    API api = (API) apiObject;
                    APIIdentifier apiIdentifier = api.getId();
                    row.put("apiName", row, apiIdentifier.getApiName());
                    row.put("version", row, apiIdentifier.getVersion());
                    row.put("status", row, checkValue(api.getStatus().toString()));
                    row.put("thumb", row, api.getThumbnailUrl());
                    row.put("subs", row, getSubscriberCount(apiIdentifier, thisObj));
                    myn.put(i, myn, row);
                    i++;
                }
            } catch (APIManagementException e) {
                log.error("Error from registry while getting all the APIs information for " +
                        "the provider: " + providerName, e);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return myn;
    }

    public static NativeArray jsFunction_getSubscribedAPIs(Context cx, Scriptable thisObj,
                                                           Object[] args,
                                                           Function funObj) throws ScriptException {
        String userName = null;
        NativeArray myn = new NativeArray(0);
        APIProvider apiProvider = getAPIProvider(thisObj);
        try {
            if (args.length != 1 || !isStringValues(args)) {
                throw new ScriptException("Invalid number of parameters or their types.");
            }
            userName = (String) args[0];
            Subscriber subscriber = new Subscriber(userName);
            Set<API> apiSet = apiProvider.getSubscriberAPIs(subscriber);
            Iterator it = apiSet.iterator();
            int i = 0;
            while (it.hasNext()) {
                NativeObject row = new NativeObject();
                Object apiObject = it.next();
                API api = (API) apiObject;
                APIIdentifier apiIdentifier = api.getId();
                row.put("apiName", row, apiIdentifier.getApiName());
                row.put("version", row, apiIdentifier.getVersion());
                row.put("updatedDate", row, api.getLastUpdated().toString());
                myn.put(i, myn, row);
                i++;
            }
        } catch (APIManagementException e) {
            log.error("Error from registry while getting the subscribed APIs information " +
                    "for the subscriber" + userName, e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return myn;
    }

    public static NativeArray jsFunction_getAllAPIUsageByProvider(Context cx, Scriptable thisObj,
                                                                  Object[] args, Function funObj) throws ScriptException {

        NativeArray myn = new NativeArray(0);
        String providerName = null;
        APIProvider apiProvider = getAPIProvider(thisObj);
        try {
            if (args.length == 0) {
                throw new ScriptException("Invalid number of input parameters.");
            }
            providerName = (String) args[0];
            if (providerName != null) {
                UserApplicationAPIUsage[] apiUsages = apiProvider.getAllAPIUsageByProvider(providerName);
                for (int i = 0; i < apiUsages.length; i++) {
                    NativeObject row = new NativeObject();
                    row.put("userName", row, apiUsages[i].getUserId());
                    row.put("application", row, apiUsages[i].getApplicationName());
                    StringBuffer apiSet = new StringBuffer("");
                    for (int k = 0; k < apiUsages[i].getApiIdentifiers().length; k++) {
                        apiSet.append(apiUsages[i].getApiIdentifiers()[k].getApiName());
                        apiSet.append("-");
                        apiSet.append(apiUsages[i].getApiIdentifiers()[k].getVersion());
                        if (k != apiUsages[i].getApiIdentifiers().length - 1) {
                            apiSet.append(",");
                        }
                    }
                    row.put("apis", row, apiSet.toString());
                    myn.put(i, myn, row);
                }
            }
        } catch (APIManagementException e) {
            log.error("Error from registry while getting subscribers of the provider: " + providerName, e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return myn;
    }

    public static NativeArray jsFunction_getAllDocumentation(Context cx, Scriptable thisObj,
                                                             Object[] args, Function funObj) throws ScriptException {
        String apiName = null;
        String version = null;
        String providerName;
        NativeArray myn = new NativeArray(0);
        APIProvider apiProvider = getAPIProvider(thisObj);
        try {
            if (args.length != 3 || !isStringValues(args)) {
                throw new ScriptException("Invalid number of parameters or their types.");
            }
            providerName = args[0].toString();
            apiName = args[1].toString();
            version = args[2].toString();
            APIIdentifier apiId = new APIIdentifier(providerName, apiName, version);

            List<Documentation> docsList = apiProvider.getAllDocumentation(apiId);
            Iterator it = docsList.iterator();
            int i = 0;
            while (it.hasNext()) {

                NativeObject row = new NativeObject();
                Object docsObject = it.next();
                Documentation doc = (Documentation) docsObject;
                Object objectSourceType = doc.getSourceType();
                String strSourceType = objectSourceType.toString();
                row.put("docName", row, doc.getName());
                row.put("docType", row, doc.getType().getType());
                row.put("sourceType", row, strSourceType);
                row.put("docLastUpdated", row, doc.getLastUpdated().toString());
                //row.put("sourceType", row, doc.getSourceType());
                if (Documentation.DocumentSourceType.URL.equals(doc.getSourceType())) {
                    row.put("sourceUrl", row, doc.getSourceUrl());
                }

                row.put("summary", row, doc.getSummary());
                myn.put(i, myn, row);
                i++;

            }

        } catch (APIManagementException e) {
            log.error("Error from registry while getting document information for the api: " + 
                    apiName + "-" + version, e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return myn;
    }

    public static NativeArray jsFunction_getInlineContent(Context cx,
                                                          Scriptable thisObj, Object[] args,
                                                          Function funObj)
            throws ScriptException, APIManagementException, UnsupportedEncodingException {
        String apiName;
        String version;
        String providerName;
        String docName;
        String content;
        NativeArray myn = new NativeArray(0);

        if (args.length != 4 || !isStringValues(args)) {
            throw new ScriptException("Invalid number of parameters or their types.");
        }
        providerName = args[0].toString();
        apiName = args[1].toString();
        version = args[2].toString();
        docName = args[3].toString();
        APIIdentifier apiId = new APIIdentifier(providerName, apiName, version);
        APIProvider apiProvider = getAPIProvider(thisObj);
        try {
            content = apiProvider.getDocumentationContent(apiId, docName);
            //log.info(content);
            //log.info(URLEncoder.encode(content));
        } catch (Exception e) {
            log.error("Error while getting Inline Document Content ", e);
            return null;
        }
        NativeObject row = new NativeObject();
        row.put("providerName", row, providerName);
        row.put("apiName", row, apiName);
        row.put("apiVersion", row, version);
        row.put("docName", row, docName);
        row.put("content", row, content);
        myn.put(0, myn, row);
        return myn;
    }

    public static void jsFunction_addInlineContent(Context cx,
                                                   Scriptable thisObj, Object[] args,
                                                   Function funObj)
            throws ScriptException, APIManagementException {
        String apiName;
        String version;
        String providerName;
        String docName;
        String docContent;
        //log.info(isStringValues(args)+"args.length"+args.length);
        if (args.length != 5 || !isStringValues(args)) {
            throw new ScriptException("Invalid number of parameters or their types.");
        }
        providerName = args[0].toString();
        apiName = args[1].toString();
        version = args[2].toString();
        docName = args[3].toString();
        docContent = args[4].toString();
        if (docContent != null) {
            docContent = docContent.replaceAll("\n", "");
        }
        APIIdentifier apiId = new APIIdentifier(providerName, apiName,
                                                version);
        APIProvider apiProvider = getAPIProvider(thisObj);
        apiProvider.addDocumentationContent(apiId, docName, docContent);
    }

    public static boolean jsFunction_addDocumentation(Context cx, Scriptable thisObj,
                                                      Object[] args, Function funObj)
            throws ScriptException {
        if (args.length < 5 || !isStringValues(args)) {
            throw new ScriptException("Invalid number of parameters or their types.");
        }
        boolean success = false;
        String providerName = args[0].toString();
        String apiName = args[1].toString();
        String version = args[2].toString();
        String docName = args[3].toString();
        String docType = args[4].toString();
        String summary = args[5].toString();
        String sourceType = args[6].toString();
        String sourceURL = null;

        APIIdentifier apiId = new APIIdentifier(providerName, apiName, version);
        Documentation doc = new Documentation(getDocType(docType), docName);
        if (sourceType.equalsIgnoreCase(Documentation.DocumentSourceType.URL.toString())) {
            doc.setSourceType(Documentation.DocumentSourceType.URL);
            sourceURL = args[7].toString();
        } else {
            doc.setSourceType(Documentation.DocumentSourceType.INLINE);
        }
        doc.setSummary(summary);
        doc.setSourceUrl(sourceURL);
        APIProvider apiProvider = getAPIProvider(thisObj);
        try {
            apiProvider.addDocumentation(apiId, doc);
            success = true;
        } catch (APIManagementException e) {
            log.error("Error from registry while adding the document: " + docName + 
                    " for the api :" + apiName + "-" + version, e);
        }
        return success;
    }

    public static boolean jsFunction_removeDocumentation(Context cx, Scriptable thisObj,
                                                         Object[] args, Function funObj)
            throws ScriptException {
        if (args.length != 5 || !isStringValues(args)) {
            throw new ScriptException("Invalid number of parameters or their types.");
        }
        boolean success = false;
        String providerName = args[0].toString();
        String apiName = args[1].toString();
        String version = args[2].toString();
        String docName = args[3].toString();
        String docType = args[4].toString();

        APIIdentifier apiId = new APIIdentifier(providerName, apiName, version);

        APIProvider apiProvider = getAPIProvider(thisObj);
        try {
            apiProvider.removeDocumentation(apiId, docName, docType);
            success = true;
        } catch (APIManagementException e) {
            log.error("Error from registry while removing the document: " + docName + 
                    " for the api:" + apiName + "-" + version, e);
        }
        return success;
    }

    public static boolean jsFunction_createNewAPIVersion(Context cx, Scriptable thisObj,
                                                         Object[] args, Function funObj)
            throws ScriptException {
        
        boolean success = false;
        if (args.length != 4 || !isStringValues(args)) {
            throw new ScriptException("Invalid number of parameters or their types.");
        }
        String providerName = args[0].toString();
        String apiName = args[1].toString();
        String version = args[2].toString();
        String newVersion = args[3].toString();

        APIIdentifier apiId = new APIIdentifier(providerName, apiName, version);
        API api = new API(apiId);
        APIProvider apiProvider = getAPIProvider(thisObj);
        try {
            apiProvider.createNewAPIVersion(api, newVersion);
            success = true;
        } catch (APIManagementException e) {
            log.error("Error from registry while creating a new api version: " + newVersion, e);
        } catch (DuplicateAPIException e) {
            log.error("Duplicate versioning error while create a new api version", e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return success;
    }

    public static NativeArray jsFunction_getSubscribersOfAPI(Context cx, Scriptable thisObj,
                                                             Object[] args, Function funObj)
            throws ScriptException {
        String apiName;
        String version;
        String providerName;
        NativeArray myn = new NativeArray(0);
        if (args.length != 3 || !isStringValues(args)) {
            throw new ScriptException("Invalid number of parameters or their types.");
        }

        providerName = args[0].toString();
        apiName = args[1].toString();
        version = args[2].toString();

        APIIdentifier apiId = new APIIdentifier(providerName, apiName, version);
        Set<Subscriber> subscribers;
        APIProvider apiProvider = getAPIProvider(thisObj);
        try {
            subscribers = apiProvider.getSubscribersOfAPI(apiId);
            Iterator it = subscribers.iterator();
            int i = 0;
            while (it.hasNext()) {
                NativeObject row = new NativeObject();
                Object subscriberObject = it.next();
                Subscriber user = (Subscriber) subscriberObject;
                row.put("userName", row, user.getName());
                row.put("subscribedDate", row, checkValue(user.getSubscribedDate().toString()));
                myn.put(i, myn, row);
                i++;
            }

        } catch (APIManagementException e) {
            log.error("Error from registry while getting subscribers for the API: " + apiName + 
                    "-" + version, e);
        }
        return myn;
    }

    public static String jsFunction_isContextExist(Context cx, Scriptable thisObj,
                                                   Object[] args, Function funObj) throws ScriptException {
        Boolean contextExist = false;
        String context = (String) args[0];
        if (context != null) {
            APIProvider apiProvider = getAPIProvider(thisObj);
            try {
                contextExist = apiProvider.isContextExist(context);
            } catch (APIManagementException e) {
                log.error("Error from registry while checking the input context is already exist", e);
            }
        } else {
            throw new ScriptException("Input context value is null");
        }
        return contextExist.toString();
    }

    private static DocumentationType getDocType(String docType) {
        DocumentationType docsType = null;
        for (DocumentationType type : DocumentationType.values()) {
            if (type.getType().equalsIgnoreCase(docType)) {
                docsType = type;
            }
        }
        return docsType;
    }

    private static boolean isStringValues(Object[] args) {
        int i = 0;
        for (Object arg : args) {
            //	log.info("i "+i +" "+args[i]);

            if (!(arg instanceof String)) {
                //  	log.info("fasle i "+i);
                return false;

            }
            i++;
        }
        return true;
    }

    private static String checkValue(String input) {
        return input != null ? input : "";
    }


    private static APIStatus getApiStatus(String status) {
        APIStatus apiStatus = null;
        for (APIStatus aStatus : APIStatus.values()) {
            if (aStatus.getStatus().equalsIgnoreCase(status)) {
                apiStatus = aStatus;
            }

        }
        return apiStatus;
    }

    public static NativeArray jsFunction_getProviderAPIVersionUsage(String providerName, String APIname, String serverURL)
            throws ScriptException {
        List<APIVersionUsageDTO> list = null;
        try {
            APIUsageStatisticsClient client = new APIUsageStatisticsClient();
            list = client.getUsageByAPIVersions(providerName, APIname);
        } catch (APIMgtUsageQueryServiceClientException e) {
            log.error("Error while invoking APIUsageStatisticsClient for ProviderAPIVersionUsage", e);
        }
        NativeArray myn = new NativeArray(0);
        Iterator it = null;
        if (list != null) {
            it = list.iterator();
        }
        int i = 0;
        if (it != null) {
            while (it.hasNext()) {
                NativeObject row = new NativeObject();
                Object usageObject = it.next();
                APIVersionUsageDTO usage = (APIVersionUsageDTO) usageObject;
                row.put("version", row, usage.getVersion());
                row.put("count", row, usage.getCount());
                myn.put(i, myn, row);
                i++;
            }
        }
        return myn;
    }

    public static NativeArray jsFunction_getProviderAPIUsage(String providerName, String serverURL) throws ScriptException {
        List<APIUsageDTO> list = null;
        try {
            APIUsageStatisticsClient client = new APIUsageStatisticsClient();
            list = client.getUsageByAPIs(providerName);
        } catch (APIMgtUsageQueryServiceClientException e) {
            log.error("Error while invoking APIUsageStatisticsClient for ProviderAPIUsage", e);
        }
        NativeArray myn = new NativeArray(0);
        Iterator it = null;
        if (list != null) {
            it = list.iterator();
        }
        int i = 0;
        if (it != null) {
            while (it.hasNext()) {
                NativeObject row = new NativeObject();
                Object usageObject = it.next();
                APIUsageDTO usage = (APIUsageDTO) usageObject;
                row.put("apiName", row, usage.getApiName());
                row.put("count", row, usage.getCount());
                myn.put(i, myn, row);
                i++;

            }
        }
        return myn;
    }

    public static NativeArray jsFunction_getProviderAPIUserUsage(String providerName, String apiName, String serverURL) throws ScriptException {
        List<PerUserAPIUsageDTO> list = null;
        try {
            APIUsageStatisticsClient client = new APIUsageStatisticsClient();
            list = client.getUsageBySubscribers(providerName, apiName, 10);
        } catch (APIMgtUsageQueryServiceClientException e) {
            log.error("Error while invoking APIUsageStatisticsClient for ProviderAPIUserUsage", e);
        }
        NativeArray myn = new NativeArray(0);
        Iterator it = null;
        if (list != null) {
            it = list.iterator();
        }
        int i = 0;
        if (it != null) {
            while (it.hasNext()) {
                NativeObject row = new NativeObject();
                Object usageObject = it.next();
                PerUserAPIUsageDTO usage = (PerUserAPIUsageDTO) usageObject;
                row.put("user", row, usage.getUsername());
                row.put("count", row, usage.getCount());
                myn.put(i, myn, row);
                i++;
            }
        }
        return myn;
    }

    public static NativeArray jsFunction_getProviderAPIVersionUserUsage(String providerName, String apiName, 
                                                                        String version, String serverURL) throws ScriptException {
        List<PerUserAPIUsageDTO> list = null;
        try {
            APIUsageStatisticsClient client = new APIUsageStatisticsClient();
            list = client.getUsageBySubscribers(providerName, apiName, version, 10);
        } catch (APIMgtUsageQueryServiceClientException e) {
            log.error("Error while invoking APIUsageStatisticsClient for ProviderAPIUserUsage", e);
        }
        NativeArray myn = new NativeArray(0);
        Iterator it = null;
        if (list != null) {
            it = list.iterator();
        }
        int i = 0;
        if (it != null) {
            while (it.hasNext()) {
                NativeObject row = new NativeObject();
                Object usageObject = it.next();
                PerUserAPIUsageDTO usage = (PerUserAPIUsageDTO) usageObject;
                row.put("user", row, usage.getUsername());
                row.put("count", row, usage.getCount());
                myn.put(i, myn, row);
                i++;
            }
        }
        return myn;
    }

    public static NativeArray jsFunction_getProviderAPIVersionUserLastAccess(String providerName,String serverURL) throws ScriptException {
        List<APIVersionLastAccessTimeDTO> list = null;
        try {
            APIUsageStatisticsClient client = new APIUsageStatisticsClient();
            list = client.getLastAccessTimesByAPI(providerName);
        } catch (APIMgtUsageQueryServiceClientException e) {
            log.error("Error while invoking APIUsageStatisticsClient for ProviderAPIVersionLastAccess", e);
        }
        NativeArray myn = new NativeArray(0);
        Iterator it = null;
        if (list != null) {
            it = list.iterator();
        }
        int i = 0;
        if (it != null) {
            while (it.hasNext()) {
                NativeObject row = new NativeObject();
                Object usageObject = it.next();
                APIVersionLastAccessTimeDTO usage = (APIVersionLastAccessTimeDTO) usageObject;
                row.put("api_name", row, usage.getApiName());
                row.put("api_version", row, usage.getApiVersion());
                row.put("user", row, usage.getUser());
                row.put("lastAccess", row, usage.getLastAccessTime());
                myn.put(i, myn, row);
                i++;
            }
        }
        return myn;
    }

    public static NativeArray jsFunction_getProviderAPIServiceTime(String providerName,String serverURL) throws ScriptException {
        List<APIResponseTimeDTO> list = null;
        try {
            APIUsageStatisticsClient client = new APIUsageStatisticsClient();
            list = client.getResponseTimesByAPIs(providerName);
        } catch (APIMgtUsageQueryServiceClientException e) {
            log.error("Error while invoking APIUsageStatisticsClient for ProviderAPIServiceTime", e);
        }
        NativeArray myn = new NativeArray(0);
        Iterator it = null;
        if (list != null) {
            it = list.iterator();
        }
        int i = 0;
        if (it != null) {
            while (it.hasNext()) {
                NativeObject row = new NativeObject();
                Object usageObject = it.next();
                APIResponseTimeDTO usage = (APIResponseTimeDTO) usageObject;
                row.put("apiName", row, usage.getApiName());
                row.put("serviceTime", row, usage.getServiceTime());
                myn.put(i, myn, row);
                i++;
            }
        }
        return myn;
    }

    public static String jsFunction_getThumb(Context cx, Scriptable thisObj,
                                             Object[] args,
                                             Function funObj) {
        String thumb = null;
        if (args.length == 1 && isStringValues(args)) {
            String providerName = args[0].toString();
            String apiName = args[1].toString();
            String version = args[2].toString();
            String docName = args[3].toString();
            String docType = args[4].toString();

            APIIdentifier apiId = new APIIdentifier(providerName, apiName, version);
            APIProvider apiProvider = getAPIProvider(thisObj);
            try {
                InputStream in = apiProvider.getIcon(apiId);
                if (in != null) {
                    thumb = IOUtils.toString(in);
                }                
            } catch (APIManagementException e) {
                log.error(e.getMessage(), e);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return thumb;

    }

    public static NativeArray jsFunction_searchAllAPIs(Context cx, Scriptable thisObj,
                                                    Object[] args,
                                                    Function funObj) throws ScriptException {
        NativeArray myn = new NativeArray(0);

        if (args.length == 0) {
            throw new ScriptException("Invalid number of parameters.");
        }
        String apiName = (String) args[0];

        APIProvider apiProvider = getAPIProvider(thisObj);
        try {
            //TODO : this regex pattern matching has to be moved to APIManager API implementation
            List<API> apiList = apiProvider.getAllAPIs();
            List<API> searchedList = new ArrayList<API>();
            String regex = "[a-zA-Z0-9_.-|]*" + apiName.toUpperCase()+ "[a-zA-Z0-9_.-|]*";
            Pattern pattern;
            Matcher matcher;
            for (API api : apiList) {
                APIIdentifier apiIdentifier = api.getId();
                String name = apiIdentifier.getApiName().toUpperCase();
                pattern = Pattern.compile(regex);
                matcher = pattern.matcher(name);
                if (matcher.matches()) {
                    searchedList.add(apiProvider.getAPI(apiIdentifier));
                }

            }
            Collections.sort(searchedList, new APINameComparator());

            Iterator it = searchedList.iterator();
            int i = 0;
            while (it.hasNext()) {

                NativeObject row = new NativeObject();
                Object apiObject = it.next();
                API api = (API) apiObject;
                APIIdentifier apiIdentifier = api.getId();
                row.put("apiName", row, apiIdentifier.getApiName());
                row.put("provider", row, apiIdentifier.getProviderName());
                row.put("version", row, apiIdentifier.getVersion());
                row.put("status", row, checkValue(api.getStatus().toString()));
                row.put("thumb", row, api.getThumbnailUrl());
                row.put("subs", row, apiProvider.getSubscribersOfAPI(api.getId()).size());
                myn.put(i, myn, row);
                i++;

            }
        } catch (APIManagementException e) {
            log.error("Error from registry while getting the APIs information for the searched API: " + apiName, e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        
        return myn;
    }

    public static NativeArray jsFunction_searchAPIs(Context cx, Scriptable thisObj,
                                                    Object[] args,
                                                    Function funObj) throws ScriptException {
        NativeArray myn = new NativeArray(0);

        if (args.length == 0) {
            throw new ScriptException("Invalid number of parameters.");
        }
        String providerName = (String) args[0];
        String apiName = (String) args[1];

        if (providerName != null) {
            APIProvider apiProvider = getAPIProvider(thisObj);
            try {
                //TODO : this regex pattern matching has to be moved to APIManager API implementation
                List<API> apiList = apiProvider.getAPIsByProvider(providerName);
                List<API> searchedList = new ArrayList<API>();
                String regex = "[a-zA-Z0-9_.-|]*" + apiName.toUpperCase()+ "[a-zA-Z0-9_.-|]*";
                Pattern pattern;
                Matcher matcher;
                for (API api : apiList) {
                    APIIdentifier apiIdentifier = api.getId();
                    String name = apiIdentifier.getApiName().toUpperCase();
                    pattern = Pattern.compile(regex);
                    matcher = pattern.matcher(name);
                    if (matcher.matches()) {
                        searchedList.add(apiProvider.getAPI(apiIdentifier));
                    }

                }
                Collections.sort(searchedList, new APINameComparator());

                Iterator it = searchedList.iterator();
                int i = 0;
                while (it.hasNext()) {

                    NativeObject row = new NativeObject();
                    Object apiObject = it.next();
                    API api = (API) apiObject;
                    APIIdentifier apiIdentifier = api.getId();
                    row.put("apiName", row, apiIdentifier.getApiName());
                    row.put("version", row, apiIdentifier.getVersion());
                    row.put("status", row, checkValue(api.getStatus().toString()));
                    row.put("thumb", row, api.getThumbnailUrl());
                    row.put("subs", row, apiProvider.getSubscribersOfAPI(api.getId()).size());
                    myn.put(i, myn, row);
                    i++;

                }
            } catch (APIManagementException e) {
                log.error("Error from registry while getting the APIs information for the searched API: " + apiName, e);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        return myn;
    }
}





