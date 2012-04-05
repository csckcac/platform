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

if (typeof WSO2 == "undefined") {
    /**
     * The WSO2 global namespace object.  If WSO2 is already defined, the
     * existing WSO2 object will not be overwritten so that defined
     * namespaces are preserved.
     * @class WSO2
     * @static
     */
    var WSO2 = {};
}


/**
 * WSO2.MashupUtils is a Class containing utility functions to create UI widgets and tasks.
 * @class WSO2.MashupUtils
 * @constructor
 * @param {String} mashupID			The Masup ID uniquely representing the Mashup
 * @param {String} MashupTitle		The Title of Mashup
 * @param {String} MashupDesc		The Description about Mashup
 */
WSO2.MashupUtils = function() {
    this._countW = 0;
    this.col1 = false;
    this.arrayXML = new Array();
};


/**
 * @description Holds the username of the current logged in user. Set after a successful login.
 * @property CurrentLoggedInUser
 * @type {String}
 */
WSO2.MashupUtils.CurrentLoggedInUser = "";


/**
 * @description Contains a pointer to any model dialog displayed at a given time.
 *              Used to restore state, in case of a system initiated logout
 * @property currentActiveModelDialog
 * @type {YUI dialog}
 */
WSO2.MashupUtils.currentActiveModelDialog = null;


/**
 * @description Creates a new Mashup object from a well formed xml
 * @method generateMashupFromXml
 * @param {XML} xmlDataContent
 * @return {WSO2.Mashup} Generated object
 */
WSO2.MashupUtils.generateMashupFromXml = function(xmlDataContent) {

    var mashupId, mashupTitle, mashupDesc, mashupEprs, mashupOperartions, mashupStatus, mashupServiceType;

    if (xmlDataContent.getElementsByTagName("ns:serviceId").length > 0) {
        mashupId = xmlDataContent.getElementsByTagName("ns:serviceId")[0].firstChild.nodeValue;
        mashupTitle = xmlDataContent.getElementsByTagName("ns:name")[0].firstChild.nodeValue;
        mashupDesc = xmlDataContent.getElementsByTagName("ns:description")[0].firstChild.nodeValue;
        mashupEprs = xmlDataContent.getElementsByTagName("ns:eprs");
        mashupOperartions = xmlDataContent.getElementsByTagName("ns:operations");
        mashupStatus = xmlDataContent.getElementsByTagName("ns:active")[0].firstChild.nodeValue;
        mashupServiceType =
        xmlDataContent.getElementsByTagName("ns:serviceType")[0].firstChild.nodeValue;
    } else {
        mashupId = xmlDataContent.getElementsByTagName("serviceId")[0].firstChild.nodeValue;
        mashupTitle = xmlDataContent.getElementsByTagName("name")[0].firstChild.nodeValue;
        mashupDesc = xmlDataContent.getElementsByTagName("description")[0].firstChild.nodeValue;
        mashupEprs = xmlDataContent.getElementsByTagName("eprs");
        mashupOperartions = xmlDataContent.getElementsByTagName("operations");
        mashupStatus = xmlDataContent.getElementsByTagName("active")[0].firstChild.nodeValue;
        mashupServiceType =
        xmlDataContent.getElementsByTagName("serviceType")[0].firstChild.nodeValue;
    }

    return new wso2.mashup.Mashup(mashupId, mashupTitle, mashupDesc, mashupEprs, mashupOperartions, mashupStatus, mashupServiceType);
};


/**
 * @description Displays the service editor.
 * @method showServiceEditor
 * @param xmlBodyContent received as a response to the backened service call
 */
WSO2.MashupUtils.showServiceEditor = function(xmlBodyContent)
{
    var browser = WSRequest.util._getBrowser();
    var serviceEditor = "";

    if (browser == "ie" || browser == "ie7") {
        serviceEditor = new wso2.mashup.MashupEditor("mashupEditor", {
            width: "60em",
            x:YAHOO.util.Dom.getViewportWidth() /
              8, //Manual fix for getting rubbish viewport dimensions in IE
            visible: false,
            modal:true,
            draggable:true
        }, xmlBodyContent);

    } else {
        serviceEditor = new wso2.mashup.MashupEditor("mashupEditor", {
            width: "60em",
            visible: false,
            fixedcenter:true,
            modal:true,
            draggable:true,
            constraintoviewport:true
        }, xmlBodyContent);
    }

    serviceEditor.render("content");

    WSO2.MashupUtils.currentActiveModelDialog = serviceEditor;
};


/**
 * @description Adds a Mashup Widget to the inerface.
 * @method createMashupWidget
 * @param {WSO2.Mashup} mashupObj The Mashup object containing details about mashup.
 */
WSO2.MashupUtils.createMashupWidget = function(mashupObj) {

    var mashupWidget = new wso2.mashup.MashupWidget(mashupObj.getID(), {visible:true, draggable:true, close:true, minimize:true}, mashupObj);
    mashupWidget.Draw();

    if (!this.col1) {
        mashupWidget.render('col_1');
        this.col1 = true;
    } else {
        mashupWidget.render('col_2');
        this.col1 = false;
    }

    //Adding the widget to the list model
    wso2.mashup.MashupWidgetModel.mashupWidgetArray.push(mashupWidget);
};


/**
 * @description Creates and displays the Mashup sharing dialog
 * @method showMashupSharingDialog
 * @param {String} serviceName The name of the service elected to share
 */
WSO2.MashupUtils.showMashupSharingDialog = function (serviceName, localUserName) {

    //Creating the content div
    var strFrmService = '<table align="center" border="0">' +
                        '<tr><td nowrap  height="25"><b>Destination Server Address: </b></td><td><input name="txtDestAddress" type="text" id="txtDestAddress" value="https://mooshup.com" size="50"/></td></tr>' +
                        '<tr><td nowrap  height="25"><b>Overwrite if the service already exists? </b></td><td><input name="chkOverwrite" type="checkbox" id="chkOverwrite"/></td></tr>' +
                        '<tr><td nowrap  height="25"><b>Migrate tags? </b></td><td><input name="chkTags" type="checkbox" id="chkTags"/></td></tr>' +
                        '<tr><td height="25"><hr>Please enter your login information for the destination server</td></tr>' +
                        '<tr><td nowrap  height="25"><b>Destination Server Username: </b></td><td><input name="txtDestUsername" type="text" id="txtDestUsername" size="50"/></td></tr>' +
                        '<tr><td nowrap  height="25"><b>Destination Server Password: </b></td><td><input name="txtDestPassword" type="password" id="txtDestPassword" size="50"/></td></tr>' +
                        '<tr><td></td><td nowrap height="30" valign="bottom"><input type="button" id="cmdShare" value="Share" onclick="WSO2.MashupUtils.shareService(\'' +
                        serviceName + '\', \'userpass\',\'' + localUserName +
                        '\', \'noBasicAuth\',\'' + null + '\',\'' + null + '\',\'' + null +
                        '\',\'' + null + '\',\'' + null + '\');"/></td></tr>';

    /* Disabled for review.
    if (InformationCard.AreCardsSupported()) {
        strFrmService +=
        '<tr><td height="25"><hr>If you have an associated InfoCard to login to the destination server, use the option below</td><td><img src="images/infocard_92x64.png" border="0"</td></tr>' +
        '<tr><td height="25"><input type="button" onclick="WSO2.MashupUtils.shareService(\'' +
        serviceName + '\', \'infocard\',\'' + localUserName + '\', \'noBasicAuth\',\'' + null +
        '\',\'' + null + '\',\'' + null + '\',\'' + null + '\',\'' + null +
        '\');" value="Share with infocard"/></td></tr>';
    }
    */

    strFrmService += '</table>' +
                     '<label id="lblStatus" style="width: auto;"></label>';

    YAHOO.util.Event.onContentReady('doc3', function() {
        // Instantiate the Dialog
        WSO2.MashupUtils.dialog("Share " + serviceName +
                                " with others", strFrmService, 500, 100, 'txtDestUsername');
    });
};

WSO2.MashupUtils.showMashupSharingWithHTTPBasicAuthDialog =
function (serviceName, localUserName, mode, destAddress, destUsername, destPassword,
          overwriteExisting, migrateTags) {

    //Creating the content div
    var strFrmService = '<table align="center" border="0">' +
                        '<tr><td nowrap  height="25"><b>Username: </b></td><td><input name="txtBasicAuthUsername" type="text" id="txtBasicAuthUsername" value="" size="50"/></td></tr>' +
                        '<tr><td nowrap  height="25"><b>Password: </b></td><td><input name="txtBasicAuthPassword" type="password" id="txtBasicAuthPassword" size="50"/></td></tr>' +
                        '<tr><td></td><td nowrap height="30" valign="bottom">';

    if (mode == "infocard") {
        strFrmService +=
        '<input type="button" onclick="WSO2.MashupUtils.shareService(\'' +
        serviceName + '\', \'infocard\',\'' + localUserName + '\' , \'withBasicAuth\',\'' +
        destAddress + '\',\'' + destUsername + '\',\'' + destPassword + '\',\'' +
        overwriteExisting + '\',\'' + migrateTags + '\');" value="Share Service"/></td></tr>';
    } else {
        strFrmService +=
        '<input type="button" id="cmdShare" value="Share" onclick="WSO2.MashupUtils.shareService(\'' +
        serviceName + '\', \'userpass\',\'' + localUserName + '\' , \'withBasicAuth\',\'' +
        destAddress + '\',\'' + destUsername + '\',\'' + destPassword + '\',\'' +
        overwriteExisting + '\',\'' + migrateTags + '\');"/></td></tr>';
    }

    strFrmService += '</table>' +
                     '<label id="lblStatus" style="width: auto;"></label>';

    YAHOO.util.Event.onContentReady('doc3', function() {
        // Instantiate the Dialog
        WSO2.MashupUtils.dialog("Enter credentials to authenticate using HTTP Basic Authentication", strFrmService, 500, 100, 'txtBasicAuthUsername');
    });
};


WSO2.MashupUtils.shareService = function (serviceName, mode, localUserName, basicAuth, destAddress,
                                          destUsername, destPassword, overwriteExisting,
                                          migrateTags) {
    function submitServiceCallback() {

        var returnElementList = this.req.responseXML.getElementsByTagName("ns:return");
        // Older browsers might not recognize namespaces (e.g. FF2)
        if (returnElementList.length == 0)
            returnElementList = this.req.responseXML.getElementsByTagName("return");
        var returnElement = returnElementList[0];

        var response = returnElement.firstChild.nodeValue;

        // If share succeeds, close initiation dialog and show one with links to target and a close option.  
        if (response == "true") {
            WSO2.MashupUtils.dialog.close();
            var mashupName = serviceName.split('-')[1];

            // Create the destination mashup URL, to be displayed on the sharing success dialog.
            var len = destAddress.length;
            var position = eval(len - 1);
            var lastChar = destAddress.substring(position, len);
            var mashupUrl;

            if (lastChar != "/") {
                destAddress = destAddress + '/';
            }

            // When shared via infocard, the destination server's username and therefore script-dir is not known.
            if (destUsername != "") {
                mashupUrl = destAddress + SERVICE_PATH + '/' + destUsername + '/' + mashupName;
            } else {
                mashupUrl = destAddress;
            }

            var strShareSuccess = '<table align="center" border="0">' +
                                  '<tr><td nowrap  height="25">The Mashup ' + mashupName +
                                  ' has been successfully shared!</td></tr>' +
                                  '<tr><td nowrap  height="25">Click <a href="' + mashupUrl +
                                  '">here</a> to view the mashup on ' + destAddress
                    + ', or <a href="javascript:WSO2.MashupUtils.dialog.close()">close</a> this window.</td></tr>' +
                                  '</table>';

            // Instantiate the Dialog
            WSO2.MashupUtils.dialog("Mashup Sharing Successful", strShareSuccess, 500, 100, null);


        } else {
            this.params.innerHTML =
            "Failed to share the service. Please verify the destination address.";
        }
    }

    function submitServiceError() {

        var error = this.req.error;
        if (!error) {
            this.params.innerHTML =
            "Failed to share the service. Please refer to system admin for more details.";
        } else if (error.reason.indexOf("Transport error: 401 Error: Authorization Required") >
                   -1) {
            if (document.getElementById("txtBasicAuthUsername") != null) {
                var labelElement = document.getElementById("lblStatus");
                labelElement.innerHTML =
                "Credentials provided for HTTP Basic Authentication are incorrect. PLease reenter your username and password."
            } else {
                WSO2.MashupUtils.dialog.close();
                WSO2.MashupUtils.showMashupSharingWithHTTPBasicAuthDialog(serviceName, localUserName,
                        mode, destAddress, destUsername, destPassword, overwriteExisting, migrateTags);
            }
        } else if (error.reason.indexOf("Access Denied. Please login first") > -1) {
            wso2.wsf.Util.alertMessage("Your session has expired.");
                //Envoking force logout
            wso2.mashup.services.logout(wso2.mashup.handleLogout);
        } else if (error.reason.indexOf("UnknownHostException") > -1) {
            this.params.innerHTML = "Failed to share the service. Unknown Host : '" +
                                    error.reason + "'";
        } else if (error.reason.indexOf("No trusted certificate found") > -1) {
            this.params.innerHTML =
            "Failed to share the service since a trusted certificate was not found for the destination site.<br/> You can manage your trusted certificates <a target='_blank' href='cert_manager.jsp'>here</a>. (Link opens a new window)";
        } else {
            this.params.innerHTML = "Failed to share the service. " + error.reason;

        }
    }

    var labelElement = document.getElementById("lblStatus");
    if (basicAuth != "withBasicAuth") {
        destAddress = document.getElementById("txtDestAddress").value;
        destUsername = document.getElementById("txtDestUsername").value;
        destPassword = document.getElementById("txtDestPassword").value;
        overwriteExisting = "false";
        if (document.getElementById("chkOverwrite").checked) {
            overwriteExisting = "true";
        }
        migrateTags = "false";
        if (document.getElementById("chkTags").checked) {
            migrateTags = "true";
        }
    }

    if (WSO2.MashupUtils.isUrl(destAddress)) {

        var infoCardToken;
        var basicAuthUsername = "";
        var basicAuthPassword = "";
        if (basicAuth == "withBasicAuth") {
            labelElement.innerHTML =
            "Contacting backend services. Please wait... Authenticating to remote site via HTTP Basic Authentication";
            basicAuthUsername = document.getElementById("txtBasicAuthUsername").value;
            basicAuthPassword = document.getElementById("txtBasicAuthPassword").value;
            labelElement.innerHTML = "Contacting backend services. Please wait...";
        }

        if (mode == "infocard") {
            infoCardToken = InformationCard.GetToken();
            wso2.mashup.services.shareServiceIC(serviceName, destAddress, infoCardToken, overwriteExisting, migrateTags, localUserName, submitServiceCallback, labelElement, submitServiceError, basicAuth, basicAuthUsername, basicAuthPassword);
        } else {
            wso2.mashup.services.shareService(serviceName, destAddress, destUsername, destPassword, overwriteExisting, migrateTags, localUserName, submitServiceCallback, labelElement, submitServiceError, basicAuth, basicAuthUsername, basicAuthPassword);
        }
    }
    else {
        labelElement.innerHTML = "Please enter a valid server address here ...";
    }
};

/**
 * @description Displays the dialog to create a new mashup.
 * @method showCreateMashupDialog
 */
WSO2.MashupUtils.showCreateMashupDialog = function(userName) {

    //Creating a dialog to get the new Mashup's name
    var parent = document.createElement("div");
    parent.setAttribute("id", "MashupCreationDialog");

    var lblMashupName = document.createElement("label");
    var txtStrong = document.createElement("strong");
    txtStrong.appendChild(document.createTextNode("Enter the name for your new Mashup: "));
    lblMashupName.appendChild(txtStrong);
    parent.appendChild(lblMashupName);

    parent.appendChild(document.createElement("br"));

    var txtMashupName = document.createElement("input");
    txtMashupName.setAttribute("type", "text");
    txtMashupName.setAttribute("id", "txtMashupName");
    txtMashupName.setAttribute("size", "50");
    parent.appendChild(txtMashupName);

    parent.appendChild(document.createElement("br"));
    parent.appendChild(document.createElement("br"));

    var lblStatusMessages = document.createElement("label");
    lblStatusMessages.setAttribute("id", "lblStatus");
    parent.appendChild(lblStatusMessages);

    parent.appendChild(document.createElement("br"));

    var cmdCancel = document.createElement("input");
    cmdCancel.setAttribute("type", "button");
    cmdCancel.setAttribute("id", "cmdCancel");
    cmdCancel.value = "Cancel";
    cmdCancel.setAttribute("onclick", "WSO2.MashupUtils.dialog.close();");
    cmdCancel.setAttribute("style", "float: right;");
    parent.appendChild(cmdCancel);

    var cmdSubmit = document.createElement("input");
    cmdSubmit.setAttribute("type", "button");
    cmdSubmit.setAttribute("id", "cmdSubmit");
    cmdSubmit.value = "Create";
    cmdSubmit.setAttribute("onclick", "WSO2.MashupUtils.makeServiceNameValidationRequest('" +
                                      userName + "');");
    cmdSubmit.setAttribute("style", "float: right;");
    parent.appendChild(cmdSubmit);

    YAHOO.util.Event.onContentReady('doc3', function() {
        // Instantiate the Dialog
        WSO2.MashupUtils.dialog("Create a new mashup", parent.innerHTML, 100, 100, 'txtMashupName');
    });
};

/**
 * @description Displays the dialog to warn about tag deletion and get confirmation.
 * @method showTagDeletionDialog
 */
WSO2.MashupUtils.showTagDeletionDialog = function(tag) {

    //Creating a dialog to show the warning and get confirmation.
    var parent = document.createElement("div");
    parent.setAttribute("id", "TagDeletionDialog");

    var lblWarnMessage = document.createElement("label");
    var txtStrong = document.createElement("strong");
    txtStrong.appendChild(document.createTextNode("Please confirm that you wish to delete the '" +
                                                  tag +
                                                  "' tag added by any and all users to this Mashup!"));
    lblWarnMessage.appendChild(txtStrong);
    parent.appendChild(lblWarnMessage);

    parent.appendChild(document.createElement("br"));
    parent.appendChild(document.createElement("br"));

    var cmdCancel = document.createElement("input");
    cmdCancel.setAttribute("type", "button");
    cmdCancel.setAttribute("id", "cmdCancel");
    cmdCancel.value = "Cancel";
    cmdCancel.setAttribute("onclick", "WSO2.MashupUtils.dialog.close();");
    cmdCancel.setAttribute("style", "float: right;");
    parent.appendChild(cmdCancel);

    var cmdSubmit = document.createElement("input");
    cmdSubmit.setAttribute("type", "button");
    cmdSubmit.setAttribute("id", "cmdSubmit");
    cmdSubmit.value = "Delete";
    cmdSubmit.setAttribute("onclick", "removeTag('" + tag + "');WSO2.MashupUtils.dialog.close();");
    cmdSubmit.setAttribute("style", "float: right;");
    parent.appendChild(cmdSubmit);

    YAHOO.util.Event.onContentReady('doc3', function() {
        // Instantiate the Dialog
        WSO2.MashupUtils.dialog("Confirm Tag Deletion", parent.innerHTML, 400, 100, 'cmdCancel');
    });
};

WSO2.MashupUtils.makeServiceNameValidationRequest = function(userName) {

    function submitServiceCallback() {

        var returnElementList = this.req.responseXML.getElementsByTagName("ns:return");
        // Older browsers might not recognize namespaces (e.g. FF2)
        if (returnElementList.length == 0)
            returnElementList = this.req.responseXML.getElementsByTagName("return");
        var returnElement = returnElementList[0];

        var response = returnElement.firstChild.nodeValue;

        if (response == "OK") {
            //Redirecting to the actual editor
            window.location.href = "editor.jsp?action=new&mashup=" + this.params[1]
        } else {
            this.params[0].innerHTML = response;
        }
    }


    var labelElement = document.getElementById("lblStatus");
    var mashupName = document.getElementById("txtMashupName").value;

    if (!(WSO2.MashupUtils.trim(mashupName) == "")) {

        //Encoding to escape special characters
        var mashupPath = encodeURI("/mashups/" + userName + "/" + mashupName);

        labelElement.innerHTML = "";
        labelElement.appendChild(document.createTextNode("Contacting backend services for validation. Please wait..."));

        var params = new Array();
        params[0] = labelElement;
        params[1] = mashupName;

        wso2.mashup.services.isPossibleToCreate(mashupPath, submitServiceCallback, params);

    }
    else {
        labelElement.innerHTML = "";
        labelElement.appendChild(document.createTextNode("Please enter a name for your new mashup."));
    }

};


/**
 * @description Constructs an XMLHTTP request object in a browser specific manner.
 * @method createXmlHttpRequest
 */
WSO2.MashupUtils.createXmlHttpRequest = function() {

    var request;

    // Lets try using ActiveX to instantiate the XMLHttpRequest object
    try {
        request = new ActiveXObject("Microsoft.XMLHTTP");
    } catch(ex1) {
        try {
            request = new ActiveXObject("Msxml2.XMLHTTP");
        } catch(ex2) {
            request = null;
        }
    }

    // If the previous didn't work, lets check if the browser natively support XMLHttpRequest
    if (!request && typeof XMLHttpRequest != "undefined") {
        //The browser does, so lets instantiate the object
        request = new XMLHttpRequest();
    }

    return request;
};


/**
 * @description Evaluates a given string in order to verify whether it contais a URL
 * @method isUrl
 * @return {boolean}
 */
WSO2.MashupUtils.isUrl = function (urlToCheck) {
    var regexp = /(ftp|http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/
    return regexp.test(urlToCheck);
};


/**
 * @description Removes leading and trailing spaces from a string
 * @method trim
 * @param {String} stringToTrim
 * @return {String} Trimmed verson of the given string
 */
WSO2.MashupUtils.trim = function(stringToTrim) {
    return stringToTrim.replace(/^\s+|\s+$/g, "");
};


/**
 * @description Verifies a given string as a valid service name
 * @method isValidServiceName
 * @param {String} serviceName
 * @return {boolean}
 */
WSO2.MashupUtils.isValidServiceName = function(serviceName) {
    //Let's replace spaces in the name with '-'
    serviceName = WSO2.MashupUtils.replaceSpacesWith(serviceName, '-');
    //Trimming leading and trailing spaces
    serviceName = WSO2.MashupUtils.trim(serviceName);

    if (serviceName != "") {
        if (!(WSO2.MashupUtils.isXMLName(serviceName))) {
            return false;
        }
    }
    return true;
};


/**
 * @description Checks a string to see whether it complies with the XML naming conventions
 * @param {String} The string to be evaluated
 * @return {boolean} Returns true if the string is compatible with the XML naming conventions
 */
WSO2.MashupUtils.isXMLName = function (string)
{
    var SERVICE_INIT_CHAR =
            "\\u0041-\\u005a\\u0061-\\u007a\\u00aa\\u00b5\\u00ba\\u00c0-\\u00d6\\u00d8-\\u00f6\\u00f8-\\u021f\\" +
            "u0222-\\u0233\\u0250-\\u02ad\\u0386\\u0388-\\u038a\\u038c\\u038e-\\u03a1\\u03a3-\\u03ce\\u03d0-\\u03d7" +
            "\\u03da-\\u03f3\\u0400-\\u0481\\u048c-\\u04c4\\u04c7\\u04c8\\u04cb\\u04cc\\u04d0-\\u04f5\\u04f8\\u04f9\\" +
            "u0531-\\u0556\\u0561-\\u0587\\u05d0-\\u05ea\\u05f0-\\u05f2\\u0621-\\u063a\\u0641-\\u064a\\u0671-\\u06d3\\" +
            "u06d5\\u06fa-\\u06fc\\u0710\\u0712-\\u072c\\u0780-\\u07a5\\u0905-\\u0939\\u093d\\u0950\\u0958-\\u0961\\u0985-\\" +
            "u098c\\u098f\\u0990\\u0993-\\u09a8\\u09aa-\\u09b0\\u09b2\\u09b6-\\u09b9\\u09dc\\u09dd\\u09df-\\u09e1\\u09f0\\" +
            "u09f1\\u0a05-\\u0a0a\\u0a0f\\u0a10\\u0a13-\\u0a28\\u0a2a-\\u0a30\\u0a32\\u0a33\\u0a35\\u0a36\\u0a38\\u0a39\\" +
            "u0a59-\\u0a5c\\u0a5e\\u0a72-\\u0a74\\u0a85-\\u0a8b\\u0a8d\\u0a8f-\\u0a91\\u0a93-\\u0aa8\\u0aaa-\\u0ab0\\u0ab2\\" +
            "u0ab3\\u0ab5-\\u0ab9\\u0abd\\u0ad0\\u0ae0\\u0b05-\\u0b0c\\u0b0f\\u0b10\\u0b13-\\u0b28\\u0b2a-\\u0b30\\u0b32\\u0b33\\" +
            "u0b36-\\u0b39\\u0b3d\\u0b5c\\u0b5d\\u0b5f-\\u0b61\\u0b85-\\u0b8a\\u0b8e-\\u0b90\\u0b92-\\u0b95\\u0b99\\u0b9a\\u0b9c" +
            "\\u0b9e\\u0b9f\\u0ba3\\u0ba4\\u0ba8-\\u0baa\\u0bae-\\u0bb5\\u0bb7-\\u0bb9\\u0c05-\\u0c0c\\u0c0e-\\u0c10\\u0c12-\\" +
            "u0c28\\u0c2a-\\u0c33\\u0c35-\\u0c39\\u0c60\\u0c61\\u0c85-\\u0c8c\\u0c8e-\\u0c90\\u0c92-\\u0ca8\\u0caa-\\u0cb3\\" +
            "u0cb5-\\u0cb9\\u0cde\\u0ce0\\u0ce1\\u0d05-\\u0d0c\\u0d0e-\\u0d10\\u0d12-\\u0d28\\u0d2a-\\u0d39\\u0d60\\u0d61\\" +
            "u0d85-\\u0d96\\u0d9a-\\u0db1\\u0db3-\\u0dbb\\u0dbd\\u0dc0-\\u0dc6\\u0e01-\\u0e30\\u0e32\\u0e33\\u0e40-\\u0e45\\" +
            "u0e81\\u0e82\\u0e84\\u0e87\\u0e88\\u0e8a\\u0e8d\\u0e94-\\u0e97\\u0e99-\\u0e9f\\u0ea1-\\u0ea3\\u0ea5\\u0ea7\\u0eaa\\" +
            "u0eab\\u0ead-\\u0eb0\\u0eb2\\u0eb3\\u0ebd\\u0ec0-\\u0ec4\\u0edc\\u0edd\\u0f00\\u0f40-\\u0f47\\u0f49-\\u0f6a\\u0f88-\\" +
            "u0f8b\\u1000-\\u1021\\u1023-\\u1027\\u1029\\u102a\\u1050-\\u1055\\u10a0-\\u10c5\\u10d0-\\u10f6\\u1100-\\u1159\\u115f-\\" +
            "u11a2\\u11a8-\\u11f9\\u1200-\\u1206\\u1208-\\u1246\\u1248\\u124a-\\u124d\\u1250-\\u1256\\u1258\\u125a-\\u125d\\u1260-\\" +
            "u1286\\u1288\\u128a-\\u128d\\u1290-\\u12ae\\u12b0\\u12b2-\\u12b5\\u12b8-\\u12be\\u12c0\\u12c2-\\u12c5\\u12c8-\\u12ce\\" +
            "u12d0-\\u12d6\\u12d8-\\u12ee\\u12f0-\\u130e\\u1310\\u1312-\\u1315\\u1318-\\u131e\\u1320-\\u1346\\u1348-\\u135a\\u13a0-\\" +
            "u13f4\\u1401-\\u166c\\u166f-\\u1676\\u1681-\\u169a\\u16a0-\\u16ea\\u1780-\\u17b3\\u1820-\\u1842\\u1844-\\u1877\\u1880-\\" +
            "u18a8\\u1e00-\\u1e9b\\u1ea0-\\u1ef9\\u1f00-\\u1f15\\u1f18-\\u1f1d\\u1f20-\\u1f45\\u1f48-\\u1f4d\\u1f50-\\u1f57\\u1f59\\u1f5b" +
            "\\u1f5d\\u1f5f-\\u1f7d\\u1f80-\\u1fb4\\u1fb6-\\u1fbc\\u1fbe\\u1fc2-\\u1fc4\\u1fc6-\\u1fcc\\u1fd0-\\u1fd3\\u1fd6-\\u1fdb\\" +
            "u1fe0-\\u1fec\\u1ff2-\\u1ff4\\u1ff6-\\u1ffc\\u207f\\u2102\\u2107\\u210a-\\u2113\\u2115\\u2119-\\u211d\\u2124\\u2126\\u2128" +
            "\\u212a-\\u212d\\u212f-\\u2131\\u2133-\\u2139\\u3006\\u3041-\\u3094\\u30a1-\\u30fa\\u3105-\\u312c\\u3131-\\u318e\\u31a0-\\" +
            "u31b7\\u3400-\\u4db5\\u4e00-\\u9fa5\\ua000-\\ua48c\\uac00-\\ud7a3\\uf900-\\ufa2d\\ufb00-\\ufb06\\ufb13-\\ufb17\\ufb1d\\" +
            "ufb1f-\\ufb28\\ufb2a-\\ufb36\\ufb38-\\ufb3c\\ufb3e\\ufb40\\ufb41\\ufb43\\ufb44\\ufb46-\\ufbb1\\ufbd3-\\ufd3d\\ufd50-\\" +
            "ufd8f\\ufd92-\\ufdc7\\ufdf0-\\ufdfb\\ufe70-\\ufe72\\ufe74\\ufe76-\\ufefc\\uff21-\\uff3a\\uff41-\\uff5a\\uff66-\\uff6f\\" +
            "uff71-\\uff9d\\uffa0-\\uffbe\\uffc2-\\uffc7\\uffca-\\uffcf\\uffd2-\\uffd7\\uffda-\\uffdc";

    var SERVICE_REST_CHARS =
            "\\u0030-\\u0039\\u02b0-\\u02b8\\u02bb-\\u02c1\\u02d0\\u02d1\\u02e0-\\u02e4\\u02ee\\u0300-\\u034e\\u0360-\\u0362\\u037a\\" +
            "u0483-\\u0486\\u0488\\u0489\\u0559\\u0591-\\u05a1\\u05a3-\\u05b9\\u05bb-\\u05bd\\u05bf\\u05c1\\u05c2\\u05c4\\u0640\\u064b-\\" +
            "u0655\\u0660-\\u0669\\u0670\\u06d6-\\u06e8\\u06ea-\\u06ed\\u06f0-\\u06f9\\u0711\\u0730-\\u074a\\u07a6-\\u07b0\\u0901-\\u0903" +
            "\\u093c\\u093e-\\u094d\\u0951-\\u0954\\u0962\\u0963\\u0966-\\u096f\\u0981-\\u0983\\u09bc\\u09be-\\u09c4\\u09c7\\u09c8\\" +
            "u09cb-\\u09cd\\u09d7\\u09e2\\u09e3\\u09e6-\\u09ef\\u0a02\\u0a3c\\u0a3e-\\u0a42\\u0a47\\u0a48\\u0a4b-\\u0a4d\\u0a66-\\u0a71\\" +
            "u0a81-\\u0a83\\u0abc\\u0abe-\\u0ac5\\u0ac7-\\u0ac9\\u0acb-\\u0acd\\u0ae6-\\u0aef\\u0b01-\\u0b03\\u0b3c\\u0b3e-\\u0b43\\u0b47" +
            "\\u0b48\\u0b4b-\\u0b4d\\u0b56\\u0b57\\u0b66-\\u0b6f\\u0b82\\u0b83\\u0bbe-\\u0bc2\\u0bc6-\\u0bc8\\u0bca-\\u0bcd\\u0bd7\\u0be7-\\" +
            "u0bef\\u0c01-\\u0c03\\u0c3e-\\u0c44\\u0c46-\\u0c48\\u0c4a-\\u0c4d\\u0c55\\u0c56\\u0c66-\\u0c6f\\u0c82\\u0c83\\u0cbe-\\u0cc4\\" +
            "u0cc6-\\u0cc8\\u0cca-\\u0ccd\\u0cd5\\u0cd6\\u0ce6-\\u0cef\\u0d02\\u0d03\\u0d3e-\\u0d43\\u0d46-\\u0d48\\u0d4a-\\u0d4d\\u0d57\\" +
            "u0d66-\\u0d6f\\u0d82\\u0d83\\u0dca\\u0dcf-\\u0dd4\\u0dd6\\u0dd8-\\u0ddf\\u0df2\\u0df3\\u0e31\\u0e34-\\u0e3a\\u0e46-\\u0e4e\\" +
            "u0e50-\\u0e59\\u0eb1\\u0eb4-\\u0eb9\\u0ebb\\u0ebc\\u0ec6\\u0ec8-\\u0ecd\\u0ed0-\\u0ed9\\u0f18\\u0f19\\u0f20-\\u0f29\\u0f35\\" +
            "u0f37\\u0f39\\u0f3e\\u0f3f\\u0f71-\\u0f84\\u0f86\\u0f87\\u0f90-\\u0f97\\u0f99-\\u0fbc\\u0fc6\\u102c-\\u1032\\u1036-\\u1039\\" +
            "u1040-\\u1049\\u1056-\\u1059\\u1369-\\u1371\\u17b4-\\u17d3\\u17e0-\\u17e9\\u1810-\\u1819\\u1843\\u18a9\\u20d0-\\u20e3\\u3005\\" +
            "u302a-\\u302f\\u3031-\\u3035\\u3099\\u309a\\u309d\\u309e\\u30fc-\\u30fe\\ufb1e\\ufe20-\\ufe23\\uff10-\\uff19\\uff70\\uff9e\\uff9f";

    var isXMLName = new RegExp("^[" + SERVICE_INIT_CHAR + "].[" + SERVICE_REST_CHARS + "]*");

    return isXMLName.test(string);
}


/**
 * @description Substitutes internal spaces in a string with a given character
 * @method replaceSpacesWith
 * @param {String} candidateString
 * @param {String} substituteCharacter
 * @return {String} The character substituted version of the string
 */
WSO2.MashupUtils.replaceSpacesWith = function(candidateString, substituteCharacter) {

    var regExp = /\s+/g;
    return candidateString.replace(regExp, substituteCharacter);

};


/**
 * @description Removes CDATA tags from a string
 * @method removeCdata
 * @param {String} candidateString
 * @return {String} The string stripped of CDATA tags
 */
WSO2.MashupUtils.removeCdata = function (candidateString) {

    //Verify whether this is a CDATA string
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

    return candidateString;

};


/**
 * @description Shows/Hides a displayed model dialog. Used during a system initiated logout to restore dialog state.
 * @method changeModelDialogStatus
 * @param {boolean} isHidden
 */
WSO2.MashupUtils.changeModelDialogStatus = function (isHidden) {
    if ((isHidden) && (WSO2.MashupUtils.currentActiveModelDialog != null)) {
        WSO2.MashupUtils.currentActiveModelDialog.cancel();
    } else if ((!isHidden) && (WSO2.MashupUtils.currentActiveModelDialog != null)) {
        WSO2.MashupUtils.currentActiveModelDialog.show();
    }
};


/**
 * @description Creates the Mashup Downloading dialog
 * @method showMashupDownloadingDialog
 * @param {String} serviceName The service to be downloaded
 */
WSO2.MashupUtils.showMashupDownloadingDialog = function (serviceName) {

    //Creating the form content
    var downloadForm = '<form method="post" style="display:inline;" id="downloadForm" name="downloadForm" action="' +
                       SERVICE_PATH + '/ServiceSharingHelper/downloadService">' +
                       '<input size="10" id="txtServiceName" name="serviceName" type="hidden" value="' +
                       serviceName + '">' +
                       '<table width="100%"  border="0" cellspacing="0" cellpadding="0">' +
                       '<tr><td nowrap height="25"><b>Your Mashup Server User Name: </b></td><td><input size="40" name="username" id="txtUserName" type="text"></td></tr>' +
                       '<tr><td nowrap height="25"><b>Your Mashup Server Password: </b></td><td><input size="40" name="password" type="password"></td></tr>' +
                       '<tr><td nowrap height="25"><b>Your Mashup Server Address: </b></td><td><input size="40" name="remoteServer" type="text" value="https://localhost:7443/"></td></tr>' +
                       '<tr><td height="30" valign="bottom"><input type="button" value="Download" onclick="WSO2.MashupUtils.makeMashupDownloadRequest();"/></td><td>&nbsp;</td></tr>' +
                       '</table>' +
                       '</form>';

    WSO2.MashupUtils.dialog("Download " + serviceName, downloadForm, 500, 100, 'txtUserName');

};


WSO2.MashupUtils.makeMashupDownloadRequest = function() {
    function handleSuccess(o) {
        WSO2.MashupUtils.dialog("Done", "The mashup was downloaded successfully.", 400, 100);
    }

    function handleFailure(o) {
        var responseText = o.responseText.split("at")[0];
        WSO2.MashupUtils.dialog("Error", "Failed to download the mashup.<br><br>" +
                                         responseText, 400, 100);
    }

    var callback =
    {
        success:handleSuccess,
        failure:handleFailure
    };

    YAHOO.util.Connect.setForm("downloadForm");
    YAHOO.util.Connect.asyncRequest('POST', SERVICE_PATH +
                                            '/ServiceSharingHelper/downloadService', callback, null);

    WSO2.MashupUtils.dialog.close();
}


/**
 * @description Takes in any URL and replaces its host with the given string.
 */
WSO2.MashupUtils.replaceHost = function(url, newHostName) {
    //Splitting the url
    var splitUrlArray = url.split("/");
    var newUrl = "";
    for (var x = 0; x < splitUrlArray.length; x++) {
        if (x == 0) {
            newUrl = splitUrlArray[x];
        } else if (x == 2) {
            //Checking whether there is a port
            var currentHost = splitUrlArray[x];
            var splitHostArray = currentHost.split(":");
            if (splitHostArray.length > 1) {
                newUrl = newUrl + "/" + newHostName + ":" + splitHostArray[1];
            } else {
                newUrl = newUrl + "/" + newHostName;
            }
        } else {
            newUrl = newUrl + "/" + splitUrlArray[x];
        }

    }

    return newUrl;
};

/**
 * @description Converts and HTML string to a Doc fragment
 * @param HTMLstring
 */
WSO2.MashupUtils.toDOM = function (HTMLstring)
{
    var d = document.createElement('div');
    d.innerHTML = HTMLstring;
    var docFrag = document.createDocumentFragment();

    while (d.firstChild) {
        docFrag.appendChild(d.firstChild)
    }
    ;

    return docFrag;
};


WSO2.MashupUtils.readCookie = function(name) {
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
    }
    return null;
};


WSO2.MashupUtils.eraseCookie = function(name) {
    WSO2.MashupUtils.createCookie(name, "", -1);
}


WSO2.MashupUtils.createCookie = function(name, value, days) {
    var expires = "";

    if (days) {
        var date = new Date();
        date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
        expires = "; expires=" + date.toGMTString();
    }

    document.cookie = name + "=" + value + expires + "; path=/";
};


/**
 * @description Handles the change to a service status. Updates the mashup list model, which propagates to the view.
 * @method handleServiceStartStop
 */
WSO2.MashupUtils.handleServiceStartStop = function(serviceName, serviceState, callback) {
    var params = new Array();
    params[0] = serviceName;
    params[1] = serviceState;

    wso2.mashup.services.activationOfService(serviceName, serviceState, callback, params);
};

WSO2.MashupUtils.dialog = function(strTitle, strBody, width, height, elementToFocusAfterLoading) {
    if (document.getElementById('lyrDialog') == null) {
        var pageBody = document.getElementsByTagName('BODY')[0];

        var x = (document.body.clientWidth - width) / 2;
        var y = (document.body.clientHeight - height) / 2;
        x = (x > 0) ? x : 0;
        y = (y > 0) ? y : 0;

        //Creating the dialog div
        var dialogContent = document.createElement('div');
        dialogContent.id = "lyrDialog";
        dialogContent.style.position = 'absolute';
        dialogContent.style.left = x + 'px';
        dialogContent.style.top = y + 'px';
        dialogContent.style.width = width + 'px';
        dialogContent.style.zIndex = 300;

        dialogContent.innerHTML +=
        '<table width="100%"  border="0" height="' + height +
        '" cellspacing="0" cellpadding="0">' +
        '<tr height="24">' +
        '<td height="24" ><table width="100%" height="24" border="0" cellspacing="0" cellpadding="0">' +
        '<tr height="24">' +
        '<td width="18" height="24"><img src="images/w_small_top_left_edit.gif" width="18" height="24"></td>' +
        '<td class="diaLogTitle" height="24" id="d_Title">' + strTitle + '</td>' +
        '<td width="17" height="24" class="diaLogTitle"><img src="images/w_top_close.gif" style="cursor: pointer;" onclick="WSO2.MashupUtils.dialog.close()" width="17" height="16"></td>' +
        '<td width="9" height="24"><img src="images/w_top_right_edit.gif" width="9" height="24"></td>' +
        '</tr>' +
        '</table></td>' +
        '</tr>' +
        '<tr>' +
        '<td id="d_Body" style="padding:15px; text-align:left;" valign="top">' + strBody + '</td>' +
        '</tr>' +
        '</table>';

        //Creating the mask div
        var dialogMask = document.createElement('div');
        dialogMask.id = 'dialogMask';

        pageBody.appendChild(dialogMask);
        dialogMask.style.display = "block";

        pageBody.appendChild(dialogContent);

        YAHOO.util.Event.onContentReady('doc3', function() {
            if (elementToFocusAfterLoading != null) {
                document.getElementById(elementToFocusAfterLoading).focus();
            }
        });

    } else {
        // Closing the existing dialog and re-buildinga a new one
        WSO2.MashupUtils.dialog.close();
        WSO2.MashupUtils.dialog(strTitle, strBody, width, height, elementToFocusAfterLoading);
    }
};

WSO2.MashupUtils.dialog.close = function() {
    //Cleaning up the dom
    document.getElementById('lyrDialog').parentNode.removeChild(document.getElementById('lyrDialog'));
    document.getElementById('dialogMask').parentNode.removeChild(document.getElementById('dialogMask'));
}