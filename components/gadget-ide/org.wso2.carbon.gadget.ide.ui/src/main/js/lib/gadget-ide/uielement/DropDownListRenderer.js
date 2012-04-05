goog.provide('gadgetide.uielement.DropDownListRenderer');

goog.require('gadgetide.uielement.UIElementRenderer');
goog.require('goog.ui.registry');
goog.require('goog.events');
goog.require('goog.ui.ComboBox');


/**
 * @constructor
 * @extends {gadgetide.uielement.UIElementRenderer}
 */
gadgetide.uielement.DropDownListRenderer = function() {
  gadgetide.uielement.UIElementRenderer.call(this);
};
goog.inherits(gadgetide.uielement.DropDownListRenderer,
  gadgetide.uielement.UIElementRenderer);
goog.addSingletonGetter(gadgetide.uielement.DropDownListRenderer);

gadgetide.uielement.DropDownListRenderer.CSS_CLASS = 'gc-combo';

/**
 * @inheritDoc
 */
gadgetide.uielement.DropDownListRenderer.prototype.createDom = function(control) {
  var dom = control.getDomHelper();
  return dom.createDom('div', this.getClassNames(control),
    dom.createDom('select'));
};

/**
 * @inheritDoc
 */
gadgetide.uielement.DropDownListRenderer.prototype.getCssClass = function(control) {
  return gadgetide.uielement.DropDownListRenderer.CSS_CLASS;
};

///**
// * @inheritDoc
// */
//gadgetide.uielement.DropDownListRenderer.prototype.initializeDom = function(control) {
//
//  };


/**
 * @inheritDoc
 */
gadgetide.uielement.DropDownListRenderer.prototype.getContext = function(uiElm) {
  var select = uiElm.getElement().firstChild;
  var selected = null;
  if (select.selectedIndex >= 0) {
    selected = select.options[select.selectedIndex].value;
  }
  return {'text': selected };
};

/**
 * @inheritDoc
 */
gadgetide.uielement.DropDownListRenderer.prototype.updateUiData = function(uiElm, input) {
  if (input && input['text'] && goog.isArray(input['text'])) {
    var arr = input['text'];
    var oldSelect = uiElm.getElement().firstChild;
    var selected = oldSelect.selectedIndex;
    if (selected < 0 || selected >= arr.length) {
      selected = 0;
    }
    var dom = uiElm.getDomHelper();
    var select = dom.createDom('select');
    for (var i = 0; i < arr.length; i++) {
      var itemText = arr[i];
      /** @type{string} */
      var value = /** @type{string} */ gadgetide.schema.getValue(itemText);
      var item = dom.createDom('option', {'value':value}, value);
      select.appendChild(item);
    }
    select.selectedIndex = selected;
    goog.dom.removeNode(oldSelect);
    uiElm.getElement().appendChild(select);
  }
};
