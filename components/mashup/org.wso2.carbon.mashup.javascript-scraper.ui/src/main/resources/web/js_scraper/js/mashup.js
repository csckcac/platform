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

wso2.mashup.Mashup = {}

/**
 * WSO2.Mashup is a JavaScript representation of the Mashup deployed in the server.
 * @class WSO2.Mashup
 * @constructor
 * @param {String} mashupID			The Masup ID uniquely representing the Mashup
 * @param {String} MashupTitle		The Title of Mashup
 * @param {String} MashupDesc		The Description about Mashup
 */
wso2.mashup.Mashup =
function(mashupID, mashupTitle, mashupDesc, eprs, operations, status, serviceType) {

    /**
     * String which uniquely represent the Mashup
     * @property ID
     * @type String
     */
    this.ID = mashupID;

    /**
     * String Title of the Mashup
     * @property Title
     * @type String
     */
    this.Title = mashupTitle;

    /**
     * String Description about the Mashup
     * @property Description
     * @type String
     */
    this.Description = mashupDesc;

    /**
     * Endpoint references of the Mashup
     * @property eprs
     * @type Array
     */
    this.eprs = eprs;


    /**
     * Available operations of the Mashup
     * @property operations
     * @type Array
     */
    this.operations = operations;

    /**
     * The current activation status of the Mashup
     * @property eprs
     * @type String
     */
    this.status = status;

    /**
     * The service type of the mashup (either a JavaScript or Java service)
     * @property servicetype
     * @type String
     */
    this.servicetype = serviceType;

};


/**
 * Get the ID of the Mashup.
 * @method getID
 * @return {String}	String ID of the Mashup
 */
wso2.mashup.Mashup.prototype.getID = function() {
    return this.ID;
};


/**
 * Get the Title of the Mashup.
 * @method getTitle
 * @return {String}	String Title of the Mashup
 */
wso2.mashup.Mashup.prototype.getTitle = function() {
    return this.Title;
};


/**
 * Get the Description about the Mashup.
 * @method getDescription
 * @return {String}	String Description about the Mashup
 */
wso2.mashup.Mashup.prototype.getDescription = function() {
    return this.Description;
};


/**
 * Get the Title EPRs for this mashup
 * @method getEprs
 * @return {Array}	String array of EPRs
 */
wso2.mashup.Mashup.prototype.getEprs = function() {
    return this.eprs;
};


/**
 * Get the Operations of the Mashup.
 * @method getOperations
 * @return {Array}	String array of mashup operations
 */
wso2.mashup.Mashup.prototype.getOperations = function() {
    return this.operations;
};


/**
 * Get the activation status of the mashup.
 * @method getServiceStatus
 * @return {String}	"true" indicates a stopped or inactive service
 */
wso2.mashup.Mashup.prototype.getServiceStatus = function() {
    return this.status;
};


/**
 * Set the activation status of the service
 * @method setServiceStatus
 * @param {String}	New status as "true" or "false"
 */
wso2.mashup.Mashup.prototype.setServiceStatus = function(newStatus) {
    this.status = newStatus;
};


/**
 * Get the type of this mashup. The type is "MashupJSService" for JavaScript services.
 * @method getServiceType
 * @return {String}	
 */
wso2.mashup.Mashup.prototype.getServiceType = function() {
    return this.servicetype;
};
