package org.wso2.carbon.governance.registry.extensions.executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.registry.extensions.interfaces.Execution;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourcePath;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CopyExecutor implements Execution {
    private static final Log log = LogFactory.getLog(ServiceVersionExecutor.class);
    private static final String CURRENT_ENVIRONMENT = "currentEnvironment";
    private static final String TARGET_ENVIRONMENT = "targetEnvironment";
    private static final String KEY = "{@version}";
    private Map parameterMap;

    public void init(Map map) {
        parameterMap = map;
    }

    public boolean execute(RequestContext requestContext, String currentState, String targetState) {

        String resourcePath = requestContext.getResource().getPath();
        String newPath = "";

//        Now we are going to get the list of parameters from the context and add it to a map
        Map<String, String> currentParameterMap = new HashMap<String, String>();

//        Here we are populating the parameter map that was given from the UI
        if (!populateParameterMap(requestContext, currentParameterMap)) {
            log.error("Failed to populate the parameter map");
            return false;
        }

        String currentEnvironment = getReformattedPath((String) parameterMap.get(CURRENT_ENVIRONMENT),
                KEY, currentParameterMap.get(resourcePath));
        String targetEnvironment = getReformattedPath((String) parameterMap.get(TARGET_ENVIRONMENT),
                KEY, currentParameterMap.get(resourcePath));

        if(resourcePath.startsWith(currentEnvironment)){
            newPath = resourcePath.substring(currentEnvironment.length());
            newPath = targetEnvironment + newPath;
        }else{
            log.warn("Resource is not in the given environment");
            return true;
        }

        try {
            requestContext.getRegistry().copy(resourcePath,newPath);
            Resource newResource = requestContext.getRegistry().get(newPath);

            requestContext.setResource(newResource);
            requestContext.setResourcePath(new ResourcePath(newPath));

            return true;
        } catch (RegistryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return false;
        }

    }
    public String getReformattedPath(String originalPath, String key, String value){
        if(key == null || value == null){
            return originalPath;
        }
        return originalPath.replace(key,value);
    }

    public boolean populateParameterMap(RequestContext requestContext, Map<String, String> currentParameterMap) {
        Set parameterMapKeySet = (Set) requestContext.getProperty("parameterNames");
        if (parameterMapKeySet == null) {
            log.warn("No parameters where found");
            return true;
        }
        for (Object entry : parameterMapKeySet) {
            String key = (String) entry;
            if (!key.equals("preserveOriginal")) {
                currentParameterMap.put(key, (String) requestContext.getProperty(key));
            }
        }
        return true;
    }
}

