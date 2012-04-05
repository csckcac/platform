goog.provide('gadgetide.flow.Constant');

goog.require('gadgetide.ui.ConfigEditor');

/**
 * @constructor
 * @param {goog.dom.DomHelper=} opt_domHelper Optional DOM helper.
 * @extends {gadgetide.ui.ConfigEditor}
 */
gadgetide.flow.Constant = function(opt_domHelper) {
  gadgetide.ui.ConfigEditor.call(this, opt_domHelper);
};
goog.inherits(gadgetide.flow.Constant, gadgetide.ui.ConfigEditor);


/**
 * @type {string}
 * @const
 */
gadgetide.flow.WSDLConfigEditor.CSS_CLASS = 'const-editor';

gadgetide.flow.WSDLConfigEditor.prototype.createDom = function() {
  gadgetide.ui.ConfigEditor.prototype.createDom.call(this);
  var el = this.getContentElement();
  var dom = this.getDomHelper();
  var css = gadgetide.ui.ConfigEditor.COMMON_CSS_CLASS;

  var labelEls = dom.createDom(
    'div', goog.ui.INLINE_BLOCK_CLASSNAME,
    dom.createDom('div', goog.getCssName(css, 'label'),
      dom.createDom('label', null, 'Value')));
};

