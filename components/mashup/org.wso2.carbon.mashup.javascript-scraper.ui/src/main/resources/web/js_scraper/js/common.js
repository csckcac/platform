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

var userLoggedOn = false;
function previewRating(ratingDivId, value) {
    if (userLoggedOn) {
        var images = $(ratingDivId).getElementsByTagName("img");
        for (var i = 0; i < 5; i++) {
            var src = images[i].src;
            var currentState = src.charAt(src.indexOf("images/r") + 9);
            if (i < value) images[i].src = "images/r4" + currentState + ".gif";
            else images[i].src = "images/r0" + currentState + ".gif";
        }
    }
}

function clearPreview(ratingDivId) {
    if (userLoggedOn) {
        var ratingDiv = $(ratingDivId);
        var initialState = ratingDiv.getAttribute('initialState').split("_");
        var images = ratingDiv.getElementsByTagName("img");
        for (var i = 0; i < 5; i++)
            images[i].src = "images/r" + initialState[i] + ".gif";
    }
}

function setRating(ratingDivId, mashupid, value) {
    if (userLoggedOn) {
        new Ajax.Request("ajax_rating.jsp?path=" + mashupid + "&ratingDivId=" + ratingDivId, {
            method: "post",
            parameters: {
                "rating" : value
            },
            onFailure: function (transport) {
                alert("Trouble contacting WSO2 Mashup Server ajax service.  Please try again later.");
            },
            onSuccess: function (transport) {
                $(ratingDivId).replace(transport.responseText);
            },
            onComplete: function (transport) {
                // Keep all mashups appearing on a single page in sync.  Edit one, update every.
                // Find any items that have the same class name (identifying a particular mashup).
                $$('div.' + $(ratingDivId).className).each(function(item) {
                    // If this isn't the view we just adjusted, have it update itself.
                    if (item.id != ratingDivId) {
                        getInitialRating(item.id, mashupid);
                    }
                });
            }
        });
    }
}


function getInitialRating(ratingDivId, mashupid) {
    new Ajax.Request("ajax_rating.jsp?path=" + mashupid + "&ratingDivId=" + ratingDivId, {
        method: "post",
        parameters: {
            "rating" : 0
        },
        onSuccess: function (transport) {
            $(ratingDivId).replace(transport.responseText);
        },
        onFailure: function (transport) {
            //alert("Trouble contacting WSO2 Mashup Server ajax service.  Please try again later.");
            //Retrying the request.
            getInitialRating(ratingDivId, mashupid);
        }
    });
}


/*
 * initInput : Call upon page load to make sure the style corresponds to the
 *             presence or absence of a hint.
 *
 *     inputId : The id of an input area to examine.
 *     hint : The hint to add if the input is empty.
 */
function initInput(inputId, hint) {
    var thisInput = document.getElementById(inputId);
    if (thisInput.className == "emptyfield") {
        if (thisInput.value == '')
            thisInput.value = hint;
        else if (thisInput.value != hint)
            thisInput.className = 'nonemptyfield';
    }
}

/*
 * prepareInput : A user is about to type into an empty field.  Clear out
 *                the hint.
 *
 *     e: event triggering this call.
 */
function prepareInput(e) {
    var thisInput = sourceElement(e);
    if (thisInput.className == "emptyfield") {
        thisInput.value = "";
        thisInput.className = "nonemptyfield";
    }
}

/*
 * restoreInput : A user has finished typing into an empty field.  If he
 *                left it empty, restore the hint.
 *
 *     e: event triggering this call.
 *     hint: the hint text
 */
function restoreInput(e, hint) {
    var thisInput = sourceElement(e);
    if (thisInput.value == "") {
        thisInput.value = hint;
        thisInput.className = "emptyfield";
    }
}

/*
 * sourceElement: Cross-browser function for determining the source element of
 *                an event.
 *
 *     e: event triggering this call.
 */
function sourceElement(e) {
    if (Prototype.Browser.IE) {
        return window.event.srcElement;
    } else {
        var node = e.target;
        while (node.nodeType != 1)
            node = node.parentNode;
        return node;
    }
}

// Dummy function for the data service wizard's cancel button
function startServices() {
    window.location = "index.jsp"
}
