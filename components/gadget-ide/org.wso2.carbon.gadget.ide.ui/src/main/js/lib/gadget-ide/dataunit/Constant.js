goog.provide('gadgetide.dataunit.Constant');

goog.require('gadgetide.datasource.DataSource');

/**
 * @param {!Object} config
 * @extends {gadgetide.datasource.DataSource}
 * @constructor
 */
gadgetide.dataunit.Constant = function(config) {
    this.text = config['text'];
};

/** @inheritDoc */
gadgetide.dataunit.Constant.prototype.execute =
  function(callback, input, context) {

  callback({'text':
      {
        '$':this.text}
      }
    );
};

/** @inheritDoc */
gadgetide.dataunit.Constant.prototype.loadInputFormat = function(callback) {
  callback(null);
};

/** @inheritDoc */
gadgetide.dataunit.Constant.prototype.loadOutputFormat =
  function(callback,inputFormat) {

  callback({'text': {'$': '?'}});
};