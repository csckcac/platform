<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<jsp:include page="../dialog/display_messages.jsp"/>

<link href="../tenant-dashboard/css/dashboard-common.css" rel="stylesheet" type="text/css" media="all"/>
<%
        Object param = session.getAttribute("authenticated");
        String passwordExpires = (String) session.getAttribute(ServerConstants.PASSWORD_EXPIRATION);
        boolean loggedIn = false;
        if (param != null) {
            loggedIn = (Boolean) param;             
        } 
%>
  
<div id="passwordExpire">
         <%
         if (loggedIn && passwordExpires != null) {
         %>
              <div class="info-box"><p>Your password expires at <%=passwordExpires%>. Please change by visiting <a href="../user/change-passwd.jsp?isUserChange=true&returnPath=../admin/index.jsp">here</a></p></div>
         <%
             }
         %>
</div>
<div id="middle">
<div id="workArea">


     <style type="text/css">
       .tip-table td.enterprice-info {
           background-image: url(../../carbon/tenant-dashboard/images/enterprice-info.png);
       }

       .tip-table td.easyuser {
           background-image: url(../../carbon/tenant-dashboard/images/easyuser.png);
       }
       .tip-table td.author-gadgets {
           background-image: url(../../carbon/tenant-dashboard/images/author-gadgets.png);
       }
       .tip-table td.client-side-gadgets {
           background-image: url(../../carbon/tenant-dashboard/images/client-side-gadgets.png);
       }


       .tip-table td.ent-gadget-repo {
           background-image: url(../../carbon/tenant-dashboard/images/ent-gadget-repo.png);
       }
       .tip-table td.anonymous {
           background-image: url(../../carbon/tenant-dashboard/images/anonymous.png);
       }
       .tip-table td.secure-sign-in {
           background-image: url(../../carbon/tenant-dashboard/images/secure-sign-in.png);
       }
       .tip-table td.mgt-consle {
           background-image: url(../../carbon/tenant-dashboard/images/mgt-consle.png);
       }
   </style>
    <h2 class="dashboard-title">WSO2 Gadget Server quick start dashboard</h2>
    <table class="tip-table">
        <tr>
            <td class="tip-top enterprice-info"></td>
            <td class="tip-empty"></td>
            <td class="tip-top easyuser"></td>
            <td class="tip-empty "></td>
            <td class="tip-top author-gadgets"></td>
            <td class="tip-empty "></td>
            <td class="tip-top client-side-gadgets"></td>
        </tr>
        <tr>
            <td class="tip-content">
                <div class="tip-content-lifter">
                    <h3 class="tip-title">Enterprise Information Portal</h3> <br/>


                    <p>Users can organize gadgets in a familiar portal interface. Sets of gadgets can be organized using tabs.</p>

                </div>
            </td>
            <td class="tip-empty"></td>
            <td class="tip-content">
                <div class="tip-content-lifter">
                    <h3 class="tip-title">Easy User Options</h3><br/>

                    <p>Add gadgets from the enterprise repository or from an external URL. Drag-and-drop gadgets into
                        new arrangements.</p>

                </div>
            </td>
            <td class="tip-empty"></td>
            <td class="tip-content">
                <div class="tip-content-lifter">
                    <h3 class="tip-title">Author Gadgets</h3> <br/>

                    <p>Suports XML, HTML, and Javascript and use 3rd party Javascript libraries. Include Flash or other
                        embeddable formats.</p>

                </div>
            </td>
            <td class="tip-empty"></td>
            <td class="tip-content">
                <div class="tip-content-lifter">
                    <h3 class="tip-title">Client-side Gadgets</h3> <br/>

                    <p>Any gadget adhering to the Google Gadget specification can be added to the portal. </p>

                </div>
            </td>
        </tr>
        <tr>
            <td class="tip-bottom"></td>
            <td class="tip-empty"></td>
            <td class="tip-bottom"></td>
            <td class="tip-empty"></td>
            <td class="tip-bottom"></td>
            <td class="tip-empty"></td>
            <td class="tip-bottom"></td>
        </tr>
    </table>
    <div class="tip-table-div"></div>
    <table class="tip-table">
        <tr>
            <td class="tip-top ent-gadget-repo"></td>
            <td class="tip-empty"></td>
            <td class="tip-top anonymous"></td>
            <td class="tip-empty "></td>
            <td class="tip-top secure-sign-in"></td>
            <td class="tip-empty "></td>
            <td class="tip-top mgt-consle"></td>
        </tr>
        <tr>
            <td class="tip-content">
                <div class="tip-content-lifter">
                    <h3 class="tip-title">Enterprise Gadget Repository</h3> <br/>


                    <p>Portal users can browse an enterprise gadget repository maintained by IT, add gadgets to their individual portals, and rate and comment on individual gadgets.</p>

                </div>
            </td>
            <td class="tip-empty"></td>
            <td class="tip-content">
                <div class="tip-content-lifter">
                    <h3 class="tip-title">Anonymous Mode</h3><br/>

                    <p>Users can try out WSO2 Gadget Server and experience most of the portal’s functionality, excluding the gadget repository, even before registration and sign-in. </p>

                </div>
            </td>
            <td class="tip-empty"></td>
            <td class="tip-content">
                <div class="tip-content-lifter">
                    <h3 class="tip-title">Secure Sign-in Options</h3> <br/>

                    <p>User registration and sign-up can be handled using OpenID and InfoCards in addition to the traditional user name and password-based scheme.</p>

                </div>
            </td>
            <td class="tip-empty"></td>
            <td class="tip-content">
                <div class="tip-content-lifter">
                    <h3 class="tip-title">Management Console</h3> <br/>

                    <p>The intuitive browser-based interface of the console helps IT professionals configure and manage WSO2 Gadget Server, its users, and the enterprise gadget repository.</p>

                </div>
            </td>
        </tr>
        <tr>
            <td class="tip-bottom"></td>
            <td class="tip-empty"></td>
            <td class="tip-bottom"></td>
            <td class="tip-empty"></td>
            <td class="tip-bottom"></td>
            <td class="tip-empty"></td>
            <td class="tip-bottom"></td>
        </tr>
    </table>
<p>
    <br/>
</p> </div>
</div>
