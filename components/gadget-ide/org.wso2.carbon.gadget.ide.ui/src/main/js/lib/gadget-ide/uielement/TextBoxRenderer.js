goog.provide('gadgetide.uielement.TextBoxRenderer');

goog.require('gadgetide.uielement.UIElementRenderer');
goog.require('goog.ui.registry');


/**
 * @constructor
 * @extends {gadgetide.uielement.UIElementRenderer}
 */
gadgetide.uielement.TextBoxRenderer = function() {
  gadgetide.uielement.UIElementRenderer.call(this);
};
goog.inherits(gadgetide.uielement.TextBoxRenderer,
  gadgetide.uielement.UIElementRenderer);
goog.addSingletonGetter(gadgetide.uielement.TextBoxRenderer);

gadgetide.uielement.TextBoxRenderer.CSS_CLASS = 'gc-text';

/**
 * @inheritDoc
 */
gadgetide.uielement.TextBoxRenderer.prototype.createDom = function(control) {
  return control.getDomHelper().createDom('input', {
    'type': 'text',
    'class': this.getClassNames(control).join(' ')
  });
};

/**
 * @inheritDoc
 */
gadgetide.uielement.TextBoxRenderer.prototype.getCssClass = function(control) {
  return gadgetide.uielement.TextBoxRenderer.CSS_CLASS;
};

/**
 * @inheritDoc
 */
gadgetide.uielement.TextBoxRenderer.prototype.canDecorate = function(element) {
  return element.tagName === 'INPUT' && element.type === 'text';
};

/**
 * @inheritDoc
 */
gadgetide.uielement.TextBoxRenderer.prototype.getContext = function(uiElm) {
  return {'text': uiElm.getElement().value};
};

