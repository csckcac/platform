goog.provide('gadgetide.dataunit.DateTimePicker');

goog.require('gadgetide.datasource.DataSource');

/**
 * @param {!Object} config
 * @extends {gadgetide.datasource.DataSource}
 * @constructor
 */
gadgetide.dataunit.DateTimePicker = function(config) {
};

/** @inheritDoc */
gadgetide.dataunit.DateTimePicker.prototype.execute =
  function(callback, input, context) {
  callback({'text': {'$': context['text']}});
};

/** @inheritDoc */
gadgetide.dataunit.DateTimePicker.prototype.loadInputFormat = function(callback) {
  callback(null);
};

/** @inheritDoc */
gadgetide.dataunit.DateTimePicker.prototype.loadOutputFormat =
  function(callback,inputFormat) {
  callback({'text': {'$': '?'}});
};
