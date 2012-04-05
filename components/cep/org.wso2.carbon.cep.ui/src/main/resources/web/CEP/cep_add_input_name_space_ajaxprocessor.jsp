<%@ page import="java.util.HashSet" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.XpathDefinitionDTO" %><%

    String prefix=  request.getParameter("prefix");
    String namespace =   request.getParameter("nameSpace");
    if(prefix != null){
        prefix = prefix.trim();
    }
    if(namespace != null){
        namespace = namespace.trim();
    }

    XpathDefinitionDTO xpathDefinition = new XpathDefinitionDTO();
    xpathDefinition.setPrefix(prefix);
    xpathDefinition.setNamespace(namespace);

    HashSet nsPrefixHashSet = (HashSet) session.getAttribute("nsPrefixHashSet");
    if (nsPrefixHashSet == null) {
        nsPrefixHashSet = new HashSet();
        nsPrefixHashSet.add(xpathDefinition);
        session.setAttribute("nsPrefixHashSet", nsPrefixHashSet);
    } else {
        nsPrefixHashSet.add(xpathDefinition);
    }
%>