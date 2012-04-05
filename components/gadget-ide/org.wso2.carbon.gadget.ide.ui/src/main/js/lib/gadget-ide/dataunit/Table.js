goog.provide('gadgetide.dataunit.Table');

goog.require('gadgetide.datasource.DataSource');

/**
 * @param {!Object} config
 * @extends {gadgetide.datasource.DataSource}
 * @constructor
 */
gadgetide.dataunit.Table = function(config) {
};

/** @inheritDoc */
gadgetide.dataunit.Table.prototype.execute =
  function(callback, input, context) {
    callback(null);
  };

/** @inheritDoc */
gadgetide.dataunit.Table.prototype.loadInputFormat = function(callback) {
  callback({
    'rows': {'$': '?'}
  });
};

/** @inheritDoc */
gadgetide.dataunit.Table.prototype.loadOutputFormat =
  function(callback, inputFormat) {
    callback(null);
  };


