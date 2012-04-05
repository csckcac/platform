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

var GLOBAL_SERVICE_STRING = "GlobalAdmin"
var SERVICE_GROUP_ADMIN_STRING = "ServiceGroupAdmin"
/*var len = ROOT_CONTEXT.length;
var position = eval(len - 1);
var lastChar = ROOT_CONTEXT.substring(position, len);
var path;
if (lastChar == "/") {
    path = ROOT_CONTEXT + SERVICE_PATH;
} else {
    path = ROOT_CONTEXT + "/" + SERVICE_PATH;
}
var mashupServerURL = self.location.protocol + "//" + self.location.hostname + ":" +
                      self.location.port + path;*/

if (typeof wso2 == "undefined") {
    /**
     * The WSO2 global namespace object.  If WSO2 is already defined, the
     * existing WSO2 object will not be overwritten so that defined
     * namespaces are preserved.
     * @class WSO2
     * @static
     */
    var wso2 = {};
}

wso2.mashup.services = {}

/**
 * @description Method used handle login operation to the admin console.
 * @method login
 * @public
 * @static
 * @param {string} 	userName	user name of the user
 * @param {string} 	password	password
 * @param {callback} callBack 	User-defined callback function or object
 */
wso2.mashup.services.login = function(userName, password, callback) {

    var bodyXML = '<ns1:login  xmlns:ns1="http://org.apache.axis2/xsd">\n' +
                  ' <arg0>' + userName + '</arg0>\n' +
                  ' <arg1>' + password + '</arg1>\n' +
                  ' </ns1:login>\n';
    var callURL = mashupServerURL + "/" + GLOBAL_SERVICE_STRING;

    new wso2.wsf.WSRequest(callURL, "login", bodyXML, callback, userName);
};


/**
 * @description Method used send a logout notification to the backend
 * @method logout
 * @public
 * @static
 * @param {callback} callBack 	User-defined callback function or object
 */
wso2.mashup.services.logout = function(callback) {

    var bodyXML = '<ns1:logout  xmlns:ns1="http://org.apache.axis2/xsd"/>\n';

    var callURL = mashupServerURL + "/" + GLOBAL_SERVICE_STRING + "/" + "logout";

    new wso2.wsf.WSRequest(callURL, "logout", bodyXML, callback);
};


/**
 * @description Method used to obtain a list of deployed service groups in the server
 * @method listServiceGroups
 * @public
 * @static
 * @param {callback} callBack 	User-defined callback function or object
 */
wso2.mashup.services.listServiceGroups = function (callback, params) {
    var body_xml = '<ns1:listServiceGroups  xmlns:ns1="http://org.apache.axis2/xsd">\n' +
                   ' </ns1:listServiceGroups>\n';

    var callURL = mashupServerURL + "/" + SERVICE_GROUP_ADMIN_STRING + "/" + "listServiceGroups";

    new wso2.wsf.WSRequest(callURL, "listServiceGroups", body_xml, callback, params, wso2.mashup.services.defaultErrHandler);

};


/**
 * @description Method used to obtain a list of operations in a given service
 * @method listAllOperations
 * @public
 * @static
 * @param {String} serviceName 	Name of the service to retrieve operations
 * @param {callback} callBack 	User-defined callback function or object
 */
wso2.mashup.services.listAllOperations = function (serviceName, callback) {

    var body_xml = '<ns1:listAllOperations xmlns:ns1="http://org.apache.axis2/xsd">\n' +
                   ' <arg0>' + serviceName + '</arg0>\n' +
                   ' <arg1>' + serviceName + '</arg1>\n' +
                   ' </ns1:listAllOperations>\n';

    var callURL = mashupServerURL + "/" + "OperationAdmin" + "/" + "listAllOperations";

    new wso2.wsf.WSRequest(callURL, "listAllOperations", body_xml, callback, "", wso2.mashup.services.defaultErrHandler);
};


/**
 * @description Method used to meta-data for a given service
 * @method listServiceData
 * @public
 * @static
 * @param {String} serviceName 	Name of the service to retrieve operations
 * @param {callback} callBack 	User-defined callback function or object
 */
wso2.mashup.services.listServiceData = function (serviceName, callback, params) {

    var body_xml = '<req:getServiceDataRequest xmlns:req="http://service.admin.mashup.wso2.org/xsd">\n' +
                   ' <req:serviceName>' + serviceName + '</req:serviceName>\n' +
                   ' </req:getServiceDataRequest>\n';

    var callURL = mashupServerURL + "/" + "MashupAdminService" + "/" ;

    new wso2.wsf.WSRequest(callURL, "getServiceData", body_xml, callback, params, wso2.mashup.services.defaultErrHandler);
};


/**
 * @description Method used to meta-data for a given service
 * @method listServiceData
 * @public
 * @static
 * @param {String} serviceName 	Name of the service to retrieve operations
 * @param {callback} callBack 	User-defined callback function or object
 */
wso2.mashup.services.getServiceDataForEditor = function (serviceName, callback) {

    var body_xml = '<req:getDataRequest xmlns:req="http://servicemetadatalister.coreservices.mashup.wso2.org/xsd">\n' +
                   ' <req:serviceName>' + serviceName + '</req:serviceName>\n' +
                   ' </req:getDataRequest>\n';

    var callURL = mashupServerURL + "/" + "ServiceMetaDataLister" + "/" ;

    new wso2.wsf.WSRequest(callURL, "getData", body_xml, callback, "", wso2.mashup.services.defaultErrHandler);
};


/**
 * @description Method used to activate/de-activate a service
 * @method activationOfService
 * @public
 * @static
 * @param {String} serviceName 	Name of the service to retrieve operations
 * @param {boolean} serviceStatus The desired status of
 * @param {callback} callBack 	User-defined callback function or object
 */
wso2.mashup.services.activationOfService = function (serviceName, serviceStatus, callback, params) {
    var body_xml = '';
    var callURL = mashupServerURL + "/" + "MashupAdminService" ;
    var soapAction = "";
    var params = new Array(serviceName, serviceStatus);

    if (serviceStatus) {
        body_xml =
        '<req:startServiceRequest xmlns:req="http://service.admin.mashup.wso2.org/xsd">\n' +
        ' <req:serviceName>' + serviceName + '</req:serviceName>\n' +
        ' </req:startServiceRequest>\n';
        soapAction = "startService";
    }
    if (!serviceStatus) {
        body_xml =
        '<req:stopServiceRequest xmlns:req="http://service.admin.mashup.wso2.org/xsd">\n' +
        ' <req:serviceName>' + serviceName + '</req:serviceName>\n' +
        ' </req:stopServiceRequest>\n';
        soapAction = "stopService";

    }
    new wso2.wsf.WSRequest(callURL, soapAction, body_xml, callback, params, wso2.mashup.services.defaultErrHandler);
};


/**
 * @description Method used to update documentation of a Service
 * @method editServiceDocumentation
 * @public
 * @static
 * @param {String} serviceName 	Name of the service to update the documentation
 * @param {String} documentation 	Updated documentation of the Service
 * @param {callback} callBack 	User-defined callback function or object
 * @param {callback} params 	Parameters to be set in the callback
 */
wso2.mashup.services.editServiceDocumentation =
function (serviceName, documentation, callback, params) {

    var callURL = mashupServerURL + "/" + "MashupAdminService" + "/" ;

    var body_xml = '<req:setServiceDocumentation xmlns:req="http://service.admin.mashup.wso2.org/xsd">\n' +
                   ' <req:serviceName>' + serviceName + '</req:serviceName>\n' +
                   ' <req:documentation>' + documentation + '</req:documentation>\n' +
                   ' </req:setServiceDocumentation>\n';

    new wso2.wsf.WSRequest(callURL, "setServiceDocumentation", body_xml, callback, params, wso2.mashup.services.defaultErrHandler);
};


/**
 * @description Method used to update documentation of a Service Operation
 * @method editOperationDocumentation
 * @public
 * @static
 * @param {String} serviceName 	Name of the service to retrieve operations
 * @param {String} operationName 	Name of the service operation
 * @param {String} documentation 	Updated documentation of the Service operation
 * @param {callback} callBack 	User-defined callback function or object
 * @param {callback} params 	Parameters to be set in the callback
 */
wso2.mashup.services.editOperationDocumentation =
function (serviceName, operationName, documentation, callback, params) {

    var callURL = mashupServerURL + "/" + "MashupAdminService" + "/" ;

    var body_xml = '<req:setOperationDocumentation xmlns:req="http://service.admin.mashup.wso2.org/xsd">\n' +
                   ' <req:serviceName>' + serviceName + '</req:serviceName>\n' +
                   ' <req:operationName>' + operationName + '</req:operationName>\n' +
                   ' <req:documentation>' + documentation + '</req:documentation>\n' +
                   ' </req:setOperationDocumentation>\n';

    new wso2.wsf.WSRequest(callURL, "setOperationDocumentation", body_xml, callback, params, wso2.mashup.services.defaultErrHandler);
};

/**
 * @description Method used to enable or disable an operation of a given service
 * @method setServiceOperationStatus
 * @public
 * @static
 * @param {String} serviceName 	Name of the service to retrieve operations
 * @param {String} operationName 	Name of the service operation
 * @param {Boolean} status 	The desired status of the operation
 * @param {callback} callBack 	User-defined callback function or object
 * @param {callback} params 	Parameters to be set in the callback
 */
wso2.mashup.services.setServiceOperationStatus =
function (serviceName, operationName, status, callback, params) {

    var callURL = mashupServerURL + "/" + "MashupAdminService" + "/" ;

    var body_xml = '<req:changeOperationStatus xmlns:req="http://service.admin.mashup.wso2.org/xsd">\n' +
                   ' <req:serviceName>' + serviceName + '</req:serviceName>\n' +
                   ' <req:operationName>' + operationName + '</req:operationName>\n' +
                   ' <req:status>' + status + '</req:status>\n' +
                   ' </req:changeOperationStatus>\n';

    new wso2.wsf.WSRequest(callURL, "changeOperationStatus", body_xml, callback, params, wso2.mashup.services.defaultErrHandler);
};


/**
 * @description Validates a given service path
 * @method createMashupSkeleton
 * @param {String} mashupPath A path to a new mashup
 * @param {callback} callBack 	User-defined callback function or object
 * @param {callback} params 	Parameters to be set in the callback
 */
wso2.mashup.services.isPossibleToCreate = function (mashupPath, callback, params) {

    var callURL = mashupServerURL + "/" + "ServiceMetaDataLister" + "/" ;

    var body_xml = '<req:isPossibleToCreateRequest xmlns:req="http://servicemetadatalister.coreservices.mashup.wso2.org/xsd">\n' +
                   ' <req:servicePath>' + mashupPath + '</req:servicePath>\n' +
                   ' </req:isPossibleToCreateRequest>\n';

    new wso2.wsf.WSRequest(callURL, "isPossibleToCreate", body_xml, callback, params, wso2.mashup.services.defaultErrHandler);
};


/**
 * @description Method used to share a selected service with another server
 * @method shareService
 * @public
 * @static
 * @param {String} serviceName 	Name of the service to retrieve operations
 * @param {String} destinationAddress 	Address of the destination server
 * @param {callback} callBack 	User-defined callback function or object
 * @param {callback} params 	Parameters to be set in the callback
 */
wso2.mashup.services.shareService =
function (serviceName, destinationAddress, username, password, overwriteExisting, migrateTags,
          localUserName, callback, params,
          errorCallback, mode, basicAuthUsername, basicAuthPassword) {

    var callURL = mashupServerURL + "/" + "ServiceSharingHelper" + "/" ;

    var body_xml = '<req:shareServiceRequest xmlns:req="http://servicesharinghelper.coreservices.mashup.wso2.org/xsd">\n' +
                   ' <req:serviceName>' + serviceName + '</req:serviceName>\n' +
                   ' <req:username>' + username + '</req:username>\n' +
                   ' <req:password>' + password + '</req:password>\n' +
                   ' <req:destinationServerAddress>' + destinationAddress +
                   '</req:destinationServerAddress>\n' +
                   ' <req:overwriteExisting>' + overwriteExisting +
                   '</req:overwriteExisting>\n' +
                   ' <req:migrateTags>' + migrateTags +
                   '</req:migrateTags>\n' +
                   ' <req:localUser>' + localUserName +
                   '</req:localUser>\n' +
                   ' <req:mode>' + mode +
                   '</req:mode>\n';

    if (mode == "withBasicAuth") {
        body_xml += ' <req:basicAuthUsername>' + basicAuthUsername +
                    '</req:basicAuthUsername>\n' +
                    ' <req:basicAuthPassword>' + basicAuthPassword +
                    '</req:basicAuthPassword>\n';
    }
    body_xml += ' </req:shareServiceRequest>\n';

    new wso2.wsf.WSRequest(callURL, "shareService", body_xml, callback, params, errorCallback);
};

/**
 * @description Method used to share a selected service with another server using InfoCard authentication
 * @method shareService
 * @public
 * @static
 * @param {String} serviceName 	Name of the service to retrieve operations
 * @param {String} destinationAddress 	Address of the destination server
 * @param {callback} callBack 	User-defined callback function or object
 * @param {callback} params 	Parameters to be set in the callback
 */
wso2.mashup.services.shareServiceIC =
function (serviceName, destinationAddress, infocardtoken, overwriteExisting, migrateTags, localUserName, callback,
          params,
          errorCallback, mode, basicAuthUsername, basicAuthPassword) {

    var callURL = mashupServerURL + "/" + "ServiceSharingHelper" + "/" ;

    //Making the infocard token transport friendly
    infocardtoken = "<![CDATA[" + infocardtoken + "]]>";

    var body_xml = '<req:shareServiceICRequest xmlns:req="http://servicesharinghelper.coreservices.mashup.wso2.org/xsd">\n' +
                   ' <req:serviceName>' + serviceName + '</req:serviceName>\n' +
                   ' <req:infoCardToken>' + infocardtoken + '</req:infoCardToken>\n' +
                   ' <req:destinationServerAddress>' + destinationAddress +
                   '</req:destinationServerAddress>\n' +
                   ' <req:overwriteExisting>' + overwriteExisting +
                   '</req:overwriteExisting>\n' +
                   ' <req:migrateTags>' + migrateTags +
                   '</req:migrateTags>\n' +
                   ' <req:localUser>' + localUserName +
                   '</req:localUser>\n' +
                   ' <req:mode>' + mode +
                   '</req:mode>\n';

    if (mode == "withBasicAuth") {
        body_xml += ' <req:basicAuthUsername>' + basicAuthUsername +
                    '</req:basicAuthUsername>\n' +
                    ' <req:basicAuthPassword>' + basicAuthPassword +
                    '</req:basicAuthPassword>\n';
    }

    body_xml += ' </req:shareServiceICRequest>';

    new wso2.wsf.WSRequest(callURL, "shareServiceIC", body_xml, callback, params, errorCallback);
};

/**
 * @description Method used to get a list of faulty service archives
 * @method listFaultyArchives
 * @public
 * @static
 * @param {callback} callBack 	User-defined callback function or object
 */
wso2.mashup.services.listFaultyArchives = function(callback) {

    var body_xml = '<req:getFaultyServiceArchivesRequest xmlns:req="http://org.apache.axis2/xsd">\n' +
                   ' </req:getFaultyServiceArchivesRequest>\n';

    var callURL = mashupServerURL + "/" + "ServiceAdmin" ;

    new wso2.wsf.WSRequest(callURL, "getFaultyServiceArchives", body_xml, callback, "", wso2.mashup.services.defaultErrHandler);

};


/**
 * @description Method used to save changes done to a service source
 * @method saveServiceSource
 * @public
 * @static
 * @param {String} path 	Path of the service
 * @param {String} modifiedSource 	Ammended source code
 * @param {callback} callBack 	User-defined callback function or object
 * @param {callback} params 	Parameters to be set in the callback
 */
wso2.mashup.services.saveServiceSource = function (path, modifiedSource, callback, params) {

    var callURL = mashupServerURL + "/" + "MashupAdminService" + "/" ;

    var serviceSource = Base64.encode(modifiedSource);

    var body_xml = '<req:saveServiceSource xmlns:req="http://service.admin.mashup.wso2.org/xsd">\n' +
                   ' <req:path>' + path + '</req:path>\n' +
                   ' <req:modifiedSource>' + serviceSource + '</req:modifiedSource>\n' +
                   ' </req:saveServiceSource>\n';

    new wso2.wsf.WSRequest(callURL, "saveServiceSource", body_xml, callback, params, wso2.mashup.services.defaultErrHandler);
};

/**
 * @description Saves the CustomUI of a JS Service
 * @param {String} path 	Path of the service
 * @param {String} modifiedSource 	Ammended source code
 * @param {callback} callBack 	User-defined callback function or object
 * @param {callback} params 	Parameters to be set in the callback
 */
wso2.mashup.services.saveUiSource = function (path, modifiedSource, callback, params, type) {
    var callURL = mashupServerURL + "/" + "MashupAdminService" + "/" ;

    var serviceSource = Base64.encode(modifiedSource);

    var body_xml = '<req:saveUiSource xmlns:req="http://service.admin.mashup.wso2.org/xsd">\n' +
                   ' <req:path>' + path + '</req:path>\n' +
                   ' <req:modifiedSource>' + serviceSource + '</req:modifiedSource>\n' +
                   ' <req:type>' + type + '</req:type>\n' +
                   ' </req:saveUiSource>\n';

    new wso2.wsf.WSRequest(callURL, "saveUiSource", body_xml, callback, params, wso2.mashup.services.defaultErrHandler);
};

/**
 * @description Deletes a JS Service and its associated resources directory
 * @param {String} serviceName Name of the Service
 * @param {callback} callback User-defined callback function or object
 */
wso2.mashup.services.deleteService = function (serviceName, callback) {
    var callURL = mashupServerURL + "/" + "MashupAdminService" + "/" ;

    var body_xml = '<req:deleteService xmlns:req="http://service.admin.mashup.wso2.org/xsd">\n' +
                   ' <req:serviceName>' + serviceName + '</req:serviceName>\n' +
                   ' </req:deleteService>\n';

    new wso2.wsf.WSRequest(callURL, "deleteService", body_xml, callback, "", wso2.mashup.services.defaultErrHandler);
};

/**
 * @description Deletes a trusted certificate from the user keystore
 * @param {String} userName User name of the keystore owner
 * @param {String} certAlias Alias used when storing the certificate in the keystore
 * @param {callback} callback User-defined callback function or object
 */
wso2.mashup.services.deleteCertificate = function (userName, certAlias, callback) {
    var callURL = mashupServerURL + "/" + "MashupAdminService" + "/" ;

    var body_xml = '<req:deleteCert xmlns:req="http://service.admin.mashup.wso2.org/xsd">\n' +
                   ' <req:userName>' + userName + '</req:userName>\n' +
                   ' <req:alias>' + certAlias + '</req:alias>\n' +
                   ' </req:deleteCert>\n';

    new wso2.wsf.WSRequest(callURL, "deleteCert", body_xml, callback, "", wso2.mashup.services.defaultErrHandler);
};

/**
 * @description Imports a certificate chain from a given URLs domain into a user keystore
 * @param {String} userName User name of the keystore owner
 * @param {String} certAlias Alias used when storing the certificate in the keystore
 * @param {String} url URL of the trusted site
 * @param {callback} callback User-defined callback function or object
 */
wso2.mashup.services.importCertFromUrl = function (userName, certAlias, url, callback) {
    var callURL = mashupServerURL + "/" + "MashupAdminService" + "/" ;

    var body_xml = '<req:importCertFromUrl xmlns:req="http://service.admin.mashup.wso2.org/xsd">\n' +
                   ' <req:userName>' + userName + '</req:userName>\n' +
                   ' <req:alias>' + certAlias + '</req:alias>\n' +
                   ' <req:url>' + url + '</req:url>\n' +
                   ' </req:importCertFromUrl>\n';

    new wso2.wsf.WSRequest(callURL, "importCertFromUrl", body_xml, callback, "", wso2.mashup.services.defaultErrHandler);
};

/**
 * @description Re deploys a JS Service
 * @param {String} serviceName Name of the Service
 * @param {callback} callback User-defined callback function or object
 */
wso2.mashup.services.reDeployService = function (serviceName, callback) {
    var callURL = mashupServerURL + "/" + "MashupAdminService" + "/" ;

    var body_xml = '<req:redeployService xmlns:req="http://service.admin.mashup.wso2.org/xsd">\n' +
                   ' <req:serviceName>' + serviceName + '</req:serviceName>\n' +
                   ' </req:redeployService>\n';

    new wso2.wsf.WSRequest(callURL, "redeployService", body_xml, callback, "", wso2.mashup.services.defaultErrHandler);
};

/**
 * @description Enables a transport for a named service
 * @param {String} serviceName
 * @param {String} transportName
 * @param {Array} params
 * @param {callback} callback
 */
wso2.mashup.services.enableServiceTransport =
function (serviceName, transportName, params, callback) {
    var callURL = mashupServerURL + "/" + "MashupAdminService" + "/" ;

    var body_xml = '<req:enableServiceTransport xmlns:req="http://service.admin.mashup.wso2.org/xsd">\n' +
                   ' <req:serviceName>' + serviceName + '</req:serviceName>\n' +
                   ' <req:transportName>' + transportName + '</req:transportName>\n' +
                   ' </req:enableServiceTransport>\n';

    new wso2.wsf.WSRequest(callURL, "enableServiceTransport", body_xml, callback, params, wso2.mashup.services.defaultErrHandler);
};

/**
 * @description Disables a transport for a named service
 * @param {String} serviceName
 * @param {String} transportName
 * @param {Array} params
 * @param {callback} callback
 */
wso2.mashup.services.disableServiceTransport =
function (serviceName, transportName, params, callback) {
    var callURL = mashupServerURL + "/" + "MashupAdminService" + "/" ;

    var body_xml = '<req:disableServiceTransport xmlns:req="http://service.admin.mashup.wso2.org/xsd">\n' +
                   ' <req:serviceName>' + serviceName + '</req:serviceName>\n' +
                   ' <req:transportName>' + transportName + '</req:transportName>\n' +
                   ' </req:disableServiceTransport>\n';

    new wso2.wsf.WSRequest(callURL, "disableServiceTransport", body_xml, callback, params, wso2.mashup.services.defaultErrHandler);
};

/**
 * @description Gets a list of faulty services and possible reasons
 * @param {callback} callback User-defined callback function or object
 */
wso2.mashup.services.getFaultyServices = function (callback) {
    var callURL = mashupServerURL + "/" + "ServiceMetaDataLister" + "/" ;

    var body_xml = '<req:getFaultyServicesOpRequest xmlns:req="http://servicemetadatalister.coreservices.mashup.wso2.org/xsd">\n' +
                   ' </req:getFaultyServicesOpRequest>\n';

    new wso2.wsf.WSRequest(callURL, "getFaultyServices", body_xml, callback, "", wso2.mashup.services.defaultErrHandler);
};

/**
 * @description Gets a list of users registered on this server.
 * @param {namePrefix} namePrefix to filter list of user names.
 * @param {callback} callback User-defined callback function or object.
 */
wso2.mashup.services.getUserNames = function (namePrefix, callback) {
    var callURL = mashupServerURL + "/" + "ServiceMetaDataLister" + "/" ;

    var body_xml = '<req:getUserNamesRequest xmlns:req="http://servicemetadatalister.coreservices.mashup.wso2.org/xsd">\n' +
                   ' <req:namePrefix>' + namePrefix + '</req:namePrefix>\n' +
                   ' </req:getUserNamesRequest>\n';

    new wso2.wsf.WSRequest(callURL, "getUserNames", body_xml, callback, "", wso2.mashup.services.defaultErrHandler);
};

/**
 * @description Gets a list of users registered on this server.
 * @param {namePrefix} namePrefix to filter list of user names.
 * @param {callback} callback User-defined callback function or object.
 */
wso2.mashup.services.suggestSearchPhrases = function (searchPrefix, searchType, callback) {
    var callURL = mashupServerURL + "/" + "ServiceMetaDataLister" + "/" ;

    var body_xml = '<req:suggestSearchPhrases xmlns:req="http://servicemetadatalister.coreservices.mashup.wso2.org/xsd">\n' +
                   ' <req:searchPrefix>' + searchPrefix + '</req:searchPrefix>\n' +
                   ' <req:type>' + searchType + '</req:type>\n' +
                   ' </req:suggestSearchPhrases>\n';

    new wso2.wsf.WSRequest(callURL, "suggestSearchPhrases", body_xml, callback, "", wso2.mashup.services.defaultErrHandler);
};

/**
 * @description Method used to download a mashup from a remote server
 * @method downloadService
 * @public
 * @static
 * @param {String} serviceName 	Name of the service to download
 * @param {String} remoteServer 	IP of the remote server
 * @param {callback} callBack 	User-defined callback function or object
 * @param {callback} params 	Parameters to be set in the callback
 * @param {callback} errorCallback 	User-defined error callback function or object
 */
wso2.mashup.services.downloadService =
function (serviceName, remoteServer, callback, params, errorCallback) {

    var callURL = mashupServerURL + "/" + "ServiceSharingHelper" + "/" ;

    var body_xml = '<req:downloadServiceRequest xmlns:req="http://servicesharinghelper.coreservices.mashup.wso2.org/xsd">\n' +
                   ' <req:remoteServer>' + remoteServer + '</req:remoteServer>\n' +
                   ' <req:serviceName>' + serviceName + '</req:serviceName>\n' +
                   ' </req:downloadServiceRequest>\n';

    new wso2.wsf.WSRequest(callURL, "downloadService", body_xml, callback, params, errorCallback);

};


/**
 * @description Method used to get a list of JavaScript services deployed in the server
 * @method getSharableServices
 * @public
 * @static
 * @param {callback} callBack 	User-defined callback function or object
 */
wso2.mashup.services.getSharableServices = function(callback) {

    var callURL = mashupServerURL + "/" + "ServiceSharingHelper" + "/" ;

    var body_xml = '<req:getSharableServicesRequest xmlns:req="http://servicesharinghelper.coreservices.mashup.wso2.org/xsd">\n' +
                   ' </req:getSharableServicesRequest>\n';

    new wso2.wsf.WSRequest(callURL, "getSharableServices", body_xml, callback, "", wso2.mashup.services.defaultErrHandler);
};


/**
 * @description Obtains the contents of a given URL
 * @param url The URL to get contents from
 * @param callback  User-defined callback function or object
 */
wso2.mashup.services.getUrlContents = function(url, renderDHTML, userAgent, callback) {
    var callURL = mashupServerURL + "ScraperService" + "/" ;

    var body_xml = '<req:getUrlRequest xmlns:req="http://org.wso2.wsf/tools">\n' +
                   ' <req:url>' + url + '</req:url>\n' +
                   ' <req:renderDHTML>' + renderDHTML + '</req:renderDHTML>\n' +
                   ' <req:userAgent>' + userAgent + '</req:userAgent>\n' +
                   ' </req:getUrlRequest>\n';

    new wso2.wsf.WSRequest(callURL, "getUrl", body_xml, callback, "", wso2.mashup.services.defaultErrHandler);
};

/**
 * @description Method used to meta-data for a given service
 * @method getSecurityAssignment
 * @public
 * @static
 * @param {String} serviceName 	Name of the service to retrieve security assignments
 * @param {callback} callBack 	User-defined callback function or object
 */
wso2.mashup.services.getSecurityAssignment = function (serviceName, callback) {

    var body_xml = '<req:getSecurityAssignmentRequest xmlns:req="http://service.admin.mashup.wso2.org/xsd">\n' +
                   ' <req:serviceId>' + serviceName + '</req:serviceId>\n' +
                   ' <serviceVersion></serviceVersion>\n' +
                   ' </req:getSecurityAssignmentRequest>\n';

    var callURL = mashupServerURL + "/" + "MashupSecurityScenarioConfigAdmin/getSecurityAssignment" + "/" ;

    new wso2.wsf.WSRequest(callURL, "getSecurityAssignment", body_xml, callback, "", wso2.mashup.services.defaultErrHandler);
};


/**
 * @description Method used to meta-data for a given service
 * @method getSecurityAssignment
 * @public
 * @static
 * @param {String} serviceName 	Name of the service to retrieve security assignments
 * @param {callback} callBack 	User-defined callback function or object
 */
wso2.mashup.services.assignSecurityScenario = function (serviceName, scenarioID, category, roles, users, callback) {

    var body_xml = '<req:assignSecurityScenarioRequest xmlns:req="http://service.admin.mashup.wso2.org/xsd">\n' +
                   ' <req:serviceName>' + serviceName + '</req:serviceName>\n' +
                   ' <req:scenarioID>' + scenarioID + '</req:scenarioID>\n' +
                   ' <req:category>' + category + '</req:category>\n' +
                   users +
                   roles +
                   ' </req:assignSecurityScenarioRequest>\n';

    var callURL = mashupServerURL + "/" + "MashupSecurityScenarioConfigAdmin/assignSecurityScenario" + "/" ;

    new wso2.wsf.WSRequest(callURL, "assignSecurityScenario", body_xml, callback, "", wso2.mashup.services.defaultErrHandler);
};


/**
 * @description The default error handler
 * @method defaultErrHandler
 */
wso2.mashup.services.defaultErrHandler = function () {

    var error = this.req.error;
    if (!error) {
        CARBON.showErrorDialog("An Error occured! Please refer to system admin for more details.");
    } else if (error.reason.indexOf("Access Denied. Please login first.") > -1) {
        var rememberMe = BrowserUtility.Cookie("rememberMe");
        var dialogText;
        if (rememberMe == 'true') {
            dialogText = "Attempting to restore session. Please close this dialog and Retry the operation.";
        } else {
            dialogText = "Please Sign In, close this dialog and retry the operation.";
        }
        CARBON.showErrorDialog("Session Expired! " + dialogText);
        //Envoking force logout
        //wso2.mashup.handleLogout();
    } else if (typeof (error.detail.indexOf) != "undefined" &&
               error.detail.indexOf("NS_ERROR_NOT_AVAILABLE") > -1) {
        CARBON.showErrorDialog("An error occured", "Your session has expired.");
        //Envoking force logout
        //wso2.mashup.handleLogout();
    } else {
        CARBON.showErrorDialog("An error occured! " + error.reason);
    }
};
