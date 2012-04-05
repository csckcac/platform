goog.provide('gadgetide.ui.TerminalViewRenderer');

goog.require('gadgetide.schema');
goog.require('gadgetide.ui.DataSourceViewRenderer');
goog.require('gadgetide.ui.TerminalFieldView');
goog.require('goog.ui.ContainerRenderer');
/**
 * @constructor
 * @extends {gadgetide.ui.DataSourceViewRenderer}
 */
gadgetide.ui.TerminalViewRenderer = function() {
  gadgetide.ui.DataSourceViewRenderer.call(this);
};
goog.inherits(gadgetide.ui.TerminalViewRenderer,
  gadgetide.ui.DataSourceViewRenderer);

goog.addSingletonGetter(gadgetide.ui.TerminalViewRenderer);

/** @type {string} */
gadgetide.ui.TerminalViewRenderer.CSS_CLASS = 'gide-tmview';

/** @inheritDoc */
gadgetide.ui.TerminalViewRenderer.prototype.getCssClass = function() {
  return gadgetide.ui.TerminalViewRenderer.CSS_CLASS;
};

/**
 * @inheritDoc
 */
gadgetide.ui.TerminalViewRenderer.prototype.createFieldViewInternal =
  function(dataSourceView, level, tree) {
  return new gadgetide.ui.TerminalFieldView(tree, dataSourceView.isInput(),
    dataSourceView.getContainer(), level);
};

