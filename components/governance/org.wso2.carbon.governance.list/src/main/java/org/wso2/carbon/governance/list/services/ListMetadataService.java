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
package org.wso2.carbon.governance.list.services;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.list.beans.PolicyBean;
import org.wso2.carbon.governance.list.beans.SchemaBean;
import org.wso2.carbon.governance.list.beans.ServiceBean;
import org.wso2.carbon.governance.list.beans.WSDLBean;
import org.wso2.carbon.governance.list.util.CommonUtil;
import org.wso2.carbon.governance.list.util.ListServiceUtil;
import org.wso2.carbon.governance.list.util.StringComparatorUtil;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.user.core.UserStoreException;

import javax.xml.stream.XMLStreamException;
import java.util.*;

public class ListMetadataService extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(ListMetadataService.class);
    private static final String REGISTRY_WSDL_TARGET_NAMESPACE = "registry.wsdl.TargetNamespace";
    private static final String REGISTRY_SCHEMA_TARGET_NAMESPACE = "targetNamespace";

    private static Map<String,String> namespaceMap;


    /**
     * This is a structure used to store a service entry prior to sorting**/
    private static class ServiceEntry {
        private String path,
                name,
                namespace;
        private boolean canDelete;
        ServiceEntry(String path, String name, String namespace, boolean canDelete) {
            this.path = path;
            this.name = name;
            this.namespace = namespace;
            this.canDelete = canDelete;
        }
    }

    public ServiceBean listservices(String criteria)throws RegistryException{
        RegistryUtils.recordStatistics(criteria);
        UserRegistry registry = (UserRegistry)getGovernanceUserRegistry();
        ServiceBean bean = new ServiceBean();
        Resource resource;
        String[] path;

        String defaultServicePath = RegistryUtils.getRelativePathToOriginal(registry.getRegistryContext().getServicePath()
                , RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);

        try {
            path = ListServiceUtil.filterServices(criteria, registry);
        } catch (RegistryException e) {
            log.error("An error occurred while obtaining the list of services.", e);
            path = new String[0];
        }
        String[] name = new String[path.length];
        String[] namespace = new String[path.length];
        boolean[] canDelete = new boolean[path.length];
        for(int i=0;i<path.length;i++){
            bean.increment();
            resource = registry.get(path[i]);

            if(path[i].startsWith(defaultServicePath)){
                try {
                    OMElement omElement = AXIOMUtil.stringToOM(new String((byte[]) resource.getContent()));
                    Iterator it = omElement.getChildrenWithLocalName("overview");
                    while (it.hasNext()) {
                        OMElement element = (OMElement) it.next();
                        Iterator iterator = element.getChildrenWithLocalName("version");
                        if (iterator.hasNext()) {
                            OMElement next = (OMElement) iterator.next();
                            path[i] = org.wso2.carbon.registry.extensions.utils.CommonUtil.computeServicePathWithVersion(path[i],next.getText());
                        }
                    }
                } catch (XMLStreamException e) {
                    log.error("Error parsing the resource content",e);
                }
            }
            name[i] = CommonUtil.getServiceName(resource);
            namespace[i] = CommonUtil.getServiceNamespace(resource);
            if (registry.getUserRealm() != null && registry.getUserName() != null) {
                try {
                    canDelete[i] =
                            registry.getUserRealm().getAuthorizationManager().isUserAuthorized(
                                    registry.getUserName(),
                                    RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + path[i],
                                    ActionConstants.DELETE);
                } catch (UserStoreException e) {
                    canDelete[i] = false;
                }
            } else {
                canDelete[i] = false;
            }
        }
        bean.setDefaultServicePath(defaultServicePath);
        bean.setNames(name);
        bean.setNamespace(namespace);
        bean.setPath(path);
        bean.setCanDelete(canDelete);
        sortServicesByName(bean);
        return bean;
    }

    /**
     * Sorts the services by name
     * **/
    private void sortServicesByName(ServiceBean bean) {

        List<ServiceEntry> serviceEntryList = new ArrayList<ServiceEntry>();
        for(int i=0; i < bean.getPath().length; i++) {
            serviceEntryList.add( new ServiceEntry(bean.getPath()[i], bean.getNames()[i], bean.getNamespace()[i], bean.getCanDelete()[i]));
        }

        Collections.sort(serviceEntryList, new Comparator<ServiceEntry>() {
            public int compare(ServiceEntry se1, ServiceEntry se2) {
                int res = StringComparatorUtil.compare(se1.name, se2.name);
                if(res != 0) return res;

                return StringComparatorUtil.compare(RegistryUtils.getResourceName(RegistryUtils.getParentPath(se1.path)),
                        ( RegistryUtils.getResourceName(RegistryUtils.getParentPath(se2.path))));
            }
        });

        int i = 0;
        for(ServiceEntry se : serviceEntryList) {
            bean.getPath()[i] = se.path;
            bean.getNames()[i] = se.name;
            bean.getNamespace()[i] = se.namespace;
            bean.getCanDelete()[i] = se.canDelete;
            i++;
        }

    }

    public WSDLBean listwsdls()throws RegistryException{
        RegistryUtils.recordStatistics();
        WSDLBean bean = new WSDLBean();
        UserRegistry registry = (UserRegistry)getGovernanceUserRegistry();
        String[] path;
        try {
            path = GovernanceUtils.findGovernanceArtifacts(RegistryConstants.WSDL_MEDIA_TYPE,
                    registry);
        } catch (RegistryException e) {
            log.error("An error occurred while obtaining the list of WSDLs.", e);
            path = new String[0];
        }
        String[] name = new String[path.length];
        String[] namespaces = new String[path.length];
        boolean[] canDelete = new boolean[path.length];
        for(int i=0;i<path.length;i++){
            bean.increment();
            name[i] = CommonUtil.getResourceName(path[i]);

            String[] pathSegments = path[i].split("/" + CommonConstants.SERVICE_VERSION_REGEX.substring(1, +
                    CommonConstants.SERVICE_VERSION_REGEX.length() - 1));

            if(namespaceMap == null){
                namespaceMap = new HashMap<String, String>();
            }

            if(pathSegments[0].endsWith(name[i])){
                pathSegments[0] = pathSegments[0].substring(0,pathSegments[0].lastIndexOf("/"));
            }

            if (namespaceMap.containsKey(pathSegments[0] + registry.getTenantId())) {
                namespaces[i] = namespaceMap.get(pathSegments[0] + registry.getTenantId());
            } else {
                Resource resource = registry.get(path[i]);
                namespaces[i] = resource.getProperty(REGISTRY_WSDL_TARGET_NAMESPACE);
                namespaceMap.put(pathSegments[0] + registry.getTenantId(), namespaces[i]);
            }

            if (registry.getUserRealm() != null && registry.getUserName() != null) {
                try {
                    canDelete[i] =
                            registry.getUserRealm().getAuthorizationManager().isUserAuthorized(
                                    registry.getUserName(),
                                    RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + path[i],
                                    ActionConstants.DELETE);
                } catch (UserStoreException e) {
                    canDelete[i] = false;
                }
            } else {
                canDelete[i] = false;
            }
        }
        bean.setName(name);
        bean.setNamespace(namespaces);
        bean.setPath(path);
        bean.setCanDelete(canDelete);
        return bean;
    }
    public PolicyBean listpolicies()throws RegistryException{
        RegistryUtils.recordStatistics();
        PolicyBean bean = new PolicyBean();
        UserRegistry registry = (UserRegistry)getGovernanceUserRegistry();
        String[] path;
        try {
            path = GovernanceUtils.findGovernanceArtifacts(RegistryConstants.POLICY_MEDIA_TYPE,
                    registry);
        } catch (RegistryException e) {
            log.error("An error occurred while obtaining the list of policies.", e);
            path = new String[0];
        }
        String[] name = new String[path.length];
        boolean[] canDelete = new boolean[path.length];
        for(int i=0;i<path.length;i++){
            bean.increment();
            name[i] = CommonUtil.getResourceName(path[i]);
            if (registry.getUserRealm() != null && registry.getUserName() != null) {
                try {
                    canDelete[i] =
                            registry.getUserRealm().getAuthorizationManager().isUserAuthorized(
                                    registry.getUserName(),
                                    RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + path[i],
                                    ActionConstants.DELETE);
                } catch (UserStoreException e) {
                    canDelete[i] = false;
                }
            } else {
                canDelete[i] = false;
            }
        }
        bean.setName(name);
        bean.setPath(path);
        bean.setCanDelete(canDelete);
        return bean;
    }
    public SchemaBean listschema()throws RegistryException{
        RegistryUtils.recordStatistics();
        SchemaBean bean = new SchemaBean();
        UserRegistry registry = (UserRegistry)getGovernanceUserRegistry();
        String[] path;
        try {
            path = GovernanceUtils.findGovernanceArtifacts(RegistryConstants.XSD_MEDIA_TYPE,
                    registry);
        } catch (RegistryException e) {
            log.error("An error occurred while obtaining the list of schemas.", e);
            path = new String[0];
        }
        String[] name = new String[path.length];
        String[] namespace = new String[path.length];
        boolean[] canDelete = new boolean[path.length];

        for(int i=0;i<path.length;i++){
            bean.increment();

            name[i] = CommonUtil.getResourceName(path[i]);

            String[] pathSegments = path[i].split("/" + CommonConstants.SERVICE_VERSION_REGEX.substring(1, +
                    CommonConstants.SERVICE_VERSION_REGEX.length() - 1));

            if (namespaceMap == null) {
                namespaceMap = new HashMap<String, String>();
            }

            if(pathSegments[0].endsWith(name[i])){
                pathSegments[0] = pathSegments[0].substring(0,pathSegments[0].lastIndexOf("/"));
            }

            if (namespaceMap.containsKey(pathSegments[0] + registry.getTenantId())) {
                namespace[i] = namespaceMap.get(pathSegments[0] + registry.getTenantId());
            } else {
                Resource resource = registry.get(path[i]);
                namespace[i] = resource.getProperty(REGISTRY_SCHEMA_TARGET_NAMESPACE);
                namespaceMap.put(pathSegments[0] + registry.getTenantId(), namespace[i]);
            }

            if (registry.getUserRealm() != null && registry.getUserName() != null) {
                try {
                    canDelete[i] =
                            registry.getUserRealm().getAuthorizationManager().isUserAuthorized(
                                    registry.getUserName(),
                                    RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + path[i],
                                    ActionConstants.DELETE);
                } catch (UserStoreException e) {
                    canDelete[i] = false;
                }
            } else {
                canDelete[i] = false;
            }
        }
        bean.setName(name);
        bean.setNamespace(namespace);
        bean.setPath(path);
        bean.setCanDelete(canDelete);
        return bean;
    }
}
