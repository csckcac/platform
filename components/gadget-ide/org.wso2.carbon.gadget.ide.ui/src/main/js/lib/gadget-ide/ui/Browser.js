goog.provide('gadgetide.ui.Browser');
goog.provide('gadgetide.ui.BrowserRenderer');

goog.require('goog.ui.tree.TreeControl');

/**
 *
 * @param {?goog.ui.ContainerRenderer=} opt_renderer Renderer used to render or
 *     decorate the container; defaults to {@link goog.ui.ContainerRenderer}.
 * @param {?goog.dom.DomHelper=} opt_domHelper DOM helper, used for document
 *     interaction.
 * @constructor
 * @extends {goog.ui.Container}
 */
gadgetide.ui.Browser = function(opt_renderer, opt_domHelper) {
  goog.ui.Container.call(goog.ui.Container.Orientation.VERTICAL,
    opt_renderer ||new gadgetide.ui.BrowserRenderer(), opt_domHelper);
};
goog.inherits(gadgetide.ui.Browser, goog.ui.Container);


gadgetide.ui.Browser.prototype.change = function() {

};

/**
 * @enum {string}
 */
gadgetide.ui.Browser.EventType = {
  SAVE:'save'
};

