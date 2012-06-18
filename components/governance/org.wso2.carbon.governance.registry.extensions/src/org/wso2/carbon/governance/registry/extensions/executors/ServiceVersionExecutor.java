package org.wso2.carbon.governance.registry.extensions.executors;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.registry.extensions.aspects.utils.LifecycleConstants;
import org.wso2.carbon.governance.registry.extensions.aspects.utils.StatCollection;
import org.wso2.carbon.governance.registry.extensions.executors.utils.ExecutorConstants;
import org.wso2.carbon.governance.registry.extensions.executors.utils.Utils;
import org.wso2.carbon.governance.registry.extensions.interfaces.Execution;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;

import javax.xml.stream.XMLStreamException;
import java.util.*;

import static org.wso2.carbon.governance.registry.extensions.aspects.utils.Utils.getHistoryInfoElement;
import static org.wso2.carbon.governance.registry.extensions.executors.utils.Utils.*;
import static org.wso2.carbon.registry.extensions.utils.CommonUtil.setServiceVersion;

public class ServiceVersionExecutor implements Execution {
    private static final Log log = LogFactory.getLog(ServiceVersionExecutor.class);

    //    from the old code
    private String serviceMediaType = "application/vnd.wso2-service+xml";

    //    To track whether we need to move comments,tags,ratings and all the associations.
    private boolean copyComments = false;
    private boolean copyTags = false;
    private boolean copyRatings = false;
    private boolean copyAllAssociations = false;
    private boolean copyDependencies = true;
    private boolean override = false;

    private Map parameterMap = new HashMap();
    private List<String> otherDependencyList = new ArrayList<String>();

    private OMElement historyOperation = null;

    public void init(Map parameterMap) {
        //To change body of implemented methods use File | Settings | File Templates.
        this.parameterMap = parameterMap;

        if (parameterMap.get(ExecutorConstants.SERVICE_MEDIATYPE) != null) {
            serviceMediaType = parameterMap.get(ExecutorConstants.SERVICE_MEDIATYPE).toString();
        }
        if (parameterMap.get(ExecutorConstants.COPY_COMMENTS) != null) {
            copyComments = Boolean.parseBoolean((String) parameterMap.get(ExecutorConstants.COPY_COMMENTS));
        }
        if (parameterMap.get(ExecutorConstants.COPY_TAGS) != null) {
            copyTags = Boolean.parseBoolean((String) parameterMap.get(ExecutorConstants.COPY_TAGS));
        }
        if (parameterMap.get(ExecutorConstants.COPY_RATINGS) != null) {
            copyRatings = Boolean.parseBoolean((String) parameterMap.get(ExecutorConstants.COPY_RATINGS));
        }
        if (parameterMap.get(ExecutorConstants.COPY_ASSOCIATIONS) != null) {
            copyAllAssociations = Boolean.parseBoolean((String) parameterMap.get(ExecutorConstants.COPY_ASSOCIATIONS));
        }
        if (parameterMap.get(ExecutorConstants.COPY_DEPENDENCIES) != null) {
            copyDependencies = Boolean.parseBoolean((String) parameterMap.get(ExecutorConstants.COPY_DEPENDENCIES));
        }
        if (parameterMap.get(ExecutorConstants.OVERRIDE) != null) {
            override = Boolean.parseBoolean((String) parameterMap.get(ExecutorConstants.OVERRIDE));
        }

    }

    public boolean execute(RequestContext requestContext, String currentState, String targetState) {
//        To keep track of the registry transaction state
        boolean transactionStatus = false;

//        for logging purposes
        try {
            historyOperation = AXIOMUtil.stringToOM("<operation></operation>");
        } catch (XMLStreamException e) {
            log.error(e);
        }

//        getting the necessary values from the request context
        Resource resource = requestContext.getResource();
        Registry registry = requestContext.getRegistry();
        String resourcePath = requestContext.getResourcePath().getPath();

        Map<String, String> currentParameterMap = new HashMap<String, String>();
        Map<String, String> newPathMappings;

//        Returning true since this executor is not compatible with collections
        if (resource instanceof Collection) {
            return true;
        } else if (resource.getMediaType() == null || "".equals(resource.getMediaType().trim())) {
            log.warn("The media-type of the resource '" + resourcePath
                    + "' is undefined. Hence exiting the service version executor.");
            return true;
        } else if (!resource.getMediaType().equals(serviceMediaType)) {
//            We have a generic copy executor to copy any resource type.
//            This executor is written for services.
//            If a resource other than a service comes here, then we simply return true
//            since we can not handle it using this executor.
            return true;
        }

//        Getting the target environment and the current environment from the parameter map.
        String targetEnvironment = (String) parameterMap.get(ExecutorConstants.TARGET_ENVIRONMENT);
        String currentEnvironment = (String) parameterMap.get(ExecutorConstants.CURRENT_ENVIRONMENT);

        if ((targetEnvironment == null || currentEnvironment == null) || (currentEnvironment.isEmpty()
                || targetEnvironment.isEmpty())) {
            log.warn("Current environment and the Target environment has not been defined to the state");
//             Here we are returning true because the executor has been configured incorrectly
//             We do NOT consider that as a execution failure
//             Hence returning true here
            return true;
        }

//        Here we are populating the parameter map that was given from the UI
        if (!populateParameterMap(requestContext, currentParameterMap)) {
            log.error("Failed to populate the parameter map");
            return false;
        }

        try {
//            Starting a registry transaction
            registry.beginTransaction();

            Resource newResource = registry.newResource();
//            This loop is there to reformat the paths with the new versions.
            newPathMappings = getNewPathMappings(targetEnvironment, currentEnvironment, currentParameterMap);
//            Once the paths are updated with the new versions we do through the service resource and update the
//            content of the service resource with the new service version, wsdl path.
            if (!CommonUtil.isUpdateLockAvailable()) {
                return false;
            }
            CommonUtil.acquireUpdateLock();
            try {
//                Iterating through the list of dependencies
                for (Map.Entry<String, String> currentParameterMapEntry : currentParameterMap.entrySet()) {
                    if (registry.resourceExists(currentParameterMapEntry.getKey())) {
                        String newTempResourcePath;
                        Resource tempResource = registry.get(currentParameterMapEntry.getKey());

                        if (!(tempResource instanceof Collection) && tempResource.getMediaType() != null) {
                            updateNewPathMappings(tempResource.getMediaType(), currentEnvironment, targetEnvironment,
                                    newPathMappings, currentParameterMapEntry.getKey(), currentParameterMapEntry.getValue());
                        }

                        String resourceContent = getResourceContent(tempResource);

//                        Update resource content to reflect new paths
                        for (Map.Entry<String, String> newPathMappingsEntry : newPathMappings.entrySet()) {
                            if (resourceContent != null) {
                                if (resourceContent.contains(newPathMappingsEntry.getKey())) {
                                    resourceContent = resourceContent.replace(newPathMappingsEntry.getKey()
                                            , newPathMappingsEntry.getValue());
                                } else {
                                    resourceContent = updateRelativePaths(targetEnvironment, currentEnvironment,
                                            resourceContent, newPathMappingsEntry);
                                }
                            }
                        }
                        tempResource.setContent(resourceContent);
                        newTempResourcePath = newPathMappings.get(tempResource.getPath());

//                        Checking whether this resource is a service resource
//                        If so, then we handle it in a different way
                        if ((tempResource.getMediaType() != null)
                                && (tempResource.getMediaType().equals(serviceMediaType))) {
                            newResource = tempResource;
                            OMElement serviceElement = getServiceOMElement(newResource);
                            OMFactory fac = OMAbstractFactory.getOMFactory();
//                            Adding required fields at the top of the xml which will help to easily read in service side
                            Iterator it = serviceElement.getChildrenWithLocalName("newServicePath");
                            if (it.hasNext()) {
                                OMElement next = (OMElement) it.next();
                                next.setText(newTempResourcePath);
                            } else {
                                OMElement operation = fac.createOMElement("newServicePath",
                                        serviceElement.getNamespace(), serviceElement);
                                operation.setText(newTempResourcePath);
                            }
                            setServiceVersion(serviceElement, currentParameterMap.get(tempResource.getPath()));
//                            This is here to override the default path
                            serviceElement.build();
                            resourceContent = serviceElement.toString();
                            newResource.setContent(resourceContent);
                            addNewId(registry, newResource, newTempResourcePath);
                            continue;
                        }
                        addNewId(registry, tempResource, newTempResourcePath);

//                        We add all the resources other than the original one here
                        if (!tempResource.getPath().equals(resourcePath)) {
//                            adding logs
                            historyOperation.addChild(getHistoryInfoElement(newTempResourcePath + " created"));
                            registry.put(newTempResourcePath, tempResource);

//                            Here we move all the comments,tags,ratings and all associations based on the configuration
//                            These operations are done for the dependent resources here.
//                            Original resources comments,tags,ratings and associations are copied later.

//                            Copying comments
                            copyComments(registry, newTempResourcePath, tempResource.getPath());
//                            Copying tags
                            copyTags(registry, newTempResourcePath, tempResource.getPath());
//                            Copying ratings. We only copy the average ratings
                            copyRatings(requestContext.getSystemRegistry(), newTempResourcePath, tempResource.getPath());
//                            Copying all the associations.
//                            We avoid copying dependencies here because they are added to the new resources
                            copyAllAssociations(registry, newTempResourcePath, tempResource.getPath());
                        }
                    }
                }
//                We check whether there is a resource with the same name,namespace and version in this environment
//                if so, we make it return false based on override flag.
                if(registry.resourceExists(newPathMappings.get(resourcePath)) & !override){
//                    This means that we should not do this operation and we should fail this
                    String message = "A resource exists with the given verion";
                    requestContext.setProperty(LifecycleConstants.EXECUTOR_MESSAGE_KEY,message);
                    throw new RegistryException(message);
                }

//                This is to handle the original resource and put it to the new path
                registry.put(newPathMappings.get(resourcePath), newResource);
                historyOperation.addChild(getHistoryInfoElement(newPathMappings.get(resourcePath) + " created"));

            } finally {
                CommonUtil.releaseUpdateLock();
            }
//            Associating the new resource with the LC
            String aspectName = resource.getProperty(ExecutorConstants.REGISTRY_LC_NAME);
            registry.associateAspect(newPathMappings.get(resourcePath)
                    , aspectName);

            makeDependencies(requestContext, currentParameterMap, newPathMappings);
            makeOtherDependencies(requestContext, newPathMappings, otherDependencyList);


//           Here we are coping the comments,tags,rating and associations of the original resource

//           Copying comments
            copyComments(registry, newPathMappings.get(resourcePath), resourcePath);

//           Copying tags
            copyTags(registry, newPathMappings.get(resourcePath), resourcePath);

//           Copying ratings. We only copy the average ratings
            copyRatings(requestContext.getSystemRegistry(), newPathMappings.get(resourcePath), resourcePath);

//           Copying all the associations.
//           We avoid copying dependencies here because they are added to the new resources
            copyAllAssociations(registry, newPathMappings.get(resourcePath), resourcePath);

            addSubscriptionAvailableProperty(newResource);

            requestContext.setResource(newResource);
            requestContext.setOldResource(resource);
            requestContext.setResourcePath(new ResourcePath(newPathMappings.get(resourcePath)));

//           adding logs
            StatCollection statCollection = (StatCollection) requestContext.getProperty(LifecycleConstants.STAT_COLLECTION);

//            keeping the old path due to logging purposes
            newResource.setProperty(LifecycleConstants.REGISTRY_LIFECYCLE_HISTORY_ORIGINAL_PATH,
                    statCollection.getOriginalPath());
            statCollection.addExecutors(this.getClass().getName(), historyOperation);

            transactionStatus = true;
        } catch (RegistryException e) {
            log.error("Failed to perform registry operation", e);
            return false;
        }finally {
            try {
                if(transactionStatus){
                    registry.commitTransaction();
                }else{
                    registry.rollbackTransaction();
                }
            } catch (RegistryException e) {
                log.error("Unable to finish the transaction", e);
            }
        }
        return true;
    }

    private void addSubscriptionAvailableProperty(Resource newResource) throws RegistryException {
        newResource.setProperty(GovernanceConstants.REGISTRY_IS_ENVIRONMENT_CHANGE,"true");

    }

    private void copyAllAssociations(Registry registry, String newPath, String path) throws RegistryException {
        if (copyAllAssociations) {
            Utils.copyAssociations(registry, newPath, path);
            historyOperation.addChild(getHistoryInfoElement("All associations copied"));
        }
    }

    private void copyRatings(Registry registry, String newPath, String path) throws RegistryException {
        if (copyRatings) {
            Utils.copyRatings(registry, newPath, path);
            historyOperation.addChild(getHistoryInfoElement("Average rating copied"));
        }
    }

    private void copyTags(Registry registry, String newPath, String path) throws RegistryException {
        if (copyTags) {
            Utils.copyTags(registry, newPath, path);
            historyOperation.addChild(getHistoryInfoElement("Tags copied"));
        }
    }

    private void copyComments(Registry registry, String newPath, String path) throws RegistryException {
        if (copyComments) {
            Utils.copyComments(registry, newPath, path);
            historyOperation.addChild(getHistoryInfoElement("Comments copied"));
        }
    }

    private void updateNewPathMappings(String mediaType, String currentExpression, String targetExpression,
                                       Map<String, String> newPathMappingsMap, String resourcePath, String version) {
        boolean hasValue = false;
        if (parameterMap.containsKey(mediaType + ":" + ExecutorConstants.CURRENT_ENVIRONMENT)) {
            hasValue = true;
            currentExpression = (String) parameterMap.get(mediaType + ":" + ExecutorConstants.CURRENT_ENVIRONMENT);
        }
        if (parameterMap.containsKey(mediaType + ":" + ExecutorConstants.TARGET_ENVIRONMENT)) {
            hasValue = true;
            targetExpression = (String) parameterMap.get(mediaType + ":" + ExecutorConstants.TARGET_ENVIRONMENT);
        }
        if (hasValue) {
            String path = reformatPath(resourcePath, currentExpression, targetExpression, version);
            newPathMappingsMap.put(resourcePath, path);
        }
    }

    private String updateRelativePaths(String targetEnvironment, String currentEnvironment, String resourceContent,
                                       Map.Entry<String, String> newPathMappingsEntry) {
        StringBuilder sourceBuffer = new StringBuilder();
        StringBuilder targetBuffer = new StringBuilder();

        String prefix = currentEnvironment.substring(0, currentEnvironment.indexOf(ExecutorConstants.RESOURCE_PATH));
        String pathSuffix = (newPathMappingsEntry.getKey()).replace(prefix, "");

        String targetPrefix = targetEnvironment.substring(0, targetEnvironment.indexOf(ExecutorConstants.RESOURCE_PATH));
        String replacementValue = newPathMappingsEntry.getValue().replace(targetPrefix, "");

        prefix = prefix.substring(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH.length() + 1);
        targetPrefix = targetPrefix.substring(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH.length() + 1);

        int prefixPathSegments = prefix.split(RegistryConstants.PATH_SEPARATOR).length;
        int targetPrefixPathSegments = targetPrefix.split(RegistryConstants.PATH_SEPARATOR).length;

        for (int i = 1; i < prefixPathSegments; i++) {
            sourceBuffer.append(".." + RegistryConstants.PATH_SEPARATOR);
        }
        for (int i = 1; i < targetPrefixPathSegments; i++) {
            targetBuffer.append(".." + RegistryConstants.PATH_SEPARATOR);
        }

        pathSuffix = sourceBuffer.toString() + pathSuffix;
        replacementValue = targetBuffer.toString() + replacementValue;

        if (resourceContent.contains(pathSuffix)) {
            resourceContent = resourceContent.replace(pathSuffix, replacementValue);
        } else {
            resourceContent = replacePathRecursively(resourceContent, pathSuffix,
                    replacementValue);
        }
        return resourceContent;
    }

    private String replacePathRecursively(String content, String value, String replacement) {
        String suffix = value.substring(value.indexOf(RegistryConstants.PATH_SEPARATOR) + 1);
        String replacementSuffix = replacement.replace(value.substring(0,
                value.indexOf(RegistryConstants.PATH_SEPARATOR) + 1), "");
        if (suffix.lastIndexOf(RegistryConstants.PATH_SEPARATOR) <= 0) {
            return content;
        }
        if (content.contains(suffix)) {
            return content.replace(suffix, replacementSuffix);
        } else {
            replacePathRecursively(content, suffix, replacementSuffix);
        }
        return content;
    }

    private Map<String, String> getNewPathMappings(String targetEnvironment, String currentEnvironment
            , Map<String, String> currentParameterMap) {

        Map<String, String> newPathMappingsMap = new HashMap<String, String>();

        for (Map.Entry<String, String> keyValueSet : currentParameterMap.entrySet()) {
            String path = reformatPath(keyValueSet.getKey(), currentEnvironment, targetEnvironment,
                    keyValueSet.getValue());
//                This condition is there to check whether we need to move the resources
//                The executor will not execute beyond this point, to all the resources that are not under the given environment prefix
            if (path.equals(keyValueSet.getKey())) {
                log.info("Resource " + path + " is not in the given environment");
                otherDependencyList.add(path);
                continue;
            }
            newPathMappingsMap.put(keyValueSet.getKey(), path);
        }

        for (String otherDependency : otherDependencyList) {
            currentParameterMap.remove(otherDependency);
        }
        return newPathMappingsMap;
    }

    private boolean populateParameterMap(RequestContext requestContext, Map<String, String> currentParameterMap) {
        Set parameterMapKeySet = (Set) requestContext.getProperty("parameterNames");
        if (parameterMapKeySet == null) {
            if (serviceMediaType.equals(requestContext.getResource().getMediaType())) {
                if (getServiceOMElement(requestContext.getResource()) != null) {
                    currentParameterMap.put(requestContext.getResource().getPath(),
                            org.wso2.carbon.registry.common.utils.CommonUtil.getServiceVersion(
                                    getServiceOMElement(requestContext.getResource())));
                    return true;
                }
            }
            return false;
        }
        for (Object entry : parameterMapKeySet) {
            String key = (String) entry;
            if (!key.equals("preserveOriginal") && !key.endsWith(".item")) {
                currentParameterMap.put(key, (String) requestContext.getProperty(key));
            }
        }
        if (currentParameterMap.isEmpty()) {
            if (serviceMediaType.equals(requestContext.getResource().getMediaType())) {
                if (getServiceOMElement(requestContext.getResource()) != null) {
                    currentParameterMap.put(requestContext.getResource().getPath(),
                            org.wso2.carbon.registry.common.utils.CommonUtil.getServiceVersion(
                                    getServiceOMElement(requestContext.getResource())));

//                    add if any dependencies are available for this resource under the version of the service
                    if (copyDependencies) {
                        try {
                            Association[] associations =
                                    requestContext.getRegistry().getAllAssociations(requestContext.getResource().getPath());
                            if (associations != null && associations.length != 0) {
                                for (Association association : associations) {
                                    if (association.getAssociationType().equals(CommonConstants.DEPENDS)) {
                                        if (requestContext.getResource().getPath().equals(association.getSourcePath())) {
                                            currentParameterMap.put(association.getDestinationPath(),
                                                    org.wso2.carbon.registry.common.utils.CommonUtil.getServiceVersion(
                                                            getServiceOMElement(requestContext.getResource())));
                                        }
                                    }
                                }
                            }

                        } catch (RegistryException e) {
                            log.error(e);
                        }
                    }
                }
            }
        }
        return true;
    }

    /*
    * This method returns the target path. The target path is calculated from the given expression
    * When calculating the target path, we split the current path using the given current expression and then map the
    * path segments to the corresponding ones in the target path expression
    * */
    private String reformatPath(String path, String currentExpression, String targetExpression, String newResourceVersion) {
        TreeMap<Integer, String> indexMap = new TreeMap<Integer, String>();

        String returnPath = targetExpression;
        String prefix;

        if (currentExpression.equals(targetExpression)) {
            return path;
        }
        indexMap.put(currentExpression.indexOf(ExecutorConstants.RESOURCE_NAME), ExecutorConstants.RESOURCE_NAME);
        indexMap.put(currentExpression.indexOf(ExecutorConstants.RESOURCE_PATH), ExecutorConstants.RESOURCE_PATH);
        indexMap.put(currentExpression.indexOf(ExecutorConstants.RESOURCE_VERSION), ExecutorConstants.RESOURCE_VERSION);

        String tempExpression = currentExpression;

        while (indexMap.lastKey() < tempExpression.lastIndexOf(RegistryConstants.PATH_SEPARATOR)) {
            tempExpression = tempExpression.substring(0, tempExpression.lastIndexOf(RegistryConstants.PATH_SEPARATOR));
            path = path.substring(0, path.lastIndexOf(RegistryConstants.PATH_SEPARATOR));
        }

        prefix = currentExpression.substring(0, currentExpression.indexOf(indexMap.get(indexMap.higherKey(-1))));

        if (!path.startsWith(prefix)) {
            return path;
        }
        path = path.replace(prefix, "");

        while (true) {
            if (indexMap.firstKey() < 0) {
                indexMap.pollFirstEntry();
            } else {
                break;
            }
        }

        while (true) {
            if (indexMap.size() == 0) {
                break;
            }
            Map.Entry lastEntry = indexMap.pollLastEntry();
            if (lastEntry.getValue().equals(ExecutorConstants.RESOURCE_PATH)) {
                String pathValue = path;

                for (int i = 0; i < indexMap.size(); i++) {
//                    pathValue = formatPath(pathValue.substring(path.indexOf(RegistryConstants.PATH_SEPARATOR)));
                    pathValue = formatPath(pathValue.substring(pathValue.indexOf(RegistryConstants.PATH_SEPARATOR)));
                }

                if (!pathValue.equals("")) {
                    returnPath = returnPath.replace(ExecutorConstants.RESOURCE_PATH, formatPath(pathValue));
                    path = path.replace(pathValue, "");
                } else {
                    returnPath = returnPath.replace("/" + lastEntry.getValue(), "");
                }

                continue;
            }
            if (lastEntry.getValue().equals(ExecutorConstants.RESOURCE_VERSION)) {
                returnPath = returnPath.replace(ExecutorConstants.RESOURCE_VERSION, newResourceVersion);
                if (path.contains("/")) {
                    path = path.substring(0, path.lastIndexOf(RegistryConstants.PATH_SEPARATOR));
                } else {
                    path = "";
                }
                continue;
            }

            String tempPath;
            if (path.contains("/")) {
                tempPath = path.substring(path.lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1);
            } else {
                tempPath = path;
            }
            if (!tempPath.equals("")) {
                returnPath = returnPath.replace((String) lastEntry.getValue(), formatPath(tempPath));
                if (path.contains("/")) {
                    path = path.substring(0, path.lastIndexOf(RegistryConstants.PATH_SEPARATOR));
                } else {
                    path = "";
                }
            } else {
                returnPath = returnPath.replace("/" + lastEntry.getValue(), "");
                if (path.contains("/")) {
                    path = path.substring(0, path.lastIndexOf(RegistryConstants.PATH_SEPARATOR));
                }
            }

        }

        if (returnPath.contains(ExecutorConstants.RESOURCE_VERSION)) {
            return returnPath.replace(ExecutorConstants.RESOURCE_VERSION, newResourceVersion);
        }
        return returnPath;
    }

    /*
    * This method creates the associations between the new resource and its new dependant resource.
    * */
    private void makeDependencies(RequestContext requestContext, Map<String, String> parameterMap
            , Map<String, String> oldPathNewPathMap) throws RegistryException {

        Registry registry = requestContext.getRegistry();

        if (!CommonUtil.isAddingAssociationLockAvailable()) {
            return;
        }
        CommonUtil.acquireAddingAssociationLock();
        try {
            for (Map.Entry<String, String> entry : oldPathNewPathMap.entrySet()) {
                Association[] associations = registry.getAllAssociations(entry.getValue());
                for (Association association : associations) {
                    if (!(oldPathNewPathMap.containsValue(association.getSourcePath()))
                            || !(oldPathNewPathMap.containsValue(association.getDestinationPath()))) {
                        registry.removeAssociation(association.getSourcePath(), association.getDestinationPath()
                                , association.getAssociationType());
                    }
                }
            }
            for (Map.Entry<String, String> keyValueSet : parameterMap.entrySet()) {
                Association[] associations = registry.getAllAssociations(keyValueSet.getKey());
                for (Association association : associations) {
                    if (oldPathNewPathMap.containsKey(association.getDestinationPath())
                            && oldPathNewPathMap.containsKey(association.getSourcePath())) {
                        registry.addAssociation(
                                oldPathNewPathMap.get(association.getSourcePath())
                                , oldPathNewPathMap.get(association.getDestinationPath())
                                , association.getAssociationType());
                    }
                }
            }
        } finally {
            CommonUtil.releaseAddingAssociationLock();
        }
    }

    private void makeOtherDependencies(RequestContext requestContext, Map<String, String> oldPathNewPathMap
            , List<String> otherDependencies) throws RegistryException {

        Registry registry = requestContext.getRegistry();

        for (Map.Entry<String, String> entry : oldPathNewPathMap.entrySet()) {
            Association[] associations = registry.getAllAssociations(entry.getKey());

            for (Association association : associations) {
                for (String dependency : otherDependencies) {
                    if (association.getDestinationPath().equals(dependency)) {
                        registry.addAssociation(entry.getValue(), dependency, association.getAssociationType());
                    }
                    if (association.getSourcePath().equals(dependency)) {
                        registry.addAssociation(dependency, entry.getValue(), association.getAssociationType());
                    }

                }
            }
        }
    }
}