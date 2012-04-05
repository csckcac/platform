goog.provide('gadgetide.uielement.JQBarRenderer');

goog.require('gadgetide.uielement.UIElementRenderer');
goog.require('gadgetide.client.Loader');


/**
 * @constructor
 * @extends {gadgetide.uielement.UIElementRenderer}
 */
gadgetide.uielement.JQBarRenderer = function() {
  gadgetide.uielement.UIElementRenderer.call(this);
};
goog.inherits(gadgetide.uielement.JQBarRenderer,
  gadgetide.uielement.UIElementRenderer);
goog.addSingletonGetter(gadgetide.uielement.JQBarRenderer);

gadgetide.uielement.JQBarRenderer.CSS_CLASS = 'gc-linec';


/**
 * @inheritDoc
 */
gadgetide.uielement.JQBarRenderer.prototype.getCssClass = function(control) {
  return gadgetide.uielement.JQBarRenderer.CSS_CLASS;
};

gadgetide.uielement.JQBarRenderer.prototype.createDom = function(control) {
  var el = goog.ui.ControlRenderer.prototype.createDom.call(this, control);
  var id = control.getId().replace(':', 'chart_');
  control.setId(id);
  el.id = id;
  return el;
};

/**
 * @inheritDoc
 */
gadgetide.uielement.JQBarRenderer.prototype.initializeDom = function(control) {
  var makePlot = function() {
    var jqplot = goog.global['$']['jqplot'];
    var plot = jqplot(control.getId(), [
      [0]
    ], {
      'seriesDefaults':{
        'renderer':goog.global['$']['jqplot']['BarRenderer']
      },
      'axes': {
        'xaxis': {
          'renderer': goog.global['$']['jqplot']['CategoryAxisRenderer']
        }
      }});
    control.setPlot(plot);
  };
  if (gadgetide.IDE) {
    var l = gadgetide.client.Loader.getInstance();
    l.loadJSOnce(['js/jquery/jquery.min.js','js/jqplot/jquery.jqplot.min.js',
      'js/jqplot/plugins/jqplot.barRenderer.min.js',
      'js/jqplot/plugins/jqplot.categoryAxisRenderer.min.js',
      'js/jqplot/plugins/jqplot.pointLabels.min.js'],
      makePlot
    );
  } else {
    makePlot();
  }
};

/**
 * @inheritDoc
 */
gadgetide.uielement.JQBarRenderer.prototype.canDecorate = function(element) {
  return element.tagName === 'DIV';
};

