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
package org.wso2.carbon.identity.entitlement.pdp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.wso2.balana.ctx.RequestCtxFactory;
import org.wso2.balana.ctx.AbstractRequestCtx;
import org.wso2.balana.ctx.xacml2.RequestCtx;
import org.wso2.balana.finder.ResourceFinder;
import org.wso2.balana.finder.ResourceFinderModule;
import net.sf.jsr107cache.Cache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.wso2.carbon.caching.core.identity.IdentityCacheEntry;
import org.wso2.carbon.caching.core.identity.IdentityCacheKey;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.entitlement.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.EntitlementUtil;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.identity.entitlement.pip.CarbonAttributeFinder;
import org.wso2.carbon.identity.entitlement.pip.PIPExtension;
import org.wso2.carbon.identity.entitlement.policy.PolicyMetaDataFinder;
import org.wso2.carbon.identity.entitlement.policy.PolicyReader;
import org.wso2.carbon.identity.entitlement.policy.PolicyRequestBuilder;
import org.wso2.carbon.identity.entitlement.policy.PolicyResponseBuilder;
import org.wso2.carbon.identity.entitlement.policy.PolicyStoreReader;
import org.wso2.carbon.identity.entitlement.policy.PolicyStore;
import org.wso2.carbon.identity.entitlement.policy.finder.RegistryBasedPolicyFinder;

import org.wso2.balana.PDP;
import org.wso2.balana.PDPConfig;
import org.wso2.balana.ParsingException;
import org.wso2.balana.ctx.ResponseCtx;
import org.wso2.balana.finder.AttributeFinder;
import org.wso2.balana.finder.PolicyFinder;
import org.wso2.balana.finder.PolicyFinderModule;
import org.wso2.balana.finder.impl.CurrentEnvModule;
import org.wso2.balana.finder.impl.SelectorModule;
import org.wso2.carbon.identity.entitlement.pip.CarbonResourceFinder;
import org.wso2.carbon.registry.core.Registry;

public class EntitlementEngine {

	private RegistryBasedPolicyFinder registryModule;
	private CarbonAttributeFinder carbonAttributeFinder;
    private CarbonResourceFinder carbonResourceFinder;
    private PolicyMetaDataFinder metaDataFinder;
	private PDP pdp;
    private PDPConfig pdpConfig;
	private int tenantId;
	private int cacheClearingNode = 0;
	private static volatile EntitlementEngine engine;
	private static final Object lock = new Object();
	private int pdpDecisionCachingInterval = 60000;
	private static ConcurrentHashMap<String, EntitlementEngine> entitlementEngines = new ConcurrentHashMap<String, EntitlementEngine>();
	private Map<String, PolicyDecision> decisionCache = new ConcurrentHashMap<String, PolicyDecision>();
	private Map<String, PolicyDecision> simpleDecisionCache = new ConcurrentHashMap<String, PolicyDecision>();

	private Cache decisionClearingCache = EntitlementUtil
			.getCommonCache(EntitlementConstants.XACML_DECISION_CACHE);

	private static Log log = LogFactory.getLog(EntitlementEngine.class);

	/**
	 * Get a EntitlementEngine instance for that tenant. This method will return an
	 * EntitlementEngine instance if exists, or creates a new one
	 * 
	 * @param registry Governance Registry instance of the corresponding tenant
	 * @param tenantId Tenant ID of corresponding tenant
	 * @return EntitlementEngine instance for that tenant
	 * @throws org.wso2.carbon.identity.base.IdentityException throws IdentityException
	 */
	public static EntitlementEngine getInstance(Registry registry, int tenantId)
			throws IdentityException {

        // TODO   why Registry is there ?
        // TODO   create a lock ()
		if (!entitlementEngines.containsKey(Integer.toString(tenantId))) {
			entitlementEngines.put(Integer.toString(tenantId), new EntitlementEngine(registry,
					tenantId));
		}
		return entitlementEngines.get(Integer.toString(tenantId));
	}

	private EntitlementEngine(Registry registry, int tenantId) throws IdentityException {

		PolicyFinder policyFinder = null;
        ResourceFinder resourceFinder = null;

		Set<PolicyFinderModule> policyModules = null;

		this.tenantId = tenantId;

		// Setup the PolicyFinder that the EntitlementEngine will use
		policyFinder = new PolicyFinder();

		registryModule = new RegistryBasedPolicyFinder(new PolicyStoreReader(new PolicyStore(
				registry)), tenantId);

		policyModules = new HashSet<PolicyFinderModule>();
		// Add all policy finders - we only have RegistryBasedPolicyFinder
		policyModules.add(registryModule);

		policyFinder.setModules(policyModules);

        resourceFinder = new ResourceFinder();
		// init policy reader
		PolicyReader.getInstance(null, policyFinder);

		// Now setup attribute finder modules for the current date/time and
		// AttributeSelectors (selectors are optional, but this project does
		// support a basic implementation)
		CurrentEnvModule envAttributeModule = new CurrentEnvModule();
		SelectorModule selectorAttributeModule = new SelectorModule();

		// Setup the AttributeFinder just like we setup the PolicyFinder. Note
		// that unlike with the policy finder, the order matters here.
		AttributeFinder attributeFinder = new AttributeFinder();
		List<Object> attributeModules = new ArrayList<Object>();
		attributeModules.add(envAttributeModule);
		attributeModules.add(selectorAttributeModule);
		carbonAttributeFinder = new CarbonAttributeFinder(tenantId);
		carbonAttributeFinder.init();
		attributeModules.add(carbonAttributeFinder);
		attributeFinder.setModules(attributeModules);

		carbonResourceFinder = new CarbonResourceFinder(tenantId);
		carbonResourceFinder.init();

        metaDataFinder = new PolicyMetaDataFinder(tenantId);
        metaDataFinder.init();
        
        List<ResourceFinderModule> resourceModuleList = new ArrayList<ResourceFinderModule>();
        resourceModuleList.add(carbonResourceFinder);
        resourceFinder.setModules(resourceModuleList);
        Properties properties = EntitlementServiceComponent.getEntitlementConfig().getCachingProperties();
		String cacheEnable = properties.getProperty(EntitlementConstants.DECISION_CACHING);
		if ("true".equals(cacheEnable)) {
			String cacheInterval = properties.getProperty(EntitlementConstants.DECISION_CACHING_INTERVAL);
			if (cacheInterval != null) {
				pdpDecisionCachingInterval = Integer.parseInt(cacheInterval);
			}
		} else {
			pdpDecisionCachingInterval = -1;
		}
        
        pdpConfig = new PDPConfig(attributeFinder, policyFinder, resourceFinder, true);
		// Finally, initialize
		pdp = new PDP(pdpConfig);
	}

	/**
	 * Evaluates the given XACML request and returns the Response that the EntitlementEngine will
	 * hand back to the PEP. PEP needs construct the XACML request before sending it to the
	 * EntitlementEngine
	 * 
	 * @param xamlRequest XACML request as an Element
	 * @return XACML response as String
	 * @throws org.wso2.balana.ParsingException throws
	 */
	public String evaluate(Element xamlRequest) throws ParsingException {

		RequestCtx requestCtx = RequestCtx.getInstance(xamlRequest);
        ResponseCtx responseCtx;
        String response;
        String request;

        OutputStream responseOutXml = new ByteArrayOutputStream();
        OutputStream requestOutXml = new ByteArrayOutputStream();
        requestCtx.encode(requestOutXml);
        request = requestOutXml.toString();
        try {
            requestOutXml.close();
        } catch (IOException e) {
           log.error("Error while closing out put stream of XACML request");
        }

        if ((response = getFromCache(request, false)) != null) {
            return response;
		}
        
		// evaluate the request
		responseCtx = pdp.evaluate(requestCtx);

        responseCtx.encode(responseOutXml);
        response = responseOutXml.toString();

        try {
            responseOutXml.close();
        } catch (IOException e) {
           log.error("Error while closing out put stream of XACML response");
        }

        addToCache(request, response, false);

		return response;

	}

	/**
	 * Evaluates the given XACML request and returns the Response that the EntitlementEngine will
	 * hand back to the PEP. PEP needs construct the XACML request before sending it to the
	 * EntitlementEngine
	 *
     * @param xacmlRequest  XACML request as String
     * @return XACML response as String
     * @throws org.wso2.balana.ParsingException throws
     * @throws org.wso2.carbon.identity.base.IdentityException throws
     */

	public String evaluate(String xacmlRequest) throws IdentityException, ParsingException {

        String xacmlResponse;

        if ((xacmlResponse = getFromCache(xacmlRequest, false)) != null) {
            return xacmlResponse;
		}

        Map<PIPExtension, Properties> extensions = EntitlementServiceComponent.getEntitlementConfig()
                .getExtensions();

        if(extensions != null && !extensions.isEmpty()){
            PolicyRequestBuilder policyRequestBuilder = new PolicyRequestBuilder();
            PolicyResponseBuilder policyResponseBuilder = new PolicyResponseBuilder();
            Element xacmlRequestElement = policyRequestBuilder.getXacmlRequest(xacmlRequest);
            AbstractRequestCtx requestCtx = RequestCtxFactory.getFactory().
                                                            getRequestCtx(xacmlRequestElement);
            Set<PIPExtension> pipExtensions = extensions.keySet();
            for (PIPExtension pipExtension : pipExtensions) {
                pipExtension.update(requestCtx);
            }
            ResponseCtx responseCtx; responseCtx = pdp.evaluate(requestCtx);
            xacmlResponse = policyResponseBuilder.getXacmlResponse(responseCtx);
        } else {
            xacmlResponse = pdp.evaluate(xacmlRequest);
        }

        addToCache(xacmlRequest, xacmlResponse, false);
        return xacmlResponse;

	}

    /**
	 * Evaluates the given XACML request and returns the Response that the EntitlementEngine will
	 * hand back to the PEP. Here PEP does not need construct the XACML request before sending it to the
	 * EntitlementEngine. Just can send the single attribute value. But here default attribute ids and data types
     * are used
     * @param subject subject
     * @param resource resource
     * @param action  action
     * @param environment  environment
     * @return XACML request as String object
     * @throws Exception throws, if fails
     */
    public String evaluate(String subject, String resource, String action, String environment)
            throws Exception {

        String response;
        String request =  (subject != null  ? subject : "")  + (resource != null  ? resource : "") +
                            (action != null  ? action : "") + (environment != null  ? environment : "");

        if ((response = getFromCache(request, true)) != null) {
            return response;
		}

        String requestAsString = EntitlementUtil.createSimpleXACMLRequest(subject, resource, action);

        response = pdp.evaluate(requestAsString);
        
        addToCache(request, response, true);
        return response;
    }


	/**
	 * This method is returns the registry based policy finder for current tenant
	 * 
	 * @return RegistryBasedPolicyFinder
	 */
	public RegistryBasedPolicyFinder getRegistryModule() {
		return registryModule;
	}

	/**
	 * This method returns the carbon based attribute finder for the current tenant
	 * 
	 * @return   CarbonAttributeFinder
	 */
	public CarbonAttributeFinder getCarbonAttributeFinder() {
		return carbonAttributeFinder;
	}

    /**
     * This method returns the policy meta data finder for the current tenant
     *
     * @return  PolicyMetaDataFinder
     */
    public PolicyMetaDataFinder getMetaDataFinder() {
        return metaDataFinder;
    }

    /**
     *  This method returns the carbon based resource finder for the current tenant
     * 
     * @return  CarbonResourceFinder
     */
    public CarbonResourceFinder getCarbonResourceFinder() {
        return carbonResourceFinder;
    }

    /**
     * This method returns PDP configurations
     * @return PDPConfig
     */
    public PDPConfig getPdpConfig() {
        return pdpConfig;
    }

    /**
     * get entry from decision caching
     * @param request XACML request as String
     * @param simpleCache whether using simple cache or not
     * @return XACML response as String
     */
    private String getFromCache(String  request, boolean simpleCache) {
		if (pdpDecisionCachingInterval > 0) {

            PolicyDecision decision;

			IdentityCacheEntry cacheEntry = (IdentityCacheEntry) decisionClearingCache
					.get(new IdentityCacheKey(tenantId, ""));
			if (cacheEntry != null) {
				if (cacheEntry.getHashEntry() != (cacheClearingNode)) {
					decisionCache.clear();
                    simpleDecisionCache.clear();
					if (log.isDebugEnabled()) {
						log.debug("Decision Cache is cleared for tenant " + tenantId);
					}
					cacheClearingNode = cacheEntry.getHashEntry();
					return null;
				}
			}

            if(simpleCache){
			    decision = simpleDecisionCache.get(request);
            } else {
                decision = decisionCache.get(request);
            }

			if (decision != null
					&& (decision.getCachedTime() + (long) pdpDecisionCachingInterval > Calendar
							.getInstance().getTimeInMillis())) {
				if (log.isDebugEnabled()) {
					log.debug("PDP Decision Cache Hit");
				}
				return decision.getResponse();
			} else {
				if (log.isDebugEnabled()) {
					log.debug("PDP Decision Cache Miss");
				}
                if(simpleCache){
                    simpleDecisionCache.remove(request);
                } else {
				    decisionCache.remove(request);                    
                }

				return null;
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("PDP Decision Caching Disabled");
		}
		return null;
	}

    /**
     * put entry in to cache
     * @param request  XACML request as String
     * @param response XACML response as String
     * @param simpleCache whether using simple cache or not
     */
	private void addToCache(String request, String response, boolean simpleCache) {
		if (pdpDecisionCachingInterval > 0) {
			PolicyDecision decision = new PolicyDecision();
			decision.setCachedTime(Calendar.getInstance().getTimeInMillis());
			decision.setResponse(response);
            if(simpleCache){
                simpleDecisionCache.put(request, decision);    
            } else {
			    decisionCache.put(request, decision);
            }

			if (log.isDebugEnabled()) {
				log.debug("PDP Decision Cache Updated");
			}
		} else {
			if (log.isDebugEnabled()) {
				log.debug("PDP Decision Caching Disabled");
			}
		}
	}

	/**
	 * Clears the decision cache.
     * @param updateAllNodes whether to propagate the clearing of the cache to other nodes
     */
	public void clearDecisionCache(boolean updateAllNodes) {
		decisionCache.clear();
        simpleDecisionCache.clear();
		if (log.isDebugEnabled()) {
			log.debug("Decision Cache is cleared for tenant " + tenantId);
		}

        if(updateAllNodes){
            IdentityCacheKey cacheKey = new IdentityCacheKey(tenantId, "");
            IdentityCacheEntry cacheEntry = (IdentityCacheEntry) decisionClearingCache.get(cacheKey);
            if(cacheEntry != null){
                cacheClearingNode = cacheEntry.getHashEntry();
            }
            cacheClearingNode ++ ;
            if(cacheClearingNode == Integer.MAX_VALUE){
                cacheClearingNode = 0;
            }
            cacheEntry = new IdentityCacheEntry(cacheClearingNode);
            decisionClearingCache.put(cacheKey, cacheEntry);
        }
        
	}

}