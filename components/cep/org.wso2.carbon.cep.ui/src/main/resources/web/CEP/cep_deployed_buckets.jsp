<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.client.Options" %>
<%@ page import="org.apache.axis2.client.ServiceClient" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.CEPAdminServiceStub" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.BucketBasicInfoDTO" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<fmt:bundle basename="org.wso2.carbon.cep.ui.i18n.Resources">
<script type="text/javascript">
    function editBucket(link) {
        var rowToEdit = link.parentNode.parentNode;
        var bucketToEdit = rowToEdit.cells[0].innerHTML.trim();
        loadBucketFromBackend(bucketToEdit.getValue());

    }
    function loadBucketFromBackend(bucketName) {

        $.ajax({
                   type:"POST",
                   url:"cep_load_bucket_from_bEnd.jsp",
                   data:{'bucketName':bucketName},
                   async:false,
                   success:function (msg) {
//                    alert("Data Saved: " + msg);
                   }
               });
        location.href = 'cep_buckets.jsp?edit=true';
    }
    function loadBucketFromBackEndForView(bucketName) {
        $.ajax({
                   type:"POST",
                   url:"cep_load_bucket_from_bEnd.jsp",
                   data:{'bucketName':bucketName},
                   async:false,
                   success:function (msg) {
//                    alert("Data Saved: " + msg);
                   }
               });
        location.href = 'cep_view_bucket.jsp?edit=true';
    }


    var allbucketsSelected = false;

    function selectAllInThisPage(isSelected) {
        allbucketsSelected = false;
        if (document.bucketsForm.buckets != null &&
            document.bucketsForm.buckets[0] != null) { // there is more than 1 sg
            if (isSelected) {
                for (var j = 0; j < document.bucketsForm.buckets.length; j++) {
                    document.bucketsForm.buckets[j].checked = true;
                }
            } else {
                for (j = 0; j < document.bucketsForm.buckets.length; j++) {
                    document.bucketsForm.buckets[j].checked = false;
                }
            }
        } else if (document.bucketsForm.buckets != null) { // only 1 sg
            document.bucketsForm.buckets.checked = isSelected;
        }
        return false;
    }

    function selectAllInAllPages() {
        selectAllInThisPage(true);
        allbucketsSelected = true;
        return false;
    }

    function resetVars() {
        allbucketsSelected = false;

        var isSelected = false;
        if (document.bucketsForm.buckets[0] != null) { // there is more than 1 sg
            for (var j = 0; j < document.bucketsForm.buckets.length; j++) {
                if (document.bucketsForm.buckets[j].checked) {
                    isSelected = true;
                }
            }
        } else if (document.bucketsForm.buckets != null) { // only 1 sg
            if (document.bucketsForm.buckets.checked) {
                isSelected = true;
            }
        }
        return false;
    }
    function deleteBuckets() {
        var selected = false;
        if (document.bucketsForm.buckets[0] != null) { // there is more than 1 sg
            for (var j = 0; j < document.bucketsForm.buckets.length; j++) {
                selected = document.bucketsForm.buckets[j].checked;
                if (selected) break;
            }
        } else if (document.bucketsForm.buckets != null) { // only 1 sg
            selected = document.bucketsForm.buckets.checked;
        }
        if (!selected) {
            CARBON.showInfoDialog('<fmt:message key="select.buckets.to.be.deleted"/>');
            return;
        }
        if (allbucketsSelected) {
            CARBON.showConfirmationDialog("<fmt:message key="delete.all.buckets.prompt"/>", function () {
                location.href = 'cep_delete_buckets_ajaxprocessor.jsp?deleteAllbuckets=true';
            });
        } else {
            CARBON.showConfirmationDialog("<fmt:message key="delete.buckets.on.page.prompt"/>", function () {
                document.bucketsForm.submit();
            });
        }
    }

</script>

<%
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
            .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    //Server URL which is defined in the server.xml
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(),
                                                 session) + "CEPAdminService.CEPAdminServiceHttpsSoap12Endpoint";
    CEPAdminServiceStub stub = new CEPAdminServiceStub(configContext, serverURL);

    String cookie = (String) session.getAttribute(org.wso2.carbon.utils.ServerConstants.ADMIN_SERVICE_COOKIE);

    ServiceClient client = stub._getServiceClient();
    Options option = client.getOptions();
    option.setManageSession(true);
    option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

    int pageNumberInt = 0;
    String pageNumberAsStr = request.getParameter("pageNumber");
    if (pageNumberAsStr != null) {
        pageNumberInt = Integer.parseInt(pageNumberAsStr);
    }

    session.removeAttribute("editingBucket");
    session.removeAttribute("bucket");
    BucketBasicInfoDTO[] availableBuckets = stub.getAllBucketNames(pageNumberInt * 10, 10);
    session.setAttribute("availableBuckets", availableBuckets);

    int bucketCount = stub.getAllBucketCount();
    int pageCount = (int) Math.ceil(((float) bucketCount) / 10);
    if (pageCount <= 0) {
        //this is to make sure it works with defualt values
        pageCount = 1;
    }
    String parameters = "serviceTypeFilter=" + "&serviceGroupSearchString=";
    boolean isAuthorizedForViewBuckets =
            CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/cep/viewBucket");
    boolean isAuthorizedForDeleteBuckets =
            CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/cep/deleteBucket");
    boolean isAuthorizedForEditingBuckets =
            CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/cep/editBucket");
%>
<div id="middle">
    <h2><img src="images/cep-buckets.gif" alt=""/> <fmt:message key="buckets"/></h2>

    <div id="workArea">
        <carbon:paginator pageNumber="<%=pageNumberInt%>" numberOfPages="<%=pageCount%>"
                          page="cep_deployed_buckets.jsp" pageNumberParameterName="pageNumber"
                          resourceBundle="org.wso2.carbon.cep.ui.i18n.Resources"
                          prevKey="prev" nextKey="next"
                          parameters="<%=parameters%>"/>
        <%
            if (isAuthorizedForDeleteBuckets) {
        %>
        <carbon:itemGroupSelector selectAllInPageFunction="selectAllInThisPage(true)"
                                  selectAllFunction="selectAllInAllPages()"
                                  selectNoneFunction="selectAllInThisPage(false)"
                                  addRemoveFunction="deleteBuckets()"
                                  addRemoveButtonId="delete1"
                                  resourceBundle="org.wso2.carbon.cep.ui.i18n.Resources"
                                  selectAllInPageKey="selectAllInPage"
                                  selectAllKey="selectAll"
                                  selectNoneKey="selectNone"
                                  addRemoveKey="delete"
                                  numberOfPages="<%=pageCount%>"/>
        <%
            }
        %>

        <form name="bucketsForm" action="cep_delete_buckets_ajaxprocessor.jsp" method="post">
            <input type="hidden" name="pageNumber" value="<%= pageNumberInt%>"/>
            <table class="styledLeft" style="width:100%">
                <thead>
                <tr>
                    <th style="width:25%">Bucket Name</th>
                    <th>Description</th>
                    <th style="width:100px">Action</th>
                </tr>
                </thead>
                <%
                    if (availableBuckets != null && availableBuckets.length > 0) {
                %>
                <tbody>
                <%
                    int position = 0;
                    for (BucketBasicInfoDTO bucket : availableBuckets) {
                        String bgColor = ((position % 2) == 1) ? "#EEEFFB" : "white";
                        position++;
                %>
                <tr bgcolor="<%= bgColor%>">
                    <%
                        if (isAuthorizedForDeleteBuckets) {
                    %>
                    <td><input type="checkbox" name="buckets"
                               value="<%=bucket.getName()%>"
                               onclick="resetVars()" class="chkBox"/>
                        <a href="#"
                           onclick="loadBucketFromBackEndForView('<%=bucket.getName()%>')"><%=bucket.getName()%>
                        </a></td>
                    <%
                    } else {
                    %>
                    <td>
                        <a href="#"
                           onclick="loadBucketFromBackEndForView('<%=bucket.getName()%>')"><%=bucket.getName()%>
                        </a></td>
                    <%
                        }
                    %>

                    <td><%=bucket.getDescription()%>
                    </td>

                    <%
                        if (isAuthorizedForEditingBuckets) {
                    %>
                    <td><a class="icon-link" style="background-image:url(../admin/images/edit.gif)"
                           onclick="loadBucketFromBackend('<%=bucket.getName()%>')"><font
                            color="#4682b4">Edit</font></a></td>
                    <%
                    } else {
                    %>
                    <td></td>
                    <%
                        }
                    %>

                </tr>
                <%
                    }
                %>
                </tbody>
                <%
                    }
                %>
            </table>
        </form>

        <%
            if (isAuthorizedForDeleteBuckets) {
        %>
        <carbon:itemGroupSelector selectAllInPageFunction="selectAllInThisPage(true)"
                                  selectAllFunction="selectAllInAllPages()"
                                  selectNoneFunction="selectAllInThisPage(false)"
                                  addRemoveFunction="deleteBuckets()"
                                  addRemoveButtonId="delete1"
                                  resourceBundle="org.wso2.carbon.cep.ui.i18n.Resources"
                                  selectAllInPageKey="selectAllInPage"
                                  selectAllKey="selectAll"
                                  selectNoneKey="selectNone"
                                  addRemoveKey="delete"
                                  numberOfPages="<%=pageCount%>"/>
        <%
            }
        %>
        <carbon:paginator pageNumber="<%=pageNumberInt%>" numberOfPages="<%=pageCount%>"
                          page="cep_deployed_buckets.jsp" pageNumberParameterName="pageNumber"
                          resourceBundle="org.wso2.carbon.cep.ui.i18n.Resources"
                          prevKey="prev" nextKey="next"
                          parameters="<%=parameters%>"/>
    </div>
</div>
</fmt:bundle>
