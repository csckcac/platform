goog.provide('gadgetide.dataunit.JQPie');

goog.require('gadgetide.datasource.DataSource');

/**
 * @param {!Object} config
 * @extends {gadgetide.datasource.DataSource}
 * @constructor
 */
gadgetide.dataunit.JQPie = function(config) {
};

/** @inheritDoc */
gadgetide.dataunit.JQPie.prototype.execute =
  function(callback, input, context) {
    callback(null);
  };

/** @inheritDoc */
gadgetide.dataunit.JQPie.prototype.loadInputFormat = function(callback) {
  callback({
    'data':{
    'labels': {'$': '?'},
    'values': {'$': '?'}
    }
  });
};

/** @inheritDoc */
gadgetide.dataunit.JQPie.prototype.loadOutputFormat =
  function(callback, inputFormat) {
    callback(null);
  };


