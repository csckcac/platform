goog.provide('gadgetide.dataunit.DropDownList');

goog.require('gadgetide.datasource.DataSource');

/**
 * @param {!Object} config
 * @extends {gadgetide.datasource.DataSource}
 * @constructor
 */
gadgetide.dataunit.DropDownList = function(config) {
};

/** @inheritDoc */
gadgetide.dataunit.DropDownList.prototype.execute =
  function(callback, input, context) {
    callback({'text': {'$':context['text']}});
  };

/** @inheritDoc */
gadgetide.dataunit.DropDownList.prototype.loadInputFormat = function(callback) {
  callback({'text': {'$': '?'}});
};

/** @inheritDoc */
gadgetide.dataunit.DropDownList.prototype.loadOutputFormat =
  function(callback, inputFormat) {
    callback({'text': {'$':'?'}});
  };