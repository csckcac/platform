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
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%
    boolean canAddMetadata = CarbonUIUtil.isUserAuthorized(request,
            "/permission/admin/manage/resources/govern/metadata/add");
    boolean canSearchMetadata = CarbonUIUtil.isUserAuthorized(request,
            "/permission/admin/manage/search/advanced-search");
    boolean canSearchActivities = CarbonUIUtil.isUserAuthorized(request,
            "/permission/admin/manage/search/activities");
    boolean canManageNotifications = CarbonUIUtil.isUserAuthorized(request,
            "/permission/admin/manage/resources/notifications");
    boolean canAddExtensions = CarbonUIUtil.isUserAuthorized(request,
            "/permission/admin/manage/extensions/add");
%>


<style type="text/css">
    .tip-table td.service {
        background-image: url(../../carbon/tenant-dashboard/images/service.png);
    }

    .tip-table td.wsdl {
        background-image: url(../../carbon/tenant-dashboard/images/wsdl.png);
    }
    .tip-table td.schema {
        background-image: url(../../carbon/tenant-dashboard/images/schema.png);
    }
    .tip-table td.policy {
        background-image: url(../../carbon/tenant-dashboard/images/policy.png);
    }


    .tip-table td.search {
        background-image: url(../../carbon/tenant-dashboard/images/search.png);
    }
    .tip-table td.activities {
        background-image: url(../../carbon/tenant-dashboard/images/activities.png);
    }
    .tip-table td.notifications {
        background-image: url(../../carbon/tenant-dashboard/images/notifications.png);
    }
    .tip-table td.extensions {
        background-image: url(../../carbon/tenant-dashboard/images/extensions.png);
    }
</style>
        <table class="tip-table">
            <tr>
                <td class="tip-top service"></td>
                <td class="tip-empty"></td>
                <td class="tip-top wsdl"></td>
                <td class="tip-empty "></td>
                <td class="tip-top schema"></td>
                <td class="tip-empty "></td>
                <td class="tip-top policy"></td>
            </tr>
            <tr>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <% if (canAddMetadata) { %><a class="tip-title" href="../services/services.jsp?region=region3&item=governance_services_menu"><% } %>Service<% if (canAddMetadata) { %></a><% } %> <br/>


                <p>Service is the basic entity of your SOA platform. You can manage service metadata and the
               service lifecycle as well as maintain multiple versions of a given service, and much more.</p>

                    </div>
                </td>
                <td class="tip-empty"></td>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <% if (canAddMetadata) { %><a class="tip-title" href="../wsdl/wsdl.jsp?region=region3&item=governance_wsdl_menu"><% } %>WSDL<% if (canAddMetadata) { %></a><% } %> <br/>


                <p>WSDL defines the interface of a web service. You can store, validate and manage WSDLs with ease, keeping track
               of dependencies and associations such as services, schema and policies.</p>

                    </div>
                </td>
                <td class="tip-empty"></td>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <% if (canAddMetadata) { %><a class="tip-title" href="../schema/schema.jsp?region=region3&item=governance_schema_menu"><% } %>Schema<% if (canAddMetadata) { %></a><% } %>  <br/>

                       

                <p>XML Schema defines data types in a WSDL. As in the case of WSDLs, you can keep associations of schema
                    which helps in the impact analysis process, when maintaining the data models associated with your SOA.</p>

                    </div>
                </td>
                <td class="tip-empty"></td>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <% if (canAddMetadata) { %><a class="tip-title" href="../policy/policy.jsp?region=region3&item=governance_policy_menu"><% } %>Policy<% if (canAddMetadata) { %></a><% } %><br/>


                <p>Policies help standardize SOA behaviour. You can keep track of the policies bound to a service.
                   It also supports policy enforcement to control how SOA behaves as desired by both IT and business personnel. </p>

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
                <td class="tip-top search"></td>
                <td class="tip-empty"></td>
                <td class="tip-top activities"></td>
                <td class="tip-empty "></td>
                <td class="tip-top notifications"></td>
                <td class="tip-empty "></td>
                <td class="tip-top extensions"></td>
            </tr>
            <tr>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <% if (canSearchMetadata) { %><a class="tip-title" href="../search/advancedSearch.jsp?region=region3&item=metadata_search_menu"><% } %>Search<% if (canSearchMetadata) { %></a><% } %><br/>


                        <p>The repository can store any arbitrary type of resource. You can search for resources by name, author, time created or updated. You also can search for resources by media type.</p>

                    </div>
                </td>
                <td class="tip-empty"></td>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <% if (canSearchActivities) { %><a class="tip-title" href="../activities/activity.jsp?region=region3&item=registry_activity_menu"><% } %>Activities<% if (canSearchActivities) { %></a><% } %><br/>


                        <p>An activity log provide an invaluable insight to what operations took place on the repository. You can browse activities while filtering them by date range, user name, or activity type.</p>

                    </div>
                </td>
                <td class="tip-empty"></td>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <% if (canManageNotifications) { %><a class="tip-title" href="../notifications/notifications.jsp?region=region1&item=governance_notification_menu"><% } %>Notifications<% if (canManageNotifications) { %></a><% } %><br/>


                        <p>The registry generates events when changes are made to a particular resource or collection. You can subscribe to these events via e-mail, or forward them to a web service via SOAP or REST.</p>

                    </div>
                </td>
                <td class="tip-empty"></td>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <% if (canAddExtensions) { %><a class="tip-title" href="../extensions/add_extensions.jsp?region=region3&item=add_extensions_menu"><% } %>Extensions<% if (canAddExtensions) { %></a><% } %><br/>

                        <p>While we address most governance and registry related scenarios out-of-the-box, you also can upload your own extensions that is capable of extending the basic functionality of the product</p>

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
</p>
