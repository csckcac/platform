goog.provide('gadgetide.datasource.ExecutionContext');
goog.require('goog.events.EventTarget');


/**
 * @param {gadgetide.datasource.Tree} tree
 * @extends {goog.events.EventTarget}
 * @constructor
 */
gadgetide.datasource.ExecutionContext = function(tree) {
  goog.events.EventTarget.call(this);
  this.tree_ = tree;
  this.data_ = new Array(tree.getSize());
  this.complete_ = new Array(tree.getSize());
};
goog.inherits(gadgetide.datasource.ExecutionContext, goog.events.EventTarget);

gadgetide.datasource.ExecutionContext.prototype.execute = function() {
  var independents = this.tree_.getLeaves();
  for (var i = 0; i < independents.length; i++) {
    var independent = independents[i];
    independent.execute(null, goog.bind(this.executed_, this, independent));
  }
};

gadgetide.datasource.ExecutionContext.prototype.executed_ =
  function(delegate, output, error) {

//    console.debug("compleated: " + this.tree_.getIndex(delegate) + " with output: " + JSON.stringify(output));

    var index = this.tree_.getIndex(delegate);
    this.data_[index] = output;
    this.complete_[index] = true;

    var delegates = this.tree_.satisfiedBy(delegate, this.complete_);
    for (var i = 0; i < delegates.length; i++) {
      var dependent = delegates[i];
      var input = this.tree_.getInputFor(dependent, this.data_);
      if (dependent == this.tree_.getRoot()) {
        dependent.execute(input);
        this.dispose();
      } else {
        dependent.execute(input, goog.bind(this.executed_, this, dependent));
      }
    }
  };

/** @enum {string} */
gadgetide.datasource.ExecutionContext.EventType = {
  DELEGATE_STARTED: 'delegate_started'
};

gadgetide.datasource.ExecutionContext.prototype.disposeInternal = function() {
  goog.events.EventTarget.prototype.disposeInternal.call(this);
  delete this.tree_;
  delete this.data_;
  delete this.complete_;
};

