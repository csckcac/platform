<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.MapPropertyDTO" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.TuplePropertyDTO" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.XMLPropertyDTO" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.List" %>
<%
    String format = request.getParameter("format");
    String propName = request.getParameter("propName");
    String propType = request.getParameter("propType");
    String propValue = request.getParameter("propValue");

    if (propName != null) {
        propName = propName.trim();
    }

    if (propType != null) {
        propType = propType.trim();
    }

    if (propValue != null) {
        propValue = propValue.trim();
    }

    if (format.equals("xml")) {

        XMLPropertyDTO property = new XMLPropertyDTO();
        property.setName(propName);
        property.setType(propType);
        property.setXpath(propValue); //is Xpath
        property.setInputProperty(true);

        HashSet propertyHashSet = (HashSet) session.getAttribute("inputXMLPropertyHashSet");
        if (propertyHashSet == null) {
            propertyHashSet = new HashSet();
            propertyHashSet.add(property);
            session.setAttribute("inputXMLPropertyHashSet", propertyHashSet);
        } else {
            propertyHashSet.add(property);
        }

    } else  if (format.equals("map")) {

        MapPropertyDTO property = new MapPropertyDTO();
        property.setName(propName);
        property.setType(propType);
        property.setInputProperty(true);

        List propertyList = (List) session.getAttribute("inputMapPropertyList");
        if (propertyList == null) {
            propertyList = new ArrayList();
            propertyList.add(property);
            session.setAttribute("inputMapPropertyList", propertyList);
        } else {
            propertyList.add(property);
        }

    } else { //tuple

        TuplePropertyDTO property = new TuplePropertyDTO();
        property.setName(propName);
        property.setType(propType);
        property.setDataType(propValue); //is DataType
        property.setInputProperty(true);

        List propertyList = (List) session.getAttribute("inputTuplePropertyList");
        if (propertyList == null) {
            propertyList = new ArrayList();
            propertyList.add(property);
            session.setAttribute("inputTuplePropertyList", propertyList);
        } else {
            propertyList.add(property);
        }
    }


%>