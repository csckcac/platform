goog.provide('gadgetide.uielement.UIElement');

goog.require('gadgetide.uielement.UIElementRenderer');
goog.require('goog.ui.Control');
goog.require('gadgetide.spec');


/**
 * @param {goog.ui.ControlContent} content
 * @param {gadgetide.uielement.UIElementRenderer=} opt_renderer
 * @param {goog.dom.DomHelper=} opt_domHelper Optional DOM hepler, used for
 *     document interaction.
 * @constructor
 * @extends {goog.ui.Control}
 */
gadgetide.uielement.UIElement = function(content, opt_renderer, opt_domHelper) {
  goog.ui.Control.call(this, content, opt_renderer ||
    gadgetide.uielement.UIElementRenderer.getInstance(), opt_domHelper);
};
goog.inherits(gadgetide.uielement.UIElement, goog.ui.Control);

/**
 * @return {Array.<gadgetide.spec.Spec>}
 */
gadgetide.uielement.UIElement.prototype.getSpec = function() {
  return this.getRenderer().getSpec(this);
};

/**
 * @type {Object}
 */
gadgetide.uielement.UIElement.prototype.map_;

/**
 * redefined just to change the type
 * @see goog.ui.Control.prototype.getRenderer
 * @return {gadgetide.uielement.UIElementRenderer|undefined} Renderer used by
 * the UIElement.
 */
gadgetide.uielement.UIElement.prototype.getRenderer;

/**
 * by default delegate to the render.
 * @return {Object}
 */
gadgetide.uielement.UIElement.prototype.getContext = function() {
  return this.getRenderer().getContext(this);
};

/**
 * by default delegate to the render.
 * @param {Object} input
 */
gadgetide.uielement.UIElement.prototype.updateUiData = function(input) {
  this.getRenderer().updateUiData(this, input);
};

/**
 * this is the default spec update callback function. will get called if a spec
 * is changed and there is no specific callback function.
 *
 * by default delegate to the render.
 * @param {Object} specMap
 * @param {number} index Index of the last changed spec in the spec array.
 */
gadgetide.uielement.UIElement.prototype.updateSpecMap = function(specMap, index) {
  this.map_ = specMap;
  //TODO: implement the case with specific callbacks.
  this.getRenderer().updateSpecMap(this);
};

gadgetide.uielement.UIElement.prototype.getSpecMap = function() {
  if (!this.map_) {
    this.map_ = gadgetide.spec.extractDefaultsMap(this.getSpec());
  }
  return this.map_;
};


/**
 * by default delegate to the render.
 * @param {!goog.math.Size} size
 */
gadgetide.uielement.UIElement.prototype.setSize = function(size) {
  this.getRenderer().setSize(this, size);
};
