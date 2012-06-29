package org.wso2.carbon.governance.registry.extensions.executors.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Utils {
    private static final Log log = LogFactory.getLog(Utils.class);

    public static String formatPath(String path){
        if(path.startsWith(RegistryConstants.PATH_SEPARATOR)){
            path = path.substring(1);
        }
        if(path.endsWith(RegistryConstants.PATH_SEPARATOR)){
            path = path.substring(0,path.length() -1);
        }
        return path;
    }
    public static void addNewId(Registry registry, Resource newResource, String newPath) throws RegistryException {
        String artifactID = UUID.randomUUID().toString();
        newResource.setUUID(artifactID);
//        CommonUtil.addGovernanceArtifactEntryWithAbsoluteValues(registry, artifactID, newPath);
    }

    public static String getResourceContent(Resource tempResource) throws RegistryException {
        if (tempResource.getContent() instanceof String) {
            return (String) tempResource.getContent();
        } else if (tempResource.getContent() instanceof byte[]) {
            return RegistryUtils.decodeBytes((byte[]) tempResource.getContent());
        }
        return null;
    }

    public static OMElement getServiceOMElement(Resource newResource) {
        try {
            String content = getResourceContent(newResource);
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(
                    new StringReader(content));
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            OMElement serviceElement = builder.getDocumentElement();
            serviceElement.build();
            return serviceElement;
        } catch (Exception e) {
            log.error("Error in parsing the resource content");
        }
        return null;
    }

    public static boolean populateParameterMap(RequestContext requestContext, Map<String, String> currentParameterMap) {
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

    public static void copyAssociations(Registry registry, String newPath, String path) throws RegistryException {
        Association[] associations = registry.getAllAssociations(path);
        for (Association association : associations) {
            if (!association.getAssociationType().equals(CommonConstants.DEPENDS)) {
                if (association.getSourcePath().equals(path)) {
                    registry.addAssociation(newPath,
                            association.getDestinationPath(), association.getAssociationType());
                } else {
                    registry.addAssociation(association.getSourcePath(), newPath,
                            association.getAssociationType());
                }
            }
        }
    }

    public static void copyRatings(Registry registry, String newPath, String path) throws RegistryException {
        float averageRating = registry.getAverageRating(path);
        registry.rateResource(newPath,
                new Float(averageRating).intValue());
    }

    public static void copyTags(Registry registry, String newPath, String path) throws RegistryException {
        Tag[] tags = registry.getTags(path);
        for (Tag tag : tags) {
            registry.applyTag(newPath, tag.getTagName());
        }
    }

    public static void copyComments(Registry registry, String newPath, String path) throws RegistryException {
        Comment[] comments = registry.getComments(path);
        for (Comment comment : comments) {
            registry.addComment(newPath, comment);
        }
    }

}
