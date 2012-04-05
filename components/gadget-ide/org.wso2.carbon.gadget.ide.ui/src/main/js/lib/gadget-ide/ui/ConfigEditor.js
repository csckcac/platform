/**
 * @fileoverview This is Component defines the UI that appears inside
 * ConfigEditorContainer. when a new data flow element has to be introduced,
 * this class should be extended, overriding 'createDom' method.
 *
 * @author rcmperea@gmail.com (Manuranga)
 */

goog.provide('gadgetide.ui.ConfigEditor');
goog.provide('gadgetide.ui.ConfigEditorChangeEvent');

goog.require('goog.events.EventType');
goog.require('goog.ui.Component');

/**
 * @constructor
 * @param {goog.dom.DomHelper=} opt_domHelper Optional DOM helper.
 * @extends {goog.ui.Component}
 */

gadgetide.ui.ConfigEditor = function(opt_domHelper) {
  goog.ui.Component.call(this, opt_domHelper);
};
goog.inherits(gadgetide.ui.ConfigEditor, goog.ui.Component);

/**
 * this css class can be used to give common elements (eg:- text boxes) same
 * L&F in all ConfigEditors.
 * @type {string}
 * @const
 */
gadgetide.ui.ConfigEditor.COMMON_CSS_CLASS = 'gide-config-editor';

/**
 * @inheritDoc
 */
gadgetide.ui.ConfigEditor.prototype.canDecorate = function(element) {
  /* since it's ConfigEditors are always created programmatically
   * , don't need to decorate
   */
  return false;
};

/**
 * @inheritDoc
 */
gadgetide.ui.ConfigEditor.prototype.createDom = function() {
  goog.ui.Component.prototype.createDom.call(this);
  goog.dom.classes.add(this.getElement(),
    gadgetide.ui.ConfigEditor.COMMON_CSS_CLASS);
};

/**
 * get called after ConfigEditor got rendered.
 * @see gadgetide.ui.DataSourceContainer.prototype.initConfig
 */
gadgetide.ui.ConfigEditor.prototype.initConfig = function() {
  //default implementation is a no-op
};

/**
 * dispatches ConfigEditorChangeEvent with the provided config object.
 *
 * @param {!Object} config JSON object containing all the config information.
 * @return {void} Nothing.
 */
gadgetide.ui.ConfigEditor.prototype.fireConfigChange = function(config) {
  this.dispatchEvent(new gadgetide.ui.ConfigEditorChangeEvent(this, config));
};


/** @enum {string} */
gadgetide.ui.ConfigEditor.EventType = {
  CONFIG_CHANGE: goog.events.getUniqueId('config_changed')
};


/**
 * Object representing a config change event. occurs when a user confirms
 * a change done to a configuration of a a config editor or in the initConfig.
 *
 * @param {gadgetide.ui.ConfigEditor} target ConfigEditor initiating event.
 * @param {!Object} config JSON object containing configuration.
 * @extends {goog.events.Event}
 * @constructor
 */
gadgetide.ui.ConfigEditorChangeEvent = function(target, config) {
  goog.events.Event.call(this,
    gadgetide.ui.ConfigEditor.EventType.CONFIG_CHANGE, target);

  this.config = config;
};
goog.inherits(gadgetide.ui.ConfigEditorChangeEvent, goog.events.Event);

/**
 * JSON object containing new configuration
 * @type {!Object}
 */
gadgetide.ui.ConfigEditorChangeEvent.prototype.config;


