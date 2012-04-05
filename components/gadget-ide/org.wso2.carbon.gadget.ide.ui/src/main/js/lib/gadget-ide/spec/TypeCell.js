goog.provide('gadgetide.spec.TypeCell');

goog.require('goog.events.EventTarget');

/**
 * @param {gadgetide.spec.TypeCellRenderer} renderer
 * @constructor
 * @extends {goog.events.EventTarget}
 */
gadgetide.spec.TypeCell = function(renderer) {
  this.renderer_ = renderer;
};
goog.inherits(gadgetide.spec.TypeCell, goog.events.EventTarget);

/**
 * @type {goog.events.EventHandler}
 * @private
 */
gadgetide.spec.TypeCell.prototype.handler_;

/**
 * @type {Element}
 * @private
 */
gadgetide.spec.TypeCell.prototype.el_;

/**
 * @type {gadgetide.spec.TypeCellRenderer}
 */
gadgetide.spec.TypeCell.prototype.renderer_;

/**
 * @inheritDoc
 */
gadgetide.spec.TypeCell.prototype.disposeInternal = function() {
  goog.events.EventTarget.prototype.disposeInternal.call(this);
  if(this.handler_){
    this.handler_.dispose();
    delete this.handler_;
  }
};

gadgetide.spec.TypeCell.prototype.getHandler = function() {
  return this.handler_ ||
    (this.handler_ = new goog.events.EventHandler(this));
};

/**
 * @param {*} value
 */
gadgetide.spec.TypeCell.prototype.renderUnselected = function(value) {
  this.renderer_.renderUnselected(this, value);
};

/**
 * @param {*} value
 */
gadgetide.spec.TypeCell.prototype.renderSelected = function(value) {
  this.renderer_.renderSelected(this, value);
};

/**
 * @param {Element} el
 */
gadgetide.spec.TypeCell.prototype.setElement = function(el) {
  this.el_ = el;
};

/**
 * @return {Element}
 */
gadgetide.spec.TypeCell.prototype.getElement = function() {
  return this.el_;
};

/**
 * @return {gadgetide.spec.TypeCellRenderer}
 */
gadgetide.spec.TypeCell.prototype.getRenderer = function() {
  return this.renderer_;
};

gadgetide.spec.TypeCell.prototype.fireChange = function(value) {
  this.dispatchEvent(new gadgetide.spec.CellChangeEvent(this,value));

};

gadgetide.spec.TypeCell.prototype.fireUnselect = function() {
  this.getHandler().removeAll();
  this.dispatchEvent(gadgetide.spec.TypeCell.EventType.UNSELECT);
};

/** @enum {string} */
gadgetide.spec.TypeCell.EventType = {
  CHANGE: goog.events.getUniqueId('change'),
  UNSELECT: goog.events.getUniqueId('unselect')
};

/**
 * @param {gadgetide.spec.TypeCell} target TypeCell.initiating event.
 * @param {*} value
 * @extends {goog.events.Event}
 * @constructor
 */
gadgetide.spec.CellChangeEvent = function(target, value) {
  goog.events.Event.call(this, gadgetide.spec.TypeCell.EventType.CHANGE,
    target);

  this.value = value;
};
goog.inherits(gadgetide.spec.CellChangeEvent, goog.events.Event);

/**
 * @type {*}
 */
gadgetide.spec.CellChangeEvent.prototype.value;


