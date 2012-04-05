goog.provide('gadgetide.uielement.JQLineRenderer');

goog.require('gadgetide.uielement.UIElementRenderer');
goog.require('gadgetide.client.Loader');


/**
 * @constructor
 * @extends {gadgetide.uielement.UIElementRenderer}
 */
gadgetide.uielement.JQLineRenderer = function() {
  gadgetide.uielement.UIElementRenderer.call(this);
};
goog.inherits(gadgetide.uielement.JQLineRenderer,
  gadgetide.uielement.UIElementRenderer);
goog.addSingletonGetter(gadgetide.uielement.JQLineRenderer);

gadgetide.uielement.JQLineRenderer.CSS_CLASS = 'gc-linec';


/**
 * @inheritDoc
 */
gadgetide.uielement.JQLineRenderer.prototype.getCssClass = function(control) {
  return gadgetide.uielement.JQLineRenderer.CSS_CLASS;
};

gadgetide.uielement.JQLineRenderer.prototype.createDom = function(control) {
  var el = goog.ui.ControlRenderer.prototype.createDom.call(this, control);
  var id = control.getId().replace(':', 'chart_');
  control.setId(id);
  el.id = id;
  return el;
};

/**
 * @inheritDoc
 */
gadgetide.uielement.JQLineRenderer.prototype.initializeDom = function(control) {
  var makePlot = function() {
    var plot = goog.global['$']['jqplot'](control.getId(), [
      [0]
    ]);
    control.setPlot(plot);
  };
  if (gadgetide.IDE) {
    var l = gadgetide.client.Loader.getInstance();
    l.loadJSOnce(['js/jquery/jquery.min.js','js/jqplot/jquery.jqplot.min.js',
      'js/jqplot/plugins/jqplot.canvasAxisLabelRenderer.min.js',
      'js/jqplot/plugins/jqplot.canvasTextRenderer.min.js'],
      makePlot
    );
  }else{
    makePlot();
  }
};

/**
 * @inheritDoc
 */
gadgetide.uielement.JQLineRenderer.prototype.canDecorate = function(element) {
  return element.tagName === 'DIV';
};

