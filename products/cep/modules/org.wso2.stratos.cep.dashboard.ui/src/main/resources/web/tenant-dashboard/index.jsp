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
        .tip-table td.cep1 {
            background-image: url(../../carbon/tenant-dashboard/images/cep1.png);
        }
        .tip-table td.cep2 {
            background-image: url(../../carbon/tenant-dashboard/images/cep2.png);
        }
        .tip-table td.cep3 {
            background-image: url(../../carbon/tenant-dashboard/images/cep3.png);
        }
        .tip-table td.cep4 {
            background-image: url(../../carbon/tenant-dashboard/images/cep4.png);
        }



        .tip-table td.cep5 {
            background-image: url(../../carbon/tenant-dashboard/images/cep5.png);
        }
        .tip-table td.cep6 {
            background-image: url(../../carbon/tenant-dashboard/images/cep6.png);
        }
        .tip-table td.cep7 {
            background-image: url(../../carbon/tenant-dashboard/images/cep7.png);
        }
        .tip-table td.cep8 {
            background-image: url(../../carbon/tenant-dashboard/images/cep8.png);
        }
    </style>
    <h2 class="dashboard-title">WSO2 CEP quick start dashboard</h2>
    <table class="tip-table">
        <tr>
            <td class="tip-top cep1"></td>
            <td class="tip-empty"></td>
            <td class="tip-top cep2"></td>
            <td class="tip-empty "></td>
            <td class="tip-top cep3"></td>
            <td class="tip-empty "></td>
            <td class="tip-top cep4"></td>
        </tr>
        <tr>
            <td class="tip-content">
                <div class="tip-content-lifter">
                    <h3 class="tip-title">Bring CEP to SOA</h3> <br/>


                    <p>Bring CEP to SOA by processing XML events and produce results as XML events</p>

                </div>
            </td>
            <td class="tip-empty"></td>
            <td class="tip-content">
                <div class="tip-content-lifter">
                    <h3 class="tip-title">Registry Storage</h3> <br/>

                    <p>Ability to define different event streams, queries and out put streams and store them in the registry as a bucket.</p>

                </div>
            </td>
            <td class="tip-empty"></td>
            <td class="tip-content">
                <div class="tip-content-lifter">
                    <h3 class="tip-title">Esper and Fusion</h3> <br/>

                    <p>Support Esper and fusion back end runtimes.</p>

                </div>
            </td>
            <td class="tip-empty"></td>
            <td class="tip-content">
                <div class="tip-content-lifter">
                    <h3 class="tip-title">Filter data from the XML event</h3> <br/>

                     <p>Ability to filter data from the XML event using xpath and format the result as an XML event.</p>

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
            <td class="tip-top cep5"></td>
            <td class="tip-empty"></td>
            <td class="tip-top cep6"></td>
            <td class="tip-empty"></td>
            <td class="tip-top cep7"></td>
            <td class="tip-empty "></td>
            <td class="tip-top cep8"></td>
        </tr>
        <tr>
            <td class="tip-content">
                <div class="tip-content-lifter">
                    <h3 class="tip-title">Support different types of brokers</h3> <br/>

                    <p>Support different types of brokers WS-Event, JMS to receive publish events.</p>

                </div>
            </td>
            <td class="tip-empty"></td>
            <td class="tip-content">
                <div class="tip-content-lifter">
                    <h3 class="tip-title">Storing broker configurations</h3> <br/>
                    <p>Define and store such different broker configurations.</p>
                </div>
            </td>
            <td class="tip-empty"></td>
            <td class="tip-content">
                <div class="tip-content-lifter">
                    <h3 class="tip-title">Administrative Console support</h3> <br/>
                    <p>Administrative Console to create buckets, add subscriptions etc..</p>

                </div>
            </td>
            <td class="tip-empty"></td>
            <td class="tip-content">
                <div class="tip-content-lifter">
                    <h3 class="tip-title">Specify CEP queries</h3> <br/>
                    <p>Ability to specify CEP queries inline or pick from the registry</p>

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
