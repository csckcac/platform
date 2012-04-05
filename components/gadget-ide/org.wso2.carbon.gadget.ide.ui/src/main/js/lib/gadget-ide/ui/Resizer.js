goog.provide('gadgetide.ui.Resizer');

goog.require('goog.fx.DragDrop');
goog.require('goog.math.Coordinate');
goog.require('goog.math.Rect');
goog.require('goog.math.Size');
goog.require('goog.ui.Component');

/**
 * @constructor
 * @param {goog.dom.DomHelper=} opt_domHelper Optional DOM helper.
 * @extends {goog.ui.Component}
 */

gadgetide.ui.Resizer = function(opt_domHelper) {
  goog.ui.Component.call(this, opt_domHelper);
  this.handlers_ = {};
};
goog.inherits(gadgetide.ui.Resizer, goog.ui.Component);


/**
 * @type {string}
 * @const
 */
gadgetide.ui.Resizer.CSS_CLASS = 'gide-resizer';

/**
 * following figure shows the upper left corner of the resizer.
 *  +-------+      ^
 *  |       |      |
 *  |       +-- -- | 2 * GAP
 *  |       |      |
 *  +---+---+      v
 *      |
 *
 *      |
 *
 * @type {number}
 * @const
 */
gadgetide.ui.Resizer.BORDER_GAP = 4;

/** @typedef {{
 *    getPos:function(goog.math.Rect):goog.math.Coordinate
 *  }}
 *
 *
 */
gadgetide.ui.Resizer.HandlerDef;

/**
 * @enum {gadgetide.ui.Resizer.HandlerDef}
 */
gadgetide.ui.Resizer.Handler = {
  //TODO: enable all the handlers and implement the functionality.
  /*
   TOP_LEFT:{
   getPos : function(b) {
   return new goog.math.Coordinate(0, 0);
   }
   },
   TOP_MID:{
   getPos : function(b) {
   return new goog.math.Coordinate(
   b.width / 2 + gadgetide.ui.Resizer.BORDER_GAP, 0);
   }
   },
   TOP_RIGHT:{
   getPos : function(b) {
   return new goog.math.Coordinate(
   b.width + 2 * gadgetide.ui.Resizer.BORDER_GAP, 0);
   }
   },
   MID_LEFT:{
   getPos : function(b) {
   return new goog.math.Coordinate(0,
   b.height / 2 + gadgetide.ui.Resizer.BORDER_GAP);
   }
   },
   MID_RIGHT:{
   getPos : function(b) {
   return new goog.math.Coordinate(
   b.width + 2 * gadgetide.ui.Resizer.BORDER_GAP,
   b.height / 2 + gadgetide.ui.Resizer.BORDER_GAP);
   }
   },
   BOT_LEFT:{
   getPos : function(b) {
   return new goog.math.Coordinate(0,
   b.height + 2 * gadgetide.ui.Resizer.BORDER_GAP);
   }
   },
   BOT_MID:{
   getPos : function(b) {
   return new goog.math.Coordinate(
   b.width / 2 + gadgetide.ui.Resizer.BORDER_GAP,
   b.height + 2 * gadgetide.ui.Resizer.BORDER_GAP);
   }
   },
   */
  BOT_RIGHT: {
    getPos: function(b) {
      return new goog.math.Coordinate(
        b.width + 2 * gadgetide.ui.Resizer.BORDER_GAP,
        b.height + 2 * gadgetide.ui.Resizer.BORDER_GAP);
    }
  }
};

/**
 * @type {!Object.<Element>}
 */
gadgetide.ui.Resizer.prototype.handlers_;

/**
 * @inheritDoc
 */
gadgetide.ui.Resizer.prototype.canDecorate = function(element) {
  return false;
};

/**
 * @return {string}
 */
gadgetide.ui.Resizer.prototype.getCssClass = function() {
  return gadgetide.ui.Resizer.CSS_CLASS;
};

/**
 * @inheritDoc
 */
gadgetide.ui.Resizer.prototype.getContentElement = function() {
  return this.contentEl_;
};

/**
 * @inheritDoc
 */
gadgetide.ui.Resizer.prototype.createDom = function() {
  goog.ui.Component.prototype.createDom.call(this);
  var dom = this.getDomHelper();
  var el = this.getElement();

  goog.dom.classes.add(el, this.getCssClass());

  el.appendChild(
    this.borderEl_ = dom.createDom('div',
      goog.getCssName(this.getCssClass(), 'border'))
  );
  var handlersEl = dom.createDom('div',
    goog.getCssName(this.getCssClass(), 'handlers')
  );
  for (var handler in gadgetide.ui.Resizer.Handler) {
    var handlerDef = gadgetide.ui.Resizer.Handler[handler];
    var handlerEl = dom.createDom('div');
    this.handlers_[handler] = handlerEl;
    handlersEl.appendChild(handlerEl);
  }
  el.appendChild(handlersEl);
  this.moverEl_ = dom.createDom('div',
    goog.getCssName(this.getCssClass(), 'mover')
  );
  el.appendChild(this.moverEl_);

  this.contentEl_ = dom.createDom('div',
    goog.getCssName(this.getCssClass(), 'content')
  );
  el.appendChild(this.contentEl_);

};

gadgetide.ui.Resizer.prototype.enterDocument = function() {
  goog.ui.Component.prototype.enterDocument.call(this);
  // both content and border element have a fix offset form wrapper.
  goog.style.setPosition(this.borderEl_,
    gadgetide.ui.Resizer.BORDER_GAP,
    gadgetide.ui.Resizer.BORDER_GAP);
  goog.style.setPosition(this.contentEl_,
    2 * gadgetide.ui.Resizer.BORDER_GAP + 1, //assuming the border size is 1
    2 * gadgetide.ui.Resizer.BORDER_GAP + 1);

  this.getHandler().listen(this.moverEl_,
    [goog.events.EventType.MOUSEDOWN, goog.events.EventType.TOUCHSTART],
    this.handleMoveStart_);

  for (var handler in gadgetide.ui.Resizer.Handler) {
    var handlerDef = gadgetide.ui.Resizer.Handler[handler];
    var handlerEl = this.handlers_[handler];
    //TODO: for now only bottom right handler is working.
    //      implement for the rest.
    if (handlerDef === gadgetide.ui.Resizer.Handler.BOT_RIGHT) {
      this.getHandler().listen(handlerEl,
        [goog.events.EventType.MOUSEDOWN, goog.events.EventType.TOUCHSTART],
        goog.partial(this.handleResizeStart_, handlerDef, handlerEl));
    }
  }

  this.setBounds(new goog.math.Rect(0, 0,
    2 * gadgetide.ui.Resizer.BORDER_GAP,
    2 * gadgetide.ui.Resizer.BORDER_GAP));

  this.getHandler().listen(this.getContentElement(), goog.events.EventType.CLICK,
    function() {
      this.dispatchEvent(gadgetide.ui.Resizer.EventType.SELECT_REQ);
    }
  );

  this.dispatchEvent(gadgetide.ui.Resizer.EventType.SELECT_REQ);
};

/**
 * @param {boolean} select Whether to select or unselect the Resizer.
 */
gadgetide.ui.Resizer.prototype.setSelected = function(select) {
  var css = goog.getCssName(this.getCssClass(), 'selected');
  if (select) {
    goog.dom.classes.add(this.getElement(), css);
  } else {
    goog.dom.classes.remove(this.getElement(), css);
  }
};

/**
 * @param {gadgetide.ui.Resizer.HandlerDef} handlerDef
 * @param {Element} handlerEl
 * @param {!goog.events.BrowserEvent} e MOUSEDOWN or TOUCHSTART event.
 * @private
 */
gadgetide.ui.Resizer.prototype.handleResizeStart_ =
  function(handlerDef, handlerEl, e) {
    this.dispatchEvent(gadgetide.ui.Resizer.EventType.SELECT_REQ);

    var dragger = new goog.fx.Dragger(handlerEl);
    dragger.setLimits(new goog.math.Rect(
      4 * gadgetide.ui.Resizer.BORDER_GAP,
      4 * gadgetide.ui.Resizer.BORDER_GAP,
      Infinity, Infinity));
    dragger.startDrag(e);

    var onDragFn = function(e) {
      this.setSize(new goog.math.Size(
        e.left - 2 * gadgetide.ui.Resizer.BORDER_GAP,
        e.top - 2 * gadgetide.ui.Resizer.BORDER_GAP));
    };
    this.getHandler().listen(dragger, goog.fx.Dragger.EventType.DRAG,
      onDragFn);

    this.getHandler().listenOnce(dragger, goog.fx.Dragger.EventType.END,
      function(e) {
        this.dispatchEvent(gadgetide.ui.Resizer.EventType.RESIZED);
        dragger.dispose();
      });
  };

/**
 * @param {!goog.events.BrowserEvent} e MOUSEDOWN or TOUCHSTART event.
 * @private
 */
gadgetide.ui.Resizer.prototype.handleMoveStart_ = function(e) {
  this.dispatchEvent(gadgetide.ui.Resizer.EventType.SELECT_REQ);

  var dragger = new goog.fx.Dragger(this.getElement(),this.moverEl_,
    this.limits_);
  dragger.startDrag(e);

  this.getHandler().listenOnce(dragger, goog.fx.Dragger.EventType.END,
    function(e) {
      dragger.dispose();
    });
};


/**
 * get the size of the container.
 * @return {!goog.math.Size} size
 */
gadgetide.ui.Resizer.prototype.getSize = function() {
  return goog.style.getSize(this.getContentElement());
};

/**
 * get the bounds of the container.
 * @return {!goog.math.Rect} Bounding rectangle.
 */
gadgetide.ui.Resizer.prototype.getBounds = function() {
  var pos = goog.style.getPosition(this.getElement());
  var size = this.getSize();
  var gap = 2 * gadgetide.ui.Resizer.BORDER_GAP;
  return new goog.math.Rect(pos.x + gap, pos.y + gap, size.width, size.height);
};

/**
 * set the size and the set positions of internal elements relative to wrapper.
 * @param {!goog.math.Size} size
 */
gadgetide.ui.Resizer.prototype.setSize = function(size) {
  // set the size of the wrapper element
  goog.style.setSize(this.getElement(),
    size.width + 4 * gadgetide.ui.Resizer.BORDER_GAP,
    size.height + 4 * gadgetide.ui.Resizer.BORDER_GAP);

  // adjust the border size. position is fix.
  goog.style.setSize(this.borderEl_,
    size.width + 2 * gadgetide.ui.Resizer.BORDER_GAP,
    size.height + 2 * gadgetide.ui.Resizer.BORDER_GAP);

  // adjust the content elements size. position is fix.
  goog.style.setSize(this.contentEl_, size.width, size.height);

  //center the mover (and 'GAP' above the wrapper element).
  goog.style.setPosition(this.moverEl_,
    size.width / 2 + gadgetide.ui.Resizer.BORDER_GAP,
    -3 * gadgetide.ui.Resizer.BORDER_GAP);

  this.updateLimit_(size);

  // place the handlers.
  for (var handler in gadgetide.ui.Resizer.Handler) {
    var handlerDef = gadgetide.ui.Resizer.Handler[handler];
    var handlerEl = this.handlers_[handler];
    goog.style.setPosition(handlerEl, handlerDef.getPos(size));
  }
};

/**
 * @param {!goog.math.Rect} bounds Bounding rectangle for the resizer.
 */
gadgetide.ui.Resizer.prototype.setBounds = function(bounds) {
  // set the position of the wrapper.
  goog.style.setPosition(this.getElement(),
    bounds.left - 2 * gadgetide.ui.Resizer.BORDER_GAP,
    bounds.top - 2 * gadgetide.ui.Resizer.BORDER_GAP);

  this.setSize(new goog.math.Size(bounds.width, bounds.height));
};

/**
 * calculate the draggable limits for the mover. extra 'GAP' is left in each
 * side for the border widths.
 * @param {!goog.math.Size} size
 * @private
 */
gadgetide.ui.Resizer.prototype.updateLimit_ = function(size) {
  var lim = this.getParentBounds_();
  this.limits_ = new goog.math.Rect(0,
    3 * gadgetide.ui.Resizer.BORDER_GAP,
    lim.width - size.width - 5 * gadgetide.ui.Resizer.BORDER_GAP,
    lim.height - size.height - 8 * gadgetide.ui.Resizer.BORDER_GAP);
};

/**
 * @return {!goog.math.Rect}
 */
gadgetide.ui.Resizer.prototype.getParentBounds_ = function() {
  var parent = this.getParent();
  var parentEl;
  if (parent) {
    parentEl = parent.getContentElement();
  } else {
    //by now this should be in the document. so following should an Element.
    parentEl = /** @type {Element} */this.getElement().parentNode;
  }
  return goog.style.getBounds(parentEl);
};

/**
 * @enum {string}
 */
gadgetide.ui.Resizer.EventType = {
  RESIZED : goog.events.getUniqueId('resized'),
  SELECT_REQ : goog.events.getUniqueId('select_req') /* not really selected yet, but requests for getting
   selected*/
};
