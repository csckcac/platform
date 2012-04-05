
<%@ page import="org.apache.axis2.AxisFault" %>
<%@ page import="org.json.JSONException" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="org.wso2.carbon.issue.tracker.stub.AccountInfo" %>
<%@ page import="org.wso2.carbon.issue.tracker.stub.GenericCredentials" %>
<%@ page import="org.wso2.carbon.issue.tracker.ui.IssueTrackerClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%

    String accountName = request.getParameter("accountNames");
    String data = null;
    IssueTrackerClient issueTrackerClient = IssueTrackerClient.getInstance(config, session);

    boolean  isService=issueTrackerClient.isStratosService();
   try {

        if (null != accountName && !"--Select--".equals(accountName) && !isService) {


            AccountInfo accountInfo = issueTrackerClient.getAccount(accountName);
            GenericCredentials credentials = accountInfo.getCredentials();
            data = issueTrackerClient.getAccountSpecificData(accountName, credentials.getUrl());

        }else if(isService){
                data = issueTrackerClient.getAccountSpecificDataInService();
        }else {
            String msg = "Unable to obtain project list for " + accountName;
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.INFO, request,
                    response, "../issue/newIssue.jsp");
        }


        if (null != data) {
            out.println(data);

        } else {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("success", "fail");
            } catch (JSONException exception) {
                throw new AxisFault("Error creating JSON string.");
            }

            data = jsonObject.toString();

            out.println(data);

        }


    } catch (Exception e) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("success", "fail");
        } catch (JSONException exception) {
            throw new AxisFault("Error creating JSON string.");
        }

        data = jsonObject.toString();
        out.println(data);
        

    }

            
%>