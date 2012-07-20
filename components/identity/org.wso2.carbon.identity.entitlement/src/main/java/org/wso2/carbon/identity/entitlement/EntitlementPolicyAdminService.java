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
package org.wso2.carbon.identity.entitlement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.balana.AbstractPolicy;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.IdentityRegistryResources;
import org.wso2.carbon.identity.entitlement.dto.*;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.identity.entitlement.pdp.EntitlementEngine;
import org.wso2.carbon.identity.entitlement.pip.AbstractPIPAttributeFinder;
import org.wso2.carbon.identity.entitlement.pip.CarbonAttributeFinder;
import org.wso2.carbon.identity.entitlement.pip.CarbonResourceFinder;
import org.wso2.carbon.identity.entitlement.pip.PIPAttributeFinder;
import org.wso2.carbon.identity.entitlement.pip.PIPResourceFinder;
import org.wso2.carbon.identity.entitlement.policy.PolicyAdmin;
import org.wso2.carbon.identity.entitlement.policy.PolicyMetaDataFinder;
import org.wso2.carbon.identity.entitlement.policy.PolicyReader;
import org.wso2.carbon.identity.entitlement.policy.PolicySearch;
import org.wso2.carbon.identity.entitlement.policy.PolicyStore;
import org.wso2.carbon.identity.entitlement.policy.PolicyStoreReader;
import org.wso2.carbon.identity.entitlement.policy.finder.RegistryBasedPolicyFinder;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Entitlement Admin Service Class which exposes the PAP
 */
public class EntitlementPolicyAdminService extends AbstractAdmin {

	private static Log log = LogFactory.getLog(EntitlementPolicyAdminService.class);

	/**
	 * This method persists a new XACML policy
	 * 
	 * @param policy PolicyDTO object
	 * @throws org.wso2.carbon.identity.base.IdentityException throws if invalid policy or if policy
	 *             with same id is exist
	 */
	public void addPolicy(PolicyDTO policy) throws IdentityException {
		PolicyAdmin policyAdmin;
		AbstractPolicy policyObj;
	    EntitlementEngine entitlementEngine = EntitlementEngine.getInstance(
				getGovernanceUserRegistry(), CarbonContext.getCurrentContext().getTenantId());
        EntitlementUtil.validatePolicy(policy);
		policyObj = PolicyReader.getInstance(null, null).getPolicy(policy.getPolicy());
		if (policyObj != null) {
			policyAdmin = new PolicyAdmin(new PolicyStore(getGovernanceUserRegistry()));
			policy.setPolicyId(policyObj.getId().toASCIIString());
			// All the policies wont be active at the time been added.
			policy.setActive(policy.isActive());
			if (getPolicy(policy.getPolicyId()) != null) {
				throw new IdentityException(
						"An Entitlement Policy with the given ID already exists");
			}
			policyAdmin.addOrUpdatePolicy(policy);
			entitlementEngine.getRegistryModule().init(null);
			// Whenever we add a policy - we need to clear decision cache.
			entitlementEngine.clearDecisionCache(true);
		} else {
			throw new IdentityException("Invalid Entitlement Policy");
		}
	}

	/**
	 * Adds XACML policies in bulk to the system.
	 * 
	 * @param policies Array of policies.
	 * @throws IdentityException throws
	 */
	public void addPolicies(PolicyDTO[] policies) throws IdentityException {

		if (policies != null && policies.length > 0) {
			EntitlementEngine entitlementEngine = EntitlementEngine.getInstance(
					getGovernanceUserRegistry(), CarbonContext.getCurrentContext().getTenantId());
			PolicyAdmin policyAdmin;
			policyAdmin = new PolicyAdmin(new PolicyStore(getGovernanceUserRegistry()));

			for (int i = 0; i < policies.length; i++) {
				AbstractPolicy policyObj;
				PolicyDTO policy = policies[i];
                EntitlementUtil.validatePolicy(policy);
				policyObj = PolicyReader.getInstance(null, null).getPolicy(policy.getPolicy());
				if (policyObj != null) {
					policy.setPolicyId(policyObj.getId().toASCIIString());
					policy.setActive(policy.isActive());
					if (getPolicy(policy.getPolicyId()) != null) {
						throw new IdentityException(
								"An Entitlement Policy with the given ID already exists");
					}
					policyAdmin.addOrUpdatePolicy(policy);
					entitlementEngine.getRegistryModule().init(null);
					// Whenever we add a policy - we need to clear decision cache.
					entitlementEngine.clearDecisionCache(true);
				} else {
					throw new IdentityException("Invalid Entitlement Policy");
				}
			}
		} else {
			throw new IdentityException("No valid Entitlement policies provided.");
		}
	}

	/**
	 * This method finds the policy file from given registry path and adds the policy
	 * 
	 * @param policyRegistryPath given registry path
	 * @throws org.wso2.carbon.identity.base.IdentityException throws when fails or registry error
	 *             occurs
	 */
	public void importPolicyFromRegistry(String policyRegistryPath) throws IdentityException {

		Registry registry;
		PolicyDTO policyDTO = new PolicyDTO();
		String policy = "";
		BufferedReader bufferedReader = null;
		InputStream inputStream = null;

		// Finding from which registry by comparing prefix of resource path
		String resourceUri = policyRegistryPath.substring(policyRegistryPath.lastIndexOf(':') + 1);
		String registryIdentifier = policyRegistryPath.substring(0,
				policyRegistryPath.lastIndexOf(':'));
		if (IdentityRegistryResources.CONFIG_REGISTRY_IDENTIFIER.equals(registryIdentifier)) {
			registry = getConfigSystemRegistry();
		} else {
			registry = getGovernanceUserRegistry();
		}

		try {
			Resource resource = registry.get(resourceUri);
			inputStream = resource.getContentStream();
			bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			String stringLine;
			StringBuffer buffer = new StringBuffer(policy);
			while ((stringLine = bufferedReader.readLine()) != null) {
				buffer.append(stringLine);
			}
			policy = buffer.toString();
			policyDTO.setPolicy(policy.replaceAll(">\\s+<", "><"));
			addPolicy(policyDTO);
		} catch (RegistryException e) {
			throw new IdentityException("Registry Error occurs while reading policy from registry");
		} catch (IOException e) {
			throw new IdentityException("I/O Error occurs while reading policy from registry");
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					log.error("Error occurs while closing inputStream", e);
				}
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					log.error("Error occurs while closing inputStream", e);
				}
			}
		}
	}

	/**
	 * This method paginates policies
	 * 
	 * @param policyTypeFilter policy type to filter
	 * @param policySearchString policy search String
	 * @param pageNumber page number
	 * @return paginated and filtered policy set
	 * @throws org.wso2.carbon.identity.base.IdentityException throws
	 */
	public PaginatedPolicySetDTO getAllPolicies(String policyTypeFilter, String policySearchString,
			int pageNumber) throws IdentityException {
		PolicyStoreReader policyReader;
		List<PolicyDTO> policyDTOList = new ArrayList<PolicyDTO>();
		EntitlementEngine.getInstance(getGovernanceUserRegistry(), CarbonContext
				.getCurrentContext().getTenantId());
		policyReader = new PolicyStoreReader(new PolicyStore(getGovernanceUserRegistry()));
		PolicyDTO[] policyDTOs = policyReader.readAllLightPolicyDTOs();

		for (PolicyDTO policyDTO : policyDTOs) {
			boolean useAttributeFiler = false;
			// Filter out policies based on policy type
			if (!policyTypeFilter.equals("ALL")
					&& !policyTypeFilter.equals(policyDTO.getPolicyType())) {
				continue;
			}

            if(policySearchString != null && policySearchString.trim().length() > 0){
                // Filter out policies based on attribute value
                PolicyDTO metaDataPolicyDTO = policyReader.readMetaDataPolicyDTO(policyDTO.getPolicyId());
                AttributeValueDTO[] attributeValueDTOs = metaDataPolicyDTO.getPolicyMetaData();
                for (AttributeValueDTO attributeValueDTO : attributeValueDTOs) {
                    if (policySearchString.equals(attributeValueDTO.getAttribute())) {
                        useAttributeFiler = true;
                        break;
                    }
                }

                if (!useAttributeFiler) {
                    // Filter out policies based on policy Search String
                    if (policySearchString != null && policySearchString.trim().length() > 0) {
                        if (policyDTO.getPolicyId().toLowerCase()
                                .indexOf(policySearchString.toLowerCase()) == -1) {
                            continue;
                        }
                    }
                }
            }

			policyDTOList.add(policyDTO);
		}

		// Do the pagination and return the set of policies.
		return doPaging(pageNumber, policyDTOList.toArray(new PolicyDTO[policyDTOList.size()]));
	}

	/**
	 * Gets policy for given policy id
	 * 
	 * @param policyId policy id
	 * @return returns policy
	 * @throws org.wso2.carbon.identity.base.IdentityException throws
	 */
	public PolicyDTO getPolicy(String policyId) throws IdentityException {
		PolicyStoreReader policyReader = null;
		EntitlementEngine.getInstance(getGovernanceUserRegistry(), CarbonContext
				.getCurrentContext().getTenantId());
		policyReader = new PolicyStoreReader(new PolicyStore(getGovernanceUserRegistry()));
		return policyReader.readPolicyDTO(policyId);
	}


	/**
	 * Gets light weight policy DTO for given policy id
	 *
	 * @param policyId policy id
	 * @return returns policy
	 * @throws org.wso2.carbon.identity.base.IdentityException throws
	 */
	public PolicyDTO getLightPolicy(String policyId) throws IdentityException {
		PolicyStoreReader policyReader = null;
		EntitlementEngine.getInstance(getGovernanceUserRegistry(), CarbonContext
				.getCurrentContext().getTenantId());
		policyReader = new PolicyStoreReader(new PolicyStore(getGovernanceUserRegistry()));
		return policyReader.readLightPolicyDTO(policyId);
	}

	/**
	 * Gets light weight policy DTO with attribute Meta data for given policy id
	 *
	 * @param policyId policy id
	 * @return returns policy
	 * @throws org.wso2.carbon.identity.base.IdentityException throws
	 */
	public PolicyDTO getMetaDataPolicy(String policyId) throws IdentityException {
		PolicyStoreReader policyReader = null;
		EntitlementEngine.getInstance(getGovernanceUserRegistry(), CarbonContext
				.getCurrentContext().getTenantId());
		policyReader = new PolicyStoreReader(new PolicyStore(getGovernanceUserRegistry()));
		return policyReader.readMetaDataPolicyDTO(policyId);
	}

	/**
	 * Removes policy for given policy object
	 * 
	 * @param policy policy object
	 * @throws org.wso2.carbon.identity.base.IdentityException throws
	 */
	public void removePolicy(PolicyDTO policy) throws IdentityException {
		PolicyAdmin policyAdmin;
		EntitlementEngine entitlementEngine = EntitlementEngine.getInstance(
				getGovernanceUserRegistry(), CarbonContext.getCurrentContext().getTenantId());
		policyAdmin = new PolicyAdmin(new PolicyStore(getGovernanceUserRegistry()));
		policyAdmin.removePolicy(policy);
		// Reload the policies to the memory.
		entitlementEngine.getRegistryModule().init(null);
		entitlementEngine.clearDecisionCache(true);
	}

	/**
	 * Updates given policy
	 * 
	 * @param policy policy object
	 * @throws org.wso2.carbon.identity.base.IdentityException throws if invalid policy
	 */
	public void updatePolicy(PolicyDTO policy) throws IdentityException {
		PolicyAdmin policyAdmin;
		AbstractPolicy policyObj;
		EntitlementEngine entitlementEngine = EntitlementEngine.getInstance(
				getGovernanceUserRegistry(), CarbonContext.getCurrentContext().getTenantId());
        EntitlementUtil.validatePolicy(policy);
        policyAdmin = new PolicyAdmin(new PolicyStore(getGovernanceUserRegistry()));
        if(policy.getPolicyId() != null && policy.getPolicy() == null){
            policyAdmin.addOrUpdatePolicy(policy);
        } else {
            policyObj = PolicyReader.getInstance(null, null).getPolicy(policy.getPolicy());
            if (policyObj != null) {
                policy.setPolicyId(policyObj.getId().toASCIIString());    
            } else {
                throw new IdentityException("Invalid Entitlement Policy");
            }
            policyAdmin.addOrUpdatePolicy(policy);
        }
        // Reload the policies to the memory.
        entitlementEngine.getRegistryModule().init(null);
        entitlementEngine.clearDecisionCache(true);

	}

    /**
     * get attribute values to XACML editor UI from meta data finder modules
     * @return Array of attribute value trees as PolicyAttributeDTO object
     * @throws IdentityException throws, if fails
     */
    public PolicyAttributeDTO[] getPolicyAttributeValues()
            throws IdentityException {
        Set<PolicyAttributeDTO> policyAttributeDTOSet = null;
		PolicyMetaDataFinder metaDataFinder = EntitlementEngine.getInstance(getGovernanceUserRegistry(),
				CarbonContext.getCurrentContext().getTenantId()).getMetaDataFinder();
        policyAttributeDTOSet = metaDataFinder.getPolicyAttributeValues();
        if(policyAttributeDTOSet != null){
            return policyAttributeDTOSet.toArray(new PolicyAttributeDTO[policyAttributeDTOSet.size()]);
        }

        return null;
    }


	/**
	 * This method is used get pre-defined data-types, functions, identifiers and algorithms related
	 * to XACML Policy
	 * 
	 * @param resourceName Registry resource Name related to one bag of data
	 * @return data as String array
	 * @throws org.wso2.carbon.identity.base.IdentityException throws when registry error occurs
	 */

	public String[] getEntitlementPolicyDataFromRegistry(String resourceName)
			throws IdentityException {
		PolicyStore policyStoreAdmin;
		List<String> entitlementPolicyResources = new ArrayList<String>();
		Resource resource;
		InputStream inputStream = null;
		BufferedReader bufferedReader = null;
		try {
			policyStoreAdmin = new PolicyStore(EntitlementServiceComponent.getRegistryService()
					.getGovernanceSystemRegistry());
			resource = policyStoreAdmin.getEntitlementPolicyResources(resourceName);
			inputStream = resource.getContentStream();
			bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			String stringLine;
			while ((stringLine = bufferedReader.readLine()) != null) {
				entitlementPolicyResources.add(stringLine.trim());
			}
		} catch (IOException e) {
			throw new IdentityException("Error occurs while reading resource content", e);
		} catch (RegistryException e) {
			throw new IdentityException("Error occurs while creating inputStream from registry "
					+ "resource", e);
		} catch (IdentityException e) {
			throw new IdentityException("Error occurs while initializing PolicyStore", e);
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					log.error("Error occurs while closing inputStream", e);
				}
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					log.error("Error occurs while closing inputStream", e);
				}
			}
		}

		return entitlementPolicyResources.toArray(new String[entitlementPolicyResources.size()]);
	}

	/**
	 * This method returns the list of policy id available in PDP
	 * 
	 * @return list of ids
	 * @throws IdentityException throws
	 */
	public String[] getAllPolicyIds() throws IdentityException {
		List<String> policyIds = new ArrayList<String>();
		PolicyStoreReader policyReader;
		policyReader = new PolicyStoreReader(new PolicyStore(getGovernanceUserRegistry()));

		for (PolicyDTO policyDTO : policyReader.readAllLightPolicyDTOs()) {
			policyIds.add(policyDTO.getPolicyId());
		}
		return policyIds.toArray(new String[policyIds.size()]);
	}

	/**
	 * Clears the decision cache.
	 * 
	 * @throws IdentityException throws
	 */
	public void clearDecisionCache() throws IdentityException {
		EntitlementEngine.getInstance(getGovernanceUserRegistry(),
				CarbonContext.getCurrentContext().getTenantId()).clearDecisionCache(true);
	}


	/**
	 * Clears Carbon attribute finder cache and All the attribute cache implementations in each
     * PIP attribute finder level
	 *
	 * @throws IdentityException throws
	 */
	public void clearAllAttributeCaches() throws IdentityException {
		CarbonAttributeFinder finder = EntitlementEngine.getInstance(getGovernanceUserRegistry(),
				CarbonContext.getCurrentContext().getTenantId()).getCarbonAttributeFinder();
		if (finder != null) {
			finder.clearAttributeCache();
			// we need invalidate decision cache as well.
			clearDecisionCache();
		} else {
			throw new IdentityException("Can not clear all attribute caches - Carbon Attribute Finder "
					+ "is not initialized");
		}

		Map<PIPAttributeFinder, Properties> designators = EntitlementServiceComponent.getEntitlementConfig()
				.getDesignators();
        if(designators != null && !designators.isEmpty()){
            Set<PIPAttributeFinder> pipAttributeFinders = designators.keySet();
            for (PIPAttributeFinder pipAttributeFinder : pipAttributeFinders) {
                pipAttributeFinder.clearCache();
            }
        }
	}


	/**
	 * Clears the carbon attribute cache
	 * 
	 * @throws IdentityException throws
	 */
	public void clearCarbonAttributeCache() throws IdentityException {
		CarbonAttributeFinder finder = EntitlementEngine.getInstance(getGovernanceUserRegistry(),
				CarbonContext.getCurrentContext().getTenantId()).getCarbonAttributeFinder();
		if (finder != null) {
			finder.clearAttributeCache();
			// we need invalidate decision cache as well.
			clearDecisionCache();
		} else {
			throw new IdentityException("Can not clear attribute cache - Carbon Attribute Finder "
					+ "is not initialized");
		}

		Map<PIPAttributeFinder, Properties> designators = EntitlementServiceComponent.getEntitlementConfig()
				.getDesignators();
        if(designators != null && !designators.isEmpty()){
            Set<PIPAttributeFinder> pipAttributeFinders = designators.keySet();
            for (PIPAttributeFinder pipAttributeFinder : pipAttributeFinders) {
                if(pipAttributeFinder instanceof AbstractPIPAttributeFinder){
                    pipAttributeFinder.clearCache();
                }
            }
        }        
	}

    /**
     * Clears the cache maintained by the attribute finder.
     *
     * @param attributeFinder Canonical name of the attribute finder class.
     */
    public void clearAttributeFinderCache(String attributeFinder) {

		Map<PIPAttributeFinder, Properties> designators = EntitlementServiceComponent.getEntitlementConfig()
				.getDesignators();
        if(designators != null && !designators.isEmpty()){
            Set<PIPAttributeFinder> pipAttributeFinders = designators.keySet();
            for (PIPAttributeFinder pipAttributeFinder : pipAttributeFinders) {
                if(pipAttributeFinder instanceof AbstractPIPAttributeFinder){
                    if (pipAttributeFinder.getClass().getCanonicalName().equals(attributeFinder)) {
                        pipAttributeFinder.clearCache();
                        break;
                    }
                }
            }
        }
    }

	/**
	 * Clears the cache maintained by the attribute finder - by attributes
	 *
	 * @param attributeFinder Canonical name of the attribute finder class.
	 * @param attributeIds An array of attribute id.
	 */
	public void clearAttributeFinderCacheByAttributes(String attributeFinder, String[] attributeIds) {

		Map<PIPAttributeFinder, Properties> designators = EntitlementServiceComponent.getEntitlementConfig()
				.getDesignators();
        if(designators != null && !designators.isEmpty()){
            Set<PIPAttributeFinder> pipAttributeFinders = designators.keySet();
            for (PIPAttributeFinder pipAttributeFinder : pipAttributeFinders) {
                if (pipAttributeFinder.getClass().getCanonicalName().equals(attributeFinder)) {
                    pipAttributeFinder.clearCache(attributeIds);
                    break;
                }
            }
        }        
	}

	/**
	 * Clears Carbon resource finder cache and All the resource cache implementations in each
     * PIP resource finder level
	 *
	 * @throws IdentityException throws
	 */
	public void clearAllResourceCaches() throws IdentityException {
        CarbonResourceFinder finder = EntitlementEngine.getInstance(getGovernanceUserRegistry(),
				CarbonContext.getCurrentContext().getTenantId()).getCarbonResourceFinder();
		if (finder != null) {
			finder.clearAttributeCache();
			// we need invalidate decision cache as well.
			clearDecisionCache();
		} else {
			throw new IdentityException("Can not clear attribute cache - Carbon Attribute Finder "
					+ "is not initialized");
		}
	}    

	/**
	 * Clears the carbon resource cache
	 *
	 * @throws IdentityException throws
	 */
	public void clearCarbonResourceCache() throws IdentityException {
        CarbonResourceFinder finder = EntitlementEngine.getInstance(getGovernanceUserRegistry(),
				CarbonContext.getCurrentContext().getTenantId()).getCarbonResourceFinder();
		if (finder != null) {
			finder.clearAttributeCache();
			// we need invalidate decision cache as well.
			clearDecisionCache();
		} else {
			throw new IdentityException("Can not clear attribute cache - Carbon Attribute Finder "
					+ "is not initialized");
		}

		Map<PIPResourceFinder, Properties> resourceConfigs = EntitlementServiceComponent.getEntitlementConfig()
				.getResourceFinders();
        if(resourceConfigs != null && !resourceConfigs.isEmpty()){
            Set<PIPResourceFinder> resourceFinders = resourceConfigs.keySet();
            for (PIPResourceFinder pipResourceFinder : resourceFinders) {
                pipResourceFinder.clearCache();
            }
        }
	}

	/**
	 * Clears the cache maintained by the resource finder.
	 *
     * @param resourceFinder Canonical name of the resource finder class.
     */
	public void clearResourceFinderCache(String resourceFinder) {

		Map<PIPResourceFinder, Properties> resourceConfigs = EntitlementServiceComponent.getEntitlementConfig()
				.getResourceFinders();
        if(resourceConfigs != null && !resourceConfigs.isEmpty()){
            Set<PIPResourceFinder> resourceFinders = resourceConfigs.keySet();
            for (PIPResourceFinder pipResourceFinder : resourceFinders) {
                if (resourceFinder.getClass().getCanonicalName().equals(resourceFinder)) {
                    pipResourceFinder.clearCache();
                    break;
                }
            }
        }
	}


	/**
	 * Refreshes the supported Attribute ids of a given attribute finder module
	 *
	 * @param attributeFinder Canonical name of the attribute finder class.
	 * @throws IdentityException throws if fails to  refresh
	 */
	public void refreshAttributeFinder(String attributeFinder) throws IdentityException {

		Map<PIPAttributeFinder, Properties> designators = EntitlementServiceComponent.getEntitlementConfig()
				.getDesignators();
        if(designators != null && !designators.isEmpty()){
            Set<Map.Entry<PIPAttributeFinder, Properties>> pipAttributeFinders = designators.entrySet();
            for (Map.Entry<PIPAttributeFinder, Properties> pipAttributeFinder : pipAttributeFinders) {
                if (pipAttributeFinder.getClass().getCanonicalName().equals(attributeFinder)) {
                    try {
                        pipAttributeFinder.getKey().init(pipAttributeFinder.getValue());
                        pipAttributeFinder.getKey().clearCache();
                        CarbonAttributeFinder carbonAttributeFinder = EntitlementEngine.
                                getInstance(getGovernanceUserRegistry(), CarbonContext.getCurrentContext().
                                        getTenantId()).getCarbonAttributeFinder();
                        carbonAttributeFinder.init();
                    } catch (Exception e) {
                        throw new IdentityException("Error while refreshing attribute finder - " +
                                                    attributeFinder);
                    }
                    break;
                }
            }
        }
	}

	/**
	 * Sets policy combining algorithm globally
	 * 
	 * @param policyCombiningAlgorithm policy combining algorithm as a String
	 * @throws IdentityException throws
	 */
	public void setGlobalPolicyAlgorithm(String policyCombiningAlgorithm) throws IdentityException {
		PolicyStore policyStore;
		EntitlementEngine entitlementEngine = EntitlementEngine.getInstance(
				getGovernanceUserRegistry(), CarbonContext.getCurrentContext().getTenantId());
		policyStore = new PolicyStore(getGovernanceUserRegistry());
		policyStore.addPolicyCombiningAlgorithm(policyCombiningAlgorithm);
		entitlementEngine.getRegistryModule().init(null);
		entitlementEngine.clearDecisionCache(true);
	}

	/**
	 * Gets globally defined policy combining algorithm
	 * 
	 * @return policy combining algorithm as a String
	 * @throws IdentityException throws
	 */
	public String getGlobalPolicyAlgorithm() throws IdentityException {
		RegistryBasedPolicyFinder policyFinder = EntitlementEngine.getInstance(
				getGovernanceUserRegistry(), CarbonContext.getCurrentContext().getTenantId())
				.getRegistryModule();
		if (policyFinder != null) {
			return policyFinder.getGlobalPolicyCombiningAlgorithm();
		}
		return null;
	}

	/**
	 * Gets entitled resources for given user or role
	 *
	 * @param subjectName subject Name, User or Role name
     * @param subjectId attribute id of the subject, user or role
	 * @param resourceName resource Name 
     * @param action action name
	 * @param enableChildSearch whether search is done for the child resources under the given  resource name
     * @param useApplicablePolices whether search is done only for applicable policies in given search parameters
     * @return entitled resources as String array
	 * @throws org.wso2.carbon.identity.base.IdentityException throws if invalid data is provided
	 */
	public EntitledResultSetDTO getEntitledAttributes(String subjectName, String resourceName,
                                      String subjectId, String action, boolean enableChildSearch,
                                      boolean useApplicablePolices) throws IdentityException {

		if (subjectName == null) {
			throw new IdentityException(
					"Invalid input data - either the userName or roleName should be non-null");
		}

		PolicySearch policySearch = new PolicySearch();
		return policySearch.getEntitledAttributes(subjectName, resourceName, subjectId, action,
                                                  enableChildSearch, useApplicablePolices);
	}

	/**
	 * This method is used internally to do the pagination purposes.
	 * 
	 * @param pageNumber page Number
	 * @param policySet set of policies
	 * @return PaginatedPolicySetDTO object containing the number of pages and the set of policies
	 *         that reside in the given page.
	 */
	private PaginatedPolicySetDTO doPaging(int pageNumber, PolicyDTO[] policySet) {

		PaginatedPolicySetDTO paginatedPolicySet = new PaginatedPolicySetDTO();
		if (policySet.length == 0) {
			paginatedPolicySet.setPolicySet(new PolicyDTO[0]);
			return paginatedPolicySet;
		}
		String itemsPerPage = ServerConfiguration.getInstance().getFirstProperty("ItemsPerPage");
		int itemsPerPageInt = EntitlementConstants.DEFAULT_ITEMS_PER_PAGE;
		if (itemsPerPage != null) {
			itemsPerPageInt = Integer.parseInt(itemsPerPage);
		}
		int numberOfPages = (int) Math.ceil((double) policySet.length / itemsPerPageInt);
		if (pageNumber > numberOfPages - 1) {
			pageNumber = numberOfPages - 1;
		}
		int startIndex = pageNumber * itemsPerPageInt;
		int endIndex = (pageNumber + 1) * itemsPerPageInt;
		PolicyDTO[] returnedPolicySet = new PolicyDTO[itemsPerPageInt];

		for (int i = startIndex, j = 0; i < endIndex && i < policySet.length; i++, j++) {
			returnedPolicySet[j] = policySet[i];
		}

		paginatedPolicySet.setPolicySet(returnedPolicySet);
		paginatedPolicySet.setNumberOfPages(numberOfPages);

		return paginatedPolicySet;
	}
}
