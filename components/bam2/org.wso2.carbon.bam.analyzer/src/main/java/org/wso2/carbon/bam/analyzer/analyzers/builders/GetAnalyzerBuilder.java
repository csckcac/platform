package org.wso2.carbon.bam.analyzer.analyzers.builders;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.bam.analyzer.analyzers.AnalyzerBuilder;
import org.wso2.carbon.bam.analyzer.analyzers.AnalyzerConfig;
import org.wso2.carbon.bam.analyzer.analyzers.GetAnalyzer;
import org.wso2.carbon.bam.analyzer.analyzers.configs.GetConfig;
import org.wso2.carbon.bam.analyzer.engine.Analyzer;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerConfigConstants;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerException;
import org.wso2.carbon.bam.core.persistence.QueryIndex;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

/**
 * Analyzer syntax :
 * <p/>
 * <get name='' batchSize='integer'>
 * <where index=''/>
 * <range column='' start='' end=''/> [1..*]
 * </where>
 * </get>
 */
public class GetAnalyzerBuilder extends AnalyzerBuilder {

    @Override
    protected AnalyzerConfig buildConfig(OMElement analyzerEl) throws AnalyzerException {

        OMAttribute name = analyzerEl.getAttribute(AnalyzerConfigConstants.name);

        if (name == null) {
            throw new AnalyzerException("Table name must be present..");
        }

        //OMAttribute fromLastCursor = analyzerEl.getAttribute(AnalyzerConfigConstants.fromLastCursor);
        OMAttribute batchSizeAtt = analyzerEl.getAttribute(AnalyzerConfigConstants.batchSize);
        OMAttribute fetchDirty = analyzerEl.getAttribute(AnalyzerConfigConstants.fetchDirty);

        boolean isFetchDirty = false;
        int batchSize = Integer.MAX_VALUE;

/*        if (fromLastCursor != null) {
            try {
                isFromLastCursor = Boolean.parseBoolean(fromLastCursor.getAttributeValue());
            } catch (Exception e) {
                throw new AnalyzerException("fromLastCursor should be either 'true' or 'false'..");
            }
        }*/

        if (fetchDirty != null) {
            try {
                isFetchDirty = Boolean.parseBoolean(fetchDirty.getAttributeValue());
            } catch (Exception e) {
                throw new AnalyzerException("fetchDirty should be either 'true' or 'false'..");
            }
        }

        if (batchSizeAtt != null) {
            try {
                batchSize = Integer.parseInt(batchSizeAtt.getAttributeValue());
                if (batchSize <= 0) {
                    throw new AnalyzerException("batchSize must be a positive integer..");
                }
            } catch (Exception e) {
                throw new AnalyzerException("batchSize must be a positive integer..");
            }
        }

        String cfName = name.getAttributeValue();
        Iterator indexIterator = analyzerEl.getChildrenWithLocalName(AnalyzerConfigConstants.WHERE);
        QueryIndex index = null;
        while (indexIterator.hasNext()) {
            Object indexElementObject = indexIterator.next();
            if (!(indexElementObject instanceof OMElement)) {
                return null;
            }

            OMElement indexEl = (OMElement) indexElementObject;
            OMAttribute indexName = indexEl.getAttribute(AnalyzerConfigConstants.index);

            Iterator<OMElement> rangeElements = indexEl.
                    getChildrenWithLocalName(AnalyzerConfigConstants.RANGE);
            if (rangeElements != null) {

                index = new QueryIndex(indexName.getAttributeValue());

                while (rangeElements.hasNext()) {
                    OMElement rangeElement = rangeElements.next();
                    
                    String column = rangeElement.getAttributeValue(AnalyzerConfigConstants.column);
                    String rangeFirst = rangeElement.getAttributeValue(
                            AnalyzerConfigConstants.start);
                    String rangeLast = rangeElement.getAttributeValue(AnalyzerConfigConstants.end);
                    
                    if (rangeFirst == null) {
                        rangeFirst = "";
                    } 
                    
                    if (rangeLast == null) {
                        rangeLast = "";
                    }
                    
                    if (column == null) {
                        throw new AnalyzerException("Index column cannot be empty..");
                    }

                    index.addCompositeRange(column, rangeFirst, rangeLast);

                }
            }

            break;

        }

/*        OMElement groupByEl = analyzerEl.getFirstChildWithName(AnalyzerConfigConstants.groupBy);
        OMAttribute groupByIndex = null;
        if (groupByEl != null) {
            groupByIndex = groupByEl.getAttribute(AnalyzerConfigConstants.index);
        }

        OMElement granularityEl = analyzerEl.getFirstChildWithName(AnalyzerConfigConstants.granularity);

        if (groupByEl != null && granularityEl != null) {
            throw new AnalyzerException("Either groupBy or granularity should be present. Not both..");
        }

        OMAttribute granularityIndex = null;
        OMAttribute granularityType = null;

        if (granularityEl != null) {
            granularityIndex = granularityEl.getAttribute(AnalyzerConfigConstants.index);
            granularityType = granularityEl.getAttribute(AnalyzerConfigConstants.type);
            if (granularityIndex == null || granularityType == null) {
                throw new AnalyzerException("Required attributes index or type missing..");
            }
        }*/

        GetConfig cfg = new GetConfig();
        cfg.setTable(cfName);
        cfg.setIndex(index);
        cfg.setBatchSize(batchSize);
        cfg.setFetchDirty(isFetchDirty);

/*        if (groupByIndex != null) {

            cfg.setGroupByColumn(groupByIndex.getAttributeValue());

        } else if (granularityEl != null) {

            cfg.setGranularityColumn(granularityIndex.getAttributeValue());
            cfg.setGranularityType(granularityType.getAttributeValue());
        }*/
        return cfg;
    }

    @Override
    public Analyzer buildAnalyzer(OMElement analyzerXML) throws AnalyzerException {
        return new GetAnalyzer(buildConfig(analyzerXML));
    }
}
