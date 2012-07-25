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

package org.wso2.carbon.cep.fusion.backend;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.conf.EventProcessingOption;
import org.wso2.carbon.cep.core.backend.CEPBackEndRuntime;
import org.wso2.carbon.cep.core.backend.CEPBackEndRuntimeFactory;
import org.wso2.carbon.cep.core.mapping.input.mapping.InputMapping;

import java.util.List;
import java.util.Properties;

public class FusionBackEndRuntimeFactory implements CEPBackEndRuntimeFactory {


    public CEPBackEndRuntime createCEPBackEndRuntime(String bucketName,
                                                     Properties providerConfiguration,
                                                     List<InputMapping> mappings, int tenantId) {
        
        KnowledgeBaseConfiguration knowledgeBaseConfiguration
                = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
        knowledgeBaseConfiguration.setOption(EventProcessingOption.STREAM);

        KnowledgeBase knowledgeBase =
                KnowledgeBaseFactory.newKnowledgeBase(knowledgeBaseConfiguration);
        KnowledgeBuilderConfiguration builderConfiguration
                = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration();

        KnowledgeBuilder knowledgeBuilder =
                KnowledgeBuilderFactory.newKnowledgeBuilder(builderConfiguration);


        return new FusionBackEndRuntime(knowledgeBuilder, knowledgeBase, tenantId);
    }
}
