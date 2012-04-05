goog.provide('gadgetide.datasource.Graph');
goog.provide('gadgetide.datasource.Graph.Entry');

goog.require('gadgetide.datasource.TreeFactory');
goog.require('goog.array');


/**
 * @constructor
 */
gadgetide.datasource.Graph = function() {
  this.clear();
};

/**
 * @type {Object.<gadgetide.datasource.Delegator>}
 */
gadgetide.datasource.Graph.prototype.hashed_;

/**
 * @type {Object.<Array.<gadgetide.datasource.Graph.Entry>>}
 */
gadgetide.datasource.Graph.prototype.map_;

/**
 * @type {Object.<gadgetide.datasource.Tree>}
 */
gadgetide.datasource.Graph.prototype.cache_;


/**
 *
 */
gadgetide.datasource.Graph.prototype.clear = function() {
  this.map_ = {};
  this.hashed_ = {};
  this.invalidateCache();
};

/**
 * @return {!Object}
 */
gadgetide.datasource.Graph.prototype.toMemento = function() {
  var pathToArr = function(path) {
    var arr = [];
    for (var i = 0; i < path.length; i++) {
      var pathPart = path[i];
      if (pathPart.namespace) {
        arr.push({'namespace':pathPart.namespace,
          'localpart':pathPart.localpart
        });
      } else {
        arr.push({
          'localpart':pathPart.localpart
        });
      }
    }
    return arr;
  };
  var dependents = [];
  var graph = {'dependents':{'dependent':dependents}};

  for (var delegatorUid in this.map_) {
    /** @type {Array.<gadgetide.datasource.Graph.Entry>} */
    var entries = this.map_[delegatorUid];
    var to = this.hashed_[delegatorUid].getNameAsVar();
    var dependenciesArr = [];

    dependents.push({'name':to,
      'dependencies':{'dependency':dependenciesArr}});

    for (var i = 0; i < entries.length; i++) {
      /** @type {gadgetide.datasource.Graph.Entry} */
      var entry = entries[i];
      dependenciesArr.push({
        'name':entry.from.getNameAsVar(),
        'from':{'qname':pathToArr(entry.fromField.path)},
        'to':{'qname':pathToArr(entry.toField.path)}
      });
    }
  }
  return graph;
};


/**
 *
 * @param {gadgetide.datasource.Delegator} from
 * @param {gadgetide.schema.DataField=} fromField
 * @param {gadgetide.datasource.Delegator=} to
 * @param {gadgetide.schema.DataField=} toField
 */
gadgetide.datasource.Graph.prototype.add = function(from, fromField, to, toField) {
  if (to && fromField && toField) {

    var id = goog.getUid(to);
    this.hashed_[id] = to;

    /** @type {Array.<gadgetide.datasource.Graph.Entry>} */
    var entries = this.map_[id];
    if (!entries) {
      entries = [];
      this.map_[id] = entries;
    }
    entries.push({
      from: from,
      fromField: fromField,
      toField: toField
    });
    this.invalidateCache();
  } else {
    var id = goog.getUid(from);
    this.map_[id] = [];
    this.hashed_[id] = from;
  }
};


/**
 *
 * @param {gadgetide.datasource.Delegator} from
 * @param {gadgetide.schema.DataField=} fromField
 * @param {gadgetide.datasource.Delegator=} to
 * @param {gadgetide.schema.DataField=} toField
 * @return {boolean}
 */
gadgetide.datasource.Graph.prototype.remove = function(from, fromField, to, toField){
  var entries;
  this.invalidateCache();
  if (to) {
    /** @type {Array.<gadgetide.datasource.Graph.Entry>} */
    entries = this.map_[goog.getUid(to)];
    if (entries) {
      for (var i = 0; i < entries.length; i++) {
        var entry = entries[i];
        if (entry.from == from &&  entry.fromField == fromField &&
          entry.toField == toField) {
          //TODO: we should clean up the hash_ map after some threshold.
          return Boolean(goog.array.removeAt(entries, i));
        }
      }
    }
    return false;
  } else {
    return delete this.map_[goog.getUid(from)];
  }
};

/**
 *
 */
gadgetide.datasource.Graph.prototype.invalidateCache =
  function() {
    this.cache_ = null;
  };

/**
 *
 * @param {gadgetide.datasource.Delegator} delegator
 * @return {Array.<gadgetide.datasource.Graph.Entry>}
 */
gadgetide.datasource.Graph.prototype.getDirectDependencies = function(delegator) {
  return delegator ? this.map_[goog.getUid(delegator)] || [] : [];
};

/**
 * @param {gadgetide.datasource.Delegator} delegator
 * @return {gadgetide.datasource.Tree}
 */
gadgetide.datasource.Graph.prototype.getDependencyTree = function(delegator) {
  var id = goog.getUid(delegator);
  var tree = this.cache_ && this.cache_[id];
  if (!tree) {
    tree = gadgetide.datasource.TreeFactory.createFromSingleRoot(this, delegator);
    if (!this.cache_) {
      this.cache_ = {};
    }
    this.cache_[id] = tree;
  }
  return tree;
};


/** @typedef {{
 *     from:gadgetide.datasource.Delegator,
 *     fromField:gadgetide.schema.DataField,
 *     toField:gadgetide.schema.DataField }}
 */
gadgetide.datasource.Graph.Entry;
