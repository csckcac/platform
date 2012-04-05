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
package org.wso2.carbon.mashup.jsservices.custom.ui.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.mashup.jsservices.custom.ui.CustomUIServletFilter;

import javax.servlet.Filter;

public class Activator implements BundleActivator {

    private static CustomUIServletFilter filter = null;

    public void start(BundleContext bundleContext) throws Exception {
        filter = new CustomUIServletFilter();
        bundleContext.registerService(Filter.class.getName(), filter, null);
    }

    public void stop(BundleContext bundleContext) throws Exception {
        
    }

    protected static CustomUIServletFilter getFilter() {
        return filter;
    }
}
