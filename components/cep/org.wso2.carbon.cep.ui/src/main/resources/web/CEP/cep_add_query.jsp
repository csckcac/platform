<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.ExpressionDTO" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.OutputDTO" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.OutputElementMappingDTO" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.OutputTupleMappingDTO" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.OutputMapMappingDTO" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.OutputXMLMappingDTO" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.QueryDTO" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.XMLPropertyDTO" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.LinkedList" %>
<%@ page import="java.util.List" %>
<%
    String queryName = request.getParameter("queryName");
    String outputMapping = request.getParameter("outputMapping");

    if (queryName != null) {
        queryName = queryName.trim();
    }

    HashSet propertyHashSet = (HashSet) session.getAttribute("outputXMLPropertyHashSet");
    OutputElementMappingDTO elementMapping = null;
    if (propertyHashSet != null) {
        String nameSpace = request.getParameter("nameSpace");
        String documentElement = request.getParameter("documentElement");

        if (nameSpace != null) {
            nameSpace = nameSpace.trim();
        }
        if (documentElement != null) {
            documentElement = documentElement.trim();
        }

        elementMapping = new OutputElementMappingDTO();
        elementMapping.setNamespace(nameSpace.trim());
        elementMapping.setDocumentElement(documentElement.trim());

        XMLPropertyDTO[] properties = new XMLPropertyDTO[propertyHashSet.size()];
        propertyHashSet.toArray(properties);
        elementMapping.setProperties(properties);
    }

    List metaDataPropertyList = (List) session.getAttribute("outputTupleMetaDataPropertyList");
    List correlationDataPropertyList = (List) session.getAttribute("outputTupleCorrelationDataPropertyList");
    List payloadDataPropertyList = (List) session.getAttribute("outputTuplePayloadDataPropertyList");

    OutputTupleMappingDTO tupleMapping = null;
    if (metaDataPropertyList != null) {
        if (tupleMapping == null) {
            tupleMapping = new OutputTupleMappingDTO();
        }
        String[] properties = new String[metaDataPropertyList.size()];
        metaDataPropertyList.toArray(properties);
        tupleMapping.setMetaDataProperties(properties);
    }
    if (correlationDataPropertyList != null) {
        if (tupleMapping == null) {
            tupleMapping = new OutputTupleMappingDTO();
        }
        String[] properties = new String[correlationDataPropertyList.size()];
        correlationDataPropertyList.toArray(properties);
        tupleMapping.setCorrelationDataProperties(properties);
    }
    if (payloadDataPropertyList != null) {
        if (tupleMapping == null) {
            tupleMapping = new OutputTupleMappingDTO();
        }
        String[] properties = new String[payloadDataPropertyList.size()];
        payloadDataPropertyList.toArray(properties);
        tupleMapping.setPayloadDataProperties(properties);
    }


    List mapPropertyList = (List) session.getAttribute("outputMapPropertyList");

    OutputMapMappingDTO mapMapping = null;
    if (mapPropertyList != null) {
        if (mapMapping == null) {
            mapMapping = new OutputMapMappingDTO();
        }
        String[] properties = new String[mapPropertyList.size()];
        mapPropertyList.toArray(properties);
        mapMapping.setProperties(properties);
    }

    String outputTopicName = request.getParameter("outputTopic");
    String brokerName = request.getParameter("brokerName");

    if (outputTopicName != null) {
        outputTopicName = outputTopicName.trim();
    }
    if (brokerName != null) {
        brokerName = brokerName.trim();
    }

    String xmlMappingText = request.getParameter("xmlMappingText");
    OutputXMLMappingDTO xmlMapping = null;
    if (xmlMappingText != null && !xmlMappingText.equals("")) {
        xmlMappingText = xmlMappingText.trim();

        xmlMapping = new OutputXMLMappingDTO();
        xmlMapping.setMappingXMLText(xmlMappingText);
    }

    OutputDTO output = null;
    if (outputTopicName != null && !outputTopicName.equals("")) {
        output = new OutputDTO();
        output.setTopic(outputTopicName);
        output.setBrokerName(brokerName);
        if (outputMapping.equals("xml")) {
            output.setOutputElementMapping(null);
            output.setOutputTupleMapping(null);
            output.setOutputMapMapping(null);
            output.setOutputXmlMapping(xmlMapping);
        } else if (outputMapping.equals("element")) {
            output.setOutputElementMapping(elementMapping);
            output.setOutputTupleMapping(null);
            output.setOutputMapMapping(null);
            output.setOutputXmlMapping(null);
        } else if (outputMapping.equals("map")) {
            output.setOutputElementMapping(null);
            output.setOutputTupleMapping(null);
            output.setOutputMapMapping(mapMapping);
            output.setOutputXmlMapping(null);
        } else {  //tuple
            tupleMapping.setStreamId(outputTopicName);
            output.setOutputElementMapping(null);
            output.setOutputTupleMapping(tupleMapping);
            output.setOutputMapMapping(null);
            output.setOutputXmlMapping(null);
        }
    }

    String querySourceText = request.getParameter("sourceText");
    String expressionType = request.getParameter("type");

    if (querySourceText != null) {
        querySourceText = querySourceText.trim();
    }
    if (expressionType != null) {
        expressionType = expressionType.trim();
    }

    ExpressionDTO expression = new ExpressionDTO();
    expression.setType(expressionType);
    expression.setText(querySourceText);


    String tableIndex = request.getParameter("tableIndex");
    int index = -1;
    try {
        index = Integer.parseInt(tableIndex.trim());
    } catch (NumberFormatException e) {
        index = -1;
    }

    LinkedList<QueryDTO> queries = (LinkedList<QueryDTO>) session.getAttribute("queries");

    if (queries == null) {
        queries = new LinkedList<QueryDTO>();
        session.setAttribute("queries", queries);
    }

    QueryDTO query;
    if (queries.size() > index) {
        query = queries.get(index);
    } else {
        query = new QueryDTO();
        queries.add(query);
    }

    query.setName(queryName.trim());
    query.setExpression(expression);
    query.setOutput(output);

    session.removeAttribute("outputXMLPropertyHashSet");
    session.removeAttribute("outputMapPropertyList");
    session.removeAttribute("outputTuplePayloadDataPropertyList");
    session.removeAttribute("outputTupleCorrelationDataPropertyList");
    session.removeAttribute("outputTupleMetaDataPropertyList");
%>