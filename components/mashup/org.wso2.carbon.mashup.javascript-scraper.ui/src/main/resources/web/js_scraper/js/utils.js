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

// Counts the number of recursive requests made
var requestCountCustomUI = 0;

// Maximum recursions allowed when an exception occurs
var maxRecursionsCustomUI = 10;

function isCustomUiAvailable(serviceLocation, callback) {
    var xmlHttpRequest4ui = createXmlHttpRequest();

    //Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpRequest4ui)
    {
        //Check for requested web-accesible artifact (e.g. index.html, gadget.xml)
        xmlHttpRequest4ui.open("GET", serviceLocation + "/index.html", true);

        xmlHttpRequest4ui.onreadystatechange = function () {
            if (xmlHttpRequest4ui.readyState == 4) {

                try {
                    if (xmlHttpRequest4ui.status == 200) {
                        if (typeof callback != 'undefined') {
                            callback.call(this);
                        }
                    }
                } catch(ex) {
                    if (requestCountCustomUI < maxRecursionsCustomUI) {
                        requestCountCustomUI++;
                        isCustomUiAvailable(serviceLocation, callback);
                    }
                }
            }
        }

        xmlHttpRequest4ui.send(null);
    }

    var xmlHttpRequest4ui2 = createXmlHttpRequest();

    xmlHttpRequest4ui2.open("GET", serviceLocation + "/index.htm", true);

    xmlHttpRequest4ui2.onreadystatechange = function () {
        if (xmlHttpRequest4ui2.readyState == 4) {
            try {
                if (xmlHttpRequest4ui2.status == 200) {
                    if (typeof callback != 'undefined') {
                        callback.call(this);
                    }
                }
            } catch(ex) {
                if (requestCountCustomUI < maxRecursionsCustomUI) {
                    requestCountCustomUI++;
                    isCustomUiAvailable(serviceLocation, callback);
                }
            }
        }
    }

    xmlHttpRequest4ui2.send(null);

}

function isGadgetAvailable(serviceLocation, callback) {
    var xmlHttpRequest4ui = createXmlHttpRequest();

    //Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpRequest4ui)
    {
        //Check for requested web-accesible artifact (e.g. index.html, gadget.xml)
        xmlHttpRequest4ui.open("GET", serviceLocation + "/gadget.xml", true);

        xmlHttpRequest4ui.onreadystatechange = function () {
            if (xmlHttpRequest4ui.readyState == 4) {

                try {
                    if (xmlHttpRequest4ui.status == 200) {
                        if (typeof callback != 'undefined') {
                            callback.call(this);
                        }
                    }
                } catch(ex) {
                    isGadgetAvailable(serviceLocation, callback);
                }
            }
        }

        xmlHttpRequest4ui.send(null);
    }
}

function isDashboardAvailable(callback) {

    var dashboardHome = "../dashboard/index.jsp"

    var xmlHttpRequest4dashboard = createXmlHttpRequest();

    //Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpRequest4dashboard)
    {
        //Check for requested web-accesible artifact (e.g. index.html)
        xmlHttpRequest4dashboard.open("GET", dashboardHome, true);

        xmlHttpRequest4dashboard.onreadystatechange = function () {
            if (xmlHttpRequest4dashboard.readyState == 4) {

                try {
                    if (xmlHttpRequest4dashboard.status == 200) {
                        // Veryfying whether we really have the dashboard home
                        if (xmlHttpRequest4dashboard.responseText.indexOf("Sorry. An error occured while processing your request.") == -1) {
                            if (typeof callback != 'undefined') {
                                callback.call(this);
                            }
                        }

                    }
                } catch(ex) {
                    isDashboardAvailable(callback);
                }
            }
        }

        xmlHttpRequest4dashboard.send(null);
    }
}


function createXmlHttpRequest() {
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
}

function redirectToHttps(bounceback) {

    wso2.wsf.Util.initURLs();
    var locationString = self.location.href;
    var _tmpURL = locationString.substring(0, locationString.lastIndexOf('/'));
    if (_tmpURL.indexOf('https') == -1) {

        //Re-direct to https
        var redirectUrl = "https://" + self.location.hostname;

        if (!(URL.indexOf('://') == URL.lastIndexOf(':'))) {
            redirectUrl += ":" + HTTPS_PORT;
        }

        redirectUrl += getRootContext() + decodeURI(bounceback);

        window.location = redirectUrl;
    }
}

function redirectToHttp(bounceback) {

    wso2.wsf.Util.initURLs();
    var locationString = self.location.href;
    var _tmpURL = locationString.substring(0, locationString.lastIndexOf('/'));
    if (_tmpURL.indexOf('http') == -1) {

        //Re-direct to http
        var redirectUrlHttp = "http://" + self.location.hostname;

        if (!(URL.indexOf('://') == URL.lastIndexOf(':'))) {
            redirectUrlHttp += ":" + HTTP_PORT;
        }

        redirectUrlHttp += getRootContext() + decodeURI(bounceback);

        window.location = redirectUrlHttp;
    }
}


function getGreeting() {
    var greeting;
    var display = "";

    var datetoday = new Date();
    var thehour = datetoday.getHours();

    if (thehour > 17) {
        display = "evening";
    } else if (thehour > 11) {
        display = "afternoon";
    } else {
        display = "morning";
    }

    greeting = ("Good " + display);

    return greeting;
}

function showHideCommon(divxName) {
    divx = document.getElementById(divxName);

    /* show the intialy hidden object */
    if (divx.style.display == 'none') {
        if (divx.nodeName == 'DIV')
            divx.style.display = 'block';
        if (divx.nodeName == 'IMG')
            try {
                divx.style.display = 'inline';
            } catch(e) {
                divx.style.display = 'block';
            }

        if (divx.nodeName == 'TR') {
            try {
                divx.style.display = 'table-row';
            } catch(e) {
                divx.style.display = 'block';
            }
        }
        if (divx.nodeName == 'TD')
            divx.style.display = 'table-cell';
        if (divx.nodeName == 'A')
            divx.style.display = 'inline';
        if (divx.nodeName == 'SPAN')
            divx.style.display = 'inline';
        if (divx.nodeName == 'INPUT')
            divx.style.display = 'inline';
        if (divx.nodeName == 'P')
            divx.style.display = 'block';
        if (divx.nodeName == 'TABLE')
            divx.style.display = 'block';
       //link.innerHTML=textHidden;
    }
        /* hide the initaly shown object */
    else
    {
        divx.style.display = 'none';
    }
}

function getRootContext() {
    var len = ROOT_CONTEXT.length;
    var position = eval(len - 1);
    var lastChar = ROOT_CONTEXT.substring(position, len);
    if (lastChar == "/") {
        return ROOT_CONTEXT;
    } else {
        return ROOT_CONTEXT + "/";
    }
}