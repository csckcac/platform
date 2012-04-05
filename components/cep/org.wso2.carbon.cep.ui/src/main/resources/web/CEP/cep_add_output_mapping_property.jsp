<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.XMLPropertyDTO" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%

    String type = request.getParameter("format");
    String propName = request.getParameter("propName");
    if (propName != null) {
        propName = propName.trim();
    }
    if (type.equals("element")) {

        String propXMLFieldName = request.getParameter("propXMLFieldName");
        String propXMLFieldType = request.getParameter("propXMLFieldType");

        if (propXMLFieldName != null) {
            propXMLFieldName = propXMLFieldName.trim();
        }
        if (propXMLFieldType != null) {
            propXMLFieldType = propXMLFieldType.trim();
        }
        XMLPropertyDTO property = new XMLPropertyDTO();

        property.setName(propName);
        property.setXmlFieldName(propXMLFieldName);
        property.setXmlFieldType(propXMLFieldType);
        property.setInputProperty(false);

        HashSet propertyHashSet = (HashSet) session.getAttribute("outputXMLPropertyHashSet");
        if (propertyHashSet == null) {
            propertyHashSet = new HashSet();
            propertyHashSet.add(property);
            session.setAttribute("outputXMLPropertyHashSet", propertyHashSet);
        } else {
            propertyHashSet.add(property);
        }
    } else {//tuple
        String dataType = request.getParameter("dataType");

        if (dataType != null) {
            dataType = dataType.trim();
        }
        List list = (List) session.getAttribute("outputTuple" + dataType + "DataPropertyList");
        if (list == null) {
            list = new ArrayList();
            list.add(propName);
            session.setAttribute("outputTuple" + dataType + "DataPropertyList", list);
        } else {
            list.add(propName);
        }
    }
%>