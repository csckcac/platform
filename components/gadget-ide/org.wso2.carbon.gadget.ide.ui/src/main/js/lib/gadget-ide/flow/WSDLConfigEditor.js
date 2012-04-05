goog.provide('gadgetide.flow.WSDLConfigEditor');

goog.require('gadgetide.ui.ConfigEditor');
goog.require('goog.ui.Button');
goog.require('goog.ui.Component');
goog.require('gadgetide.client.Admin');
goog.require('goog.ui.Css3ButtonRenderer');
goog.require('goog.ui.INLINE_BLOCK_CLASSNAME');

/**
 * @constructor
 * @param {goog.dom.DomHelper=} opt_domHelper Optional DOM helper.
 * @extends {gadgetide.ui.ConfigEditor}
 */

gadgetide.flow.WSDLConfigEditor = function(opt_domHelper) {
  gadgetide.ui.ConfigEditor.call(this, opt_domHelper);
};
goog.inherits(gadgetide.flow.WSDLConfigEditor, gadgetide.ui.ConfigEditor);

/**
 * @type {string}
 * @const
 */
gadgetide.flow.WSDLConfigEditor.CSS_CLASS = 'wsdl-config-editor';


/**
 * @inheritDoc
 */
gadgetide.flow.WSDLConfigEditor.prototype.createDom = function() {
  gadgetide.ui.ConfigEditor.prototype.createDom.call(this);
  var el = this.getContentElement();
  var dom = this.getDomHelper();
  var css = gadgetide.ui.ConfigEditor.COMMON_CSS_CLASS;

  var labelEls = dom.createDom(
    'div', goog.ui.INLINE_BLOCK_CLASSNAME,
    dom.createDom('div', goog.getCssName(css, 'label'),
      dom.createDom('label', null, 'WSDL URL')),
    dom.createDom('div', goog.getCssName(css, 'label'),
      dom.createDom('label', null, 'Endpoint')),
    dom.createDom('div', goog.getCssName(css, 'label'),
      dom.createDom('label', null, 'Operation')));

  var inputEls = dom.createDom(
    'div', {'class': goog.ui.INLINE_BLOCK_CLASSNAME + ' ' +
      goog.getCssName(css, 'input-block')},
    dom.createDom('div', goog.getCssName(css, 'input'),
      this.urlEl = dom.createDom('input', { 'type': 'text'})),
    this.endpointEl = dom.createDom('div', goog.getCssName(css, 'input'),
      dom.createDom('select')),
    this.operationsEl = dom.createDom('div', goog.getCssName(css, 'input'),
      dom.createDom('select')));

  var loadButtonEl;
  var buttonEls = dom.createDom(
    'div', goog.ui.INLINE_BLOCK_CLASSNAME,
    (loadButtonEl = dom.createDom('div', goog.getCssName(css, 'button'))),
    dom.createDom('div', goog.getCssName(css, 'loader'), '\u00A0'),
    dom.createDom('div', goog.getCssName(css, 'loader'), '\u00A0'),
    dom.createDom('div', goog.getCssName(css, 'resize-hack'), '\u00A0')
  );

  var button = new goog.ui.Button('load',
    goog.ui.Css3ButtonRenderer.getInstance());
  button.render(loadButtonEl);

  this.getHandler().listen(button, goog.ui.Component.EventType.ACTION,
    this.handleLoadClick_);

  dom.appendChild(el, labelEls);
  dom.appendChild(el, inputEls);
  dom.appendChild(el, buttonEls);
  goog.dom.classes.add(el, gadgetide.flow.WSDLConfigEditor.CSS_CLASS);
};

gadgetide.flow.WSDLConfigEditor.prototype.handleLoadClick_ = function() {
  gadgetide.client.Admin.getInstance().getEndpoints(this.urlEl.value,
    goog.bind(this.handleEndpointLoad_, this)
  );
};

gadgetide.flow.WSDLConfigEditor.prototype.replaceSelect_ = function(selectDiv,newValues) {
  var dom = this.getDomHelper();
  var newSelect = dom.createDom('select');
  for (var i = 0; i < newValues.length; i++) {
    var value = newValues[i];
    newSelect.appendChild(dom.createDom('option', {'value':value}, value));
  }
  selectDiv.removeChild(selectDiv.firstChild);
  selectDiv.appendChild(newSelect);
};

gadgetide.flow.WSDLConfigEditor.prototype.handleEndpointLoad_ = function(endpoints) {
  this.replaceSelect_(this.endpointEl,endpoints);
  this.handleEndpointChange_();
};

gadgetide.flow.WSDLConfigEditor.prototype.handleOperationsLoad_ = function(operations) {
  this.replaceSelect_(this.operationsEl,operations)
  this.handleOperationsChange_();
};

gadgetide.flow.WSDLConfigEditor.prototype.handleOperationsChange_ = function() {
  this.fireConfigChange({
    'wsdlUrl':this.urlEl.value,
    'endpoint': this.endpointEl.firstChild.value,
    'operation': this.operationsEl.firstChild.value
  });


};

gadgetide.flow.WSDLConfigEditor.prototype.handleEndpointChange_ = function() {
  gadgetide.client.Admin.getInstance().getOperations(this.urlEl.value,
    this.endpointEl.firstChild.value,
    goog.bind(this.handleOperationsLoad_, this)
  );
};

/**
 * @inheritDoc
 */
gadgetide.flow.WSDLConfigEditor.prototype.enterDocument = function() {
  this.getHandler().listen(this.endpointEl,goog.events.EventType.CHANGE,
    this.handleEndpointChange_);

  this.getHandler().listen(this.operationsEl,goog.events.EventType.CHANGE,
    this.handleOperationsChange_);
};

