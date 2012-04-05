goog.provide('gadgetide.client.Admin');

goog.require('goog.net.XhrIo');
goog.require('gadgetide.client.util');
goog.require('goog.testing.singleton');

/**
 * @constructor
 */
gadgetide.client.Admin = function() {
};

/**
 * this is here so we can mock it
 * @type {gadgetide.client.Admin}
 */
gadgetide.client.Admin.instance_;

/**
 * @return {gadgetide.client.Admin}
 */
gadgetide.client.Admin.getInstance = function() {
  if (!gadgetide.client.Admin.instance_) {
    gadgetide.client.Admin.instance_ = new gadgetide.client.Admin();
    goog.testing.singletons_.push(gadgetide.client.Admin);
  }
  return gadgetide.client.Admin.instance_;
};

/**
 * A reference to the XhrIo logger
 * @type {goog.debug.Logger}
 * @private
 */
gadgetide.client.Admin.prototype.logger_ =
  goog.debug.Logger.getLogger('gadgetide.client.Admin');

/**
 * @const
 * @type {string}
 */
gadgetide.client.Admin.AJAX_URL = 'admin_ajaxprocessor.jsp';


/**
 *
 * @param {string} action
 * @param {string=} opt_data
 * @param {Function=} opt_callback
 * @param {string=} opt_filter
 */
gadgetide.client.Admin.prototype.sendXhr = function(action, opt_data, opt_callback, opt_filter) {
  var param = 'action=' + action + (opt_data ? '&' + opt_data : '');
  this.logger_.fine('admin client call with data ="' + param + '"');
  goog.net.XhrIo.send(gadgetide.client.Admin.AJAX_URL, opt_callback &&
    function(e) {
      var xhr = e.target;
      var obj = null;
      try {
        obj = gadgetide.client.util.JSON_PARSE(xhr.getResponseText());
        obj = opt_filter ? obj[opt_filter] : obj;
      } catch(ex) {
      }
      if (obj) {
        opt_callback(obj);

      }
    },
    'POST',
    param
  );

};

/**
 * @param {string} settings
 * @param {Function=} opt_callback
 */
gadgetide.client.Admin.prototype.saveSettings = function(settings, filename, opt_callback) {
  this.sendXhr(
    'saveSettings',
    this.joinParam(['settings','filename'], settings, filename),
    opt_callback
  );
    //hack : alerting file location
  //todo:check isSuccess method
    alert("File saved in to /registry/resource/_system/config/repository/dashboards/gadgets/"+ filename +".xml");
};

/**
 *
 * @param {string} wsdlUrl
 * @param {string} endpoint
 * @param {Function=} opt_callback
 */
gadgetide.client.Admin.prototype.getOperations = function(wsdlUrl, endpoint, opt_callback) {
  this.sendXhr(
    'getOperations',
    this.joinParam(['wsdlUrl','endpoint'], wsdlUrl, endpoint),
    opt_callback,
    'operations'
  );
};

/**
 *
 * @param {string} wsdlUrl
 * @param {Function=} opt_callback
 */
gadgetide.client.Admin.prototype.getEndpoints = function(wsdlUrl, opt_callback) {
  this.sendXhr(
    'getEndpoints',
    this.joinParam(['wsdlUrl'], wsdlUrl),
    opt_callback,
    'endpoints'
  );
};


/**
 *
 * @param {string} wsdlUrl
 * @param {string} endpoint
 * @param {string} operation
 * @param {Function=} opt_callback
 */
gadgetide.client.Admin.prototype.getResponseXml = function(wsdlUrl, endpoint, operation, opt_callback) {
  this.sendXhr(
    'getResponseXml',
    this.joinParam(['wsdlUrl','endpoint', 'operation'], wsdlUrl, endpoint, operation),
    opt_callback,
    'responseSchema'
  );
};


/**
 * @param {Array.<string>} names
 * @param {...string} var_args
 */
gadgetide.client.Admin.prototype.joinParam = function(names, var_args) {
  var args = goog.array.slice(arguments, 1);
  var arr = [];
  var i;
  for (i = 0; i < names.length; i++) {
    arr.push(names[i]);
    arr.push('=');
    arr.push(args[i]);
    if (i != names.length - 1) {
      arr.push('&');
    }
  }
  return arr.join('');
};

