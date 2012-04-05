goog.provide('gadgetide.ui.SidePanel');

goog.require('gadgetide.ui.CaptionSideItemRenderer');
goog.require('gadgetide.ui.SideItem');
goog.require('gadgetide.ui.SidePanelRenderer');
goog.require('goog.ui.Container');


/**
 * @typedef {{
 *   name:string,
 *   collapsible:boolean,
 *   items:Array.<gadgetide.ui.SidePanelTreeEl>}}
 */
gadgetide.ui.SidePanelTreeEl;

/**
 * @param {?goog.ui.ContainerRenderer=} opt_renderer Renderer used to render or
 *     decorate the container; defaults to {@link goog.ui.ContainerRenderer}.
 * @param {?goog.dom.DomHelper=} opt_domHelper DOM helper, used for document
 *     interaction.
 * @constructor
 * @extends {goog.ui.Container}
 */
gadgetide.ui.SidePanel =
  function(opt_renderer, opt_domHelper) {
    goog.ui.Container.call(
      this,
      goog.ui.Container.Orientation.VERTICAL,
      opt_renderer || gadgetide.ui.SidePanelRenderer.getInstance(),
      opt_domHelper
    );

    /**@type gadgetide.ui.SidePanelTreeEl */
    var root = {
      name: '',
      collapsible: true,
      items: []};

    this.setModel(root);

    this.setFocusable(false);
  };
goog.inherits(gadgetide.ui.SidePanel, goog.ui.Container);

/**
 * delimiter between path segments.
 * @type {string}
 * @const
 */
gadgetide.ui.SidePanel.PATH_SEPARATOR = '/';

/**
 * compare the SidePanelRenderers by
 *   1) collapsible ones have the priority
 *   2) then by alphanumeric order
 * @param {gadgetide.ui.SidePanelTreeEl} a First SidePanelTreeEl.
 * @param {gadgetide.ui.SidePanelTreeEl} b Second SidePanelTreeEl.
 * @return {number} -1 if a < b, 1 if a > b, 0 if a = b.
 */
gadgetide.ui.SidePanel.COMPARATOR = function(a, b) {
  var aStr = a.name;
  var bStr = b.name;
  if (a.collapsible != b.collapsible) {
    return a.collapsible ? -1 : 1;
  } else {
    if (aStr < bStr) {
      return -1;
    } else if (aStr > bStr) {
      return 1;
    }
    return 0;
  }

};

/**
 * just re-declared to change the return type.
 * @return {gadgetide.ui.SidePanelTreeEl} The Model.
 * @override
 */
gadgetide.ui.SidePanel.prototype.getModel;

/**
 * Add all the Collapsible and Caption SideItems requerd fro viewing given path.
 *
 * @param {string} path Sting containing paths separated by
 *   gadgetide.ui.SidePanel.PATH_SEPARATOR.
 */
gadgetide.ui.SidePanel.prototype.addPath = function(path) {
  /**@type {gadgetide.ui.SidePanelTreeEl} */
  var treeEl = this.getModel();
  var pathFragment;

  var pathFragments = path.split(gadgetide.ui.SidePanel.PATH_SEPARATOR);
  for (var i = 0; i < pathFragments.length - 1; i++) {
    pathFragment = pathFragments[i];
    treeEl = this.addPath_(treeEl, true, pathFragment);
  }
  treeEl = this.addPath_(treeEl, false,
    pathFragments[pathFragments.length - 1], path);
};

/**
 * add a SideItem to sorted position.
 *
 * @private
 * @param {gadgetide.ui.SidePanelTreeEl} parentEl SidePanelTreeEl to which the
 *   new SidePanelTreeEl will get added.
 * @param {boolean} collapsible Is it a Collapsible or Caption type Item.
 * @param {string} name Caption for the SideItem.
 * @param {string = } fullPath
 * @return {gadgetide.ui.SidePanelTreeEl} newly added SidePanelTreeEl or
 *   already excising one with the same value (if there was any).
 */
gadgetide.ui.SidePanel.prototype.addPath_ = function(parentEl, collapsible, name, fullPath) {
  /**@type gadgetide.ui.SidePanelTreeEl */
  var childEl = {
    collapsible: collapsible,
    name: name,
    items: []
  };

  var arrIndex = goog.array.binarySearch(parentEl.items, childEl,
    gadgetide.ui.SidePanel.COMPARATOR);

  if (arrIndex < 0) {
    goog.array.insertAt(parentEl.items, childEl, -(arrIndex + 1));
    var renderer = collapsible ?
      gadgetide.ui.CollapsibleSideItemRenderer.getInstance() :
      gadgetide.ui.CaptionSideItemRenderer.getInstance();

    var count = 0;
    //count the items in the tree up this to find out liner index of the item
    //there are better ways to do this, but for simplicity left this way.
    var countItems = function(inEl, to) {
      for (var i = 0; i < inEl.items.length; i++) {
        var item = inEl.items[i];
        if (item !== to) {
          count++;
          if (!countItems(item, to)) {
            return false;
          }
        } else {
          return false;
        }
      }
      return true;
    };
    countItems(this.getModel(), childEl);


    this.addChildAt(new gadgetide.ui.SideItem(name, fullPath || null, undefined, renderer),
      count, true/*render*/);
    return childEl;
  } else {
    return parentEl.items[arrIndex];
  }
};


/** @inheritDoc */
gadgetide.ui.SidePanel.prototype.enterDocument = function() {
  goog.ui.Container.prototype.enterDocument.call(this);
};

