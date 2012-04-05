goog.provide('gadgetide.client.Loader');

goog.require('goog.net.BulkLoader');
goog.require('goog.Timer');


/**
 * @constructor
 */
gadgetide.client.Loader = function() {
  this.loaded_ = {};
  this.pending_ = {};
  this.eventHandler_ = new goog.events.EventHandler(this);
};
goog.addSingletonGetter(gadgetide.client.Loader);

/**
 * @param {Array.<string>} uris The URIs of the js files to load.
 * @param {function()=} opt_successFn The callback for success.
 * @param {function(?number)=} opt_errorFn The callback for error.
 */
gadgetide.client.Loader.prototype.loadJS =
  function(uris, opt_successFn, opt_errorFn) {
    var bulkLoader = new goog.net.BulkLoader(uris);
    var eventHandler = this.eventHandler_;
    if (opt_successFn) {
      eventHandler.listen(
        bulkLoader,
        goog.net.EventType.SUCCESS,
        goog.bind(this.handleSuccess, this, bulkLoader,
          opt_successFn, opt_errorFn),
        false,
        null);
    }
    if (opt_errorFn) {
      eventHandler.listen(
        bulkLoader,
        goog.net.EventType.ERROR,
        goog.bind(this.handleError, this, bulkLoader, opt_errorFn),
        false,
        null);
    }
    bulkLoader.load();
  };

/**
 * Handles a successful response to a request for one or more modules.
 * @param {goog.net.BulkLoader} bulkLoader The bulk loader.
 * @param {function()} successFn The callback for success.
 * @param {function(?number)=} errorFn The callback for error.
 */
gadgetide.client.Loader.prototype.handleSuccess =
  function(bulkLoader, successFn, errorFn) {
    var jsCode = bulkLoader.getResponseTexts().join('\n');
    var success = true;
    try {
      goog.globalEval(jsCode);
    } catch (e) {
        success = false;
    }
    if(success){
      successFn();
    }else{
      if (errorFn) {
      errorFn(null);
      }
    }
    goog.Timer.callOnce(bulkLoader.dispose, 5, bulkLoader);
  };

/**
 * Handles an error during a request for one or more modules.
 * @param {goog.net.BulkLoader} bulkLoader The bulk loader.
 * @param {function(?number)} errorFn The function to call on failure.
 * @param {?number} status The response status.
 */
gadgetide.client.Loader.prototype.handleError =
  function(bulkLoader, errorFn, status) {
    errorFn(status);
    goog.Timer.callOnce(bulkLoader.dispose, 5, bulkLoader);
  };


/**
 * @param {Array.<string>} uris The URIs of the js files to load.
 * @param {function()=} opt_successFn The callback for success.
 * @param {function(?number)=} opt_errorFn The callback for error.
 */
gadgetide.client.Loader.prototype.loadJSOnce =
  function(uris, opt_successFn, opt_errorFn) {
    var unloadedUris = [];
    for (var i = 0; i < uris.length; i++) {
      var uri = uris[i];
      if (!this.loaded_[uri]) {
        unloadedUris.push(uri);
        this.pending_[uri] = true;
        //TODO: delete following line. handle pending ones properly.
        this.loaded_[uri] = true;
      }
    }
    if(unloadedUris.length==0){
      opt_successFn();
    }else{
      this.loadJS(unloadedUris, opt_successFn, opt_errorFn);
    }
  };

//following code loads js using script tag, now depicted.
////to prevent a bug in EI. see goog.module.Loader.prototype.load_
//goog.Timer.callOnce(function() {
//  var s = goog.dom.createDom('script',
//    {'type': 'text/javascript', 'src': url});
//  document.body.appendChild(s);
//}, 0, this);
//var t = new goog.Timer(100);
//goog.events.listen(t, goog.Timer.TICK,
//
//  goog.bind(this.callbackOnSymbol_,this,opt_symbol,opt_callback,t));
//t.start();
//gadgetide.client.Loader.prototype.callbackOnSymbol_ =
//  function(symbol, callback, timer) {
//    var symbolObj = goog.global;
//    var i = 0;
//    while(i<symbol.length && symbolObj[symbol[i]]){
//      i++;
//    }
//    console.log(i);
//    if(i==symbol.length){
//      timer.dispose();
//      callback(symbolObj);
//    }
//  };

