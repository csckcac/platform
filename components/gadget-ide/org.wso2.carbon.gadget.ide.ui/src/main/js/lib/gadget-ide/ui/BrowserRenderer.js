goog.provide('gadgetide.ui.BrowserRenderer');

goog.require('goog.ui.ContainerRenderer');
/**
 * @constructor
 * @extends {goog.ui.ContainerRenderer}
 */
gadgetide.ui.BrowserRenderer = function() {
  goog.ui.ContainerRenderer.call(this);
};
goog.inherits(gadgetide.ui.BrowserRenderer, goog.ui.ContainerRenderer);
goog.addSingletonGetter(gadgetide.ui.BrowserRenderer);

/** @type {string} */
gadgetide.ui.BrowserRenderer.CSS_CLASS = 'gide-files';


/** @inheritDoc */
gadgetide.ui.BrowserRenderer.prototype.getCssClass = function() {
  return gadgetide.ui.BrowserRenderer.CSS_CLASS;
};

