goog.provide('gadgetide.uielement.FuelGaugeRenderer');

goog.require('gadgetide.client.Loader');
goog.require('gadgetide.uielement.UIElementRenderer');
goog.require('goog.dom');


/**
 * @constructor
 * @extends {gadgetide.uielement.UIElementRenderer}
 */
gadgetide.uielement.FuelGaugeRenderer = function() {
    gadgetide.uielement.UIElementRenderer.call(this);
};
goog.inherits(gadgetide.uielement.FuelGaugeRenderer,
    gadgetide.uielement.UIElementRenderer);
goog.addSingletonGetter(gadgetide.uielement.FuelGaugeRenderer);

/**
 *
 */
gadgetide.uielement.FuelGaugeRenderer.CSS_CLASS = 'gc-linec';


/**
 * @inheritDoc
 */
gadgetide.uielement.FuelGaugeRenderer.prototype.getCssClass =
    function(control) {
    return gadgetide.uielement.FuelGaugeRenderer.CSS_CLASS;
};


gadgetide.uielement.FuelGaugeRenderer.prototype.createDom = function(control) {
    var dom = control.getDomHelper();
    var element = dom.createDom(
        'canvas', this.getClassNames(control));
    var id = control.getId().replace(':', 'chart_');
    control.setId(id);
    element.id = id;
    return element;
};

/**
 * @inheritDoc
 */
gadgetide.uielement.FuelGaugeRenderer.prototype.initializeDom =
    function(control) {
        var makeGuage = function() {
            /** @type {function(new:jsgauge.Gauge, Element, jsgauge.Options)} */
            var Gauge = goog.global['Gauge'];
            /** @type {jsgauge.Gauge} */
            var periodicRand = new Gauge(goog.dom.getElement(control.getId()),
                {label: 'Gauge'});
            //periodicRand.setValue(43);
            control.setGauge(periodicRand);
        };
        if (gadgetide.IDE) {
            var l = gadgetide.client.Loader.getInstance();
            l.loadJSOnce(['js/gauge/gauge.min.js'],
                makeGuage
            );
        } else {
            makeGuage();
        }
    };

/**
 * @inheritDoc
 */
gadgetide.uielement.FuelGaugeRenderer.prototype.canDecorate =
    function(element) {
        return element.tagName === 'DIV';
    };

