// create a Address or a WSDL Endpoint form with given ID
function createAddressOrWSDLEndpointForm(newID, type) {
    var formID = 'form_' + newID;
    YAHOO.util.Event.onAvailable("info", function() {

        var formatOptions = [
            ["SelectAValue","Leave As-Is"],
            ["soap11","SOAP 1. 1"],
            ["soap12","SOAP 1.2"],
            ["pox","Plain Old XML (POX)"],
            ["get","REpresentational State Transfer (REST/GET)"]
        ];

        var optimizeOptions = [
            ["SelectAValue","Leave As-Is"],
            ["swa","SwA"],
            ["mtom","MTOM"]
        ];

        var timeoutActionOptions = [
            ["SelectAValue","Never timeout"],
            ["discard","Discard message"],
            ["fault","Execute fault sequence"]
        ];
        var i;
        var endpointName = '';
        var endpointAddress = '';
        var formatSelectedOption = '';
        var optimizeSelectedOption = '';

        var errorCode = '';
        var suspendDurationOnFailure = '';
        var maxDuration = '';
        var factor = '1.0'; // default value

        var timeOutErrorCode = '';
        var retryTimeOut = '0'; // A retryTime out is a MUST
        var retryDelayTimeOut = '0'; // A retryDelay is a MUST 

        var actionDuration = '0'; // Initilized to zero

        var enableAddressing = null;
        var sepListener = null;

        var enableWSSec = null;
        var secPolicy = '';

        var enableRM = null;
        var rmPolicy = '';

        var tableDiv = document.createElement('div');
        tableDiv.style.display = 'none';
        tableDiv.id = formID;

        var endpointNode = getNodeById(newID);
        var addressOrWSDLNode = endpointNode.childNodes[0]; // first child of the child given by ID
        var addressOrWSDLEpChildNodes = null;

        if (addressOrWSDLNode.hasChildNodes()) {
            addressOrWSDLEpChildNodes = addressOrWSDLNode.childNodes;
        }

        // the endpoint HTML
        var epInnerHTML = '';
        var epHerader = '';

        // hold the HTML of the second table
        var epInnerHTMLT2 = '';

        if (type == 'address') {
            epHerader = 'Address Endpoint';
        } else if (type == 'wsdl') {
            epHerader = 'WSDL Endpoint';
        }

        if (endpointNode.getAttribute('name') != null && endpointNode.getAttribute('name') != undefined) {
            endpointName = endpointNode.getAttribute('name');
        }
        epInnerHTML += '<table class="normal-nopadding" width="100%">' +
                       '<tbody>' +
                       '<tr><td colspan="2"><h2> ' + epHerader + '</h2></td></tr>' +
                       '<tr>' +
                           '<td class="leftCol-small">' + jsi18n['endpointName'] + '</td>' +
                           '<td><input id="' + formID + '_name"' + 'name="' + formID + '_name"' + 'type="text" size="50" value="' + endpointName + '"/>' +
                           '</td>' +
                       '</tr>';
        if (type == 'address') {
            // address
            /*
             <address uri="endpoint address" [format="soap11|soap12|pox|get"] [optimize="mtom|swa"]
             [encoding="charset encoding"]
             [statistics="enable|disable"] [trace="enable|disable"]>
             * */
            if (addressOrWSDLNode.getAttribute('uri') != null || addressOrWSDLNode.getAttribute('uri') != undefined) {
                endpointAddress = addressOrWSDLNode.getAttribute('uri');
            }

            epInnerHTML += '<tr>' +
                           '<td class="leftCol-small">' + jsi18n['address'] + '<span class="required">*</span></td>' +
                           '<td><input id="' + formID + '_address"' + 'name="' + formID + '_address"' + 'type="text" size="50" value="' + endpointAddress + '"/>' +
                //                           '<input id="' + formID + '_testAddress"' + 'name="' + formID + '_testAddress"' + 'type="button" class="button" onclick="testURL(\'' + document.getElementById(formID + '_address').value + '\')" value="' + jsi18n['test.url'] + ' "/>' +
                           '</td>' +
                           '</tr>';

            epInnerHTML += '<tr>' +
                           '<td colspan="2"><span id="' + formID + '_adv" style="float: left; position: relative;"> <a class="icon-link"  onclick="javascript:showAdvancedOptions(\'' + formID + '\');"' +
                           'style="background-image: url(images/down.gif);">' + jsi18n['show.advanced.options'] + '</a></span></td></tr>';

            // end the first table
            epInnerHTML += '</tbody></table>';

            // start of second table
            epInnerHTML += '<table id="' + formID + '_advancedForm" style="display:none"  class="normal-nopadding">' +
                           '<tbody>';

            epInnerHTML += '<tr>' +
                           '<td colspan="2" class="sub-header">' + jsi18n['message.content'] + '</td>' +
                           '</tr>';

            // format
            var tempDivNode = document.createElement('div');
            var formatSelect = document.createElement('select');
            var formatOption = '';

            formatSelect.setAttribute('id', formID + '_format');
            if (addressOrWSDLNode.getAttribute('format')) {
                formatSelectedOption = addressOrWSDLNode.getAttribute('format');
            } else {
                formatSelectedOption = 'SelectAValue';
            }
            for (i = 0; i < formatOptions.length; i++) {
                formatOption = document.createElement('option');
                formatOption.innerHTML = formatOptions[i][1];
                if (formatSelectedOption == formatOptions[i][0]) {
                    formatOption.setAttribute('selected', 'selected');
                }
                formatOption.value = formatOptions[i][0];
                formatSelect.appendChild(formatOption);
            }
            tempDivNode.appendChild(formatSelect);

            epInnerHTML += '<tr>' +
                           '<td>' + jsi18n['format'] + '</td>' +
                           '<td>' + tempDivNode.innerHTML + '</td>' +
                           '</tr>';

            // optimize

            tempDivNode = document.createElement('div');
            var optimizeSelect = document.createElement('select');
            var optimizeOption = '';

            optimizeSelect.setAttribute('id', formID + '_optimize');
            if (addressOrWSDLNode.getAttribute('optimize')) {
                optimizeSelectedOption = addressOrWSDLNode.getAttribute('optimize');
            } else {
                optimizeSelectedOption = 'SelectAValue';
            }
            for (i = 0; i < optimizeOptions.length; i++) {
                optimizeOption = document.createElement('option');
                optimizeOption.innerHTML = optimizeOptions[i][1];
                if (optimizeSelectedOption == optimizeOptions[i][0]) {
                    optimizeOption.setAttribute('selected', 'selected');
                }
                optimizeOption.value = optimizeOptions[i][0];
                optimizeSelect.appendChild(optimizeOption);
            }
            tempDivNode.appendChild(optimizeSelect);

            epInnerHTML += '<tr>' +
                           '<td>' + jsi18n['optimize'] + '</td>' +
                           '<td>' + tempDivNode.innerHTML + '</td>' +
                           '</tr>';
        }
        else if (type == 'wsdl') {
            var inlineWSDL = '';
            var inlineWSDLText = '';
            var isInLineWSDL = false; // assume default is inLine
            var uriWSDL = '';
            var specifyAsText = '';
            var URIText = ''; // specify the URI or the inLined text
            /* <wsdl [uri="wsdl-uri"] service="qname" port/endpoint="qname">
             */
            // TODO handle FF2 and FF3 seprately ?
            inlineWSDL = addressOrWSDLNode.getElementsByTagName('wsdl:definitions');
            if (inlineWSDL.length == 0) {
                inlineWSDL = addressOrWSDLNode.getElementsByTagName('definitions'); // TODO - does the synapse configuration allow this ?
            }
            if (inlineWSDL.length == 0) {
                inlineWSDL = addressOrWSDLNode.getElementsByTagName('wsdl20:description');
            }
            if (inlineWSDL.length > 0) {
                isInLineWSDL = true;
                inlineWSDLText = xmlToString(inlineWSDL[0]);
            } else {
                uriWSDL = addressOrWSDLNode.getAttribute('uri');
                if (uriWSDL == null) {
                    uriWSDL = '';
                }
            }

            if (isInLineWSDL) {
                specifyAsText = '<input type="radio" id="' + formID + '_inlineWSDL"' + 'name="' + formID + '_inlineWSDL"' + 'value="inlineWSDL" ' +
                                ' onclick="showinLineWSDLArea(\'' + formID + '\')" checked="checked" /> In-lined WSDL ' +
                                '<input type="radio" id="' + formID + '_uriWSDL" name="' + formID + '_inlineWSDL"' + ' value="uriWSDL" ' +
                                ' onclick="showWSDLURI(\'' + formID + '\')"/> URI';

                URIText = '<tr id="' + formID + '_inLineWSDLID">' +
                          '<td class="leftCol-small"></td>' +
                          '<td><textarea id="' + formID + '_inlineWSDLVal" name="' + formID + '_inlineWSDLVal" rows="10" ' +
                          'cols="50">' + inlineWSDLText + '</textarea> ' +
                          '</td></tr>';

                URIText += '<tr id="' + formID + '_wsdlURIID" style="display:none" >' +
                           '<td class="leftCol-small">WSDL URI <span class="required">*</span></td> ' +
                           '<td> ' +
                           '<input type="text" id="' + formID + '_uriWSDLVal" name="' + formID + '_uriWSDLVal"' +
                           'value="' + uriWSDL + '">' +
                           '</td>' +
                           '</tr>';
            } else {
                specifyAsText = '<input type="radio" id="' + formID + '_inlineWSDL"' + 'name="' + formID + '_inlineWSDL"' + 'value="inlineWSDL" ' +
                                ' onclick="showinLineWSDLArea(\'' + formID + '\')"  /> In-lined WSDL ' +
                                '<input type="radio" id="' + formID + '_uriWSDL" name="' + formID + '_inlineWSDL"' + ' value="uriWSDL" ' +
                                ' onclick="showWSDLURI(\'' + formID + '\')"  checked="checked"/> URI';

                URIText = '<tr id="' + formID + '_inLineWSDLID" style="display:none">' +
                          '<td class="leftCol-small"></td>' +
                          '<td><textarea id="' + formID + '_inlineWSDLVal" name="' + formID + '_inlineWSDLVal" rows="10" ' +
                          'cols="50">' + inlineWSDLText + '</textarea> ' +
                          '</td></tr>';

                URIText += '<tr id="' + formID + '_wsdlURIID">' +
                           '<td class="leftCol-small">WSDL URI <span class="required">*</span></td> ' +
                           '<td> ' +
                           '<input type="text" id="' + formID + '_uriWSDLVal" name="' + formID + '_uriWSDLVal"' +
                           'value="' + uriWSDL + '">' +
                           '</td>' +
                           '</tr>';
            }

            epInnerHTML += '<tr>' +
                           '<td class="leftCol-small">Specify As</td>' +
                           '<td>' + specifyAsText + '</td>' +
                           '</tr>';

            epInnerHTML += URIText;

            // service
            var service = '';
            if (addressOrWSDLNode.getAttribute('service')) {
                service = addressOrWSDLNode.getAttribute('service');
            }
            epInnerHTML += '<tr>' +
                           '<td class="leftCol-small">' + jsi18n['service'] + ' <span class="required">*</span></td>' +
                           '<td><input id="' + formID + '_service"' + 'name="' + formID + '_service"' + 'type="text" value="' + service + '"/></td>' +
                           '</tr>';

            // port
            var port = '';
            if (addressOrWSDLNode.getAttribute('port')) {
                port = addressOrWSDLNode.getAttribute('port');
            }
            epInnerHTML += '<tr>' +
                           '<td class="leftCol-small">' + jsi18n['port'] + ' <span class="required">*</span></td>' +
                           '<td><input id="' + formID + '_port"' + 'name="' + formID + '_port"' + 'type="text" value="' + port + '"/></td>' +
                           '</tr>';

            epInnerHTML += '<tr>' +
                           '<td colspan="2"><span id="' + formID + '_adv" style="float: left; position: relative;"> <a class="icon-link"  onclick="javascript:showAdvancedOptions(\'' + formID + '\');"' +
                           'style="background-image: url(images/down.gif);">' + jsi18n['show.advanced.options'] + '</a></span></td></tr>';
            // end the first table
            epInnerHTML += '</tbody></table>';

            // start of second table
            epInnerHTML += '<table id="' + formID + '_advancedForm" style="display:none" class="normal-nopadding">' +
                           '<tbody>';

        }

        // suspend header
        epInnerHTML += '<tr>' +
                       '<td colspan="2" class="sub-header">' + jsi18n['suspend'] + '</td>' +
                       '</tr>';
        /*
         * <suspendOnFailure>
         [<errorCodes>xxx,yyy</errorCodes>]
         <initialDuration>n</initialDuration>
         <progressionFactor>r</progressionFactor>
         <maximumDuration>l</maximumDuration>
         </suspendOnFailure>
         */

        // error codes
        if (addressOrWSDLNode.getElementsByTagName('suspendOnFailure').length > 0) {
            var errorCodeChild = addressOrWSDLNode.getElementsByTagName('suspendOnFailure')[0].childNodes;
            for (i = 0; i < errorCodeChild.length; i ++) {
                if (errorCodeChild[i].nodeName == 'errorCodes') {
                    errorCode = errorCodeChild[i].firstChild.nodeValue;
                }
            }
        }
        epInnerHTML += '<tr>' +
                       '<td>' + jsi18n['error.codes'] + '</td>' +
                       '<td><input type="text" id="' + formID + '_errorCodes"' + 'value="' + errorCode + '"/></td>' +
                       '</tr>';

        // intial duration
        if (addressOrWSDLNode.getElementsByTagName('initialDuration').length > 0) {
            suspendDurationOnFailure = addressOrWSDLNode.getElementsByTagName('initialDuration')[0].firstChild.nodeValue;
        }
        epInnerHTML += '<tr>' +
                       '<td>' + jsi18n['intial.duration.millis'] + '</td>' +
                       '<td><input type="text" id="' + formID + '_suspend"' + 'value="' + suspendDurationOnFailure + '"/></td>' +
                       '</tr>';

        // max duration
        if (addressOrWSDLNode.getElementsByTagName('maximumDuration').length > 0) {
            maxDuration = addressOrWSDLNode.getElementsByTagName('maximumDuration')[0].firstChild.nodeValue;
        }
        epInnerHTML += '<tr>' +
                       '<td>' + jsi18n['max.duration.millis'] + '</td>' +
                       '<td><input type="text" id="' + formID + '_maxDur"' + 'value="' + maxDuration + '"/></td>' +
                       '</tr>';

        // factor
        if (addressOrWSDLNode.getElementsByTagName('progressionFactor').length > 0) {
            factor = addressOrWSDLNode.getElementsByTagName('progressionFactor')[0].firstChild.nodeValue;
        }
        epInnerHTML += '<tr>' +
                       '<td>' + jsi18n['factor'] + '</td>' +
                       '<td><input type="text" id="' + formID + '_factor"' + 'value="' + factor + '"/></td>' +
                       '</tr>';

        // retry time out  header
        epInnerHTML += '<tr>' +
                       '<td colspan="2" class="sub-header">' + jsi18n['on.timedout'] + '</td>' +
                       '</tr>';
        /*
         * <markForSuspension>
         [<errorCodes>xxx,yyy</errorCodes>]
         <retriesBeforeSuspension>m</retriesBeforeSuspension>
         <retryDelay>d</retryDelay>
         </markForSuspension>

         */

        // retry time out  error code
        if (addressOrWSDLNode.getElementsByTagName('markForSuspension').length > 0) {
            var timeOutErrorCodeChild = addressOrWSDLNode.getElementsByTagName('markForSuspension')[0].childNodes;
            for (i = 0; i < timeOutErrorCodeChild.length; i++) {
                if (timeOutErrorCodeChild[i].nodeName == 'errorCodes') {
                    timeOutErrorCode = timeOutErrorCodeChild[i].firstChild.nodeValue;
                }
            }
        }
        epInnerHTML += '<tr>' +
                       '<td>' + jsi18n['error.codes'] + '</td>' +
                       '<td><input type="text" id="' + formID + '_timeoutErrorCodes"' + 'value="' + timeOutErrorCode + '"/></td>' +
                       '</tr>';


        // retry
        if (addressOrWSDLNode.getElementsByTagName('retriesBeforeSuspension').length > 0) {
            retryTimeOut = addressOrWSDLNode.getElementsByTagName('retriesBeforeSuspension')[0].firstChild.nodeValue;
        }
        epInnerHTML += '<tr>' +
                       '<td>' + jsi18n['retry'] + '</td>' +
                       '<td><input type="text" id="' + formID + '_retry"' + 'value="' + retryTimeOut + '"/></td>' +
                       '</tr>';

        // retry delay
        if (addressOrWSDLNode.getElementsByTagName('retryDelay').length > 0) {
            retryDelayTimeOut = addressOrWSDLNode.getElementsByTagName('retryDelay')[0].firstChild.nodeValue;
        }
        epInnerHTML += '<tr>' +
                       '<td>' + jsi18n['retry.delay.millis'] + '</td>' +
                       '<td><input type="text" id="' + formID + '_retryDelay"' + 'value="' + retryDelayTimeOut + '"/></td>' +
                       '</tr>';


        // time out header
        epInnerHTML += '<tr>' +
                       '<td colspan="2" class="sub-header">' + jsi18n['timeout'] + '</td>' +
                       '</tr>';

        /*<timeout>
         <action>discard|fault</action>
         <duration>timeout-duration</duration>
         </timeout>?
         */

        // action duaration
        var actionChildNode = null;

        // extracting the child nodes of timeout node
        if (addressOrWSDLEpChildNodes != null) {
            for (i = 0; i < addressOrWSDLEpChildNodes.length; i++) {
                if (addressOrWSDLEpChildNodes[i].nodeName == 'timeout') {
                    actionChildNode = addressOrWSDLEpChildNodes[i];
                }
            }
        }
        // action
        var actionOptionSelected = 'SelectAValue';
        var actionOption = '';
        var actionSelect = document.createElement('select');
        var timeOutChildNode;

        /* TODO , the synpase configuaration for timeout reveled that it can have one or more timeout elements
         here we haven't addressed that since the UI only support one option
         * */
        if (addressOrWSDLNode.getElementsByTagName('timeout').length > 0) {
            timeOutChildNode = addressOrWSDLNode.getElementsByTagName('timeout')[0].childNodes;
            for (i = 0; i < timeOutChildNode.length; i++) {
                if (timeOutChildNode[i].nodeName == 'action') {
                    actionOptionSelected = timeOutChildNode[i].firstChild.nodeValue;
                } else if (timeOutChildNode[i].nodeName == 'duration') {
                    actionDuration = timeOutChildNode[i].firstChild.nodeValue;
                }
            }
        }

        tempDivNode = document.createElement('div');

        for (i = 0; i < timeoutActionOptions.length; i++) {
            actionOption = document.createElement('option');
            actionOption.innerHTML = timeoutActionOptions[i][1];
            actionOption.value = timeoutActionOptions[i][0];
            if (actionOptionSelected == timeoutActionOptions[i][0]) {
                actionOption.setAttribute('selected', 'selected');
            }
            actionSelect.appendChild(actionOption);
        }
        actionSelect.setAttribute('id', formID + '_timeoutAction');
        actionSelect.setAttribute('onchange', 'activateDuration(this, \'' + formID + '\')');
        tempDivNode.appendChild(actionSelect);
        epInnerHTML += '<tr>' +
                       '<td>Action</td>' +
                       '<td>' + tempDivNode.innerHTML + '</td>' +
                       '</tr>';

        var actionOptionText = '';
        if (actionOptionSelected == 'SelectAValue') {
            actionOptionText = '<input type="text" id="' + formID + '_duration"' + 'name="' + formID + '_duration" disabled="disabled"  />';
        } else {
            actionOptionText = '<input type="text" value="' + actionDuration + '" id="' + formID + '_duration"' + 'name="' + formID + '_duration" />';
        }

        epInnerHTML += '<tr>' +
                       '<td>' + jsi18n['duration.millis'] + '</td>' +
                       '<td>' + actionOptionText + '</td>' +
                       '</tr>';


        // QoS header
        epInnerHTML += '<tr><td colspan="2" class="sub-header">' + jsi18n['qos'] + '</td></tr>';

        // WS-Addressing
        if (addressOrWSDLNode.getElementsByTagName('enableAddressing').length > 0) {
            enableAddressing = 'checked';
            if (addressOrWSDLNode.getElementsByTagName('enableAddressing')[0].hasAttribute('separateListener')) {
                sepListener = 'checked';
            }
        }
        var WSAddressingText = '';
        if (enableAddressing == 'checked') {
            WSAddressingText = '<input type="checkbox" ';
            WSAddressingText += 'onclick="showHideOnSelect(\''+formID+'_wsAddressing\',\''+formID+'_tr_separate_listener\')" ';
            WSAddressingText += ' checked="checked" id="' + formID + '_wsAddressing"' + 'name="' + formID + '_wsAddressing" />';
        } else if (enableAddressing == null) {
            WSAddressingText = '<input type="checkbox" ';
            WSAddressingText += 'onclick="showHideOnSelect(\''+formID+'_wsAddressing\',\''+formID+'_tr_separate_listener\')" ';
            WSAddressingText +='id="' + formID + '_wsAddressing"' + 'name="' + formID + '_wsAddressing" />';
        }
        epInnerHTML += '<tr>' +
                       '<td class="leftCol-small">' + jsi18n['ws.addressing'] + '</td>' +
                       '<td>' + WSAddressingText + '</td>' +
                       '</tr>';
        // use seprate listner
        var useSerateListnerText;
        if (sepListener == 'checked') {
            useSerateListnerText = '<input type="checkbox" checked="checked" id="' + formID + '_separeteListener"' + 'name="' + formID + '_separeteListener" />';
        } else if (sepListener == null) {
            useSerateListnerText = '<input type="checkbox" id="' + formID + '_separeteListener"' + 'name="' + formID + '_separeteListener" />';
        }
        epInnerHTML += '<tr id="'+formID+'_tr_separate_listener" ';
        if(enableAddressing == null){
            epInnerHTML += 'style="display:none"';
        }
        epInnerHTML += '>' +
                       '<td><div class="indented">' + jsi18n['seperate.listener'] + '</div></td>' +
                       '<td>' + useSerateListnerText + '</td>' +
                       '</tr>';

        // WS-Security
        /* <enableSec [policy="key"]/>?
         * */
        var WSSecurityText = '';
        var WSSecurityPolicyText = '';
        if (addressOrWSDLNode.getElementsByTagName('enableSec').length > 0) {
            enableWSSec = 'checked';
            if (enableWSSec == 'checked') {
                var childSecNode = addressOrWSDLNode.getElementsByTagName('enableSec')[0];
                if (childSecNode != null) {
                    if (childSecNode.hasAttribute('policy')) {
                        secPolicy = childSecNode.getAttribute('policy');
                    }
                }
            }
        }
        if (enableWSSec == 'checked') {
            WSSecurityText = '<input type="checkbox" ';
            WSSecurityText += 'onclick="showHideOnSelect(\''+formID+'_wssecurity\',\''+formID+'_tr_wssecurity\')" ';
            WSSecurityText += 'checked="checked" id="' + formID + '_wssecurity"' + 'name="' + formID + '_wssecurity" />';
        } else {
            WSSecurityText = '<input type="checkbox" ';
            WSSecurityText += 'onclick="showHideOnSelect(\''+formID+'_wssecurity\',\''+formID+'_tr_wssecurity\')" ';
            WSSecurityText +='id="' + formID + '_wssecurity"' + 'name="' + formID + '_wssecurity" />';
        }
        WSSecurityPolicyText = '<input class="longInput" type="text" id="' + formID + '_wssecurityPolicy" ' +
                               ' name="' + formID + '_wssecurityPolicy" ' +
                               ' value="' + secPolicy + '" readonly="true"/> ';


        var registryBrowser = '<a href="#registryBrowserLink" ' +
                              ' class="registry-picker-icon-link" ' +
                              ' style="padding-left:20px;padding-right:20px;" ' +
                              ' onclick="showRegistryBrowser(\'' + formID + '_wssecurityPolicy' + '\',\'/_system/config\')">' +
                              'Configuration Registry</a>'+
                              '<a href="#registryBrowserLink" ' +
                              ' class="registry-picker-icon-link" ' +
                              ' style="padding-left:20px;" ' +
                              ' onclick="showRegistryBrowser(\'' + formID + '_wssecurityPolicy' + '\',\'/_system/governance\')">' +
                              'Governance Registry</a>';
        epInnerHTML += '<tr>' +
                       '<td class="leftCol-small">' + jsi18n['ws.security'] + '</td>' +
                       '<td>' + WSSecurityText + '</td>' +
                       '</tr>';

        epInnerHTML += '<tr id="' + formID + '_tr_wssecurity" ';
        if (enableWSSec == null) {
            epInnerHTML += 'style="display:none"';
        }
        epInnerHTML += '>' +
                '<td><div class="indented">' + jsi18n['policy.key'] + '</div></td>' +
                '<td><table class="normal"><tr><td>' + WSSecurityPolicyText + '</td><td>' + registryBrowser + '</td></tr></table></td>' +
                '</tr>';


        // WS-RM
        /* <enableRM [policy="key"]/>?*/
        var WSRMText = '';
        var WSRMPolicyText = '';
        if (addressOrWSDLNode.getElementsByTagName('enableRM').length > 0) {
            enableRM = 'checked';
            if (enableRM == 'checked') {
                var childRMNode = addressOrWSDLNode.getElementsByTagName('enableRM')[0];
                if (childRMNode != null) {
                    if (childRMNode.hasAttribute('policy')) {
                        rmPolicy = childRMNode.getAttribute('policy');
                    }
                }
            }
        }
        if (enableRM == 'checked') {
            WSRMText = '<input type="checkbox" ';
            WSRMText += 'onclick="showHideOnSelect(\''+formID+'_wsRM\',\''+formID+'_tr_wsRM\')" ';;
            WSRMText +='checked="checked" id="' + formID + '_wsRM"' + 'name="' + formID + '_wsRM" />';
        } else {
            WSRMText = '<input type="checkbox" ';
            WSRMText += 'onclick="showHideOnSelect(\''+formID+'_wsRM\',\''+formID+'_tr_wsRM\')" ';;
            WSRMText += ' id="' + formID + '_wsRM"' + 'name="' + formID + '_wsRM" />';
        }
        WSRMPolicyText = '<input class="longInput" type="text" id="' + formID + '_wsrmPolicy" ' +
                         ' name="' + formID + '_wsrmPolicy" ' +
                         ' value="' + rmPolicy + '" readonly="true"/> ' ;

        registryBrowser = '<a href="#registryBrowserLink" ' +
                          ' class="registry-picker-icon-link" ' +
                          ' style="padding-left:20px;padding-right:20px;" ' +
                          ' onclick="showRegistryBrowser(\'' + formID + '_wsrmPolicy' +  '\',\'/_system/config\')">' +
                          'Configuration Registry</a>'+
                          '<a href="#registryBrowserLink" ' +
                          ' class="registry-picker-icon-link" ' +
                          ' style="padding-left:20px;\" ' +
                          ' onclick="showRegistryBrowser(\'' + formID + '_wsrmPolicy' +  '\',\'/_system/governance\')">' +
                          'Governance Registry</a>';

        epInnerHTML += '<tr>' +
                       '<td class="leftCol-small">' + jsi18n['ws.rm'] + '</td>' +
                       '<td>' + WSRMText + '</td>' +
                       '</tr>';

        epInnerHTML += '<tr  id="' + formID + '_tr_wsRM" ';
        if (enableRM == null) {
            epInnerHTML += 'style="display:none"';
        }
        epInnerHTML += '>' +
                '<td><div class="indented">' + jsi18n['policy.key'] + '</div></td>' +
                '<td><table class="normal"><tr><td>' + WSRMPolicyText + '</td><td>' + registryBrowser + '</td></tr></table></td>' +
                '</tr>';

        // end the HTML
        epInnerHTML += '</tbody></table>';

        //property table
        epInnerHTML += '<table class="normal-nopadding">' +
                '<tbody><tr><td colspan="2" class="sub-header">Endpoint Properties</td></tr>' +
                '<tr>' +
                '<td colspan="2">' +
                '<a href="#" onclick="addServiceParams(\'' + formID + '_headerTable\')" style="background-image: url(\'../admin/images/add.gif\');" class="icon-link">Add Property' +
                '</a><input type="hidden" name="endpointProperties" id="endpointProperties"/>' +
                '</td>' +
                '</tr>' +
                '<tr>' +
                '<table cellpadding="0" cellspacing="0" border="0" class="styledLeft"' +
                'id="' + formID + '_headerTable" style="display:none;">' +
                '<thead>' +
                '<tr>' +
                '<th style="width:25%">Name</th>' +
                '<th style="width:25%">Value</th>' +
                '<th style="width:25%">Scope</th>' +
                '<th style="width:25%">Action</th>' +
                '</tr></thead><tbody></tbody></table></tr></tbody></table>';

         // the update button
        epInnerHTML += '<table class="styledLeft">' +
                       '<tbody>' +
                       '<tr>' +
                       '<td><br/><input id="" class="button" type="button" value="' + jsi18n['update'] + '"' +
                       ' onclick="upDateEndpoint(\'' + formID + '\',\'' + type + '\')"></td>' +
                       '</tr>' +
                       '</tbody>' +
                       '</table>' ;

        tableDiv.innerHTML = epInnerHTML;

        // attach the created table
        var desginViewDiv = document.getElementById('info');
        if (desginViewDiv != null || desginViewDiv != undefined) {
            desginViewDiv.appendChild(tableDiv);
        }

        for (i = 0; i < endpointNode.childNodes.length; ++i) {
            if (endpointNode.childNodes[i].nodeName == 'property') {
                addServiceParamRow(endpointNode.childNodes[i].getAttribute("name"),endpointNode.childNodes[i].getAttribute("value"),endpointNode.childNodes[i].getAttribute("scope"),formID+"_headerTable");
            }
        }
    });
}


// create a load balacne endpoint with given ID
function createLoadBalanceForm(newID) {
    var type = 'loadbalance';
    var formID = 'form_' + newID;
    YAHOO.util.Event.onAvailable("info", function() {
        var i;
        var tableDiv = document.createElement('div');
        tableDiv.style.display = 'none';
        tableDiv.id = formID;
        var sessionNode = null;
        var sessionSelectedOption = '';
        var sessionTimeout = '0'; // keep the session time out selected, default is 0

        var endpointNode = getNodeById(newID);
        for (i = 0; i < endpointNode.childNodes.length; i++) {
            if (endpointNode.childNodes[i].nodeName == 'session' || endpointNode.childNodes[i].nodeName == 'session') {
                sessionNode = endpointNode.childNodes[i];
                if (sessionNode != null) {
                    sessionSelectedOption = sessionNode.getAttribute('type');
                    for (j = 0; j < sessionNode.length; j++) {
                        if (sessionNode[j].nodeName == 'sessionTimeOut') {
                            sessionTimeout = sessionNode[j].firstChild.nodeValue;
                        }
                    }
                }

            } else if (endpointNode.childNodes[i].nodeName == 'loadbalance') {
                // TODO-other nodes
            }
        }

        var sessionManagementOptions = [
            ['SelectAValue','None'],
            ['http','Transport'],
            ['soap','SOAP'],
            ['simpleClientSession','Client ID']
        ];

        var epInnerHTML = '';
        epInnerHTML += '<table class="normal" width="100%">' +
                       '<tbody>' +
                       '<tr><td colspan="2"><h2>Load Balance Endpoint</h2></td></tr>';


        // session management
        var tempDivNode = document.createElement('div');
        var sessionSelect = document.createElement('select');
        var sesstionOtion = '';  // keep the session management option selected
        sessionSelect.setAttribute('id', formID + '_sesOptions');
        for (i = 0; i < sessionManagementOptions.length; i++) {
            sesstionOtion = document.createElement('option');
            sesstionOtion.innerHTML = sessionManagementOptions[i][1];
            if (sessionSelectedOption == sessionManagementOptions[i][0]) {
                sesstionOtion.setAttribute('selected', 'selected');
            }
            sesstionOtion.value = sessionManagementOptions[i][0];
            sessionSelect.appendChild(sesstionOtion);
        }
        sessionSelect.setAttribute('onchange', 'activateManagement(this, \'' + formID + '\')');
        tempDivNode.appendChild(sessionSelect);
        epInnerHTML += '<tr>' +
                       '<td class="leftCol-small">Session Management</td>' +
                       '<td>' + tempDivNode.innerHTML + '</td>' +
                       '</tr>';

        // session time out
        var sesstionTimeOutText = '';
        if (sessionSelectedOption == 'SelectAValue' || sessionSelectedOption == '') {
            sesstionTimeOutText = '<input type="text" value="' + sessionTimeout + '" id="' + formID + '_sessionTimeOut" class="textbox" disabled="disabled"/>';
        } else {
            sesstionTimeOutText = '<input type="text" value="' + sessionTimeout + '" id="' + formID + '_sessionTimeOut" class="textbox"/>';
        }

        epInnerHTML += '<tr>' +
                       '<td>Session Timeout (Mills)</td>' +
                       '<td>' + sesstionTimeOutText + '</td>' +
                       '</tr>';

        // the update button
        epInnerHTML += '<tr>' +
                       '<table class="styledLeft">' +
                       '<tbody>' +
                       '<tr>' +
                       '<td class="buttonRow"><input id="" class="button" type="button" value="Update"' +
                       ' onclick="upDateEndpoint(\'' + formID + '\',\'' + type + '\')"></td>' +
                       '</tr>' +
                       '</tbody>' +
                       '</table>' +
                       '</tr>';

        // end the HTML
        epInnerHTML += '</tbody></table>';

        tableDiv.innerHTML = epInnerHTML;

        // attach the created table
        var desginViewDiv = document.getElementById('info');
        if (desginViewDiv != null || desginViewDiv != undefined) {
            desginViewDiv.appendChild(tableDiv);
        }

    });
}
