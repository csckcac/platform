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

package org.wso2.carbon.identity.entitlement.internal;

import org.wso2.carbon.identity.entitlement.pip.PIPAttributeFinder;
import org.wso2.carbon.identity.entitlement.pip.PIPExtension;
import org.wso2.carbon.identity.entitlement.pip.PIPResourceFinder;
import org.wso2.carbon.identity.entitlement.policy.PolicyMetaDataFinderModule;

import javax.xml.validation.Schema;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * keeps track of the configuration found in entitlement-config.xml
 */
public class EntitlementConfigHolder {

    /**
     * PIPExtensions will be fired for each and every XACML request - which will give a handle to
     * the incoming request.
     */
    private Map<PIPExtension, Properties> extensions = new HashMap<PIPExtension, Properties>();

    /**
     * This will be fired by CarbonAttributeFinder whenever it finds an attribute supported by this
     * module and missing in the XACML request.
     */
    private Map<PIPAttributeFinder, Properties> designators = new HashMap<PIPAttributeFinder, Properties>();

    /**
     * This will be fired by CarbonResourceFinder whenever it wants to find a descendant or child resource
     * of a given resource
     */
    private Map<PIPResourceFinder, Properties> resourceFinders  = new HashMap<PIPResourceFinder, Properties>();

    /**
     * This will be fired by PolicyMetaDataFinder, whenever it wants to retrieve an attribute values to build the
     * XACML policy
     */
    private Map<PolicyMetaDataFinderModule, Properties> policyMetaDataFinderModules  =
                                                        new HashMap<PolicyMetaDataFinderModule, Properties>();

    /**
     * This holds the policy schema against its version
     */
    private Map<String, Schema> policySchemaMap = new HashMap<String, Schema>();

    /**
     * Holds all caching related configurations
     */
    private Properties cachingProperties;


    
    public Map<PIPExtension, Properties> getExtensions() {
        return extensions;
    }

    public void addExtensions(PIPExtension extension, Properties properties) {
        this.extensions.put(extension, properties);
    }

    public Map<PIPAttributeFinder, Properties> getDesignators() {
        return designators;
    }

    public void addDesignators(PIPAttributeFinder attributeFinder,  Properties properties) {
        this.designators.put(attributeFinder, properties);
    }

    public Map<PIPResourceFinder, Properties> getResourceFinders() {
        return resourceFinders;
    }

    public void addResourceFinders(PIPResourceFinder resourceFinder, Properties properties) {
        this.resourceFinders.put(resourceFinder, properties);
    }

    public Map<PolicyMetaDataFinderModule, Properties> getPolicyMetaDataFinderModules() {
        return policyMetaDataFinderModules;
    }

    public void addPolicyMetaDataFinderModules(PolicyMetaDataFinderModule metaDataFinderModule,
                                               Properties properties) {
        this.policyMetaDataFinderModules.put(metaDataFinderModule, properties);
    }

    public Properties getCachingProperties() {
        return cachingProperties;
    }

    public void setCachingProperties(Properties cachingProperties) {
        this.cachingProperties = cachingProperties;
    }

    public Map<String, Schema> getPolicySchemaMap() {
        return policySchemaMap;
    }

    public void setPolicySchema(String schemaNS, Schema schema) {
        this.policySchemaMap.put(schemaNS, schema);
    }
}
