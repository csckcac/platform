goog.provide('gadgetide.uielement.LabelRenderer');

goog.require('gadgetide.uielement.UIElementRenderer');
goog.require('goog.ui.registry');


/**
 * @constructor
 * @extends {gadgetide.uielement.UIElementRenderer}
 */
gadgetide.uielement.LabelRenderer = function() {
  gadgetide.uielement.UIElementRenderer.call(this);
};
goog.inherits(gadgetide.uielement.LabelRenderer,
  gadgetide.uielement.UIElementRenderer);
goog.addSingletonGetter(gadgetide.uielement.LabelRenderer);

gadgetide.uielement.LabelRenderer.CSS_CLASS = 'gc-lable';

/**
 * @inheritDoc
 */
gadgetide.uielement.LabelRenderer.prototype.getCssClass = function(control) {
  return gadgetide.uielement.LabelRenderer.CSS_CLASS;
};

/**
 * @inheritDoc
 */
gadgetide.uielement.LabelRenderer.prototype.getSpec = function(uiElm) {
  return [
    {
      name:'Text',
      type:gadgetide.spec.Types.STRING,
      defaultVal:''
    }
//    ,
//    {
//      name:'Bold',
//      type:gadgetide.spec.Types.BOOLEAN,
//      defaultVal:false
//    }
  ];
};

/**
 * @inheritDoc
 */
gadgetide.uielement.LabelRenderer.prototype.createDom = function(control) {
  return control.getDomHelper().createDom('div',
    this.getClassNames(control).join(' '));
};

/**
 * @inheritDoc
 */
gadgetide.uielement.LabelRenderer.prototype.updateSpecMap = function(uiElm, specMap) {
  var map = uiElm.getSpecMap();
  var el = uiElm.getElement();
  goog.dom.setTextContent(el,map['Text']);
};

