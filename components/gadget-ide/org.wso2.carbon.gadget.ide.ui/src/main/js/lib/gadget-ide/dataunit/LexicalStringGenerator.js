goog.provide('gadgetide.dataunit.LexicalStringGen');

goog.require('gadgetide.datasource.DataSource');
//goog.require('gadgetide.ui.ConfigEditor');

/**
 * @param {!Object} config
 * @extends {gadgetide.datasource.DataSource}
 * @constructor
 */
gadgetide.dataunit.LexicalStringGen = function(config) {
    //this.text = config['text'];
};

/** @inheritDoc */
gadgetide.dataunit.LexicalStringGen.prototype.execute =
  function(callback, input, context) {
    var str = input['text']['$'];
    var lastChar = String.fromCharCode(str.charCodeAt(str.length-1)+1);
    var LexStr =  str.substring(0,str.length-1) + lastChar;
    callback({'text': {'$':LexStr}});
};

/** @inheritDoc */
gadgetide.dataunit.LexicalStringGen.prototype.loadInputFormat = function(callback) {
    callback({'text': {'$': '?'}});
};

/** @inheritDoc */
gadgetide.dataunit.LexicalStringGen.prototype.loadOutputFormat =
  function(callback,inputFormat) {

  callback({'text': {'$':'?'}});
};