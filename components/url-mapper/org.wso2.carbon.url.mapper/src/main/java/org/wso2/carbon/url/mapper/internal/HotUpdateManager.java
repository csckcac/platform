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
package org.wso2.carbon.url.mapper.internal;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.url.mapper.HotUpdateService;
import org.wso2.carbon.url.mapper.internal.exception.UrlMapperException;
import org.wso2.carbon.url.mapper.internal.util.DataHolder;
import org.wso2.carbon.url.mapper.internal.util.HostUtil;
import org.wso2.carbon.url.mapper.internal.util.UrlMapperConstants;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.FileManipulator;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.File;
import java.util.List;

/**
 * To handle hot update of webApps when they update in web-app
 * mgt these methods are getting called.
 */

public class HotUpdateManager implements HotUpdateService {
    private static final Log log = LogFactory.getLog(HotUpdateManager.class);

    /**
     * Handles deploying webapps in all hosts in any hot update
     *
     * @param file the context of a webapp
     * @throws UrlMapperException
     */
    public void deployWebApp(File file) throws UrlMapperException {
        try {
            String context = getContextFromFile(file);
            List<String> hosts = HostUtil.getMappingsPerWebApp(context);
            if (hosts != null) {
                StandardHost host;
                StandardContext container;
                for (String hostName : hosts) {
                    host = (StandardHost) findHost(hostName);
                    container = (StandardContext) host.findChild("/");
                    if (container != null) {
                        if (!container.getAvailable()) {
                            container.start();
                            DataHolder.getInstance().getCarbonTomcatService().addWebApp(host, "/", file.getAbsolutePath());
                        }
                    } else {
                        DataHolder.getInstance().getCarbonTomcatService().
                                addWebApp(host, "/", file.getAbsolutePath());
                    }

                }
            }
        } catch (Exception e) {
            log.error(e);
            throw new UrlMapperException("error while deploying webApp in a host");
        }
    }

    /**
     * Handles un deploying webapp in all hosts in any hot update
     *
     * @param file the context of webapp
     * @throws UrlMapperException
     */
    public void undeployWebApp(File file) throws UrlMapperException {
        try {
            String context = getContextFromFile(file);
            List<String> hosts = HostUtil.getMappingsPerWebApp(context);
            if (hosts != null) {
                StandardHost host;
                StandardContext container;
                for (String hostName : hosts) {
                    host = (StandardHost) findHost(hostName);
                    container = (StandardContext) host.findChild("/");
                    try {
                        if (container.getAvailable()) {
                            container.setRealm(null);
                            container.stop();
                            container.destroy();
                            log.info("Unloaded webapp: " + host + "as context:" + context);
                        }
                    } catch (Exception e) {
                        log.error("Cannot lazy unload webapp in url-mapper", e);
                        throw new UrlMapperException(
                                "Cannot lazy unload webapp in url-mapper" + context, e);
                    }
                    File webAppDir = new File(UrlMapperConstants.HostProperties.CATALINA_HOME
                            + host.getName() + "/_");
                    if (webAppDir.exists() && !FileManipulator.deleteDir(webAppDir)) {
                        throw new UrlMapperException(
                                webAppDir + "deletion failed in url-mapper when deleting hosts");
                    }
                }
            }
        } catch (Exception e) {
            log.error("error while un deploying webApp in a host", e);
            throw new UrlMapperException("error while un deploying webApp in a host");
        }
    }

    /**
     * Handles deleting webapps in all hosts in any hot update
     *
     * @param file the context of webapp
     * @throws UrlMapperException
     */
    public void deleteWebApp(File file) throws UrlMapperException {
        undeployWebApp(file);
        //TODO delete
    }

    /**
     * stopping the context
     *
     * @param context
     * @return
     * @throws UrlMapperException
     */
    public boolean stop(Context context) throws UrlMapperException {
        List<String> hosts = HostUtil.getMappingsPerWebApp(context.getName());
        if (hosts != null) {
            StandardHost host;
            StandardContext container;
            for (String hostName : hosts) {
                host = (StandardHost) findHost(hostName);
                container = (StandardContext) host.findChild("/");
                try {
                    if (!container.getAvailable()) {
                        container.stop();
                        log.info("Stopped webapp in host: " + host + "as context:" + container);
                        return true;
                    }
                } catch (Exception e) {
                    throw new UrlMapperException("Cannot stop webapp " + host + "as context:" + container, e);
                }
            }
        }
        return false;
    }

    /**
     * reloading the context
     *
     * @param context
     * @return
     * @throws UrlMapperException
     */
    public boolean reload(Context context) throws UrlMapperException {
        List<String> hosts = HostUtil.getMappingsPerWebApp(context.getName());
        if (hosts != null) {
            StandardHost host;
            StandardContext container;
            for (String hostName : hosts) {
                host = (StandardHost) findHost(hostName);
                container = (StandardContext) host.findChild("/");
                try {
                    if (!container.getAvailable()) {
                        container.reload();
                        log.info("Reloaded webapp: " + host + "as context:" + container);
                        return true;
                    }
                } catch (Exception e) {
                    throw new UrlMapperException("Cannot reload webapp " + host + "as context:" + container, e);
                }
            }
        }
        return false;
    }

    /**
     * starting the context
     *
     * @param context
     * @return
     * @throws UrlMapperException
     */
    public boolean start(Context context) throws UrlMapperException {
        List<String> hosts = HostUtil.getMappingsPerWebApp(context.getName());
        if (hosts != null) {
            StandardHost host;
            StandardContext container;
            for (String hostName : hosts) {
                host = (StandardHost) findHost(hostName);
                container = (StandardContext) host.findChild("/");
                try {
                    if (!container.getAvailable()) {
                        container.start();
                        log.info("Started webapp: " + host + "as context:" + container);
                        return true;
                    }
                } catch (Exception e) {
                    throw new UrlMapperException("Cannot start webapp " + host + "as context:" + container, e);
                }
            }
        }
        return false;
    }

    /**
     * to find the host
     *
     * @param hostName
     * @return
     */
    private Host findHost(String hostName) {
        Engine engine = DataHolder.getInstance().getCarbonTomcatService().getTomcat().getEngine();
        Container[] children = engine.findChildren();
        for (Container container : children) {
            if (hostName.equalsIgnoreCase(container.getName())) {
                return (Host) container;
            }
        }
        return null;
    }

    /**
     * getting webapp context from the file
     *
     * @param file
     * @return
     */
    private String getContextFromFile(File file) {
        String fileName = file.getName();
        String webAppFile = fileName.substring(0, fileName.length() - 4);
        String context;
        String tenantDomain = "";
        if (CarbonUtils.getCarbonTenantsDirPath().contains(file.getAbsolutePath())) {
            //TODO tenants get tenant domain from id
            context = "/" + MultitenantConstants.TENANT_AWARE_URL_PREFIX + "/" + tenantDomain
                    + UrlMapperConstants.HostProperties.WEB_APPS + "/" + webAppFile;
        } else {
            context = "/" + webAppFile;
        }
        return context;
    }
}
