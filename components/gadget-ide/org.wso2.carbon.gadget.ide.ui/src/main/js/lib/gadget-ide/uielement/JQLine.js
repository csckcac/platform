goog.provide('gadgetide.uielement.JQLine');

goog.require('gadgetide.uielement.JQLineRenderer');
goog.require('gadgetide.uielement.UIElement');


/**
 * @constructor
 * @extends {gadgetide.uielement.UIElement}
 */
gadgetide.uielement.JQLine = function() {
  gadgetide.uielement.UIElement.call(this, '',
    new gadgetide.uielement.JQLineRenderer());
};
goog.inherits(gadgetide.uielement.JQLine, gadgetide.uielement.UIElement);

gadgetide.uielement.JQLine.prototype.setPlot = function(plot) {
  this.maxDataLen = 20;
  this.plot_ = plot;
};

/**
 * @private
 * @type {jqplot.Plot}
 */
gadgetide.uielement.JQLine.prototype.plot_;

/**
 * @inheritDoc
 */
gadgetide.uielement.JQLine.prototype.setSize = function(size) {
  this.plot_.replot();
};

/**
 * by default delegate to the render.
 * @param {Object} input
 */
gadgetide.uielement.JQLine.prototype.updateUiData = function(input) {
  if (this.plot_ && input) {
    var n = Number(input['number']['$']);
    // array's first element is 1-indexed;
    // see : https://groups.google.com/group/jqplot-users/browse_thread/thread/59df82899617242b/77fe0972f88aef6d%3Fq%3D%2522Groups.%2BCom%2522%2377fe0972f88aef6d&ei=iGwTS6eaOpW8Qpmqic0O&sa=t&ct=res&cd=71&source=groups&usg=AFQjCNHotAa6Z5CIi_-BGTHr_k766ZXXLQ?hl=en&pli=1
    var arr = this.plot_.series[0].data;
    if (arr.length == this.maxDataLen) {
      for (var i = 1; i < arr.length; i++) {
        arr[i - 1][1] = arr[i][1];
      }
      arr[arr.length - 1][1] = n;
    } else {
      arr.push([arr.length + 1,n]);
    }
    this.plot_.replot({'clear':true,'resetAxes':true});
  }
};
//
//
// * @return {Array.<gadgetide.spec.Spec>}
// */
//gadgetide.uielement.JQLineRenderer.prototype.getSpec = function() {
//  if (this.spec_) {
//    /** @type {gadgetide.spec.Spec} */
//    var maxWindow = {
//      name:"Max Data Window",
//      type:gadgetide.spec.Types.INT,
//      defaultVal:1,
//      callback : function() {
//      }};
//
//    this.spec_ = [maxWindow];
//  }
//  return this.spec_;
//};

