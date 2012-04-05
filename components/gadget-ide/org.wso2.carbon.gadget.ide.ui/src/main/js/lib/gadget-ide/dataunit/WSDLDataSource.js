goog.provide('gadgetide.dataunit.WSDLDataSource');

goog.require('goog.dom.xml');
goog.require('gadgetide.datasource.DataSource');
goog.require('gadgetide.client.util');
goog.require('gadgetide.client.Loader');
goog.require('goog.json');
goog.require('goog.debug.Logger');
goog.require('gadgetide.client.Admin');


/**
 * @param {!Object} config
 * @extends {gadgetide.datasource.DataSource}
 * @constructor
 */
gadgetide.dataunit.WSDLDataSource = function(config) {
  this.endpoint_ = config['endpoint'];
  this.operation_ = config['operation'];
  var w = config['wsdlUrl'];
  this.wsdlUrl_ = w;
  this.name_ = w.substring(w.lastIndexOf('/') + 1, w.lastIndexOf('?'));
};



/**
 * A reference to the WSDLDataSource logger
 * @type {goog.debug.Logger}
 * @private
 */
gadgetide.dataunit.WSDLDataSource.prototype.logger_ =
  goog.debug.Logger.getLogger('gadgetide.dataunit.WSDLDataSource');

/** @inheritDoc */
gadgetide.dataunit.WSDLDataSource.prototype.execute =
  function(callback, input, context) {
    //serializing the message is costly. so only do if debug
    if (goog.DEBUG) {
      this.logger_.fine('calling web service "' + this.name_ + '" at "' + this.operation_ + '" with ' +
        gadgetide.client.util.JSON_SERIALIZE(input));
    }

    this.operationObj_['callback'] = goog.partial(this.wsCallback, callback);
    this.operationObj_(input);
  };

gadgetide.dataunit.WSDLDataSource.prototype.wsCallback = function(callback, data) {
  var dataJson = gadgetide.client.util.xml2bf(data);
  callback(dataJson);
};

gadgetide.dataunit.WSDLDataSource.prototype.wsError = function(err) {
  this.logger_.warning('web service failed : "' + this.name_ + '" at "' + this.operation_ + '" with error : ' +
    gadgetide.client.util.JSON_SERIALIZE(err));
};

gadgetide.dataunit.WSDLDataSource.prototype.jspPath_ =
    '/carbon/gadget/ide/WSRequestXSSproxy_ajaxprocessor.jsp';

gadgetide.dataunit.WSDLDataSource.prototype.setJspPath = function(){
    var url_ = window.location.pathname;
    var context_ = url_.split('/');
    //url_ = this.jspPath_;
    if(context_[1] != 'carbon' && context_[1] != 'ifr'){url_ = '/'+context_[1]+this.jspPath_;}
    else{url_ = this.jspPath_;}
    return url_;
};

/** @inheritDoc */
gadgetide.dataunit.WSDLDataSource.prototype.loadInputFormat = function(callback) {
  var handleInputLoad = function() {
    var service = goog.global['services'][this.name_];
    this.operationObj_ = service['operations'][this.operation_];

    /* HACK: there is a problem with the new WSRequestXSSproxy_ajaxprocessor.jsp
     * so we are using old version of it locally. should be changed to
     * ${carbon-context}/carbon/admin/jsp/WSRequestXSSproxy_ajaxprocessor.jsp
     * when the problem is fixed.
     */
    if(gadgetide.IDE){
      service['$']['proxyAddress'] = 'WSRequestXSSproxy_ajaxprocessor.jsp';
    }else{
      //KNOWN_ISSUE: dose not work when a carbon context is specified.
      service['$']['proxyAddress'] = this.setJspPath();
    }

    this.operationObj_['onError'] = goog.bind(this.wsError, this);
    callback(this.operationObj_['payloadJSON']());
  };


  if (gadgetide.IDE) {
    gadgetide.client.Loader.getInstance().loadJSOnce(
      ['stubgen_ajaxprocessor.jsp?wsdlUrl=' + this.wsdlUrl_],
      goog.bind(handleInputLoad, this));
  } else {
    handleInputLoad.call(this);
  }
};

/** @inheritDoc */
gadgetide.dataunit.WSDLDataSource.prototype.loadOutputFormat =
  function(callback, inputFormat) {
    if (gadgetide.IDE) {
      gadgetide.client.Admin.getInstance().getResponseXml(this.wsdlUrl_,
        this.endpoint_, this.operation_, function(d) {
          var s = goog.dom.xml.loadXml(d);
          var x = gadgetide.client.util.xml2bf(s);
          callback(x)
        });
    }
  };

