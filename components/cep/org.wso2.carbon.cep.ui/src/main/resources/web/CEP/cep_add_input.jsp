<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.InputDTO" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.InputTupleMappingDTO" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.InputXMLMappingDTO" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.TuplePropertyDTO" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.XMLPropertyDTO" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.XpathDefinitionDTO" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.LinkedList" %>
<%@ page import="java.util.List" %>
<%
    String topic = request.getParameter("topic");
    String mappingStream = request.getParameter("mappingStream");
    String mappingType = request.getParameter("mappingType");
    String tableIndex = request.getParameter("tableIndex");
    String brokerName = request.getParameter("brokerName");
//    String eventClassName = request.getParameter("eventClassName");
    String eventClassName = null; //not allowed via ui

    if (topic != null) {
        topic = topic.trim();
    }
    if (mappingStream != null) {
        mappingStream = mappingStream.trim();
    }

    if (eventClassName != null && eventClassName.trim().equals("")) {
        eventClassName = null;
    }

    HashSet XMLPropertySet = (HashSet) session.getAttribute("inputXMLPropertyHashSet");
    HashSet nsPrefixesSet = (HashSet) session.getAttribute("nsPrefixHashSet");
    List tuplePropertyList = (List) session.getAttribute("inputTuplePropertyList");

    int index = -1;
    try {
        index = Integer.parseInt(tableIndex.trim());
    } catch (NumberFormatException e) {
        index = -1;
    }

    LinkedList<InputDTO> inputs = (LinkedList<InputDTO>) session.getAttribute("inputs");

    if (inputs == null) {
        inputs = new LinkedList<InputDTO>();
        session.setAttribute("inputs", inputs);
    }

    InputDTO input;
    if (inputs.size() > index) {
        input = inputs.get(index);
    } else {
        input = new InputDTO();
        inputs.add(input);
    }

    input.setTopic(topic);
    input.setBrokerName(brokerName);

    if (mappingType.equals("xml")) {
        InputXMLMappingDTO mapping = new InputXMLMappingDTO();
        mapping.setStream(mappingStream);
        mapping.setMappingClass(eventClassName);
        if (nsPrefixesSet != null) {
            XpathDefinitionDTO[] xpathDefinitions = new XpathDefinitionDTO[nsPrefixesSet.size()];
            nsPrefixesSet.toArray(xpathDefinitions);
            mapping.setXpathDefinition(xpathDefinitions);
        }
        if (XMLPropertySet != null && XMLPropertySet.size() > 0) {
            XMLPropertyDTO[] properties = new XMLPropertyDTO[XMLPropertySet.size()];
            XMLPropertySet.toArray(properties);
            mapping.setProperties(properties);
        }
        input.setInputTupleMappingDTO(null);
        input.setInputXMLMappingDTO(mapping);
    } else {

        InputTupleMappingDTO mapping = new InputTupleMappingDTO();
        mapping.setStream(mappingStream);
        mapping.setMappingClass(eventClassName);
        if (tuplePropertyList != null && tuplePropertyList.size() > 0) {
            TuplePropertyDTO[] properties = new TuplePropertyDTO[tuplePropertyList.size()];
            tuplePropertyList.toArray(properties);
            mapping.setProperties(properties);
        }
        input.setInputTupleMappingDTO(mapping);
        input.setInputXMLMappingDTO(null);
    }

    try {
        session.removeAttribute("inputXMLPropertyHashSet");
        session.removeAttribute("nsPrefixHashSet");
        session.removeAttribute("inputTuplePropertyList");
    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request);
    }
%>