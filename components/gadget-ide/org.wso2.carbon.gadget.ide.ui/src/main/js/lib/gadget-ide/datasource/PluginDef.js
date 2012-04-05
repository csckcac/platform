goog.provide('gadgetide.datasource.PluginDef');


/**
 * defines a plugin for the editor.
 * @constructor
 */
gadgetide.datasource.PluginDef = function() {
};

/**
 * this should be unique string among all the plugins.
 *
 * @type {string}
 */
gadgetide.datasource.PluginDef.prototype.path;

/**
 * should be one of following:
 *
 * 1) constructor of the gadgetide.datasource.DataSource of this plugin.
 *
 * 2) null
 *
 * @type {Function}
 */
gadgetide.datasource.PluginDef.prototype.datasource;

/**
 * should be one of following:
 *
 * 1) constructor of a the gadgetide.ui.DataSourceContainer : if so will be
 *    instantiated with WireIt.Layer as it's first argument.
 *
 * 2) constructor of a the gadgetide.ui.ConfigEditor : if so will be
 *    instantiated and passed to a gadgetide.ui.ConfigEditorContainer to be
 *    displayed in.
 *
 * 3) null : default Container will be used.
 * @type {?Function}
 */
gadgetide.datasource.PluginDef.prototype.container;


/**
 * should be one of following:
 *
 * 1) constructor of a gadgetide.uielement.UIElement : constructed with no
 *    argument constructor.
 *
 * 2) constructor of a gadgetide.uielement.UIElementRenderer which has the
 *    static singleton method 'getInstance' : a UIElement will be constructed
 *    with '' content and 'ui.getInstance()' as the renderer.
 *
 * 3) null : no UI will be created.
 * @type {?Function}
 */
gadgetide.datasource.PluginDef.prototype.ui;

/**
 * @type {gadgetide.datasource.TimingPolicy}
 */
gadgetide.datasource.PluginDef.prototype.timing;
