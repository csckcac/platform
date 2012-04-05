goog.provide('gadgetide.uielement.DateTimePickerRenderer');

goog.require('goog.dom');
goog.require('goog.date');
goog.require('gadgetide.uielement.UIElementRenderer');
goog.require('goog.ui.DatePicker');
goog.require('goog.ui.registry');
goog.require('goog.events');

/**
 * @constructor
 * @extends {gadgetide.uielement.UIElementRenderer}
 */
gadgetide.uielement.DateTimePickerRenderer = function() {
    gadgetide.uielement.UIElementRenderer.call(this);
};
goog.inherits(gadgetide.uielement.DateTimePickerRenderer,
    gadgetide.uielement.UIElementRenderer);
goog.addSingletonGetter(gadgetide.uielement.DateTimePickerRenderer);


gadgetide.uielement.DateTimePickerRenderer.CSS_CLASS = 'hasDatepicker';

/**
 * @inheritDoc
 */
gadgetide.uielement.DateTimePickerRenderer.prototype.createDom =
    function(control){
        gadgetide.uielement.UIElementRenderer.prototype.createDom.call(this,control);
//        var el = control.getDomHelper().createDom('div');
        var el = control.getDomHelper().createDom('input',{'type':'text',
            'class':'dpPickerTxtBox'});
        var id = control.getId().replace(':','picker_');
        /*var dpTxtBox = el.appendChild(goog.dom.createDom('input',{'type':'text',
            'class':'dpPickerTxtBox',
            'id':id+'_txt'}));*/
        /*var dpWidget = el.appendChild(goog.dom.createDom('div',
            {'id':id+'_widget','class':'ui-datePickerDiv'}));*/
        control.setId(id);
        el.id = id;
        return el;
    };

/**
 * @inheritDoc
 */
gadgetide.uielement.DateTimePickerRenderer.prototype.initializeDom =
    function(control){
        gadgetide.uielement.UIElementRenderer.prototype.initializeDom.call(this,control);
        var makePicker = function(){
            var dtPicker = new goog.ui.DatePicker();
            //goog.dom.insertSiblingAfter(goog.dom.createDom('span',{'class':'datePickerBtn'}),goog.dom.getElement(control.getId()));
            goog.dom.insertSiblingAfter(goog.dom.createDom('div',{'class':'ui-datePickerDiv','id':control.getId()+'_pickerDiv'}),goog.dom.getElement(control.getId()));
            var dtPickerDiv = goog.dom.getElement(control.getId()+'_pickerDiv');
//                control.getPare(goog.dom.createDom('div',{'class':'ui-datePickerDiv'}));
//            var dtPickerTxtBox = new goog.ui.Control('click to select...');
//            dtPickerTxtBox.render(goog.dom.getElement(control.getId()));
            dtPicker.render(dtPickerDiv);
            var dtPickerTxtBox = goog.dom.getElement(control.getId());
            goog.events.listen(dtPicker,goog.ui.DatePicker.Events.CHANGE,
                function(event){
                    dtPickerTxtBox.value =
                        event.date ? event.date.toIsoString(true) : 'none';
                        jQuery(dtPickerDiv).hide(300);
                });
            goog.events.listen(dtPickerTxtBox,goog.events.EventType.FOCUSIN,function(e){

            });
            jQuery(dtPickerTxtBox).focusin(function(){
                jQuery(dtPickerDiv).show(300);
            });
            /*jQuery(dtPickerTxtBox).focusout(function(){
                jQuery(dtPickerDiv).hide(300);
            });*/
        };
        if (gadgetide.IDE) {
            var l = gadgetide.client.Loader.getInstance();
            l.loadJSOnce(['js/jquery/jquery.min.js'],
            makePicker
         );
        } else {
           makePicker();
        }
    };

/**
 * @inheritDoc
 */
gadgetide.uielement.DateTimePickerRenderer.prototype.getCssClass = function(control) {
  return gadgetide.uielement.DateTimePickerRenderer.CSS_CLASS;
};

/**
 * @inheritDoc
 */

gadgetide.uielement.DateTimePickerRenderer.prototype.getContext = function(uiElm) {
  return {'text': uiElm.getElement().value};
};