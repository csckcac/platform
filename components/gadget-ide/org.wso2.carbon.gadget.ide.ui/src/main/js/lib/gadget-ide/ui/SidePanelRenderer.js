goog.provide('gadgetide.ui.SidePanelRenderer');

goog.require('goog.ui.ContainerRenderer');
/**
 * @constructor
 * @extends {goog.ui.ContainerRenderer}
 */
gadgetide.ui.SidePanelRenderer = function() {
  goog.ui.ContainerRenderer.call(this);
};
goog.inherits(gadgetide.ui.SidePanelRenderer, goog.ui.ContainerRenderer);
goog.addSingletonGetter(gadgetide.ui.SidePanelRenderer);

/** @type {string} */
gadgetide.ui.SidePanelRenderer.CSS_CLASS = 'gide-side';


/** @inheritDoc */
gadgetide.ui.SidePanelRenderer.prototype.getCssClass = function() {
  return gadgetide.ui.SidePanelRenderer.CSS_CLASS;
};

