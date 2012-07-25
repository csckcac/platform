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

package org.wso2.carbon.cep.core.backend;

import org.wso2.carbon.cep.core.exception.CEPConfigurationException;
import org.wso2.carbon.cep.core.mapping.input.mapping.InputMapping;

import java.util.List;
import java.util.Properties;

/**
 * this class is used to create real cep runtime objects. One bucket use one cep back end
 * runtime object.
 */
public interface CEPBackEndRuntimeFactory {

    /**
     * Wso2 CEP Engine invokes this method to create back end runtime objects
     *
     *
     * @param bucketName - String value
     * @param providerConfiguration
     *@param mappings   - list of input mappings objects to generate the cep configuration.  @return - back end runtime
     */
    public CEPBackEndRuntime createCEPBackEndRuntime(String bucketName,
                                                     Properties providerConfiguration,
                                                     List<InputMapping> mappings,
                                                     int tenantId) throws
                                                                   CEPConfigurationException;
}
