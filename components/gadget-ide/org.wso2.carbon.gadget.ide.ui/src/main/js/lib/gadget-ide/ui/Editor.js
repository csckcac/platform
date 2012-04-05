goog.provide('gadgetide.ui.Editor');

goog.require('gadgetide.datasource.Delegator');
goog.require('gadgetide.datasource.Executor');
goog.require('gadgetide.datasource.Graph');
goog.require('gadgetide.datasource.Graph');
goog.require('gadgetide.ui.ConfigEditorContainer');
goog.require('gadgetide.ui.FlowEditor');
goog.require('gadgetide.ui.SidePanel');
goog.require('gadgetide.ui.TerminalFieldView');
goog.require('gadgetide.ui.UIEditor');
goog.require('goog.events.EventHandler');
goog.require('goog.ui.Component');
goog.require('goog.ui.TabBar');
goog.require('gadgetide.spec.SpecTable');
goog.require('goog.structs.Map');
goog.require('goog.iter');
goog.require('gadgetide.client.util');
goog.require('gadgetide.client.Admin');
goog.require('goog.ui.AnimatedZippy');
goog.require('goog.ui.Css3ButtonRenderer');
goog.require('goog.ui.Button');
goog.require('goog.debug.Logger');
goog.require('gadgetide.spec.SpecChangeEvent');


/**
 * @constructor
 */
gadgetide.ui.Editor = function() {
  this.pluginDefs_ = {};
  this.handler_ = new goog.events.EventHandler(this);
  this.graph_ = new gadgetide.datasource.Graph();
  this.executor_ = new gadgetide.datasource.Executor(this.graph_);
  this.delegates_ = new goog.structs.Map();
};

/**
 * A reference to the Editor's logger
 * @type {goog.debug.Logger}
 * @private
 */
gadgetide.ui.Editor.prototype.logger_ =
  goog.debug.Logger.getLogger('gadgetide.ui.Editor');

/**
 * map contains gadgetide.datasource.Delegator values mapped by their names.
 * @type {goog.structs.Map}
 */
gadgetide.ui.Editor.prototype.delegates_;

/**
 * @type {gadgetide.ui.FlowEditor}
 */
gadgetide.ui.Editor.prototype.flowEditor_ = null;

/**
 * @type {gadgetide.ui.SidePanel}
 */
gadgetide.ui.Editor.prototype.sidePanel_ = null;

/**
 * @type {gadgetide.ui.UIEditor}
 */
gadgetide.ui.Editor.prototype.uiEditor_ = null;

/**
 * @type {goog.events.Event}
 */
gadgetide.ui.Editor.prototype.lastWireEvent_ = null;

/**
 * @type {Object.<gadgetide.datasource.PluginDef>}
 */
gadgetide.ui.Editor.prototype.pluginDefs_;

/**
 * @param {gadgetide.datasource.PluginDef} pluginDef
 */
gadgetide.ui.Editor.prototype.addPluginDef = function(pluginDef) {
  this.pluginDefs_[pluginDef.path] = pluginDef;
};


/**
 *
 * @param {Element} uiEditDiv
 * @param {Element} layerDiv
 * @param {Element} designEditDiv
 * @param {Element} specEditDiv
 * @param {Element} leftTabDiv
 * @param {Element} settingsDiv
 * @param {Element} itemsDiv
 * @param {Element} rightTabDiv
 */
gadgetide.ui.Editor.prototype.init =
  function(uiEditDiv, layerDiv, designEditDiv, specEditDiv, leftTabDiv, itemsDiv, settingsDiv, rightTabDiv, saveDiv, publishHeaderDiv, publishDiv, publishNameText, publishButtonDiv) {

    var rightTabBar = new goog.ui.TabBar();
    rightTabBar.decorate(rightTabDiv);
    this.handler_.listen(rightTabBar, goog.ui.Component.EventType.SELECT,
      function(e) {
        var tabSelected = e.target;
        var isFlowEditor = tabSelected === rightTabBar.getChildAt(0);
        goog.style.showElement(layerDiv, isFlowEditor);
        goog.style.showElement(uiEditDiv, !isFlowEditor);
      });
    goog.style.showElement(uiEditDiv, false);
    this.rightTabBar_ = rightTabBar;

    var leftTabBar = new goog.ui.TabBar();
    leftTabBar.decorate(leftTabDiv);
    this.handler_.listen(leftTabBar, goog.ui.Component.EventType.SELECT,
      function(e) {
        var tabSelected = e.target;
        var isSettings = tabSelected === leftTabBar.getChildAt(1);
        goog.style.showElement(itemsDiv, !isSettings);
        goog.style.showElement(settingsDiv, isSettings);
      });
    goog.style.showElement(settingsDiv, false);
    this.leftTabBar_ = leftTabBar;

    this.specTable_ = new gadgetide.spec.SpecTable();
    this.specTable_.render(specEditDiv);
    this.handler_.listen(this.specTable_,
      gadgetide.spec.SpecTable.EventType.CHANGE, this.handleSpecChange_);

    new goog.ui.AnimatedZippy(publishHeaderDiv, publishDiv, false);

    this.flowEditor_ = new gadgetide.ui.FlowEditor();
    this.flowEditor_.decorate(layerDiv);

    this.uiEditor_ = new gadgetide.ui.UIEditor();
    this.uiEditor_.render(designEditDiv);

    this.handler_.listen(this.uiEditor_, goog.events.EventType.CLICK,
      this.handleSelectReq_);

    this.sidePanel_ = new gadgetide.ui.SidePanel();
    this.sidePanel_.render(itemsDiv);

    for (var path in this.pluginDefs_) {
      var pluginDef = this.pluginDefs_[path];
      this.sidePanel_.addPath(path);
    }

    this.handler_.listen(
      this.sidePanel_, goog.ui.Component.EventType.ACTION, this.onSideItemAction_);


    this.publishNameEl_ = publishNameText;

    var publishButton = new goog.ui.Button('load',
      goog.ui.Css3ButtonRenderer.getInstance());
    publishButton.decorate(publishButtonDiv);
    this.handler_.listen(publishButton, goog.ui.Component.EventType.ACTION,
      this.handleSave_);

  };

/**
 *
 * @param {goog.events.Event} e The event.
 * @private
 */
gadgetide.ui.Editor.prototype.onSideItemAction_ = function(e) {
  var sideItem = /** @type {gadgetide.ui.SideItem} */ e.target;
  if (sideItem.getPath()) {
    var pluginDef = this.pluginDefs_[sideItem.getPath()];
    if (pluginDef) {
      var name = this.getUniqueName_(sideItem.getName());
      var delegator = new gadgetide.datasource.Delegator(pluginDef, name);
      this.delegates_.set(name, delegator);

      // if it has a UIElement we have to make sure UIEditor is visible.if not
      // the drag area will not get calculated correctly.
      this.rightTabBar_.setSelectedTabIndex(pluginDef.ui ? 1 : 0);

      this.handler_.listen(delegator,
        [
          gadgetide.ui.TerminalFieldView.EventType.WIRE_ADDED_IN,
          gadgetide.ui.TerminalFieldView.EventType.WIRE_ADDED_OUT
        ],
        this.handleWireAdd_
      );

      this.handler_.listen(delegator,
        [
          gadgetide.ui.TerminalFieldView.EventType.WIRE_REMOVE_IN,
          gadgetide.ui.TerminalFieldView.EventType.WIRE_REMOVE_OUT
        ],
        this.handleWireRemove_);
      this.handler_.listen(delegator,
        gadgetide.datasource.Delegator.EventType.SELECT_REQ,
        this.handleSelectReq_);
      this.handler_.listen(delegator,
        gadgetide.datasource.Delegator.EventType.REMOVE_REQ, function(e) {
//          console.log(e.target);
        });

      delegator.display(this.uiEditor_, this.flowEditor_.getLayer());
      this.executor_.setPolicy(delegator, pluginDef.timing);
    }
  }
};


/**
 *
 * @param {string} baseName
 * @param {number=} opt_startIndex
 */
gadgetide.ui.Editor.prototype.getUniqueName_ = function(baseName, opt_startIndex) {
  var startIndex = (opt_startIndex || 1);
  var proposedName = baseName + ' ' + startIndex;
  if (!this.delegates_.containsKey(proposedName)) {
    return proposedName;
  } else {
    return this.getUniqueName_(baseName, startIndex + 1);
  }
};

/**
 *
 * @param {goog.events.Event} e
 */
gadgetide.ui.Editor.prototype.handleSave_ = function(e) {
  var state = {};
  var units = [];
  state['units'] = {'unit':units};

    this.rightTabBar_.setSelectedTabIndex(1);

  goog.iter.forEach(this.delegates_.getKeyIterator(), function(key) {
    /** @type {gadgetide.datasource.Delegator} */
    var delegate = this.delegates_.get(key);
    var varName = delegate.getNameAsVar();
    var unit = {
      'name':varName,
      'type':delegate.getPluginDef().path,
      'state':delegate.toMemento()};
    units.push(unit);
  }, this);

  state['graph'] = this.graph_.toMemento();

  var stateXML = gadgetide.client.util.json2XML({'gadget-setting':state});
  gadgetide.client.Admin.getInstance().saveSettings(stateXML, this.publishNameEl_.value);
};

/**
 *
 * @param {gadgetide.spec.SpecChangeEvent} e
 */
gadgetide.ui.Editor.prototype.handleSpecChange_ = function(e) {
  /** @type {!gadgetide.datasource.Delegator} */
  var selected = this.selectedDelegator_;// not null.table visible => selected.
  selected.updateSpecMap(this.specTable_.getMap(),e.index);
};

/**
 *
 * @param {goog.events.Event} e
 */
gadgetide.ui.Editor.prototype.handleSelectReq_ = function(e) {
  /** @type {gadgetide.datasource.Delegator} */
  var selected = null;
  if (e.target instanceof gadgetide.datasource.Delegator) {
    selected = /** @type {gadgetide.datasource.Delegator} */ e.target;
  }
  if (this.selectedDelegator_ !== selected) {
    if (this.selectedDelegator_) {
      this.selectedDelegator_.setSelected(false);
    }
    if (selected) {
      selected.setSelected(true);
      this.specTable_.populate(selected.getSpec(), selected.getSpecMap());
    } else {
      this.specTable_.clear();
    }
    this.selectedDelegator_ = selected;
  }
};

gadgetide.ui.Editor.prototype.handleWireRemove_ = function(e) {
  if (this.lastWireEvent_) {
    // the starting end's event gets fired first.
    var to = /** @type {gadgetide.datasource.Delegator} */ e.currentTarget;
    var toField = /** @type {gadgetide.schema.DataField}*/
      e.target.getModel();

    var from = /** @type {gadgetide.datasource.Delegator} */
      this.lastWireEvent_.currentTarget;
    var fromField = /** @type {gadgetide.schema.DataField}*/
      this.lastWireEvent_.target.getModel();

    this.graph_.remove(from, fromField, to, toField);
    this.lastWireEvent_ = null;
  } else {
    this.lastWireEvent_ = e;
  }
  e.stopPropagation();
};

/**
 *
 * @param {goog.events.Event} e
 */
gadgetide.ui.Editor.prototype.handleWireAdd_ = function(e) {
  if (this.lastWireEvent_) {

    // the starting end's event gets fired first.
    var to = /** @type {gadgetide.datasource.Delegator} */ e.currentTarget;
    var toField = /** @type {gadgetide.schema.DataField}*/
      e.target.getModel();

    var from = /** @type {gadgetide.datasource.Delegator} */
      this.lastWireEvent_.currentTarget;
    var fromField = /** @type {gadgetide.schema.DataField}*/
      this.lastWireEvent_.target.getModel();

//    this.logger_.shout('wire added from ' + form + ' to ' + to);

    this.graph_.add(from, fromField, to, toField);
    this.lastWireEvent_ = null;
  } else {
    this.lastWireEvent_ = e;
  }
  e.stopPropagation();
};


