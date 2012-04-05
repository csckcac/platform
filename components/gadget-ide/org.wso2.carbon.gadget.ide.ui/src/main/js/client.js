goog.require('goog.dom');
goog.require('goog.debug.Console');
goog.require('gadgetide.pluginlist');
goog.require('gadgetide.datasource.Graph');
goog.require('gadgetide.datasource.Executor');


/** @define {boolean} IDE. */
gadgetide.IDE = false;

var unitDefs = gadgetide.pluginlist.load();


/**
 * @constructor
 */
var Unit = function(datasource, ui) {
  this.datasource_ = datasource;
  this.ui_ = ui;
};
Unit.prototype.render = function(div) {
  if (this.ui_) {
    this.ui_.render(div);
  }
};
Unit.prototype.execute = function(input, callback) {
  if (callback) {
    this.datasource_.execute(callback, input, this.ui_ && this.ui_.getContext());
  }
  if (this.ui_) {
    this.ui_.updateUiData(input);
  }
};

var unitDefMap = {};
for (var i = 0; i < unitDefs.length; i++) {
  var unitDef = unitDefs[i];
  unitDefMap[unitDef.path] = unitDef;
}
var graph = new gadgetide.datasource.Graph();
var exec = new gadgetide.datasource.Executor(graph);
var constructUnit = function(type, config) {

  var unitDef = unitDefMap[type];
  var ui = null;
  if (unitDef.ui) {
    if (goog.isFunction(unitDef.ui.getInstance)) {
      ui = new gadgetide.uielement.UIElement('', unitDef.ui.getInstance());
    } else {
      ui = new unitDef.ui();
    }
  }
  var datasource = new unitDef.datasource(config);
  var unit = new Unit(datasource, ui);
  datasource.loadInputFormat(
    function(){
      exec.setPolicy(unit, unitDef.timing);
    }
  );
  return unit;
};

var addDependency = function(from, fromField, to, toField) {
  var arrToField = function(arr) {
    var path = [];
    for (var i = 0; i < arr.length; i = i + 2) {
      path.push({
        namespace:arr[i],localpart:arr[i + 1]
      });
    }
    var field = {
      name:arr[arr.length - 2],
      xmlns:arr[arr.length - 1],
      path:path
    };
    return field;
  };
  graph.add(from,
    arrToField(fromField),
    to,
    arrToField(toField));
};


goog.exportProperty(Unit.prototype, 'render', Unit.prototype.render);
goog.exportSymbol('addDependency', addDependency);
goog.exportSymbol('new_unitByType', constructUnit);