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

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
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
import org.wso2.carbon.apimgt.hostobjects.utils.APIHostObjectUtil;
import org.wso2.carbon.apimgt.impl.APIConsumerImpl;
import org.wso2.carbon.apimgt.impl.APIManagerImpl;
import org.wso2.carbon.apimgt.impl.APIProviderImpl;
import org.wso2.carbon.apimgt.usage.client.APIMgtUsageQueryServiceClient;
import org.wso2.carbon.apimgt.usage.client.dto.ProviderAPIUsageDTO;
import org.wso2.carbon.apimgt.usage.client.dto.ProviderAPIServiceTimeDTO;
import org.wso2.carbon.apimgt.usage.client.dto.ProviderAPIUserUsageDTO;
import org.wso2.carbon.apimgt.usage.client.dto.ProviderAPIVersionUsageDTO;
import org.wso2.carbon.apimgt.usage.client.dto.ProviderAPIVersionUserLastAccessDTO;
import org.wso2.carbon.apimgt.usage.client.dto.ProviderAPIVersionUserUsageDTO;
import org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException;
import org.wso2.carbon.hostobjects.web.RequestHostObject;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.rest.api.ui.client.AuthAdminServiceClient;
import org.wso2.carbon.rest.api.ui.client.RestAPITemplateClient;
import org.wso2.carbon.rest.api.ui.client.template.APITemplateBuilder;
import org.wso2.carbon.rest.api.ui.client.template.impl.BasicTemplateBuilder;
import org.wso2.carbon.scriptengine.exceptions.ScriptException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class APIProviderHostObject extends ScriptableObject {
    private static final Log log = LogFactory.getLog(APIProviderHostObject.class);
    private static APIHostObjectUtil hostObjectUtil = APIHostObjectUtil.getApiHostObjectUtils();

    public String getClassName() {
        return "APIProvider";
    }

    // The zero-argument constructor used for create instances for runtime
    public APIProviderHostObject() {
    }

    // Method jsConstructor defines the JavaScript constructor
    public void jsConstructor() {
    }


    static boolean logStatus = false;
    static APIManagerImpl apiManagerImpl;
    static APIProviderImpl apiProviderImpl;
    static APIConsumerImpl apiConsumerImpl;

    public static boolean jsFunction_login(Context cx, Scriptable thisObj,
                                           Object[] args,
                                           Function funObj)
            throws ScriptException {
        if (args.length == 2 && isStringValues(args)) {
            String userName = (String) args[0];
            String password = (String) args[1];
            if (!logStatus) {
                try {
                    apiManagerImpl = hostObjectUtil.getApiManager();
                    apiProviderImpl = hostObjectUtil.getApiProvider();
                    apiConsumerImpl = hostObjectUtil.getApiConsumer();
                } catch (APIManagementException e) {
                    log.error("Error from registry while while login the user", e);
                }
                logStatus = true;
            }
        }
        return logStatus;
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
        String thumbUrl = (String) apiData.get("imageUrl", apiData);
        String contextVal = (String) apiData.get("context", apiData);
        String context = contextVal.startsWith("/") ? contextVal : ("/" + contextVal);

        HttpServletRequest req = ((RequestHostObject) apiData.get("request", apiData)).getHttpServletRequest();
        NativeArray uriTemplateArr = (NativeArray) apiData.get("uriTemplateArr", apiData);


        APIIdentifier apiId = new APIIdentifier(provider, name, version);
        try {
            boolean apiExist = apiManagerImpl.isAPIAvailable(apiId);
            if (apiExist) {
                throw new ScriptException("Failed saving the new API due to an API already exists " +
                                          " with same name: " + name + " and version: "
                                          + version + " for the provider: " + provider);
            }
            API api = new API(apiId);

            NativeArray uriMethodArr = (NativeArray) apiData.get("uriMethodArr", apiData);
            ;

            if (uriTemplateArr.getLength() == uriMethodArr.getLength()) {
                Set<URITemplate> uriTemplates = new HashSet<URITemplate>();

                for (int i = 0; i < uriTemplateArr.getLength(); i++) {
                    URITemplate utemp = new URITemplate();
                    utemp.setUriTemplate((String) uriTemplateArr.get(i, uriTemplateArr));
                    utemp.setMethod((String) uriMethodArr.get(i, uriMethodArr));
                    uriTemplates.add(utemp);
                }

                api.setUriTemplates(uriTemplates);
            }

            api.setThumbnailUrl(thumbUrl);
            api.setDescription(description);
            api.setWsdlUrl(wsdl);
            api.setLastUpdated(new Date());
            api.setUrl(endpoint);
            api.addTags(tag);
            Set<Tier> availableTier = new HashSet<Tier>();
            availableTier.add(new Tier(tier));
            api.addAvailableTiers(availableTier);
            api.setStatus(APIStatus.CREATED);
            api.setContext(context);

            apiProviderImpl.addAPI(api);
            success = true;
            FileItem fi = getThumbFile(req);

            if (fi != null) {
                api.setThumbnailUrl(apiManagerImpl.addApiThumb(api, fi));
                apiProviderImpl.updateAPI(api);
            }

        } catch (APIManagementException e) {
            log.error("Error from registry while adding the API: " + name + "-" + version, e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return success;

    }


    private static FileItem getThumbFile(HttpServletRequest request)
            throws IOException, FileUploadException {
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (!isMultipart) {
            return null;
        }
        FileItemFactory factory = new DiskFileItemFactory();

        ServletFileUpload upload = new ServletFileUpload(factory);

        List items = upload.parseRequest(request);

        Iterator iter = items.iterator();
        while (iter.hasNext()) {
            FileItem item = (FileItem) iter.next();

            if (item.isFormField()) {

            } else {
                return ((FileItem) item);
            }
        }
        return null;
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
        String imageUrl = (String) apiData.get("imageUrl", apiData);
        String endpoint = (String) apiData.get("endpoint", apiData);
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
        try {
            API oldapi = apiManagerImpl.getAPI(oldApiId);
            HttpServletRequest req = ((RequestHostObject) apiData.get("request", apiData)).getHttpServletRequest();

            String tier = (String) apiData.get("tier", apiData);
            String status = ((String) apiData.get("status", apiData)).toUpperCase();
            String context = (String) apiData.get("context", apiData);

            APIIdentifier apiId = new APIIdentifier(provider, name, version);
            API api = new API(apiId);

            NativeArray uriTemplateArr = (NativeArray) apiData.get("uriTemplateArr", apiData);
            NativeArray uriMethodArr = (NativeArray) apiData.get("uriMethodArr", apiData);

            if (uriTemplateArr.getLength() == uriMethodArr.getLength()) {
                Set<URITemplate> uriTemplates = new HashSet<URITemplate>();

                for (int i = 0; i < uriTemplateArr.getLength(); i++) {
                    URITemplate utemp = new URITemplate();
                    String templateVal = (String) uriTemplateArr.get(i, uriTemplateArr);
                    String template = templateVal.startsWith("/") ? templateVal : ("/" + templateVal);
                    utemp.setUriTemplate(template);
                    utemp.setMethod((String) uriMethodArr.get(i, uriMethodArr));
                    utemp.setResourceURI(endpoint);

                    uriTemplates.add(utemp);
                }


                if (oldapi.getStatus().equals(APIStatus.CREATED)) {
                    createSynapseDef(uriTemplates, context, name, version, status);
                } else {
                    updateSynapseDef(uriTemplates, context, name, version, status);
                }

                api.setUriTemplates(uriTemplates);
            }

            api.setDescription(description);
            if (imageUrl == null) {
                imageUrl = oldapi.getThumbnailUrl();
            }
            api.setThumbnailUrl(imageUrl);
            api.setLastUpdated(new Date());
            api.setUrl(endpoint);
            api.addTags(tag);
            api.setContext(context);
            Set<Tier> availableTier = new HashSet<Tier>();
            availableTier.add(new Tier(tier));
            api.addAvailableTiers(availableTier);
            api.setStatus(getApiStatus(status));
            api.setWsdlUrl(wsdl);
            api.setLastUpdated(new Date());

            apiProviderImpl.updateAPI(api);

            FileItem fi = getThumbFile(req);

            if (fi != null) {
                api.setThumbnailUrl(apiManagerImpl.addApiThumb(api, fi));
                apiProviderImpl.updateAPI(api);
            }
            success = true;

        } catch (APIManagementException e) {
            log.error("Error from registry while updating the API :" + name + "-" + version, e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return success;
    }

    private static void createSynapseDef(Set<URITemplate> uriTemplates, String context, String name,
                                         String version, String status) throws Exception {
        if (status.equals("PUBLISHED")) {
            List<URITemplate> resourceData = new ArrayList<URITemplate>(uriTemplates);
            addRestClient(name, version, context, resourceData);
        }
    }

    private static void updateSynapseDef(Set<URITemplate> uriTemplates, String context, String name,
                                         String version, String status) throws Exception {
        if (status.equals("PUBLISHED")) {
            List<URITemplate> resourceData = new ArrayList<URITemplate>(uriTemplates);
            updateRestClient(name, version, context, resourceData);
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
        try {
            API api = apiManagerImpl.getAPI(apiId);

            Set<Subscriber> subs = apiProviderImpl.getSubscribersOfAPI(apiId);

            Set<URITemplate> uritemplates = api.getUriTemplates();

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
            myn.put(6, myn, checkValue(api.getAvailableTiers().iterator().next().getName()));
            myn.put(7, myn, checkValue(api.getStatus().toString()));
            myn.put(8, myn, api.getThumbnailUrl());
            myn.put(9, myn, api.getContext());
            myn.put(10, myn, checkValue(api.getLastUpdated().toString()));
            myn.put(11, myn, subs.size());

            if (uritemplates.size() != 0) {
                NativeArray uriTempArr = new NativeArray(uritemplates.size());
                Iterator i = uritemplates.iterator();

                ArrayList uriTemplatesArr = new ArrayList();

                while (i.hasNext()) {
                    ArrayList utArr = new ArrayList();
                    URITemplate ut = (URITemplate) i.next();
                    utArr.add(ut.getUriTemplate());
                    utArr.add(ut.getMethod());

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
            }
        } catch (APIManagementException e) {
            log.error("Error from registry while getting API information for the api: " + apiName + "-" + version, e);
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
            try {
                List<API> apiList = apiProviderImpl.getAPIsByProvider(providerName);
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
                    row.put("subs", row, apiProviderImpl.getSubscribersOfAPI(api.getId()).size());

                    myn.put(i, myn, row);
                    i++;

                }
            } catch (APIManagementException e) {
                log.error("Error from registry while getting all the APIs information for the provider: " + providerName, e);
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
        try {
            if (args.length != 1 || !isStringValues(args)) {
                throw new ScriptException("Invalid number of parameters or their types.");
            }
            userName = (String) args[0];
            Subscriber subscriber = new Subscriber(userName);
            Set<API> apiSet = apiConsumerImpl.getSubscriberAPIs(subscriber);
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
            log.error("Error from registry while getting the subscribed APIs information for the subscriber" + userName, e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return myn;


    }

    public static NativeArray jsFunction_getAllAPIUsageByProvider(Context cx, Scriptable thisObj,
                                                                  Object[] args,
                                                                  Function funObj)
            throws ScriptException {

        NativeArray myn = new NativeArray(0);
        String providerName = null;
        try {
            if (args.length == 0) {
                throw new ScriptException("Invalid number of input parameters.");
            }
            providerName = (String) args[0];
            if (providerName != null) {
                UserApplicationAPIUsage[] apiUsages = apiProviderImpl.getAllAPIUsageByProvider(providerName);
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
                                                             Object[] args, Function funObj)
            throws ScriptException {
        String apiName = null;
        String version = null;
        String providerName;
        NativeArray myn = new NativeArray(0);
        try {
            if (args.length != 3 || !isStringValues(args)) {
                throw new ScriptException("Invalid number of parameters or their types.");
            }
            providerName = args[0].toString();
            apiName = args[1].toString();
            version = args[2].toString();
            APIIdentifier apiId = new APIIdentifier(providerName, apiName, version);

            List<Documentation> docsList = apiManagerImpl.getAllDocumentation(apiId);
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
            log.error("Error from registry while getting document information for the api: " + apiName + "-" + version, e);
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
        APIIdentifier apiId = new APIIdentifier(providerName, apiName,
                                                version);
        try {
            content = apiManagerImpl.getDocumentationContent(apiId, docName);
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


        /*
        * } catch (APIManagementException e) { log.error(
        * "Error from Registry API while getting Inline Document Content ",
        * e); return null;
        */

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
        APIIdentifier apiId = new APIIdentifier(providerName, apiName,
                                                version);
        apiProviderImpl.addDocumentationContent(apiId, docName, docContent);


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
        try {
            apiProviderImpl.addDocumentation(apiId, doc);
            success = true;
        } catch (APIManagementException e) {
            log.error("Error from registry while adding the document :" + docName + "for the api :" + apiName + "-" + version, e);
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

        try {
            apiProviderImpl.removeDocumentation(apiId, docName, docType);
            success = true;
        } catch (APIManagementException e) {
            log.error("Error from registry while removing the document :" + docName + "for the api :" + apiName + "-" + version, e);
        }
        return success;

    }

    public static boolean jsFunction_createNewAPIVersion(Context cx, Scriptable thisObj,
                                                         Object[] args, Function funObj)
            throws ScriptException

    {
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
        try {
            apiProviderImpl.createNewAPIVersion(api, newVersion);
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
        try {
            subscribers = apiProviderImpl.getSubscribersOfAPI(apiId);
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
            log.error("Error from registry while getting subscribers for the API: " + apiName + "-" + version, e);
        }
        return myn;
    }

    public static String jsFunction_isContextExist(Context cx, Scriptable thisObj,
                                                   Object[] args, Function funObj)
            throws ScriptException {
        Boolean contextExist = false;
        String context = (String) args[0];
        if (context != null) {
            try {
                contextExist = apiManagerImpl.isContextExist(context);
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
        List<ProviderAPIVersionUsageDTO> list = null;
        try {
            APIMgtUsageQueryServiceClient client = new APIMgtUsageQueryServiceClient(serverURL);
            list = client.getProviderAPIVersionUsage(providerName,APIname);
        } catch (APIMgtUsageQueryServiceClientException e) {
            log.error("Error while invoking APIMgtUsageQueryServiceClient for ProviderAPIVersionUsage", e);
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
                ProviderAPIVersionUsageDTO usage = (ProviderAPIVersionUsageDTO) usageObject;
                row.put("version", row, usage.getVersion());
                row.put("count", row, usage.getCount());
                myn.put(i, myn, row);
                i++;

            }
        }
        return myn;
    }

    public static NativeArray jsFunction_getProviderAPIVersionUserUsage(String providerName, String APIname, String serverURL)
            throws ScriptException {
        List<ProviderAPIVersionUserUsageDTO> list = null;
        try {
            APIMgtUsageQueryServiceClient client = new APIMgtUsageQueryServiceClient(serverURL);
            list = client.getProviderAPIVersionUserUsage(providerName,APIname);
        } catch (APIMgtUsageQueryServiceClientException e) {
            log.error("Error while invoking APIMgtUsageQueryServiceClient for ProviderAPIVersionUserUsage", e);
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
                ProviderAPIVersionUserUsageDTO usage = (ProviderAPIVersionUserUsageDTO) usageObject;
                row.put("version", row, usage.getVersion());
                row.put("user", row, usage.getUser());
                row.put("count", row, usage.getCount());
                myn.put(i, myn, row);
                i++;

            }
        }
        return myn;
    }

    public static NativeArray jsFunction_getProviderAPIUsage(String providerName, String serverURL) throws ScriptException {
        List<ProviderAPIUsageDTO> list = null;
        try {
            APIMgtUsageQueryServiceClient client = new APIMgtUsageQueryServiceClient(serverURL);
            list = client.getProviderAPIUsage(providerName);
        } catch (APIMgtUsageQueryServiceClientException e) {
            log.error("Error while invoking APIMgtUsageQueryServiceClient for ProviderAPIUsage", e);
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
                ProviderAPIUsageDTO usage = (ProviderAPIUsageDTO) usageObject;
                row.put("apiName", row, usage.getApiName());
                row.put("count", row, usage.getCount());
                myn.put(i, myn, row);
                i++;

            }
        }
        return myn;
    }

    public static NativeArray jsFunction_getProviderAPIUserUsage(String providerName, String apiName, String serverURL) throws ScriptException {
        List<ProviderAPIUserUsageDTO> list = null;
        try {
            APIMgtUsageQueryServiceClient client = new APIMgtUsageQueryServiceClient(serverURL);
            list = client.getProviderAPIUserUsage(providerName, apiName);
        } catch (APIMgtUsageQueryServiceClientException e) {
            log.error("Error while invoking APIMgtUsageQueryServiceClient for ProviderAPIUserUsage", e);
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
                ProviderAPIUserUsageDTO usage = (ProviderAPIUserUsageDTO) usageObject;
                row.put("user", row, usage.getUser());
                row.put("count", row, usage.getCount());
                myn.put(i, myn, row);
                i++;

            }
        }
        return myn;
    }

    public static NativeArray jsFunction_getProviderAPIVersionUserLastAccess(String providerName,String serverURL) throws ScriptException {
        List<ProviderAPIVersionUserLastAccessDTO> list = null;
        try {
            APIMgtUsageQueryServiceClient client = new APIMgtUsageQueryServiceClient(serverURL);
            list = client.getProviderAPIVersionUserLastAccess(providerName);
        } catch (APIMgtUsageQueryServiceClientException e) {
            log.error("Error while invoking APIMgtUsageQueryServiceClient for ProviderAPIVersionLastAccess", e);
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
                ProviderAPIVersionUserLastAccessDTO usage = (ProviderAPIVersionUserLastAccessDTO) usageObject;
                row.put("api_version", row, usage.getApi_version());
                row.put("user", row, usage.getUser());
                row.put("lastAccess", row, usage.getLastAccess());
                myn.put(i, myn, row);
                i++;

            }
        }
        return myn;
    }

    public static NativeArray jsFunction_getProviderAPIServiceTime(String providerName,String serverURL) throws ScriptException {
        List<ProviderAPIServiceTimeDTO> list = null;
        try {
            APIMgtUsageQueryServiceClient client = new APIMgtUsageQueryServiceClient(serverURL);
            list = client.getProviderAPIServiceTime(providerName);
        } catch (APIMgtUsageQueryServiceClientException e) {
            log.error("Error while invoking APIMgtUsageQueryServiceClient for ProviderAPIServiceTime", e);
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
                ProviderAPIServiceTimeDTO usage = (ProviderAPIServiceTimeDTO) usageObject;
                row.put("apiName", row, usage.getApiName());
                row.put("serviceTime", row, usage.getServiceTime());
                myn.put(i, myn, row);
                i++;

            }
        }
        return myn;
    }

    public static void addRestClient(String apiName, String version, String context,
                                     List<URITemplate> resourceData) throws Exception {

        new AuthAdminServiceClient();
        String adminCookie = AuthAdminServiceClient.login(AuthAdminServiceClient.HOST_NAME,
                                                          AuthAdminServiceClient.USER_NAME,
                                                          AuthAdminServiceClient.PASSWORD);

        Map<String, String> testAPIMappings = new HashMap<String, String>();

        testAPIMappings.put(APITemplateBuilder.KEY_FOR_API_NAME, apiName);
        testAPIMappings.put(APITemplateBuilder.KEY_FOR_API_CONTEXT, context);
        testAPIMappings.put(APITemplateBuilder.KEY_FOR_API_VERSION, version);

        Iterator it = resourceData.iterator();
        List<Map<String, String>> resourceMappings = new ArrayList<Map<String, String>>();
        while (it.hasNext()) {
            Map uriTemplateMap = new HashMap();
            URITemplate temp = (URITemplate) it.next();
            uriTemplateMap.put(APITemplateBuilder.KEY_FOR_RESOURCE_URI_TEMPLATE, temp.getUriTemplate());
            uriTemplateMap.put(APITemplateBuilder.KEY_FOR_RESOURCE_METHODS, temp.getMethod());
            uriTemplateMap.put(APITemplateBuilder.KEY_FOR_RESOURCE_URI, temp.getResourceURI());
            resourceMappings.add(uriTemplateMap);
        }

        Map<String, String> testHandlerMappings_1 = new HashMap<String, String>();
        testHandlerMappings_1.put(APITemplateBuilder.KEY_FOR_HANDLER, "org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageHandler");

        Map<String, String> testHandlerMappings_2 = new HashMap<String, String>();
        testHandlerMappings_2.put(APITemplateBuilder.KEY_FOR_HANDLER, "org.wso2.carbon.apimgt.handlers.security.APIAuthenticationHandler");

        Map<String, String> testHandlerMappings_3 = new HashMap<String, String>();
        testHandlerMappings_3.put(APITemplateBuilder.KEY_FOR_HANDLER, "org.wso2.carbon.apimgt.handlers.throttling.APIThrottleHandler");
        testHandlerMappings_3.put(APITemplateBuilder.KEY_FOR_HANDLER_POLICY_KEY, "conf:/basic-throttle-policy.xml");

        List<Map<String, String>> handlerMappings = new ArrayList<Map<String, String>>();
        handlerMappings.add(testHandlerMappings_1);
        handlerMappings.add(testHandlerMappings_2);
        handlerMappings.add(testHandlerMappings_3);
        RestAPITemplateClient client = new RestAPITemplateClient(new BasicTemplateBuilder(
                testAPIMappings, resourceMappings, handlerMappings), adminCookie);
        client.addApi();
    }


    public static void updateRestClient(String apiName, String version, String context,
                                        List<URITemplate> resourceData) throws Exception {

        new AuthAdminServiceClient();
        String adminCookie = AuthAdminServiceClient.login(AuthAdminServiceClient.HOST_NAME,
                                                          AuthAdminServiceClient.USER_NAME,
                                                          AuthAdminServiceClient.PASSWORD);

        Map<String, String> testAPIMappings = new HashMap<String, String>();

        testAPIMappings.put(APITemplateBuilder.KEY_FOR_API_NAME, apiName);
        testAPIMappings.put(APITemplateBuilder.KEY_FOR_API_CONTEXT, context);
        testAPIMappings.put(APITemplateBuilder.KEY_FOR_API_VERSION, version);

        Iterator it = resourceData.iterator();
        List<Map<String, String>> resourceMappings = new ArrayList<Map<String, String>>();
        while (it.hasNext()) {
            Map uriTemplateMap = new HashMap();
            URITemplate temp = (URITemplate) it.next();
            uriTemplateMap.put(APITemplateBuilder.KEY_FOR_RESOURCE_URI_TEMPLATE, temp.getUriTemplate());
            uriTemplateMap.put(APITemplateBuilder.KEY_FOR_RESOURCE_METHODS, temp.getMethod());
            uriTemplateMap.put(APITemplateBuilder.KEY_FOR_RESOURCE_URI, temp.getResourceURI());
            resourceMappings.add(uriTemplateMap);
        }

        Map<String, String> testHandlerMappings_1 = new HashMap<String, String>();
        testHandlerMappings_1.put(APITemplateBuilder.KEY_FOR_HANDLER, "org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageHandler");

        Map<String, String> testHandlerMappings_2 = new HashMap<String, String>();
        testHandlerMappings_2.put(APITemplateBuilder.KEY_FOR_HANDLER, "org.wso2.carbon.apimgt.handlers.security.APIAuthenticationHandler");

        Map<String, String> testHandlerMappings_3 = new HashMap<String, String>();
        testHandlerMappings_3.put(APITemplateBuilder.KEY_FOR_HANDLER, "org.wso2.carbon.apimgt.handlers.throttling.APIThrottleHandler");
        testHandlerMappings_3.put(APITemplateBuilder.KEY_FOR_HANDLER_POLICY_KEY, "conf:/basic-throttle-policy.xml");

        List<Map<String, String>> handlerMappings = new ArrayList<Map<String, String>>();
        handlerMappings.add(testHandlerMappings_1);
        handlerMappings.add(testHandlerMappings_2);
        handlerMappings.add(testHandlerMappings_3);
        RestAPITemplateClient client = new RestAPITemplateClient(new BasicTemplateBuilder(
                testAPIMappings, resourceMappings, handlerMappings), adminCookie);
        client.updateApi();
    }

    public static String jsFunction_getThumb(Context cx, Scriptable thisObj,
                                             Object[] args,
                                             Function funObj) {
        String thumb = null;
        if (args.length == 1 && isStringValues(args)) {
            String thumbPath = args[0].toString();
            try {
                thumb = apiManagerImpl.getThumbAsString(thumbPath);
            } catch (RegistryException e) {
                log.error(e.getMessage(), e);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return thumb;

    }
}





