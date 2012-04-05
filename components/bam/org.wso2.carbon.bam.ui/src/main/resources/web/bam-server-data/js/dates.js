//variables to keep the date interval start and end
var startHr = null;
var endHr = null;

var startDay = null;
var endDay = null;

var startMonth = null;
var endMonth = null;

var startEndHrState = "init";   // this varialbe is use to keep the state of the start and end time stamp of the hr selection range.
var d = new Date();
var currentTimestamp = d.getTime();
// startSet, endSet
var oneDay = 1000 * 60 * 60 * 24;
var oneHour = 1000 * 60 * 60;
var pageMode = 'hour'; //This is a flag set to keep the page mode (hour,day,or month);
var attributes = "";
//var firstStart = true; // user to hide the date selection box in the first start

var chartWidthToSend = 0;
var m_names = new Array("Jan", "Feb", "Mar",
                "Apr", "May", "Jun", "Jul", "Aug", "Sep",
                "Oct", "Nov", "Dec");
(function() {

    /**
     * IntervalCalendar is an extension of the CalendarGroup designed specifically
     * for the selection of an interval of dates.
     *
     * @namespace YAHOO.example.calendar
     * @module calendar
     * @since 2.5.2
     * @requires yahoo, dom, event, calendar
     */

    /**
     * IntervalCalendar is an extension of the CalendarGroup designed specifically
     * for the selection of an interval of dates, as opposed to a single date or
     * an arbitrary collection of dates.
     * <p>
     * <b>Note:</b> When using IntervalCalendar, dates should not be selected or
     * deselected using the 'selected' configuration property or any of the
     * CalendarGroup select/deselect methods. Doing so will corrupt the internal
     * state of the control. Instead, use the provided methods setInterval and
     * resetInterval.
     * </p>
     * <p>
     * Similarly, when handling select/deselect/etc. events, do not use the
     * dates passed in the arguments to attempt to keep track of the currently
     * selected interval. Instead, use getInterval.
     * </p>
     *
     * @namespace YAHOO.example.calendar
     * @class IntervalCalendar
     * @extends YAHOO.widget.CalendarGroup
     * @constructor
     * @param {String | HTMLElement} container The id of, or reference to, an HTML DIV element which will contain the control.
     * @param {Object} cfg optional The initial configuration options for the control.
     */
    function IntervalCalendar(container, cfg) {
        /**
         * The interval state, which counts the number of interval endpoints that have
         * been selected (0 to 2).
         *
         * @private
         * @type Number
         */
        this._iState = 0;

        // Must be a multi-select CalendarGroup
        cfg = cfg || {};
        cfg.multi_select = true;

        // Call parent constructor
        IntervalCalendar.superclass.constructor.call(this, container, cfg);

        // Subscribe internal event handlers
        this.beforeSelectEvent.subscribe(this._intervalOnBeforeSelect, this, true);
        this.selectEvent.subscribe(this._intervalOnSelect, this, true);
        this.beforeDeselectEvent.subscribe(this._intervalOnBeforeDeselect, this, true);
        this.deselectEvent.subscribe(this._intervalOnDeselect, this, true);
    }

    /**
     * Default configuration parameters.
     *
     * @property IntervalCalendar._DEFAULT_CONFIG
     * @final
     * @static
     * @private
     * @type Object
     */
    IntervalCalendar._DEFAULT_CONFIG = YAHOO.widget.CalendarGroup._DEFAULT_CONFIG;

    YAHOO.lang.extend(IntervalCalendar, YAHOO.widget.CalendarGroup, {

        /**
         * Returns a string representation of a date which takes into account
         * relevant localization settings and is suitable for use with
         * YAHOO.widget.CalendarGroup and YAHOO.widget.Calendar methods.
         *
         * @method _dateString
         * @private
         * @param {Date} d The JavaScript Date object of which to obtain a string representation.
         * @return {String} The string representation of the JavaScript Date object.
         */
        _dateString : function(d) {
            var a = [];
            a[this.cfg.getProperty(IntervalCalendar._DEFAULT_CONFIG.MDY_MONTH_POSITION.key) - 1] = (d.getMonth() + 1);
            a[this.cfg.getProperty(IntervalCalendar._DEFAULT_CONFIG.MDY_DAY_POSITION.key) - 1] = d.getDate();
            a[this.cfg.getProperty(IntervalCalendar._DEFAULT_CONFIG.MDY_YEAR_POSITION.key) - 1] = d.getFullYear();
            var s = this.cfg.getProperty(IntervalCalendar._DEFAULT_CONFIG.DATE_FIELD_DELIMITER.key);
            return a.join(s);
        },

        /**
         * Given a lower and upper date, returns a string representing the interval
         * of dates between and including them, which takes into account relevant
         * localization settings and is suitable for use with
         * YAHOO.widget.CalendarGroup and YAHOO.widget.Calendar methods.
         * <p>
         * <b>Note:</b> No internal checking is done to ensure that the lower date
         * is in fact less than or equal to the upper date.
         * </p>
         *
         * @method _dateIntervalString
         * @private
         * @param {Date} l The lower date of the interval, as a JavaScript Date object.
         * @param {Date} u The upper date of the interval, as a JavaScript Date object.
         * @return {String} The string representing the interval of dates between and
         *                   including the lower and upper dates.
         */
        _dateIntervalString : function(l, u) {
            var s = this.cfg.getProperty(IntervalCalendar._DEFAULT_CONFIG.DATE_RANGE_DELIMITER.key);
            return (this._dateString(l)
                    + s + this._dateString(u));
        },

        /**
         * Returns the lower and upper dates of the currently selected interval, if an
         * interval is selected.
         *
         * @method getInterval
         * @return {Array} An empty array if no interval is selected; otherwise an array
         *                 consisting of two JavaScript Date objects, the first being the
         *                 lower date of the interval and the second being the upper date.
         */
        getInterval : function() {
            // Get selected dates
            var dates = this.getSelectedDates();
            if (dates.length > 0) {
                // Return lower and upper date in array
                var l = dates[0];
                var u = dates[dates.length - 1];
                return [l, u];
            }
            else {
                // No dates selected, return empty array
                return [];
            }
        },

        /**
         * Sets the currently selected interval by specifying the lower and upper
         * dates of the interval (in either order).
         * <p>
         * <b>Note:</b> The render method must be called after setting the interval
         * for any changes to be seen.
         * </p>
         *
         * @method setInterval
         * @param {Date} d1 A JavaScript Date object.
         * @param {Date} d2 A JavaScript Date object.
         */
        setInterval : function(d1, d2) {
            // Determine lower and upper dates
            var b = (d1 <= d2);
            var l = b ? d1 : d2;
            var u = b ? d2 : d1;
            // Update configuration
            this.cfg.setProperty('selected', this._dateIntervalString(l, u), false);
            this._iState = 2;
        },

        /**
         * Resets the currently selected interval.
         * <p>
         * <b>Note:</b> The render method must be called after resetting the interval
         * for any changes to be seen.
         * </p>
         *
         * @method resetInterval
         */
        resetInterval : function() {
            // Update configuration
            this.cfg.setProperty('selected', [], false);
            this._iState = 0;
        },

        /**
         * Handles beforeSelect event.
         *
         * @method _intervalOnBeforeSelect
         * @private
         */
        _intervalOnBeforeSelect : function(t, a, o) {
            // Update interval state
            this._iState = (this._iState + 1) % 3;
            if (this._iState == 0) {
                // If starting over with upcoming selection, first deselect all
                this.deselectAll();
                this._iState++;
            }
        },

        /**
         * Handles selectEvent event.
         *
         * @method _intervalOnSelect
         * @private
         */
        _intervalOnSelect : function(t, a, o) {
            // Get selected dates
            var dates = this.getSelectedDates();
            if (dates.length > 1) {
                /* If more than one date is selected, ensure that the entire interval
                 between and including them is selected */
                var l = dates[0];
                var u = dates[dates.length - 1];
                this.cfg.setProperty('selected', this._dateIntervalString(l, u), false);
            }
            // Render changes
            this.render();
        },

        /**
         * Handles beforeDeselect event.
         *
         * @method _intervalOnBeforeDeselect
         * @private
         */
        _intervalOnBeforeDeselect : function(t, a, o) {
            if (this._iState != 0) {
                /* If part of an interval is already selected, then swallow up
                 this event because it is superfluous (see _intervalOnDeselect) */
                return false;
            }
        },

        /**
         * Handles deselectEvent event.
         *
         * @method _intervalOnDeselect
         * @private
         */
        _intervalOnDeselect : function(t, a, o) {
            if (this._iState != 0) {
                // If part of an interval is already selected, then first deselect all
                this._iState = 0;
                this.deselectAll();

                // Get individual date deselected and page containing it
                var d = a[0][0];
                var date = YAHOO.widget.DateMath.getDate(d[0], d[1] - 1, d[2]);
                var page = this.getCalendarPage(date);
                if (page) {
                    // Now (re)select the individual date
                    page.beforeSelectEvent.fire();
                    this.cfg.setProperty('selected', this._dateString(date), false);
                    page.selectEvent.fire([d]);
                }
                // Swallow up since we called deselectAll above
                return false;
            }
        }
    });

    YAHOO.namespace("example.calendar");
    YAHOO.example.calendar.IntervalCalendar = IntervalCalendar;
})();
function toggleDateSelector() {
    var anim = "";
    var datesSelectionBox = document.getElementById('datesSelectionBox');
    var imgObj = document.getElementById('imgObj');
    if (datesSelectionBox.style.display == "none") {
        attributes = {
            opacity: { to: 1 },
            height: { to: 210 }
        };
        anim = new YAHOO.util.Anim('datesSelectionBox', attributes);
        datesSelectionBox.style.display = "";
        imgObj.src = "images/up.png";
    } else {
        attributes = {
            opacity: { to: 0 },
            height: { to: 0 }
        };
        anim = new YAHOO.util.Anim('datesSelectionBox', attributes);

        anim.onComplete.subscribe(function() {
            datesSelectionBox.style.display = "none";
        }, datesSelectionBox);
        imgObj.src = "images/down.png";
    }

    anim.duration = 0.3;
    anim.animate();
}
/*-----------------------------------------------------------------------------------------------------*/
/*Create houre range selector*/
function setHourRange(theli) {
    var inTxt = YAHOO.util.Dom.get("in"),outTxt = YAHOO.util.Dom.get("out"),dateDisplay=YAHOO.util.Dom.get("dateDisplay");
    var timestamp = theli.title;
    timestamp = parseInt(timestamp);
    var allDivs = document.getElementById("timeBox-main").getElementsByTagName("*");

    if (startEndHrState == "init") {
        startHr = timestamp;
        for (var i = 0; i < allDivs.length; i++) {
            YAHOO.util.Dom.removeClass(allDivs[i], 'selected');
        }
        YAHOO.util.Dom.addClass(theli, 'selected');
        startEndHrState = "startSet";
        //set the headers and the text boxes
        var d = new Date(timestamp);
        inTxt.value = (d.getMonth() + 1) + "/" + d.getDate() + "/" + d.getFullYear() + " - " + d.getHours()+":00";
        outTxt.value = '';
        var tmpString = getStringMonth(d.getMonth()) + ' ' + d.getDate() + ',' + d.getFullYear() + ' - <span class="hourStrong">' + d.getHours() + ':00</span>';
        dateDisplay.innerHTML = tmpString;

    } else if (startEndHrState == "endSet") {
        startHr = timestamp;
        for (var i = 0; i < allDivs.length; i++) {
            YAHOO.util.Dom.removeClass(allDivs[i], 'selected');
        }
        YAHOO.util.Dom.addClass(theli, 'selected');
        startEndHrState = "startSet";

        //set the headers and the text boxes
        var d = new Date(timestamp);
        inTxt.value = (d.getMonth() + 1) + "/" + d.getDate() + "/" + d.getFullYear() + " - " + d.getHours()+":00";
        outTxt.value = '';
        var tmpString = getStringMonth(d.getMonth()) + ' ' + d.getDate() + ',' + d.getFullYear() + ' - <span class="hourStrong">' + d.getHours() + ':00</span>';
        dateDisplay.innerHTML = tmpString;

    } else if (startEndHrState == "startSet") {
        endHr = timestamp;
        if (startHr > endHr) {//Swap if the end time is smaller than start time
            var tmp = endHr;
            endHr = startHr;
            startHr = tmp;
        }
        for (var i = 0; i < allDivs.length; i++) {
            if (allDivs[i].title <= endHr && allDivs[i].title >= startHr) {
                YAHOO.util.Dom.addClass(allDivs[i], 'selected');
            }
            else {
                YAHOO.util.Dom.removeClass(allDivs[i], 'selected');
            }
        }
        startEndHrState = "endSet";

        //set the headers and the text boxes
        var dStart = new Date(startHr);
        var dEnd = new Date(endHr);
        inTxt.value = (dStart.getMonth() + 1) + "/" + dStart.getDate() + "/" + dStart.getFullYear() + " - " + dStart.getHours()+":00";
        outTxt.value = (dEnd.getMonth() + 1) + "/" + dEnd.getDate() + "/" + dEnd.getFullYear() + " - " + dEnd.getHours()+":00";
        var tmpString = getStringMonth(dStart.getMonth()) + ' ' + dStart.getDate() + ',' + dStart.getFullYear() + ' - <span class="hourStrong">' + dStart.getHours() + ':00</span>' +' -> ' +getStringMonth(dEnd.getMonth()) + ' ' + dEnd.getDate() + ',' + dEnd.getFullYear() + ' - <span class="hourStrong">' + dEnd.getHours() + ':00</span>';
        dateDisplay.innerHTML = tmpString;
    }
    set_cookie(startHr_cookie,startHr);
    set_cookie(endHr_cookie,endHr);
}
function genHourTable(timestamp) {
    YAHOO.util.Event.onAvailable("timeBox-content", function() {
        var timeBoxContent = document.getElementById('timeBox-content');
    var d = new Date(timestamp);
    var externDiv = document.createElement("DIV");
    YAHOO.util.Dom.addClass(externDiv, 'timeBox-sub');
    var insideStr = '<div class="date-title">' + getStringMonth(d.getMonth()) + ' ' + d.getDate() + ',' + d.getFullYear() + '</div>' +
                    '<div class="timeBox-Wrapper">' +
                    '<ul>';
    for (var i = 0; i <= 23; i++) {
        insideStr += '<li title="' + (timestamp + i * oneHour) + '" onclick="setHourRange(this)">' + i + '</li>';
    }
    insideStr += '</ul></div>';
    externDiv.innerHTML = insideStr;

        timeBoxContent.appendChild(externDiv);
    });

}
function genTimeHours() {
    var clearToday = getClearTimestamp(currentTimestamp);


    //set the buttons
    var timeBoxMain = document.getElementById('timeBox-main');
    var navButtons = '<div class="navButtons yui-skin-sam">' +
                         '<table class="hourNavigation">' +
                         '<tbody>' +
                             '<tr>' +
                             '<td><a onclick="navHour(-oneDay)" title="Back one day"><img src="images/left.gif" /></a></td>' +
                             '<td><a onclick="navHour(-10*oneDay)" title="Back 10 days"><img src="images/left-fast.gif" /></a></td>' +
                             '<td><a onclick="navHour(-(currentTimestamp -getPrevMonth(currentTimestamp)))" title="Back one month"><img src="images/left-mo.gif" /></a></td>' +
                             '<td class="hourControl-cell"><a onclick="javascript:currentTimestamp = d.getTime();navHour(0)" title="Today"><img src="images/stop.gif" /></a></td>' +
                             '<td><a onclick="navHour(getNextMonth(currentTimestamp)-currentTimestamp)" title="Forward one month"><img src="images/right-mo.gif" /></a></td>' +
                             '<td><a onclick="navHour(10*oneDay)" title="Forward 10 days"><img src="images/right-fast.gif" /></a></td>' +
                             '<td><a onclick="navHour(oneDay)" title="Forward 1 day"><img src="images/right.gif" /></a></td>' +
                             '</tr>' +
                         '</tbody>' +
                         '</table>' +
                     '</div>' +
                     '<div class="slider-time-show" id="slider_time_show"></div><div id="timeBox-content"></div>';
    var navButtonDiv = document.createElement("DIV");
    navButtonDiv.innerHTML = navButtons;
    timeBoxMain.innerHTML = "";
    timeBoxMain.appendChild(navButtonDiv);

    genHourTable(clearToday - oneDay * 2);
    genHourTable(clearToday - oneDay);
    genHourTable(clearToday);
}
function navHour(extraTimeStamp) {

    currentTimestamp += extraTimeStamp;

    YAHOO.util.Event.onAvailable("timeBox-content", function() {
        document.getElementById('timeBox-content').innerHTML = "";
    });
    var clearToday = getClearTimestamp(currentTimestamp);
    genHourTable(clearToday - oneDay * 2);
    genHourTable(clearToday - oneDay);
    genHourTable(clearToday);

    var allDivs = document.getElementById("timeBox-main").getElementsByTagName("*");
    if (startEndHrState == "startSet") {
        for (var i = 0; i < allDivs.length; i++) {
            if (allDivs[i].title == startHr) {
                YAHOO.util.Dom.addClass(allDivs[i], 'selected');
            }
        }
    } else if (startEndHrState == "endSet") {
        for (var i = 0; i < allDivs.length; i++) {
            if (allDivs[i].title <= endHr && allDivs[i].title >= startHr) {
                YAHOO.util.Dom.addClass(allDivs[i], 'selected');
            }
            else {
                YAHOO.util.Dom.removeClass(allDivs[i], 'selected');
            }
        }
    }

}
/* --------------------------------------------------------*/
/*Create Monthe range selector*/
function setMonthRange(theli) {
    var inTxt = YAHOO.util.Dom.get("in"),outTxt = YAHOO.util.Dom.get("out"),dateDisplay=YAHOO.util.Dom.get("dateDisplay");
    var timestamp = theli.title;
    timestamp = parseInt(timestamp);
    var allDivs = document.getElementById("monthBox-main").getElementsByTagName("*");

    if (startEndHrState == "init") {
        startMonth = timestamp;
        for (var i = 0; i < allDivs.length; i++) {
            YAHOO.util.Dom.removeClass(allDivs[i], 'selected');
        }
        YAHOO.util.Dom.addClass(theli, 'selected');
        startEndHrState = "startSet";
        //set the headers and the text boxes
        var d = new Date(timestamp);
        inTxt.value = (d.getMonth() + 1) + "/" + d.getDate() + "/" + d.getFullYear();
        outTxt.value = '';
        var tmpString = getStringMonth(d.getMonth()) + ' ' + d.getDate() + ',' + d.getFullYear();
        dateDisplay.innerHTML = tmpString;

    } else if (startEndHrState == "endSet") {
        startMonth = timestamp;
        for (var i = 0; i < allDivs.length; i++) {
            YAHOO.util.Dom.removeClass(allDivs[i], 'selected');
        }
        YAHOO.util.Dom.addClass(theli, 'selected');
        startEndHrState = "startSet";

        //set the headers and the text boxes
        var d = new Date(timestamp);
        inTxt.value = (d.getMonth() + 1) + "/" + d.getDate() + "/" + d.getFullYear();
        outTxt.value = '';
        var tmpString = getStringMonth(d.getMonth()) + ' ' + d.getDate() + ',' + d.getFullYear() ;
        dateDisplay.innerHTML = tmpString;

    } else if (startEndHrState == "startSet") {
        endMonth = timestamp;
        if (startMonth > endMonth) {//Swap if the end time is smaller than start time
            var tmp = endMonth;
            endMonth = startMonth;
            startMonth = tmp;
        }
        for (var i = 0; i < allDivs.length; i++) {
            if (allDivs[i].title <= endMonth && allDivs[i].title >= startMonth) {
                YAHOO.util.Dom.addClass(allDivs[i], 'selected');
            }
            else {
                YAHOO.util.Dom.removeClass(allDivs[i], 'selected');
            }
        }
        startEndHrState = "endSet";

        //set the headers and the text boxes
        var dStart = new Date(startMonth);
        var dEnd = new Date(endMonth);
        inTxt.value = (dStart.getMonth() + 1) + "/" + dStart.getDate() + "/" + dStart.getFullYear();
        outTxt.value = (dEnd.getMonth() + 1) + "/" + dEnd.getDate() + "/" + dEnd.getFullYear();
        var tmpString = getStringMonth(dStart.getMonth()) + ' ' + dStart.getDate() + ',' + dStart.getFullYear() + ' -> ' + getStringMonth(dEnd.getMonth()) + ' ' + dEnd.getDate() + ',' + dEnd.getFullYear();
        dateDisplay.innerHTML = tmpString;
    }
    set_cookie(startMonth_cookie,startMonth);
    set_cookie(endMonth_cookie,endMonth);
}
function genMonthTable(timestamp) {
    var timeBoxMain = document.getElementById('monthBox-main');
    var d = new Date(timestamp);
    var externDiv = document.createElement("DIV");
    YAHOO.util.Dom.addClass(externDiv, 'monthBox-sub');
    var insideStr = '<div class="date-title">' + d.getFullYear() + '</div>' +
                    '<div class="monthBox-Wrapper">' +
                    '<ul>';
    var iTime = timestamp;
    for (var i = 0; i < m_names.length; i++) {
        insideStr += '<li title="' + iTime + '" onclick="setMonthRange(this)">' + m_names[i] + '</li>';
        iTime = getNextMonth(iTime);
    }
    insideStr += '</ul></div>';
    externDiv.innerHTML = insideStr;

    timeBoxMain.appendChild(externDiv);
}
function genTimeMonths() {
    //set the buttons
    var timeBoxMain = document.getElementById('monthBox-main');
    var navButtons = '<div class="navButtons"><a class="left" onclick="navMonth(\'left\')"><<</a><a class="right" onclick="navMonth(\'right\')">>></a></div>';
    var navButtonDiv = document.createElement("DIV");
    navButtonDiv.innerHTML = navButtons;
    timeBoxMain.innerHTML = "";
    timeBoxMain.appendChild(navButtonDiv);
    var jan1st = new Date((new Date(currentTimestamp)).getFullYear(),0,1);
    genMonthTable(getPrevYear(currentTimestamp)); 
    genMonthTable(jan1st.getTime());
}
function navMonth(direction) {
    if (direction == "left") {
        currentTimestamp = getPrevYear(currentTimestamp);
    } else if (direction == "right") {
        currentTimestamp = getNextYear(currentTimestamp);
    }
    genTimeMonths();
    var allDivs = document.getElementById("monthBox-main").getElementsByTagName("*");
    if (startEndHrState == "startSet") {
        for (var i = 0; i < allDivs.length; i++) {
            if (allDivs[i].title == startHr) {
                YAHOO.util.Dom.addClass(allDivs[i], 'selected');
            }
        }
    } else if (startEndHrState == "endSet") {
        for (var i = 0; i < allDivs.length; i++) {
            if (allDivs[i].title <= endHr && allDivs[i].title >= startHr) {
                YAHOO.util.Dom.addClass(allDivs[i], 'selected');
            }
            else {
                YAHOO.util.Dom.removeClass(allDivs[i], 'selected');
            }
        }
    }

}


function setPageMode(mode, clickedObj) {
    var d = new Date();
    pageMode = mode;
    set_cookie(pageMode_cookie,pageMode);
    var dateDisplay = YAHOO.util.Dom.get("dateDisplay");
    var allObjs = document.getElementById("datesTypes").getElementsByTagName("*");
    for (var i = 0; i < allObjs.length; i++) {
        if (YAHOO.util.Dom.hasClass(allObjs[i], "sel-left")) {
            YAHOO.util.Dom.removeClass(allObjs[i], "sel-left");
            YAHOO.util.Dom.addClass(allObjs[i], "nor-left");
        }
        if (YAHOO.util.Dom.hasClass(allObjs[i], "sel-right")) {
            YAHOO.util.Dom.removeClass(allObjs[i], "sel-right");
            YAHOO.util.Dom.addClass(allObjs[i], "nor-right");
        }
        if (YAHOO.util.Dom.hasClass(allObjs[i], "sel-rep")) {
            YAHOO.util.Dom.removeClass(allObjs[i], "sel-rep");
            YAHOO.util.Dom.addClass(allObjs[i], "nor-rep");
        }
    }
    var timeBoxMain = document.getElementById('timeBox-main');
    var cal1Container = document.getElementById('cal1Container');
    var monthBoxMain = document.getElementById('monthBox-main');
    gotoInitMode();            

    //get cookie variables
    get_date_cookies();
     //Set the time ranges
    var time,timeEarly;
    if (pageMode == 'hour') {
        timeBoxMain.style.display = '';
        cal1Container.style.display = 'none';
        monthBoxMain.style.display = 'none';
        YAHOO.util.Dom.removeClass(clickedObj, "nor-left");
        YAHOO.util.Dom.addClass(clickedObj, "sel-left");
        if (startEndHrState == 'init') {
            //Set the time ranges
            if (startHr == null) {
                time = d;
                startHr = time.getTime();
            } else {
                time = new Date(startHr);
            }
            if (endHr == null) {
                timeEarly = new Date(d.getTime() - oneHour * 24);
                endHr = timeEarly.getTime();
            } else {
                timeEarly = new Date(endHr);
            }
            var tmpString = getStringMonth(timeEarly.getMonth()) + ' ' + timeEarly.getDate() + ',' + timeEarly.getFullYear() + ' - <span class="hourStrong">' + timeEarly.getHours() + ':00</span>';
            tmpString += ' -> ' + getStringMonth(time.getMonth()) + ' ' + time.getDate() + ',' + time.getFullYear() + ' - <span class="hourStrong">' + time.getHours() + ':00</span>';
            dateDisplay.innerHTML = tmpString;
        }
        updatePage();
    }
    if (pageMode == 'day') {
        d = new Date(d.getFullYear(),d.getMonth(),d.getDate(),0,0,0);
        timeBoxMain.style.display = 'none';
        monthBoxMain.style.display = 'none';
        cal1Container.style.display = '';
        YAHOO.util.Dom.removeClass(clickedObj, "nor-rep");
        YAHOO.util.Dom.addClass(clickedObj, "sel-rep");
        if (startEndHrState == 'init') {
            if (startDay == null) {
                time = d;
                startDay = time.getTime();
            } else {
                time = new Date(startDay);
            }
            if (endDay == null) {
                timeEarly = new Date(d.getTime() - 30*oneDay); //Get the yesterdays midnight
                endDay = timeEarly.getTime();
            } else {
                timeEarly = new Date(endDay);
            }

            var tmpString = getStringMonth(timeEarly.getMonth()) + ' ' + timeEarly.getDate() + ',' + timeEarly.getFullYear();
            tmpString += ' -> ' + getStringMonth(time.getMonth()) + ' ' + time.getDate() + ',' + time.getFullYear();
            dateDisplay.innerHTML = tmpString;

        }
        updatePage();
    }
    if (pageMode == 'month') {
        d = new Date(d.getFullYear(),d.getMonth(),1,0,0,0);
        timeBoxMain.style.display = 'none';
        monthBoxMain.style.display = '';
        cal1Container.style.display = 'none';
        YAHOO.util.Dom.removeClass(clickedObj, "nor-right");
        YAHOO.util.Dom.addClass(clickedObj, "sel-right");
        if (startEndHrState == 'init') {
            if (startMonth == null) {
                time = d;
                startMonth = time.getTime();
            } else {
                time = new Date(startMonth);
            }
            if (endMonth == null) {
                timeEarly = new Date(getPrevMonth(getPrevMonth(getPrevMonth(d.getTime())))); //Get the prev month
                endMonth = timeEarly.getTime();
            } else {
                timeEarly = new Date(endMonth);
            }

            var tmpString = getStringMonth(timeEarly.getMonth()) + ' ' + timeEarly.getDate() + ',' + timeEarly.getFullYear();
            tmpString += ' -> ' + getStringMonth(time.getMonth()) + ' ' + time.getDate() + ',' + time.getFullYear();
            dateDisplay.innerHTML = tmpString;
                   }
        updatePage();
    }
}
function gotoInitMode(){
    var allDivs1 = document.getElementById("timeBox-main").getElementsByTagName("*");
    var allDivs2 = document.getElementById("monthBox-main").getElementsByTagName("*");

    for (i = 0; i < allDivs1.length; i++) {
        YAHOO.util.Dom.removeClass(allDivs1[i], 'selected');
    }
    for (i = 0; i < allDivs2.length; i++) {
        YAHOO.util.Dom.removeClass(allDivs2[i], 'selected');
    }
    startEndHrState = "init";
}
//util function
function getStringMonth(num) {
    var m_names = new Array("January", "February", "March",
            "April", "May", "June", "July", "August", "September",
            "October", "November", "December");

    return m_names[num];
}
function getClearTimestamp(timestamp) {
    var d = new Date(timestamp);
    var dateClear = new Date(Date.UTC(d.getFullYear(), d.getMonth(), d.getDate()));
    return (dateClear.getTime()+d.getTimezoneOffset()*1000 * 60);
}
function createTabs(type) {
    //Create a TabView
    var tabView = new YAHOO.widget.TabView();

    //Add a tab for the Line Chart
    tabView.addTab(new YAHOO.widget.Tab({
        label: 'Line Chart',
        content: '<div id="chartline' + type + '"><div class="loadingPosition"><img align="top" src="images/ajax-loader.gif"/></div></div>',
        active: true
    }));

    //Add a tab for the Bar Chart
    tabView.addTab(new YAHOO.widget.Tab({
        label: 'Bar Chart',
        content: '<div id="chartbar' + type + '"><div class="loadingPosition"><img align="top" src="images/ajax-loader.gif"/></div></div>'
    }));

    //Add a tab for the Column Chart
    tabView.addTab(new YAHOO.widget.Tab({
        label: 'Column Chart',
        content: '<div id="chartcolumn' + type + '"><div class="loadingPosition"><img align="top" src="images/ajax-loader.gif"/></div></div>'
    }));
    document.getElementById('tabContainer_'+type).innerHTML = "";
    //Append TabView to its container div
    tabView.appendTo('tabContainer_' + type);
}
function formatDate(inDate) {
	 var year = inDate.split("T")[0].split("-")[0];
     var month = inDate.split("T")[0].split("-")[1];
     var day = inDate.split("T")[0].split("-")[2];
     var hour = inDate.split("T")[1].split(":")[0];

        return m_names[month - 1] + " " + day + "-" + hour +":00";
    }
function getNextMonth(timestamp){
    now = new Date(timestamp);
    var current;
    if (now.getMonth() == 11) {
        current = new Date(now.getFullYear() + 1, 0, 1);
    } else {
        current = new Date(now.getFullYear(), now.getMonth() + 1, 1);
    }
    return current.getTime();

}
function getPrevMonth(timestamp){
    now = new Date(timestamp);
    var current;
    if (now.getMonth() == 0) {
        current = new Date(now.getFullYear() - 1, 11, 1);
    } else {
        current = new Date(now.getFullYear(), now.getMonth() - 1, 1);
    }
    return current.getTime();

}
function getNextYear(timestamp){
    now = new Date(timestamp);
    var current;
    current = new Date(now.getFullYear() + 1, 0, 1);
    return current.getTime();
}
function getPrevYear(timestamp){
    now = new Date(timestamp);
    var current;
    current = new Date(now.getFullYear() - 1, 0, 1);
    return current.getTime();
}
function init() {
    var chartWidth = YAHOO.util.Dom.getViewportWidth() - 580;
    var styleBlock = document.getElementById('chartSizes');
    styleBlock.innerHTML = '<style type="text/css">' +
                           '#chartlinecount {width: ' + chartWidth + 'px;height: 350px;}' +
                           '#chartlinetime {width: ' + chartWidth + 'px;height: 350px;}' +
                           '#chartbarcount {width: ' + chartWidth + 'px;height: 350px;}' +
                           '#chartbartime {width: ' + chartWidth + 'px;height: 350px;}' +
                           '#chartcolumncount {width: ' + chartWidth + 'px;height: 350px;}' +
                           '#chartcolumntime {width: ' + chartWidth + 'px;height: 350px;}' +
                           '</style>';
    chartWidthToSend = chartWidth;

    var d = new Date();
    var dateDisplay = YAHOO.util.Dom.get("dateDisplay");
    var inTxtTop = getStringMonth(d.getMonth()) + ' ' + d.getDate() + ',' + d.getFullYear();
    var outTxtTop = "";
    var inTxt = YAHOO.util.Dom.get("in"),
            outTxt = YAHOO.util.Dom.get("out");
    var inDate = null, outDate = null, interval = null;

    //assigning cookie values to global variables
    if (get_cookie(pageMode_cookie) != null) {
        pageMode = get_cookie(pageMode_cookie);
    }
    get_date_cookies();

    inTxt.value = "";
    outTxt.value = "";
    var cal = new YAHOO.example.calendar.IntervalCalendar("cal1Container", {pages:3,width:60});

    cal.selectEvent.subscribe(function() {
        interval = this.getInterval();

        if (interval.length == 2) {
            inDate = interval[0];
            inTxt.value = (inDate.getMonth() + 1) + "/" + inDate.getDate() + "/" + inDate.getFullYear();
            inTxtTop = getStringMonth(inDate.getMonth()) + ' ' + inDate.getDate() + ',' + inDate.getFullYear();
            startDay = inDate.getTime();
            set_cookie(startDay_cookie, startDay);
            if (interval[0].getTime() != interval[1].getTime()) {
                outDate = interval[1];
                outTxt.value = (outDate.getMonth() + 1) + "/" + outDate.getDate() + "/" + outDate.getFullYear();
                outTxtTop = getStringMonth(outDate.getMonth()) + ' ' + outDate.getDate() + ',' + outDate.getFullYear();
                endDay = outDate.getTime();
                //set_cookie(endDay_cookie, endDay);
            } else {
                outTxt.value = "";
                outTxtTop = "";
            }
        }
        dateDisplay.innerHTML = inTxtTop + " - " + outTxtTop;
    }, cal, true);

    cal.render();
    // create the hour selections
    genTimeHours();
    genTimeMonths();


    if(get_cookie(pageMode_cookie) == null){
        pageMode = "hour";
    }
    if(pageMode == "hour"){
        setPageMode(pageMode,document.getElementById("hourLink"));
    }else if(pageMode == "day"){
        setPageMode(pageMode,document.getElementById("dayLink"));
    }else if(pageMode == "month"){
        setPageMode(pageMode,document.getElementById("monthLink"));   
    }
    //Set the time ranges
   /* var time,timeEarly;
    if (startHr == null) {
        time = d;
    } else {
        time = new Date(startHr);
    }
    if (endHr == null) {
        timeEarly = new Date(d.getTime() - oneHour * 8);
    } else {
        timeEarly = new Date(endHr);
    }
    var tmpString = getStringMonth(timeEarly.getMonth()) + ' ' + timeEarly.getDate() + ',' + timeEarly.getFullYear() + ' - <span class="hourStrong">' + timeEarly.getHours() + ':00</span>';
    tmpString += ' -> ' + getStringMonth(time.getMonth()) + ' ' + time.getDate() + ',' + time.getFullYear() + ' - <span class="hourStrong">' + time.getHours() + ':00</span>';
    dateDisplay.innerHTML = tmpString;

    updatePage();*/
}
function genTime(dateStr) {
    var dsubs = dateStr.split('T');
    var year = dsubs[0].split("-")[0];
    var month = dsubs[0].split("-")[1];
    var day = dsubs[0].split("-")[2];

    var hour = dsubs[1].split(":")[0];
    var min = dsubs[1].split(":")[1];
    var d = new Date(year, month, day, hour, min);
    return d.getTime();
}
function compareTimestamps(a, b) {
    return a.timestmp - b.timestmp;
}
//function quicksort(m, n, desc) {
//    if (n <= m + 1) return;
//    if ((n - m) == 2) {
//        if (compare(get(n - 1), get(m), desc)) exchange(n - 1, m);
//        return;
//    }
//    i = m + 1;
//    j = n - 1;
//    if (compare(get(m), get(i), desc)) exchange(i, m);
//    if (compare(get(j), get(m), desc)) exchange(m, j);
//    if (compare(get(m), get(i), desc)) exchange(i, m);
//    pivot = get(m);
//    while (true) {
//        j--;
//        while (compare(pivot, get(j), desc)) j--;
//        i++;
//        while (compare(get(i), pivot, desc)) i++;
//        if (j <= i) break;
//        exchange(i, j);
//    }
//    exchange(m, j);
//    if ((j - m) < (n - j)) {
//        quicksort(m, j, desc);
//        quicksort(j + 1, n, desc);
//    } else {
//        quicksort(j + 1, n, desc);
//        quicksort(m, j, desc);
//    }
//}
/*function fixTimes() {
    var now = new Date();
    if (startEndHrState == "init") {
        if (pageMode == "hour") {
            now = getClearTimestamp(now.getTime());
            startHr = now - 8 * oneHour;
            endHr = now;
        } else if (pageMode == "day") {
            now = new Date(now.getFullYear(), now.getMonth(), now.getDate());
            startDay = now.getTime() - oneDay * 7;
            endDay = now.getTime();
        } else if (pageMode == "month") {
            now = new Date(now.getFullYear(), now.getMonth(), 1);
            startMonth = getPrevMonth(getPrevMonth(now.getTime()));
            endMonth = now.getTime();
        }
    }else if(startEndHrState == "startSet") {
        endHr = startHr;
        if (pageMode == "hour") {
            startHr = startHr - 8 * oneHour;
         } else if (pageMode == "day") {
            startHr = startHr - oneDay * 7;
        } else if (pageMode == "month") {
            startHr = getPrevMonth(getPrevMonth(startHr));
        }
    } else if(startEndHrState == "endSet") {
    }
}*/

