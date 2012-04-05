<%--
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
 --%>
<%
    ProductsClient client = new ProductsClient();
    Category[] categories = client.listProductCategories();
%>

<%@page import="com.acme.shoppingcart.portal.ProductsClient" %>
<%@ page import="com.acme.shoppingcart.portal.product.types.Category" %>

<ul class="catagories-list">
    <% if (categories != null) { %>
    <% for (Category category : categories) {%>
    <li>
        <a href="view-category.jsp?category=<%= category.getCategoryName() %>">
            <%= category.getCategoryName() %>
        </a>
    </li>
    <% } %>
    <% } %>
</ul>