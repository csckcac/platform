goog.provide('gadgetide.flow.CassandraEditor');

goog.require('gadgetide.ui.ConfigEditor');

/**
 * @constructor
 * @param {goog.dom.DomHelper=} opt_domHelper Optional DOM helper.
 * @extends {gadgetide.ui.ConfigEditor}
 */
gadgetide.flow.CassandraEditor = function(opt_domHelper) {
  gadgetide.ui.ConfigEditor.call(this, opt_domHelper);
  this.selectorRows = [];
};
goog.inherits(gadgetide.flow.CassandraEditor, gadgetide.ui.ConfigEditor);


/**
 * @type {string}
 * @const
 */
gadgetide.flow.CassandraEditor.CSS_CLASS = 'gide-cass-editor';

gadgetide.flow.CassandraEditor.prototype.createDom = function() {
  gadgetide.ui.ConfigEditor.prototype.createDom.call(this);
  var el = this.getContentElement();
  var dom = this.getDomHelper();
  var css = gadgetide.ui.ConfigEditor.COMMON_CSS_CLASS;

  var tableNameEls = dom.createDom(
    'div', undefined,
    dom.createDom('div', goog.getCssName(css, 'label') + ' ' + goog.ui.INLINE_BLOCK_CLASSNAME
      , dom.createDom('label', null, 'Table Name')),
    dom.createDom('div', goog.getCssName(css, 'input') + ' ' + goog.ui.INLINE_BLOCK_CLASSNAME + ' ' +
      goog.getCssName(css, 'input-block'), this.tableNameEl_ = dom.createDom('input', { 'type': 'text'}))
  );

  var tableIndexNameEl = dom.createDom(
    'div', undefined,
    dom.createDom('div', goog.getCssName(css, 'label') + ' ' + goog.ui.INLINE_BLOCK_CLASSNAME
      , dom.createDom('label', null, 'Index Name')),
    dom.createDom('div', goog.getCssName(css, 'input') + ' ' + goog.ui.INLINE_BLOCK_CLASSNAME + ' ' +
      goog.getCssName(css, 'input-block'), this.tableIndexNameEl_ = dom.createDom('input', { 'type': 'text'}))
  );

  var loadButtonEl,addButtonEl;
  this.selectorEl = dom.createDom('div');

  var buttonCss = goog.getCssName(gadgetide.flow.CassandraEditor.CSS_CLASS, 'buttonDiv');

  var buttonEls = dom.createDom('div', goog.getCssName(css, 'button'),
    loadButtonEl = dom.createDom('div', buttonCss),
    addButtonEl = dom.createDom('div', buttonCss));

  var loadButton = new goog.ui.Button('load',
    goog.ui.Css3ButtonRenderer.getInstance());
  loadButton.render(loadButtonEl);

  var addButton = new goog.ui.Button('add Selector',
    goog.ui.Css3ButtonRenderer.getInstance());
  addButton.render(addButtonEl);

  this.getHandler().listen(loadButton, goog.ui.Component.EventType.ACTION,
    this.handleLoadClick_);

  this.getHandler().listen(addButton, goog.ui.Component.EventType.ACTION,
    goog.partial(this.addSelectors, true));

  var clearEl = dom.createDom('div', 'gide-clear');

  this.addSelectors();

  dom.appendChild(el, tableNameEls);
  dom.appendChild(el, tableIndexNameEl);
  dom.appendChild(el, this.selectorEl);
  dom.appendChild(el, buttonEls);
  dom.appendChild(el, clearEl);

  goog.dom.classes.add(el, gadgetide.flow.CassandraEditor.CSS_CLASS);
};

/**
 *
 * @param {boolean=} hasMin
 */
gadgetide.flow.CassandraEditor.prototype.addSelectors = function(hasMin) {
  var dom = this.getDomHelper();

  var css = gadgetide.flow.CassandraEditor.CSS_CLASS;
  var removeEl;
  var selectorRow = dom.createDom('div', goog.getCssName(css, 'row'),
    dom.createDom('div', undefined, 'Indexed Column'),
    dom.createDom('input',goog.getCssName(css, 'index')),
    removeEl = dom.createDom('div', goog.getCssName(css, 'min'))
  );

  if (hasMin) {
    var removeButton = new goog.ui.Button(' - ',
      goog.ui.Css3ButtonRenderer.getInstance());
    removeButton.render(removeEl);

    this.getHandler().listen(removeButton, goog.ui.Component.EventType.ACTION,
      function() {
        goog.array.remove(this.selectorRows,selectorRow);
        removeButton.dispose();
        this.selectorEl.removeChild(selectorRow);
      }
    );
  }

  this.selectorRows.push(selectorRow);
  this.selectorEl.appendChild(selectorRow);
};

/**
 * @private
 * @type {string}
 */
gadgetide.flow.CassandraEditor.prototype.WsdlUrl_;

/*gadgetide.flow.CassandraEditor.prototype.setDynamicContext = function(url){
    var aa = gadgetide.dataunit.WSDLDataSource.setServicePort();
    return wsdlUrl_;
};*/



gadgetide.flow.CassandraEditor.prototype.handleLoadClick_ = function() {
  var indexes = [];
  var css = gadgetide.flow.CassandraEditor.CSS_CLASS;

  for (var i = 0; i < this.selectorRows.length; i++) {
    var selectorRow = this.selectorRows[i];
    var text = goog.dom.getElementByClass(goog.getCssName(css, 'index'),selectorRow);
    indexes.push(text.value);
  }
  this.WsdlUrl_ =  '/services/QueryService?wsdl2';
  this.fireConfigChange({
    'wsdlUrl': ''+this.WsdlUrl_+'',
    'endpoint': 'QueryServiceHttpsEndpoint',
    'operation': 'queryColumnFamily',
    'tableName':this.tableNameEl_.value,
    'tableIndexName': this.tableIndexNameEl_.value,
    'indexes':{'index':indexes}
  });
};

