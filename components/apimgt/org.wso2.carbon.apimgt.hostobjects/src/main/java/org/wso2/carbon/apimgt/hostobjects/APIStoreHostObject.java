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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.*;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.hostobjects.internal.HostObjectComponent;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dto.xsd.APIInfoDTO;
import org.wso2.carbon.apimgt.keymgt.client.SubscriberKeyMgtClient;
import org.wso2.carbon.scriptengine.exceptions.ScriptException;
import org.wso2.carbon.scriptengine.util.HostObjectUtil;

import java.util.*;

@SuppressWarnings("unused")
public class APIStoreHostObject extends ScriptableObject {

	private static final long serialVersionUID = -3169012616750937045L;
	private static final Log log = LogFactory.getLog(APIStoreHostObject.class);
    private static final String hostObjectName = "APIStore";

    private APIConsumer apiConsumer;

    private String username;

    public String getUsername() {
        return username;
    }

    @Override
	public String getClassName() {
		return hostObjectName;	}

    // The zero-argument constructor used for create instances for runtime
    public APIStoreHostObject() throws APIManagementException {
        apiConsumer = APIManagerFactory.getInstance().getAPIConsumer();
    }

    public APIStoreHostObject(String loggedUser) throws APIManagementException {
        this.username = loggedUser;
        apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
    }

    public static Scriptable jsConstructor(Context cx, Object[] args, Function Obj,
                                           boolean inNewExpr)
            throws ScriptException, APIManagementException {

        int length = args.length;
        if (length == 1) {
            String username = (String) args[0];
            return new APIStoreHostObject(username);
        }
        return new APIStoreHostObject();
    }

    private static String getUsernameFromObject(Scriptable obj) {
        return ((APIStoreHostObject) obj).getUsername();
    }

    public APIConsumer getApiConsumer() {
        return apiConsumer;
    }

    private static APIConsumer getAPIConsumer(Scriptable thisObj) {
        return ((APIStoreHostObject) thisObj).getApiConsumer();
    }

    private static SubscriberKeyMgtClient getKeyManagementClient() throws APIManagementException {
        APIManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
        String url = config.getFirstProperty(APIConstants.API_KEY_MANAGER_URL);
        if (url == null) {
            throw new APIManagementException("API key manager URL unspecified");
        }

        String username = config.getFirstProperty(APIConstants.API_KEY_MANAGER_USERNAME);
        String password = config.getFirstProperty(APIConstants.API_KEY_MANAGER_PASSWORD);
        if (username == null || password == null) {
            throw new APIManagementException("Authentication credentials for API key manager " +
                    "unspecified");
        }

        try {
            return new SubscriberKeyMgtClient(url, username, password);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new APIManagementException(e);
        }
    }

	/*
	 * getting key for API subscriber args[] list String subscriberID, String
	 * api, String apiVersion, String Date
	 */
	public static String jsFunction_getKey(Context cx, Scriptable thisObj,
			Object[] args, Function funObj) throws ScriptException {
		int argsCount = args.length;
        String methodName = "getKey";
        if(argsCount != 7) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, methodName, argsCount, false);
        }
        if(!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, methodName, "1", "string", args[0], false);
        }
        if(!(args[1] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, methodName, "2", "string", args[1], false);
        }
        if(!(args[2] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, methodName, "3", "string", args[2], false);
        }
        if(!(args[3] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, methodName, "4", "string", args[3], false);
        }
        if(!(args[4] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, methodName, "5", "string", args[4], false);
        }
        if(!(args[5] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, methodName, "6", "string", args[5], false);
        }
        if(!(args[5] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, methodName, "7", "string", args[6], false);
        }
        APIInfoDTO apiInfo = new APIInfoDTO();
        apiInfo.setProviderId((String) args[0]);
        apiInfo.setApiName((String) args[1]);
        apiInfo.setVersion((String) args[2]);
        apiInfo.setContext((String) args[3]);
        try {
            SubscriberKeyMgtClient keyMgtClient = getKeyManagementClient();
            return keyMgtClient.getAccessKey((String) args[5], apiInfo, (String) args[4], (String) args[6]);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ScriptException(e);
        }
    }

	static boolean logStatus = false;

	public static String jsFunction_login(Context cx, Scriptable thisObj,
			Object[] args, Function funObj) throws ScriptException,
			APIManagementException {
        // TODO: Get rid of this
		return "" + logStatus;
	}

	public static String jsFunction_userLogin(Context cx, Scriptable thisObj,
			Object[] args, Function funObj) throws ScriptException,
			APIManagementException {
		String userName = "";
		String password = "";
		if (isStringArray(args)) {
			userName = args[0].toString();
			password = args[1].toString();
		}
        try {
            login(userName, password);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ScriptException(e);
        }
        return "" + logStatus;
	}

	public static boolean login(String userName, String password) throws APIManagementException {
        if (!logStatus) {
            logStatus = true;
        }
        return logStatus;
	}

	public static NativeArray jsFunction_getTopRatedAPIs1(Context cx,
			Scriptable thisObj, Object[] args, Function funObj)
			throws ScriptException {
		NativeArray myn = new NativeArray(0);
		String limitArg;
		int limit = 0;
		APIIdentifier[] serviceList = sampleData.giveAPIIdentifiers();
		if (isStringArray(args)) {
			limitArg = args[0].toString();
			limit = Integer.parseInt(limitArg);
		}

		for (int i = 0; i < limit; i++) {
			NativeObject row = new NativeObject();
			row.put("name", row, serviceList[i].getApiName());
			row.put("provider", row, serviceList[i].getProviderName());
			row.put("version", row, serviceList[i].getVersion());
			myn.put(i, myn, row);
		}
		return myn;
	}

	public static NativeArray jsFunction_getTopRatedAPIs(Context cx,
			Scriptable thisObj, Object[] args, Function funObj)
			throws ScriptException, APIManagementException {

		NativeArray myn = new NativeArray(0);
		if (isStringArray(args)) {
			String limitArg = args[0].toString();
			int limit = Integer.parseInt(limitArg);
			Set<API> apiSet;
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
			try {
				apiSet = apiConsumer.getTopRatedAPIs(limit);
			} catch (APIManagementException e) {
				log.error("Error from Registry API while getting Top Rated APIs Information "
						+ e);
				return myn;
			} catch (NullPointerException e) {
				log.error("Error from Registry API while getting Top Rated APIs Information, No APIs in Registry "
						+ e);
				return myn;
			} catch (Exception e) {
				log.error("Error while getting Top Rated APIs Information" + e);
				return myn;
			}
			Iterator it = apiSet.iterator();
			int i = 0;
			while (it.hasNext()) {
				NativeObject row = new NativeObject();
				Object apiObject = it.next();
				API api = (API) apiObject;
				APIIdentifier apiIdentifier = api.getId();
				row.put("name", row, apiIdentifier.getApiName());
				row.put("provider", row, apiIdentifier.getProviderName());
				row.put("version", row, apiIdentifier.getVersion());
				row.put("description", row, api.getDescription());
				row.put("rates", row, api.getRating());
				myn.put(i, myn, row);
				i++;
			}

		}// end of the if
		return myn;
	}

	public static NativeArray jsFunction_getRecentlyAddedAPIs(Context cx,
			Scriptable thisObj, Object[] args, Function funObj)
			throws ScriptException, APIManagementException {
		NativeArray apiArray = new NativeArray(0);
		if (isStringArray(args)) {
			String limitArg = args[0].toString();
			int limit = Integer.parseInt(limitArg);
			Set<API> apiSet;
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
			try {
				apiSet = apiConsumer.getRecentlyAddedAPIs(limit);
			} catch (APIManagementException e) {
				log.error("Error from Registry API while getting Recently Added APIs Information "
						+ e);
				return apiArray;
			} catch (NullPointerException e) {
				log.error("Error from Registry API while getting Recently Added APIs Information, No APIs in Registry "
						+ e);
				return apiArray;
			} catch (Exception e) {
				log.error("Error while getting Recently Added APIs Information"
						+ e);
				return apiArray;
			}

			Iterator it = apiSet.iterator();
			int i = 0;
            while (it.hasNext()) {
                NativeObject currentApi = new NativeObject();
                Object apiObject = it.next();
                API api = (API) apiObject;
                APIIdentifier apiIdentifier = api.getId();
                currentApi.put("name", currentApi, apiIdentifier.getApiName());
                currentApi.put("provider", currentApi,
                        apiIdentifier.getProviderName());
                currentApi.put("version", currentApi,
                        apiIdentifier.getVersion());
                currentApi.put("description", currentApi, api.getDescription());
                currentApi.put("rates", currentApi, api.getRating());
                if (api.getThumbnailUrl() == null) {
                    currentApi.put("thumbnailurl", currentApi, "images/api-default.png");
                } else {
                    currentApi.put("thumbnailurl", currentApi, api.getThumbnailUrl());
                }
                apiArray.put(i, apiArray, currentApi);
                i++;
            }

		}// end of the if
		return apiArray;
	}

	public static NativeArray jsFunction_searchAPI(Context cx,
			Scriptable thisObj, Object[] args, Function funObj)
			throws ScriptException, APIManagementException {
		NativeArray apiArray = new NativeArray(0);
		if (isStringArray(args)) {
			String searchTerm = args[0].toString();
			Set<API> apiSet;
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
			try {
				apiSet = apiConsumer.searchAPI(searchTerm);
			} catch (APIManagementException e) {
				log.error("Error from Registry API while getting SearchAPI Information "
						+ e);
				return apiArray;
			} catch (NullPointerException e) {
				log.error("Error from Registry API while getting SearchAPI Information, No APIs in Registry "
						+ e);
				return apiArray;
			} catch (Exception e) {
				log.error("Error while getting SearchAPI APIs Information" + e);
				return apiArray;
			}

			Iterator it = apiSet.iterator();
			int i = 0;
			while (it.hasNext()) {
				NativeObject currentApi = new NativeObject();
				Object apiObject = it.next();
				API api = (API) apiObject;
				APIIdentifier apiIdentifier = api.getId();
				currentApi.put("name", currentApi, apiIdentifier.getApiName());
				currentApi.put("provider", currentApi,
						apiIdentifier.getProviderName());
				currentApi.put("version", currentApi,
						apiIdentifier.getVersion());
				currentApi.put("description", currentApi, api.getDescription());
				currentApi.put("rates", currentApi, api.getRating());
				currentApi.put("description", currentApi, api.getDescription());
				currentApi.put("endpoint", currentApi, api.getUrl());
				if (api.getThumbnailUrl() == null) {
					currentApi.put("thumbnailurl", currentApi, "images/api-default.png");
				} else {
					currentApi.put("thumbnailurl", currentApi, api.getThumbnailUrl());
				}
				apiArray.put(i, apiArray, currentApi);
				i++;
			}

		}// end of the if
		return apiArray;
	}

	public static NativeArray jsFunction_searchAPIbyType(Context cx,
			Scriptable thisObj, Object[] args, Function funObj)
			throws ScriptException, APIManagementException {
		NativeArray apiArray = new NativeArray(0);
		if (isStringArray(args)) {
			String searchTerm = args[0].toString();
			String searchType = args[1].toString();
			Set<API> apiSet;
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
			try {
				apiSet = apiConsumer.searchAPI(searchTerm,searchType);
			} catch (APIManagementException e) {
				log.error("Error from Registry API while getting SearchAPI by type Information "
						+ e);
				return apiArray;
			} catch (NullPointerException e) {
				log.error("Error from Registry API while getting SearchAPI by type Information, No APIs in Registry "
						+ e);
				return apiArray;
			} catch (Exception e) {
				log.error("Error while getting SearchAPI APIs by type Information" + e);
				return apiArray;
			}

			Iterator it = apiSet.iterator();
			int i = 0;
			while (it.hasNext()) {

				NativeObject currentApi = new NativeObject();
				Object apiObject = it.next();
				API api = (API) apiObject;
				APIIdentifier apiIdentifier = api.getId();
				currentApi.put("name", currentApi, apiIdentifier.getApiName());
				currentApi.put("provider", currentApi,
						apiIdentifier.getProviderName());
				currentApi.put("version", currentApi,
						apiIdentifier.getVersion());
				currentApi.put("description", currentApi, api.getDescription());
				currentApi.put("rates", currentApi, api.getRating());
				currentApi.put("description", currentApi, api.getDescription());
				currentApi.put("endpoint", currentApi, api.getUrl());
				if (api.getThumbnailUrl() == null) {
					currentApi.put("thumbnailurl", currentApi, "images/api-default.png");
				} else {
					currentApi.put("thumbnailurl", currentApi, api.getThumbnailUrl());
				}
				apiArray.put(i, apiArray, currentApi);
				i++;
			}

		}// end of the if
		return apiArray;
	}

	public static NativeArray jsFunction_getAPIsWithTag(Context cx,
			Scriptable thisObj, Object[] args, Function funObj)
			throws ScriptException, APIManagementException {
		NativeArray apiArray = new NativeArray(0);
		if (isStringArray(args)) {
			String tagName = args[0].toString();
			Set<API> apiSet;
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
			try{
                apiSet = apiConsumer.getAPIsWithTag(tagName);
            } catch (APIManagementException e) {
                log.error("Error from Registry API while getting APIs With Tag Information "
                        + e);
                return apiArray;
            } catch (NullPointerException e) {
                log.error("Error from Registry API while getting APIs With Tag Information, No APIs in Registry "
                        + e);
                return apiArray;
            } catch (Exception e) {
                log.error("Error while getting APIs With Tag Information" + e);
                return apiArray;
            }

			Iterator it = apiSet.iterator();
			int i = 0;
			while (it.hasNext()) {
				NativeObject currentApi = new NativeObject();
				Object apiObject = it.next();
				API api = (API) apiObject;
				APIIdentifier apiIdentifier = api.getId();
				currentApi.put("name", currentApi, apiIdentifier.getApiName());
				currentApi.put("provider", currentApi,
						apiIdentifier.getProviderName());
				currentApi.put("version", currentApi,
						apiIdentifier.getVersion());
				currentApi.put("description", currentApi, api.getDescription());
				currentApi.put("rates", currentApi, api.getRating());
				if (api.getThumbnailUrl() == null) {
					currentApi.put("thumbnailurl", currentApi,
							"images/service-test-icon.png");
				} else {
					currentApi.put("thumbnailurl", currentApi,
							api.getThumbnailUrl());
				}
				apiArray.put(i, apiArray, currentApi);
				i++;
			}

		}// end of the if
		return apiArray;
	}

	public static NativeArray jsFunction_getSubscribedAPIs(Context cx,
			Scriptable thisObj, Object[] args, Function funObj)
			throws ScriptException, APIManagementException {
		NativeArray apiArray = new NativeArray(0);
		if (isStringArray(args)) {
			String limitArg = args[0].toString();
			int limit = Integer.parseInt(limitArg);
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            try {
                Set<API> apiSet = apiConsumer.getTopRatedAPIs(limit);
                Iterator it = apiSet.iterator();
                int i = 0;
                while (it.hasNext()) {
                    NativeObject currentApi = new NativeObject();
                    Object apiObject = it.next();
                    API api = (API) apiObject;
                    APIIdentifier apiIdentifier = api.getId();
                    currentApi.put("name", currentApi, apiIdentifier.getApiName());
                    currentApi.put("provider", currentApi,
                            apiIdentifier.getProviderName());
                    currentApi.put("version", currentApi,
                            apiIdentifier.getVersion());
                    currentApi.put("description", currentApi, api.getDescription());
                    currentApi.put("rates", currentApi, api.getRating());
                    apiArray.put(i, apiArray, currentApi);
                    i++;
                }
            } catch (APIManagementException e) {
                log.error("Error while getting API list", e);
                return apiArray;
            }
		}// end of the if
		return apiArray;
	}

	public static NativeArray jsFunction_getAllTags(Context cx,
			Scriptable thisObj, Object[] args, Function funObj)
			throws ScriptException, APIManagementException {
		NativeArray tagArray = new NativeArray(0);
		Set<Tag> tags;
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
		try{
		    tags = apiConsumer.getAllTags();
		} catch (APIManagementException e) {
			log.error("Error from Registry API while getting AllTags Information "
					+ e);
			return tagArray;
		} catch (NullPointerException e) {
			log.error("Error from Registry API while getting APIs All Tags Information, No APIs in Registry "
					+ e);
			return tagArray;
		} catch (Exception e) {
			log.error("Error while getting All Tags" + e);
			return tagArray;
		}

		Iterator tagsI = tags.iterator();
		int i = 0;
		while (tagsI.hasNext()) {

			NativeObject currentTag = new NativeObject();
			Object tagObject = tagsI.next();
			Tag tag = (Tag) tagObject;

			currentTag.put("name", currentTag, tag.getName());
			currentTag.put("count", currentTag, "1");

			tagArray.put(i, tagArray, currentTag);
			i++;
		}

		return tagArray;
	}

	public static NativeArray jsFunction_getAllPublishedAPIs(Context cx,
			Scriptable thisObj, Object[] args, Function funObj)
			throws ScriptException, APIManagementException {
		Set<API> apiSet;
		NativeArray myn = new NativeArray(0);
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
		try {
			apiSet = apiConsumer.getAllPublishedAPIs();
		} catch (APIManagementException e) {
			log.error("Error from Registry API while getting API Information"
					+ e);
			return myn;
		} catch (Exception e) {
			log.error("Error while getting API Information" + e);
			return myn;
		}

		Iterator it = apiSet.iterator();
		int i = 0;
		while (it.hasNext()) {
			NativeObject row = new NativeObject();
			Object apiObject = it.next();
			API api = (API) apiObject;
			APIIdentifier apiIdentifier = api.getId();
			row.put("name", row, apiIdentifier.getApiName());
			row.put("provider", row, apiIdentifier.getProviderName());
			row.put("version", row, apiIdentifier.getVersion());
			row.put("description", row, api.getDescription());
			row.put("rates", row, api.getRating());
			row.put("endpoint", row, api.getUrl());
			row.put("wsdl", row, "http://appserver/services/echo?wsdl");
			row.put("updatedDate", row, api.getLastUpdated().toString());
			row.put("tier", row, api.getAvailableTiers());
            row.put("context",row,api.getContext());
			row.put("status", row, "Deployed"); // api.getStatus().toString()
			if (api.getThumbnailUrl() == null) {
				row.put("thumbnailurl", row, "images/api-default.png");
			} else {
				row.put("thumbnailurl", row, api.getThumbnailUrl());
			}
			myn.put(i, myn, row);
			i++;
		}
		return myn;
	}

	public static NativeArray jsFunction_getAPI(Context cx, Scriptable thisObj,
			Object[] args, Function funObj) throws ScriptException,
			APIManagementException {

		String providerName;
		String apiName;
		String version;
        String username = null;
        boolean isSubscribed = false;
        String methodName = "getAPI";
        int argsCount = args.length;
        if(argsCount != 4) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, methodName, argsCount, false);
        }
        if(!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, methodName, "1", "string", args[0], false);
        }
        if(!(args[1] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, methodName, "2", "string", args[1], false);
        }
        if(!(args[2] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, methodName, "3", "string", args[2], false);
        }
        if (args[3] != null) {
            if (!(args[3] instanceof String)) {
                HostObjectUtil.invalidArgsError(hostObjectName, methodName, "4", "string", args[3], false);
            }
            username = (String) args[3];
        }
        providerName = (String) args[0];
	    apiName = (String) args[1];
		version = (String) args[2];
		APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
		NativeArray myn = new NativeArray(0);
		API api;
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        try {
            api = apiConsumer.getAPI(apiIdentifier);
            if (username != null) {
                //TODO @sumedha : remove hardcoded tenant Id            
                isSubscribed = apiConsumer.isSubscribed(apiIdentifier, username);
            }
        } catch (APIManagementException e) {
            log.error("Error from Registry API while getting get API Information on " + apiName
                    + e);
            return myn;
        } catch (NullPointerException e) {
            log.error("Error from Registry API while getting API information on " + apiName
                    + e);
            return myn;
        } catch (Exception e) {
            log.error("Error while getting API Information" + e);
            return myn;
        }

        NativeObject row = new NativeObject();
        apiIdentifier = api.getId();
        row.put("name", row, apiIdentifier.getApiName());
        row.put("provider", row, apiIdentifier.getProviderName());
        row.put("version", row, apiIdentifier.getVersion());
        row.put("description", row, api.getDescription());
        row.put("rates", row, api.getRating());
        row.put("endpoint", row, api.getUrl());
        row.put("wsdl", row, "http://appserver/services/echo?wsdl");
        row.put("updatedDate", row, api.getLastUpdated().toString());
        row.put("context",row, api.getContext());
        row.put("status", row, api.getStatus().getStatus());

        APIManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
        row.put("serverURL", row, config.getFirstProperty(APIConstants.API_GATEWAY_API_ENDPOINT));

        //TODO : need to pass in the full available tier list to front end
        Set<Tier> tiers = api.getAvailableTiers();
        if (tiers.size() > 0) {
            Tier tier = tiers.iterator().next();
            row.put("tier", row, tier.getName());
        }

        // row.put("status", row, "Deployed"); // api.getStatus().toString()
        // row.put("status", row, "Deployed"); // api.getStatus().toString()
        row.put("subscribed", row, isSubscribed);
        if (api.getThumbnailUrl() == null) {
            row.put("thumbnailurl", row, "images/api-default.png");
        } else {
            row.put("thumbnailurl", row, api.getThumbnailUrl());
        }
        myn.put(0, myn, row);

        return myn;
    }

	public static boolean jsFunction_isSubscribed(Context cx, Scriptable thisObj,
			Object[] args, Function funObj) throws ScriptException,
			APIManagementException {

        String username = null;
        String methodName = "isSubscribed";
        int argsCount = args.length;
        if(argsCount != 4) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, methodName, argsCount, false);
        }
        if(!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, methodName, "1", "string", args[0], false);
        }
        if(!(args[1] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, methodName, "2", "string", args[1], false);
        }
        if(!(args[2] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, methodName, "3", "string", args[2], false);
        }
        if (args[3] != null) {
            if (!(args[3] instanceof String)) {
                HostObjectUtil.invalidArgsError(hostObjectName, methodName, "4", "string", args[3], false);
            }
            username = (String) args[3];
        }

        String providerName = (String) args[0];
	    String apiName = (String) args[1];
		String version = (String) args[2];
		APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        return username != null && apiConsumer.isSubscribed(apiIdentifier, username);
    }

	public static NativeArray jsFunction_getAPIKey(Context cx, Scriptable thisObj,
			Object[] args, Function funObj) throws ScriptException,
			APIManagementException {

		String providerName = "";
		String apiName = "";
		String version = "";
		String apiContext = "";
		if (isStringArray(args)) {
			providerName = args[0].toString();
			apiName = args[1].toString();
			version = args[2].toString();
			apiContext = args[3].toString();
		}

		APIInfoDTO apiInfoDTO = new APIInfoDTO();
		apiInfoDTO.setApiName(apiName);
		apiInfoDTO.setContext(apiContext);
		apiInfoDTO.setProviderId(providerName);
		apiInfoDTO.setVersion(version);
		SubscriberKeyMgtClient subscriberKeyMgtClient;
		String key="";
		NativeArray myn = new NativeArray(0);
        NativeObject row = new NativeObject();
		myn.put(0, myn, row);
		return myn;
	}

	public static NativeArray jsFunction_getAllDocumentation(Context cx,
			Scriptable thisObj, Object[] args, Function funObj)
			throws ScriptException, APIManagementException {
		java.util.List<Documentation> doclist;
		String providerName = "";
		String apiName = "";
		String version = "";
		if (isStringArray(args)) {
			providerName = args[0].toString();
			apiName = args[1].toString();
			version = args[2].toString();
		}
		APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
		NativeArray myn = new NativeArray(0);
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
		try{
		    doclist = apiConsumer.getAllDocumentation(apiIdentifier);
		} catch (APIManagementException e) {
			log.error("Error from Registry API while getting All Documentation on"+ apiName
					+ e);
			return myn;
		} catch (NullPointerException e) {
			log.error("Error from Registry API while getting All Documentation on"+ apiName
					+ e);
			return myn;
		} catch (Exception e) {
			log.error("Error while getting All Documentation "+apiName  + e);
			return myn;
		}

		Iterator it = doclist.iterator();
		int i = 0;
		while (it.hasNext()) {
			NativeObject row = new NativeObject();
			Object docObject = it.next();
			Documentation documentation = (Documentation) docObject;
			Object objectSourceType= documentation.getSourceType();
         	String strSourceType = objectSourceType.toString();

			row.put("name", row, documentation.getName());
			row.put("sourceType", row, strSourceType);
			row.put("summary", row, documentation.getSummary());
			row.put("sourceUrl", row, documentation.getSourceUrl());
			DocumentationType documentationType = documentation.getType();
			row.put("type", row, documentationType.getType());
			myn.put(i, myn, row);
			i++;
		}
		return myn;
	}


	public static NativeArray jsFunction_getComments(Context cx,
			Scriptable thisObj, Object[] args, Function funObj)
			throws ScriptException, APIManagementException {
		Comment[] commentlist;
		String providerName = "";
		String apiName = "";
		String version = "";
		if (isStringArray(args)) {
			providerName = args[0].toString();
			apiName = args[1].toString();
			version = args[2].toString();
		}
		APIIdentifier apiIdentifyer = new APIIdentifier(providerName, apiName,
				version);
		NativeArray myn = new NativeArray(0);
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
		try {
			commentlist = apiConsumer.getComments(apiIdentifyer);
		} catch (APIManagementException e) {
			log.error("Error from Registry API while getting Comments for "+ apiName
					+ e);
			return myn;
		} catch (NullPointerException e) {
			log.error("Error from Registry API while getting Comments for "+ apiName
					+ e);
			return myn;
		} catch (Exception e) {
			log.error("Error while getting Comments for "+apiName  + e);
			return myn;
		}

		int i=0;
		for (Comment n: commentlist) {
			NativeObject row = new NativeObject();
			row.put("userName", row, n.getUser());
			row.put("comment", row, n.getText());
			row.put("createdTime", row, n.getCreatedTime().getTime());
			myn.put(i, myn, row);
			i++;
		}
		return myn;
	}

	public static NativeArray jsFunction_addComments(Context cx,
			Scriptable thisObj, Object[] args, Function funObj)
			throws ScriptException, APIManagementException {
		Comment[] commentlist;
		String providerName = "";
		String apiName = "";
		String version = "";
		String commentStr = "";
		if (isStringArray(args)) {
			providerName = args[0].toString();
			apiName = args[1].toString();
			version = args[2].toString();
			commentStr = args[3].toString();
		}
		APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
		NativeArray myn = new NativeArray(0);
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
		try {
			apiConsumer.addComment(apiIdentifier, commentStr, getUsernameFromObject(thisObj));
		} catch (APIManagementException e) {
			log.error("Error from Registry API while adding Comments for "+ apiName
					+ e);
			return myn;
		} catch (NullPointerException e) {
			log.error("Error from Registry API while adding Comments for "+ apiName
					+ e);
			return myn;
		} catch (Exception e) {
			log.error("Error while adding Comments for "+apiName  + e);
			return myn;
		}

		int i=0;
			NativeObject row = new NativeObject();
			row.put("userName", row, providerName);
			row.put("comment", row, commentStr);
			myn.put(i, myn, row);

		return myn;
	}


	public static NativeArray jsFunction_ListProviders(Context cx,
			Scriptable thisObj, Object[] args, Function funObj)
			throws ScriptException {
		NativeArray myn = new NativeArray(0);
		String[] providers = SampleData.providers;
		for (int i = 0; i < providers.length; i++) {
			myn.put(i, myn, providers[i]);
		}
		return myn;
	}

	public static NativeArray jsFunction_ListApplications(Context cx,
			Scriptable thisObj, Object[] args, Function funObj)
			throws ScriptException {
		NativeArray myn = new NativeArray(0);
		String[] application = SampleData.application;
		for (int i = 0; i < application.length; i++) {
			myn.put(i, myn, application[i]);
		}
		return myn;
	}

	public static NativeArray jsFunction_ListAPIServices()
			throws ScriptException {
		NativeArray myn = new NativeArray(0);

		Services[] serviceList = SampleData.listSerives;
		for (int i = 0; i < serviceList.length; i++) {

			NativeObject row = new NativeObject();
			Object o = serviceList[i].getName();
			row.put("name", row, o);
			row.put("rates", row, serviceList[i].getRating());
			row.put("author", row, serviceList[i].getAuthor());
			myn.put(i, myn, row);

		}
		return myn;
	}

	public static NativeArray jsFunction_ListProvidersNames()
			throws ScriptException {
		NativeArray providersN = new NativeArray(0);

		String[] providers = SampleData.providers;
		for (int i = 0; i < providers.length; i++) {
			Object o = providers[i];
			providersN.put(i, providersN, o);
		}
		return providersN;
	}

	// used for greg model data
	static SampleData sampleData = new SampleData();

	public static NativeArray jsFunction_giveAPIIdentifiers()
			throws ScriptException {
		NativeArray myn = new NativeArray(0);

		APIIdentifier[] serviceList = sampleData.giveAPIIdentifiers();
		for (int i = 0; i < serviceList.length; i++) {

			NativeObject row = new NativeObject();
			row.put("name", row, serviceList[i].getApiName());
			row.put("provider", row, serviceList[i].getProviderName());
			row.put("version", row, serviceList[i].getVersion());
			myn.put(i, myn, row);

		}
		return myn;
	}

	public static boolean jsFunction_addSubscription(Context cx,
			Scriptable thisObj, Object[] args, Function funObj) {
        if(!(args[0] instanceof String) ||
                !(args[1] instanceof String) ||
                !(args[2] instanceof String) ||
                !(args[3] instanceof String) ||
                (!(args[4] instanceof Double) && !(args[4] instanceof Integer) ||
                !(args[5] instanceof String))) {
            return false;
        }

        String providerName = args[0].toString();
        String apiName = args[1].toString();
        String version = args[2].toString();
        String tier = args[3].toString();
        int applicationId = ((Number) args[4]).intValue();
        String userId = args[5].toString();
		APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
        apiIdentifier.setTier(tier);

        APIConsumer apiConsumer = getAPIConsumer(thisObj);
		try {
			apiConsumer.addSubscription(apiIdentifier, userId, applicationId);
            return true;
		} catch (APIManagementException e) {
			e.printStackTrace();
            return false;
		}
	}

    public static boolean jsFunction_removeSubscriber(Context cx,
			Scriptable thisObj, Object[] args, Function funObj) {
		String providerName = "";
		String apiName = "";
		String version = "";
		String application = "";
		String userId = "";
		if (isStringArray(args)) {
			providerName = args[0].toString();
			apiName = args[1].toString();
			version = args[2].toString();
			application = (String) args[3];
			userId = args[4].toString();
		}
		APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
        apiIdentifier.setApplicationId(application);
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
		try {
			apiConsumer.removeSubscriber(apiIdentifier, userId);
            return true;
        } catch (APIManagementException e) {
            e.printStackTrace();
            return false;
        }
	}


	public static NativeArray jsFunction_rateAPI(Context cx,
			Scriptable thisObj, Object[] args, Function funObj)
			throws ScriptException, APIManagementException {

		NativeArray myn = new NativeArray(0);
		if (isStringArray(args)) {
			String providerName = args[0].toString();
			String apiName = args[1].toString();
			String version = args[2].toString();
			String rateStr = args[3].toString();
			int rate;
			try{
			rate = Integer.parseInt(rateStr.substring(0, 1));
			} catch (NumberFormatException e) {
				log.error("Rate must to be number " + rateStr+ e);
				return myn;
			}
			catch (Exception e) {
				log.error("Error from while Rating API " + rateStr+ e);
				return myn;
			}

            APIConsumer apiConsumer = getAPIConsumer(thisObj);
			try {
				APIIdentifier apiId = new APIIdentifier(providerName, apiName, version);
                String user = getUsernameFromObject(thisObj);
				switch (rate) {
				   case 1: {
					  apiConsumer.rateAPI(apiId, APIRating.RATING_ONE, user);
				      break;
				   }
				   case 2: {
                       apiConsumer.rateAPI(apiId, APIRating.RATING_TWO, user);
				      break;
				   }
				   case 3: {
                       apiConsumer.rateAPI(apiId, APIRating.RATING_THREE, user);
                      break;
                   }
				   case 4: {
                       apiConsumer.rateAPI(apiId, APIRating.RATING_FOUR, user);
                      break;
                   }
				   case 5: {
                       apiConsumer.rateAPI(apiId, APIRating.RATING_FIVE, user);
                      break;
                   }
				   default: {
				      throw new IllegalArgumentException("Can't handle " + rate);
				   }

			    }
			} catch (APIManagementException e) {
				log.error("Error from Registry API while Rating API " + apiName
						+ e);
				return myn;
			} catch (IllegalArgumentException e) {
				log.error("Error from Registry API while Rating API " + apiName
						+ e);
				return myn;
			} catch (NullPointerException e) {
				log.error("Error from Registry API while Rating API " + apiName
						+ e);
				return myn;
			} catch (Exception e) {
				log.error("Error while Rating API " + apiName+ e);
				return myn;
			}

            NativeObject row = new NativeObject();
            row.put("name", row, apiName);
            row.put("provider", row, providerName);
            row.put("version", row, version);
            row.put("rates", row, rateStr);
            myn.put(0, myn, row);

		}// end of the if
		return myn;
	}

	public static NativeArray jsFunction_getSubscribedAPIs()
			throws ScriptException {
		NativeArray purchases = new NativeArray(0);

		purchasedServices[] purchasedlist = SampleData.purchasedServiceList;
		for (int i = 0; i < purchasedlist.length; i++) {

			NativeObject row = new NativeObject();
			Object name = purchasedlist[i].getName();
			Object author = purchasedlist[i].getAuthor();
			Object rate = purchasedlist[i].getRating();
			Object canDel = purchasedlist[i].getCanDelete();
			Object descrp = purchasedlist[i].getDescription();
			Object namespace = purchasedlist[i].getNamespace();
			Object path = purchasedlist[i].getPath();
			Object purchased = purchasedlist[i].getPurchased();
			Object supportUrl = purchasedlist[i].getSupportForumURL();
			Object thumbUrl = purchasedlist[i].getThumbURL();
			Object version = purchasedlist[i].getVersion();
			row.put("name", row, name);
			row.put("path", row, path);
			row.put("author", row, author);
			row.put("purchased", row, purchased);
			row.put("description", row, descrp);
			row.put("supportForumURL", row, supportUrl);
			row.put("version", row, version);
			row.put("rating", row, rate);
			row.put("namespace", row, namespace);
			row.put("canDelete", row, canDel);
			row.put("thumbURL", row, thumbUrl);
			purchases.put(i, purchases, row);
			// return row;
		}
		return purchases;

	}

    public static NativeArray jsFunction_getSubscriptions(Context cx,
                                                          Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        NativeArray myn = new NativeArray(0);
        if (isStringArray(args)) {
            String providerName = args[0].toString();
            String apiName = args[1].toString();
            String version = args[2].toString();
            String user = args[3].toString();

            APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
            Subscriber subscriber = new Subscriber(user);
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            Set<SubscribedAPI> apis = apiConsumer.getSubscribedIdentifiers(subscriber, apiIdentifier);
            int i = 0;
            for(SubscribedAPI api : apis) {
                NativeObject row = new NativeObject();
                row.put("application", row, api.getApplication().getName());
                row.put("applicationId", row, api.getApplication().getId());
                row.put("prodKey", row, getKey(api, APIConstants.API_KEY_TYPE_PRODUCTION));
                row.put("sandboxKey", row, getKey(api, APIConstants.API_KEY_TYPE_SANDBOX));
                myn.put(i++, myn, row);
            }
        }
        return myn;
    }

    private static String getKey(SubscribedAPI api, String keyType) {
        List<APIKey> apiKeys = api.getKeys();
        for (APIKey key : apiKeys) {
            if (keyType.equals(key.getType())) {
                return key.getKey();
            }
        }
        return null;
    }

    public static NativeArray jsFunction_getAllSubscriptions(Context cx,
                                                          Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        if(args.length != 1 || !(args[0] instanceof String)) {
            return null;
        }
        String user = (String) args[0];
        Subscriber subscriber = new Subscriber(user);
        Map<Integer, NativeArray> subscriptionsMap = new HashMap<Integer, NativeArray>();
        NativeArray appsObj = new NativeArray(0);
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        Set<SubscribedAPI> subscribedAPIs = apiConsumer.getSubscribedAPIs(subscriber);
        int i = 0;
        for(SubscribedAPI subscribedAPI : subscribedAPIs) {
            NativeArray apisArray = subscriptionsMap.get(subscribedAPI.getApplication().getId());
            if(apisArray == null) {
                apisArray = new NativeArray(1);
                NativeObject appObj = new NativeObject();
                appObj.put("id", appObj, subscribedAPI.getApplication().getId());
                appObj.put("name", appObj, subscribedAPI.getApplication().getName());
                addAPIObj(subscribedAPI, apisArray, thisObj);
                appObj.put("subscriptions", appObj, apisArray);
                appsObj.put(i++, appsObj, appObj);
                //keep a subscriptions map in order to efficiently group appObj vice.
                subscriptionsMap.put(subscribedAPI.getApplication().getId(), apisArray);
            } else {
                addAPIObj(subscribedAPI, apisArray, thisObj);
            }
        }
        return appsObj;
    }

    private static void addAPIObj(SubscribedAPI subscribedAPI, NativeArray apisArray,
                                  Scriptable thisObj) throws ScriptException {
        NativeObject apiObj = new NativeObject();
        APIConsumer apiConsumer = getAPIConsumer(thisObj);
        try {
            API api = apiConsumer.getAPI(subscribedAPI.getApiId());
            apiObj.put("name", apiObj, subscribedAPI.getApiId().getApiName());
            apiObj.put("provider", apiObj, subscribedAPI.getApiId().getProviderName());
            apiObj.put("version", apiObj, subscribedAPI.getApiId().getVersion());
            apiObj.put("thumburl", apiObj, api.getThumbnailUrl());
            apiObj.put("context", apiObj, api.getContext());
            apiObj.put("prodKey", apiObj, getKey(subscribedAPI, APIConstants.API_KEY_TYPE_PRODUCTION));
            apiObj.put("sandboxKey", apiObj, getKey(subscribedAPI, APIConstants.API_KEY_TYPE_SANDBOX));
            apiObj.put("hasMultipleEndpoints", apiObj, String.valueOf(api.getSandboxUrl() != null));
            apisArray.put(apisArray.getIds().length, apisArray, apiObj);
        } catch (APIManagementException e) {
            throw new ScriptException(e);
        }
    }

    public static NativeObject jsFunction_getSubscriber(Context cx,
                                                        Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        if (isStringArray(args)) {
            NativeObject user = new NativeObject();
            String userName = args[0].toString();
            Subscriber subscriber;
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            try {
                subscriber = apiConsumer.getSubscriber(userName);
            }catch (APIManagementException e) {
				log.error("Error from Registry API while getting Subscriber"
						+ e);
				return null;
			} catch (IllegalArgumentException e) {
				log.error("Error from Registry API while getting Subscriber "
						+ e);
				return null;
			} catch (NullPointerException e) {
				log.error("Error from Registry API while getting Subscriber"
						+ e);
				return null;
			} catch (Exception e) {
				log.error("Error while getting Subscriber " + e);
				return null;
			}

            if (subscriber == null) {
                return null;
            }
            user.put("name", user, subscriber.getName());
            user.put("id", user, subscriber.getId());
            user.put("email", user, subscriber.getEmail());
            user.put("subscribedDate", user, subscriber.getSubscribedDate());
            return user;
        }
        return null;
    }

    public static boolean jsFunction_addSubscriber(Context cx,
                                                        Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        if (isStringArray(args)) {
            Subscriber subscriber = new Subscriber((String) args[0]);
            subscriber.setSubscribedDate(new Date());
            //TODO : need to set the proper email
            subscriber.setEmail("");
            subscriber.setTenantId(0);
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            try {
                apiConsumer.addSubscriber(subscriber);
            } catch (APIManagementException e) {
				log.error("Error from Registry API while adding Subscriber"
						+ e);
				return false;
			} catch (IllegalArgumentException e) {
				log.error("Error from Registry API while adding Subscriber "
						+ e);
				return false;
			} catch (NullPointerException e) {
				log.error("Error from Registry API while adding Subscriber"
						+ e);
				return false;
			} catch (Exception e) {
				log.error("Error while adding Subscriber " + e);
				return false;
			}
            return true;
        }
        return false;
    }

    public static NativeArray jsFunction_getApplications(Context cx,
                                                          Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        NativeArray myn = new NativeArray(0);
        if (isStringArray(args)) {
            String username = args[0].toString();
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            Application[] applications = apiConsumer.getApplications(new Subscriber(username));
            if (applications != null) {
                int i = 0;
                for (Application application : applications) {
                    NativeObject row = new NativeObject();
                    row.put("name", row, application.getName());
                    row.put("id", row, application.getId());
                    myn.put(i++, myn, row);
                }
            }
        }
        return myn;
    }

    public static boolean jsFunction_addApplication(Context cx,
                                                          Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        if (isStringArray(args)) {
            String name = (String) args[0];
            String username = (String) args[1];
            Application application = new Application(name, new Subscriber(username));
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            apiConsumer.addApplication(application, username);
            return true;
        }
        return false;
    }

    public static boolean jsFunction_removeApplication(Context cx,
                                                    Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {
        
        if (isStringArray(args)) {
            String name = (String) args[0];
            String username = (String) args[1];
            Subscriber subscriber = new Subscriber(username);
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            Application[] apps = apiConsumer.getApplications(subscriber);
            if (apps == null || apps.length == 0) {
                return false;
            }
            for (Application app : apps) {
                if (app.getName().equals(name)) {
                    apiConsumer.removeApplication(app);
                    return true;
                }
            }
        }
        return false;
    }

    public static NativeArray jsFunction_getSubscriptionsByApplication(Context cx,
                                                       Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        NativeArray myn = new NativeArray(0);
        if (isStringArray(args)) {
            String name = (String) args[0];
            String username = (String) args[1];
            Subscriber subscriber = new Subscriber(username);
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            Set<SubscribedAPI> subscribedAPIs = apiConsumer.getSubscribedAPIs(subscriber);
            int i = 0;
            for (SubscribedAPI api : subscribedAPIs) {
                if (api.getApplication().getName().equals(name)) {
                    NativeObject row = new NativeObject();                    
                    row.put("apiName", row, api.getApiId().getApiName());
                    row.put("apiVersion", row, api.getApiId().getVersion());
                    myn.put(i, myn, row);
                    i++;
                }
            }
        }
        return myn;
    }

    public static boolean jsFunction_updateApplication(Context cx,
                                                    Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, APIManagementException {

        if (isStringArray(args)) {
            String name = (String) args[0];
            String oldName = (String) args[1];
            String username = (String) args[2];
            Subscriber subscriber = new Subscriber(username);
            APIConsumer apiConsumer = getAPIConsumer(thisObj);
            Application[] apps = apiConsumer.getApplications(subscriber);
            if (apps == null || apps.length == 0) {
                return false;
            }
            for (Application app : apps) {
                if (app.getName().equals(oldName)) {
                    Application application = new Application(name, subscriber);
                    application.setId(app.getId());
                    apiConsumer.updateApplication(application);
                    return true;
                }
            }
        }
        return false;
    }

    public static NativeArray jsFunction_getInlineContent(Context cx,
			Scriptable thisObj, Object[] args, Function funObj)
			throws ScriptException, APIManagementException {
		String apiName;
		String version;
		String providerName;
		String docName;
		String content;
		NativeArray myn = new NativeArray(0);


		 if (isStringArray(args)) {
				providerName = args[0].toString();
				apiName = args[1].toString();
				version = args[2].toString();
				docName = args[3].toString();
				APIIdentifier apiId = new APIIdentifier(providerName, apiName,
						version);
				try {
                    APIConsumer apiConsumer = getAPIConsumer(thisObj);
                    content = apiConsumer.getDocumentationContent(apiId,docName);
                    log.info(content);
                } catch (Exception e) {
                    log.error("Error while getting Inline Document Content ", e);
                    return null;
                }
				NativeObject row = new NativeObject();
				row.put("providerName", row, providerName);
				row.put("apiName", row, apiName);
				row.put("apiVersion", row, version);
				row.put("docName", row, docName);
				row.put("content", row, "Content of the File"+content);
				myn.put(0, myn, row);

			}
			/*
			 * } catch (APIManagementException e) { log.error(
			 * "Error from Registry API while getting Inline Document Content ",
			 * e); return null;
			 */

		return myn;
	}

	/*
	 * here return boolean with checking all objects in array is string
	 */
	public static boolean isStringArray(Object[] args) {
		int argsCount = args.length;
		for (int i = 0; i < argsCount; i++) {
			if (!(args[i] instanceof String)) {
				return false;
			}
		}
		return true;

	}
}
