/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bpel.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.wso2.carbon.bpel.stub.mgt.types.*;
import org.wso2.carbon.bpel.ui.clients.ProcessManagementServiceClient;
import org.wso2.carbon.statistics.stub.types.carbon.Metric;
import org.wso2.carbon.utils.xml.XMLPrettyPrinter;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.*;

/**
 * Some common UI functionalities, used in org.wso2.carbon.bpel.ui
 */
public class BpelUIUtil {
    private static Log log = LogFactory.getLog("bpel.ui");
    private static final String DISABLED = "disabled";
    private static final String CHECKED = "checked";


    public static String encodeXML(String xmlString) {

        if (xmlString != null && xmlString.length() != 0) {
            log.info(xmlString);
            log.info("     gg");
            // System.out.println(xmlString);
            xmlString = xmlString.replaceAll("<", "&lt;");
            xmlString = xmlString.replaceAll(">", "&gt;");
            xmlString = xmlString.replaceAll("\n", "");
        }
        return xmlString;
    }

    public static String encodeHTML(String aText) {
        final StringBuilder result = new StringBuilder();
        final StringCharacterIterator iterator = new StringCharacterIterator(aText);
        char character = iterator.current();
        while (character != CharacterIterator.DONE) {
            if (character == '<') {
                result.append("&lt;");
            } else if (character == '>') {
                result.append("&gt;");
            } else if (character == '&') {
                result.append("&amp;");
//            } else if (character == '\"') {
//                result.append("&quot;");
//            } else if (character == '\t') {
//                addCharEntity(9, result);
//            } else if (character == '!') {
//                addCharEntity(33, result);
//            } else if (character == '#') {
//                addCharEntity(35, result);
//            } else if (character == '$') {
//                addCharEntity(36, result);
//            } else if (character == '%') {
//                addCharEntity(37, result);
//            } else if (character == '\'') {
//                addCharEntity(39, result);
//            } else if (character == '(') {
//                addCharEntity(40, result);
//            } else if (character == ')') {
//                addCharEntity(41, result);
//            } else if (character == '*') {
//                addCharEntity(42, result);
//            } else if (character == '+') {
//                addCharEntity(43, result);
//            } else if (character == ',') {
//                addCharEntity(44, result);
//            } else if (character == '-') {
//                addCharEntity(45, result);
//            } else if (character == '.') {
//                addCharEntity(46, result);
//            } else if (character == '/') {
//                addCharEntity(47, result);
//            } else if (character == ':') {
//                addCharEntity(58, result);
//            } else if (character == ';') {
//                addCharEntity(59, result);
//            } else if (character == '=') {
//                addCharEntity(61, result);
//            } else if (character == '?') {
//                addCharEntity(63, result);
//            } else if (character == '@') {
//                addCharEntity(64, result);
//            } else if (character == '[') {
//                addCharEntity(91, result);
//            } else if (character == '\\') {
//                addCharEntity(92, result);
//            } else if (character == ']') {
//                addCharEntity(93, result);
//            } else if (character == '^') {
//                addCharEntity(94, result);
//            } else if (character == '_') {
//                addCharEntity(95, result);
//            } else if (character == '`') {
//                addCharEntity(96, result);
//            } else if (character == '{') {
//                addCharEntity(123, result);
//            } else if (character == '|') {
//                addCharEntity(124, result);
//            } else if (character == '}') {
//                addCharEntity(125, result);
//            } else if (character == '~') {
//                addCharEntity(126, result);
            } else {
                //the char is not a special one
                //add it to the result as is
                result.append(character);
            }
            character = iterator.next();
        }
        return result.toString();
    }

    public static String prettyPrint(String rawXML) {
        rawXML = rawXML.replaceAll("\n|\\r|\\f|\\t", "");
        rawXML = rawXML.replaceAll("> +<", "><");
        InputStream xmlIn = new ByteArrayInputStream(rawXML.getBytes());
        XMLPrettyPrinter xmlPrettyPrinter = new XMLPrettyPrinter(xmlIn);
        rawXML = xmlPrettyPrinter.xmlFormat();
//        rawXML = rawXML.replaceAll("\n", "<br>");

        return rawXML;
    }

    public static TreeMap<String, QName> getEndpointRefsMap(ProcessInfoType info) {
        EndpointRef_type0[] endpoints = info.getEndpoints().getEndpointRef();

        TreeMap<String, QName> pLinkServiceMap = new TreeMap<String, QName>();
        for (EndpointRef_type0 ref : endpoints) {
            pLinkServiceMap.put(ref.getPartnerLink(), ref.getService());
        }
        return pLinkServiceMap;
    }

    public static Map<String, EndpointRef_type0> getEndpointReferences(ProcessInfoType info) {
        EndpointRef_type0[] endpoints = info.getEndpoints().getEndpointRef();

        Map<String, EndpointRef_type0> pLinkEprMap = new TreeMap<String, EndpointRef_type0>();
        for (EndpointRef_type0 eprRef : endpoints) {
            pLinkEprMap.put(eprRef.getPartnerLink(), eprRef);
        }

        return pLinkEprMap;
    }

    public static String makeCommaSeparatedListOfPackageVersions(String[] versionArray) {
        if (versionArray == null) {
            return "";
        }

        StringBuilder strBuilder = new StringBuilder();
        for (String version : versionArray) {
            strBuilder.append(version);
            strBuilder.append(", ");
        }

        String finalString = strBuilder.toString();
        return finalString.substring(0, finalString.lastIndexOf(","));
    }

    public static String getUndeployLink(String packageName, int pageNumber) {
        return "process_list.jsp?operation=undeploy&packageName=" + packageName + "&pageNumber=" +
                pageNumber;
    }

    public static String getRetireLink(String pid, String filter, String order, int pageNumber) {
        return "process_list.jsp?operation=retire&processID=" + pid + "&filter=" + filter +
                "&order=" + order + "&pageNumber=" + pageNumber;
    }

    public static String getActivateLink(String pid, String filter, String order, int pageNumber) {
        return "process_list.jsp?operation=activate&processID=" + pid + "&filter=" + filter +
                "&order=" + order + "&pageNumber=" + pageNumber;
    }

    public static QName stringToQName(String pid) {
        int endURI = pid.indexOf('}');
        return new QName(pid.substring(1, endURI), pid.substring(endURI + 1));
    }

    /*private String[] getServiceLocations(ProcessInfoType info) {
        String[] serviceLocations = info.getEndpoints().getEndpointRef()[0].getServiceLocations().getServiceLocation();
        //Removing the processEPR

        return null;
    }*/

    public static String getProcessDefinition(ProcessInfoType info) {
        BpelDefinition definition = info.getDefinitionInfo().getDefinition();
        return definition.getExtraElement().toString();
    }

    public static String getInstanceOperations(InstanceStatus status, String iid) {
        ResourceBundle rsc = ResourceBundle.getBundle("org.wso2.carbon.bpel.ui.i18n.Resources");
        StringBuilder strBuilder = new StringBuilder();
        if (status.equals(InstanceStatus.ACTIVE)) {
            generateActiveActionLinks(strBuilder, rsc, iid);
        } else if (status.equals(InstanceStatus.COMPLETED)) {
            generateCompletedActionLinks(strBuilder, rsc, iid);
        } else if (status.equals(InstanceStatus.SUSPENDED)) {
            generateSuspendedActionLinks(strBuilder, rsc, iid);
        } else if (status.equals(InstanceStatus.TERMINATED)) {
            generateTerminatedActionLinks(strBuilder, rsc, iid);
        } /*else if (status.equals(InstanceStatus.ERROR)) {
            generateErrorActionLinks(strBuilder, rsc, iid);
        }*/ else if (status.equals(InstanceStatus.FAILED)) {
            // For failed instances only delete operation is available.
            generateTerminatedActionLinks(strBuilder, rsc, iid);
        }

        return strBuilder.toString();
    }

    private static void generateActiveActionLinks(StringBuilder strBuilder, ResourceBundle rsc,
                                                  String iid) {
        generateTDStart(strBuilder);
        generateStartTag(strBuilder);
        generateDeleteLink(strBuilder, rsc, iid);
        generateEndTag(strBuilder);
        generateStartTag(strBuilder);
        generateSuspendLink(strBuilder, rsc, iid);
        generateEndTag(strBuilder);
        generateStartTag(strBuilder);
        generateTerminateLink(strBuilder, rsc, iid);
        generateEndTag(strBuilder);
        generateTDEnd(strBuilder);
    }

    private static void generateCompletedActionLinks(StringBuilder strBuilder, ResourceBundle rsc,
                                                     String iid) {
        generateTDStart(strBuilder);
        generateStartTag(strBuilder);
        generateDeleteLink(strBuilder, rsc, iid);
        generateEndTag(strBuilder);
        generateTDEnd(strBuilder);
    }

    private static void generateSuspendedActionLinks(StringBuilder strBuilder, ResourceBundle rsc,
                                                     String iid) {
        generateTDStart(strBuilder);
        generateStartTag(strBuilder);
        generateResumeLink(strBuilder, rsc, iid);
        generateEndTag(strBuilder);
        generateStartTag(strBuilder);
        generateTerminateLink(strBuilder, rsc, iid);
        generateEndTag(strBuilder);
        generateStartTag(strBuilder);
        generateDeleteLink(strBuilder, rsc, iid);
        generateEndTag(strBuilder);
        generateTDEnd(strBuilder);
    }

    private static void generateTerminatedActionLinks(StringBuilder strBuilder, ResourceBundle rsc,
                                                      String iid) {
        generateTDStart(strBuilder);
        generateStartTag(strBuilder);
        generateDeleteLink(strBuilder, rsc, iid);
        generateEndTag(strBuilder);
        generateTDEnd(strBuilder);
    }

//    private static void generateErrorActionLinks(StringBuilder strBuilder, ResourceBundle rsc,
//                                                 String iid) {
//        generateTDStart(strBuilder);
//        generateStartTag(strBuilder);
//        generateTerminateLink(strBuilder, rsc, iid);
//        generateEndTag(strBuilder);
//        generateStartTag(strBuilder);
//        generateDeleteLink(strBuilder, rsc, iid);
//        generateEndTag(strBuilder);
//        generateTDEnd(strBuilder);
//    }

    private static void generateSuspendLink(StringBuilder strBuilder, ResourceBundle rsc,
                                            String iid) {
        generateFirstPartOfTheLink(strBuilder, iid, "suspend");
        strBuilder.append("&operation=suspend'");
        strBuilder.append(" class='bpel-icon-link' style='background-image:url(images/suspend.gif);'>");
        generateOnSuspendEventAttachScript(strBuilder, iid);
        strBuilder.append(rsc.getString("suspend"));
        strBuilder.append("</a>");
    }

    private static void generateResumeLink(StringBuilder strBuilder, ResourceBundle rsc,
                                           String iid) {
        generateFirstPartOfTheLink(strBuilder, iid, "resume");
        strBuilder.append("&operation=resume'");
        strBuilder.append(" class='bpel-icon-link' style='background-image:url(images/resume.gif);'>");
        generateOnResumeEventAttachScript(strBuilder, iid);
        strBuilder.append(rsc.getString("resume"));
        strBuilder.append("</a>");
    }

    private static void generateTerminateLink(StringBuilder strBuilder, ResourceBundle rsc,
                                              String iid) {
        generateFirstPartOfTheLink(strBuilder, iid, "terminate");
        strBuilder.append("&operation=terminate'");
        strBuilder.append(" class='bpel-icon-link' style='background-image:url(images/terminate.gif);'>");
        generateOnTerminateEventAttachScript(strBuilder, iid);
        strBuilder.append(rsc.getString("terminate"));
        strBuilder.append("</a>");
    }

    private static void generateDeleteLink(StringBuilder strBuilder, ResourceBundle rsc,
                                           String iid) {
        generateFirstPartOfTheLink(strBuilder, iid, "delete");
        strBuilder.append("&operation=delete'");
        strBuilder.append(" class='bpel-icon-link' style='background-image:url(images/delete.gif);'>");
        generateOnDeleteEventAttachScript(strBuilder, iid);
        strBuilder.append(rsc.getString("delete"));
        strBuilder.append("</a>");
    }

    private static void generateOnDeleteEventAttachScript(StringBuilder strBuilder, String id) {
        strBuilder.append("<script type='text/javascript'>(function(){jQuery('#instancedelete");
        strBuilder.append(id);
        strBuilder.append("').click(BPEL.instance.onDelete);})();</script>");
    }

    private static void generateOnResumeEventAttachScript(StringBuilder strBuilder, String id) {
        strBuilder.append("<script type='text/javascript'>(function(){jQuery('#instanceresume");
        strBuilder.append(id);
        strBuilder.append("').click(BPEL.instance.onResume);})();</script>");
    }

    private static void generateOnSuspendEventAttachScript(StringBuilder strBuilder, String id) {
        strBuilder.append("<script type='text/javascript'>(function(){jQuery('#instancesuspend");
        strBuilder.append(id);
        strBuilder.append("').click(BPEL.instance.onSuspend);})();</script>");
    }

    private static void generateOnTerminateEventAttachScript(StringBuilder strBuilder, String id) {
        strBuilder.append("<script type='text/javascript'>(function(){jQuery('#instanceterminate");
        strBuilder.append(id);
        strBuilder.append("').click(BPEL.instance.onTerminate);})();</script>");
    }



    private static void generateFirstPartOfTheLink(StringBuilder strBuilder, String iid,
                                                   String operation) {
        strBuilder.append("<a id='instance");
        strBuilder.append(operation);
        strBuilder.append(iid);
        strBuilder.append("' href='list_instances.jsp?iid=");
        strBuilder.append(iid);
    }

    private static void generateStartTag(StringBuilder strBuilder) {
        //strBuilder.append("<td style='border-right:0px;border-left:0px;'>");
        strBuilder.append("[ ");
    }

    private static void generateEndTag(StringBuilder strBuilder) {
        strBuilder.append(" ] ");
    }

    private static void generateTDEnd(StringBuilder strBuilder) {
        strBuilder.append("</td>");
    }

    private static void generateTDStart(StringBuilder strbuilder) {
        strbuilder.append("<td style=\"white-space: nowrap\">");
    }

    public static void logWarn(String message, Exception e) {
        if (log.isWarnEnabled()) {
            log.warn(message, e);
        }
    }

    public static String generateRetireLinkForProcessInfoPage(String processId) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("process_info.jsp?Pid=");
        strBuilder.append(processId);
        strBuilder.append("&operation=retire");

        return strBuilder.toString();
    }

    public static String generateActivateLinkForProcessInfoPage(String processId) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("process_info.jsp?Pid=");
        strBuilder.append(processId);
        strBuilder.append("&operation=activate");

        return strBuilder.toString();
    }

    public static int getTotalInstance(ProcessInfoType processInfo) {
        int totalInstances = 0;
        for (Instances_type0 processInstance : processInfo.getInstanceSummary().getInstances()) {
            totalInstances += processInstance.getCount();
        }

        return totalInstances;
    }
    

    public static String getInstanceFilterURL(String processId) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("list_instances.jsp?filter=pid=");
        strBuilder.append(processId);
        strBuilder.
            append(" status=active|completed|suspended|terminated|failed|error&order=-last-active");

        return strBuilder.toString();
    }

    /**
     * Create instance summary JSON object from process instances array.
     * @param processInstances process instance array
     * @return instance summary object
     */
    public static JSONObject createInstanceSummaryJSONObject(Instances_type0[] processInstances) {
        JSONObject summary = new JSONObject();
        int totalInstances  = 0;
        for (Instances_type0 processInstance : processInstances) {
            String state = processInstance.getState().getValue();
            if (state.equals(BPELUIConstant.INSTANCE_STATE_ACTIVE)) {
                summary.put(BPELUIConstant.INSTANCE_STATE_ACTIVE, processInstance.getCount());
            } else if (state.equals(BPELUIConstant.INSTANCE_STATE_COMPLETED)) {
                summary.put(BPELUIConstant.INSTANCE_STATE_COMPLETED, processInstance.getCount());
            } else if (state.equals(BPELUIConstant.INSTANCE_STATE_TERMINATED)) {
                summary.put(BPELUIConstant.INSTANCE_STATE_TERMINATED, processInstance.getCount());
            } else if (state.equals(BPELUIConstant.INSTANCE_STATE_FAILED)) {
                summary.put(BPELUIConstant.INSTANCE_STATE_FAILED, processInstance.getCount());
            } else if (state.equals(BPELUIConstant.INSTANCE_STATE_SUSPENDED)) {
                summary.put(BPELUIConstant.INSTANCE_STATE_SUSPENDED, processInstance.getCount());
            } else {
                log.error("Invalid instance state: " + state);
            }
            totalInstances += processInstance.getCount();
        }
        summary.put(BPELUIConstant.TOTAL_INSTANCES, totalInstances);
        return summary;
    }

    /**
     * Create instance summary JSON object from return value of getInstanceSummary operation
     * of instance management web service.
     * @param globalInstanceSummary instance summary from instance management API(global counts)
     * @return global instance count
     */
    public static JSONObject createInstanceSummaryJSONObject(InstanceSummaryE globalInstanceSummary) {
        JSONObject summary = new JSONObject();

        summary.put(BPELUIConstant.INSTANCE_STATE_ACTIVE, globalInstanceSummary.getActive());
        summary.put(BPELUIConstant.INSTANCE_STATE_COMPLETED, globalInstanceSummary.getCompleted());
        summary.put(BPELUIConstant.INSTANCE_STATE_TERMINATED, globalInstanceSummary.getTerminated());
        summary.put(BPELUIConstant.INSTANCE_STATE_FAILED, globalInstanceSummary.getFailed());
        summary.put(BPELUIConstant.INSTANCE_STATE_SUSPENDED, globalInstanceSummary.getSuspended());

        return summary;
    }

    public static JSONObject getMemoryInfoJSONObject(Metric usedMem, Metric totalMem){
        JSONObject memInfo = new JSONObject();

        memInfo.put("UsedMemoryValue", usedMem.getValue());
        memInfo.put("UsedMemoryUnit", usedMem.getUnit());
        memInfo.put("TotalMemoryValue", totalMem.getValue());
        memInfo.put("TotalMemoryUnit", totalMem.getUnit());

        return memInfo;
    }

    public static String createLongRunningInstanceJSONString(PaginatedInstanceList longRunningInstances) {
        //LinkedHashMap is introduced instead of JSONObject, in-order to keep the order of the instances
        LinkedHashMap<String, JSONObject> longRunning = new LinkedHashMap<String, JSONObject>();
        if (longRunningInstances.isInstanceSpecified()) {
            for (LimitedInstanceInfoType instance : longRunningInstances.getInstance()) {
                JSONObject instanceJson = new JSONObject();
                long instanceLifetimeTillLastActive =
                                (instance.getDateLastActive().getTime().getTime() -
                                instance.getDateStarted().getTime().getTime());
                long instanceLifetimeFromLastActiveToNow =
                                (new Date().getTime() -
                                 instance.getDateLastActive().getTime().getTime());
                instanceJson.put(BPELUIConstant.INSTANCE_LIFETIME_TILL_LASTACTIVE,
                                 instanceLifetimeTillLastActive);
                instanceJson.put(BPELUIConstant.INSTANCE_LIFETIME_FROM_LASTACTIVE_TO_NOW,
                                 instanceLifetimeFromLastActiveToNow);
                longRunning.put(instance.getIid(), instanceJson);
            }
        }
        return JSONObject.toJSONString(longRunning);
    }


    private static String[] setEnabledEventsList(ProcessDeployDetailsList_type0 processDeployDetailsList_type0) {

        String[] enabledEvents = null;
        if (processDeployDetailsList_type0.getProcessEventsList() != null &&
                processDeployDetailsList_type0.getProcessEventsList().getEnableEventsList() != null) {

            EnableEventListType enableEventListType = processDeployDetailsList_type0.getProcessEventsList().getEnableEventsList();
            if (enableEventListType.getEnableEvent() != null) {
                enabledEvents = enableEventListType.getEnableEvent();
            }
        }

        return enabledEvents;
    }

    public static ScopeEventType[] setScopeEventsList(ProcessDeployDetailsList_type0 processDeployDetailsList_type0) {

        ScopeEventType[] scopeEnabledEvents = null;
        if (processDeployDetailsList_type0.getProcessEventsList() != null &&
                processDeployDetailsList_type0.getProcessEventsList().getScopeEventsList() != null) {

            ScopeEventListType scopeEnableEventListType = processDeployDetailsList_type0.getProcessEventsList().getScopeEventsList();
            if (scopeEnableEventListType.getScopeEvent() != null) {
                scopeEnabledEvents = scopeEnableEventListType.getScopeEvent();
            }
        }

        return scopeEnabledEvents;
    }


    private static String[] setSuccessTypeCleanups(ProcessDeployDetailsList_type0 processDeployDetailsList_type0) {

        List<String> successCategories = null;
        if (processDeployDetailsList_type0.getCleanUpList() != null &&
                processDeployDetailsList_type0.getCleanUpList().getCleanUp() != null) {
            successCategories = new ArrayList<String>();
            CleanUpType[] cleanUpTypes = processDeployDetailsList_type0.getCleanUpList().getCleanUp();


            for (CleanUpType cleanUpType : cleanUpTypes) {
                if (cleanUpType.getOn().getValue().equalsIgnoreCase("success") && cleanUpType.getCategoryList() != null) {

                    CategoryListType categoryList = cleanUpType.getCategoryList();
                    if (categoryList.getCategory() != null) {
                        Category_type1[] categories = categoryList.getCategory();
                        for (Category_type1 category_type1 : categories) {
                            successCategories.add(category_type1.getValue());

                        }
                    }
                }
            }

        }
        String[] successList = new String[0]; //Collection to array
        if (successCategories != null) {
            successList = successCategories.toArray(new String[successCategories.size()]);
        }

        return successList;
    }

    private static String[] setFailureTypeCleanups(ProcessDeployDetailsList_type0 processDeployDetailsList_type0) {

        List<String> failureCategories = null;
        if (processDeployDetailsList_type0.getCleanUpList() != null &&
                processDeployDetailsList_type0.getCleanUpList().getCleanUp() != null) {
            failureCategories = new ArrayList<String>();
            CleanUpType[] cleanUpTypes = processDeployDetailsList_type0.getCleanUpList().getCleanUp();
            for (CleanUpType cleanUpType : cleanUpTypes) {
                if (cleanUpType.getOn().getValue().equalsIgnoreCase("failure") && cleanUpType.getCategoryList() != null) {
                    CategoryListType categoryList = cleanUpType.getCategoryList();
                    if (categoryList.getCategory() != null) {
                        Category_type1[] categories = categoryList.getCategory();

                        for (Category_type1 category_type1 : categories) {
                            failureCategories.add(category_type1.getValue());
                        }
                    }

                }
            }
        }

        String[] failureList = new String[0]; //Collection to array
        if (failureCategories != null) {
            failureList = failureCategories.toArray(new String[failureCategories.size()]);
        }


        return failureList;

    }

    private static String setGenerateType(ProcessDeployDetailsList_type0 processDeployDetailsList_type0) {

        String type = null;
        if (processDeployDetailsList_type0.getProcessEventsList() == null) {
            type = "all";
        } else {
            if (processDeployDetailsList_type0.getProcessEventsList().getGenerate() != null) {
                if (processDeployDetailsList_type0.getProcessEventsList().getGenerate().getValue().equalsIgnoreCase("all")) {
                    type = "all";
                }
                if (processDeployDetailsList_type0.getProcessEventsList().getGenerate().getValue().equalsIgnoreCase("none")) {
                    type = "none";
                }
            } else {
                if (processDeployDetailsList_type0.getProcessEventsList().getEnableEventsList() != null
                        && processDeployDetailsList_type0.getProcessEventsList().getEnableEventsList().getEnableEvent() != null) {
                    List<String> events = Arrays.asList(processDeployDetailsList_type0.getProcessEventsList().getEnableEventsList().getEnableEvent());
                    if (events.contains("instanceLifecycle") && events.contains("activityLifecycle")
                            && events.contains("dataHandling") && events.contains("correlation") && !events.contains("scopeHandling")) {
                        type = "all";
                    } else {
                        type = "selected";
                    }


                } else {
                    type = "all";
                }
            }
        }
        return type;
    }

    public static void updateBackEnd(ProcessManagementServiceClient processMgtClient, ProcessDeployDetailsList_type0 processDeployDetailsList_type0, DeploymentDescriptorUpdater deployDescriptorUpdater, String[] selecttype, ArrayList<String> scopeNames) throws org.wso2.carbon.bpel.stub.mgt.ProcessManagementException, RemoteException {

        updateScopeEvents(selecttype, scopeNames, deployDescriptorUpdater);
        ProcessStatus processStatus = ProcessStatus.Factory.fromValue(deployDescriptorUpdater.getProcessstate().toUpperCase());
        processDeployDetailsList_type0.setProcessState(processStatus);
        processDeployDetailsList_type0.setIsInMemory(Boolean.parseBoolean(deployDescriptorUpdater.getInmemorystatus()));

        ProcessEventsListType processEventsListType = new ProcessEventsListType();
        EnableEventListType enableEventListType = new EnableEventListType();
        ScopeEventListType scopeEventListType = new ScopeEventListType();
        enableEventListType.setEnableEvent(deployDescriptorUpdater.getEvents());
        scopeEventListType.setScopeEvent(deployDescriptorUpdater.getScopeEvents());
        processEventsListType.setEnableEventsList(enableEventListType);
        processEventsListType.setScopeEventsList(scopeEventListType);


        if (!deployDescriptorUpdater.getGentype().equalsIgnoreCase("selected")) {
            Generate_type1 generate = Generate_type1.Factory.fromValue(deployDescriptorUpdater.getGentype());
            processEventsListType.setGenerate(generate);
        }
        processDeployDetailsList_type0.setProcessEventsList(processEventsListType);

        CleanUpListType cleanUpList = new CleanUpListType();
        CleanUpType successCleanUpType = new CleanUpType();
        On_type1 successOn = On_type1.success;
        successCleanUpType.setOn(successOn);
        CategoryListType successCategoryList = new CategoryListType();

        String[] sCategories = deployDescriptorUpdater.getSuccesstypecleanups();
        if (sCategories != null) {
            for (String categoryname : sCategories) {
                Category_type1 category_type1 = Category_type1.Factory.fromValue(categoryname);
                successCategoryList.addCategory(category_type1);

            }

        }
        successCleanUpType.setCategoryList(successCategoryList);
        cleanUpList.addCleanUp(successCleanUpType);


        CleanUpType failureCleanUpType = new CleanUpType();
        On_type1 failureOn = On_type1.failure;
        failureCleanUpType.setOn(failureOn);
        CategoryListType failureCategoryList = new CategoryListType();

        String[] fCategories = deployDescriptorUpdater.getFailuretypecleanups();
        if (fCategories != null) {
            for (String categoryname : fCategories) {
                Category_type1 category_type1 = Category_type1.Factory.fromValue(categoryname);
                failureCategoryList.addCategory(category_type1);
            }
        }
        failureCleanUpType.setCategoryList(failureCategoryList);
        cleanUpList.addCleanUp(failureCleanUpType);
        processDeployDetailsList_type0.setCleanUpList(cleanUpList);

        processMgtClient.updateDeployInfo(processDeployDetailsList_type0);
    }

    public static void configureDeploymentDescriptorUpdater(ProcessDeployDetailsList_type0 processDeployDetailsList_type0, DeploymentDescriptorUpdater deployDescriptorUpdater) {

        deployDescriptorUpdater.setProcessstate(processDeployDetailsList_type0.getProcessState().getValue());
        deployDescriptorUpdater.setInmemorystatus(String.valueOf(processDeployDetailsList_type0.getIsInMemory()));
        deployDescriptorUpdater.setInvokedServiceList(processDeployDetailsList_type0.getInvokeServiceList());
        deployDescriptorUpdater.setProvideServiceList(processDeployDetailsList_type0.getProvideServiceList());
        deployDescriptorUpdater.setMexInterceptors(processDeployDetailsList_type0.getMexInterperterList());
        deployDescriptorUpdater.setPropertyList(processDeployDetailsList_type0.getPropertyList());
        deployDescriptorUpdater.setScopeEvents(setScopeEventsList(processDeployDetailsList_type0));
        deployDescriptorUpdater.setGentype(setGenerateType(processDeployDetailsList_type0));
        deployDescriptorUpdater.setEvents(setEnabledEventsList(processDeployDetailsList_type0));
        deployDescriptorUpdater.setSuccesstypecleanups(setSuccessTypeCleanups(processDeployDetailsList_type0));
        deployDescriptorUpdater.setFailuretypecleanups(setFailureTypeCleanups(processDeployDetailsList_type0));

    }



    public static String isElementDisabled(boolean authorizedToManageProcesses) {
          if(!authorizedToManageProcesses){
             return DISABLED;
          }else {
              return "" ;
          }
    }

    public static String isProcessStateChecked(DeploymentDescriptorUpdater deployDescriptorUpdater, String processState) {

        if(deployDescriptorUpdater.getProcessstate() != null){
            if(deployDescriptorUpdater.getProcessstate().equalsIgnoreCase(processState)){
                return  CHECKED;
            }
        }
        return "";
    }

    public static String isGenerateTypeChecked(DeploymentDescriptorUpdater deployDescriptorUpdater, String genType) {
        if(deployDescriptorUpdater.getGentype() != null){
            if(deployDescriptorUpdater.getGentype().equalsIgnoreCase(genType)){
                return  CHECKED;
            }
        }
        return "";
    }

    public static String isGivenEventChecked(String[] eventsList, String enabledEvent) {

        if(eventsList!= null){
               for(String targetEvent : eventsList){
                    if(targetEvent.equalsIgnoreCase(enabledEvent)){
                        return CHECKED;
                    }
               }
        }
        return "";
    }
    public static String isInMemoryTypeChecked(DeploymentDescriptorUpdater deployDescriptorUpdater, String inMemory) {

        if(deployDescriptorUpdater.getInmemorystatus() != null){
            if(deployDescriptorUpdater.getInmemorystatus().equalsIgnoreCase(inMemory)){
                return  CHECKED;
            }
        }
        return "";
    }



    /**
     * We heavily use jQuery functions in front-end BPEL component.
     * There are some meta-characters used in jQuery like (":", "."), etc.,
     * eg - http://api.jquery.com/animated-selector/
     * So we should use html ids which should non-conflicted with jQuery api.
     * This method is used to generate non-conflcting html ids w.r.t jQuery api
     * @param originalId
     * @return
     */
    public static String generateJQueryCompliantID(String originalId) {
        String validId = originalId;
        // add any meta characters needed to be defined into this array
        String[] metaCharacters = {":", ".", "-"};
        for (String character : metaCharacters) {
            if (originalId.contains(character)) {
                 // replaces the meta charaters in a bpel package name with double underscores
                validId = validId.replace(character, "__");
            }
        }
        return validId;
    }

    /*
    * when scope events defined in deploy.xml is changed in runtime by DD Editor this method is used to get the updated events list
    * assigned with each scope event. It is got as a string array with the data about the events selected and
    * categories into (5 for each) groups. Then these events list is again written back to the corresponding scope event
    *
    */
    public static void updateScopeEvents(String[] selecttype, ArrayList<String> scopeNames, DeploymentDescriptorUpdater deployDescriptorUpdater) {
        ArrayList<String> valueArray = selecttype!=null?new ArrayList<String>(Arrays.asList(selecttype)):new ArrayList<String>();
            ListIterator<String> it = valueArray.listIterator();
            while (it.hasNext()) {

                String nextVal = it.next();
                if (!nextVal.equalsIgnoreCase("0")) {
                    if (it.hasNext()) {
                        it.next();
                        it.remove();
                    }
                }

            }

            String[] allEnabledEvents = valueArray.toArray(new String[valueArray.size()]);
            int i=0;
            ScopeEventType[] scopeEventTypes = new ScopeEventType[scopeNames.size()];
            for (int j=0;j< scopeNames.size(); j++) {
                ScopeEventType scopeEventType = new ScopeEventType();
                scopeEventType.setScope(scopeNames.get(j));
                String[] events = new String[5];
                for(int k=0; k<5; k++){
                    events[k]= allEnabledEvents[i];
                    i++;
                }
                ArrayList<String> actualEventsList = new ArrayList<String>();
                for(String event: events){
                    if(!event.equalsIgnoreCase("0")){
                       actualEventsList.add(event);
                    }
                }
                String[] eventsArray = actualEventsList.toArray(new String[actualEventsList.size()]);

                EnableEventListType enableEventListType = new EnableEventListType();
                enableEventListType.setEnableEvent(eventsArray);
                scopeEventType.setEnabledEventList(enableEventListType);

                scopeEventTypes[j] = scopeEventType;
            }
            deployDescriptorUpdater.setScopeEvents(scopeEventTypes);

    }
}
