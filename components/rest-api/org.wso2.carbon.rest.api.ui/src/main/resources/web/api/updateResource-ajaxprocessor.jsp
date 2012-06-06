<%--
  ~  Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~  Licensed under the Apache License, Version 2.0 (the "License");
  ~  you may not use this file except in compliance with the License.
  ~  You may obtain a copy of the License at
  ~
  ~        http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~  Unless required by applicable law or agreed to in writing, software
  ~  distributed under the License is distributed on an "AS IS" BASIS,
  ~  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~  See the License for the specific language governing permissions and
  ~  limitations under the License.
  --%>
<%@page import="java.util.ArrayList"%>
<%@page import="org.wso2.carbon.rest.api.stub.types.carbon.APIData" %>
<%@page import="org.wso2.carbon.rest.api.stub.types.carbon.ResourceData"%>
<%@page import="java.util.List"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    int index = Integer.parseInt(request.getParameter("index"));
    String methods = request.getParameter("methods");
    String urlStyle = request.getParameter("urlStyle");
    String url = request.getParameter("url");
    String inSequence = request.getParameter("inSequence");
    String outSequence = request.getParameter("outSequence");
    String faultSequence = request.getParameter("faultSequence");
    boolean inlineInSeq = Boolean.valueOf(request.getParameter("isSeqIsInline"));
    boolean inlineOutSeq = Boolean.valueOf(request.getParameter("outSeqIsInline"));
    boolean inlineFaultSeq = Boolean.valueOf(request.getParameter("faultSeqIsInline"));

    List<ResourceData> resourceList = (ArrayList<ResourceData>) session.getAttribute("apiResources");
    ResourceData data;

    String value = request.getParameter("isTemp");
    boolean createTmpRes = false;
    if (value != null && !"".equals(value)) {
        createTmpRes = Boolean.valueOf(value);
    }
    data = (ResourceData) session.getAttribute("resourceData");
    if (data == null) {
        data = new ResourceData();
    }

    String mode = (String) session.getAttribute("mode");
    if (mode != null && !"".equals(mode)) {
        if ("add".equals(mode)) {
            String name = request.getParameter("apiName");
            String context = request.getParameter("apiContext");
            APIData apiData = (APIData) session.getAttribute("apiData");
            if (apiData != null) {
                apiData.setName(name);
                apiData.setContext(context);
            }
        }
    }
    String[] methodList = methods.split(",");
    data.setMethods(methodList);
    if (urlStyle != null && !"".equals(urlStyle) && !"none".equals(urlStyle)) {
        if ("uritemplate".equals(urlStyle)) {
            data.setUriTemplate(url);
            data.setUrlMapping(null);
        } else {
            data.setUrlMapping(url);
            data.setUriTemplate(null);
        }
    } else {
        if (index == -1) {
            for (ResourceData rdata : resourceList) {
                if (!rdata.isUriTemplateSpecified() && !rdata.isUrlMappingSpecified()
                        && !createTmpRes) {
                    response.setStatus(451);
                    return;
                }
            }
        } else {
            for (int i = 0; i < resourceList.size(); ++i) {
                if (!resourceList.get(i).isUriTemplateSpecified()
                    && !resourceList.get(i).isUrlMappingSpecified()
                    && !createTmpRes) {
                    if (i != index) {
                        response.setStatus(451);
                        return;
                    }
                }
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////
    if (inlineInSeq) {
        String inSeqXml = (String) session.getAttribute("inSeqXml");
        if (inSeqXml == null) {
            inSeqXml = "";
        }
        data.setInSeqXml(inSeqXml);
    } else {
        if (inSequence == null) {
            inSequence = "";
        }
        data.setInSequenceKey("none".equals(inSequence) ? "" : inSequence);
    }

    if (inlineOutSeq) {
        String outSeqXml = (String) session.getAttribute("outSeqXml");
        if (outSeqXml == null) {
            outSeqXml = "";
        }
        data.setOutSeqXml(outSeqXml);
    } else {
        if (outSequence == null) {
            outSequence = "";
        }
        data.setOutSequenceKey("none".equals(outSequence) ? "" : outSequence);
    }

    if (inlineFaultSeq) {
        String faultSeqXml = (String) session.getAttribute("faultSeqXml");
        if (faultSeqXml == null) {
            faultSeqXml = "";
        }
        data.setFaultSeqXml(faultSeqXml);
    } else {
        if (faultSequence == null) {
            faultSequence = "";
        }
        data.setFaultSequenceKey("none".equals(faultSequence) ? "" : faultSequence);
    }
    session.removeAttribute("inSeqXml");
    session.removeAttribute("outSeqXml");
    session.removeAttribute("faultSeqXml");
    //////////////////////////////////////////////////////////////
    session.setAttribute("index", index);
    if (createTmpRes) {
        session.setAttribute("resourceData", data);
        return;
    }

    //If index is -1, we are adding a Resource. Updating otherwise.
    if (index == -1) {
        resourceList.add(data);
        //index = resourceList.indexOf(data);
    } else {
        if (resourceList.isEmpty()) {
            resourceList = new ArrayList<ResourceData>();
            resourceList.add(data);
        } else {
            resourceList.set(index, data);
        }
    }
    session.setAttribute("apiResources", resourceList);
    //session.setAttribute("index", String.valueOf(index));
    /// finally this is a real update.
    APIData apiData = (APIData) session.getAttribute("apiData");
    apiData.setResources(resourceList.toArray(new ResourceData[resourceList.size()]));
    session.removeAttribute("index");
    session.removeAttribute("resourceData");
    session.removeAttribute("createTmpResource");
%>
