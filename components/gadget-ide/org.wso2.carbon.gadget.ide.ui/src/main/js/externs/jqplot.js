/**
 * @fileoverview Externs for jqPlot Charts version 1.0
 *
 * @see http://www.jqplot.com/
 * @externs
 */

/**
 * (fake) namespace for jqPlot
 */
var jqplot = {};

/**
 * @constructor
 */
jqplot.Plot = function(){};

/**
 *
 * @param {{clear:boolean,resetAxes:boolean}=} opt_options
 */
jqplot.Plot.prototype.replot = function(opt_options){};

/**
 * @type {Array.<jqplot.Series>}
 */
jqplot.Plot.prototype.series;

/**
 * @type {Object.<jqplot.Axes>}
 */
jqplot.Plot.prototype.axes;



/**
 * @constructor
 */
jqplot.Axes = function(){};

/**
 * @type {Array.<string>}
 */
jqplot.Axes.prototype.ticks;






/**
 * @constructor
 */
jqplot.Series = function(){};

/**
 * @type {Array.<Array.<number>>}
 */
jqplot.Series.prototype.data;

/**
 * @type {?number}
 */
jqplot.Series.prototype.barWidth;



