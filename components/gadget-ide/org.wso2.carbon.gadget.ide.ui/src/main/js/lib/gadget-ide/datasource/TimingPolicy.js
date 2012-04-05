goog.provide('gadgetide.datasource.TimingPolicy');

goog.require('gadgetide.datasource.Tree');

/**
 * @enum {!function(
 *     gadgetide.datasource.Executor,
 *     gadgetide.datasource.Graph,
 *     gadgetide.datasource.Delegator,
 *     gadgetide.datasource.Timer)}
 */
gadgetide.datasource.TimingPolicy = {
  NONE: function() {},
  GLOBAL_TICK: function(exec,graph,deleg,global) {
    /** @type {gadgetide.datasource.Executor} */
    var executor = exec;
    executor.addEventGenerator(global, deleg);
  }
};
