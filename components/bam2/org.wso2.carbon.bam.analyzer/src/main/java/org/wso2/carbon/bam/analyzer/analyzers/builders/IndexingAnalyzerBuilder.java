package org.wso2.carbon.bam.analyzer.analyzers.builders;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.bam.analyzer.analyzers.AnalyzerBuilder;
import org.wso2.carbon.bam.analyzer.analyzers.AnalyzerConfig;
import org.wso2.carbon.bam.analyzer.analyzers.IndexingAnalyzer;
import org.wso2.carbon.bam.analyzer.engine.Analyzer;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerException;

/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class IndexingAnalyzerBuilder extends AnalyzerBuilder {
    @Override
    protected AnalyzerConfig buildConfig(OMElement analyzerXML) throws AnalyzerException {

/*        Iterator cfIterator = analyzerXML.getChildrenWithName(AnalyzerConfigConstants.CF_ELEMENT);

        List<CFConfigBean> cfConfigurations = new ArrayList<CFConfigBean>();
        while (cfIterator.hasNext()) {
            Object cfObject = cfIterator.next();
            if (!(cfObject instanceof OMElement)) {
                continue;
            }

            OMElement cfElement = (OMElement) cfObject;

            OMAttribute cfName = cfElement.getAttribute(AnalyzerConfigConstants.name);

            if (cfName == null || cfName.getAttributeValue() == null ||
                cfName.getAttributeValue().trim().equals("")) {
                throw new AnalyzerException("Required attribute 'name' missing..");
            }

            String cfNameStr = cfName.getAttributeValue();

            OMAttribute defaultCF = cfElement.getAttribute(AnalyzerConfigConstants.DEFAULT_CF_QNAME);

            boolean isDefaultCf = false;
            if (defaultCF != null) {
                isDefaultCf = Boolean.getBoolean(defaultCF.getAttributeValue());
            }

            OMElement granularity = cfElement.getFirstChildWithName(
                    AnalyzerConfigConstants.granularity);

            String granularityStr = null;
            if (granularity != null) {
                granularityStr = granularity.getText();
            }

            OMElement indexRowKey = cfElement.getFirstChildWithName(
                    AnalyzerConfigConstants.INDEX_ROW_KEY_QNAME);

            String indexRowKeyStr = null;
            if (indexRowKey != null) {
                indexRowKeyStr = indexRowKey.getText();
            }

            OMElement rowKey = cfElement.getFirstChildWithName(AnalyzerConfigConstants.ROWKEY_QNAME);

            List<KeyPart> keyParts = new ArrayList<KeyPart>();
            if (rowKey != null) {
                Iterator<OMElement> partIterator = rowKey.getChildrenWithName(AnalyzerConfigConstants.PART_QNAME);

                while (partIterator.hasNext()) {
                    OMElement partElement = partIterator.next();

                    OMAttribute partName = partElement.getAttribute(AnalyzerConfigConstants.name);

                    if (partName == null || partName.getAttributeValue() == null ||
                        partName.getAttributeValue().trim().equals("")) {
                        throw new AnalyzerException("Required attribute 'name' missing..");
                    }

                    String partNameStr = partName.getAttributeValue();

                    OMAttribute storeIndex = partElement.getAttribute(
                            AnalyzerConfigConstants.STORE_INDEX);

                    boolean isStoreIndex = false;
                    if (storeIndex != null) {
                        isStoreIndex = Boolean.getBoolean(storeIndex.getAttributeValue());
                    }

                    KeyPart part = new KeyPart(partNameStr, null, isStoreIndex);
                    keyParts.add(part);
                }
            }

            CFConfigBean cfConfig = new CFConfigBean();
            cfConfig.setCfName(cfNameStr);
            cfConfig.setDefaultCF(isDefaultCf);
            cfConfig.setGranularity(granularityStr);
            cfConfig.setIndexRowKey(indexRowKeyStr);
            cfConfig.setRowKeyParts(keyParts);

            cfConfigurations.add(cfConfig);
        }

        // A clone of cfConfigurations should be passed to configuration holder since once inside
        // it may get modified while the cfConfigs inside indexing analyzer should be immutable
        List<CFConfigBean> clonedCfConfigs = new ArrayList<CFConfigBean>();
        clonedCfConfigs.addAll(cfConfigurations);

        int tenantOwningTheConfig = ConfigurationHolder.getInstance().
                getCurrentConfigProcessingTenant();
        ConfigurationHolder.getInstance().resetIndexConfigurations(tenantOwningTheConfig,
                                                                   clonedCfConfigs);*/

        return null;

    }

    @Override
    public Analyzer buildAnalyzer(OMElement analyzerXML) throws AnalyzerException {
        return new IndexingAnalyzer(buildConfig(analyzerXML));
    }
}
