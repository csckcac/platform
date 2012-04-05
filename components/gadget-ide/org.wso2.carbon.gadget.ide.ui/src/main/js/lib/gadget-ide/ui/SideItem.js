goog.provide('gadgetide.ui.SideItem');

goog.require('gadgetide.ui.CollapsibleSideItemRenderer');
goog.require('goog.ui.Control');


/**
 *
 * @param {string} value Label to be displayed.
 * @param {?string} path
 * @param {string=} opt_iconClass icon class to be to attached to icon DIV,
 *   this string will be prepended by render css class fragment
 *   before attaching.
 * @param {goog.ui.ControlRenderer=} opt_renderer Renderer used to render
 *    the component; defaults to {@link gadgetide.ui.DataFieldViewRenderer}.
 * @param {goog.dom.DomHelper=} opt_domHelper Optional DOM helper, used for
 *     document interaction.
 * @constructor
 * @extends {goog.ui.Control}
 */
gadgetide.ui.SideItem =
  function(value, path, opt_iconClass, opt_renderer, opt_domHelper) {
    goog.ui.Control.call(
      this,
      null /* content */,
      opt_renderer || gadgetide.ui.CollapsibleSideItemRenderer.getInstance(),
      opt_domHelper
    );

    this.setSupportedState(goog.ui.Component.State.FOCUSED, false);
    this.setAutoStates(goog.ui.Component.State.HOVER, true);
    this.setContent(value);
    this.path_ = path;
  };
goog.inherits(gadgetide.ui.SideItem, goog.ui.Control);

/**
 * @type {?string}
 * @private
 */
gadgetide.ui.SideItem.prototype.path_;

/**
 * get the Label caption.
 * @return {string} Caption.
 */
gadgetide.ui.SideItem.prototype.getName = function() {
  var pathParts = this.path_.split(gadgetide.ui.SidePanel.PATH_SEPARATOR);
  return  pathParts[pathParts.length-1];
};

/**
 * @return {?string} Caption.
 */
gadgetide.ui.SideItem.prototype.getPath = function() {
  return this.path_;
};

