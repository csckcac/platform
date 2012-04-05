goog.provide('gadgetide.uielement.JQBar');

goog.require('gadgetide.uielement.JQBarRenderer');
goog.require('gadgetide.uielement.UIElement');


/**
 * @constructor
 * @extends {gadgetide.uielement.UIElement}
 */
gadgetide.uielement.JQBar = function() {
  gadgetide.uielement.UIElement.call(this, '',
    new gadgetide.uielement.JQBarRenderer());
};
goog.inherits(gadgetide.uielement.JQBar, gadgetide.uielement.UIElement);

gadgetide.uielement.JQBar.prototype.setPlot = function(plot) {
  this.plot_ = plot;
};

/**
 * @private
 * @type {jqplot.Plot}
 */
gadgetide.uielement.JQBar.prototype.plot_;

/**
 * @inheritDoc
 */
gadgetide.uielement.JQBar.prototype.setSize = function(size) {
  this.plot_.replot();
};

/**
 * by default delegate to the render.
 * @param {Object} input
 */
gadgetide.uielement.JQBar.prototype.updateUiData = function(input) {
  if (this.plot_ && input) {
    var labels = /** @type {Array.<string>}*/
      gadgetide.schema.getValue(input['data']['labels']);
    var values = gadgetide.schema.getValue(input['data']['values']);
    var valuePairs = [];
    for (var i = 0; i < values.length; i++) {
      var value = values[i];
      // array's first element is 1-indexed;
      // see : https://groups.google.com/group/jqplot-users/browse_thread/thread/59df82899617242b/77fe0972f88aef6d%3Fq%3D%2522Groups.%2BCom%2522%2377fe0972f88aef6d&ei=iGwTS6eaOpW8Qpmqic0O&sa=t&ct=res&cd=71&source=groups&usg=AFQjCNHotAa6Z5CIi_-BGTHr_k766ZXXLQ?hl=en&pli=1
      valuePairs.push([i + 1,Number(value)]);
    }
    // WORKAROUND : jqplot seems to wrongly calculate the bar width in generated
    // gadget, this resets it.
    this.plot_.series[0].barWidth = null;
    this.plot_.series[0].data = valuePairs;
    this.plot_.axes['xaxis'].ticks = labels;
    // TODO: can optimize this more for the generated gadget case because size
    // is fixed.
    this.plot_.replot({'clear':true,'resetAxes':true});
  }
};

