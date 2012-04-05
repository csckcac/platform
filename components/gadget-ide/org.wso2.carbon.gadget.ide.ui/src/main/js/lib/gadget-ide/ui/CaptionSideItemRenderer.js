goog.provide('gadgetide.ui.CaptionSideItemRenderer');

goog.require('goog.dom');
goog.require('goog.ui.ControlRenderer');
goog.require('goog.ui.INLINE_BLOCK_CLASSNAME');


/**
 * @constructor
 * @extends {goog.ui.ControlRenderer}
 */
gadgetide.ui.CaptionSideItemRenderer = function() {
  goog.ui.ControlRenderer.call(this);
};
goog.inherits(gadgetide.ui.CaptionSideItemRenderer, goog.ui.ControlRenderer);
goog.addSingletonGetter(gadgetide.ui.CaptionSideItemRenderer);


/** @type {string} */
gadgetide.ui.CaptionSideItemRenderer.CSS_CLASS = 'gide-item-cap';


/** @inheritDoc */
gadgetide.ui.CaptionSideItemRenderer.prototype.getCssClass = function() {
  return gadgetide.ui.CaptionSideItemRenderer.CSS_CLASS;
};


/**
 * @param {gadgetide.ui.SideItem} sideItem SideItem to be
 *     rendered as caption element.
 * @return {Element} Root element for the CaptionSideItemRenderer.
 */
gadgetide.ui.CaptionSideItemRenderer.prototype.createDom =
  function(sideItem) {
    return goog.ui.ControlRenderer.prototype.createDom.call(this, sideItem);
  };

/**
 * @override
 * @param {Element} element Root element of the control whose content element
 *     is to be returned.
 * @return {Element} The control's content element.
 */
gadgetide.ui.CaptionSideItemRenderer.prototype.getContentElement =
  function(element) {
    return goog.dom.getElementByClass(
      goog.getCssName(this.getCssClass(), 'content'),
      element
    );
  };

