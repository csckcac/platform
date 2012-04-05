goog.provide('gadgetide.uielement.JQPieRenderer');

goog.require('gadgetide.uielement.UIElementRenderer');
goog.require('gadgetide.client.Loader');


/**
 * @constructor
 * @extends {gadgetide.uielement.UIElementRenderer}
 */
gadgetide.uielement.JQPieRenderer = function() {
  gadgetide.uielement.UIElementRenderer.call(this);
};
goog.inherits(gadgetide.uielement.JQPieRenderer,
  gadgetide.uielement.UIElementRenderer);
goog.addSingletonGetter(gadgetide.uielement.JQPieRenderer);

gadgetide.uielement.JQPieRenderer.CSS_CLASS = 'gc-pie';


/**
 * @inheritDoc
 */
gadgetide.uielement.JQPieRenderer.prototype.getCssClass = function(control) {
  return gadgetide.uielement.JQPieRenderer.CSS_CLASS;
};

gadgetide.uielement.JQPieRenderer.prototype.createDom = function(control) {
  var el = goog.ui.ControlRenderer.prototype.createDom.call(this, control);
  var id = control.getId().replace(':', 'chart_');
  control.setId(id);
  el.id = id;
  return el;
};

/**
 * @inheritDoc
 */
gadgetide.uielement.JQPieRenderer.prototype.initializeDom = function(control) {
  var makePlot = function() {
    var jqplot = goog.global['$']['jqplot'];
    var plot = jqplot(control.getId(), [
      [0,0]
    ], {
      'seriesDefaults':{
        'renderer':goog.global['$']['jqplot']['PieRenderer'],
      'rendererOptions':{
           'showDataLabels': true
        }
      },
      'legend': { 'show':true, 'location': 'e' }
    });
    control.setPlot(plot);
  };
  if (gadgetide.IDE) {
    var l = gadgetide.client.Loader.getInstance();
    l.loadJSOnce(['js/jquery/jquery.min.js','js/jqplot/jquery.jqplot.min.js',
      'js/jqplot/plugins/jqplot.pieRenderer.min.js'],
      makePlot
    );
  } else {
    makePlot();
  }
};

/**
 * @inheritDoc
 */
gadgetide.uielement.JQPieRenderer.prototype.canDecorate = function(element) {
  return element.tagName === 'DIV';
};

