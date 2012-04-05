goog.provide('gadgetide.pluginlist');

goog.require('gadgetide.datasource.TimingPolicy');
goog.require('gadgetide.dataunit.DynamicText');
goog.require('gadgetide.dataunit.Table');
goog.require('gadgetide.dataunit.RandomInt');
goog.require('gadgetide.dataunit.TextBox');
goog.require('gadgetide.dataunit.JQLine');
goog.require('gadgetide.dataunit.JQBar');
goog.require('gadgetide.dataunit.WSDLDataSource');
goog.require('gadgetide.dataunit.Cassandra');
goog.require('gadgetide.ui.ImageTabRenderer');
goog.require('gadgetide.uielement.DynamicTextRenderer');
goog.require('gadgetide.uielement.TableRenderer');
goog.require('gadgetide.uielement.JQLine');
goog.require('gadgetide.uielement.JQBar');
goog.require('gadgetide.uielement.TextBoxRenderer');
goog.require('gadgetide.uielement.LabelRenderer');
goog.require('gadgetide.flow.WSDLConfigEditor');
goog.require('gadgetide.flow.CassandraEditor');
goog.require('gadgetide.dataunit.FuelGauge');
goog.require('gadgetide.uielement.FuelGauge');
goog.require('gadgetide.dataunit.JQPie');
goog.require('gadgetide.uielement.JQPie');
goog.require('gadgetide.dataunit.DateTimePicker');
goog.require('gadgetide.uielement.DateTimePickerRenderer');
goog.require('gadgetide.dataunit.DropDownList');
goog.require('gadgetide.uielement.DropDownListRenderer');
goog.require('gadgetide.dataunit.Constant');
goog.require('gadgetide.flow.ConstantEditor');
goog.require('gadgetide.dataunit.LexicalStringGen');

/**
 * @return {Array.<gadgetide.datasource.PluginDef>}
 */
gadgetide.pluginlist.load = function() {
  /** @type {Array.<gadgetide.datasource.PluginDef>} */
  var list = [];

  list.push({
    path: 'UI/Static Text',
    datasource: null,
    container: null,
    ui: gadgetide.uielement.LabelRenderer,
    timing: gadgetide.datasource.TimingPolicy.NONE
  });

  list.push({
    path: 'UI/Line Chart',
    datasource: gadgetide.dataunit.JQLine,
    container: null,
    ui: gadgetide.uielement.JQLine,
    timing: gadgetide.datasource.TimingPolicy.GLOBAL_TICK
  });

  list.push({
    path: 'UI/Bar Chart',
    datasource: gadgetide.dataunit.JQBar,
    container: null,
    ui: gadgetide.uielement.JQBar,
    timing: gadgetide.datasource.TimingPolicy.GLOBAL_TICK
  });

  list.push({
    path: 'Math/Random Integer',
    datasource: gadgetide.dataunit.RandomInt,
    container: null,
    ui:null,
    timing: gadgetide.datasource.TimingPolicy.NONE
  });

  list.push({
    path: 'UI/Dynamic Text',
    datasource: gadgetide.dataunit.DynamicText,
    container: null,
    ui: gadgetide.uielement.DynamicTextRenderer,
    timing: gadgetide.datasource.TimingPolicy.GLOBAL_TICK
  });

  list.push({
    path: 'UI/Table',
    datasource: gadgetide.dataunit.Table,
    container: null,
    ui: gadgetide.uielement.TableRenderer,
    timing: gadgetide.datasource.TimingPolicy.GLOBAL_TICK
  });

  list.push({
    path: 'UI/Text Box',
    datasource: gadgetide.dataunit.TextBox,
    container: null,
    ui: gadgetide.uielement.TextBoxRenderer,
    timing: gadgetide.datasource.TimingPolicy.NONE
  });

  list.push({
    path: 'Data Sources/Web Service (WSDL)',
    datasource: gadgetide.dataunit.WSDLDataSource,
    container: !COMPILED || gadgetide.IDE ? gadgetide.flow.WSDLConfigEditor : null,
    ui: null,
    timing: gadgetide.datasource.TimingPolicy.NONE
  });

  list.push({
    path: 'Data Sources/Cassandra Sources',
    datasource: gadgetide.dataunit.Cassandra,
    container: !COMPILED || gadgetide.IDE ? gadgetide.flow.CassandraEditor : null,
    ui: null,
    timing: gadgetide.datasource.TimingPolicy.NONE
  });

  list.push({
      path: 'UI/Fuel Gauge',
      datasource: gadgetide.dataunit.FuelGauge,
      container: null,
      ui: gadgetide.uielement.FuelGauge,
      timing: gadgetide.datasource.TimingPolicy.GLOBAL_TICK
  });

  list.push({
    path: 'UI/Pie Chart',
    datasource: gadgetide.dataunit.JQPie,
    container: null,
    ui: gadgetide.uielement.JQPie,
    timing: gadgetide.datasource.TimingPolicy.GLOBAL_TICK
  });

  list.push({
    path: 'UI/Date Time Picker',
    datasource: gadgetide.dataunit.DateTimePicker,
    container: null,
    ui: gadgetide.uielement.DateTimePickerRenderer,
    timing: gadgetide.datasource.TimingPolicy.NONE
  });

  list.push({
      path: 'UI/ComboBox',
      datasource: gadgetide.dataunit.DropDownList,
      container: null,
      ui: gadgetide.uielement.DropDownListRenderer,
      timing: gadgetide.datasource.TimingPolicy.GLOBAL_TICK
  });

  list.push({
    path: 'Constant/Text',
    datasource: gadgetide.dataunit.Constant,
    container: !COMPILED || gadgetide.IDE ? gadgetide.flow.ConstantEditor : null,
    ui: null,
    timing: gadgetide.datasource.TimingPolicy.NONE
  });
  list.push({
    path: 'Math/Lexical String',
    datasource: gadgetide.dataunit.LexicalStringGen,
    container: null,
    ui: null,
    timing: gadgetide.datasource.TimingPolicy.NONE
  });
  return list;
}

