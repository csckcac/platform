goog.provide('gadgetide.spec');

goog.require('gadgetide.spec.StringCellRenderer');


/**
 *
 * @typedef {{
 *   name:string,
 *   type:gadgetide.spec.Types,
 *   defaultVal:*,
 *   callback:?Function
 * }}
 */
gadgetide.spec.Spec;

/**
 * @enum {(!gadgetide.spec.TypeCell|!gadgetide.spec.TypeCellRenderer)}
 */
gadgetide.spec.Types = {
  STRING:new gadgetide.spec.StringCellRenderer(),
  BOOLEAN:new gadgetide.spec.TypeCellRenderer()
};

/**
 * @param {Array.<gadgetide.spec.Spec>} specs
 */
gadgetide.spec.extractDefaultsMap = function(specs) {
  var map = {};
  for (var i = 0; i < specs.length; i++) {
    var spec = specs[i];
    map[spec.name] = spec.defaultVal;
  }
  return map;
};
