goog.provide('gadgetide.ui.CollapsibleSideItemRenderer');

goog.require('goog.dom');
goog.require('goog.ui.ControlRenderer');
goog.require('goog.ui.INLINE_BLOCK_CLASSNAME');


/**
 * @constructor
 * @extends {goog.ui.ControlRenderer}
 */
gadgetide.ui.CollapsibleSideItemRenderer = function() {
  goog.ui.ControlRenderer.call(this);
};
goog.inherits(gadgetide.ui.CollapsibleSideItemRenderer,
  goog.ui.ControlRenderer);
goog.addSingletonGetter(gadgetide.ui.CollapsibleSideItemRenderer);


/** @type {string} */
gadgetide.ui.CollapsibleSideItemRenderer.CSS_CLASS = 'gide-item-col';


/** @inheritDoc */
gadgetide.ui.CollapsibleSideItemRenderer.prototype.getCssClass = function() {
  return gadgetide.ui.CollapsibleSideItemRenderer.CSS_CLASS;
};


/**
 * @param {gadgetide.ui.SideItem} sideItem  SideItem to be
 *     rendered as a collapsible element.
 * @return {Element} Root element for the CollapsibleSideItemRenderer.
 */
gadgetide.ui.CollapsibleSideItemRenderer.prototype.createDom =
  function(sideItem) {
    return goog.ui.ControlRenderer.prototype.createDom.call(this, sideItem);

  };

/**
 * @override
 * @param {Element} element Root element of the control whose content element
 *     is to be returned.
 * @return {Element} The control's content element.
 */
gadgetide.ui.CollapsibleSideItemRenderer.prototype.getContentElement =
  function(element) {
    return goog.dom.getElementByClass(
      goog.getCssName(this.getCssClass(), 'content'),
      element
    );
  };

