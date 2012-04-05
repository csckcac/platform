<%
    /**
     Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.

     WSO2 Inc. licenses this file to you under the Apache License,
     Version 2.0 (the "License"); you may not use this file except
     in compliance with the License.
     You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing,
     software distributed under the License is distributed on an
     "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
     KIND, either express or implied.  See the License for the
     specific language governing permissions and limitations
     under the License.
     */
%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="java.util.Random" %>
<%@ page import="org.wso2.carbon.bam.gauges.ui.*" %>

<%
    String funcName = request.getParameter("funcName");
    Random randomGenerator = new Random();

    String DATE_FORMAT = "yyyy-MM-dd";
    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(DATE_FORMAT);
    Calendar c1 = Calendar.getInstance();
    c1.add(Calendar.DATE, -1);
    String date = sdf.format(c1.getTime());

    BAMDataServiceDataProcessor bamDataProcessor = new BAMDataServiceDataProcessor(config, session, request);
    ResponseTimesManager timesManager = new ResponseTimesManager(config, session, request);
    BAMMediationDataProcessor mediationDataProcessor = new BAMMediationDataProcessor(config, session, request);
    BAMStatQueryDSClient client = new BAMStatQueryDSClient(config, session, request);

    //out.print(funcName);
    if ("pgauge_data".equals(funcName)) {
        out.print("&value=38");
    } else if ("dgauge_data".equals(funcName)) {
        out.print("&value=60&hist=28");
    } else if ("meter_data".equals(funcName)) {
        int randomInt = randomGenerator.nextInt(1000);
        out.print("&value=" + randomInt + "&range=1000");
    } else if ("iogauge_data".equals(funcName)) {
        int randomInt = randomGenerator.nextInt(500);
        int randomInt2 = randomGenerator.nextInt(500);
        out.print("&in_range=500&out_range=500&in_value=" + randomInt + "&out_value=" + randomInt2
                  + "&avg_in_value=160&avg_out_value=230");
    } else if ("digital_data".equals(funcName)) {
        int randomInt = randomGenerator.nextInt(500);
        int randomInt2 = randomGenerator.nextInt(500);
        out.print("&value_a=" + randomInt + "&value_b=" + randomInt2);
    } else if ("digital_data2".equals(funcName)) {
        int randomInt = 45675;
        int randomInt2 = 54600;
        out.print("&value_a=" + randomInt + "&value_b=" + randomInt2);
    } else if ("status_data".equals(funcName)) {
        out.print("&value=1");
    } else if ("temp_data".equals(funcName)) {
        out.print("&value=75&range=100");
    } else if ("pmeter_data".equals(funcName)) {
        out.print("&value=-45&range=100");
    } else if ("bar_data".equals(funcName)) {
        out.print("&value=70&range=90");
    } else if ("res_data".equals(funcName)) {
        int randomInt = randomGenerator.nextInt(90);
        out.print("&value=" + randomInt + "&range=90");
    } else if ("min_max_data".equals(funcName)) {
        String data = "&title=Min Max Processing Times,{font-size:20px; color: #FFFFFF; margin: 5px; background-color: #505050; padding:5px; padding-left: 20px; padding-right: 20px;}&\n"
                      + "&bg_colour=#ffffff&\n"
                      + "&x_axis_steps=1&\n"
                      + "&x_axis_3d=1&\n"
                      + "&y_legend=Time (100x ms),12,#736AFF&\n"
                      + "&y_ticks=5,10,5&\n"
                      + "&x_labels="
                      + date
                      + "&\n"
                      + "&y_min=0&\n"
                      + "&y_max=10&\n"
                      + "&x_axis_colour=#909090&\n"
                      + "&x_grid_colour=#ADB5C7&\n"
                      + "&y_axis_colour=#909090&\n"
                      + "&y_grid_colour=#ADB5C7&\n"
                      + "&bar_3d=75,#D54C78,Min,10&\n"
                      + "&values=2&\n"
                      + "&bar_3d_2=75,#3334AD,Max,10&\n" + "&values_2=6&";
        out.print(data);
    } else if (funcName.indexOf("lastminuterequestcount") > -1) {
        int serviceID = Integer.parseInt(request.getParameter("serviceID"));

        String data = timesManager.getLastMinuteRequestCount(serviceID);
        out.print(data);

    } else if ("getBackendServerUrl".equals(funcName)) {
        String data = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        data = data.split("/services/")[0];
        out.print(data);
    } else if (funcName.indexOf("lastminuterequestcountsystem") > -1) {
        out.print("TODO - Dumindu");
    } else if (funcName.indexOf("getServerList") > -1) {
        String data = bamDataProcessor.getServerList();
        out.print(data);
    } else if (funcName.equals("getServicesList")) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String data = bamDataProcessor.getServicesList(serverID);
        out.print(data);
    } else if (funcName.indexOf("getminmaxaverageresptimesservice") > -1) {
        int serviceID = Integer.parseInt(request.getParameter("serviceID"));
        String data = timesManager.getMinMaxAverageRespTimesService(serviceID);
        out.print(data);
    } else if (funcName.indexOf("getAvgResponseTime") > -1) {
        int serviceID = Integer.parseInt(request.getParameter("serviceID"));

        String data = timesManager.getAvgResponseTime(serviceID);
        out.print(data);
    } else if (funcName.indexOf("getMaxResponseTime") > -1) {
        int serviceID = Integer.parseInt(request.getParameter("serviceID"));
        String data = timesManager.getMaxResponseTime(serviceID);
        out.print(data);
    } else if (funcName.indexOf("getLatestAverageResponseTimeForServer") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));

        String data = client.getLatestAverageResponseTimeForServer(serverID);
        out.print(data);
    } else if (funcName.indexOf("getLatestMaximumResponseTimeForServer") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));

        String data = client.getLatestMaximumResponseTimeForServer(serverID);
        out.print(data);
    } else if (funcName.indexOf("getLatestMinimumResponseTimeForServer") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));

        String data = client.getLatestMinimumResponseTimeForServer(serverID);
        out.print(data);
    } else if (funcName.indexOf("getLatestRequestCountForServer") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));

        String data = client.getLatestRequestCountForServer(serverID);
        out.print(data);
    } else if (funcName.indexOf("getLatestResponseCountForServer") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));

        String data = client.getLatestResponseCountForServer(serverID);
        out.print(data);
    } else if (funcName.indexOf("getLatestFaultCountForServer") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));

        String data = client.getLatestFaultCountForServer(serverID);
        out.print(data);
    } else if (funcName.indexOf("getLatestAverageResponseTimeForService") > -1) {
        int serviceID = Integer.parseInt(request.getParameter("serviceID"));

        String data = client.getLatestAverageResponseTimeForService(serviceID);
        out.print(data);
    } else if (funcName.indexOf("getLatestMaximumResponseTimeForService") > -1) {
        int serviceID = Integer.parseInt(request.getParameter("serviceID"));

        String data = client.getLatestMaximumResponseTimeForService(serviceID);
        out.print(data);
    } else if (funcName.indexOf("getLatestMinimumResponseTimeForService") > -1) {
        int serviceID = Integer.parseInt(request.getParameter("serviceID"));

        String data = client.getLatestMinimumResponseTimeForService(serviceID);
        out.print(data);
    } else if (funcName.indexOf("getLatestRequestCountForService") > -1) {
        int serviceID = Integer.parseInt(request.getParameter("serviceID"));

        String data = client.getLatestRequestCountForService(serviceID);
        out.print(data);
    } else if (funcName.indexOf("getLatestResponseCountForService") > -1) {
        int serviceID = Integer.parseInt(request.getParameter("serviceID"));

        String data = client.getLatestResponseCountForService(serviceID);
        out.print(data);
    } else if (funcName.indexOf("getLatestFaultCountForService") > -1) {
        int serviceID = Integer.parseInt(request.getParameter("serviceID"));

        String data = client.getLatestFaultCountForService(serviceID);
        out.print(data);
    } else if (funcName.indexOf("getLatestAverageResponseTimeForOperation") > -1) {
        int operationID = Integer.parseInt(request.getParameter("operationID"));

        String data = client.getLatestAverageResponseTimeForOperation(operationID);
        out.print(data);
    } else if (funcName.indexOf("getLatestMaximumResponseTimeForOperation") > -1) {
        int operationID = Integer.parseInt(request.getParameter("operationID"));

        String data = client.getLatestMaximumResponseTimeForOperation(operationID);
        out.print(data);
    } else if (funcName.indexOf("getLatestMinimumResponseTimeForOperation") > -1) {
        int operationID = Integer.parseInt(request.getParameter("operationID"));

        String data = client.getLatestMinimumResponseTimeForOperation(operationID);
        out.print(data);
    } else if (funcName.indexOf("getLatestRequestCountForOperation") > -1) {
        int operationID = Integer.parseInt(request.getParameter("operationID"));

        String data = client.getLatestRequestCountForOperation(operationID);
        out.print(data);
    } else if (funcName.indexOf("getLatestResponseCountForOperation") > -1) {
        int operationID = Integer.parseInt(request.getParameter("operationID"));

        String data = client.getLatestResponseCountForOperation(operationID);
        out.print(data);
    } else if (funcName.indexOf("getLatestFaultCountForOperation") > -1) {
        int operationID = Integer.parseInt(request.getParameter("operationID"));

        String data = client.getLatestFaultCountForOperation(operationID);
        out.print(data);
    } else if (funcName.indexOf("getEndpoints") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));


        String data = mediationDataProcessor.getEndpoints(serverID);
        out.print(data);
    } else if (funcName.indexOf("getLatestInAverageProcessingTimeForEndpoint") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String endpointName = "EndpointInAvgProcessingTime-" + request.getParameter("endpointName");

        String data = client.getLatestInAverageProcessingTimeForEndpoint(serverID, endpointName);
        out.print(data);
    } else if (funcName.indexOf("getLatestInMaximumProcessingTimeForEndpoint") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String endpointName = "EndpointInMaxProcessingTime-" + request.getParameter("endpointName");

        String data = client.getLatestInMaximumProcessingTimeForEndpoint(serverID, endpointName);
        out.print(data);
    } else if (funcName.indexOf("getLatestInMinimumProcessingTimeForEndpoint") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String endpointName = "EndpointInMinProcessingTime-" + request.getParameter("endpointName");

        String data = client.getLatestInMinimumProcessingTimeForEndpoint(serverID, endpointName);
        out.print(data);
    } else if (funcName.indexOf("getLatestInCumulativeCountForEndpoint") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String endpointName = "EndpointInCumulativeCount-" + request.getParameter("endpointName");

        String data = client.getLatestInCumulativeCountForEndpoint(serverID, endpointName);
        out.print(data);
    } else if (funcName.indexOf("getLatestInFaultCountForEndpoint") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String endpointName = "EndpointInFaultCount-" + request.getParameter("endpointName");

        String data = client.getLatestInFaultCountForEndpoint(serverID, endpointName);
        out.print(data);
    } else if (funcName.indexOf("getSequences") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));

        String data = mediationDataProcessor.getSequences(serverID);
        out.print(data);
    } else if (funcName.indexOf("getLatestInAverageProcessingTimeForSequence") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String sequenceName = "SequenceInAvgProcessingTime-" + request.getParameter("sequenceName");

        String data = client.getLatestInAverageProcessingTimeForSequence(serverID, sequenceName);
        out.print(data);
    } else if (funcName.indexOf("getLatestInMaximumProcessingTimeForSequence") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String sequenceName = "SequenceInMaxProcessingTime-" + request.getParameter("sequenceName");

        String data = client.getLatestInMaximumProcessingTimeForSequence(serverID, sequenceName);
        out.print(data);
    } else if (funcName.indexOf("getLatestInMinimumProcessingTimeForSequence") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String sequenceName = "SequenceInMinProcessingTime-" + request.getParameter("sequenceName");

        String data = client.getLatestInMinimumProcessingTimeForSequence(serverID, sequenceName);
        out.print(data);
    } else if (funcName.indexOf("getLatestInCumulativeCountForSequence") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String sequenceName = "SequenceInCumulativeCount-" + request.getParameter("sequenceName");

        String data = client.getLatestInCumulativeCountForSequence(serverID, sequenceName);
        out.print(data);
    } else if (funcName.indexOf("getLatestInFaultCountForSequence") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String sequenceName = "SequenceInFaultCount-" + request.getParameter("sequenceName");

        String data = client.getLatestInFaultCountForSequence(serverID, sequenceName);
        out.print(data);
    } else if (funcName.indexOf("getLatestOutAverageProcessingTimeForSequence") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String sequenceName = "SequenceOutAvgProcessingTime-" + request.getParameter("sequenceName");

        String data = client.getLatestOutAverageProcessingTimeForSequence(serverID, sequenceName);
        out.print(data);
    } else if (funcName.indexOf("getLatestOutMaximumProcessingTimeForSequence") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String sequenceName = "SequenceOutMaxProcessingTime-" + request.getParameter("sequenceName");

        String data = client.getLatestOutMaximumProcessingTimeForSequence(serverID, sequenceName);
        out.print(data);
    } else if (funcName.indexOf("getLatestOutMinimumProcessingTimeForSequence") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String sequenceName = "SequenceOutMinProcessingTime-" + request.getParameter("sequenceName");

        String data = client.getLatestOutMinimumProcessingTimeForSequence(serverID, sequenceName);
        out.print(data);
    } else if (funcName.indexOf("getLatestOutCumulativeCountForSequence") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String sequenceName = "SequenceOutCumulativeCount-" + request.getParameter("sequenceName");

        String data = client.getLatestOutCumulativeCountForSequence(serverID, sequenceName);
        out.print(data);
    } else if (funcName.indexOf("getLatestOutFaultCountForSequence") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String sequenceName = "SequenceOutFaultCount-" + request.getParameter("sequenceName");

        String data = client.getLatestOutFaultCountForSequence(serverID, sequenceName);
        out.print(data);
    } else if (funcName.indexOf("getProxyServices") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));

        String data = mediationDataProcessor.getProxyServices(serverID);
        out.print(data);
    } else if (funcName.indexOf("getLatestInAverageProcessingTimeForProxy") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String proxyName = "ProxyInAvgProcessingTime-" + request.getParameter("proxyName");

        String data = client.getLatestInAverageProcessingTimeForProxy(serverID, proxyName);
        out.print(data);
    } else if (funcName.indexOf("getLatestInMaximumProcessingTimeForProxy") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String proxyName = "ProxyInMaxProcessingTime-" + request.getParameter("proxyName");

        String data = client.getLatestInMaximumProcessingTimeForProxy(serverID, proxyName);
        out.print(data);
    } else if (funcName.indexOf("getLatestInMinimumProcessingTimeForProxy") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String proxyName = "ProxyInMinProcessingTime-" + request.getParameter("proxyName");

        String data = client.getLatestInMinimumProcessingTimeForProxy(serverID, proxyName);
        out.print(data);
    } else if (funcName.indexOf("getLatestInCumulativeCountForProxy") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String proxyName = "ProxyInCumulativeCount-" + request.getParameter("proxyName");

        String data = client.getLatestInCumulativeCountForProxy(serverID, proxyName);
        out.print(data);
    } else if (funcName.indexOf("getLatestInFaultCountForProxy") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String proxyName = "ProxyInFaultCount-" + request.getParameter("proxyName");

        String data = client.getLatestInFaultCountForProxy(serverID, proxyName);
        out.print(data);
    } else if (funcName.indexOf("getLatestOutAverageProcessingTimeForProxy") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String proxyName = "ProxyOutAvgProcessingTime-" + request.getParameter("proxyName");

        String data = client.getLatestOutAverageProcessingTimeForProxy(serverID, proxyName);
        out.print(data);
    } else if (funcName.indexOf("getLatestOutMaximumProcessingTimeForProxy") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String proxyName = "ProxyOutMaxProcessingTime-" + request.getParameter("proxyName");

        String data = client.getLatestOutMaximumProcessingTimeForProxy(serverID, proxyName);
        out.print(data);
    } else if (funcName.indexOf("getLatestOutMinimumProcessingTimeForProxy") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String proxyName = "ProxyOutMinProcessingTime-" + request.getParameter("proxyName");

        String data = client.getLatestOutMinimumProcessingTimeForProxy(serverID, proxyName);
        out.print(data);
    } else if (funcName.indexOf("getLatestOutCumulativeCountForProxy") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String proxyName = "ProxyOutCumulativeCount-" + request.getParameter("proxyName");

        String data = client.getLatestOutCumulativeCountForProxy(serverID, proxyName);
        out.print(data);
    } else if (funcName.indexOf("getLatestOutFaultCountForProxy") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String proxyName = "ProxyOutFaultCount-" + request.getParameter("proxyName");

        String data = client.getLatestOutFaultCountForProxy(serverID, proxyName);
        out.print(data);
    } else if (funcName.indexOf("getServiceAvgResponseTimesOfServer") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String demo = request.getParameter("demo");
        boolean demoFlag = (demo != null);

        String data = bamDataProcessor.getServiceResponseTimesOfServer(serverID, 0, demoFlag);
        out.print(data);
    } else if (funcName.indexOf("getServiceMaxResponseTimesOfServer") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String demo = request.getParameter("demo");
        boolean demoFlag = (demo != null);

        String data = bamDataProcessor.getServiceResponseTimesOfServer(serverID, 2, demoFlag);
        out.print(data);
    } else if (funcName.indexOf("getServiceMinResponseTimesOfServer") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String demo = request.getParameter("demo");
        boolean demoFlag = (demo != null);

        String data = bamDataProcessor.getServiceResponseTimesOfServer(serverID, 1, demoFlag);
        out.print(data);
    } else if (funcName.indexOf("getServerInfo") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String demo = request.getParameter("demo");
        boolean demoFlag = (demo != null);

        ServiceInfo serviceInfo = new ServiceInfo(config, session, request);
        String data = serviceInfo.getServerInfo(serverID, demoFlag);
        out.print(data);
    } else if (funcName.indexOf("getServiceReqResFaultCountsOfServer") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String demo = request.getParameter("demo");
        boolean demoFlag = (demo != null);

        String data = bamDataProcessor.getServiceReqResFaultCountsOfServer(serverID, demoFlag);
        out.print(data);
    } else if (funcName.indexOf("getSequenceInAvgProcessingTimesOfServer") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String demo = request.getParameter("demo");
        boolean demoFlag = (demo != null);

        String data = mediationDataProcessor.getSequenceInAvgProcessingTimesOfServer(serverID, demoFlag);
        out.print(data);
    } else if (funcName.indexOf("getEndpointInAvgProcessingTimesOfServer") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String demo = request.getParameter("demo");
        boolean demoFlag = (demo != null);

        String data = mediationDataProcessor.getEndpointInAvgProcessingTimesOfServer(serverID, demoFlag);
        out.print(data);
    } else if (funcName.indexOf("getProxyServiceInAvgProcessingTimesOfServer") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String demo = request.getParameter("demo");
        boolean demoFlag = (demo != null);

        String data = mediationDataProcessor.getProxyServiceInAvgProcessingTimesOfServer(serverID, demoFlag);
        out.print(data);
    } else if (funcName.indexOf("getServerMediationInfo") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String demo = request.getParameter("demo");
        boolean demoFlag = (demo != null);

        String data = mediationDataProcessor.getServerMediationInfo(serverID, demoFlag);
        out.print(data);
    } else if (funcName.indexOf("getSequenceReqResFaultCountsOfServer") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String demo = request.getParameter("demo");
        boolean demoFlag = (demo != null);

        String data = bamDataProcessor.getSequenceReqResFaultCountsOfServer(serverID, demoFlag);
        out.print(data);
    } else if (funcName.indexOf("getProxyServiceReqResFaultCountsOfServer") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String demo = request.getParameter("demo");
        boolean demoFlag = (demo != null);

        String data = bamDataProcessor.getProxyServiceReqResFaultCountsOfServer(serverID, demoFlag);
        out.print(data);
    } else if (funcName.indexOf("getEndpointReqResFaultCountsOfServer") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String demo = request.getParameter("demo");
        boolean demoFlag = (demo != null);

        String data = bamDataProcessor.getEndpointReqResFaultCountsOfServer(serverID, demoFlag);
        out.print(data);
    } else if (funcName.indexOf("getLatestMaximumOperationsForAnActivityID") > -1) {
        int activityID = Integer.parseInt(request.getParameter("activityID"));
        String activityName = request.getParameter("activityName");

        String data = client.getLatestMaximumOperationsForAnActivityID(activityID);
        out.print(data);
    } else if (funcName.indexOf("getActivityList") > -1) {

        BAMDataServiceDataProcessor BAMDataProcessor = new BAMDataServiceDataProcessor(config,
                                                                                       session, request);
        String data = BAMDataProcessor.getActivityList();
        out.print(data);
    } else if (funcName.indexOf("getActivityInfoForActivityID") > -1) {

        int activityID = Integer.parseInt(request.getParameter("activityID"));

        String data = bamDataProcessor.getActivityInfoForActivityID(activityID);
        out.print(data);
    } else if (funcName.indexOf("getOperationInfoForActivityID") > -1) {
        int activityID = Integer.parseInt(request.getParameter("activityID"));

        String data = bamDataProcessor.getOperationInfoForActivityID(activityID);
        out.print(data);
    } else if (funcName.indexOf("getDetailsForActivity") > -1) {
        String activityName = request.getParameter("activityName");

        String data = bamDataProcessor.getDetailsForActivity(activityName);
        out.print(data);
    } else if (funcName.indexOf("getActivityDetailsForTimeRange") > -1) {
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");
        String propertyKey1 = request.getParameter("propertyKey1");
        String propertyValue1 = request.getParameter("propertyValue1");
        String propertyKey2 = request.getParameter("propertyKey2");
        String propertyValue2 = request.getParameter("propertyValue2");
        String propertyKey3 = request.getParameter("propertyKey3");
        String propertyValue3 = request.getParameter("propertyValue3");

        String data = bamDataProcessor.getActivityDetailsForTimeRange(startTime, endTime, propertyKey1, propertyValue1,
                                                               propertyKey2, propertyValue2, propertyKey3, propertyValue3);
        out.print(data);
    } else if (funcName.indexOf("getActivityDetailsForActivity") > -1) {
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");

        String data = bamDataProcessor.getActivityDetailsForActivity(startTime, endTime);
        out.print(data);
    } else if (funcName.indexOf("getPropertyBagForActivity") > -1) {

        String data = bamDataProcessor.getServerListForActivity();
        out.print(data);
    } else if (funcName.indexOf("getPropertyKeyForActivity") > -1) {
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");

        String data = bamDataProcessor.getPropertyKeyForActivity(startTime, endTime);
        out.print(data);
    } else if (funcName.indexOf("getPropertyChildrenForActivity") > -1) {
        String childPropertyParam = request.getParameter("childPropertyParam");
        int activityId = Integer.parseInt(request.getParameter("activityId"));

        String data = bamDataProcessor.getPropertyChildrenForActivity(childPropertyParam, activityId);
        out.print(data);
    } else if (funcName.indexOf("getServiceListForActivity") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String data = bamDataProcessor.getServiceListForActivity(serverID);
        out.print(data);
    } else if (funcName.indexOf("getOperationListForActivity") > -1) {
        int serviceId = Integer.parseInt(request.getParameter("serviceId"));

        String data = bamDataProcessor.getOperationListForActivity(serviceId);
        out.print(data);
    } else if (funcName.indexOf("gettimestampForOperation") > -1) {
        int operationId = Integer.parseInt(request.getParameter("operationId"));

        String data = bamDataProcessor.gettimestampForOperation(operationId);
        out.print(data);
    } else if (funcName.indexOf("getDirectionForOperation") > -1) {
        int operationId = Integer.parseInt(request.getParameter("operationId"));

        String data = bamDataProcessor.getDirectionForOperation(operationId);
        out.print(data);
    } else if (funcName.indexOf("getMessagesForOperation") > -1) {
        int operationId = Integer.parseInt(request.getParameter("operationId"));
        String direction = request.getParameter("direction");
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");

        String data = bamDataProcessor.getMessagesForOperation(operationId, direction, startTime, endTime);
        out.print(data);
    } else if (funcName.indexOf("getXpathKeys") > -1) {
        String data = bamDataProcessor.getXpathKeys();
        out.print(data);
    } else if (funcName.indexOf("getMessageForMessageID") > -1) {
        int messageId = Integer.parseInt(request.getParameter("messageId"));

        String data = bamDataProcessor.getMessageForMessageID(messageId);
        out.print(data);
    } else if (funcName.indexOf("getPropertyValueForStatus") > -1) {
        String statusKey = request.getParameter("statusKey");
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");

        String data = bamDataProcessor.getPropertyValueForStatus(statusKey, startTime, endTime);
        out.print(data);
    } else if (funcName.indexOf("getMessagesForStatus") > -1) {
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");
        String server = request.getParameter("server");
        String service = request.getParameter("service");
        String operation = request.getParameter("operation");
        String direction = request.getParameter("direction");
        String status = request.getParameter("status");
        String activityType = request.getParameter("activityType");
        String messageType = request.getParameter("messageType");
        String messageFormat = request.getParameter("messageFormat");
        String applicationStatus = request.getParameter("applicationStatus");
        String technicalStatus = request.getParameter("technicalStatus");
        String messageGUID = request.getParameter("messageGUID");
        String namespaceDefs = request.getParameter("namespaceDef");
        String xpathKey = request.getParameter("xpath");
        String xpathValue = request.getParameter("xpathValue");
        int startDataset = Integer.parseInt(request.getParameter("startDataset"));
        int endDataset = Integer.parseInt(request.getParameter("endDataset"));

        String data = "";
        try {
            data = bamDataProcessor.getMessagesForStatus(startTime, endTime, server, service, operation, direction, status, activityType, messageType
                    , messageFormat, applicationStatus, technicalStatus, messageGUID, xpathKey, xpathValue, namespaceDefs, startDataset, endDataset);

        } catch (Exception e) {
            data = "Access Denied";
        } finally {
            out.print(data);
        }

    } else if (funcName.indexOf("getMessagesCountForSAP") > -1) {
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");
        String server = request.getParameter("server");
        String service = request.getParameter("service");
        String operation = request.getParameter("operation");
        String direction = request.getParameter("direction");
        String status = request.getParameter("status");
        String activityType = request.getParameter("activityType");
        String messageType = request.getParameter("messageType");
        String messageFormat = request.getParameter("messageFormat");
        String applicationStatus = request.getParameter("applicationStatus");
        String technicalStatus = request.getParameter("technicalStatus");
        String messageGUID = request.getParameter("messageGUID");
        String namespaceDefs = request.getParameter("namespaceDef");
        String xpathKey = request.getParameter("xpath");
        String xpathValue = request.getParameter("xpathValue");

        String data = "";
        try {
            data = bamDataProcessor.getMessagesCountForSAP(startTime, endTime, server, service, operation, direction, status, activityType, messageType
                    , messageFormat, applicationStatus, technicalStatus, messageGUID, xpathKey, xpathValue, namespaceDefs);

        } catch (Exception e) {
            data = "Access Denied";
        } finally {
            out.print(data);
        }

    } else if (funcName.indexOf("getChildrenMessagesForSAP") > -1) {
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");
        String server = request.getParameter("server");
        String service = request.getParameter("service");
        String operation = request.getParameter("operation");
        String direction = request.getParameter("direction");
        String status = request.getParameter("status");
        String activityType = request.getParameter("activityType");
        String messageType = request.getParameter("messageType");
        String messageFormat = request.getParameter("messageFormat");
        String applicationStatus = request.getParameter("applicationStatus");
        String technicalStatus = request.getParameter("technicalStatus");
        String messageGUID = request.getParameter("messageGUID");
        String namespaceDefs = request.getParameter("namespaceDef");
        String xpathKey = request.getParameter("xpath");
        String xpathValue = request.getParameter("xpathValue");
        int activityId = Integer.parseInt(request.getParameter("activityId"));
        int startDataset = Integer.parseInt(request.getParameter("startDataset"));
        int endDataset = Integer.parseInt(request.getParameter("endDataset"));

        String data = "";
        try {
            data = bamDataProcessor.getChildrenMessagesForSAP(startTime, endTime, server, service, operation, direction, status, activityType, messageType
                    , messageFormat, applicationStatus, technicalStatus, messageGUID, xpathKey, xpathValue, namespaceDefs, activityId, startDataset, endDataset);

        } catch (Exception e) {
            data = "Access Denied";
        } finally {
            out.print(data);
        }

    } else if (funcName.indexOf("getAleauditMessagesForSAP") > -1) {
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");
        String server = request.getParameter("server");
        String service = request.getParameter("service");
        String operation = request.getParameter("operation");
        String direction = request.getParameter("direction");
        String status = request.getParameter("status");
        String messageType = request.getParameter("messageType");
        String messageFormat = request.getParameter("messageFormat");
        String applicationStatus = request.getParameter("applicationStatus");
        String technicalStatus = request.getParameter("technicalStatus");
        String messageGUID = request.getParameter("messageGUID");
        String namespaceDefs = request.getParameter("namespaceDef");
        String xpathKey = request.getParameter("xpath");
        String xpathValue = request.getParameter("xpathValue");
        int startDataset = Integer.parseInt(request.getParameter("startDataset"));
        int endDataset = Integer.parseInt(request.getParameter("endDataset"));

        String data = "";
        try {
            data = bamDataProcessor.getAleauditMessagesForSAP(startTime, endTime, server, service, operation, direction, status, messageType
                    , messageFormat, applicationStatus, technicalStatus, messageGUID, xpathKey, xpathValue, namespaceDefs, startDataset, endDataset);

        } catch (Exception e) {
            data = "Access Denied";
        } finally {
            out.print(data);
        }

    } else if (funcName.indexOf("getCountofChildrenFailedMessagesString") > -1) {
        BAMDataServiceDataProcessor bamDataServiceDataProcessor = new BAMDataServiceDataProcessor(config, session,
                                                                                request);
        int activityId = Integer.parseInt(request.getParameter("activityId"));
        String data = bamDataProcessor.getCountofChildrenFailedMessagesString(activityId);
        out.print(data);

    } else if (funcName.indexOf("getMessagesWithXPathValue") > -1) {


    } else if (funcName.indexOf("getMessageCount") > -1) {

        String data = bamDataProcessor.getMessageCount();
        out.print(data);

    } else if (funcName.indexOf("getpropertyBagForActivity") > -1) {
        String activityName = request.getParameter("activityName");

        String data = bamDataProcessor.getpropertyBagForActivity(activityName);
        out.print(data);
    } else if (funcName.indexOf("getPropertyList") > -1) {
        String key = request.getParameter("key");

        String data = bamDataProcessor.getPropertyList(key);
        out.print(data);
    } else if (funcName.indexOf("getActivityDetailsForServer") > -1) {
        String serverUrl = request.getParameter("serverURL");

        String data = bamDataProcessor.getActivityDetailsForServer(serverUrl);
        out.print(data);
    } else if (funcName.indexOf("getOperationNameList") > -1) {
        int serviceID = Integer.parseInt(request.getParameter("serviceID"));

        String data = bamDataProcessor.getOperationNameList(serviceID);
        out.print(data);
    } else if (funcName.indexOf("getActivityInfo") > -1) {
        int activityID = Integer.parseInt(request.getParameter("activityID"));
        String demo = request.getParameter("demo");
        boolean demoFlag = (demo != null);

        String data = bamDataProcessor.getActivityInfo(activityID, demoFlag);
        out.print(data);
    } else if (funcName.indexOf("getOperationsOfService") > -1) {

        int serverID = Integer.parseInt(request.getParameter("serverID"));
        int serviceID = Integer.parseInt(request.getParameter("serviceID"));
        String demo = request.getParameter("demo");
        boolean demoFlag = (demo != null);

        String data = bamDataProcessor.getOperationsOfService(serverID, serviceID, demoFlag);
        out.print(data);
    } else if (funcName.indexOf("getAdminConsoleUrl") > -1) {

        String data = bamDataProcessor.getAdminConsoleUrl(request);
        out.print(data);
    } else if (funcName.indexOf("getServerWithData") > -1) {
        String func = request.getParameter("function");

        BAMMediationDataProcessor mediationProcessor = new BAMMediationDataProcessor(config, session,
                                                                                     request);
        String data = bamDataProcessor.getServerWithData(func, mediationProcessor);
        out.print(data);
    } else if (funcName.indexOf("getJMXMetricsWindow") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String data = bamDataProcessor.getJMXMetricsWindow(serverID);
        out.print(data);
    } else if (funcName.indexOf("getClientList") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String data = bamDataProcessor.getClientList(serverID);

        out.print(data);
    } else if (funcName.indexOf("getClientServiceOperation") > -1) {
        int serverID = Integer.parseInt(request.getParameter("serverID"));
        String data = bamDataProcessor.getClientServiceOperation(serverID);
        out.print(data);
    } else if (funcName.indexOf("getEndpointReqResFaultCountsForAllServers") > -1) {
        String data = bamDataProcessor.getEndpointReqResFaultCountsForAllServers();
        out.print(data);
    } else if (funcName.indexOf("getMediationRealTimeFaultStat_temp") > -1) {
        String categoryType = request.getParameter("categoryType");
        String mediationName = request.getParameter("mediationName");
        String cacheId = request.getParameter("cacheId");
        int serverId = Integer.parseInt(request.getParameter("serverID"));

        String data = bamDataProcessor.getMediationRealTimeFaultStat_temp(categoryType, serverId, mediationName, cacheId);
        out.print(data);
    } else if (funcName.indexOf("getServersHierarchy") > -1) {
        String type = request.getParameter("type");

        String data = bamDataProcessor.getServersHeirarchy(type);
        out.print(data);
    } else if (funcName.indexOf("GetServiceSummary") > -1) {
        String categoryType = request.getParameter("categoryType");
        String summaryType = request.getParameter("summaryType");
        int categoryID = Integer.parseInt(request.getParameter("categoryID"));
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");
    } else if (funcName.indexOf("GetMediationSummaries") > -1) {
        String categoryType = request.getParameter("categoryType");
        String summaryType = request.getParameter("summaryType");
        int categoryID = Integer.parseInt(request.getParameter("categoryID"));
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");

        String data = bamDataProcessor.getMediationSummaries(categoryType, summaryType, categoryID, startTime, endTime);
        out.print(data);
    }
    
    String startTime = request.getParameter("startTime");
    String endTime = request.getParameter("endTime");
    String activityId = request.getParameter("activityId");

    if (funcName.equals("getAllActivityDataForTimeRange")) {
        String activity = bamDataProcessor.getAllActivityDataForTimeRange(startTime, endTime);
        out.print(activity);
    } else if (funcName.equals("getAllMessagesForTimeRangeAndActivity")) {
        String fullMsg = bamDataProcessor.getAllMessagesForTimeRangeAndActivity(startTime, endTime, Integer.parseInt(activityId));
        out.print(fullMsg);
    }
%>

