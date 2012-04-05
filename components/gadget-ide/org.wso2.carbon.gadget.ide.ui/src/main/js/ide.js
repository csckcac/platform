goog.require('goog.dom');
goog.require('gadgetide.pluginlist');
goog.require('goog.debug.Console');
goog.require('gadgetide.client.Admin');
goog.require('gadgetide.ui.Editor');

/** @define {boolean} IDE. */
gadgetide.IDE = true;

if (goog.DEBUG) {
  var c = new goog.debug.Console;
  c.setCapturing(true);
  goog.debug.LogManager.getRoot().setLevel(goog.debug.Logger.Level.ALL);
}

var editor = new gadgetide.ui.Editor();
var list = gadgetide.pluginlist.load();

for (var i = 0; i < list.length; i++) {
  editor.addPluginDef(list[i]);
}

editor.init(
  goog.dom.getElement('uiEditDiv'),
  goog.dom.getElement('layerDiv'),
  goog.dom.getElement('designEditDiv'),
  goog.dom.getElement('specEditDiv'),
  goog.dom.getElement('leftTabBarDiv'),
  goog.dom.getElement('itemsDiv'),
  goog.dom.getElement('settingsDiv'),
  goog.dom.getElement('rightTabBarDiv'),
  goog.dom.getElement('savePanelDiv'),
  goog.dom.getElement('publishHeader'),
  goog.dom.getElement('publishContentDiv'),
  goog.dom.getElement('publishNameText'),
  goog.dom.getElement('publishButton')
);
