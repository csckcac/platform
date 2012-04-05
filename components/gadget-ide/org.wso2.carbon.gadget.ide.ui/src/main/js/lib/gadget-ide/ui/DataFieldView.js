/**
 * @fileoverview This is Control visualizes one field(Element) in a schema.
 *
 * @author rcmperea@gmail.com (Manuranga)
 */
goog.provide('gadgetide.ui.DataFieldView');

goog.require('gadgetide.schema');
goog.require('gadgetide.ui.DataFieldViewRenderer');
goog.require('goog.ui.Control');


/**
 *
 * @param {gadgetide.schema.DataField} dataField DataField to be visualized by
 *     this viewer. it will be set as the module of this Control.
 * @param {number=} opt_tabLevel Units of offset. corresponds to the indentation
 *     level.
 * @param {boolean=} opt_iconRight if this is true , icon section will be
 *     rendered right to the name.
 * @param {goog.ui.ControlRenderer=} opt_renderer Renderer used to render
 *    the component; defaults to {@link gadgetide.ui.DataFieldViewRenderer}.
 * @param {goog.dom.DomHelper=} opt_domHelper Optional DOM helper, used for
 *     document interaction.
 * @constructor
 * @extends {goog.ui.Control}
 */
gadgetide.ui.DataFieldView =
  function(dataField, opt_tabLevel, opt_iconRight, opt_renderer,
           opt_domHelper) {
    goog.ui.Control.call(
      this,
      null /* content */,
      opt_renderer || gadgetide.ui.DataFieldViewRenderer.getInstance(),
      opt_domHelper
    );
    /**
     * icon is to right of the text
     * @private
     * @type {boolean}
     */
    this.iconRight_ = Boolean(opt_iconRight);

    /**
     * level of indention
     * @private
     * @type {number}
     */
    this.tabLevel_ = opt_tabLevel || 0;

    this.setSupportedState(goog.ui.Component.State.FOCUSED, false);
    this.setAutoStates(goog.ui.Component.State.HOVER, true);
    this.setModel(dataField);
  };
goog.inherits(gadgetide.ui.DataFieldView, goog.ui.Control);


/**
 * just re-declared to change the return type.
 * @override
 * @return {gadgetide.schema.DataField} the Model.
 */
gadgetide.ui.DataFieldView.prototype.getModel;


/**
 * @return {boolean} is icon being rendered right to the name.
 */
gadgetide.ui.DataFieldView.prototype.isIconRight = function() {
  return this.iconRight_;
};

/**
 * @return {number} number of tab level.
 */
gadgetide.ui.DataFieldView.prototype.getTabLevel = function() {
  return this.tabLevel_;
};

/**
 * @return {Element} DIV element of the icon.
 */
gadgetide.ui.DataFieldView.prototype.getIconElement = function() {
  // Delegate to renderer.
  return this.getRenderer().getIconElement(this.getElement());
};

