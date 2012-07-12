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
package org.wso2.carbon.url.mapper.internal.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardHost;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.ApplicationContext;
import org.wso2.carbon.tomcat.api.CarbonTomcatService;
import org.wso2.carbon.tomcat.ext.valves.CarbonContextCreatorValve;
import org.wso2.carbon.tomcat.ext.valves.CompositeValve;
import org.wso2.carbon.url.mapper.data.MappingData;
import org.wso2.carbon.url.mapper.internal.exception.UrlMapperException;
import org.wso2.carbon.url.mapper.internal.registry.RegistryManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * util class which is doing add host to engine and getting resources from the
 * registry.
 */
public class HostUtil {
    private static final Log log = LogFactory.getLog(HostUtil.class);
    private static RegistryManager registryManager = new RegistryManager();

    /**
     * This method is used to retrieve list of host names for a given
     * webapplication.
     *
     * @param webAppName the webapp name
     * @return list of mapped hosts.
     * @throws UrlMapperException throws it when failing to get resource from registry
     */
    public static List<String> getMappingsPerWebApp(String webAppName) throws UrlMapperException {
        List<String> hostNames = new ArrayList<String>();
        try {
            MappingData mappings[] = getAllMappingsFromRegistry();
            if (mappings != null) {
                for (MappingData mapping : mappings) {
                    String hostName = mapping.getMappingName();
                    if (!mapping.isServiceMapping() && webAppName.equals(mapping.getUrl())) {
                        hostNames.add(hostName);
                    }
                }
            }
            return hostNames;
            
        } catch (Exception e) {
            log.error("Failed to get url mappings for the webapp ", e);
            throw new UrlMapperException("Failed to get url mappings for the webapp " + webAppName,
                    e);
        }
    }

    /**
     * Find out whether the hostname exists already
     *
     * @param mappingName the host name to be mapped
     * @return Whether the hostname is valid or not
     * @throws UrlMapperException throws when error while retrieve from registry
     */
    public static boolean isMappingExist(String mappingName) throws UrlMapperException {
        mappingName = mappingName + UrlMapperConstants.HostProperties.DOMAIN_NAME_PREFIX;
        MappingData mappings[] = getAllMappingsFromRegistry();
        boolean isExist = false;
        if (mappings != null) {
            for (MappingData mapping : mappings) {
                if (mappingName.equalsIgnoreCase(mapping.getMappingName())) {
                    isExist = true;
                }
            }
        }
        return isExist;
    }


    /**
     * retrieving all hosts from registry.
     *
     * @return all hosts from registry as List
     * @throws UrlMapperException
     */
    public static List<String> getAllHostsFromRegistry() throws UrlMapperException {
        List<String> allHosts = new ArrayList<String>();
        try {
            // get all virtual host from the registry
            MappingData mappings[] = getAllMappingsFromRegistry();
            if (mappings != null) {
                for (MappingData mapping : mappings) {
                    String hostName = mapping.getMappingName().replace(UrlMapperConstants.HostProperties.FILE_SERPERATOR
                            + UrlMapperConstants.HostProperties.HOSTINFO, "");
                    if (!mapping.isServiceMapping()) {
                        allHosts.add(hostName);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to get all hosts ", e);
            throw new UrlMapperException("Failed to get all hosts ", e);
        }
        return allHosts;
    }


    public static MappingData[] getAllMappingsFromRegistry() throws UrlMapperException {
        try {
            // get all URL mapping information.
            ApplicationContext.getCurrentApplicationContext().getUrlMappingOfApplication();
            return registryManager.getAllMappingsFromRegistry();
        } catch (Exception e) {
            log.error("Failed to get all hosts ", e);
            throw new UrlMapperException("Failed to get all url mappings from the registry ", e);
        }
    }


    /**
     * This method is used to retrieve list of host names for a given
     * webapplication.
     *
     * @param url
     * @return list of mapped hosts.
     * @throws UrlMapperException
     */
    public static List<String> getMappingsPerEppr(String url) throws UrlMapperException {
        List<String> hostNames = new ArrayList<String>();
        if (isServiceURLPattern(url)) {
            url = getServiceEndpoint(url);
            try {
                MappingData mappings[] = getAllMappingsFromRegistry();
                if (mappings != null) {
                    for (MappingData mapping : mappings) {
                        String hostName = mapping.getMappingName();
                        if (mapping.isServiceMapping() && url.equals(mapping.getUrl())) {
                            hostNames.add(hostName);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Failed to get url mappings for the webapp " + url, e);
                throw new UrlMapperException("Failed to get url mappings for the webapp " + url, e);
            }
        }
        return hostNames;
    }

    /**
     * retrieving host for a specific service
     *
     * @param hostName name of the host
     * @return
     * @throws UrlMapperException
     */
    public static String getServiceNameForHost(String hostName) throws UrlMapperException {
        try {
            return registryManager.getServiceNameForHost(hostName);
        } catch (Exception e) {
            log.error("Failed to retrieve the servicename from the host " + hostName, e);
            throw new UrlMapperException("Failed to retrieve the servicename from the host "
                    + hostName, e);
        }
    }

    public static int getTenantIdForHost(String hostName) throws UrlMapperException {
        int tenantId;
        try {
            tenantId = MultitenantConstants.SUPER_TENANT_ID;
            String tenantDomain = registryManager.getTenantDomainForHost(hostName);
            TenantManager tenantManager = DataHolder.getInstance().getRealmService()
                    .getTenantManager();
                try {
                    tenantId = tenantManager.getTenantId(tenantDomain);
                } catch (UserStoreException e) {
                    log.error("error in getting tenant id when adding host to tomcat engine", e);
                    throw new UrlMapperException(
                            "error in getting tenant id when adding host to tomcat engine");
                }
        } catch (Exception e) {
            log.error("Failed to retrieve the tenant domain from the host " + hostName, e);
            throw new UrlMapperException("Failed to retrieve the tenant domain from the host "
                    + hostName, e);
        }
        return tenantId;
    }
    /**
     * It is taken the webApp which is already deployed in
     * /repository/../webapps and redeploy it within added virtual host.
     *
     * @param hostName The virtual host name
     * @param uri      The web app to be deployed in the virtual host
     * @throws org.wso2.carbon.url.mapper.internal.exception.UrlMapperException
     *          When adding directory throws an Exception
     */
    public static void addWebAppToHost(String hostName, String uri) throws UrlMapperException {
        int tenantId;
        String tenantDomain;
        String webAppsDir;
        String webAppPath;
        // if the request if from tenant
        {
            if (MultitenantUtils.getTenantDomainFromRequestURL(uri) != null) {
                tenantDomain = MultitenantUtils.getTenantDomainFromRequestURL(uri);
                TenantManager tenantManager = DataHolder.getInstance().getRealmService()
                        .getTenantManager();
                try {
                    tenantId = tenantManager.getTenantId(tenantDomain);
                } catch (UserStoreException e) {
                    log.error("error in getting tenant id when adding host to tomcat engine", e);
                    throw new UrlMapperException(
                            "error in getting tenant id when adding host to tomcat engine");
                }
                // getting the web app .war file name from the uri
                
                // path of web app for the tenant in the server
                webAppsDir = CarbonUtils.getCarbonTenantsDirPath() + "/" + tenantId + "/"
                        + UrlMapperConstants.HostProperties.WEB_APPS + "/";
                webAppPath = getWebappPath(webAppsDir, uri);

            } else {
                tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
                webAppsDir = CarbonUtils.getCarbonRepository()
                        + UrlMapperConstants.HostProperties.WEB_APPS + "/";
                webAppPath = getWebappPath(webAppsDir, uri);

            }
        }
        Host host = addHostToEngine(hostName, webAppsDir);
        try {

            // deploying the copied webapp as the root in our own host directory
            /* TODO add listeners once integrate with webapp-mgt */
            DataHolder.getInstance().getCarbonTomcatService().addWebApp(host, "/", webAppPath);
            // add entry to registry with the tenant domain if exist in the uri if adding virtual host is successful.
            registryManager.addHostToRegistry(hostName, uri, tenantDomain);
            ApplicationContext.getCurrentApplicationContext().putUrlMappingForApplication(hostName,uri);
        } catch (Exception e) {
            log.error("error in adding the virtual host to tomcat engine", e);
            throw new UrlMapperException("error in adding the virtual host to tomcat engine");
        }
    }
    
    public static String getWebappPath(String webappsDir, String uri) {
        String webAppFile;
        String webAppPath;
        webAppFile = getContextFromUri(uri) + UrlMapperConstants.HostProperties.WAR;
        webAppPath = webappsDir + webAppFile;
        File warFile = new File(webAppPath);
        if(warFile.exists()) {
            //it is war
            return webAppPath;
        } else {
            webAppPath = webappsDir + getContextFromUri(uri);
            return webAppPath;
        }
    }

    /**
     * add host to engine.
     *
     * @param hostName name of the host
     * @return will return the added host of Engine
     */
    public static Host addHostToEngine(String hostName, String appBase) {
        String hostBaseDir = appBase;
        CarbonTomcatService carbonTomcatService = DataHolder.getInstance().getCarbonTomcatService();
        // adding virtual host to tomcat engine
        Engine engine = carbonTomcatService.getTomcat().getEngine();
        StandardHost host = new StandardHost();
        host.setAppBase(hostBaseDir);
        host.setName(hostName);
        host.setUnpackWARs(false);
        host.addValve(new CarbonContextCreatorValve());
        host.addValve(new CompositeValve());
        engine.addChild(host);
        log.info("host added to the tomcat: " + host);
        return host;
    }

    /**
     * delete the host from CATALINA-HOME directory
     *
     * @param hostName
     */
    public static void deleteHostDirectory(String hostName) {
        String filePath = CarbonUtils.getCarbonCatalinaHome() + "/" + hostName;
        File file = new File(filePath);
        if (file.isDirectory()) {
            // make sure tomcat engine has removed folder structure inside the
            // host folder
            if (file.list().length == 0) {
                file.delete();
            }
        }
    }

    /**
     * edit the existing host with the given name
     *
     * @param webAppName the associated webapp name of the host to be edited
     * @param oldHost    the existing hostname to be edited
     * @param newHost    the hostname given
     * @throws UrlMapperException throws when error while removing oldHost or adding newHost
     */
    public static void editHostInEngine(String webAppName, String newHost, String oldHost)
            throws UrlMapperException {
        removeHost(oldHost);
        addWebAppToHost(newHost, webAppName);
    }

    /**
     * remove the host from the engine and registry
     *
     * @param hostName name of the host to be removed
     * @throws UrlMapperException throws when error while removing
     *                            context or host from engine and from registry
     */
    public static void removeHost(String hostName)
            throws UrlMapperException {
        Container[] hosts = DataHolder.getInstance().getCarbonTomcatService().getTomcat()
                .getEngine().findChildren();
        CarbonTomcatService carbonTomcatService = DataHolder.getInstance().getCarbonTomcatService();
        Engine engine = carbonTomcatService.getTomcat().getEngine();
        for (Container host : hosts) {
            if (host.getName().contains(hostName)) {
                try {
                    Context context = (Context) host.findChild("/");
                    if (host.getState().isAvailable()) {
                        if (context != null && context.getAvailable()) {
                            context.setRealm(null);
                            context.stop();
                            context.destroy();
                            log.info("Unloaded webapp from the host: " + host + " as the context of: " + context);
                        }
                        host.removeChild(context);
                        host.setRealm(null);
                        host.stop();
                        host.destroy();
                        engine.removeChild(host);
                        deleteResourceToRegistry(host.getName());
                        ApplicationContext.getCurrentApplicationContext().removeUrlMappingMap(host.getName()) ;
                        log.info("Unloaded host from the engine: " + host);
                        break;
                    }
                } catch (LifecycleException e) {
                    throw new UrlMapperException("Error when removing host from tomcat engine." + host, e);
                }

            }
        }
        // host name should be deleted explicitly because when host is deleted
        // from tomcat engine the folder with the host name will not get
        // removed.
        deleteHostDirectory(hostName);
        try {
            registryManager.removeFromRegistry(hostName);
        } catch (Exception e) {
            log.error("error in adding the domain to the resitry", e);
            throw new UrlMapperException("error in adding the domain to the resitry", e);
        }
    }

    /**
     * adding domain for service in registry
     *
     * @param hostName
     * @param url
     * @throws UrlMapperException
     */
    public static void addDomainToServiceEpr(String hostName, String url) throws UrlMapperException {

        // if the request if from tenant
        String tenantDomain = "";
        if (MultitenantConstants.TENANT_AWARE_URL_PREFIX.contains(url)) {
            tenantDomain = MultitenantUtils.getTenantDomainFromRequestURL(url);
        }

        if (isServiceURLPattern(url)) {
            url = getServiceEndpoint(url);
        }
        try {
            // add entry to registry with the tenant domain if exist in the uri
            registryManager.addEprToRegistry(hostName, url, tenantDomain);
            ApplicationContext.getCurrentApplicationContext().putUrlMappingForApplication(hostName,url);
        } catch (Exception e) {
            log.error("error in adding the domain to the resitry", e);
            throw new UrlMapperException("error in adding the domain to the resitry");
        }
    }
    /**
     * update endpoint in the registry for host
     *
     * @param newHost new host to be updated
     * @param oldHost existing old host
     * @throws UrlMapperException
     */
    public static void updateEprToRegistry(String newHost, String oldHost)
            throws UrlMapperException {
        try {
            String epr = getServiceNameForHost(oldHost);
            deleteResourceToRegistry(oldHost);
            addDomainToServiceEpr(newHost, epr);
        } catch (Exception e) {
            log.error("error in updating the domain to the resitry", e);
            throw new UrlMapperException("error in updating the domain to the resitry");
        }
    }
    
   
    /**
     * deleting resource in registry when deleting host
     *
     * @param host hostName
     * @throws UrlMapperException
     */
    public static void deleteResourceToRegistry(String host) throws UrlMapperException {
        try {
            registryManager.removeFromRegistry(host);
            ApplicationContext.getCurrentApplicationContext().removeUrlMappingMap(host) ;
        } catch (Exception e) {
            log.error("error in removing the domain to the resitry", e);
            throw new UrlMapperException("error in updating the domain to the resitry");
        }
    }

    /**
     * getting context of the webapp from the uri
     *
     * @param uri uri of the actual webapp url
     * @return returns the context
     */
    public static String getContextFromUri(String uri) {
        // context path is /t/tenantdomain/webapps/webapp-context, then the
        // context is webapp-context
        String[] temp = uri.split(UrlMapperConstants.HostProperties.FILE_SERPERATOR);
        return temp[temp.length - 1];
    }

    /**
     * getting service endpoint from url
     *
     * @param url url of the service
     * @return
     * @throws UrlMapperException
     */
    public static String getServiceEndpoint(String url) throws UrlMapperException {
        String str[] = url.split(UrlMapperConstants.SERVICE_URL_PATTERN);
        if (str.length > 1) {
            return str[1];
        } else {
            throw new UrlMapperException("Invalid End point URL");
        }
    }

    /**
     * checking for the pattern of the url for service
     *
     * @param url url of the service invoked
     * @return boolean
     */
    public static boolean isServiceURLPattern(String url) {
        Pattern pattern = Pattern.compile(UrlMapperConstants.SERVICE_URL_PATTERN);
        Matcher matcher;
        matcher = pattern.matcher(url);
        return matcher.find();
    }

    /**
     * validating the host name  //TODO
     *
     * @param domain hostName as domain
     * @return
     * @throws UrlMapperException
     */
    public static boolean isValidHost(String domain) throws UrlMapperException {
        try {
            MappingData mappings[] = getAllMappingsFromRegistry();
            if (mappings != null) {
                for (MappingData mapping : mappings) {
                    String hostName = mapping.getMappingName().substring(UrlMapperConstants.HostProperties.HOSTINFO_DIR
                            .length());
                    if (hostName.equals(domain)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed validating the endpoint domain " + domain, e);
            throw new UrlMapperException("Failed validating the endpoint domain " + domain, e);
        }

        return false;
    }
}