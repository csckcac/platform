/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.appfactory.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.ArtifactStorage;
import org.wso2.carbon.appfactory.core.BuildDriver;
import org.wso2.carbon.appfactory.core.RevisionControlDriver;

import java.util.HashMap;
import java.util.Map;

/**
 * @scr.component name=
 *                "org.wso2.carbon.appfactory.core.internal.AppFactoryCoreServiceComponent"
 *                immediate="true"
 * @scr.reference name="appfactory.maven"
 *                interface="org.wso2.carbon.appfactory.core.BuildDriver"
 *                cardinality="1..1" policy="dynamic" bind="setBuildDriver"
 *                unbind="unsetBuildDriver"
 * @scr.reference name="appfactory.svn"
 *                interface="org.wso2.carbon.appfactory.core.RevisionControlDriver"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setRevisionControlDriver"
 *                unbind="unsetRevisionControlDriver"
 * @scr.reference name="appfactory.configuration"
 *                interface="org.wso2.carbon.appfactory.common.AppFactoryConfiguration"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setAppFactoryConfiguration"
 *                unbind="unsetAppFactoryConfiguration"
 * @scr.reference name="appfactory.artifact"
 *                interface="org.wso2.carbon.appfactory.core.ArtifactStorage"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setArtifactStorage"
 *                unbind="unsetArtifactStorage"
 */

public class AppFactoryCoreServiceComponent {

	private static final Log log = LogFactory
			.getLog(AppFactoryCoreServiceComponent.class);
    private static Map<String,RevisionControlDriver> revisionControlDriverMap;
    private static BundleContext bundleContext;
    protected void activate(ComponentContext context) {
        AppFactoryCoreServiceComponent. bundleContext = context.getBundleContext();
        revisionControlDriverMap=new HashMap<String, RevisionControlDriver>();


        if (getBundleContext() != null) {
            ServiceTracker tracker = new ServiceTracker(getBundleContext(),
                                                        RevisionControlDriver.class.getName(), null);
            tracker.open();
            ServiceReference reference[] = tracker.getServiceReferences();
            if (reference != null) {
                for (ServiceReference serviceReference : reference) {
                    revisionControlDriverMap.put((String)serviceReference.getProperty("type"),
                                                 (RevisionControlDriver)tracker.getService(serviceReference));
                }
            } else {
                String msg = "This instance does not have revision controller";
                log.error(msg);
                   }
        }
        try {
            if (log.isDebugEnabled()) {
                log.debug("Appfactory core bundle is activated");
            }
        } catch (Throwable e) {
            log.error("Error in creating appfactory configuration", e);
        }

    }

	protected void deactivate(ComponentContext context) {
		if (log.isDebugEnabled()) {
			log.debug("Appfactory common bundle is deactivated");
		}
	}

	protected void unsetBuildDriver(BuildDriver buildDriver) {
		ServiceHolder.setBuildDriver(null);
	}

	protected void setBuildDriver(BuildDriver buildDriver) {
		ServiceHolder.setBuildDriver(buildDriver);
	}

	protected void unsetRevisionControlDriver(
			RevisionControlDriver revisionControlDriver) {
		ServiceHolder.setRevisionControlDriver(null);
	}

	protected void setRevisionControlDriver(
			RevisionControlDriver revisionControlDriver) {
		ServiceHolder.setRevisionControlDriver(revisionControlDriver);
	}

    protected void unsetArtifactStorage(ArtifactStorage artifactStorage) {
        ServiceHolder.setArtifactStorage(null);
    }

    protected void setArtifactStorage(ArtifactStorage artifactStorage) {
        ServiceHolder.setArtifactStorage(artifactStorage);
    }

    protected void setAppFactoryConfiguration(AppFactoryConfiguration appFactoryConfiguration) {
        ServiceHolder.setAppFactoryConfiguration(appFactoryConfiguration);
    }

    protected void unsetAppFactoryConfiguration(AppFactoryConfiguration appFactoryConfiguration) {
        ServiceHolder.setAppFactoryConfiguration(null);
    }

    public static  RevisionControlDriver getRevisionControlDriverMap(String type) {
        return revisionControlDriverMap.get(type);
    }

    public static void setRevisionControlDriverMap(
            Map<String, RevisionControlDriver> revisionControlDriverMap) {
        AppFactoryCoreServiceComponent.revisionControlDriverMap = revisionControlDriverMap;
    }

    public static BundleContext getBundleContext() {
        return bundleContext;
    }

    public static void setBundleContext(BundleContext bundleContext) {
        AppFactoryCoreServiceComponent.bundleContext = bundleContext;
    }
}
