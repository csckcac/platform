goog.provide('gadgetide.dataunit.Cassandra');
/**
 * @param {!Object} config
 * @extends {gadgetide.datasource.DataSource}
 * @constructor
 */
gadgetide.dataunit.Cassandra = function(config) {
  if (!gadgetide.dataunit.Cassandra.ROW_DATA_FILED) {
    var xmlns = {"ns":"http://org.apache.axis2/xsd"};
    var dsSchema = {"ns:queryColumnFamilyResponse": {"@xmlns":xmlns, "ns:return": {"@xmlns":xmlns,"rows": {"@xmlns":xmlns,"row": {"$":"?"}}}}};
    gadgetide.dataunit.Cassandra.ROW_DATA_FILED = gadgetide.schema.parseDataFieldTree(dsSchema).children[0].children[0].children[0];
  }

  var w  = 'http://'+window.location.hostname+':'
        +this.setServicePort()+this.setWsdlPath(config['wsdlUrl']);
  config['wsdlUrl'] = w;
   this.wsdl_ = new gadgetide.dataunit.WSDLDataSource(config);
  // we have to check if this is an array because, in gendered gadget this will not
  // be an array if there is only one index.
  this.indexes_ = goog.isArray(config['indexes']['index']) ? config['indexes']['index'] : [config['indexes']['index']];
  this.tableName_ = config['tableName'];
  this.tableIndexName_ = config['tableIndexName'];

};

gadgetide.dataunit.Cassandra.prototype.setServicePort = function(){
    var port =  window.location.port;
    var d = port-9443;
    port =9763+d;
    return port;
};

gadgetide.dataunit.Cassandra.prototype.setWsdlPath = function(path){
    var url_ = window.location.pathname;
    var context_ = url_.split('/');
    if(context_[1] != 'carbon' && context_[1] != 'ifr'){url_ = '/'+context_[1]+path;}
    else{url_ = path;}
    return url_;
};

/**
 * @type {gadgetide.schema.DataField}
 */
gadgetide.dataunit.Cassandra.ROW_DATA_FILED;

/** @inheritDoc */
gadgetide.dataunit.Cassandra.prototype.execute =
  function(callback, input, context) {
    var xmlns = {
      "xsd": "http://org.apache.axis2/xsd",
      "xsd1": "http://persistence.utils.bam.carbon.wso2.org/xsd"
    };
    var indexes = [];

    for (var i = 0; i < this.indexes_.length; i++) {
      var index = this.indexes_[i];
      indexes.push(
        { "@xmlns": xmlns,
          "xsd1:indexName": {
            "@xmlns": xmlns,
            "$": index
          },
          "xsd1:rangeFirst": {
            "@xmlns": xmlns,
            "$": input ? gadgetide.schema.getValue(input['range']['from'+i]) : '!'
          },
          "xsd1:rangeLast": {
            "@xmlns": xmlns,
            "$":input ? gadgetide.schema.getValue(input['range']['to'+i]) : '~'
          }});
    }

    var payload = {
      "xsd:queryColumnFamily": {
        "@xmlns": xmlns,
        "xsd:table": {
          "@xmlns": xmlns,
          "$": this.tableName_
        },
        "xsd:indexName": {
          "@xmlns": xmlns,
          "$": this.tableIndexName_
        },
        "xsd:indexes":indexes
      }
    };

    this.wsdl_.execute(function(data) {
      var rows = gadgetide.schema.filterPath(gadgetide.dataunit.Cassandra.ROW_DATA_FILED, data).data;
      if (!goog.isArray(rows)) {
        rows = [rows];
      }
      callback({'rows':rows});
    }, payload, context);
  };

gadgetide.dataunit.Cassandra.prototype.loadInputFormat = function(callback) {
  var range = {};
  for (var i = 0; i < this.indexes_.length; i++) {
    range['from' + i] = {'$':'?'};
    range['to' + i] = {'$':'?'};
  }
  this.wsdl_.loadInputFormat(function() {
    callback({'range':range});
  });
};

/** @inheritDoc */
gadgetide.dataunit.Cassandra.prototype.loadOutputFormat = function(callback, inputFormat) {
  this.execute(function(data) {
    data['rows'] = data['rows'][0];
    callback(data);
  }, null, null);
};

