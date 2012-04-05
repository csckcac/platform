goog.provide('gadgetide.uielement.UIElementRenderer');

goog.require('goog.ui.ControlRenderer');
goog.require('gadgetide.spec');



/**
 * @constructor
 * @extends {goog.ui.ControlRenderer}
 */
gadgetide.uielement.UIElementRenderer = function() {
  goog.ui.ControlRenderer.call(this);
};
goog.inherits(gadgetide.uielement.UIElementRenderer, goog.ui.ControlRenderer);
goog.addSingletonGetter(gadgetide.uielement.UIElementRenderer);


/**
 * @param {gadgetide.uielement.UIElement} uiElm
 * @return {Array.<gadgetide.spec.Spec>}
 */
gadgetide.uielement.UIElementRenderer.prototype.getSpec = function(uiElm) {
  return [];
};


/**
 * @param {gadgetide.uielement.UIElement} uiElm
 * @return {Object}
 */
gadgetide.uielement.UIElementRenderer.prototype.getContext = function(uiElm) {
  return null;
};

/**
 * @param {gadgetide.uielement.UIElement} uiElm
 * @param {Object} input
 */
gadgetide.uielement.UIElementRenderer.prototype.updateUiData = function(uiElm,input) {
  //no-op
};

/**
 * @param {gadgetide.uielement.UIElement} uiElm
 */
gadgetide.uielement.UIElementRenderer.prototype.updateSpecMap = function(uiElm) {
  //no-op
};

/**
 * @param {gadgetide.uielement.UIElement} uiElm
 * @param {!goog.math.Size} size
 */
gadgetide.uielement.UIElementRenderer.prototype.setSize = function(uiElm,size) {
  //no-op, resizing is usually handled by the CSS
};

