goog.provide('gadgetide.flow.ConstantEditor');

goog.require('gadgetide.ui.ConfigEditor');

/**
 * @constructor
 * @param {goog.dom.DomHelper=} opt_domHelper Optional DOM helper.
 * @extends {gadgetide.ui.ConfigEditor}
 */
gadgetide.flow.ConstantEditor = function(opt_domHelper) {
  gadgetide.ui.ConfigEditor.call(this, opt_domHelper);
};
goog.inherits(gadgetide.flow.ConstantEditor, gadgetide.ui.ConfigEditor);


/**
 * @type {string}
 * @const
 */
gadgetide.flow.ConstantEditor.CSS_CLASS = 'const-editor';

gadgetide.flow.ConstantEditor.prototype.createDom = function() {
  gadgetide.ui.ConfigEditor.prototype.createDom.call(this);
  var el = this.getContentElement();
  var dom = this.getDomHelper();
  var css = gadgetide.ui.ConfigEditor.COMMON_CSS_CLASS;

  var loadButtonEl;
  var tableNameEl = dom.createDom(
    'div', goog.ui.INLINE_BLOCK_CLASSNAME,
    dom.createDom('div', goog.getCssName(css, 'label'),
      dom.createDom('label', null, 'Value')),
            this.tableNameEl_ = dom.createDom('input', { 'type': 'text'})

          );

  var buttonCss = goog.getCssName(gadgetide.flow.CassandraEditor.CSS_CLASS, 'buttonDiv');

  var buttonEls = dom.createDom('div', goog.getCssName(css, 'button'),
    loadButtonEl = dom.createDom('div', buttonCss));

  var loadButton = new goog.ui.Button('load',
    goog.ui.Css3ButtonRenderer.getInstance());
  loadButton.render(loadButtonEl);


    dom.appendChild(el,tableNameEl);
    dom.appendChild(el,loadButtonEl);


  this.getHandler().listen(loadButton, goog.ui.Component.EventType.ACTION,
    this.handleLoadClick_);
};

gadgetide.flow.ConstantEditor.prototype.handleLoadClick_ = function () {
   this.fireConfigChange({
//       "text" : this.tableNameEl_.value
//    'wsdlUrl':'http://localhost:9763/services/QueryService?wsdl2',
    'text' : this.tableNameEl_.value

   });
}