/*
 * Copyright 2004,2012 The Apache Software Foundation.
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

package org.wso2.carbon.cep.siddhi.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.cep.core.CEPServiceInterface;
import org.wso2.carbon.cep.core.backend.CEPEngineProvider;
import org.wso2.carbon.cep.core.exception.CEPConfigurationException;
import org.wso2.carbon.cep.siddhi.backend.SiddhiBackEndRuntimeFactory;

/**
 * @scr.component name="siddhibackend.component" immediate="true"
 * @scr.reference name="cep.service"
 * interface="org.wso2.carbon.cep.core.CEPServiceInterface" cardinality="1..1"
 * policy="dynamic" bind="setCEPService" unbind="unSetCEPService"
 */

public class SiddhiBackendRuntimeDS {

    private static final Log log = LogFactory.getLog(SiddhiBackendRuntimeDS.class);

    protected void activate(ComponentContext context) {
        // registers with the cep service
        CEPEngineProvider esperCEPEngineProvider = new CEPEngineProvider();
        esperCEPEngineProvider.setName("SiddhiCEPRuntime");
        esperCEPEngineProvider.setProviderClass(SiddhiBackEndRuntimeFactory.class);

        try {
            SiddhiBackendRuntimeValueHolder.getInstance().getCEPService()
                    .registerCEPEngineProvider(esperCEPEngineProvider);
        } catch (CEPConfigurationException e) {
            log.error("Can not register Fusion back end runtime with the cep service ");
        }

    }

    protected void setCEPService(CEPServiceInterface cepService) {
        SiddhiBackendRuntimeValueHolder.getInstance().registerCEPService(cepService);
    }

    protected void unSetCEPService(CEPServiceInterface cepService) {

    }

}
