/*
 * Copyright 2007 WSO2, Inc. http://www.wso2.org
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

var userName;
var locationString = self.location.href;


if (typeof wso2 == "undefined")
{
    var wso2 = {};
}

wso2.mashup = {

    init : function(isSecure) {

            wso2.wsf.Util.initURLs();

            if (isSecure) {
                redirectToHttps();
            }


            //Initializing the helper function for XSLT transformations inherited from WSO2 AdminUI
            wso2.wsf.XSLTHelper.init();


    },

/**
 * @description This method is used to show a yui simple dialog containing warning, help and other messages.
 * @method showSimpleDialog
 * @public
 * @static
 * @param {string} 	msg	        message to display inside simple dialog
 * @param {string} msgType 	    String wich specify the type of the message. Possible types are 'blkicon', 'alrticon', 'hlpicon', 'infoicon', 'warnicon' and 'tipicon'
 * @param {callback} callBack   function that must be execute after user confirms the message. This is optional.
 */
    showSimpleDialog : function(msg, msgType, callBack) {
        var _callBack, iconType;
        if (callBack == null || callBack == "undefine") {
            _callBack = this.handleOK;
        } else {
            _callBack = callBack;
        }

        switch (msgType) {
            case 'blkicon':
                iconType = YAHOO.widget.SimpleDialog.ICON_BLOCK;
                break;
            case 'alrticon':
                iconType = YAHOO.widget.SimpleDialog.ICON_ALARM;
                break;
            case 'hlpicon':
                iconType = YAHOO.widget.SimpleDialog.ICON_HELP;
                break;
            case 'infoicon':
                iconType = YAHOO.widget.SimpleDialog.ICON_INFO;
                break;
            case 'warnicon':
                iconType = YAHOO.widget.SimpleDialog.ICON_WARN;
                break;
            case 'tipicon':
                iconType = YAHOO.widget.SimpleDialog.ICON_TIP;
                break;
        }

        var browser = WSRequest.util._getBrowser();
        var simpledialog = "";

        if (browser == "ie" || browser == "ie7") {
            simpledialog = new YAHOO.widget.SimpleDialog("simpledialog",
            {
                width:"300px",
                x:YAHOO.util.Dom.getViewportWidth() /
                  8, //Manual fix for getting rubbish viewport dimensions in IE
                y:100,
                visible:true,
                draggable:true,
                close:true,
                text:msg,
                icon: iconType,
                buttons:[{ text:"OK", handler:_callBack , isDefault:true}] });

        } else {
            simpledialog = new YAHOO.widget.SimpleDialog("simpledialog",
            {
                width:"300px",
                fixedcenter:true,
                visible:true,
                draggable:true,
                close:true,
                text:msg,
                icon: iconType,
                constraintoviewport: true,
                buttons:[{ text:"OK", handler:_callBack , isDefault:true}] });

        }

        var strSMHeader = '<table border="0" width="100%" cellpadding="0" cellspacing="0">' +
                          '<tr>' +
                          '<td><img src="images/w_small_top_left_edit.gif"/></td>' +
                          '<td class="mashup-editer-hd" >Mashup Server</td>' +
                          '<td><img src="images/w_top_right_edit.gif"/></td>' +
                          '</tr>' +
                          '</table>';

        simpledialog.setHeader(strSMHeader);
        simpledialog.render(document.body);
    },

    handleOK : function() {
        var thisNode = document.getElementById(this.element.id);
        var parent = thisNode.parentNode;
        parent.removeChild(thisNode);
    },


/**
 * @description Handles the response of the service call to login. Used as a callback for WSRequest
 * @method  hanldeLogin
 */
    hanldeLogin : function() {
        var isLogInDone = this.req.responseXML.getElementsByTagName("return")[0].firstChild.nodeValue;
        if (isLogInDone != "true") {
            wso2.mashup.loginFail();
            return;
        }

        WSO2.MashupUtils.CurrentLoggedInUser = this.params;

        //Setting the coockie indicating successful login to Mashup Server AdminUI
        document.cookie = 'userName=' + WSO2.MashupUtils.CurrentLoggedInUser + '; path=/';

        //Setting the cookie indicating successful login to wsasadmin webapp
        document.cookie = 'userName=' + WSO2.MashupUtils.CurrentLoggedInUser + '; path=/wsasadmin/';

        wso2.mashup.loginSuccessful();

    },


/**
 * @description Handles the transition of view from login-form to main after a successful login
 * @method  loginSuccessful
 */
    loginSuccessful : function() {

        WSO2.MashupUtils.CurrentLoggedInUser = WSO2.MashupUtils.getCookieValue("userName");

        if (document.getElementById("login-data")) {
            var loginDataDiv = document.getElementById("login-data");
            loginDataDiv.innerHTML = "Signed in as <strong>" +
                                     WSO2.MashupUtils.CurrentLoggedInUser +
                                     "</strong>&nbsp;&nbsp;|&nbsp;&nbsp;<a href='#' onclick='javascript:wso2.mashup.handleLogout(); " +
                                     "return false;'>Sign Out</a>"
            loginDataDiv.style.display = "inline";
        }

        if (document.getElementById("logincontainer")) {
            document.getElementById("logincontainer").style.display = "none";
        }

        if (document.getElementById("content")) {
            document.getElementById("content").style.display = "block";
            wso2.mashup.createUI();
        }
    },


/**
 * @description Handles a failed login attempt
 * @method  loginFail
 */
    loginFail : function() {
        wso2.mashup.showSimpleDialog("Login failed. Please recheck the user name and password and try again.", "warnicon");
    },


/**
 * @description Handles the logout service call visually by transitioning the view and re-setting cookies.
 * @method  handleLogout
 */
    handleLogout : function() {


        wso2.wsf.Util.deleteCookie("JSESSIONID");

        wso2.wsf.Util.deleteCookie("userName");

        WSO2.MashupUtils.changeModelDialogStatus(true);

        document.getElementById("txtUserName").value = "";
        document.getElementById("txtPassword").value = "";

        document.getElementById("login-data").style.display = "none";
        document.getElementById("content").style.display = "none";
        document.getElementById("logincontainer").style.display = "block";

    },


/**
 * @description Extracts data received from the service call and calls the utility function to display the Mashup Editor.
 *              Used as a callback for WSRequest
 * @method  populateServiceEditor
 */
    populateServiceEditor : function() {

        var returnElementList = this.req.responseXML.getElementsByTagName("ns:return");
        // Older browsers might not recognize namespaces (e.g. FF2)
        if (returnElementList.length == 0)
            returnElementList = this.req.responseXML.getElementsByTagName("return");
        var returnElement = returnElementList[0];

        var serviceData = returnElement;

        WSO2.MashupUtils.showServiceEditor(serviceData);
    },


/**
 * @description Draws the initial user interface presented to the user after a successful login
 * @method  createUI
 */
    createUI : function() {
        /*wso2.mashup.services.listServiceGroups(wso2.mashup.MashupListModel.populateMashupsList, "true");
        wso2.mashup.MashupListModel.mashupListRefresherId =
        window.setInterval("wso2.mashup.services.listServiceGroups(wso2.mashup.MashupListModel.populateMashupsList, 'true')", 60000);

        //Restore the hidden dialog masks if it was aforce logout by the system
        WSO2.MashupUtils.changeModelDialogStatus(false);*/
    }

};
