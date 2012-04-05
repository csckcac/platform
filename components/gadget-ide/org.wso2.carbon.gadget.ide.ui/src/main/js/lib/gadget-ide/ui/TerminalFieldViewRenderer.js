goog.provide('gadgetide.ui.TerminalFieldViewRenderer');

goog.require('gadgetide.ui.DataFieldViewRenderer');
goog.require('goog.ui.ControlRenderer');
goog.require('goog.ui.INLINE_BLOCK_CLASSNAME');


/**
 * @constructor
 * @extends {gadgetide.ui.DataFieldViewRenderer}
 */
gadgetide.ui.TerminalFieldViewRenderer = function() {
  gadgetide.ui.DataFieldViewRenderer.call(this);
};
goog.inherits(gadgetide.ui.TerminalFieldViewRenderer,
  gadgetide.ui.DataFieldViewRenderer);
goog.addSingletonGetter(gadgetide.ui.TerminalFieldViewRenderer);


/** @type {string} */
gadgetide.ui.TerminalFieldViewRenderer.CSS_CLASS = 'gide-tfview';


/** @inheritDoc */
gadgetide.ui.TerminalFieldViewRenderer.prototype.getCssClass = function() {
  return gadgetide.ui.TerminalFieldViewRenderer.CSS_CLASS;
};


/**
 * @override
 * @param {gadgetide.ui.DataFieldView} dataFieldView TerminalFieldView to be
 *     rendered.
 * @return {Element} Root element for the TerminalFieldViewRenderer.
 */
gadgetide.ui.TerminalFieldViewRenderer.prototype.createDom =
  function(dataFieldView) {
    var terminalFieldView = /** @type {gadgetide.ui.TerminalFieldView}*/
      dataFieldView;
    var el = gadgetide.ui.DataFieldViewRenderer.prototype.createDom.call(this,
      terminalFieldView);
    var dom = terminalFieldView.getDomHelper();

    //violates the visibility,but needed for getIconElement to work
    terminalFieldView.setElementInternal(el);

    var iconEl = terminalFieldView.getIconElement();
    var terminalEl = dom.createDom('div', goog.getCssName(this.getCssClass(),
      'terminal'));
    iconEl.appendChild(terminalEl);


    var config = {
      'direction' : terminalFieldView.isIconRight() ? [1, 0] : [-1, 0],
      'wireConfig' : {
        // unfortunately this can't set this using css since it's hard-coded
        // into WireIt lib.
        'color': '#CCCCCC',
        'bordercolor': '#999999'
      },
      'ddConfig' : {
        'type': terminalFieldView.isInput() ? 'input' : 'output' ,
        'allowedTypes': terminalFieldView.isInput() ? [] : ['input']
      }
    };

    var container = dataFieldView.getParent().getContainer();
    var terminal;
    if (container) {
      terminal = container.addTerminalTo(terminalEl, config);
    }else {
      terminal = new WireIt.Terminal(terminalEl, config, null);
    }

    terminal['eventAddWire']['subscribe'](
      goog.partial(this.handleAddWire_, terminal, terminalFieldView));

    terminal['eventRemoveWire']['subscribe'](
      goog.partial(this.handleRemoveWire_, terminal, terminalFieldView));

    return el;
  };

gadgetide.ui.TerminalFieldViewRenderer.prototype.handleRemoveWire_ =
  function(terminal,tfView ,e,params) {
    var wire = params[0];

    //ignore the dummy event.
    if (wire.terminal2.el) {
      tfView.fireRemoveWire(wire.terminal2 == terminal);
    }
  };

gadgetide.ui.TerminalFieldViewRenderer.prototype.handleAddWire_ =
 function(terminal,tfView ,e,params) {
  var wire = params[0];

  //ignore the dummy event.
  if (wire.terminal2.el) {
    tfView.fireAddWire(wire.terminal2 == terminal);
  }
};
