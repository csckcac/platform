goog.provide('gadgetide.ui.FlowEditor');

goog.require('goog.ui.Component');

/**
 * @constructor
 * @param {goog.dom.DomHelper=} opt_domHelper Optional DOM helper.
 * @extends {goog.ui.Component}
 */

gadgetide.ui.FlowEditor = function(opt_domHelper) {
  goog.ui.Component.call(this, opt_domHelper);
};
goog.inherits(gadgetide.ui.FlowEditor, goog.ui.Component);

/**
 * @type {WireIt.Layer}
 */
gadgetide.ui.FlowEditor.prototype.layer_;

/**
 * @inheritDoc
 */
gadgetide.ui.FlowEditor.prototype.canDecorate = function(element) {
  return true;
};
/**
 * @type {string}
 * @const
 */
gadgetide.ui.FlowEditor.CSS_CLASS = 'gide-layer';

/**
 * @inheritDoc
 */
gadgetide.ui.FlowEditor.prototype.createDom = function() {
  goog.ui.Component.prototype.createDom.call(this);
  this.decorateInternal(this.getElement());
};


/**
 * @inheritDoc
 */
gadgetide.ui.FlowEditor.prototype.enterDocument = function() {
  goog.ui.Component.prototype.enterDocument.call(this);

  this.layer_ = new WireIt.Layer({
    parentEl: this.getElement()
  });
};

/**
 * @inheritDoc
 */
gadgetide.ui.FlowEditor.prototype.decorateInternal = function(element) {
  goog.ui.Component.prototype.decorateInternal.call(this, element);
  goog.dom.classes.add(element, gadgetide.ui.FlowEditor.CSS_CLASS);
};

/**
 * @return {WireIt.Layer}
 */
gadgetide.ui.FlowEditor.prototype.getLayer = function() {
  return this.layer_;
};

