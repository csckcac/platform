goog.provide('gadgetide.dataunit.RandomInt');

goog.require('gadgetide.datasource.DataSource');

/**
 * @param {!Object} config
 * @extends {gadgetide.datasource.DataSource}
 * @constructor
 */
gadgetide.dataunit.RandomInt = function(config) {
};

/** @inheritDoc */
gadgetide.dataunit.RandomInt.prototype.execute =
  function(callback, input, context) {
    callback({'integer':
      {
        '$':Math.round(Math.random() * 100)}
      }
    );
  };

/** @inheritDoc */
gadgetide.dataunit.RandomInt.prototype.loadInputFormat = function(callback) {
  callback(null);
};

/** @inheritDoc */
gadgetide.dataunit.RandomInt.prototype.loadOutputFormat =
  function(callback, inputFormat) {

    callback({'integer': {'$': '?'}});
  };

