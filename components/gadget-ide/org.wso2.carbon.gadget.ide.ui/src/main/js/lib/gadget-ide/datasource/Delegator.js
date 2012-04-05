goog.provide('gadgetide.datasource.Delegator');

goog.require('gadgetide.datasource.DataSource');
goog.require('gadgetide.spec');
goog.require('gadgetide.datasource.PluginDef');
goog.require('gadgetide.ui.DataSourceContainer');
goog.require('gadgetide.ui.TerminalFieldView');
goog.require('gadgetide.uielement.UIElement');
/**
 * @param {gadgetide.datasource.PluginDef} pluginDef Definitions for this.
 * @param {string} name Unique name of this delegator.
 * @constructor
 * @extends {goog.events.EventTarget}
 */
gadgetide.datasource.Delegator =
  function(pluginDef, name) {
    goog.events.EventTarget.call(this);

    this.pluginDef_ = pluginDef;
    this.name_ = name;
    this.handler = new goog.events.EventHandler(this);
  };
goog.inherits(
  gadgetide.datasource.Delegator, goog.events.EventTarget);

/**
 * @type {gadgetide.datasource.PluginDef}
 */
gadgetide.datasource.Delegator.prototype.pluginDef_;

/**
 * @type {boolean}
 * @private
 */
gadgetide.datasource.Delegator.prototype.ready_;

/**
 * @type {gadgetide.ui.DataSourceContainer}
 * @private
 */
gadgetide.datasource.Delegator.prototype.container_;

/**
 * @type {gadgetide.datasource.DataSource}
 * @private
 */
gadgetide.datasource.Delegator.prototype.dataSource_;

/**
 * @type {gadgetide.uielement.UIElement}
 * @private
 */
gadgetide.datasource.Delegator.prototype.ui_;

/**
 * @type {!goog.events.EventHandler}
 * @protected
 */
gadgetide.datasource.Delegator.prototype.handler;

/**
 * @param {goog.ui.Component} uiEditor
 */
gadgetide.datasource.Delegator.prototype.display = function(uiEditor, layer) {
  if (this.pluginDef_.datasource) {
    // ___creating the container__.
    // if a specific container/config editor is present in plugin-def use that, or else
    // use the ConfigEditorContainer.
    if (this.pluginDef_.container) {
      if (this.pluginDef_.container instanceof gadgetide.ui.DataSourceContainer) {
        this.container_ = new this.pluginDef_.container(layer);
      } else {
        this.container_ = new gadgetide.ui.ConfigEditorContainer(layer, new this.pluginDef_.container());
      }
    } else {
      this.container_ = new gadgetide.ui.ConfigEditorContainer(layer);
    }
    this.container_.applyEventTarget(this.handler.listen, this.handler)(
      gadgetide.ui.DataSourceContainer.EventType.CONFIG_CHANGE,
      this.handleNewConfig_
    );
    this.container_.applyEventTarget(this.handler.listen, this.handler)(
      gadgetide.ui.DataSourceContainer.EventType.CLICK_CLOSE,
      this.handleCloseReq_
    );
    this.container_.setParentEventTarget(this);
    this.container_.initConfig();
  }


  // ___creating the ui__.
  if (this.pluginDef_.ui) {
    if (goog.isFunction(this.pluginDef_.ui.getInstance)) {
      this.ui_ = new gadgetide.uielement.UIElement('',
        this.pluginDef_.ui.getInstance());
    } else {
      this.ui_ = new this.pluginDef_.ui();
    }

    this.resizer_ = new gadgetide.ui.Resizer();

    this.handler.listen(this.resizer_,
      gadgetide.ui.Resizer.EventType.SELECT_REQ, this.handleSelectReq_);

    this.handler.listen(this.resizer_, gadgetide.ui.Resizer.EventType.RESIZED,
      this.handleResize_);


    uiEditor.addChild(this.resizer_, true /*render*/);
    this.resizer_.addChild(this.ui_, true /*render*/);
    this.resizer_.setBounds(new goog.math.Rect(50, 50, 100, 20));

  }
};

/**
 * @return {!Object}
 */
gadgetide.datasource.Delegator.prototype.getSpecMap = function() {
  return this.ui_.getSpecMap();
};

/**
 * @param {goog.events.Event} e
 */
gadgetide.datasource.Delegator.prototype.handleCloseReq_ = function(e) {
  // we will just wrap the event and rethrow it.
  this.dispatchEvent(gadgetide.datasource.Delegator.EventType.REMOVE_REQ);
};

/**
 * @param {goog.events.Event} e
 */
gadgetide.datasource.Delegator.prototype.handleSelectReq_ = function(e) {
  // we will just wrap the event and rethrow it.
  // we have to do this manually because Delegator is not parent of the Resizes.
  this.dispatchEvent(gadgetide.datasource.Delegator.EventType.SELECT_REQ);
};

/**
 * @param {goog.events.Event} e
 */
gadgetide.datasource.Delegator.prototype.handleResize_ = function(e) {
  this.ui_.setSize(this.resizer_.getSize());
};

/**
 * @param {boolean} select Whether to select or unselect the Delegator.
 */
gadgetide.datasource.Delegator.prototype.setSelected = function(select) {
  this.resizer_.setSelected(select)
};

/**
 * delegated to the ui.
 * @return {Array.<gadgetide.spec.Spec>}
 */
gadgetide.datasource.Delegator.prototype.getSpec = function() {
  return this.ui_.getSpec();
};

/**
 * @param {gadgetide.ui.DataSourceConfigChangeEvent} configEvent
 */
gadgetide.datasource.Delegator.prototype.handleNewConfig_ =
  function(configEvent) {

    // configEvent.config  can be 'undefined' if it's created using
    // string version of dispatchEvent
    this.config_ = configEvent.config || {};
    this.dataSource_ = new this.pluginDef_.datasource(this.config_);
    this.dataSource_.loadInputFormat(
      goog.bind(this.handleInputFormat_, this, this.dataSource_));
    configEvent.stopPropagation();

    this.ready = false;
    this.dispatchEvent(gadgetide.datasource.Delegator.EventType.READY);
  };

gadgetide.datasource.Delegator.prototype.getDataSource =
  function() {
    return this.dataSource_;

  };

/**
 * @return {!Object} json object containing the state.
 */
gadgetide.datasource.Delegator.prototype.toMemento = function() {
  var mem = {};
  mem['config'] = this.config_;
  if (this.ui_) {
    var bounds = this.resizer_.getBounds();
    mem['ui'] = {
      'width':bounds.width,
      'height':bounds.height,
      'left':bounds.left,
      'top':bounds.top
    }
    mem['spec'] = this.ui_.getSpecMap();
  }
  return mem;
};

/**
 * @param {gadgetide.datasource.DataSource} source
 * @param {gadgetide.schema.DSSchema} dsSchema
 */
gadgetide.datasource.Delegator.prototype.handleInputFormat_ =
  function(source, dsSchema) {
    // if config changed while dataSource is still loading, this event should be
    // ignored.
    if (this.dataSource_ === source) {

      //if dsSchema is null ,inFieldTree_ should be null else parse it.
      this.inFieldTree_ = dsSchema && gadgetide.schema.parseDataFieldTree(dsSchema);

      this.container_.updateInputSchema(this.inFieldTree_);
      this.dataSource_.loadOutputFormat(
        goog.bind(this.handleOutputFormat_, this, this.dataSource_),
        dsSchema
      );
    }
  };
/**
 * @param {Object} input this is json object specifying input values for the
 *     execute.
 * @param {(function(Object,string=): void)=} callback will be called when the
 *     execution is over.
 */
gadgetide.datasource.Delegator.prototype.execute = function(input, callback) {
  if (callback) {
    this.dataSource_.execute(callback, input, this.ui_ && this.ui_.getContext());
  }
  if (this.ui_) {
    this.ui_.updateUiData(input);
  }
};

/**
 * @return {gadgetide.datasource.PluginDef}
 */
gadgetide.datasource.Delegator.prototype.getPluginDef = function() {
  return this.pluginDef_;
};

/**
 * @return {string}
 */
gadgetide.datasource.Delegator.prototype.getName = function() {
  return this.name_;
};

/**
 * @return {string}
 */
gadgetide.datasource.Delegator.prototype.getNameAsVar = function() {
  var firstLetter = this.name_.charAt(0).toLowerCase();
  return firstLetter + this.name_.replace(/[ \)\(]/g, '').substring(1);
};

/**
 * @param {Object} specMap
 * @param {number} index Index of the last changed spec in the spec array.
 */
gadgetide.datasource.Delegator.prototype.updateSpecMap = function(specMap,index) {
  this.ui_.updateSpecMap(specMap,index);
};

/**
 * @param {gadgetide.datasource.DataSource} source
 * @param {gadgetide.schema.DSSchema} dsSchema
 */
gadgetide.datasource.Delegator.prototype.handleOutputFormat_ =
  function(source, dsSchema) {
    // just like in handleInputFormat_, if config changed while dataSource is
    // still loading, this event should be ignored.
    if (this.dataSource_ === source) {

      //if dsSchema is null ,outFieldTree_ should be null else parse it.
      this.outFieldTree_ = dsSchema && gadgetide.schema.parseDataFieldTree(dsSchema);

      this.container_.updateOutputSchema(this.outFieldTree_);
      this.dispatchEvent(
        gadgetide.datasource.Delegator.EventType.READY);
    }
  };

/** @enum {string} */
gadgetide.datasource.Delegator.EventType = {
  READY: goog.events.getUniqueId('ready'),
  UNREADY: goog.events.getUniqueId('unready'),
  SELECT_REQ : goog.events.getUniqueId('select_req'), /* not really selected yet,
   but requests for getting selected*/
  REMOVE_REQ : goog.events.getUniqueId('remove_req') /* not really removed yet,
   but requests for getting removed*/
};
