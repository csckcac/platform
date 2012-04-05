goog.provide('gadgetide.spec.StringCellRenderer');

goog.require('gadgetide.spec.TypeCellRenderer');

/**
 * @constructor
 * @extends {gadgetide.spec.TypeCellRenderer}
 */
gadgetide.spec.StringCellRenderer = function() {
};
goog.inherits(gadgetide.spec.StringCellRenderer,gadgetide.spec.TypeCellRenderer);

/**
 * @inheritDoc
 */
gadgetide.spec.StringCellRenderer.prototype.renderUnselected = function(cell, value) {
  goog.dom.setTextContent(cell.getElement(), '"' + String(value) + '"');
};

