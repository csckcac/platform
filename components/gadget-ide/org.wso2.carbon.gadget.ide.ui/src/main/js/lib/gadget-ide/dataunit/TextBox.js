goog.provide('gadgetide.dataunit.TextBox');

goog.require('gadgetide.datasource.DataSource');

/**
 * @param {!Object} config
 * @extends {gadgetide.datasource.DataSource}
 * @constructor
 */
gadgetide.dataunit.TextBox = function(config) {
};

/** @inheritDoc */
gadgetide.dataunit.TextBox.prototype.execute =
  function(callback, input, context) {
  callback(null);
};

/** @inheritDoc */
gadgetide.dataunit.TextBox.prototype.loadInputFormat = function(callback) {
  callback(null);
};

/** @inheritDoc */
gadgetide.dataunit.TextBox.prototype.loadOutputFormat =
  function(callback,inputFormat) {

  callback({'text': {'$': '?'}});
};

