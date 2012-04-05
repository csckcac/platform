/**
 * @fileoverview this is the concrete implementation of DataSourceContainer.
 * but only extend this if extending ConfigEditor is not enough.
 *
 * @author rcmperea@gmail.com (Manuranga)
 */

goog.provide('gadgetide.ui.ConfigEditorContainer');

goog.require('gadgetide.ui.ConfigEditor');
goog.require('gadgetide.ui.DataSourceContainer');
goog.require('gadgetide.ui.TerminalView');
goog.require('goog.ui.INLINE_BLOCK_CLASSNAME');
goog.require('goog.style');

/**
 * @constructor
 * @param {WireIt.Layer} layer Layer to which the Container will get rendered.
 * @param {gadgetide.ui.ConfigEditor=} opt_configEditor ConfigEditor to be
 *   show in the body of the Container.
 * @extends {gadgetide.ui.DataSourceContainer}
 */

gadgetide.ui.ConfigEditorContainer = function(layer, opt_configEditor) {

  this.configEditor_ = opt_configEditor || null;
  this.outEl_ = null;
  this.inEl_ = null;

  /* since WireIt will call render method inside the constructor we have to put
   * 'super' call at the bottom of the constructor instead at the top.
   */
  gadgetide.ui.DataSourceContainer.call(this, layer);
};
goog.inherits(gadgetide.ui.ConfigEditorContainer,
  gadgetide.ui.DataSourceContainer);


/**
 * DIV for rending output terminals
 * @type {?Element}
 */
gadgetide.ui.ConfigEditorContainer.prototype.outEl_;

/**
 * DIV for rending input terminals
 * @type {?Element}
 */
gadgetide.ui.ConfigEditorContainer.prototype.inEl_;

/**
 * ConfigEditor that will get rendered in this Container.
 * @type {?gadgetide.ui.ConfigEditor}
 */
gadgetide.ui.ConfigEditorContainer.prototype.configEditor_;

/**
 * @type {string}
 */
gadgetide.ui.ConfigEditorContainer.CSS_CLASS = 'gide-configcontainer';

/**
 * @inheritDoc
 */
gadgetide.ui.ConfigEditorContainer.prototype.render = function() {
  gadgetide.ui.DataSourceContainer.prototype.render.call(this);
  var dom = goog.dom.getDomHelper(this.bodyEl);
  var editorDiv = dom.createDom('div');

  dom.appendChild(this.bodyEl, editorDiv);
  if (this.configEditor_) {
    this.configEditor_.render(editorDiv);

    this.getHandler().listen(this.configEditor_,
      gadgetide.ui.ConfigEditor.EventType.CONFIG_CHANGE,
      this.fireConfigChange
    );
  }

  var leftCss = goog.getCssName(
    gadgetide.ui.ConfigEditorContainer.CSS_CLASS, 'terminalin');
  var rightCss = goog.getCssName(
    gadgetide.ui.ConfigEditorContainer.CSS_CLASS, 'terminalout');
  var endCss = goog.getCssName(
    gadgetide.ui.ConfigEditorContainer.CSS_CLASS, 'terminalend');


  dom.appendChild(this.bodyEl, dom.createDom('div', undefined,
    dom.createDom('div', leftCss,
      this.inEl_ = dom.createDom('div')),
    dom.createDom('div', rightCss,
      this.outEl_ = dom.createDom('div')),
    dom.createDom('div',endCss)
  ));


};

/**
 * @inheritDoc
 */
gadgetide.ui.ConfigEditorContainer.prototype.updateInputSchema =
  function(dataFieldTree) {
    if (dataFieldTree) {
      goog.dom.setTextContent(this.inEl_, '');
      goog.dom.classes.remove(this.inEl_, goog.getCssName(
        gadgetide.ui.ConfigEditorContainer.CSS_CLASS, 'empty'));
      if (this.inputView_) {
        this.inputView_.dispose();
      }
      var inputView = new gadgetide.ui.TerminalView(dataFieldTree,
        true /* is Input */, this);
      inputView.render(this.inEl_);
      this.correctTerminalBoxHeight_();

      //we can't use setParent because 'this' is not a component.
      //so as a work-around we set the ParentEventTarget to internal eventTarget.
      this.applyEventTarget(inputView.setParentEventTarget, inputView)();
      this.inputView_ = inputView;
    } else {
      //this container has no inputs

      goog.dom.setTextContent(this.inEl_, 'no input');
      goog.dom.classes.add(this.inEl_, goog.getCssName(
        gadgetide.ui.ConfigEditorContainer.CSS_CLASS, 'empty'));
    }
  };

gadgetide.ui.ConfigEditorContainer.prototype.correctTerminalBoxHeight_ =
  function(){
//  console.log(goog.style.getSize(this.outEl_));
};

/**
 * @inheritDoc
 */
gadgetide.ui.ConfigEditorContainer.prototype.updateOutputSchema =
  function(dataFieldTree) {
    if (dataFieldTree) {
      goog.dom.classes.remove(this.outEl_, goog.getCssName(
        gadgetide.ui.ConfigEditorContainer.CSS_CLASS, 'empty'));
      goog.dom.setTextContent(this.outEl_, '');
      if (this.outputView_) {
        this.outputView_.dispose();
      }
      var outputView = new gadgetide.ui.TerminalView(dataFieldTree,
        false /* is Input */, this);
      outputView.render(this.outEl_);
      this.correctTerminalBoxHeight_();

      // see the comment in 'updateInputSchema'
      this.applyEventTarget(outputView.setParentEventTarget, outputView)();
      this.outputView_ = outputView;
    } else {
      //this container has no output

      goog.dom.setTextContent(this.outEl_, 'no output');
      goog.dom.classes.add(this.outEl_, goog.getCssName(
        gadgetide.ui.ConfigEditorContainer.CSS_CLASS, 'empty'));
    }
  };

/**
 * @inheritDoc
 */
gadgetide.ui.ConfigEditorContainer.prototype.initConfig = function() {
  if (this.configEditor_) {
    this.configEditor_.initConfig();
  } else {
    this.fireConfigChange();
  }
};

/**
 * re-wrap the event data in DataSourceConfigChangeEvent and re-throw.
 * @param {gadgetide.ui.ConfigEditorChangeEvent=} e Event with config.
 */
gadgetide.ui.ConfigEditorContainer.prototype.fireConfigChange = function(e) {
  this.eventTarget_.dispatchEvent(
    new gadgetide.ui.DataSourceConfigChangeEvent(this, e ? e.config : {}));
};

