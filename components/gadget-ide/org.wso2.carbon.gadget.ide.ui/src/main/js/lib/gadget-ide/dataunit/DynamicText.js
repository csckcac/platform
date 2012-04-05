goog.provide('gadgetide.dataunit.DynamicText');

goog.require('gadgetide.datasource.DataSource');

/**
 * @param {!Object} config
 * @extends {gadgetide.datasource.DataSource}
 * @constructor
 */
gadgetide.dataunit.DynamicText = function(config) {
};

/** @inheritDoc */
gadgetide.dataunit.DynamicText.prototype.execute =
  function(callback, input, context) {
    callback(null);
  };

/** @inheritDoc */
gadgetide.dataunit.DynamicText.prototype.loadInputFormat = function(callback) {

  callback({'text': {'$': '?'}});
};

/** @inheritDoc */
gadgetide.dataunit.DynamicText.prototype.loadOutputFormat =
  function(callback,inputFormat) {

    callback(null);
  };

