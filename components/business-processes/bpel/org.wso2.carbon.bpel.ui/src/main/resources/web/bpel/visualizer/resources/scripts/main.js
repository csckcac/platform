/*
   Copyright 2010 Gregor Latuske

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
 */

/*
 * Initialize reload function & slider
 */
function init() {
//	jsf.ajax.addOnEvent(createSlider);
	checkReloadInterval();
}

/*
 * Create the slider
 */
var createSlider = function createSlider(data) {
	if (data.status == "success") {
		fdSliderController.construct(null);
	}
};

/*
 * Slider value changed
 */
var pmGranularity = 0;
var piGranularity = 0;

function updateSlider() {
	var newPmGranularity = parseInt(document.getElementById("form:svg:pm:slider").value, 10) || 0;
	var newPiGranularity = parseInt(document.getElementById("form:svg:pi:slider").value, 10) || 0;

	if (newPmGranularity != pmGranularity || newPiGranularity != piGranularity) {
		pmGranularity = newPmGranularity;
		piGranularity = newPiGranularity;

		// Reload table
		//doReload();
	}
}

/*
 * Sets the focus to the input field
 */
function focusElement(data) {
	document.getElementById(data.source.id).focus();
}

/*
 * Scroll position
 */
var scrollPosition;

// Saves the current scroll position
function saveScrollPosition() {
	var x = 0, y = 0;

	// Netscape compliant
	if (typeof (window.pageYOffset) == 'number') {
		x = window.pageXOffset;
		y = window.pageYOffset;

		// DOM compliant
	} else if (document.body && (document.body.scrollLeft || document.body.scrollTop)) {
		x = document.body.scrollLeft;
		y = document.body.scrollTop;

		// IE6 standards compliant mode
	} else if (document.documentElement
			&& (document.documentElement.scrollLeft || document.documentElement.scrollTop)) {
		x = document.documentElement.scrollLeft;
		y = document.documentElement.scrollTop;
	}

	scrollPosition = [ x, y ];
}

// Restores scroll position
function restoreScrollPositionCallback(data) {
	if (data.status == "success") {
		setTimeout("restoreScrollPosition()", 500);
	}
}

// Restores scroll position
function restoreScrollPosition() {
	window.scrollTo(scrollPosition[0], scrollPosition[1]);
}

/*
 * Reload
 */
var reloadInterval = 60 * 1000;
var reloadTimestamp = getNextReloadTimestamp();

// Sets the the reload interval to the new value
function setReloadInterval(newReloadInterval) {
	reloadInterval = newReloadInterval * 1000;
	reloadTimestamp = getNextReloadTimestamp();
}

// Check if the current timestamp is small than the next reload timestamp
function checkReloadInterval() {
	if (getCurrentTimestamp() >= reloadTimestamp) {
		doReload();
	}

	setTimeout("checkReloadInterval()", 1000);
}

// Executing the reload of the SVG span
function doReload() {
	reloadTimestamp = getNextReloadTimestamp();
	document.form.submit();
}

// Returns the next reload timestamp
function getNextReloadTimestamp() {
	return getCurrentTimestamp() + reloadInterval;
}

// Returns the current timestamp
function getCurrentTimestamp() {
	return new Date().getTime();
}

window.onload=init
