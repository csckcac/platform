goog.provide('gadgetide.datasource.Tree');
goog.provide('gadgetide.datasource.TreeFactory');

goog.require('gadgetide.schema');

/**
 *
 * @param {gadgetide.datasource.Graph} graph
 * @param {gadgetide.datasource.Delegator} root
 * @return {gadgetide.datasource.Tree}
 */
gadgetide.datasource.TreeFactory.createFromSingleRoot = function(graph, root) {

  /** @type {Object.<number,number>}
   * map between delegate UIDs and position of the delegators in the array. */
  var indexes = {};


  /** @type {Array.<gadgetide.datasource.Delegator>}
   * temp array of all the Delegators. */
  var delegates = [];

  /** @type {Array.<Array.<number>>} */
  var dependencies = [];

  /** @type {Array.<gadgetide.datasource.Delegator>}
   * delegates with no dependencies. */
  var leaves = [];

  /** @type {Array.<Array.<number>>}
   *  array of each delegate's dependents' index.
   */
  var dependents = [];

  /**
   *
   * @type {Array.<Array.<gadgetide.datasource.Graph.Entry>>}
   */
  var transfoms = [];


  var populateDelegators = function(delegator) {
    var id = goog.getUid(delegator);
    var index = indexes[id];

    if (!index) {
      index = delegates.length;
      indexes[id] = index;
      delegates.push(delegator);
      dependents[index] = [];

      /** @type {Array.<gadgetide.datasource.Graph.Entry>} */
      var entries = graph.getDirectDependencies(delegator);
      if (entries.length == 0) {
        // if it has no dependencies it is a leaf
        leaves.push(delegator);
      } else {
        for (var i = 0; i < entries.length; i++) {
          /** @type {gadgetide.datasource.Graph.Entry} */
          var entry = entries[i];
          populateDelegators(entry.from);
        }
      }
    }
  };
  /* pass 1
   * we are doing this two passes though the graph.
   * in this pass collect all the dependencies and assigning them indexes.
   */
  populateDelegators(root);

  var populateDependents = function(delegator) {
    var id = goog.getUid(delegator);
    var index = indexes[id];
    var visited = Boolean(dependencies[index]);
    if (!visited) {
      dependencies[index] = [];
      transfoms[index] = [];
    }
    /** @type {Array.<gadgetide.datasource.Graph.Entry>} */
    var entries = graph.getDirectDependencies(delegator);
    for (var i = 0; i < entries.length; i++) {
      var entry = entries[i];
      var dependencyId = goog.getUid(entry.from);
      var dependencyIndex = indexes[dependencyId];

      /* we could have arrive at the same delegator using two paths. so we have
       * check before pushing decencies. */
      if (goog.array.indexOf(dependents[dependencyIndex], index) < 0) {
        dependents[dependencyIndex].push(index);
      }

      /* we don't have to check each like in dependents (above) because we can
       * stop if the node is visited */
      if (!visited) {
        dependencies[index].push(dependencyIndex);
        transfoms[index].push(entry);
        populateDependents(entry.from);
      }
    }
  };
  /* pass 2
   * populate the dependencies, dependents. */
  populateDependents(root);


  return new gadgetide.datasource.Tree(root,
    delegates, dependencies, dependents, indexes, leaves, transfoms);
};


/**
 * @constructor
 */
gadgetide.datasource.Tree =
  function(root, delegates, dependencies, dependents, indexCache, leaves, tr) {
    this.root_ = root;
    this.delegates_ = delegates;
    this.dependencies_ = dependencies;
    this.indexCache_ = indexCache;
    this.leaves_ = leaves;
    this.dependents_ = dependents;
    this.transforms_ = tr;
  };


/**
 * the root Delegate.this one has no dependents.
 * @type {gadgetide.datasource.Delegator}
 * @private
 */
gadgetide.datasource.Tree.prototype.root_;

/**
 * array of all Delegates.
 * @type {Array.<gadgetide.datasource.Delegator>}
 * @private
 */
gadgetide.datasource.Tree.prototype.delegates_;

/**
 * this is a map between UID of delegate and it's position in delegates_ array.
 * used for fast look-ups of the index without going through the array.
 * @type {Object.<number,number>}
 * @private
 */
gadgetide.datasource.Tree.prototype.indexCache_;

/**
 * index of each dependencies of each delegate in the same order as
 * this.delegates_
 * @type {Array.<Array.<number>>}
 * @private
 */
gadgetide.datasource.Tree.prototype.dependencies_;

/**
 * index of each dependent of each delegate in the same order as
 * this.delegates_
 * even though this are calculable using dependencies_ array this is cached
 * separately for speed look-ups.
 * @type {Array.<Array.<number>>}
 * @private
 */
gadgetide.datasource.Tree.prototype.dependents_;

/**
 * @type {Array.<Array.<gadgetide.datasource.Graph.Entry>>}
 * @private
 */
gadgetide.datasource.Tree.prototype.transforms_;


/**
 * delegates with no dependencies.
 * even though this are calculable using dependencies_ array this is cached
 * separately for speed look-ups.
 * @type {Array.<gadgetide.datasource.Delegator>}
 */
gadgetide.datasource.Tree.prototype.leaves_;

/**
 * @return {gadgetide.datasource.Delegator}
 */
gadgetide.datasource.Tree.prototype.getRoot = function() {
  return this.root_;
};

/**
 * @return {Array.<gadgetide.datasource.Delegator>}
 */
gadgetide.datasource.Tree.prototype.getLeaves = function() {
  return this.leaves_;
};

/**
 * @param {gadgetide.datasource.Delegator} delegator
 * @param {Array.<boolean|undefined>} completed
 * @return {Array.<gadgetide.datasource.Delegator>}
 */
gadgetide.datasource.Tree.prototype.satisfiedBy = function(delegator, completed) {
  var dependants = this.dependents_[this.getIndex(delegator)];
  var satisfied = [];
  for (var i = 0; i < dependants.length; i++) {
    var dependant = dependants[i];
    var requirements = this.dependencies_[dependant];
    var add = true;
    for (var j = 0; j < requirements.length; j++) {
      var requirement = requirements[j];
      if (!completed[requirement]) {
        add = false;
        break;
      }
    }
    if (add) {
      satisfied.push(this.delegates_[dependant]);
    }
  }
  return satisfied;
};

/**
 * @param {gadgetide.datasource.Delegator} delegator
 * @return {number}
 */
gadgetide.datasource.Tree.prototype.getIndex = function(delegator) {
  return this.indexCache_[goog.getUid(delegator)];
};

/**
 *
 * @param {gadgetide.datasource.Delegator} delegator
 * @param {Array.<Object|undefined>} data
 * @return {Object}
 */
gadgetide.datasource.Tree.prototype.getInputFor = function(delegator, data) {
  var j, qname;
  var index = this.getIndex(delegator);
  var dependencies = this.dependencies_[index];
  var transforms = this.transforms_[index];

  //TODO: more optimization possible. eg: no change mode.
  var obj = {};

  if (dependencies.length != 0) {

    for (var i = 0; i < dependencies.length; i++) {
      var dependency = dependencies[i];
      var from = transforms[i].fromField;
      var to = transforms[i].toField;

      var other = data[dependencies[i]];
      var returned1 =  gadgetide.schema.filterPath(from,other || {});

      var returned2 =  gadgetide.schema.makePath(to,obj);
      gadgetide.schema.setValue(returned2.data,returned2.key,
        gadgetide.schema.getValue(returned1.data,returned1.inArray)
      );
    }
  }
  return obj;
};

/**
 *
 * @return {number}
 */
gadgetide.datasource.Tree.prototype.getSize = function() {
  return this.delegates_.length;
};

