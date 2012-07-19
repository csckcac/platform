/*
 * Copyright WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.url.mapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.url.mapper.data.MappingData;
import org.wso2.carbon.url.mapper.internal.exception.UrlMapperException;
import org.wso2.carbon.url.mapper.internal.util.HostUtil;
import org.wso2.carbon.url.mapper.internal.util.UrlMapperConstants;

import java.util.List;

/**
 * Backend service to handle virtual host addition to registry and to tomcat.
 */
public class UrlMapperAdminService extends AbstractAdmin {
    private static final Log log = LogFactory.getLog(UrlMapperAdminService.class);

    public void addWebAppToHost(String hostName, String uri) throws UrlMapperException {
        //TODO have to figure out exception handling
        try {
            hostName = hostName + UrlMapperConstants.HostProperties.DOMAIN_NAME_PREFIX;
            HostUtil.addWebAppToHost(hostName, uri);
        } catch (Exception e) {
            log.error(e);  //To change body of catch statement use File | Settings | File Templates.
            throw new UrlMapperException("Failed to add webapp to host ", e);
        }
    }

    public static MappingData[] getAllMappings() throws UrlMapperException {
        return HostUtil.getAllMappingsFromRegistry();
    }

    public void addServiceDomain(String hostName, String url) throws UrlMapperException {
        hostName = hostName + UrlMapperConstants.HostProperties.DOMAIN_NAME_PREFIX;
        HostUtil.addDomainToServiceEpr(hostName, url);
    }

    public void editServiceDomain(String newHost, String oldhost) throws UrlMapperException {
        newHost = newHost + UrlMapperConstants.HostProperties.DOMAIN_NAME_PREFIX;
        HostUtil.updateEprToRegistry(newHost, oldhost);
    }

    public void deleteServiceDomain(String hostName) throws UrlMapperException {
        HostUtil.deleteResourceToRegistry(hostName);
    }

    public String[] getHostForEpr(String url) throws UrlMapperException {
        List<String> domains = HostUtil.getMappingsPerEppr(url);
        return domains.toArray(new String[domains.size()]);
    }

    public String[] getHostForWebApp(String url) throws UrlMapperException {
        List<String> domains = HostUtil.getMappingsPerWebApp(url);
        return domains.toArray(new String[domains.size()]);
    }

    public boolean editHost(String webappName, String newHost, String oldhost) throws UrlMapperException {
        newHost = newHost + UrlMapperConstants.HostProperties.DOMAIN_NAME_PREFIX;
        HostUtil.editHostInEngine(webappName, newHost, oldhost);
        return true;
    }

    public void deleteHost(String hostName) throws UrlMapperException {
        HostUtil.removeHost(hostName);
        HostUtil.deleteResourceToRegistry(hostName);
    }

    public boolean isMappingExist(String mappingName) throws UrlMapperException {
        return HostUtil.isMappingExist(mappingName);
    }
}
