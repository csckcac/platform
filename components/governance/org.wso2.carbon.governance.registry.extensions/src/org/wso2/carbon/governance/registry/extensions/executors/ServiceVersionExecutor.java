package org.wso2.carbon.governance.registry.extensions.executors;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.registry.extensions.aspects.DefaultLifeCycle;
import org.wso2.carbon.governance.registry.extensions.aspects.utils.StatCollection;
import org.wso2.carbon.governance.registry.extensions.interfaces.Execution;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;

import java.util.*;

import static org.wso2.carbon.governance.registry.extensions.executors.utils.Utils.*;
import static org.wso2.carbon.registry.extensions.utils.CommonUtil.setServiceVersion;

public class ServiceVersionExecutor implements Execution {
    private static final Log log = LogFactory.getLog(ServiceVersionExecutor.class);
    private static final String REGISTRY_LC_NAME = "registry.LC.name";
    private static final String TARGET_ENVIRONMENT = "targetEnvironment";
    private static final String CURRENT_ENVIRONMENT = "currentEnvironment";

    //    The parameters that can be passed to the class
    final String RESOURCE_NAME = "{@resourceName}";
    final String RESOURCE_PATH = "{@resourcePath}";
    final String RESOURCE_VERSION = "{@version}";

    //    from the old code
    private String serviceMediaType = "application/vnd.wso2-service+xml";

    private Map parameterMap = new HashMap();
    private List<String> otherDependencyList =  new ArrayList<String>();

    public void init(Map parameterMap) {
        //To change body of implemented methods use File | Settings | File Templates.
        this.parameterMap = parameterMap;

        if(parameterMap.get("service.mediatype")!= null){
            serviceMediaType = parameterMap.get("service.mediatype").toString();
        }

    }

    public boolean execute(RequestContext requestContext, String currentState, String targetState) {
//        for logging purposes
        StringBuilder historyInfoBuilder = new StringBuilder();

//        getting the necessary values from the request context
        Resource resource = requestContext.getResource();
        Registry registry = requestContext.getRegistry();
        String resourcePath = requestContext.getResourcePath().getPath();

        Map<String, String> currentParameterMap = new HashMap<String, String>();
        Map<String, String> newPathMappings;

//        Returning true since this executor is not compatible with collections
        if(resource instanceof Collection){
            return true;
        }
        else if(resource.getMediaType() == null || "".equals(resource.getMediaType().trim())){
            log.warn("The media-type of the resource '"+ resourcePath
                    +"' is undefined. Hence exiting the service version executor.");
            return true;
        }
        else if(!resource.getMediaType().equals(serviceMediaType)){
//            We have a generic copy executor to copy any resource type.
//            This executor is written for services.
//            If a resource other than a service comes here, then we simply return true
//            since we can not handle it using this executor.
            return true;
        }

//        Getting the target environment and the current environment from the parameter map.
        String targetEnvironment = (String) parameterMap.get(TARGET_ENVIRONMENT);
        String currentEnvironment = (String) parameterMap.get(CURRENT_ENVIRONMENT);

        if ((targetEnvironment == null || currentEnvironment == null) || (currentEnvironment.isEmpty()
                || targetEnvironment.isEmpty())) {
            log.warn("Current environment and the Target environment has not been defined to the state");
            /*
             *  Here we are returning true because the executor has been configured incorrectly
             *  We do NOT consider that as a execution failure
             *  Hence returning true here
             */
            return true;
        }

//        Here we are populating the parameter map that was given from the UI
        if (!populateParameterMap(requestContext, currentParameterMap)) {
            log.error("Failed to populate the parameter map");
            return false;
        }

        try {
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
                        Resource tempResource = registry.get(currentParameterMapEntry.getKey());

                        if (!(tempResource instanceof Collection) && tempResource.getMediaType() != null) {
                            updateNewPathMappings(tempResource.getMediaType(),currentEnvironment,targetEnvironment,
                                    newPathMappings,currentParameterMapEntry.getKey(),currentParameterMapEntry.getValue());
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
//                        Checking whether this resource is a service resource
//                        If so, then we handle it in a different way
                        if (tempResource.getMediaType().equals(serviceMediaType)) {
                            String newPath = newPathMappings.get(tempResource.getPath());
                            newResource = tempResource;
                            OMElement serviceElement = getServiceOMElement(newResource);
                            OMFactory fac = OMAbstractFactory.getOMFactory();
//                            Adding required fields at the top of the xml which will help to easily read in service side
                            Iterator it = serviceElement.getChildrenWithLocalName("newServicePath");
                            if (it.hasNext()) {
                                OMElement next = (OMElement) it.next();
                                next.setText(newPath);
                            } else {
                                OMElement operation = fac.createOMElement("newServicePath",
                                        serviceElement.getNamespace(), serviceElement);
                                operation.setText(newPath);
                            }
                            setServiceVersion(serviceElement, currentParameterMap.get(tempResource.getPath()));
//                            This is here to override the default path
                            serviceElement.build();
                            resourceContent = serviceElement.toString();
                            newResource.setContent(resourceContent);
                            addNewId(registry, newResource, newPath);
                            continue;
                        }
                        addNewId(registry, tempResource, newPathMappings.get(tempResource.getPath()));

//                        We add all the resources other than the original one here
                        if (!tempResource.getPath().equals(resourcePath)) {
//                            adding logs
                            historyInfoBuilder.append(newPathMappings.get(tempResource.getPath()))
                                    .append(" created. \n");
                            registry.put(newPathMappings.get(tempResource.getPath()), tempResource);
                        }
                    }
                }

//                This is to handle the original resource and put it to the new path
                registry.put(newPathMappings.get(resourcePath), newResource);
            } finally {
                CommonUtil.releaseUpdateLock();
            }
//            Associating the new resource with the LC
            String aspectName = resource.getProperty(REGISTRY_LC_NAME);
            registry.associateAspect(newPathMappings.get(resourcePath)
                    , aspectName);

            makeAssociations(requestContext, currentParameterMap, newPathMappings);
            makeOtherAssociations(requestContext, newPathMappings, otherDependencyList);

//            keeping the old path due to logging purposes
            newResource.setProperty(DefaultLifeCycle.REGISTRY_LIFECYCLE_HISTORY_ORIGINAL_PATH,
                    resourcePath);
            historyInfoBuilder.append(newPathMappings.get(resourcePath)).append(" created. \n");

            requestContext.setResource(newResource);
            requestContext.setOldResource(resource);
            requestContext.setResourcePath(new ResourcePath(newPathMappings.get(resourcePath)));

//           adding logs
            StatCollection statCollection = (StatCollection) requestContext.getProperty(DefaultLifeCycle.STAT_COLLECTION);
            statCollection.addExecutors(this.getClass().getName(),historyInfoBuilder.toString());
        } catch (RegistryException e) {
            log.error("Failed to perform registry operation", e);
        }
        return true;
    }

    private void updateNewPathMappings(String mediaType,String currentExpression,String targetExpression,
                                       Map<String,String> newPathMappingsMap,String resourcePath,String version){
        boolean hasValue = false;
        if(parameterMap.containsKey(mediaType + ":" + CURRENT_ENVIRONMENT)){
            hasValue = true;
            currentExpression = (String) parameterMap.get(mediaType + ":" + CURRENT_ENVIRONMENT);
        }
        if(parameterMap.containsKey(mediaType + ":" + TARGET_ENVIRONMENT)){
            hasValue = true;
            targetExpression = (String) parameterMap.get(mediaType + ":" + TARGET_ENVIRONMENT);
        }
        if (hasValue) {
            String path = reformatPath(resourcePath,currentExpression,targetExpression,version);
            newPathMappingsMap.put(resourcePath,path);
        }
    }

    private String updateRelativePaths(String targetEnvironment, String currentEnvironment, String resourceContent,
                                       Map.Entry<String, String> newPathMappingsEntry) {
        StringBuilder sourceBuffer = new StringBuilder();
        StringBuilder targetBuffer = new StringBuilder();

        String prefix = currentEnvironment.substring(0, currentEnvironment.indexOf(RESOURCE_PATH));
        String pathSuffix = (newPathMappingsEntry.getKey()).replace(prefix, "");

        String targetPrefix = targetEnvironment.substring(0, targetEnvironment.indexOf(RESOURCE_PATH));
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
        }else{
            resourceContent = replacePathRecursively(resourceContent, pathSuffix,
                    replacementValue);
        }
        return resourceContent;
    }

    private String replacePathRecursively(String content,String value,String replacement) {
        String suffix = value.substring(value.indexOf(RegistryConstants.PATH_SEPARATOR) + 1);
        String replacementSuffix = replacement.replace(value.substring(0,
                value.indexOf(RegistryConstants.PATH_SEPARATOR) + 1),"");
        if(suffix.lastIndexOf(RegistryConstants.PATH_SEPARATOR) <= 0){
            return content;
        }
        if(content.contains(suffix)){
            return content.replace(suffix,replacementSuffix);
        }else{
            replacePathRecursively(content,suffix,replacementSuffix);
        }
        return content;
    }

    private Map<String,String> getNewPathMappings(String targetEnvironment, String currentEnvironment
            , Map<String, String> currentParameterMap) {

        Map<String, String> newPathMappingsMap = new HashMap<String, String>();

        for (Map.Entry<String, String> keyValueSet : currentParameterMap.entrySet()) {
            String path = reformatPath(keyValueSet.getKey(), currentEnvironment, targetEnvironment,
                    keyValueSet.getValue());
//                This condition is there to check whether we need to move the resources
//                The executor will not execute beyond this point, to all the resources that are not under the given environment prefix
            if(path.equals(keyValueSet.getKey())){
                log.info("Resource "+ path +" is not in the given environment");
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
                if(getServiceOMElement(requestContext.getResource()) != null){
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
        if(currentParameterMap.isEmpty()){
            if (serviceMediaType.equals(requestContext.getResource().getMediaType())) {
                if(getServiceOMElement(requestContext.getResource()) != null){
                    currentParameterMap.put(requestContext.getResource().getPath(),
                            org.wso2.carbon.registry.common.utils.CommonUtil.getServiceVersion(
                                    getServiceOMElement(requestContext.getResource())));
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
    private String reformatPath(String path, String currentExpression, String targetExpression,String newResourceVersion) {
        TreeMap<Integer,String> indexMap = new TreeMap<Integer, String>();
        
        String returnPath = targetExpression;
        String prefix;
        
        if (currentExpression.equals(targetExpression)) {
            return path;
        }
        indexMap.put(currentExpression.indexOf(RESOURCE_NAME),RESOURCE_NAME);
        indexMap.put(currentExpression.indexOf(RESOURCE_PATH),RESOURCE_PATH);
        indexMap.put(currentExpression.indexOf(RESOURCE_VERSION),RESOURCE_VERSION);

        String tempExpression = currentExpression;
        
        while (indexMap.lastKey() < tempExpression.lastIndexOf(RegistryConstants.PATH_SEPARATOR)){
            tempExpression = tempExpression.substring(0,tempExpression.lastIndexOf(RegistryConstants.PATH_SEPARATOR));
            path = path.substring(0,path.lastIndexOf(RegistryConstants.PATH_SEPARATOR));
        }

        prefix = currentExpression.substring(0,currentExpression.indexOf(indexMap.get(indexMap.higherKey(-1))));

        if(!path.startsWith(prefix)){
            return path;
        }
        path = path.replace(prefix,"");

        while (true){
            if(indexMap.firstKey() < 0){
                indexMap.pollFirstEntry();
            }else {
                break;
            }
        }

        while (true){
            if(indexMap.size() == 0){
                break;
            }
            Map.Entry lastEntry = indexMap.pollLastEntry();
            if(lastEntry.getValue().equals(RESOURCE_PATH)){
                String pathValue = path;

                for (int i = 0; i < indexMap.size(); i++) {
                     pathValue = formatPath(pathValue.substring(pathValue.indexOf(RegistryConstants.PATH_SEPARATOR)));
                }

                returnPath = returnPath.replace(RESOURCE_PATH,formatPath(pathValue));
                path = path.replace(pathValue,"");
                continue;
            }
            if(lastEntry.getValue().equals(RESOURCE_VERSION)){
                returnPath = returnPath.replace(RESOURCE_VERSION,newResourceVersion);
                path = path.substring(0,path.lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1);
                continue;
            }
            returnPath = returnPath.replace((String) lastEntry.getValue(),
                    formatPath(path.substring(path.lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1)));
            path = path.substring(0,path.lastIndexOf(RegistryConstants.PATH_SEPARATOR));
        }

        if (returnPath.contains(RESOURCE_VERSION)) {
            return returnPath.replace(RESOURCE_VERSION, newResourceVersion);
        }
        return returnPath;
    }

    /*
    * This method creates the associations between the new resource and its new dependant resource.
    * */
    private void makeAssociations(RequestContext requestContext, Map<String, String> parameterMap
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
                    if(!(oldPathNewPathMap.containsValue(association.getSourcePath()))
                            || !(oldPathNewPathMap.containsValue(association.getDestinationPath()))){
                        registry.removeAssociation(association.getSourcePath(),association.getDestinationPath()
                                ,association.getAssociationType());
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

    private void makeOtherAssociations(RequestContext requestContext, Map<String, String> oldPathNewPathMap
            , List<String> otherDependencies) throws RegistryException {

        Registry registry = requestContext.getRegistry();

        for (Map.Entry<String, String> entry : oldPathNewPathMap.entrySet()) {
            Association[] associations = registry.getAllAssociations(entry.getKey());

            for (Association association : associations) {
                for (String dependency : otherDependencies) {
                    if(association.getDestinationPath().equals(dependency)){
                        registry.addAssociation(entry.getValue(),dependency,association.getAssociationType());
                    }
                    if(association.getSourcePath().equals(dependency)){
                        registry.addAssociation(dependency,entry.getValue(),association.getAssociationType());
                    }

                }
            }


        }
    }
}
