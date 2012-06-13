<%--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
--%>

<%@ page import="org.wso2.carbon.cassandra.explorer.stub.data.xsd.Column" %>
<%@ page import="org.wso2.carbon.cassandra.explorer.ui.CassandraExplorerAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.Date" %>
<%@ page import="org.wso2.carbon.cassandra.explorer.stub.data.xsd.Row" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%
    response.setHeader("Cache-Control", "no-cache");
    String keyspace = request.getParameter("keySpace");
    String columnFamily = request.getParameter("columnFamily");
  //  String rowId = request.getParameter("row_id");
    String startKey = request.getParameter("startKey");
    String endKey = request.getParameter("endKey");
    boolean isReversed = Boolean.valueOf(request.getParameter("isReversed"));
    int echoValue = Integer.parseInt(request.getParameter("sEcho"));
    int displayStart = Integer.parseInt(request.getParameter("iDisplayStart"));
    int displayLenght = Integer.parseInt(request.getParameter("iDisplayLength"));
    String searchKey = request.getParameter("sSearch");
    CassandraExplorerAdminClient cassandraExplorerAdminClient = null;

    try {
        cassandraExplorerAdminClient =
                new CassandraExplorerAdminClient(config.getServletContext(), session);
    } catch (Exception e) {
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
    }
    Row[] rows;
    int noOfTotalRows;
    int noOfFilteredRows;

    noOfTotalRows = cassandraExplorerAdminClient.getNoOfRows(keyspace, columnFamily);

    if (searchKey != null && !searchKey.isEmpty()) {
        rows = cassandraExplorerAdminClient.searchRows(keyspace, columnFamily, searchKey,
                displayStart, displayLenght);
        noOfFilteredRows = cassandraExplorerAdminClient.getNoOfFilteredResultsoforRows(keyspace,
                                                                                       columnFamily,
                                                                                       searchKey);
    }
     else{
         rows = cassandraExplorerAdminClient.
            getPaginateSliceforRows(keyspace, columnFamily,displayStart,displayLenght);
        noOfFilteredRows = noOfTotalRows;
    }
    int totalDisplayRecords =0;
    if(rows !=null){
         totalDisplayRecords = rows.length;
    }
    response.getWriter().print("{");
    response.getWriter().print("\"sEcho\":"+echoValue+",");
    response.getWriter().print("\"iTotalRecords\":" + noOfTotalRows + ",");
    response.getWriter().print("\"iTotalDisplayRecords\":" + noOfFilteredRows + ",");
    response.getWriter().print("\"aaData\":");

    response.getWriter().print("[");
    if (rows != null && rows[0] != null) {
        for (int i = 0; i < rows.length; i++) {
            response.getWriter().print("[");
            response.getWriter().print("\""+ rows[i].getRowId() + "\",");
            for(int j=0 ; j <rows[i].getColumns().length; j++){
                response.getWriter().print("\""+ rows[i].getColumns()[j].getValue() + "\"");
                response.getWriter().print(",");
                /*if ((j + 1) != rows[i].getColumns().length) {
                    response.getWriter().print(",");
                }*/
            }
            if(rows[i].getColumns().length <3){
               for(int k=0; k<3-rows[i].getColumns().length;k++){
                   response.getWriter().print("\" \"");
                       response.getWriter().print(",");
               }
            }
            response.getWriter().print("\"<a class=\\\"view-icon-link\\\" href=\\\"#\\\" \\\" onclick=\\\"getDataPageForRow(\'"+keyspace+"\',\'"+columnFamily+"\',\'"+rows[i].getRowId()+"\')\\\">View more</a>\"");
            response.getWriter().print("]");
            if ((i + 1) != rows.length) {
                response.getWriter().print(",");
            }
        }
    }
    response.getWriter().print("]");
    response.getWriter().print("}");
    // [{ name: "foo", value : "bar"}, ...... ]
    //response.getWriter().print("");
%>