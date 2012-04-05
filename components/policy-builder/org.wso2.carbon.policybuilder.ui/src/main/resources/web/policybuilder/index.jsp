<%--
 * Copyright 2006-2008 WSO2, Inc. http://www.wso2.org
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
--%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="java.io.*" %>

<%--
<link rel="stylesheet" type="text/css" href="../admin/css/main.css"/>
<link rel="stylesheet" type="text/css" href="css/local-styles.css"/>
<link rel="stylesheet" type="text/css" href="js/yui/menu/assets/skins/sam/menu.css"/>
<link rel="stylesheet" type="text/css" href="js/yui/treeview/assets/treeview.css"/>
<link rel="stylesheet" type="text/css"
      href="js/yui/tabview/assets/skins/policyeditor/tabview.css"/>
<link rel="stylesheet" type="text/css" href="js/yui/button/assets/skins/sam/button.css"/>
  --%>

<link href="../yui/build/container/assets/skins/sam/container.css" rel="stylesheet" type="text/css"
      media="all"/>
<script type="text/javascript" src="../yui/build/utilities/utilities.js"></script>

 <%--
 
<script type="text/javascript" src="../yui/build/container/container-min.js"></script>
<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/dragdrop/dragdrop-min.js"></script>


<script type="text/javascript" src="js/yui/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="js/yui/element/element-beta-min.js"></script>
<script type="text/javascript" src="js/yui/container/container_core-min.js"></script>
<script type="text/javascript" src="js/yui/menu/menu-min.js"></script>
<script type="text/javascript" src="js/yui/treeview/treeview-min.js"></script>
<script type="text/javascript" src="js/yui/tabview/tabview-min.js"></script>
<script type="text/javascript" src="js/yui/button/button-beta-min.js"></script>


<script type="text/javascript" src="js/xml-for-script/tinyxmlsax.js"></script>
<script type="text/javascript" src="js/xml-for-script/tinyxmlw3cdom.js"></script>

<script type="text/javascript" src="js/sax-tree.js"></script>
<script type="text/javascript" src="js/sax-policy-menu.js"></script>
<script type="text/javascript" src="js/policy-builder.js"></script>

--%>

<fmt:bundle basename="org.wso2.carbon.policybuilder.ui.i18n.Resources">
<carbon:breadcrumb 
		label="Policy"
		resourceBundle="org.wso2.carbon.policybuilder.ui.i18n.Resources"
		topPage="true" 
		request="<%=request%>" />


<script>
    var callback =
        {
            upload:handleUpload
        };

    function handleUpload(o) {
            var responseText = o.responseText;
            
            if (responseText) {
	       var text = removeCDATA(responseText);
	       text = removeEncoding(removeEncoding(text,"&lt;","<"),"&gt;",">");
	       //alert(text)    ;
	       //CARBON.showInfoDialog(text);
               var textArea = document.getElementById("policyText");
               textArea.innerHTML=text;

                var form = document.getElementById("post-back-form");
                var policyText = form.policy;
                policyText.value = text;
                form.submit();
            } else {
                CARBON.showWarningDialog('<fmt:message key="error.fileUploadFailed"/>');
            }
        }
    
    function submitSoapFile(){
             
        var form = document.getElementById("Form1");
        YAHOO.util.Connect.setForm(form, true, true);
        YAHOO.util.Connect.asyncRequest("POST", form.getAttribute("action"), callback, null);
        CARBON.showInfoDialog('<fmt:message key="msg.sent"/>');
        
    }

   
     function submitSoapTextFile(){
     
         var soapTextInner = document.getElementById("policyText").value;
         var form = document.getElementById("Form2");
       //  alert(soapTextInner);
         form.soapText.value = soapTextInner;
         YAHOO.util.Connect.setForm(form, true, true);
         YAHOO.util.Connect.asyncRequest("POST", form.getAttribute("action"), callback, null);
         CARBON.showInfoDialog('<fmt:message key="msg.sent"/>');
    
     /*     document.getElementById("soap").value = soapText;
        alert(soapText);
        alert(document.getElementById("soap").value);
        var form = document.getElementById("soapTextForm");
        YAHOO.util.Connect.setForm(form, true, true);
        YAHOO.util.Connect.asyncRequest("POST", form.getAttribute("action"), callback, null);

      */   

    }

    function removeCDATA(taggedStr){
	 if(taggedStr){
		candidateString = taggedStr;
		    if (candidateString.substring(0, 9) == "<![CDATA[") {
			//Removing <![CDATA[
			candidateString = candidateString.substring(9, candidateString.length);
			//Removing ]]>
			candidateString = candidateString.substring(0, candidateString.length - 3);
		    } else if (candidateString.substring(0, 12) == "&lt;![CDATA[") {
			//Removing &lt;![CDATA[
			candidateString = candidateString.substring(12, candidateString.length);
			//Removing ]]&lt;
			candidateString = candidateString.substring(0, candidateString.length - 6);
		    }
			else if (candidateString.substring(0, 17) == "<pre>&lt;![CDATA[") {
			//Removing &lt;![CDATA[
			candidateString = candidateString.substring(17, candidateString.length);
			//Removing ]]&lt;
			candidateString = candidateString.substring(0, candidateString.length - 12);
		    }
			return candidateString;
	}
	return taggedStr;
     }

  function removeEncoding(inputStr,searchStr,replaceStr){
       if(!(inputStr.indexOf(searchStr) > -1)){
		return inputStr;	
	}

        var myNewString = inputStr;
		while(myNewString.indexOf(searchStr) > -1){
			myNewString = myNewString.replace(searchStr, replaceStr);

		}
	
	return myNewString;
}
 
    var attributes = {
        height: { to: 100 },
        width: { to: 100 }
	    };

    var anim = new YAHOO.util.Anim('demo', attributes);

    function startAnim(){
          anim.animate();

    }




    function responseFailure(o){
         alert("failed");    
    }


</script>





<div id="middle">
<h2><fmt:message key="policy.editor"/></h2>
    <div style="padding-top:10px;padding-bottom:10px"><p><fmt:message key="tool.tip"/></p></div>

<div id="workArea">
                 

                               <form name="Form1" id="Form1" method ="POST" action="../../fileupload/UploadExec" ENCTYPE='multipart/form-data'>
                                   <table class="styledLeft">
                                    <thead>
                                    <tr>
                                      <th>  <fmt:message key="select.soap.local"/>  </th>

                                    </tr>
                                        
                                    </thead>

                                    <tr>
                                        <td class="formRow">
                                            <table class="normal">
                                                <tr>
                                                    <td style="width:100px">
                                                        <label><fmt:message key="select.soap"/></label>
                                                     </td>


                                                   <td>
                                                    <input type="file" name="soapFile" id="soapFile" >
                                                   </td>
                                                </tr>
                                           </table>
                                        </td>
                                     </tr>
                                      <tr>
                                        <td class="buttonRow">
                                            <input type="button" name="Submit" value='<fmt:message key="submit.soap"/>' onclick="submitSoapFile()">

                                       </td>
                                       </tr>
                                   </table>


                               </form>
                                 <br><br>
                               <form name="Form2" id="Form2" method ="POST" action="../../fileupload/UploadExec?action=soapTextAction" >
                               <table class="styledLeft">

                                   <thead>
                                    <tr>
                                      <th>  <fmt:message key="select.soap.text"/>  </th>

                                    </tr>

                                    </thead>

                                   <tr>
                                       <td class="formRow">
                                        <table class="normal">
                                            <tr>
                                                <td style="height:250px; width: 700px;">
                                                    <input type="text" id="policyText" name="policyText" style="height:220px; width: 660px;" >

                                                 </td>
                                            </tr>
                                        </table>
                                       </td>
                                   </tr>
                                   <tr>
                                        <td class="buttonRow">
                                            <input type="button" name="Submit" value='<fmt:message key="submit.soap.text"/>' onclick="submitSoapTextFile()">
                                            <input type="hidden" name="soapText" id="soapText" >
                                       </td>
                                   </tr>
                                   <tr>
                                        <td class="formRow">
                                               <fmt:message key="ps.note"/>         
                                       </td>
                                   </tr>
                               </table>

                               </form>

                  





    <!-- This div will contain the hidden form, which will do the POST back when saving a Policy -->
    <div id="post-back-content" style="display: none;">
        <form name="postbackForm" id="post-back-form" action="../policyeditor/index.jsp" method="post">
                <input type="hidden" name="policy" id="policy">
        </form>
    </div>    


<%

    String soapText = request.getParameter("soapText");


    if (soapText != null && !soapText.equals("")) {  %>
         <div id="soapTextDiv" name="soapTextDiv" >
                <form name="soapTextForm" id="soapTextForm" action="<%=CarbonUIUtil.getServerURL(config.getServletContext(), session)%>UploaderService/getPolicy" method="post">
                    <input type="hidden" name="soap" id="soap" value="">
                    <div name="soapTextArea" id="soapTextArea">
                      <%=soapText%>
                    </div>
                </form>
             <script>

                 submitSoapTextFile();

            </script>
          </div>

  <%
    }

  %>
<%--
<script type="text/javascript">
    wso2.wsf.Util.initURLs();
    wso2.wsf.XSLTHelper.init();
    window.dhtmlHistory.initialize();

    // store the policy metadata to be used by javascript code

    // If the policy is to be loaded from a URL
    var currentPolicyURL = '<%=policyURL%>';

    // If the policy is posted to the editor with additional meta-data
    var policyText = '<%=policyText%>';
    var callbackURL = '<%=callbackURL%>';

    // Create design and source view tabs
    var tabView = new YAHOO.widget.TabView('editor-canvas');

    var serviceBaseURL = '<%=CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(),request.getSession())%>';

    // Once the DOM has loaded, we can go ahead and set up the policy tree
    function beginPolicyRetrieval() {
        if (currentPolicyURL != "null") {
            getPolicyDoc('<%=policyURL%>');
        } else if (policyText != "") {
            syncRawPolicyView(policyText);
            buildTreeView(policyText);
        }
    }
    YAHOO.util.Event.onDOMReady(beginPolicyRetrieval);
</script>
--%>


</div>
</div>
</fmt:bundle>
