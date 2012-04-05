goog.provide('gadgetide.dataunit.JQLine');

goog.require('gadgetide.datasource.DataSource');

/**
 * @param {!Object} config
 * @extends {gadgetide.datasource.DataSource}
 * @constructor
 */
gadgetide.dataunit.JQLine = function(config) {
};

/** @inheritDoc */
gadgetide.dataunit.JQLine.prototype.execute =
  function(callback, input, context) {
    callback(null);
  };

/** @inheritDoc */
gadgetide.dataunit.JQLine.prototype.loadInputFormat = function(callback) {
  callback({'number': {'$': '?'}});
};

/** @inheritDoc */
gadgetide.dataunit.JQLine.prototype.loadOutputFormat =
  function(callback, inputFormat) {
    callback(null);
  };


