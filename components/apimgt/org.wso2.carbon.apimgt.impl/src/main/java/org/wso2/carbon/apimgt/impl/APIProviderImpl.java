package org.wso2.carbon.apimgt.impl;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.dto.UserApplicationAPIUsage;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.template.APITemplateBuilder;
import org.wso2.carbon.apimgt.impl.template.BasicTemplateBuilder;
import org.wso2.carbon.apimgt.impl.utils.APINameComparator;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.RESTAPIAdminClient;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.common.CommonConstants;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.*;
import java.util.Collection;

/**
 * This class provides the core API provider functionality. It is implemented in a very
 * self-contained and 'pure' manner, without taking requirements like security into account,
 * which are subject to frequent change. Due to this 'pure' nature and the significance of
 * the class to the overall API management functionality, the visibility of the class has
 * been reduced to package level. This means we can still use it for internal purposes and
 * possibly even extend it, but it's totally off the limits of the users. Users wishing to
 * programmatically access this functionality should use one of the extensions of this
 * class which is visible to them. These extensions may add additional features like
 * security to this class.
 */
class APIProviderImpl extends AbstractAPIManager implements APIProvider {
    
    public APIProviderImpl() throws APIManagementException {
        super();
    }

    /**
     * Returns a list of all #{@link org.wso2.carbon.apimgt.api.model.Provider} available on the system.
     *
     * @return Set<Provider>
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to get Providers
     */
    public Set<Provider> getAllProviders() throws APIManagementException {
        Set<Provider> providerSet = new HashSet<Provider>();
        GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                APIConstants.PROVIDER_KEY);
        try {
            GenericArtifact[] genericArtifact = artifactManager.getAllGenericArtifacts();
            if (genericArtifact == null || genericArtifact.length == 0) {
                return providerSet;
            }
            for (GenericArtifact artifact : genericArtifact) {
                Provider provider =
                        new Provider(artifact.getAttribute(APIConstants.PROVIDER_OVERVIEW_NAME));
                provider.setDescription(APIConstants.PROVIDER_OVERVIEW_DESCRIPTION);
                provider.setEmail(APIConstants.PROVIDER_OVERVIEW_EMAIL);
                providerSet.add(provider);
            }
        } catch (GovernanceException e) {
            handleException("Failed to get all providers", e);
        }
        return providerSet;
    }

    /**
     * Get a list of APIs published by the given provider. If a given API has multiple APIs,
     * only the latest version will
     * be included in this list.
     *
     * @param providerId , provider id
     * @return set of API
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to get set of API
     */
    public List<API> getAPIsByProvider(String providerId) throws APIManagementException {

        List<API> apiSortedList = new ArrayList<API>();

        try {
            String providerPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                    providerId;

            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);
            Association[] associations = registry.getAssociations(providerPath,
                    APIConstants.PROVIDER_ASSOCIATION);
            for (Association association : associations) {
                String apiPath = association.getDestinationPath();
                Resource resource = registry.get(apiPath);
                String apiArtifactId = resource.getUUID();
                if (apiArtifactId != null) {
                    GenericArtifact apiArtifact = artifactManager.getGenericArtifact(apiArtifactId);
                    apiSortedList.add(APIUtil.getAPI(apiArtifact, registry));
                } else {
                    throw new GovernanceException("artifact id is null of " + apiPath);
                }
            }

        } catch (RegistryException e) {
            handleException("Failed to get APIs for provider : " + providerId, e);
        }
        Collections.sort(apiSortedList, new APINameComparator());

        return apiSortedList;

    }


    /**
     * Get a list of all the consumers for all APIs
     *
     * @param providerId if of the provider
     * @return Set<Subscriber>
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to get subscribed APIs of given provider
     */
    public Set<Subscriber> getSubscribersOfProvider(String providerId)
            throws APIManagementException {

        Set<Subscriber> subscriberSet = null;
        try {
            subscriberSet = apiMgtDAO.getSubscribersOfProvider(providerId);
        } catch (APIManagementException e) {
            handleException("Failed to get Subscribers for : " + providerId, e);
        }
        return subscriberSet;
    }

    /**
     * get details of provider
     *
     * @param providerName name of the provider
     * @return Provider
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to get Provider
     */
    public Provider getProvider(String providerName) throws APIManagementException {
        Provider provider = null;
        String providerPath = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                APIConstants.PROVIDERS_PATH + RegistryConstants.PATH_SEPARATOR + providerName;
        try {
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                    APIConstants.PROVIDER_KEY);
            Resource providerResource = registry.get(providerPath);
            String artifactId =
                    providerResource.getUUID();
            if (artifactId == null) {
                throw new APIManagementException("artifact it is null");
            }
            GenericArtifact providerArtifact = artifactManager.getGenericArtifact(artifactId);
            provider = APIUtil.getProvider(providerArtifact);

        } catch (RegistryException e) {
            handleException("Failed to get Provider form : " + providerName, e);
        }
        return provider;
    }

    /**
     * Return Usage of given APIIdentifier
     *
     * @param apiIdentifier APIIdentifier
     * @return Usage
     */
    public Usage getUsageByAPI(APIIdentifier apiIdentifier) {
        return null;
    }

    /**
     * Return Usage of given provider and API
     *
     * @param providerId if of the provider
     * @param apiName    name of the API
     * @return Usage
     */
    public Usage getAPIUsageByUsers(String providerId, String apiName) {
        return null;
    }

    /**
     * Returns usage details of all APIs published by a provider
     *
     * @param providerName Provider Id
     * @return UserApplicationAPIUsages for given provider
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          If failed to get UserApplicationAPIUsage
     */
    public UserApplicationAPIUsage[] getAllAPIUsageByProvider(
            String providerName) throws APIManagementException {
        return apiMgtDAO.getAllAPIUsageByProvider(providerName);
    }

    /**
     * Shows how a given consumer uses the given API.
     *
     * @param apiIdentifier APIIdentifier
     * @param consumerEmail E-mal Address of consumer
     * @return Usage
     */
    public Usage getAPIUsageBySubscriber(APIIdentifier apiIdentifier, String consumerEmail) {
        return null;
    }

    /**
     * Returns full list of Subscribers of an API
     *
     * @param identifier APIIdentifier
     * @return Set<Subscriber>
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to get Subscribers
     */
    public Set<Subscriber> getSubscribersOfAPI(APIIdentifier identifier)
            throws APIManagementException {

        Set<Subscriber> subscriberSet = null;
        try {
            subscriberSet = apiMgtDAO.getSubscribersOfAPI(identifier);
        } catch (APIManagementException e) {
            handleException("Failed to get subscribers for API : " + identifier.getApiName(), e);
        }
        return subscriberSet;
    }

    /**
     * this method returns the Set<APISubscriptionCount> for given provider and api
     *
     * @param identifier APIIdentifier
     * @return Set<APISubscriptionCount>
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to get APISubscriptionCountByAPI
     */
    public long getAPISubscriptionCountByAPI(APIIdentifier identifier)
            throws APIManagementException {
        long count = 0L;
        try {
            count = apiMgtDAO.getAPISubscriptionCountByAPI(identifier);
        } catch (APIManagementException e) {
            handleException("Failed to get APISubscriptionCount for: " + identifier.getApiName(), e);
        }
        return count;
    }

    /**
     * Returns a list of pre-defined # {@link org.wso2.carbon.apimgt.api.model.Tier} in the system.
     *
     * @return Set<Tier>
     */
    public Set<Tier> getTiers() throws APIManagementException {
        Set<Tier> tiers = new HashSet<Tier>();
        try {
            if (registry.resourceExists(APIConstants.API_TIER_LOCATION)) {
                Resource resource = registry.get(APIConstants.API_TIER_LOCATION);
                String content = new String((byte[]) resource.getContent());
                OMElement element = AXIOMUtil.stringToOM(content);
                OMElement assertion = element.getFirstChildWithName(APIConstants.ASSERTION_ELEMENT);
                Iterator policies = assertion.getChildrenWithName(APIConstants.POLICY_ELEMENT);
                while (policies.hasNext()) {
                    OMElement policy = (OMElement) policies.next();
                    OMElement id = policy.getFirstChildWithName(APIConstants.THROTTLE_ID_ELEMENT);
                    Tier tier = new Tier(id.getText());
                    tier.setPolicyContent(policy.toString().getBytes());
                    tiers.add(tier);
                }
            }
        } catch (RegistryException e) {
            handleException("Error while retrieving API tiers from registry", e);
        } catch (XMLStreamException e) {
            handleException("Malformed XML found in the API tier policy resource", e);
        }
        return tiers;
    }

    public void addTier(Tier tier) throws APIManagementException {
        addOrUpdateTier(tier, false);
    }

    public void updateTier(Tier tier) throws APIManagementException {
        addOrUpdateTier(tier, true);
    }
    
    private void addOrUpdateTier(Tier tier, boolean update) throws APIManagementException {
        Set<Tier> tiers = getTiers();
        if (update && !tiers.contains(tier)) {
            throw new APIManagementException("No tier exists by the name: " + tier.getName());
        }
        
        Set<Tier> finalTiers = new HashSet<Tier>();
        for (Tier t : tiers) {
            if (!t.getName().equals(tier.getName())) {
                finalTiers.add(t);    
            }
        }
        finalTiers.add(tier);
        saveTiers(finalTiers);
    }
    
    private void saveTiers(Collection<Tier> tiers) throws APIManagementException {        
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMElement root = fac.createOMElement(APIConstants.POLICY_ELEMENT);
        OMElement assertion = fac.createOMElement(APIConstants.ASSERTION_ELEMENT);
        try {
            for (Tier tier : tiers) {
                String policy = new String(tier.getPolicyContent());
                assertion.addChild(AXIOMUtil.stringToOM(policy));
            }
            root.addChild(assertion);
            Resource resource = registry.newResource();
            resource.setContent(root.toString());
            registry.put(APIConstants.API_TIER_LOCATION, resource);
        } catch (XMLStreamException e) {
            handleException("Error while constructing tier policy file", e);
        } catch (RegistryException e) {
            handleException("Error while saving tier configurations to the registry", e);
        }
    }

    public void removeTier(Tier tier) throws APIManagementException {
        Set<Tier> tiers = getTiers();
        if (tiers.remove(tier)) {
            saveTiers(tiers);
        } else {
            throw new APIManagementException("No tier exists by the name: " + tier.getName());
        }
    }

    /**
     * Adds a new API to the Store
     *
     * @param api API
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to add API
     */
    public void addAPI(API api) throws APIManagementException {
        createAPI(api);
    }

    /**
     * Updates an existing API
     *
     * @param api API
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to update API
     */
    public void updateAPI(API api) throws APIManagementException {
        API oldApi = getAPI(api.getId());
        if (oldApi.getStatus().equals(api.getStatus())) {
            createAPI(api);
            if (isAPIPublished(api)) {
                updateGatewayConfiguration(api);
            }
        } else {
            // We don't allow API status updates via this method.
            // Use changeAPIStatus for that kind of updates.
            throw new APIManagementException("Invalid API update operation involving API status changes");
        }
    }

    public void changeAPIStatus(API api, APIStatus status, 
                                boolean updateGatewayConfig) throws APIManagementException {
        APIStatus currentStatus = api.getStatus();
        if (!currentStatus.equals(status)) {
            api.setStatus(status);
            createAPI(api);
            if (updateGatewayConfig && currentStatus.equals(APIStatus.CREATED) &&
                    status.equals(APIStatus.PUBLISHED)) {
                publishToGateway(api);
            }
        }        
    }

    private void publishToGateway(API api) throws APIManagementException {
        try {
            RESTAPIAdminClient client = new RESTAPIAdminClient(getTemplateBuilder(api));
            client.addApi();
        } catch (AxisFault axisFault) {
            handleException("Error while creating new API in gateway", axisFault);
        }
    }

    private void updateGatewayConfiguration(API api) throws APIManagementException {        
        try {
            RESTAPIAdminClient client = new RESTAPIAdminClient(getTemplateBuilder(api));
            client.updateApi();
        } catch (AxisFault axisFault) {
            handleException("Error while updating API in the API gateway", axisFault);
        }
    }
    
    private boolean isAPIPublished(API api) throws APIManagementException {
        try {
            RESTAPIAdminClient client = new RESTAPIAdminClient(getTemplateBuilder(api));
            return client.getApi() != null;
        } catch (AxisFault axisFault) {
            handleException("Error while checking API status", axisFault);
        }
        return false;
    }
    
    private APITemplateBuilder getTemplateBuilder(API api) {
        Map<String, String> testAPIMappings = new HashMap<String, String>();

        testAPIMappings.put(APITemplateBuilder.KEY_FOR_API_NAME, api.getId().getApiName());
        testAPIMappings.put(APITemplateBuilder.KEY_FOR_API_CONTEXT, api.getContext());
        testAPIMappings.put(APITemplateBuilder.KEY_FOR_API_VERSION, api.getId().getVersion());

        Iterator it = api.getUriTemplates().iterator();
        List<Map<String, String>> resourceMappings = new ArrayList<Map<String, String>>();
        while (it.hasNext()) {
            Map<String,String> uriTemplateMap = new HashMap<String,String>();
            URITemplate temp = (URITemplate) it.next();
            uriTemplateMap.put(APITemplateBuilder.KEY_FOR_RESOURCE_URI_TEMPLATE, temp.getUriTemplate());
            uriTemplateMap.put(APITemplateBuilder.KEY_FOR_RESOURCE_METHODS, temp.getMethodsAsString());
            uriTemplateMap.put(APITemplateBuilder.KEY_FOR_RESOURCE_URI, temp.getResourceURI());
            uriTemplateMap.put(APITemplateBuilder.KEY_FOR_RESOURCE_SANDBOX_URI, temp.getResourceSandboxURI());
            resourceMappings.add(uriTemplateMap);
        }

        Map<String, String> testHandlerMappings_1 = new HashMap<String, String>();
        testHandlerMappings_1.put(APITemplateBuilder.KEY_FOR_HANDLER,
                "org.wso2.carbon.apimgt.handlers.security.APIAuthenticationHandler");

        Map<String, String> testHandlerMappings_2 = new HashMap<String, String>();
        testHandlerMappings_2.put(APITemplateBuilder.KEY_FOR_HANDLER,
                "org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageHandler");

        Map<String, String> testHandlerMappings_3 = new HashMap<String, String>();
        testHandlerMappings_3.put(APITemplateBuilder.KEY_FOR_HANDLER,
                "org.wso2.carbon.apimgt.handlers.throttling.APIThrottleHandler");
        testHandlerMappings_3.put(APITemplateBuilder.KEY_FOR_HANDLER_POLICY_KEY,
                "gov:" + APIConstants.API_TIER_LOCATION);

        List<Map<String, String>> handlerMappings = new ArrayList<Map<String, String>>();
        handlerMappings.add(testHandlerMappings_1);
        handlerMappings.add(testHandlerMappings_2);
        handlerMappings.add(testHandlerMappings_3);

        return new BasicTemplateBuilder(testAPIMappings, resourceMappings, handlerMappings);
    }

    /**
     * Create a new version of the <code>api</code>, with version <code>newVersion</code>
     *
     * @param api        The API to be copied
     * @param newVersion The version of the new API
     * @throws org.wso2.carbon.apimgt.api.model.DuplicateAPIException
     *          If the API trying to be created already exists
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          If an error occurs while trying to create
     *          the new version of the API
     */
    public void createNewAPIVersion(API api, String newVersion) throws DuplicateAPIException,
            APIManagementException {
        String apiSourcePath = APIUtil.getAPIPath(api.getId());

        String targetPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                api.getId().getProviderName() +
                RegistryConstants.PATH_SEPARATOR + api.getId().getApiName() +
                RegistryConstants.PATH_SEPARATOR + newVersion +
                APIConstants.API_RESOURCE_NAME;
        try {
            if (registry.resourceExists(targetPath)) {
                throw new DuplicateAPIException("API version already exist with version :"
                        + newVersion);
            }
            Resource apiSourceArtifact = registry.get(apiSourcePath);
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);
            GenericArtifact artifact = artifactManager.getGenericArtifact(
                    apiSourceArtifact.getUUID());

            //Create new API version
            artifact.setId(UUID.randomUUID().toString());
            artifact.setAttribute(APIConstants.API_OVERVIEW_VERSION, newVersion);

            //Check the status of the existing api,if its not in 'CREATED' status set
            //the new api status as "CREATED"
            String status = artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS);
            if (!status.equals(APIConstants.CREATED)) {
                artifact.setAttribute(APIConstants.API_OVERVIEW_STATUS, APIConstants.CREATED);
            }
            artifactManager.addGenericArtifact(artifact);
            registry.addAssociation(APIUtil.getAPIProviderPath(api.getId()), targetPath,
                    APIConstants.PROVIDER_ASSOCIATION);

            // Make sure to unset the isLatest flag on the old version
            GenericArtifact oldArtifact = artifactManager.getGenericArtifact(
                    apiSourceArtifact.getUUID());
            oldArtifact.setAttribute(APIConstants.API_OVERVIEW_IS_LATEST, "false");
            artifactManager.updateGenericArtifact(oldArtifact);

        } catch (RegistryException e) {
            String msg = "Failed to create new version : " + newVersion + " of : "
                    + api.getId().getApiName();
            handleException(msg, e);
        }
    }

    /**
     * Removes a given documentation
     *
     * @param apiId   APIIdentifier
     * @param docType the type of the documentation
     * @param docName name of the document
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to remove documentation
     */
    public void removeDocumentation(APIIdentifier apiId, String docName, String docType)
            throws APIManagementException {
        String docPath = APIUtil.getAPIDocPath(apiId) + docName;
        try {
            Association[] associations = registry.getAssociations(docPath,
                    APIConstants.DOCUMENTATION_KEY);
            for (Association association : associations) {
                registry.delete(association.getDestinationPath());
            }
        } catch (RegistryException e) {
            handleException("Failed to delete documentation", e);
        }
    }

    /**
     * Adds Documentation to an API
     *
     * @param apiId         APIIdentifier
     * @param documentation Documentation
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to add documentation
     */
    public void addDocumentation(APIIdentifier apiId, Documentation documentation)
            throws APIManagementException {
        createDocumentation(apiId, documentation);
    }

    /**
     * This method used to save the documentation content
     *
     * @param identifier,        API identifier
     * @param documentationName, name of the inline documentation
     * @param text,              content of the inline documentation
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to add the document as a resource to registry
     */
    public void addDocumentationContent(APIIdentifier identifier, String documentationName, String text)
            throws APIManagementException {

        String documentationPath = APIUtil.getAPIDocPath(identifier) + documentationName;
        String contentPath = APIUtil.getAPIDocPath(identifier) + APIConstants.INLINE_DOCUMENT_CONTENT_DIR +
                RegistryConstants.PATH_SEPARATOR + documentationName;
        try {
            Resource docContent = registry.newResource();
            docContent.setContent(text);
            registry.put(contentPath, docContent);
            registry.addAssociation(documentationPath, contentPath,
                    APIConstants.DOCUMENTATION_CONTENT_ASSOCIATION);
        } catch (RegistryException e) {
            String msg = "Failed to add the documentation content of : "
                    + documentationName + " of API :" + identifier.getApiName();
            handleException(msg, e);
        }
    }

    /**
     * Updates a given documentation
     *
     * @param apiId         APIIdentifier
     * @param documentation Documentation
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to update docs
     */
    public void updateDocumentation(APIIdentifier apiId, Documentation documentation)
            throws APIManagementException {
        createDocumentation(apiId, documentation);
    }

    /**
     * Copies current Documentation into another version of the same API.
     *
     * @param toVersion Version to which Documentation should be copied.
     * @param apiId     id of the APIIdentifier
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to copy docs
     */
    public void copyAllDocumentation(APIIdentifier apiId, String toVersion)
            throws APIManagementException {

        String oldVersion = APIUtil.getAPIDocPath(apiId);
        String newVersion = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                apiId.getProviderName() + RegistryConstants.PATH_SEPARATOR + apiId.getApiName() +
                RegistryConstants.PATH_SEPARATOR + toVersion + RegistryConstants.PATH_SEPARATOR +
                APIConstants.DOC_DIR;

        try {
            Resource resource = registry.get(oldVersion);
            if (resource instanceof org.wso2.carbon.registry.core.Collection) {
                String[] docsPaths = ((org.wso2.carbon.registry.core.Collection) resource).getChildren();
                for (String docPath : docsPaths) {
                    registry.copy(docPath, newVersion);
                }
            }
        } catch (RegistryException e) {
            handleException("Failed to copy docs to new version : " + newVersion, e);
        }

    }

    /**
     * Create an Api
     *
     * @param api API
     * @throws APIManagementException if failed to create API
     */
    private void createAPI(API api) throws APIManagementException {
        GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                APIConstants.API_KEY);
        try {
            GenericArtifact genericArtifact =
                    artifactManager.newGovernanceArtifact(new QName(api.getId().getApiName()));
            GenericArtifact artifact = APIUtil.createAPIArtifactContent(genericArtifact, api);
            artifactManager.addGenericArtifact(artifact);
            String artifactPath = GovernanceUtils.getArtifactPath(registry, artifact.getId());
            String providerPath = APIUtil.getAPIProviderPath(api.getId());
            //provider ------provides----> API
            registry.addAssociation(providerPath, artifactPath, APIConstants.PROVIDER_ASSOCIATION);
            Set<String> tagSet = api.getTags();
            if (tagSet != null && tagSet.size() > 0) {
                for (String tag : tagSet) {
                    registry.applyTag(artifactPath, tag);
                }
            }
            //Setting context name as property of API
            Resource resource = registry.get(artifactPath);
            resource.setProperty(APIConstants.API_CONTEXT_ID, api.getContext());
            registry.put(artifactPath, resource);
            //create the wsdl in registry . if  failed we ignore after logging the error.
            if (api.getWsdlUrl() != null && !"".equals(api.getWsdlUrl())) {
                String path = APIUtil.createWSDL(api.getWsdlUrl());
                registry.addAssociation(artifactPath, path, CommonConstants.ASSOCIATION_TYPE01);
            }
            if(api.getUrl() !=null && !"".equals(api.getUrl())){
                String path = APIUtil.createEndpoint(api.getUrl());
                registry.addAssociation(artifactPath, path, CommonConstants.ASSOCIATION_TYPE01);
            }
        } catch (RegistryException e) {
            handleException("Error while adding API", e);
        }
    }

    /**
     * Create a documentation
     *
     * @param apiId         APIIdentifier
     * @param documentation Documentation
     * @throws APIManagementException if failed to add documentation
     */
    private void createDocumentation(APIIdentifier apiId, Documentation documentation)
            throws APIManagementException {
        try {
            GenericArtifactManager artifactManager = new GenericArtifactManager(registry,
                    APIConstants.DOCUMENTATION_KEY);
            GenericArtifact artifact =
                    artifactManager.newGovernanceArtifact(new QName(documentation.getName()));
            artifactManager.addGenericArtifact(
                    APIUtil.createDocArtifactContent(artifact, apiId, documentation));
            String apiPath = APIUtil.getAPIPath(apiId);
            //Adding association from api to documentation . (API -----> doc)
            registry.addAssociation(apiPath, artifact.getPath(),
                    APIConstants.DOCUMENTATION_ASSOCIATION);
        } catch (RegistryException e) {
            handleException("Failed to add documentation", e);
        }
    }
}
