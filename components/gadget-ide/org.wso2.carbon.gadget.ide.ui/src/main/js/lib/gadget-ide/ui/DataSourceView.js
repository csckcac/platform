goog.provide('gadgetide.ui.DataSourceView');

goog.require('gadgetide.ui.DataSourceViewRenderer');
goog.require('goog.ui.Container');

/**
 * @param {gadgetide.schema.DataFieldTree} dataFieldTree DataFieldTree to be
 *   visualized.
 * @param {boolean=} opt_iconRight if this is true , icon DIV will be
 *     rendered right to the name.
 * @param {?goog.ui.ContainerRenderer=} opt_renderer Renderer used to render or
 *     decorate the container; defaults to {@link goog.ui.ContainerRenderer}.
 * @param {?goog.dom.DomHelper=} opt_domHelper DOM helper, used for document
 *     interaction.
 * @constructor
 * @extends {goog.ui.Container}
 */
gadgetide.ui.DataSourceView =
  function(dataFieldTree, opt_iconRight, opt_renderer, opt_domHelper) {
    goog.ui.Container.call(
      this,
      goog.ui.Container.Orientation.VERTICAL,
      opt_renderer || gadgetide.ui.DataSourceViewRenderer.getInstance(),
      opt_domHelper
    );
    /**
     * @private
     * @type {boolean}
     */
    this.iconRight_ = (opt_iconRight == true);
    this.setModel(dataFieldTree || null);
    this.setFocusable(false);
  };
goog.inherits(gadgetide.ui.DataSourceView, goog.ui.Container);

/**
 * just re-declared to change the return type.
 * @return {gadgetide.schema.DataFieldTree} the Model.
 * @override
 */
gadgetide.ui.DataSourceView.prototype.getModel;

/** @inheritDoc */
gadgetide.ui.DataSourceView.prototype.enterDocument = function() {
  goog.ui.Container.prototype.enterDocument.call(this);
};


/**
 * @return {boolean} is icon DIV is right to name.
 */
gadgetide.ui.DataSourceView.prototype.isIconRight = function() {
  return this.iconRight_;
};
