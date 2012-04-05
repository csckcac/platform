goog.provide('gadgetide.uielement.FuelGauge');
goog.require('gadgetide.uielement.FuelGaugeRenderer');
goog.require('gadgetide.uielement.UIElement');


/**
 * @constructor
 * @extends {gadgetide.uielement.UIElement}
 */
gadgetide.uielement.FuelGauge = function() {
  gadgetide.uielement.UIElement.call(this, '',
    new gadgetide.uielement.FuelGaugeRenderer());
};
goog.inherits(gadgetide.uielement.FuelGauge, gadgetide.uielement.UIElement);


/**
 * set the Gauge for this Element.
 * @param {jsgauge.Gauge} gauge Gauge object.
 */
gadgetide.uielement.FuelGauge.prototype.setGauge = function(gauge) {
  this.gauge_ = gauge;
};


/**
 * @private
 * @type {jsgauge.Gauge}
 */
gadgetide.uielement.FuelGauge.prototype.gauge_;

/**
 * @inheritDoc
 */
gadgetide.uielement.FuelGauge.prototype.updateUiData = function(input) {
  if (this.gauge_ && input) {
    var n = Number(input['value']['$']);
    this.gauge_.setValue(n);
  }
};


