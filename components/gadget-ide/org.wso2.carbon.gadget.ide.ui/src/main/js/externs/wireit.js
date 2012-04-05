/**
 * @fileoverview Externs for WireIt 0.5
 *
 *
 * @see http://neyric.github.com/wireit/
 * @externs
 */


/** namespace for YUI related libs*/
var YAHOO = {};


/** namespace for WireIt */
var WireIt = {};

/** namespace for WireIt utils */
WireIt.util = {};


/**
 * WireIt.util.DD is a wrapper class for YAHOO.util.DD, to redraw the wires associated with the given terminals while drag-dropping
 * (HACK: this should extend YAHOO.util.DD, but it's not shown here for simplicity)
 *
 * @see http://neyric.github.com/wireit/api/WireIt.util.DD.html
 * @constructor
 * @param {Array} terminals List of WireIt.Terminal objects associated within the DragDrop element
 * @param {String} id Parameter of YAHOO.util.DD
 * @param {String} sGroup Parameter of YAHOO.util.DD
 * @param {Object} config Parameter of YAHOO.util.DD
 */
WireIt.util.DD = function(terminals, id, sGroup, config) {
};


/**
 * HACK: this is actually defined in the super class, not here
 * @see http://developer.yahoo.com/yui/docs/YAHOO.util.DragDrop.html#method_setXConstraint
 * @param {number=} iLeft the number of pixels the element can move to the left
 * @param {number=} iRight the number of pixels the element can move to the
 * right
 * @param {number=} iTickSize optional parameter for specifying that the
 * element should move iTickSize pixels at a time.
 */
WireIt.util.DD.prototype.setXConstraint = function(iLeft, iRight, iTickSize) {
};


/**
 * HACK: this is actually defined in the super class, not here
 * @see http://developer.yahoo.com/yui/docs/YAHOO.util.DragDrop.html#method_setYConstraint
 * @param {number=} iUp the number of pixels the element can move up
 * @param {number=} iDown the number of pixels the element can move down
 * @param {number=} iTickSize optional parameter for specifying that the
 * element should move iTickSize pixels at a time.
 */
WireIt.util.DD.prototype.setYConstraint = function(iUp, iDown, iTickSize) {
};


/**
 * @see http://neyric.github.com/wireit/api/WireIt.Container.html
 * @constructor
 * @param {Object} options is the Configuration object
 * @param {WireIt.Layer=} layer is the The WireIt.Layer (or subclass) instance
 *    that contains this container
 */
WireIt.Container = function(options, layer) {
};

/**
 * associated drag'n drop utility to make the container draggable.
 * @protected
 * @type {?WireIt.util.DD}
 */
WireIt.Container.prototype.dd;

/**
 * Container DOM element
 * @see http://neyric.github.com/wireit/api/WireIt.Container.html#property_el
 * @protected
 * @type {Element}
 */
WireIt.Container.prototype.el;

/**
 * Body element
 * @see  http://neyric.github.com/wireit/api/WireIt.Container.html#property_bodyEl
 * @protected
 * @type {Element}
 */
WireIt.Container.prototype.bodyEl;

/**
 * the WireIt.Layer object that should contain this container
 * @see http://neyric.github.com/wireit/api/WireIt.Container.html#property_layer
 * @protected
 * @type {WireIt.Layer}
 */
WireIt.Container.prototype.layer;

/**
 * Render the dom of the container
 * @see http://neyric.github.com/wireit/api/WireIt.Container.html#method_render
 */
WireIt.Container.prototype.render = function() {
};

/**
 * Called when the user clicked on the close button
 * @see http://neyric.github.com/wireit/api/WireIt.Container.html#method_onCloseButton
 * @param {goog.events.Event} e
 * @param {Object} args
 */
WireIt.Container.prototype.onCloseButton = function(e, args) {
};

/**
 * Instanciate the terminal from the class pointer "xtype" (default WireIt.Terminal).
 * @see http://neyric.github.com/wireit/api/WireIt.Container.html#method_addTerminal
 * @param {Object=} terminalConfig Configuration object for Terminal.
 * @return {WireIt.Terminal} Created terminal
 */
WireIt.Container.prototype.addTerminal = function(terminalConfig) {
};


/**
 * @see http://neyric.github.com/wireit/api/WireIt.Terminal.html
 * @constructor
 * @param {Element} parentEl is the Element that will contain the terminal
 * @param {Object} options is the Configuration object
 * @param {WireIt.Container=} container is the Container containing this terminal
 */
WireIt.Terminal = function(parentEl, options, container) {
};

/**
 * Render the DOM of the terminal
 * @see http://neyric.github.com/wireit/api/WireIt.Terminal.html#method_render
 * @return {void} Nothing.
 */
WireIt.Terminal.prototype.render = function() {
};

/**
 * @see http://neyric.github.com/wireit/api/WireIt.Terminal.html#method_setPosition
 * @param {Array.<number>|{top: number, left: number}|{right: number, top: number}} pos Position
 * @return {void} Nothing.
 */
WireIt.Terminal.prototype.setPosition = function(pos) {
};

/**
 * DIV dom element that will display the Terminal
 * @protected
 * @type {Element}
 */
WireIt.Terminal.prototype.el;

/**
 * DOM parent element
 * @protected
 * @type {Element}
 */
WireIt.Terminal.prototype.parentEl;

/**
 * offset position from the parentEl position. Can be an array [top,left] or an
 * object {left: 100, bottom: 20} or {right: 10, top: 5}
 * @protected
 * @type {Array.<number>|{top: number, left: number}|{right: number, top: number}}
 */
WireIt.Terminal.prototype.offsetPosition;

/**
 * CSS class name for the terminal element
 * @protected
 * @type {string}
 */
WireIt.Terminal.prototype.className;

/**
 * @see http://neyric.github.com/wireit/api/WireIt.TerminalProxy.html
 * @param {WireIt.Terminal} terminal
 * @param {Object=} options
 * @constructor
 * @extends {WireIt.Terminal}
 */
WireIt.TerminalProxy = function(terminal, options) {
};


/**
 * @see http://neyric.github.com/wireit/api/WireIt.Layer.html
 * @constructor
 * @param {Object} options is the Configuration object
 */
WireIt.Layer = function(options) {
};

/**
 * Layer DOM element
 * @see http://neyric.github.com/wireit/api/WireIt.Layer.html#property_el
 * @protected
 * @type {Element}
 */
WireIt.Layer.prototype.el;

/**
 * HACK: this is not actually a public method, but we need it for adding
 * containers after creating them somewhere else.
 * @param {WireIt.Container} container is the Configuration object
 */
WireIt.layer.addContainerDirect = function(container) {
};


/**
 * Remove a container
 * @param {WireIt.Container} container Container instance to remove
 */
WireIt.layer.removeContainer = function(container) {
};


/**
 * @see http://neyric.github.com/wireit/api/WireIt.Terminal.html
 * @constructor
 * @param {WireIt.Terminal} terminal1
 * @param {WireIt.Terminal} terminal2
 * @param {Element} parentEl
 * @param {Object=} options
 */
WireIt.Wire = function(terminal1, terminal2, parentEl, options) {
};

/**
 * Source terminal
 * @see http://neyric.github.com/wireit/api/WireIt.Wire.html#property_terminal1
 * @type {WireIt.Terminal}
 */
WireIt.Wire.prototype.terminal1;

/**
 * Target terminal
 * @see http://neyric.github.com/wireit/api/WireIt.Wire.html#property_terminal2
 * @type {WireIt.Terminal || WireIt.TerminalProxy}
 */
WireIt.Wire.prototype.terminal2;

//TODO: ArrowWire
//TODO: BezierArrowWire
//TODO: BezierWire
//TODO: CanvasElement	
//TODO: ImageContainer	
//TODO: InOutContainer
//TODO: LayerMap
//TODO: Scissors
//TODO: StepWire
//TODO: TerminalProxy
