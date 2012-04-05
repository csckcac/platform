goog.provide('gadgetide.ui.DataSourceContainer');
goog.provide('gadgetide.ui.DataSourceContainer.EventType');

goog.require('gadgetide.schema');
goog.require('goog.disposable.IDisposable');
goog.require('goog.events');
goog.require('goog.events.EventHandler');
goog.require('goog.events.EventTarget');
goog.require('goog.events.EventType');
goog.require('goog.style');

/**
 * @constructor
 * @param {WireIt.Layer} layer Layer to which the Container will get rendered
 *   during construction.(due to WireIt [mis]architecture is it impossible to
 *   construct a Container independently and attache it to a layer afterwords).
 * @extends {WireIt.Container}
 * @implements {goog.disposable.IDisposable}
 */
gadgetide.ui.DataSourceContainer = function(layer) {

  this.eventTarget_ = new goog.events.EventTarget();
  // HACK: to stop the compiler form renaming some methods, which should
  // remain under the same name because wireIt is calling it form outside.
  this['render'] = this.render;
  this['onCloseButton'] = this.onCloseButton;

  WireIt.Container.call(this, {
    'resizable': false,
    /* providing an empty title here,so WireIt will add a 'span' */
    'title': ' '
  }, layer);

  layer.addContainerDirect(this);
};
goog.inherits(gadgetide.ui.DataSourceContainer, WireIt.Container);


/**
 * Whether the object has been disposed of.
 * @type {boolean}
 * @private
 */
gadgetide.ui.DataSourceContainer.prototype.disposed_ = false;

/**
 * Event handler.
 * @type {goog.events.EventHandler}
 * @private
 */
gadgetide.ui.DataSourceContainer.prototype.handler_;

/**
 * @see goog.ui.Component.prototype.dom_
 * @type {goog.dom.DomHelper}
 */
gadgetide.ui.DataSourceContainer.prototype.dom_;

/**
 * The "delegate" EventTarget
 *
 * We want this class to extend {@code EventTarget} class, but since we have to
 * sub-class it from WireIt.Container, this is not possible.it would have been
 * ideal to change the WireIt.Container class to extend {@code EventTarget},
 * but to avoid modifying WireIt code, delegation design pattern is used.
 *
 * LIMITATION: because of the way this is implemented, goog.events.listen can't
 * be used directly to listen this object. use {@code applyEventTarget}
 * instead.
 *
 * @type {goog.events.EventTarget}
 * @private
 */
gadgetide.ui.DataSourceContainer.prototype.eventTarget_;


/**
 * partially applies the EventTarget as the first argument to the given
 * function
 *
 * usage:
 * <code>
 *    container.applyEventTarget(goog.events.listen)(CLICK_CLOSE,
 *      function(e){
 *        //handle close
 *      });
 * </code>
 *
 * also can be used with EventHandler or any other function that accepts
 * EventTarget as the first arg.
 *
 * @param {Function} fn Function who's first argument is a EventTarget.
 * @param {Object=} opt_self  Object to be bind to 'this'.
 * @return {!Function} A partially-applied form of the function.
 */
gadgetide.ui.DataSourceContainer.prototype.applyEventTarget =
  function(fn, opt_self) {
    if (goog.isDefAndNotNull(opt_self)) {
      return goog.bind(fn, opt_self, this.eventTarget_);
    }
    return goog.partial(fn, this.eventTarget_);
  };


/**
 * Sets the parent of this event target to use for bubbling.
 * @see goog.events.EventTarget.prototype.setParentEventTarget
 * @param {goog.events.EventTarget?} parent Parent EventTarget (null if none).
 */
gadgetide.ui.DataSourceContainer.prototype.setParentEventTarget =
  function(parent) {
    this.eventTarget_.setParentEventTarget(parent);
  };


/**
 * @inheritDoc
 */
gadgetide.ui.DataSourceContainer.prototype.render = function() {
  WireIt.Container.prototype.render.call(this);
  this.getHandler().listen(this.getDomHelper().getWindow(),
    goog.events.EventType.RESIZE,
    this.handleWindowResize);

};

/**
 * fixes a bug in WireIt where, a layer with some containers will not get it's
 * X,Y constraints set correctly when you resize the browser window.
 * @see https://github.com/neyric/wireit/issues/52
 */
gadgetide.ui.DataSourceContainer.prototype.handleWindowResize = function() {

  /**
   * @type {!goog.math.Coordinate}
   */
  var offset = goog.style.getRelativePosition(this.el, this.layer.el);

  this.dd.setXConstraint(offset.x);
  this.dd.setYConstraint(offset.y);
};


/**
 * @inheritDoc
 */
gadgetide.ui.DataSourceContainer.prototype.onCloseButton = function(e, args) {
  this.eventTarget_.dispatchEvent(
    gadgetide.ui.DataSourceContainer.EventType.CLICK_CLOSE);
};


/**
 * @return {boolean} Whether the object has been disposed of.
 */
gadgetide.ui.DataSourceContainer.prototype.isDisposed = function() {
  return this.disposed_;
};

/**
 * add a terminal inside a given DIV
 *
 * @param {Element} el DIV to add the terminal in.
 * @param {!Object} config
 */
gadgetide.ui.DataSourceContainer.prototype.addTerminalTo = function(el, config) {
  /*
   * HACK: WireIt will add the terminal directly in to the el element.
   * but we want it to be added to terminalEl. so we temporary change the
   * containers el to terminalEl.
   */
  var temp = this.el;
  this.el = el;
  var terminal = this.addTerminal(config);
  this.el = temp;
  return terminal;
};


/**
 * Disposes of the object. invalidate the event handler, and disappear form the
 * screen.
 *
 * since WireIt objects doesn't provide destructors, they may not be disposed
 * correctly.
 *
 * TODO: really clear the DOM nodes inside WireIt Container
 *
 * @return {void} Nothing.
 */
gadgetide.ui.DataSourceContainer.prototype.dispose = function() {
  this.layer.removeContainer(this);
  delete this.el;
  delete this.bodyEl;
  delete this.dd;
  delete this.layer;

  this.handler_.dispose();
  this.eventTarget_.dispose();

  delete this.dom_;
  delete this.handler_;
  delete this.eventTarget_;

  this.disposed_ = true;
};

/**
 * Returns the dom helper that is being used on this container.
 * @return {!goog.dom.DomHelper} The dom helper used on this container.
 */
gadgetide.ui.DataSourceContainer.prototype.getDomHelper = function() {
  return this.dom_ ||
    (this.dom_ = goog.dom.getDomHelper(this.el));
};

/**
 * Returns the event handler for this component, lazily created the first time
 * this method is called.
 * @return {!goog.events.EventHandler} Event handler for this container.
 * @protected
 */
gadgetide.ui.DataSourceContainer.prototype.getHandler = function() {
  return this.handler_ ||
    (this.handler_ = new goog.events.EventHandler(this));
};

/**
 * @param {gadgetide.schema.DataFieldTree} dataFieldTree DataFieldTree to be
 *    visualized as the input.
 * @return {void} Nothing.
 */
gadgetide.ui.DataSourceContainer.prototype.updateInputSchema =
  goog.abstractMethod;

/**
 * @param {gadgetide.schema.DataFieldTree} dataFieldTree DataFieldTree to be
 *    visualized as the output.
 * @return {void} Nothing.
 */
gadgetide.ui.DataSourceContainer.prototype.updateOutputSchema =
  goog.abstractMethod;


/**
 * this will get called after the Container has been rendered.
 * override this method to proved the default config if there is one.
 * this can be achieved by dispatching a CONFIG_CHANGE event
 *
 * @return {void} Nothing.
 */
gadgetide.ui.DataSourceContainer.prototype.initConfig =
  goog.abstractMethod;

/** @enum {string} */
gadgetide.ui.DataSourceContainer.EventType = {
  CONFIG_CHANGE: goog.events.getUniqueId('config_changed'),
  CLICK_CLOSE: goog.events.getUniqueId('click_close')
};


/**
 * Object representing a config change event. occurs when a user confirms
 * a change done to a configuration of a a DataSourceContainer.
 *
 * @param {gadgetide.ui.DataSourceContainer} target DataSourceContainer
 *   initiating event.
 * @param {!Object} config JSON object containing configuration.
 * @extends {goog.events.Event}
 * @constructor
 */
gadgetide.ui.DataSourceConfigChangeEvent = function(target, config) {
  goog.events.Event.call(this,
    gadgetide.ui.DataSourceContainer.EventType.CONFIG_CHANGE,
    target);

  this.config = config;
};
goog.inherits(gadgetide.ui.DataSourceConfigChangeEvent,
  goog.events.Event);

/**
 * JSON object containing new configuration
 * @type {!Object}
 */
gadgetide.ui.DataSourceConfigChangeEvent.prototype.config;


