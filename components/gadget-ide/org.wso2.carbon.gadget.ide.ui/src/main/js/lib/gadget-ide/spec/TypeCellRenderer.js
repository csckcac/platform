goog.provide('gadgetide.spec.TypeCellRenderer');

goog.require('goog.ui.INLINE_BLOCK_CLASSNAME');

/**
 * @constructor
 */
gadgetide.spec.TypeCellRenderer = function() {
};

/**
 * @param {gadgetide.spec.TypeCell} cell
 * @param {*} value
 */
gadgetide.spec.TypeCellRenderer.prototype.renderSelected = function(cell, value) {
  var element = cell.getElement();
  var dom = goog.dom.getDomHelper(element);
  var content = dom.createDom('input', { 'type':'text', 'value': String(value),
    'class':'gide-tcell-input'
  });
  element.appendChild(content);
  var handler = cell.getHandler();

  handler.listen(element, goog.events.EventType.KEYUP, function(e) {
    /** @type {gadgetide.spec.TypeCell} */
    var typeCell = this;
    typeCell.fireChange(content.value);
  });
  handler.listen(content, goog.events.EventType.BLUR, function(e) {
    /** @type {gadgetide.spec.TypeCell} */
    var typeCell = this;
    typeCell.fireUnselect();
  });
  // can use this as a hack to fix the alignment bug.
  // el.appendChild(dom.createDom('div',goog.ui.INLINE_BLOCK_CLASSNAME ,
  // '\u00A0'/*non-breaking space*/));
  content.focus();
};

/**
 * @param {gadgetide.spec.TypeCell} cell
 * @param {*} value
 */
gadgetide.spec.TypeCellRenderer.prototype.renderUnselected = function(cell, value) {
  goog.dom.setTextContent(cell.getElement(), /* non-breaking space,to stop ui from breaking when
   value is empty */ '\u00A0' + String(value));
};

