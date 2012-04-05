goog.provide('gadgetide.dataunit.FuelGauge');

goog.require('gadgetide.datasource.DataSource');

/**
 * @extends {gadgetide.datasource.DataSource}
 * @constructor
 */
gadgetide.dataunit.FuelGauge = function() {
};

/** @inheritDoc */
gadgetide.dataunit.FuelGauge.prototype.execute =
  function(callback, input, context) {
    callback(null);
  };

/** @inheritDoc */
gadgetide.dataunit.FuelGauge.prototype.loadInputFormat = function(callback) {
    callback({'value': {'$': '?'}});
};

/** @inheritDoc */
gadgetide.dataunit.FuelGauge.prototype.loadOutputFormat =
  function(callback, inputFormat) {
    callback(null);
  };
