goog.provide('gadgetide.ui.ImageTabRenderer');

goog.require('goog.dom');
goog.require('goog.ui.Tab');
goog.require('goog.ui.TabBar.Location');
goog.require('goog.ui.TabRenderer');
goog.require('goog.ui.registry');


/**
 * Image based renderer for {@link goog.ui.Tab}s.
 * @constructor
 * @extends {goog.ui.TabRenderer}
 */
gadgetide.ui.ImageTabRenderer = function() {
  goog.ui.TabRenderer.call(this);
};
goog.inherits(gadgetide.ui.ImageTabRenderer, goog.ui.TabRenderer);
goog.addSingletonGetter(gadgetide.ui.ImageTabRenderer);


/**
 * Default CSS class to be applied to the root element of components rendered
 * by this renderer.
 * @type {string}
 */
gadgetide.ui.ImageTabRenderer.CSS_CLASS = goog.getCssName('gide-tab');


/**
 * Returns the CSS class name to be applied to the root element of all tabs
 * rendered or decorated using this renderer.
 * @return {string} Renderer-specific CSS class name.
 * @override
 */
gadgetide.ui.ImageTabRenderer.prototype.getCssClass = function() {
  return gadgetide.ui.ImageTabRenderer.CSS_CLASS;
};


/**
 * Creates the tab's DOM structure, based on the containing tab bar's location
 * relative to tab contents.  For example, the DOM for a tab in a tab bar
 * would look like this:
 * <pre>
 *   <div>
 *       <div class="gide-tab-left"></div>
 *       <div class="gide-tab-caption">Hungry Hungry Hippos</div>
 *       <div class="gide-tab-right"></div>
 *   </div>
 * </pre>
 * @param {goog.ui.Control} tab Tab to render.
 * @return {Element} Root element for the tab.
 * @override
 */
gadgetide.ui.ImageTabRenderer.prototype.createDom = function(tab) {
  return this.decorate(tab,
    gadgetide.ui.ImageTabRenderer.superClass_.createDom.call(this, tab));
};


/**
 * Decorates the element with the tab.  Overrides the superclass implementation
 * by wrapping the tab's content in a DIV structure.
 * @param {goog.ui.Control} tab Tab to decorate the element.
 * @param {Element} element Element to decorate.
 * @return {Element} Decorated element.
 * @override
 */
gadgetide.ui.ImageTabRenderer.prototype.decorate = function(tab, element) {
  var tabBar = tab.getParent();

  if (!this.getContentElement(element)) {
    // The element to be decorated doesn't appear to have the full tab DOM,
    // so we have to create it.
    element.appendChild(this.createTab(tab.getDomHelper(), element.childNodes,
      tabBar.getLocation()));
  }

  return gadgetide.ui.ImageTabRenderer.superClass_.decorate.call(this, tab,
    element);
};


/**
 * Creates a image based implementing a rounded corner tab.
 * @param {goog.dom.DomHelper} dom DOM helper to use for element construction.
 * @param {goog.ui.ControlContent} caption Text caption or DOM structure
 *     to display as the tab's caption.
 * @param {goog.ui.TabBar.Location} location Tab bar location relative to the
 *     tab contents.
 * @return {Element} Table implementing a rounded corner tab.
 * @protected
 */
gadgetide.ui.ImageTabRenderer.prototype.createTab = function(dom, caption,
                                                             location) {

  var baseClass = this.getStructuralCssClass();
  return dom.createDom('div', {

    }, dom.createDom('div', goog.getCssName(baseClass, 'left')),
    dom.createDom('div', goog.getCssName(baseClass, 'caption'), caption),
    dom.createDom('div', goog.getCssName(baseClass, 'right')));
};


/** @inheritDoc */
gadgetide.ui.ImageTabRenderer.prototype.getContentElement = function(element) {
  var baseClass = this.getStructuralCssClass();
  return element && goog.dom.getElementsByTagNameAndClass(
    'div', goog.getCssName(baseClass, 'caption'), element)[0];
};


// Register a decorator factory function for goog.ui.Tabs using the rounded
// tab renderer.
goog.ui.registry.setDecoratorByClassName(
  gadgetide.ui.ImageTabRenderer.CSS_CLASS, function() {
    return new goog.ui.Tab(null, gadgetide.ui.ImageTabRenderer.getInstance());
});

