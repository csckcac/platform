<%@ page import="org.wso2.rnd.nosql.EMRClient" %>
<%@ page import="org.wso2.rnd.nosql.UIHelper" %>
<%@ page import="org.wso2.rnd.nosql.model.User" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.UUID" %>
<!--
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
-->
<!DOCTYPE html>
<html><head>
<title>DeleteRecord</title>
<meta charset="UTF-8">
<meta name="description" content="" />
<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.6.4/jquery.min.js"></script>
<!--[if lt IE 9]><script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script><![endif]-->
<script type="text/javascript" src="../js/prettify.js"></script>                                   <!-- PRETTIFY -->
<script type="text/javascript" src="../js/kickstart.js"></script>                                  <!-- KICKSTART -->
<link rel="stylesheet" type="text/css" href="../css/kickstart.css" media="all" />                  <!-- KICKSTART -->
<link rel="stylesheet" type="text/css" href="../css/style.css" media="all" />                          <!-- CUSTOM STYLES -->
</head><body><a id="top-of-page"></a><div id="wrap" class="clearfix">
	<div class="col_12">
<%
    User user = UIHelper.getUser(session);
    boolean isLoggedIn = user != null;
    String recordId = null;
    String userId = null;
    if (isLoggedIn) {
        recordId = request.getParameter("recordId");
        //userId = request.getParameter("userId");
        //if (request != null) {
        //get blob list
        if(recordId !=null){
        List<UUID> blobIds = EMRClient.getInstance().getRecordBlobIds(recordId);
        for (UUID blobId : blobIds) {
            if(EMRClient.getInstance().isBlobAvailable(blobId)){
            //delete bob list
                EMRClient.getInstance().deleteBlob(blobId);
                EMRClient.getInstance().deleteRecordBlob(blobId);
                
             }
        }
        //remove user record
        EMRClient.getInstance().deleteUserRecord(recordId);
        // remove record
        EMRClient.getInstance().deleteRecord(recordId);
        }
    }
%>
<%
    if (isLoggedIn) {
%>
<script type="text/javascript">
    location.href = '../index.jsp'
</script>
<%
    } else {
        session.setAttribute("ERROR_MSG", "User Not Logged In.");
        response.sendRedirect("../login.jsp");
    }
%>
</div>
</div>
</body>
</html>