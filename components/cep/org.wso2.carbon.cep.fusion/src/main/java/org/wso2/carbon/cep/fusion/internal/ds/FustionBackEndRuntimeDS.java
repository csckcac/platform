/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.wso2.carbon.cep.fusion.internal.ds;

import org.wso2.carbon.cep.core.CEPServiceInterface;
import org.wso2.carbon.cep.core.exception.CEPConfigurationException;
import org.wso2.carbon.cep.core.backend.CEPEngineProvider;
import org.wso2.carbon.cep.fusion.backend.FusionBackEndRuntimeFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;

/**
 * this class is used to get the CEPServiceInterface service. it is used to
 * register this compoent with the cep service
 *
 * @scr.component name="fusionbackend.component" immediate="true"
 * @scr.reference name="cep.service"
 * interface="org.wso2.carbon.cep.core.CEPServiceInterface" cardinality="1..1"
 * policy="dynamic" bind="setCEPService" unbind="unSetCEPService"
 */

public class FustionBackEndRuntimeDS {

    private static final Log log = LogFactory.getLog(FustionBackEndRuntimeDS.class);
    private CEPEngineProvider droolsFusionCEPEngineProvider = null;

    protected void activate(ComponentContext context) {
        try {
            if (droolsFusionCEPEngineProvider == null) {
                // registers with the cep service
                droolsFusionCEPEngineProvider = new CEPEngineProvider();
                droolsFusionCEPEngineProvider.setName("DroolsFusionCEPRuntime");
                droolsFusionCEPEngineProvider.setProviderClass(FusionBackEndRuntimeFactory.class);

                try {
                    FusionBackEndRuntimeValueHolder.getInstance().getCEPService()
                            .registerCEPEngineProvider(droolsFusionCEPEngineProvider);
                } catch (CEPConfigurationException e) {
                    log.error("Can not register Fusion back end runtime with the cep service ", e);
                }
            }
        } catch (Throwable e) {
            log.error("Can not register Fusion back end runtime with the cep service ", e);

        }

    }

    protected void setCEPService(CEPServiceInterface cepService) {
        FusionBackEndRuntimeValueHolder.getInstance().registerCEPService(cepService);
    }

    protected void unSetCEPService(CEPServiceInterface cepService) {

    }
}
