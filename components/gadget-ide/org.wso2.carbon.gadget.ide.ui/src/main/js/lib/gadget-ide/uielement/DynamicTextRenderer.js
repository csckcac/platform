goog.provide('gadgetide.uielement.DynamicTextRenderer');

goog.require('gadgetide.uielement.UIElementRenderer');
goog.require('goog.ui.registry');


/**
 * @constructor
 * @extends {gadgetide.uielement.UIElementRenderer}
 */
gadgetide.uielement.DynamicTextRenderer = function() {
  gadgetide.uielement.UIElementRenderer.call(this);
};
goog.inherits(gadgetide.uielement.DynamicTextRenderer,
  gadgetide.uielement.UIElementRenderer);
goog.addSingletonGetter(gadgetide.uielement.DynamicTextRenderer);

gadgetide.uielement.DynamicTextRenderer.CSS_CLASS = 'gc-dyntext';

/**
 * @inheritDoc
 */
gadgetide.uielement.DynamicTextRenderer.prototype.createDom = function(control) {
  return control.getDomHelper().createDom('div',
    this.getClassNames(control).join(' ')
  );
};

/**
 * @inheritDoc
 */
gadgetide.uielement.DynamicTextRenderer.prototype.getCssClass = function(control) {
  return gadgetide.uielement.DynamicTextRenderer.CSS_CLASS;
};

/**
 * @inheritDoc
 */
gadgetide.uielement.DynamicTextRenderer.prototype.canDecorate = function(element) {
  return element.tagName === 'DIV';
};

/**
 * @inheritDoc
 */
gadgetide.uielement.DynamicTextRenderer.prototype.updateUiData = function(uiElm, input) {
  var text = input && gadgetide.schema.getValue(input['text']) || '';
  if (goog.isObject(text)) {
    try {
      text = gadgetide.client.util.JSON_SERIALIZE(text);
    } catch(ex) {
    }
  }
  goog.dom.setTextContent(uiElm.getElement(), String(text));
};

/**
 * @inheritDoc
 */
gadgetide.uielement.DynamicTextRenderer.prototype.getContext = function() {
  return null;
};


