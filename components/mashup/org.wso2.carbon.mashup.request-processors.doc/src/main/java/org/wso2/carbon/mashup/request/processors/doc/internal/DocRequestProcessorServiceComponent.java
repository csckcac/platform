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
package org.wso2.carbon.mashup.request.processors.doc.internal;

import org.osgi.service.http.HttpService;
import org.osgi.service.component.ComponentContext;
import org.osgi.framework.BundleContext;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.wso2.carbon.core.util.Utils;

/**
 * @scr.component name="mashup.requestprocessors.doc.dscomponent" immediate="true"
 * @scr.reference name="http.service" interface="org.osgi.service.http.HttpService"
 * cardinality="1..1" policy="dynamic"  bind="setHttpService" unbind="unsetHttpService"
 */
public class DocRequestProcessorServiceComponent {

    private HttpService httpService = null;
    private static Log log = LogFactory.getLog(DocRequestProcessorServiceComponent.class);

    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

    protected void activate(ComponentContext context) {
        try {
            BundleContext bundleContext = context.getBundleContext();
            httpService.registerResources("/doc_request_processor", "/web", httpService.createDefaultHttpContext());
            Utils.registerHTTPGetRequestProcessors(bundleContext);
            log.debug("******* Doc Request Processor is activated ******* ");
        } catch (Throwable e) {
            log.error("******* Failed to activate Doc Request Processor ******* ", e);
        }
    }

    protected void deactivate(ComponentContext context) {
        log.debug("******* Doc Request Processor is deactivated ******* ");
    }
}
