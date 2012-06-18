<!--
/*
* Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
-->
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.*" %>
<jsp:useBean id="entitlementPolicyBean" type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<jsp:setProperty name="entitlementPolicyBean" property="*" />

<%
//    BasicRuleElementDTO basicRuleElementDTO = new BasicRuleElementDTO();
    RuleDTO ruleDTO = new RuleDTO();
    TargetDTO targetDTO = new TargetDTO();
    entitlementPolicyBean.setRuleElementOrder(null);


    //////////////////////// New Set ////////////////////////////////////////////
    int rowNumber = 0;

    List<String> targetCategories = new ArrayList<String>();
    List<String> targetPreFunctions = new ArrayList<String>();
    List<String> targetFunctions = new ArrayList<String>();
    List<String> targetAttributeValues = new ArrayList<String>();
    List<String> targetAttributeIds = new ArrayList<String>();
    List<String> targetAttributeTypes = new ArrayList<String>();
    List<String> targetCombineFunctions = new ArrayList<String>();

    while(true){
        String targetCategory = CharacterEncoder.getSafeText(request.
                getParameter("targetCategory_" + rowNumber));
        if(targetCategory != null){
            targetCategories.add(targetCategory);
        } else {
            break;
        }
        System.out.println(targetCategory);

        String targetPreFunction = CharacterEncoder.getSafeText(request.
                getParameter("targetPreFunction_" + rowNumber));
        if(targetPreFunction != null){
            targetPreFunctions.add(targetPreFunction);
        }

        System.out.println(targetPreFunction);

        String targetFunction = CharacterEncoder.getSafeText(request.
                getParameter("targetFunction_" + rowNumber));
        if(targetFunction != null){
            targetFunctions.add(targetFunction);
        }

        System.out.println(targetFunction);


        String targetAttributeValue = CharacterEncoder.getSafeText(request.
                getParameter("targetAttributeValue_" + rowNumber));
        if(targetAttributeValue != null){
            targetAttributeValues.add(targetAttributeValue);
        }


        System.out.println(targetAttributeValue);

        String targetAttributeId = CharacterEncoder.getSafeText(request.
                getParameter("targetAttributeId_" + rowNumber));
        if(targetAttributeId != null){
            targetAttributeIds.add(targetAttributeId);
        }

        System.out.println(targetAttributeId);




        String targetAttributeType = CharacterEncoder.getSafeText(request.
                getParameter("targetAttributeTypes_" + rowNumber));
        if(targetAttributeType != null){
            targetAttributeTypes.add(targetAttributeType);
        }


        System.out.println(targetAttributeType);

        String targetCombineFunction = CharacterEncoder.getSafeText(request.
                getParameter("targetCombineFunctions_" + rowNumber));
        if(targetCombineFunction != null){
            targetCombineFunctions.add(targetCombineFunction);
        }


        System.out.println(targetCombineFunction);

        rowNumber ++;
    }

    for(int i = 0; i < rowNumber; i++){
        
        RowDTO  rowDTO = new RowDTO();
        if(targetCategories.size() != 0 && targetCategories.size() > i){
            rowDTO.setCategory(targetCategories.get(i));
        }

        System.out.println("111111111111111");

        if(targetCategories.size() != 0 && targetPreFunctions.size() > i){
            rowDTO.setPreFunction(targetPreFunctions.get(i));
        }

        System.out.println("22222222222222222222");

        if(targetCategories.size() != 0 && targetFunctions.size() > i){
            rowDTO.setFunction(targetFunctions.get(i));
        }

        System.out.println("333333333333333333333");

        if(targetCategories.size() != 0 && targetAttributeValues.size() > i){
            rowDTO.setAttributeValue(targetAttributeValues.get(i));
        }

        System.out.println("4444444444444444444444");

        if(targetCategories.size() != 0 && targetAttributeIds.size() > i){
            rowDTO.setAttributeId(targetAttributeIds.get(i));
        }

        System.out.println("55555555555555555555555");
        
        if(targetCategories.size() != 0 && targetAttributeTypes.size() > i){
            rowDTO.setAttributeDataType(targetAttributeTypes.get(i));
        }

        System.out.println("666666666666666666666");

        if(targetCategories.size() != 0 && targetCombineFunctions.size() > i){
            rowDTO.setCombineFunction(targetCombineFunctions.get(i));    
        }

        System.out.println("7777777777777777");                  

        targetDTO.addRowDTO(rowDTO);

    }

    entitlementPolicyBean.setTargetDTO(targetDTO);

System.out.println("Target is SET for TargetDTO");


    ///RULE target

    rowNumber = 0;
    targetDTO = new TargetDTO();

    targetCategories = new ArrayList<String>();
    targetPreFunctions = new ArrayList<String>();
    targetFunctions = new ArrayList<String>();
    targetAttributeValues = new ArrayList<String>();
    targetAttributeIds = new ArrayList<String>();
    targetAttributeTypes = new ArrayList<String>();
    targetCombineFunctions = new ArrayList<String>();

    while(true){
        String targetCategory = CharacterEncoder.getSafeText(request.
                getParameter("ruleTargetCategory_" + rowNumber));
        if(targetCategory != null){
            targetCategories.add(targetCategory);
        } else {
            break;
        }

        System.out.println(targetCategory);

        String targetPreFunction = CharacterEncoder.getSafeText(request.
                getParameter("ruleTargetPreFunction_" + rowNumber));
        if(targetPreFunction != null){
            targetPreFunctions.add(targetPreFunction);
        }

         System.out.println(targetPreFunction);

        String targetFunction = CharacterEncoder.getSafeText(request.
                getParameter("ruleTargetFunction_" + rowNumber));
        if(targetFunction != null){
            targetFunctions.add(targetFunction);
        }


        System.out.println(targetFunction);



        String targetAttributeValue = CharacterEncoder.getSafeText(request.
                getParameter("ruleTargetAttributeValue_" + rowNumber));
        if(targetAttributeValue != null){
            targetAttributeValues.add(targetAttributeValue);
        }



         System.out.println(targetAttributeValue);


        String targetAttributeId = CharacterEncoder.getSafeText(request.
                getParameter("ruleTargetAttributeId_" + rowNumber));
        if(targetAttributeId != null){
            targetAttributeIds.add(targetAttributeId);
        }



         System.out.println(targetAttributeId);



        String targetAttributeType = CharacterEncoder.getSafeText(request.
                getParameter("ruleTargetAttributeTypes_" + rowNumber));
        if(targetAttributeType != null){
            targetAttributeTypes.add(targetAttributeType);
        }



        System.out.println(targetAttributeType);



        String targetCombineFunction = CharacterEncoder.getSafeText(request.
                getParameter("ruleTargetCombineFunctions_" + rowNumber));
        if(targetCombineFunction != null){
            targetCombineFunctions.add(targetCombineFunction);
        }
        System.out.println(targetCombineFunction);
        rowNumber ++;
    }

    for(int i = 0; i < rowNumber; i++){
        RowDTO  rowDTO = new RowDTO();
        if(targetCategories.size() != 0 && targetCategories.size() > i){
            rowDTO.setCategory(targetCategories.get(i));
        }

        System.out.println("111111111111111");

        if(targetCategories.size() != 0 && targetPreFunctions.size() > i){
            rowDTO.setPreFunction(targetPreFunctions.get(i));
        }

        System.out.println("22222222222222222222");

        if(targetCategories.size() != 0 && targetFunctions.size() > i){
            rowDTO.setFunction(targetFunctions.get(i));
        }

        System.out.println("333333333333333333333");

        if(targetCategories.size() != 0 && targetAttributeValues.size() > i){
            rowDTO.setAttributeValue(targetAttributeValues.get(i));
        }

        System.out.println("4444444444444444444444");

        if(targetCategories.size() != 0 && targetAttributeIds.size() > i){
            rowDTO.setAttributeId(targetAttributeIds.get(i));
        }

        System.out.println("55555555555555555555555");

        if(targetCategories.size() != 0 && targetAttributeTypes.size() > i){
            rowDTO.setAttributeDataType(targetAttributeTypes.get(i));
        }

        System.out.println("666666666666666666666");

        if(targetCategories.size() != 0 && targetCombineFunctions.size() > i){
            rowDTO.setCombineFunction(targetCombineFunctions.get(i));
        }

        System.out.println("7777777777777777");

        targetDTO.addRowDTO(rowDTO);
    }


    ruleDTO.setTargetDTO(targetDTO);    
    System.out.println("Target is SET for RULE");
     // Rule condition

    rowNumber = 0;
    targetCategories = new ArrayList<String>();
    targetPreFunctions = new ArrayList<String>();
    targetFunctions = new ArrayList<String>();
    targetAttributeValues = new ArrayList<String>();
    targetAttributeIds = new ArrayList<String>();
    targetAttributeTypes = new ArrayList<String>();
    targetCombineFunctions = new ArrayList<String>();

    while(true){
        String targetCategory = CharacterEncoder.getSafeText(request.
                getParameter("ruleCategory_" + rowNumber));
        if(targetCategory != null){
            targetCategories.add(targetCategory);
        } else {
            break;
        }

         System.out.println(targetCategory);        

        String targetPreFunction = CharacterEncoder.getSafeText(request.
                getParameter("rulePreFunction_" + rowNumber));
        if(targetPreFunction != null){
            targetPreFunctions.add(targetPreFunction);
        }


                 System.out.println(targetPreFunction);


        String targetFunction = CharacterEncoder.getSafeText(request.
                getParameter("ruleFunction_" + rowNumber));
        if(targetFunction != null){
            targetFunctions.add(targetFunction);
        }

          System.out.println(targetFunction);

        String targetAttributeValue = CharacterEncoder.getSafeText(request.
                getParameter("ruleAttributeValue_" + rowNumber));
        if(targetAttributeValue != null){
            targetAttributeValues.add(targetAttributeValue);
        }

               System.out.println(targetAttributeValue);


        String targetAttributeId = CharacterEncoder.getSafeText(request.
                getParameter("ruleAttributeId_" + rowNumber));
        if(targetAttributeId != null){
            targetAttributeIds.add(targetAttributeId);
        }


              System.out.println(targetAttributeId);

        String targetAttributeType = CharacterEncoder.getSafeText(request.
                getParameter("ruleAttributeTypes_" + rowNumber));
        if(targetAttributeType != null){
            targetAttributeTypes.add(targetAttributeType);
        }



                 System.out.println(targetAttributeType);

        String targetCombineFunction = CharacterEncoder.getSafeText(request.
                getParameter("ruleCombineFunctions_" + rowNumber));
        if(targetCombineFunction != null){
            targetCombineFunctions.add(targetCombineFunction);
        }



                 System.out.println(targetCombineFunction);

        rowNumber ++;
    }

    for(int i = 0; i < rowNumber; i++){
        RowDTO  rowDTO = new RowDTO();
        if(targetCategories.size() != 0 && targetCategories.size() > i){
            rowDTO.setCategory(targetCategories.get(i));
        }

        System.out.println("111111111111111");

        if(targetCategories.size() != 0 && targetPreFunctions.size() > i){
            rowDTO.setPreFunction(targetPreFunctions.get(i));
        }

        System.out.println("22222222222222222222");

        if(targetCategories.size() != 0 && targetFunctions.size() > i){
            rowDTO.setFunction(targetFunctions.get(i));
        }

        System.out.println("333333333333333333333");

        if(targetCategories.size() != 0 && targetAttributeValues.size() > i){
            rowDTO.setAttributeValue(targetAttributeValues.get(i));
        }

        System.out.println("4444444444444444444444");

        if(targetCategories.size() != 0 && targetAttributeIds.size() > i){
            rowDTO.setAttributeId(targetAttributeIds.get(i));
        }

        System.out.println("55555555555555555555555");

        if(targetCategories.size() != 0 && targetAttributeTypes.size() > i){
            rowDTO.setAttributeDataType(targetAttributeTypes.get(i));
        }

        System.out.println("666666666666666666666");

        if(targetCategories.size() != 0 && targetCombineFunctions.size() > i){
            rowDTO.setCombineFunction(targetCombineFunctions.get(i));
        }

        System.out.println("7777777777777777");

        ruleDTO.addRowDTO(rowDTO);
    }

System.out.println("Condition is SET for RULE");


    ////////////////////////////////////////////////////////////////////////////





    
    
    String ruleElementOrder = request.getParameter("ruleElementOrder");
    String updateRule = request.getParameter("updateRule");
    String nextPage = request.getParameter("nextPage");
    String ruleId = request.getParameter("ruleId");
    String ruleEffect = request.getParameter("ruleEffect");
    String ruleDescription = request.getParameter("ruleDescription");

    String categoryType = request.getParameter("attributeType");


    String completedRule = request.getParameter("completedRule");
    String editRule = request.getParameter("editRule");

    if(ruleId != null && !ruleId.trim().equals("") && !ruleId.trim().equals("null") && editRule == null ) {

        ruleDTO.setRuleId(ruleId);
        ruleDTO.setRuleEffect(ruleEffect);

        if(ruleDescription != null && ruleDescription.trim().length() > 0 ){
            ruleDTO.setRuleDescription(ruleDescription);
        }


        if(completedRule != null && completedRule.equals("true")){
            ruleDTO.setCompletedRule(true);
        }

        entitlementPolicyBean.setRuleDTO(ruleDTO);
    }

    String forwardTo;

    if(ruleElementOrder != null && !ruleElementOrder.equals("")){
        if(ruleDTO.isCompletedRule() && !"true".equals(updateRule)){
            entitlementPolicyBean.setRuleElementOrder(ruleElementOrder.trim() + ", " +
                                                      ruleDTO.getRuleId());
        } else{
            entitlementPolicyBean.setRuleElementOrder(ruleElementOrder.trim());
        }
    }

    if(completedRule != null && completedRule.equals("true")){
        forwardTo = nextPage + ".jsp?";
    } else {
        forwardTo = nextPage + ".jsp?ruleId=" + ruleId;
        if(categoryType != null && categoryType.trim().length() > 0){
            forwardTo = forwardTo + "&attributeType=" + categoryType;
        }
    }

%>

<script type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>