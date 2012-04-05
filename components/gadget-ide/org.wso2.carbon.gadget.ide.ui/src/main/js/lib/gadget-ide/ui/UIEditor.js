goog.provide('gadgetide.ui.UIEditor');

goog.require('gadgetide.ui.Resizer');
goog.require('goog.ui.Component');

/**
 * @constructor
 * @param {goog.dom.DomHelper=} opt_domHelper Optional DOM helper.
 * @extends {goog.ui.Component}
 */

gadgetide.ui.UIEditor = function(opt_domHelper) {
  goog.ui.Component.call(this, opt_domHelper);
};
goog.inherits(gadgetide.ui.UIEditor, goog.ui.Component);

/**
 * Element for the content area.
 * @type {Element}
 * @private
 */
gadgetide.ui.UIEditor.prototype.contentEl_ = null;


/**
 * @type {string}
 * @const
 */
gadgetide.ui.UIEditor.CSS_CLASS = 'gide-uieidt';


/**
 * @type {number}
 * @const
 */
gadgetide.ui.UIEditor.INTI_WIDTH = 400;

/**
 * @type {number}
 * @const
 */
gadgetide.ui.UIEditor.INTI_HEIGHT = 300;

/**
 * @inheritDoc
 */
gadgetide.ui.UIEditor.prototype.canDecorate = function(element) {
  return false;
};

/**
 * @inheritDoc
 */
gadgetide.ui.UIEditor.prototype.createDom = function() {
  goog.ui.Component.prototype.createDom.call(this);
  this.decorateInternal(this.getElement());
};

/**
 * @return {Element} The content element.
 */
gadgetide.ui.UIEditor.prototype.getContentElement = function() {
  return this.contentEl_;
};

/**
 * @inheritDoc
 */
gadgetide.ui.UIEditor.prototype.decorateInternal = function(element) {
  goog.dom.classes.add(element, gadgetide.ui.UIEditor.CSS_CLASS);
  var dom = this.getDomHelper();
  var contentClass = goog.getCssName(gadgetide.ui.UIEditor.CSS_CLASS, 'content');
  var contentEl = goog.dom.getElementByClass(contentClass);
  if (!contentEl) {
    element.appendChild(dom.createDom('div',
      goog.getCssName(gadgetide.ui.UIEditor.CSS_CLASS, 'border'),
      //TODO: make the title dynamic
      dom.createDom('div',
        goog.getCssName(gadgetide.ui.UIEditor.CSS_CLASS, 'title'),
        'My Gadget'),
      contentEl = dom.createDom('div', contentClass)
    ));
  }
  this.handlerEl_ = dom.createDom('div',
    goog.getCssName(gadgetide.ui.UIEditor.CSS_CLASS, 'corner'));
  contentEl.appendChild(this.handlerEl_);
  this.contentEl_ = contentEl;
};

gadgetide.ui.UIEditor.prototype.enterDocument = function() {
  goog.ui.Component.prototype.enterDocument.call(this);
  this.getHandler().listen(this.getContentElement(), goog.events.EventType.CLICK,
    this.handleClick_);

  this.getHandler().listen(this.handlerEl_,
    [goog.events.EventType.MOUSEDOWN, goog.events.EventType.TOUCHSTART],
    this.handleResizeStart_);

  goog.style.setSize(this.contentEl_,
    gadgetide.ui.UIEditor.INTI_WIDTH,
    gadgetide.ui.UIEditor.INTI_HEIGHT);

  goog.style.setPosition(this.handlerEl_,
    gadgetide.ui.UIEditor.INTI_WIDTH - 16,
    gadgetide.ui.UIEditor.INTI_HEIGHT - 16);

};

gadgetide.ui.UIEditor.prototype.handleClick_ = function(e) {
  if (e.target === this.getContentElement()) {
    this.dispatchEvent(goog.events.EventType.CLICK);
  }
};


/**
 * @param {!goog.events.BrowserEvent} e MOUSEDOWN or TOUCHSTART event.
 * @private
 */
gadgetide.ui.UIEditor.prototype.handleResizeStart_ = function(e) {

  var dragger = new goog.fx.Dragger(this.handlerEl_);
  var pos = goog.style.getPosition(this.contentEl_);
  dragger.setLimits(new goog.math.Rect(
    pos.x + 100, pos.y, Infinity, Infinity));
  dragger.startDrag(e);

  var onDragFn = function(e) {
    goog.style.setSize(this.contentEl_,
      e.left + 16, e.top + 16);
  };
  this.getHandler().listen(dragger, goog.fx.Dragger.EventType.DRAG,
    onDragFn);

  this.getHandler().listenOnce(dragger, goog.fx.Dragger.EventType.END,
    function(e) {
      this.dispatchEvent(gadgetide.ui.Resizer.EventType.RESIZED);
      dragger.dispose();
    });
};

