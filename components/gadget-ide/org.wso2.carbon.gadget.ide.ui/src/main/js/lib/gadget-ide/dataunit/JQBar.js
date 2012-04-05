goog.provide('gadgetide.dataunit.JQBar');

goog.require('gadgetide.datasource.DataSource');

/**
 * @param {!Object} config
 * @extends {gadgetide.datasource.DataSource}
 * @constructor
 */
gadgetide.dataunit.JQBar = function(config) {
};

/** @inheritDoc */
gadgetide.dataunit.JQBar.prototype.execute =
  function(callback, input, context) {
    callback(null);
  };

/** @inheritDoc */
gadgetide.dataunit.JQBar.prototype.loadInputFormat = function(callback) {
  callback({
    'data':{
    'labels': {'$': '?'},
    'values': {'$': '?'}
    }
  });
};

/** @inheritDoc */
gadgetide.dataunit.JQBar.prototype.loadOutputFormat =
  function(callback, inputFormat) {
    callback(null);
  };


