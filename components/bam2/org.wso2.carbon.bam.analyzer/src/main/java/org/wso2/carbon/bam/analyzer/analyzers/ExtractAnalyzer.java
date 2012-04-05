/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.analyzer.analyzers;

import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.wso2.carbon.bam.analyzer.analyzers.configs.ExtractConfig;
import org.wso2.carbon.bam.analyzer.analyzers.configs.ExtractField;
import org.wso2.carbon.bam.analyzer.engine.DataContext;
import org.wso2.carbon.bam.core.dataobjects.Record;

import javax.xml.stream.XMLStreamException;
import java.util.List;
import java.util.Map;

/*
 * Syntax :
 *
 * <extract>
 *   +<field from='' name='' xpath=''>
 *      +<namespace prefix='' uri=''/>
 *    </field>
 * </extract>
 *
 * This analyzer creates new columns out of extracted values from xml content present in columns.
 *
 * Syntax Explanation :
 *
 * <extract>                        : Top level element for extract analyzer
 * <field from='' name='' xpath=''> : Specifies details about new column to extract out from xml content
 *                                       from = In which column xml content is present which is used
 *                                              to extract out content
 *                                       name = Name of newly formed column out of extracted value
 *                                       xpath = Xpath to extract content from the xml
 * <namespace prefix='' uri=''>     : Specifies a namespace used in xpath expression
 *
 * Example :
 *
 * Input : List of rows as given below with each row having an xml as a value for one of its columns.
 *
 *   {employee1} : {{name : ben}, {age : 42}, {post : eng},
 *                 { address : <address xmlns:x='http://example.org'>
 *                              <x:apartment>24</x:apartment>
 *                              <x:lane>first lane</x:lane>
 *                              <x:city>Witchita</x:city>
 *                              <x:state>KS</x:state>
 *                            </address>
 *                 }}
 *   {employee2} : {{name : alex}, {age : 35}, {post : admin}
 *                 { address : <address xmlns:x='http://example.org'>
 *                              <x:apartment>Chase Suite</x:apartment>
 *                              <x:lane>lily road</x:lane>
 *                              <x:city>Helena</x:city>
 *                              <x:state>MT</x:state>
 *                            </address>
 *                 }}
 *   {employee3} : {{name : bob}, {age : 48}, {post : eng}
 *                 { address : <address xmlns:x='http://example.org'>
 *                              <x:apartment>26</x:apartment>
 *                              <x:lane>first lane</x:lane>
 *                              <x:city>Witchita</x:city>
 *                              <x:state>KS</x:state>
 *                            </address>
 *                 }}
 *   {employee4} : {{name : sarah}, {age : 26}, {post : admin}
 *                 { address : <address xmlns:x='http://example.org'>
 *                              <x:apartment>22</x:apartment>
 *                              <x:lane>Wolfsville Road</x:lane>
 *                              <x:city>Hagerstown</x:city>
 *                              <x:state>MD</x:state>
 *                            </address>
 *                 }}
 *
 * Extract specification : <extract>
 *                           <field from='address' name='state' xpath='//address/x:state'>
 *                              <namespace prefix='x' uri='http://example.org'/>
 *                           </field>
 *                         </extract>
 *
 * Output : List of rows each row having newly added state column whose value extracted out from
 *          xml found in address column.
 *
 *   {employee1} : {{name : ben}, {age : 42}, {post : eng},
 *                 { address : <address xmlns:x='http://example.org'>
 *                              <x:apartment>24</x:apartment>
 *                              <x:lane>first lane</x:lane>
 *                              <x:city>Witchita</x:city>
 *                              <x:state>KS</x:state>
 *                            </address>
 *                 }, {state : KS}}
 *   {employee2} : {{name : alex}, {age : 35}, {post : admin}
 *                 { address : <address xmlns:x='http://example.org'>
 *                              <x:apartment>Chase Suite</x:apartment>
 *                              <x:lane>lily road</x:lane>
 *                              <x:city>Helena</x:city>
 *                              <x:state>MT</x:state>
 *                            </address>
 *                 }, {state : MT}}
 *   {employee3} : {{name : bob}, {age : 48}, {post : eng}
 *                 { address : <address xmlns:x='http://example.org'>
 *                              <x:apartment>26</x:apartment>
 *                              <x:lane>first lane</x:lane>
 *                              <x:city>Witchita</x:city>
 *                              <x:state>KS</x:state>
 *                            </address>
 *                 }, {state : KS}}
 *   {employee4} : {{name : sarah}, {age : 26}, {post : admin}
 *                 { address : <address xmlns:x='http://example.org'>
 *                              <x:apartment>22</x:apartment>
 *                              <x:lane>Wolfsville Road</x:lane>
 *                              <x:city>Hagerstown</x:city>
 *                              <x:state>MD</x:state>
 *                            </address>
 *                 }, {state : MD}}
 *
 * If the input is grouped rows the behaviour will be the same for each row within the groups with
 * no changes to the grouping structure.
 *
 */
public class ExtractAnalyzer extends AbstractAnalyzer {

    private static final Log log = LogFactory.getLog(ExtractAnalyzer.class);

    public ExtractAnalyzer(AnalyzerConfig analyzerConfig) {
        super(analyzerConfig);
    }

    public void analyze(DataContext dataContext) {

        Object result = getData(dataContext);

        analyze(result);

    }

    public void analyze(Object result) {
        if (result != null) {
            if (result instanceof List) {
                List<Record> list = (List<Record>) result;

                processRecords(list);

            } else if (result instanceof Map) {
                Map<String, List<Record>> existingRecordMap = (Map<String, List<Record>>)
                        result;

                for (List<Record> list : existingRecordMap.values()) {
                    processRecords(list);
                }
            } else {
                log.error("Unknown data format in received data for extract analyzer..");
            }
        } else {
            log.warn("Data flow empty at extract analyzer in sequence : " + getAnalyzerSequenceName());
        }
    }

    private void processRecords(List<Record> records) {
        ExtractConfig config = (ExtractConfig) getAnalyzerConfig();
        List<ExtractField> fields = config.getFields();

        for (Record record : records) {
            Map<String, String> columns = record.getColumns();

/*            List<ResultColumn> newColumnList = new ArrayList<ResultColumn>();

            // Populate result columns to list
            for (ResultColumn column : columns) {
                newColumnList.add(column);
            }*/

            for (ExtractField field : fields) {
                for (Map.Entry<String, String> column : columns.entrySet()) {

                    if (field.getFrom().equals(column.getKey())) {

                        try {
                            AXIOMXPath xpath = new AXIOMXPath(field.getXpath());

                            Map<String, String> namespaces = field.getNamespaces();
                            for (Map.Entry<String, String> entry : namespaces.entrySet()) {
                                xpath.addNamespace(entry.getKey(), entry.getValue());
                            }

                            String xmlContent = column.getValue();
                            OMElement element = AXIOMUtil.stringToOM(xmlContent);

                            List results = xpath.selectNodes(element);
                            String value = null;
                            if (results != null && results.size() > 0) {
                                OMContainer container = (OMContainer) results.get(0); // Get only the first result

                                OMElement resultElement;
                                if (container instanceof OMDocument) {
                                    resultElement = ((OMDocument) container).getOMDocumentElement();
                                } else {
                                    resultElement = (OMElement) container;
                                }

                                value = resultElement.getText();
                            }

                            if (value != null) {
                                record.addColumn(field.getName(), value);
                            }

                        } catch (JaxenException e) {
                            log.error("Error while initializing xpath..", e);
                        } catch (XMLStreamException e) {
                            log.error("Error while initializing xml content..", e);
                        }

                        break;
                    }
                }
            }

        }
    }

}
