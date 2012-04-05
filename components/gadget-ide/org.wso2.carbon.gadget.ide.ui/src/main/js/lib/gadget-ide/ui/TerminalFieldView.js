goog.provide('gadgetide.ui.TerminalFieldView');

goog.require('gadgetide.schema');
goog.require('gadgetide.ui.DataFieldView');
goog.require('gadgetide.ui.TerminalFieldViewRenderer');
goog.require('goog.ui.Control');


/**
 *
 * @param {gadgetide.schema.DataField} dataField DataField to be visualized by
 *  this viewer. it will be set as the model of this Control.
 * @param {boolean} isInput Input terminals or Output terminals to be rendered.
 * @param {?WireIt.Container} opt_container Container to which the terminals
 *  will get added.
 * @param {number=} opt_tabLevel Units of offset. corresponds to the
 *  indentation level.
 * @param {goog.ui.ControlRenderer=} opt_renderer Renderer used to render
 *  the component; defaults to {@link gadgetide.ui.DataFieldViewRenderer}.
 * @param {goog.dom.DomHelper=} opt_domHelper Optional DOM helper, used for
 *  document interaction.
 * @constructor
 * @extends {gadgetide.ui.DataFieldView}
 */
gadgetide.ui.TerminalFieldView =
  function(dataField, isInput, opt_container, opt_tabLevel,
           opt_renderer, opt_domHelper) {
    gadgetide.ui.DataFieldView.call(this, dataField, opt_tabLevel,
      !isInput /* input terminals appear to the left */,
      opt_renderer || gadgetide.ui.TerminalFieldViewRenderer.getInstance(),
      opt_domHelper
    );

    this.container_ = opt_container || null;
    this.isInput_ = isInput;
  };
goog.inherits(gadgetide.ui.TerminalFieldView, gadgetide.ui.DataFieldView);

/**
 * @return {WireIt.Container} Container associated with the terminal.
 */
gadgetide.ui.TerminalFieldView.prototype.getContainer = function() {
  return this.container_;
};

/**
 * gives the state of Terminal connectivity.
 * @return {boolean} 'true' if this has input terminals.
 */
gadgetide.ui.TerminalFieldView.prototype.isInput = function() {
  return this.isInput_;
};

gadgetide.ui.TerminalFieldView.prototype.fireRemoveWire = function(isInput) {
  this.dispatchEvent(
    isInput ?
      gadgetide.ui.TerminalFieldView.EventType.WIRE_REMOVE_IN :
      gadgetide.ui.TerminalFieldView.EventType.WIRE_REMOVE_OUT);
};


gadgetide.ui.TerminalFieldView.prototype.fireAddWire = function(isInput) {
  this.dispatchEvent(
    isInput ?
      gadgetide.ui.TerminalFieldView.EventType.WIRE_ADDED_IN :
      gadgetide.ui.TerminalFieldView.EventType.WIRE_ADDED_OUT);
};

/** @enum {string} */
gadgetide.ui.TerminalFieldView.EventType = {
  WIRE_ADDED_IN: goog.events.getUniqueId('wire_added_in'),
  WIRE_ADDED_OUT: goog.events.getUniqueId('wire_added_out'),
  WIRE_REMOVE_IN: goog.events.getUniqueId('wire_remove_in'),
  WIRE_REMOVE_OUT: goog.events.getUniqueId('wire_remove_out')
};
