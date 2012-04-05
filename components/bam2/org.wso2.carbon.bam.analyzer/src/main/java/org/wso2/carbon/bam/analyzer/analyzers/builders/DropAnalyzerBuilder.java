package org.wso2.carbon.bam.analyzer.analyzers.builders;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.analyzer.analyzers.AnalyzerBuilder;
import org.wso2.carbon.bam.analyzer.analyzers.AnalyzerConfig;
import org.wso2.carbon.bam.analyzer.analyzers.DropAnalyzer;
import org.wso2.carbon.bam.analyzer.analyzers.configs.DropConfig;
import org.wso2.carbon.bam.analyzer.analyzers.configs.FilterField;
import org.wso2.carbon.bam.analyzer.engine.Analyzer;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerConfigConstants;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
public class DropAnalyzerBuilder extends AnalyzerBuilder {

    private static final Log log = LogFactory.getLog(DropAnalyzerBuilder.class);

    /*
      * Syntax :
      *
      * <drop type="group|row|column">
      *    [fieldSet] || [groupSet]
      * </drop>
      *
      * [fieldSet] := <fieldSet matchUsing='and|or'>
      *                  +<field name="" (0..1)regex=""/>
      *               </fieldSet>
      *
      * [groupSet] := <groupSet>
      *                  +<group regex=""/>
      *               </groupSet>
      */

    @Override
    protected AnalyzerConfig buildConfig(OMElement analyzerXML) throws AnalyzerException {

        DropConfig dropConfig = new DropConfig();

        OMAttribute type = analyzerXML.getAttribute(AnalyzerConfigConstants.type);

        if (isEmptyAttribute(type)) {
            handleException("Error at Drop : Type should not be empty..");
        }

        String typeStr = type.getAttributeValue();
        dropConfig.setType(typeStr);

        if (!(typeStr.equalsIgnoreCase(AnalyzerConfigConstants.ROW) ||
              typeStr.equalsIgnoreCase(AnalyzerConfigConstants.COLUMN) ||
              typeStr.equalsIgnoreCase(AnalyzerConfigConstants.GROUP))) {
            handleException("Error at Drop : Type should be one of 'row','column' or 'group'..");
        }

        OMElement fieldSet = analyzerXML.getFirstChildWithName(AnalyzerConfigConstants.fieldSet);
        OMElement groupSet = analyzerXML.getFirstChildWithName(AnalyzerConfigConstants.groupSet);

        if (fieldSet != null && groupSet != null) {
            handleException("Error at Drop: Only one of fieldSet or groupSet " +
                                        "specifications should be present..");
        } else if (fieldSet == null && groupSet == null) {
            handleException("Error at Drop : At least one of fieldSet or groupSet " +
                                        "specifications should be present..");
        }

        boolean isRowType = false;
        boolean isColumnType = false;
        boolean isGroupType = false;
        if (typeStr.equalsIgnoreCase(AnalyzerConfigConstants.ROW)) {
            isRowType = true;
        } else if (typeStr.equalsIgnoreCase(AnalyzerConfigConstants.COLUMN)) {
            isColumnType = true;
        } else if (typeStr.equalsIgnoreCase(AnalyzerConfigConstants.GROUP)) {
            isGroupType = true;
        }

        // row or column types must have fieldSet specification
        if ((isRowType || isColumnType) && fieldSet == null) {
            handleException("Error at Drop : Missing fieldSet specification for 'row'" +
                                        " or 'column' type..");
        }
        // group type must have groupSet specification
        if (isGroupType && groupSet == null) {
            handleException("Error at Drop : Missing groupSet specification for " +
                                        " 'group' type..");
        }

        if (fieldSet != null) {
            OMAttribute matchUsing = fieldSet.getAttribute(AnalyzerConfigConstants.matchUsing);
            if (isColumnType && !isEmptyAttribute(matchUsing)) {
                log.warn("Unnecessary matchUsing attribute in fieldSet specification for type " +
                         "'column'. Ignoring..");
            }

            String matchUsingStr;
            if (matchUsing != null) {
                matchUsingStr = matchUsing.getAttributeValue();
                if (!matchUsingStr.equalsIgnoreCase(AnalyzerConfigConstants.AND_SEMANTIC) ||
                    !matchUsingStr.equalsIgnoreCase(AnalyzerConfigConstants.OR_SEMANTIC)) {
                    handleException("Error at Drop : matchUsing should only contain values" +
                                                " 'and' or 'or'");
                }
            } else {
                matchUsingStr = "and"; // defaults to 'and'
            }

            dropConfig.setMatchUsing(matchUsingStr);

            Iterator<OMElement> fieldIterator = fieldSet.getChildrenWithName(
                    AnalyzerConfigConstants.field);
            if (!fieldIterator.hasNext()) {
                handleException("Error at Drop : fieldSpecification should contain" +
                                            " at least one field..");
            }

            List<FilterField> fieldFilters = new ArrayList<FilterField>();
            while (fieldIterator.hasNext()) {
                OMElement field = fieldIterator.next();

                OMAttribute name = field.getAttribute(AnalyzerConfigConstants.name);
                if (isEmptyAttribute(name)) {
                    handleException("Error at Drop : field name should not be empty..");
                }

                String nameStr = name.getAttributeValue();
                OMAttribute regex = field.getAttribute(AnalyzerConfigConstants.regex);
                String regexStr = null;
                if (!isEmptyAttribute(regex)) {
                    regexStr = regex.getAttributeValue();
                }

                FilterField filterField = new FilterField(nameStr, regexStr);
                fieldFilters.add(filterField);
            }

            dropConfig.setFieldFilters(fieldFilters);

        }

        if (groupSet != null) {
            Iterator<OMElement> groupIterator = groupSet.getChildrenWithName(
                    AnalyzerConfigConstants.group);
            if (!groupIterator.hasNext()) {
                handleException("Error at Drop : groupSpecification should contain" +
                                            " at least one group..");
            }

            List<String> groupFilters = new ArrayList<String>();
            while (groupIterator.hasNext()) {
                OMElement group = groupIterator.next();

                OMAttribute regex = group.getAttribute(AnalyzerConfigConstants.regex);

                if (isEmptyAttribute(regex)) {
                    handleException("Error at Drop : group regex should not be empty..");
                }

                groupFilters.add(regex.getAttributeValue());
            }

            dropConfig.setGroupFilters(groupFilters);
        }

        return dropConfig;
    }

    @Override
    public Analyzer buildAnalyzer(OMElement analyzerXML) throws AnalyzerException {
        return new DropAnalyzer(buildConfig(analyzerXML));
    }

    private boolean isEmptyAttribute(OMAttribute attribute) {
        if (attribute == null || attribute.getAttributeValue() == null ||
            attribute.getAttributeValue().equals("")) {
            return true;
        }

        return false;
    }

    private void handleException(String message) throws AnalyzerException {
        log.error(message);
        throw new AnalyzerException(message);
    }
}
