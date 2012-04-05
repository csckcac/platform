goog.provide('gadgetide.spec.SpecTable');
goog.provide('gadgetide.spec.SpecChangeEvent');

goog.require('gadgetide.spec.TypeCell');
goog.require('goog.debug.Logger');
goog.require('goog.ui.Container');
goog.require('goog.ui.INLINE_BLOCK_CLASSNAME');


/**
 * @constructor
 * @param {goog.dom.DomHelper=} opt_domHelper Optional DOM helper.
 * @extends {goog.ui.Component}
 */
gadgetide.spec.SpecTable = function(opt_domHelper) {
  goog.ui.Component.call(this, opt_domHelper);
};
goog.inherits(gadgetide.spec.SpecTable, goog.ui.Component);

/**
 * A reference to this Table's logger
 * @type {goog.debug.Logger}
 * @private
 */
gadgetide.spec.SpecTable.prototype.logger_ =
  goog.debug.Logger.getLogger('gadgetide.spec.SpecTable');

/**
 * @type {string}
 * @const
 */
gadgetide.spec.SpecTable.CSS_CLASS = 'gide-table';

/**
 * @type {Element}
 * @private
 */
gadgetide.spec.SpecTable.prototype.contentEl_;

/**
 * @type {Array.<gadgetide.spec.TypeCell>}
 * @private
 */
gadgetide.spec.SpecTable.prototype.cells;

/**
 * @type {Array.<gadgetide.spec.Spec>}
 * @private
 */
gadgetide.spec.SpecTable.prototype.specs_;

/**
 * @type {!Object}
 * @private
 */
gadgetide.spec.SpecTable.prototype.map_;

/**
 * @type {goog.events.EventHandler}
 * @private
 */
gadgetide.spec.SpecTable.prototype.contentHandler_;

/**
 * @return {string}
 */
gadgetide.spec.SpecTable.prototype.getCssClass = function() {
  return gadgetide.spec.SpecTable.CSS_CLASS;
};

/**
 * @inheritDoc
 */
gadgetide.spec.SpecTable.prototype.createDom = function() {
  goog.ui.Component.prototype.createDom.call(this);
  var dom = this.getDomHelper();
  var el = this.getElement();
  var inlineBlock = goog.ui.INLINE_BLOCK_CLASSNAME + ' ';
  var cellCss = inlineBlock + goog.getCssName(this.getCssClass(), 'cell');
  var headCss = goog.getCssName(this.getCssClass(), 'headerCell');

  goog.dom.classes.add(el, this.getCssClass());
  el.appendChild(
    dom.createDom('div',
      goog.getCssName(this.getCssClass(), 'header'),
      dom.createDom('div', cellCss,
        dom.createDom('div', headCss, 'Name')),
      dom.createDom('div', cellCss,
        dom.createDom('div', headCss, 'Value'))));

  el.appendChild(this.contentEl_ = dom.createDom('div',
    goog.getCssName(this.getCssClass(), 'content')));
};

gadgetide.spec.SpecTable.prototype.clear = function() {
  if (this.contentHandler_) {
    this.contentHandler_.dispose();
    this.contentHandler_ = null;
  }
  if (this.cells) {
    for (var i = 0; i < this.cells.length; i++) {
      var cell = this.cells[i];
      cell.dispose();
    }
  }
  this.cells = null;
  this.contentEl_.innerHTML = '';
};

/**
 *
 * @param {Array.<gadgetide.spec.Spec>} specs
 * @param {!Object} map
 */
gadgetide.spec.SpecTable.prototype.populate = function(specs, map) {
  this.specs_ = specs;
  this.map_ = map;
  this.clear();
  this.contentHandler_ = new goog.events.EventHandler(this);
  this.cells = [];
  for (var i = 0; i < specs.length; i++) {
    /** @type {gadgetide.spec.Spec} */
    var spec = specs[i];
    var name = spec.name;
    if (spec.type instanceof gadgetide.spec.TypeCellRenderer) {
      var typeCell = new gadgetide.spec.TypeCell(spec.type);
      this.cells.push(typeCell);
      this.addRow_(name, map[name], typeCell, i);
    } else {
      //TODO:
    }
  }
};

/**
 *
 * @param {string} name
 * @param {*} value
 * @param {gadgetide.spec.TypeCell} typeCell
 */
gadgetide.spec.SpecTable.prototype.addRow_ =
  function(name, value, typeCell, rowIndex) {
    var dom = this.getDomHelper();
    var inlineBlock = goog.ui.INLINE_BLOCK_CLASSNAME + ' ';
    var cellCss = inlineBlock + goog.getCssName(this.getCssClass(), 'cell');
    var valueCss = goog.getCssName(this.getCssClass(), 'valCell');
    var valueEl;

    var rowEl = dom.createDom('div',
      goog.getCssName(this.getCssClass(), 'row'),
      dom.createDom('div', cellCss,
        dom.createDom('div', valueCss, name)),
      dom.createDom('div', cellCss,
        valueEl = dom.createDom('div', valueCss)));
    this.contentEl_.appendChild(rowEl);
    typeCell.setElement(valueEl);

    this.renderValueCell_(typeCell, value, rowEl, rowIndex);
  };

gadgetide.spec.SpecTable.prototype.renderValueCell_ = function(typeCell, value, rowEl, rowIndex) {
  typeCell.renderUnselected(value);
  this.contentHandler_.listenOnce(rowEl, goog.events.EventType.CLICK,
    goog.partial(this.handleRowSelect_, rowIndex)
  );
};

gadgetide.spec.SpecTable.prototype.handleRowSelect_ = function(rowIndex) {
  var spec = this.specs_[rowIndex];
  var typeCell = this.cells[rowIndex];
  var valCell = typeCell.getElement();
  valCell.innerHTML = '';
  this.contentHandler_.listen(typeCell, gadgetide.spec.TypeCell.EventType.CHANGE,
    goog.partial(this.handleRowChange_, rowIndex)
  );
  this.contentHandler_.listenOnce(typeCell, gadgetide.spec.TypeCell.EventType.UNSELECT,
    goog.partial(this.handleRowUnselect_, rowIndex)
  );
  typeCell.renderSelected(this.map_[spec.name]);

  if (goog.DEBUG) {
    this.logger_.shout('row selected. [' + spec.name + '=' +
      String(this.map_[spec.name]) + ']');
  }
};

gadgetide.spec.SpecTable.prototype.handleRowChange_ = function(rowIndex, e) {
  var spec = this.specs_[rowIndex];
  var value = e.value;
  var name = spec.name;
  if (this.map_[name] != value) {
    this.map_[name] = value;
    this.dispatchEvent(new gadgetide.spec.SpecChangeEvent(this,rowIndex));
  }
};


gadgetide.spec.SpecTable.prototype.handleRowUnselect_ = function(rowIndex) {
  var spec = this.specs_[rowIndex];
  var typeCell = this.cells[rowIndex];
  var valCell = typeCell.getElement();
  valCell.innerHTML = '';
  var rowEl = goog.dom.getChildren(this.contentEl_)[rowIndex];
  this.renderValueCell_(typeCell, this.map_[spec.name], rowEl, rowIndex);

  if (goog.DEBUG) {
    this.logger_.shout('row editing finished. [' + spec.name + '=' +
      String(this.map_[spec.name]) + ']');
  }
};

gadgetide.spec.SpecTable.prototype.getMap = function() {
 return this.map_;
};

/** @enum {string} */
gadgetide.spec.SpecTable.EventType = {
  CHANGE: goog.events.getUniqueId('change')
};

/**
 * @param {gadgetide.spec.SpecTable} target TypeCell.initiating event.
 * @param {number} index Index of the last changed spec in the spec array.
 * @extends {goog.events.Event}
 * @constructor
 */
gadgetide.spec.SpecChangeEvent = function(target, index) {
  goog.events.Event.call(this, gadgetide.spec.SpecTable.EventType.CHANGE,
    target);
  this.index = index;
};
goog.inherits(gadgetide.spec.SpecChangeEvent, goog.events.Event);

/**
 * @type {number}
 */
gadgetide.spec.SpecChangeEvent.prototype.index;


