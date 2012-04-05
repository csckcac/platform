package org.wso2.carbon.bam.analyzer.analyzers;

import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.wso2.carbon.bam.analyzer.analyzers.configs.DetectFaultConfig;
import org.wso2.carbon.bam.analyzer.analyzers.configs.ExtractConfig;
import org.wso2.carbon.bam.analyzer.analyzers.configs.ExtractField;
import org.wso2.carbon.bam.analyzer.engine.DataContext;
import org.wso2.carbon.bam.core.dataobjects.Record;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class DetectFaultAnalyzer extends AbstractAnalyzer {

    private static final Log log = LogFactory.getLog(DetectFaultAnalyzer.class);
    public static final String SOAP_ENV_NAMESPACE_PREFIX = "soapenv";
    public static final String FAULT_CODE = "faultCode";
    public static final String FAULT_REASON = "faultReason";

    public static final String TIMESTAMP = "timestamp";
    public static final String SOAP_ENVELOP_NAMESPACE_URI = "soap_envelop_namespace";
    public static final String MSG_BODY = "message_body";

    private DetectFaultConfig detectFaultAnalyzerConfig;

    public DetectFaultAnalyzer(AnalyzerConfig analyzerConfig) {
        super(analyzerConfig);
    }

    public void analyze(DataContext dataContext) {

        detectFaultAnalyzerConfig = (DetectFaultConfig) getAnalyzerConfig();
        List<String> errorFields = detectFaultAnalyzerConfig.getErrorFields();
        String currentSequenceIdentifier = detectFaultAnalyzerConfig.getCurrentSequenceIdentifier();
        List<Record> detectFaultRecords = new ArrayList<Record>();

        Object result = getData(dataContext);

        if (result != null) {
            if (result instanceof Map) {
                Map<String, List> recordMap = (Map<String, List>) result;

                for (Map.Entry<String, List> group : recordMap.entrySet()) {
                    String groupKey = group.getKey();
                    List<Record> records = group.getValue();
                    String path = "";
                    for (Record record : records) {

                        Map<String, String> columns = record.getColumns();

                        boolean fault = false;
                        String timestamp = null;
                        ExtractConfig extractConfig = null;
                        String soapBody = null;

                        for (Map.Entry<String, String> entry : columns.entrySet()) {
                            String columnKey = entry.getKey();
                            String columnValue = entry.getValue();

                            if (columnKey.equals(currentSequenceIdentifier)) {
                                if (path.equals("")) {
                                    path += columnValue;
                                } else {
                                    path += "-->" + columnValue;
                                }
                            } else if (columnKey.equals(TIMESTAMP)) {
                                timestamp = columnValue;
                            } else if (columnKey.equals(SOAP_ENVELOP_NAMESPACE_URI)) {
                                if (columnValue.equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
                                    extractConfig = createAndGetExtractConfigForSoap11();
                                } else if (columnValue.equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
                                    extractConfig = createAndGetExtractConfigForSoap12();
                                } else {
                                    log.error("Not a standard soap message");
                                }
                            } else if (columnKey.equals(MSG_BODY)) {
                                soapBody = columnValue;
                            } else {
                                if (errorFields != null) {
                                    for (String errorField : errorFields) {
                                        if (columnKey.equals(errorField.trim())) {
                                            fault = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        //Extract faultCode and faultReason using xpath
                        if (soapBody != null) {
                            List<ExtractField> fields = extractConfig.getFields();
                            for (ExtractField field : fields) {
                                try {
                                    AXIOMXPath xpath = new AXIOMXPath(field.getXpath());
                                    Map<String, String> namespaces = field.getNamespaces();
                                    for (Map.Entry<String, String> entry : namespaces.entrySet()) {
                                        xpath.addNamespace(entry.getKey(), entry.getValue());
                                    }
                                    String xmlContent = soapBody;
                                    OMElement element = AXIOMUtil.stringToOM(xmlContent);

                                    List results = xpath.selectNodes(element);
                                    String value = null;
                                    if (results != null && results.size() > 0) {
                                        fault = true;
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
                                        //ResultColumn newColumn = new ResultColumn(field.getName(), value);
                                        columns.put(field.getName(), value);
                                    }
                                } catch (JaxenException e) {
                                    log.error("Error while initializing xpath..", e);
                                } catch (XMLStreamException e) {
                                    log.error("Error while initializing xml content..", e);
                                }
                            }
                        }
                        if (fault) {
                            String newKey = timestamp + groupKey + UUID.randomUUID().toString();
                            Map<String, String> resultColumns = new HashMap<String, String>();
                            resultColumns.putAll(columns);
                            resultColumns.put("bam_message_path", path);

                            Record resultRecord = new Record(newKey, columns);
                            detectFaultRecords.add(resultRecord);
                        }

                    }
                }
            } else if (result instanceof List) {

            }
            setData(dataContext, detectFaultRecords);
        }
    }

    private ExtractConfig createAndGetExtractConfigForSoap12() {
        List<ExtractField> extractFields = new ArrayList<ExtractField>();

        Map<String, String> namespace = new HashMap<String, String>();
        namespace.put(SOAP_ENV_NAMESPACE_PREFIX, SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);

        createExtractFieldObj(extractFields, namespace, FAULT_CODE, "//soapenv:Code//soapenv:Value");
        createExtractFieldObj(extractFields, namespace, FAULT_REASON, "//soapenv:Reason//soapenv:Text");

        ExtractConfig extractConfig = new ExtractConfig();
        extractConfig.setFields(extractFields);
        return extractConfig;
    }

    private ExtractConfig createAndGetExtractConfigForSoap11() {
        List<ExtractField> extractFields = new ArrayList<ExtractField>();

        Map<String, String> namespace = new HashMap<String, String>();
        namespace.put(SOAP_ENV_NAMESPACE_PREFIX, SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);

        createExtractFieldObj(extractFields, namespace, FAULT_CODE, "//faultcode");
        createExtractFieldObj(extractFields, namespace, FAULT_REASON, "//faultstring");

        ExtractConfig extractConfig = new ExtractConfig();
        extractConfig.setFields(extractFields);
        return extractConfig;
    }

    private void createExtractFieldObj(List<ExtractField> extractFields,
                                       Map<String, String> namespace, String name, String xpath) {
        ExtractField extractFaultCodeField = new ExtractField();
        extractFaultCodeField.setFrom(MSG_BODY);
        extractFaultCodeField.setName(name);
        extractFaultCodeField.setXpath(xpath);
        extractFaultCodeField.setNamespaces(namespace);
        extractFields.add(extractFaultCodeField);
    }
}
