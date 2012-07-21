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

package org.wso2.carbon.cep.siddhi.internal.ds;

import org.wso2.carbon.cep.core.CEPServiceInterface;
import org.wso2.carbon.cep.core.backend.CEPEngineProvider;
import org.wso2.siddhi.core.persistence.PersistenceStore;

/**
 * Value holder to keep the other services
 */
public class SiddhiBackendRuntimeValueHolder {

    private static SiddhiBackendRuntimeValueHolder instance = new SiddhiBackendRuntimeValueHolder();

    private static CEPServiceInterface cepService;
    private PersistenceStore persistenceStore;
    private CEPEngineProvider CEPEngineProvider=null;

    public static SiddhiBackendRuntimeValueHolder getInstance(){
        return instance;
    }

    public void registerCEPService(CEPServiceInterface cepServiceInterface){
        cepService = cepServiceInterface;
    }

    public CEPServiceInterface getCEPService(){
        return cepService;
    }

    public void setPersistenceStore(PersistenceStore persistenceStore) {
        this.persistenceStore = persistenceStore;
    }

    public PersistenceStore getPersistenceStore() {
        return persistenceStore;
    }

    public void setCEPEngineProvider(CEPEngineProvider CEPEngineProvider) {
        this.CEPEngineProvider = CEPEngineProvider;
    }

    public CEPEngineProvider getCEPEngineProvider() {
        return CEPEngineProvider;
    }
}
