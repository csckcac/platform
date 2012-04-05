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

<%@page import="com.sforce.soap.partner.PartnerConnection" %>
<%@page import="org.wso2.salesforce.webapp.exception.SalesForceWebAppException" %>
<%@page import="org.wso2.salesforce.webapp.salesforce.SalesForceLoginManager" %>
<%@page import="org.wso2.salesforce.webapp.salesforce.SalesForceUtil" %>
<%@page import="org.wso2.salesforce.webapp.salesforce.entity.Case" %>
<%@page import="org.wso2.salesforce.webapp.salesforce.entity.Contact" %>
<%@page import="org.wso2.salesforce.webapp.salesforce.entity.Lead" %>
<%@page import="org.wso2.salesforce.webapp.salesforce.entity.Opportunity" %>
<%@ page import="java.util.Vector" %>


<%
    String username = (String) session.getAttribute("username");
    if (username == null) {
%>
    <script type="text/javascript" language="javascript">
        document.location.href = "../otauth/login.jsp?error=Please log in first!";
    </script>
<%
    }

    String context = request.getContextPath();

    SalesForceLoginManager manager = (SalesForceLoginManager) session
            .getAttribute("manager");
    if (manager == null || manager.getConnection() == null) {
        response.sendRedirect(response.encodeRedirectURL(context + "/login.jsp?error=Session invalid!"));
    }

    PartnerConnection conn = null;
    if (manager != null) {
        conn = manager.getConnection();
    }

    String category = request.getParameter("category");
    category = (category != null && !"All".equals(category)) ? category : "Leads";
%>


<%
    if ("Contacts".equals(category)) {
        Vector<Contact> contacts = null;
        try {
            if (conn != null) {
                contacts = SalesForceUtil.getContactsForAccount(conn);
            } else {
                //response.sendRedirect(response.encodeRedirectURL(context + "/otauth/login-error.jsp"));
            }
        } catch (SalesForceWebAppException e) {
            response.sendRedirect(response.encodeRedirectURL(context + "/salesforce/index.jsp?error=Unable to retrieve contact data"));
            contacts = new Vector<Contact>();
        }
%>
<table id="tblContacts" class="dataTable">
    <thead>
    <tr>
        <th>ID</th>
        <th>First Name</th>
        <th>Last Name</th>
        <th>Title</th>
        <th>Phone</th>
        <th>Email</th>
    </tr>
    </thead>
    <tbody>
    <%
        for (Contact con : contacts) {
    %>
    <tr>
        <td><%=con.getId()%>
        </td>
        <td><%=con.getFirstName()%>
        </td>
        <td><%=con.getLastName()%>
        </td>
        <td><%=con.getTitle()%>
        </td>
        <td><%=con.getPhone()%>
        </td>
        <td><%=con.getEmail()%>
        </td>
    </tr>
    <%
        }
    %>
    </tbody>
</table>
<%
} else if ("Opportunities".equals(category)) {
    Vector<Opportunity> opportunities = null;
    try {
        if (conn != null) {
            opportunities = SalesForceUtil.getOpportunitiesForAccount(conn);
        } else {
            //response.sendRedirect(context + "/otauth/login-error.jsp");
        }
    } catch (SalesForceWebAppException e) {
        response.sendRedirect(response.encodeRedirectURL(context + "/salesforce/index.jsp?error=Unable to retrieve opportunity data"));
        opportunities = new Vector<Opportunity>();
    }
%>
<table id="tblOpportunities" class="dataTable">
    <thead>
    <tr>
        <th>ID</th>
        <th>Name</th>
        <th>Stage</th>
        <th>Amount</th>
        <th>Closing Date</th>
        <!-- th>Email</th -->
    </tr>
    </thead>
    <tbody>
    <%
        for (Opportunity op : opportunities) {
    %>
    <tr>
        <td><%=op.getId()%>
        </td>
        <td><%=op.getName()%>
        </td>
        <td><%=op.getStage()%>
        </td>
        <td><%=op.getAmount()%>
        </td>
        <td><%=op.getCloseDate()%>
        </td>
    </tr>
    <%
        }
    %>
    </tbody>
</table>
<%
} else if ("Cases".equals(category)) {
    Vector<Case> cases = null;
    try {
        if (conn != null) {
            cases = SalesForceUtil.getCasesForAccount(conn);
        } else {
            //response.sendRedirect(context + "/otauth/login-error.jsp");
        }
    } catch (SalesForceWebAppException e) {
        response.sendRedirect(response.encodeRedirectURL(context + "/salesforce/index.jsp?error=Unable to retrieve case data"));
        cases = new Vector<Case>();
    }
%>
<table id="tblCases" class="dataTable">
    <thead>
    <tr>
        <th>Case Number</th>
        <th>Contact Name</th>
        <th>Subject</th>
        <th>Priority</th>
        <th>Status</th>
        <!-- th>Email</th -->
    </tr>
    </thead>
    <tbody>
    <%
        for (Case c : cases) {
    %>
    <tr>
        <td><%=c.getCaseNo()%>
        </td>
        <td><%=c.getContactName()%>
        </td>
        <td><%=c.getSubject()%>
        </td>
        <td><%=c.getPriority()%>
        </td>
        <td><%=c.getStatus()%>
        </td>
    </tr>
    <%
        }
    %>
    </tbody>
</table>
<%
} else if ("Leads".equals(category)) {
    Vector<Lead> leads = null;
    try {
        if (conn != null) {
            leads = SalesForceUtil.getLeadsForAccount(conn);
        } else {
            //response.sendRedirect(context + "/otauth/login-error.jsp");
        }
    } catch (SalesForceWebAppException e) {
        response.sendRedirect(context + "/salesforce/index.jsp?error=Unable to retrieve lead data");
        leads = new Vector<Lead>();
    }
%>
<table id="tblLeads" class="dataTable">
    <thead>
    <tr>
        <th>ID</th>
        <th>First Name</th>
        <th>Last Name</th>
        <th>Title</th>
        <th>Phone</th>
        <th>Email</th>
    </tr>
    </thead>
    <tbody>
    <%
        for (Lead l : leads) {
    %>
    <tr>
        <td><%=l.getId()%>
        </td>
        <td><%=l.getFirstName()%>
        </td>
        <td><%=l.getLastName()%>
        </td>
        <td><%=l.getTitle()%>
        </td>
        <td><%=l.getPhone()%>
        </td>
        <td><%=l.getEmail()%>
        </td>
    </tr>
    <%
        }
    %>
    </tbody>
</table>
<%
    }
%>
<div style="clear:both"></div>
<div style="height:40px;margin-top:10px;margin-bottom:10px;">
    <input id="btnExportTrigger" class="button" value="Export To Google Spread Sheet"
           onclick="showPopup()"
           style="width:230px;height:30px;padding-top:5px;text-align:center;padding-bottom:5px;"/>
</div>

<input id="category" type="hidden" name="category" value="<%=category%>"/>
	


