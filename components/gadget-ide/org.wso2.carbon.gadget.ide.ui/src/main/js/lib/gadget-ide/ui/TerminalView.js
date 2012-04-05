goog.provide('gadgetide.ui.TerminalView');

goog.require('gadgetide.ui.DataSourceView');
goog.require('gadgetide.ui.TerminalViewRenderer');
goog.require('goog.ui.Container');

/**
 * @param {gadgetide.schema.DataFieldTree} dataFieldTree dataFieldTree to be
 *   visualized.
 * @param {boolean} isInput Input terminals or Output terminals to be rendered.
 * @param {?WireIt.Container} opt_container Container to which the terminals
 *     will get added.
 * @param {?goog.ui.ContainerRenderer=} opt_renderer Renderer used to render or
 *     decorate the container; defaults to {@link goog.ui.ContainerRenderer}.
 * @param {?goog.dom.DomHelper=} opt_domHelper DOM helper, used for document
 *     interaction.
 * @constructor
 * @extends {gadgetide.ui.DataSourceView}
 */
gadgetide.ui.TerminalView =
  function(dataFieldTree, isInput, opt_container, opt_renderer,
           opt_domHelper) {
    gadgetide.ui.DataSourceView.call(
      this,
      dataFieldTree,
      !isInput /* input are left aligned */,
      opt_renderer || gadgetide.ui.TerminalViewRenderer.getInstance(),
      opt_domHelper
    );
    /**
     * @private
     * @type {boolean}
     */
    this.isInput_ = isInput;

    this.container_ = opt_container || null;
  };
goog.inherits(gadgetide.ui.TerminalView, gadgetide.ui.DataSourceView);

/**
 * just re-declared to change the return type.
 * @return {gadgetide.schema.DataFieldTree} The Model.
 * @override
 */
gadgetide.ui.TerminalView.prototype.getModel;

/**
 * gives the state of Terminal connectivity.
 * @return {boolean} 'true' if this has input terminals.
 */
gadgetide.ui.TerminalView.prototype.isInput = function() {
  return this.isInput_;
};

/**
 * @return {WireIt.Container} Container associated with the terminals.
 */
gadgetide.ui.TerminalView.prototype.getContainer = function() {
  return this.container_;
};

