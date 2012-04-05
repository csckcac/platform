goog.provide('gadgetide.datasource.Executor');

goog.require('gadgetide.datasource.ExecutionContext');
goog.require('gadgetide.datasource.Timer');
goog.require('goog.events.EventHandler');

/**
 * @param {gadgetide.datasource.Graph} graph
 * @constructor
 */
gadgetide.datasource.Executor = function(graph) {
  this.graph_ = graph;
  this.globalTimer_ = new gadgetide.datasource.Timer(2000);
  this.handler_ = new goog.events.EventHandler(this);
  this.addEventGenerator(this.globalTimer_);
  this.dependents = {};
};

/**
 * @type {Object.<Array.<gadgetide.datasource.Delegator>>}
 */
gadgetide.datasource.Executor.prototype.dependents = null;

/**
 * @param {gadgetide.datasource.Delegator} delegator
 * @param {gadgetide.datasource.TimingPolicy} policy
 */
gadgetide.datasource.Executor.prototype.setPolicy =
  function(delegator, policy) {
    policy(this, this.graph_, delegator, this.globalTimer_);
  };

/**
 * @param {gadgetide.datasource.Delegator|
  *     gadgetide.datasource.Timer} source
 *
 * @param {gadgetide.datasource.Delegator=} target
 */
gadgetide.datasource.Executor.prototype.addEventGenerator = function(source, target) {
  this.handler_.listen(source,
    gadgetide.datasource.Executor.EventType.TIMING_EVENT, this.handleTimerEvent);
  var id = goog.getUid(source);
  if (target) {
    var targets = this.dependents[id];
    if (!targets) {
      targets = [];
      this.dependents[id] = targets;
    }
    targets.push(target);
  }
};

gadgetide.datasource.Executor.prototype.handleTimerEvent = function(e) {
  var targets = this.dependents[goog.getUid(e.target)];
  if (targets) {
    for (var i = 0; i < targets.length; i++) {
      var target = targets[i];
      var tree = this.graph_.getDependencyTree(target);
      new gadgetide.datasource.ExecutionContext(tree).execute();
    }
  }
};


/** @enum {string} */
gadgetide.datasource.Executor.EventType = {
  TIMING_EVENT: goog.events.getUniqueId('timer_event')
};

