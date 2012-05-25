package org.wso2.carbon.registry.samples.handler.wadl.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.List;

public class ResourceProcessor {
    private Registry registry;

    public ResourceProcessor(Registry registry){
        this.registry = registry;
    }

    public String addResource(OMElement resourceElement, String basePath) throws JaxenException, RegistryException {
        String path;
        if((path = resourceElement.getAttribute(new QName("path")).getAttributeValue()).startsWith("/")){
            path = path.substring(1);
        }

        Registry governanceSystemRegistry =
                RegistryCoreServiceComponent.getRegistryService().getGovernanceSystemRegistry();
        GenericArtifactManager genericArtifactManager =
                new GenericArtifactManager(governanceSystemRegistry, "wadlresource");
        GenericArtifact resourceArtifact = genericArtifactManager.newGovernanceArtifact(
                new QName(path));
        resourceArtifact.addAttribute("overview_name", path);
        resourceArtifact.addAttribute("overview_base", basePath);

        Iterator<OMElement> methods = resourceElement.getChildrenWithLocalName("method");
        while(methods.hasNext()){
            OMElement method = methods.next();
            resourceArtifact.addAttribute("methods_entry",
                    method.getAttribute(new QName("name")).getAttributeValue() + ":" +
                            method.getAttribute(new QName("id")).getAttributeValue());
        }

        genericArtifactManager.addGenericArtifact(resourceArtifact);
        return GovernanceUtils.getArtifactPath(governanceSystemRegistry, resourceArtifact.getId());
    }
}
