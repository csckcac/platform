/**
 * @fileoverview This is Control visualizes a schema.
 *
 * @author rcmperea@gmail.com (Manuranga)
 */
goog.require('goog.ui.ControlRenderer');
goog.require('goog.ui.INLINE_BLOCK_CLASSNAME');

goog.provide('gadgetide.ui.DataFieldViewRenderer');


/**
 * @constructor
 * @extends {goog.ui.ControlRenderer}
 */
gadgetide.ui.DataFieldViewRenderer = function() {
  goog.ui.ControlRenderer.call(this);
};
goog.inherits(gadgetide.ui.DataFieldViewRenderer, goog.ui.ControlRenderer);
goog.addSingletonGetter(gadgetide.ui.DataFieldViewRenderer);


/** @type {string} */
gadgetide.ui.DataFieldViewRenderer.CSS_CLASS = 'gide-dfview';


/** @inheritDoc */
gadgetide.ui.DataFieldViewRenderer.prototype.getCssClass = function() {
  return gadgetide.ui.DataFieldViewRenderer.CSS_CLASS;
};


/**
 * created dom will have following structure.
 *
 * <pre>
 *
 * <div class="c c-right">
 *    <div class="c-tab">&nbsp;</div>
 *    ....
 *    <div class="c-tab">&nbsp;</div>
 *
 *    <div class="c-icon"></div>
 *    <div class="c-name" title="http://namespce : name">name</div>
 * </div>
 *
 * </pre>
 *
 * @param {gadgetide.ui.DataFieldView} dataFieldView DataFieldView to be
 *     rendered.
 * @return {Element} Root element for the DataFieldViewRenderer.
 */
gadgetide.ui.DataFieldViewRenderer.prototype.createDom =
  function(dataFieldView) {

    var dom = dataFieldView.getDomHelper();
    var model = dataFieldView.getModel();
    var baseCss = this.getStructuralCssClass();
    var inlineBlock = goog.ui.INLINE_BLOCK_CLASSNAME + ' ';

    var tooltip = goog.isDefAndNotNull(model.xmlns) ?
      (model.xmlns + ' : ') : '';


    var subDom = [];

    //adds indentations
    for (var i = 0; i < dataFieldView.getTabLevel(); i++) {
      subDom.push(dom.createDom(
        'div',
        goog.getCssName(baseCss, 'tab'),
        /* non-breaking space */ '\u00A0'
      ));
    }

    //adds empty DIV for icon
    subDom.push(
      dom.createDom(
        'div',
        goog.getCssName(baseCss, 'icon')
      )
    );

    //adds caption
    var nameAttributes = {
      'class': goog.getCssName(baseCss, 'name'),
      'title': tooltip + model.name
    };
    subDom.push(
      dom.createDom(
        'div',
        nameAttributes,
        model.name
      )
    );


    var el = dom.createDom(
      'div',
      baseCss + ' ' + (dataFieldView.isIconRight() ?
        goog.getCssName(baseCss, 'right') :
        goog.getCssName(baseCss, 'left')),
      subDom
    );

    return el;
  };

/**
 * @param {Element} element Root element of the control whose icon element
 *     is to be returned.
 * @return {Element} DIV element of the icon.
 */
gadgetide.ui.DataFieldViewRenderer.prototype.getIconElement =
  function(element) {
    return goog.dom.getElementByClass(
      goog.getCssName(this.getCssClass(), 'icon'), element);
};
