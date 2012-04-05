goog.provide('gadgetide.ui.DataSourceViewRenderer');

goog.require('gadgetide.schema');
goog.require('gadgetide.ui.DataFieldView');
goog.require('goog.ui.ContainerRenderer');
/**
 * @constructor
 * @extends {goog.ui.ContainerRenderer}
 */
gadgetide.ui.DataSourceViewRenderer = function() {
  goog.ui.ContainerRenderer.call(this);
};
goog.inherits(gadgetide.ui.DataSourceViewRenderer, goog.ui.ContainerRenderer);
goog.addSingletonGetter(gadgetide.ui.DataSourceViewRenderer);

/** @type {string} */
gadgetide.ui.DataSourceViewRenderer.CSS_CLASS = 'gide-dsview';

/** @inheritDoc */
gadgetide.ui.DataSourceViewRenderer.prototype.getCssClass = function() {
  return gadgetide.ui.DataSourceViewRenderer.CSS_CLASS;
};

/**
 * @param {gadgetide.ui.DataSourceView} dataSourceView DataSourceView to
 *  be rendered.
 * @return {Element} Root element for the container.
 */
gadgetide.ui.DataSourceViewRenderer.prototype.createDom =
  function(dataSourceView) {

    var el = goog.ui.ContainerRenderer.prototype.createDom.call(
      this,
      dataSourceView
    );
    //violates the visibility,but needed for addChild
    dataSourceView.setElementInternal(el);

    var dataFieldTree = dataSourceView.getModel();
    var createFunc = goog.bind(this.createFieldViewInternal, this,
      dataSourceView);

    /**
     * @param {number} level Tab Level.
     * @param {gadgetide.schema.DataFieldTree} tree DataFieldTree, root
     * of which to be rendered.
     */
    function addChildren(level, tree) {
      var control = createFunc(level, tree);
      dataSourceView.addChild(control, true /* opt_render */);
      if (tree.hasChildren) {
        for (var i = 0; i < tree.children.length; i++) {
          var subTree = tree.children[i];
          addChildren(level + 1, subTree);
        }
      }
    }

    addChildren(0, dataFieldTree);

    return el;
  };


/**
 * creates a DataFieldView for given DataFieldTree
 *
 * @param {gadgetide.ui.DataSourceView} dataSourceView DataSourceView to which
 *   this will get appended.
 * @param {number} level Tab level.
 * @param {gadgetide.schema.DataFieldTree} tree DataFieldTree, root
 *   of which to be rendered.
 * @return {gadgetide.ui.DataFieldView} DataFieldView representing the
 *   given field.
 */
gadgetide.ui.DataSourceViewRenderer.prototype.createFieldViewInternal =
  function(dataSourceView, level, tree) {
    return new gadgetide.ui.DataFieldView(tree, level,
      dataSourceView.isIconRight());
  };

