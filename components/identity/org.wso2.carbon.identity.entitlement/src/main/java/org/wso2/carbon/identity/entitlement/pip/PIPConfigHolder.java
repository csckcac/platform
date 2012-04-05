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

package org.wso2.carbon.identity.entitlement.pip;

import org.wso2.carbon.identity.entitlement.policy.PolicyMetaDataFinderModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PIPConfigHolder keeps track of the configuration found in pip-config.xml
 * 
 */
public class PIPConfigHolder {

    /**
     * PIPExtensions will be fired for each and every XACML request - which will give a handle to
     * the incoming request.
     */
    private List<PIPExtension> extensions = new ArrayList<PIPExtension>();

    /**
     * Will be fired by CarbonAttributeFinder whenever it finds an attribute supported by this
     * module and missing in the XACML request.
     */
    private List<PIPAttributeFinder> designators = new ArrayList<PIPAttributeFinder>();

    /**
     * Will be fired by CarbonResourceFinder whenever it wants to find a descendant or child resource
     * of a given resource
     */
    private List<PIPResourceFinder> resourceFinders  = new ArrayList<PIPResourceFinder>();

    /**
     * Will be fired by PolicyMetaDataFinder, whenever it wants to retrieve an attribute values to build the
     * XACML policy
     */
    private List<PolicyMetaDataFinderModule> policyMetaDataFinderModules  =
                                                        new ArrayList<PolicyMetaDataFinderModule>();


    public List<PIPExtension> getExtensions() {
        return extensions;
    }

    public void setExtensions(List<PIPExtension> extensions) {
        this.extensions = extensions;
    }

    public List<PIPAttributeFinder> getDesignators() {
        return designators;
    }

    public void setDesignators(List<PIPAttributeFinder> designators) {
        this.designators = designators;
    }

    public List<PIPResourceFinder> getResourceFinders() {
        return resourceFinders;
    }

    public void setResourceFinders(List<PIPResourceFinder> resourceFinders) {
        this.resourceFinders = resourceFinders;
    }

    public List<PolicyMetaDataFinderModule> getPolicyMetaDataFinderModules() {
        return policyMetaDataFinderModules;
    }

    public void setPolicyMetaDataFinderModules(
            List<PolicyMetaDataFinderModule> policyMetaDataFinderModules) {
        this.policyMetaDataFinderModules = policyMetaDataFinderModules;
    }
}
