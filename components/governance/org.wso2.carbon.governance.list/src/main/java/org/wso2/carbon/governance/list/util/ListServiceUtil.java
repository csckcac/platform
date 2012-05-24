/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.governance.list.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceFilter;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.list.beans.ServiceBean;
import org.wso2.carbon.governance.list.util.beans.ArtifactInfoBean;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.handlers.utils.ServiceUtils;
import org.wso2.carbon.user.core.UserStoreException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.wso2.carbon.governance.list.util.CommonUtil.buildServiceOMElement;
import static org.wso2.carbon.governance.list.util.CommonUtil.getVersionFromContent;

public class ListServiceUtil {
    private static final Log log = LogFactory.getLog(ListServiceUtil.class);
    private static final String REGISTRY_LC_NAME = "registry.LC.name";
    private static int poolSize = 10;

    private static ExecutorService preFetcher;
    private static final Map<String,ArtifactInfoBean> serviceListMap =
            Collections.synchronizedMap(new HashMap<String, ArtifactInfoBean>());

    public static Map<String, ArtifactInfoBean> getServiceListMap() {
        return serviceListMap;
    }

    public static String[] filterServices(String criteria, Registry governanceRegistry) throws RegistryException {
        try {
            ServiceManager serviceManger = new ServiceManager(governanceRegistry);
            final Service referenceService;
            if (criteria != null) {
                XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(criteria));
                StAXOMBuilder builder = new StAXOMBuilder(reader);
                OMElement referenceServiceElement = builder.getDocumentElement();
                referenceService = serviceManger.newService(referenceServiceElement);

            //ListServiceFilter listServiceFilter = new ListServiceFilter(referenceService);
            ServiceFilter listServiceFilter = new ServiceFilter() {
                GovernanceArtifactFilter filter = new GovernanceArtifactFilter(referenceService);
                public boolean matches(Service service) throws GovernanceException {
                    return filter.matches(service);
                }
            };
            Service[] services = serviceManger.findServices(listServiceFilter);

            List<String> servicePaths = new ArrayList<String>();
            if (services != null) {
                GovernanceUtils.setTenantGovernanceSystemRegistry(CarbonContext.getCurrentContext().getTenantId());
                for (Service service: services) {
                    String path = service.getPath();
                    if (path != null) {
                        servicePaths.add(path);
                    }
                }
                GovernanceUtils.unsetTenantGovernanceSystemRegistry();
            }
            return servicePaths.toArray(new String[servicePaths.size()]);
            }else {
                   return  serviceManger.getAllServicePaths();
            }
        } catch (Exception ignore) {
            String msg = "Error in filtering the services. " + ignore.getMessage();
            throw new RegistryException(msg);
        }
    }

    public static void startArtifactFetcher(Registry userRegistry) {
        try {
            if (preFetcher == null) {
                preFetcher = Executors.newFixedThreadPool(poolSize);
                Registry governanceRegistry = GovernanceUtils.getGovernanceSystemRegistry(userRegistry);

                String paths[] = ListServiceUtil.filterServices(null, governanceRegistry);

                int chunkSize = paths.length / poolSize;
                int mod = paths.length % poolSize;

                for (int i = 0; i < poolSize; i++) {
                    int start = i * chunkSize;
                    int end = start + chunkSize + mod;

                    if (end > paths.length) {
                        end = paths.length;
                    }

                    PreFetcher fetcher = new PreFetcher();
                    fetcher.setGovernanceRegistry(governanceRegistry);
                    fetcher.setPaths(Arrays.copyOfRange(paths, start, end));
                    preFetcher.execute(fetcher);
                }

            }
        } catch (RegistryException e) {
            log.error("Unable to get the list of services from the registry", e);
        }
    }

    public static void stopArtifactFetcher(){
        if(preFetcher != null){
            preFetcher.shutdown();
        }
    }

    public static void populateServiceInfoMap(Registry governanceRegistry,String[] paths,
                                              Map<String,ArtifactInfoBean> listMap){
        try {
            for (String path : paths) {
                if(!listMap.containsKey(path) && governanceRegistry.resourceExists(path)){
                    ArtifactInfoBean bean = new ArtifactInfoBean();
                    Resource resource = governanceRegistry.get(path);

                    String version = getVersionFromContent(buildServiceOMElement(resource));

                    bean.setName(CommonUtil.getServiceName(resource));
                    bean.setNameSpace(CommonUtil.getServiceNamespace(resource));
                    bean.setVersion(version);
                    if(resource.getProperty(REGISTRY_LC_NAME) != null){
                        String lcName = resource.getProperty(REGISTRY_LC_NAME);
                        bean.setLifecycleName(lcName);
                        bean.setLifecycleState(resource.getProperty("registry.lifecycle."+lcName+".state"));
                    }

                    listMap.put(path,bean);
                }
            }
        } catch (RegistryException e) {
            log.error("Unable to get the resource from the registry",e);
        }

    }
    
    public static ServiceBean fillServiceBean(UserRegistry registry,String criteria) throws RegistryException {
        
        ServiceBean bean = new ServiceBean();
        Resource resource;
        String[] path;
        List<String> modifiedList = ServiceUtils.getModifiedList();


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
        String[] LCName = new String[path.length];
        String[] LCState = new String[path.length];
        String[] version = new String[path.length];
        boolean[] canDelete = new boolean[path.length];
        for (int i = 0; i < path.length; i++) {
            bean.increment();

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

            if (serviceListMap.containsKey(path[i])) {
                if (!modifiedList.contains(path[i])) {
                    ArtifactInfoBean artifactInfoBean = serviceListMap.get(path[i]);
                    name[i] = artifactInfoBean.getName();
                    namespace[i] = artifactInfoBean.getNameSpace();
                    version[i] = artifactInfoBean.getVersion();
                    LCName[i] = artifactInfoBean.getLifecycleName() == null ? "" : artifactInfoBean.getLifecycleName();
                    LCState[i] = artifactInfoBean.getLifecycleState() == null ? "" : artifactInfoBean.getLifecycleState();
                    continue;
                } else {
                    modifiedList.remove(path[i]);
                }
            }

            resource = registry.get(path[i]);
            version[i] = CommonUtil.getVersionFromContent(CommonUtil.buildServiceOMElement(resource));
            name[i] = CommonUtil.getServiceName(resource);
            namespace[i] = CommonUtil.getServiceNamespace(resource);
            LCName[i] = CommonUtil.getLifeCycleName(resource);
            LCState[i] = CommonUtil.getLifeCycleState(resource);

            ArtifactInfoBean artifactInfoBean = new ArtifactInfoBean();
            artifactInfoBean.setName(name[i]);
            artifactInfoBean.setNameSpace(namespace[i]);
            artifactInfoBean.setLifecycleName(LCName[i]);
            artifactInfoBean.setLifecycleState(LCState[i]);
            artifactInfoBean.setVersion(version[i]);

            serviceListMap.put(path[i], artifactInfoBean);
        }
        bean.setDefaultServicePath(defaultServicePath);
        bean.setNames(name);
        bean.setNamespace(namespace);
        bean.setPath(path);
        bean.setLCName(LCName);
        bean.setLCState(LCState);
        bean.setCanDelete(canDelete);
        bean.setVersion(version);
        sortServicesByName(bean);
        return bean;
    }

    /**
     * Sorts the services by name
     *
     * @param bean**/
    private static void sortServicesByName(ServiceBean bean) {

        List<ServiceEntry> serviceEntryList = new ArrayList<ServiceEntry>();
        for(int i=0; i < bean.getPath().length; i++) {
            serviceEntryList.add( new ServiceEntry(bean.getPath()[i], bean.getNames()[i], bean.getNamespace()[i],
                    bean.getLCName()[i],bean.getLCState()[i],bean.getVersion()[i],bean.getCanDelete()[i]));
        }

        Collections.sort(serviceEntryList, new Comparator<ServiceEntry>() {
            public int compare(ServiceEntry se1, ServiceEntry se2) {
                int res = StringComparatorUtil.compare(se2.name, se1.name);
                if (res != 0) return res;

//                return StringComparatorUtil.compare(RegistryUtils.getResourceName(RegistryUtils.getParentPath(se1.path)),
//                        (RegistryUtils.getResourceName(RegistryUtils.getParentPath(se2.path))));
                return StringComparatorUtil.compare(se1.version, se2.version);
            }
        });

        int i = 0;
        for(ServiceEntry se : serviceEntryList) {
            bean.getPath()[i] = se.path;
            bean.getNames()[i] = se.name;
            bean.getNamespace()[i] = se.namespace;
            bean.getLCName()[i]=se.lcname;
            bean.getLCState()[i]=se.lcstate;
            bean.getCanDelete()[i] = se.canDelete;
            bean.getVersion()[i] = se.version;
            i++;
        }

    }
    /**
     * This is a structure used to store a service entry prior to sorting**/
    private static class ServiceEntry {
        private String path,
                name,
                namespace,
                lcname,
                lcstate,
                version;
        private boolean canDelete;
        ServiceEntry(String path, String name, String namespace,String lcname,String lcstate,String version, boolean canDelete) {
            this.path = path;
            this.name = name;
            this.namespace = namespace;
            this.lcname=lcname;
            this.lcstate=lcstate;
            this.canDelete = canDelete;
            this.version = version;
        }
    }
}
