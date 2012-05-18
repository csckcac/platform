$(document).ready(function() {
    if(($.cookie("tab")!=null)){
    var tabLink=$.cookie("tab");
    $('#' + tabLink).tab('show');
    $.cookie("tab", null);
    }

    $('a[data-toggle="tab"]').on('shown', function (e) {
        var clickedTab = e.target.href.split('#')[1];
        ////////////// edit tab
        if (clickedTab == "versions") {
            var apiName = $("#item-info h2")[0].innerHTML.split("-v")[0];
            jagg.post("/site/blocks/usage/ajax/usage.jag", { action:"getProviderAPIVersionUsage", apiName:apiName, server:"https://localhost:9444/" },
                      function (json) {
                          if (!json.error) {
                              var length = json.usage.length,data = [];
                              $('#versionChart').empty();
                              $('#versionTable').find("tr:gt(0)").remove();
                              for (var i = 0; i < length; i++) {
                                  data[i] = [json.usage[i].version, parseInt(json.usage[i].count)];
                                  $('#versionTable').append($('<tr><td>' + json.usage[i].version + '</td><td>' + json.usage[i].count + '</td></tr>'));

                              }

                              if (length > 0) {
                                  $('#versionTable').show();
                                  var plot1 = jQuery.jqplot('versionChart', [data],
                                                            {
                                                                seriesDefaults:{
                                                                    // Make this a pie chart.
                                                                    renderer:jQuery.jqplot.PieRenderer,
                                                                    rendererOptions:{
                                                                        // Put data labels on the pie slices.
                                                                        // By default, labels show the percentage of the slice.
                                                                        showDataLabels:true
                                                                    }
                                                                },
                                                                legend:{ show:true, location:'e' }
                                                            }
                                          );
                              } else {
                                  $('#versionTable').hide();
                                  $('#versionChart').css("fontSize", 14);
                                  $('#versionChart').text('No Data Found ...');
                              }

                          } else {
                              jagg.message(result.message);
                          }
                      }, "json");


            jagg.post("/site/blocks/usage/ajax/usage.jag", { action:"getProviderAPIVersionUserUsage", apiName:apiName, server:"https://localhost:9444/" },
                      function (json) {
                          if (!json.error) {
                              $('#versionUserChart').empty();
                              $('#versionUserTable').find("tr:gt(0)").remove();
                              var jsonLength = json.usage.length,version_user_associative_array = [];
                              for (var n = 0; n < jsonLength; n++) {
                                  if (!version_user_associative_array.hasOwnProperty(json.usage[n].version)) {
                                      version_user_associative_array[json.usage[n].version] = new Array();
                                  }
                                  for (var m = 0; m < json.usage.length; m++) {
                                      version_user_associative_array[json.usage[m].version][json.usage[m].user] = 0;
                                  }
                              }
                              for (var k = 0; k < json.usage.length; k++) {
                                  version_user_associative_array[json.usage[k].version][json.usage[k].user] = parseInt(json.usage[k].count);
                              }

                              var data = [];
                              var ticks = [];
                              var series = [];

                              var key;
                              for (key in version_user_associative_array) {
                                  ticks[ticks.length] = key;
                              }
                              for (key in version_user_associative_array[ticks[0]]) {
                                  series[series.length] = {label:key};
                              }
                              for (var i = 0; i < ticks.length; i++) {
                                  if (i == 0) {
                                      data[i] = new Array();
                                  }
                                  for (var j = 0; j < series.length; j++) {
                                      data[i][j] = version_user_associative_array[ticks[i]][[series[j].label]];
                                  }
                              }
                              for (var p = 0; p < jsonLength; p++) {
                                  $('#versionUserTable').append($('<tr><td>' + json.usage[p].version + '</td><td>' + json.usage[p].user + '</td></tr><td>' + json.usage[p].count + '</td></tr>'));
                              }

                              if (jsonLength > 0) {
                                  $('#versionUserTable').show();
                                  var plot1 = $.jqplot('versionUserChart', data, {

                                      seriesDefaults:{
                                          renderer:$.jqplot.BarRenderer,
                                          rendererOptions: {fillToZero: true}
                                      },

                                      series:series,

                                      legend: {
                                          show: true,
                                          placement: 'outsideGrid'
                                      },
                                      axes: {
                                          xaxis: {
                                              renderer: $.jqplot.CategoryAxisRenderer,
                                              ticks: ticks
                                          },
                                          yaxis: {
                                              pad: 1.05,
                                              tickOptions: {formatString: '%d'}
                                          }
                                      }
                                  });

                              } else {
                                  $('#versionUserTable').hide();
                                  $('#versionUserChart').css("fontSize", 14);
                                  $('#versionUserChart').text('No Data Found ...');
                              }
                          } else {
                              jagg.message(result.message);
                          }
                      }, "json");

        }

        if (clickedTab == "users") {
            var name = $("#item-info h2")[0].innerHTML.split("-v")[0];
            jagg.post("/site/blocks/usage/ajax/usage.jag", { action:"getProviderAPIUserUsage", apiName:name, server:"https://localhost:9444/" },
                      function (json) {
                          if (!json.error) {
                              var length = json.usage.length,data = [];
                              $('#userChart').empty();
                              $('#userTable').find("tr:gt(0)").remove();
                              for (var i = 0; i < length; i++) {
                                  data[i] = [json.usage[i].user, parseInt(json.usage[i].count)];
                                  $('#userTable').append($('<tr><td>' + json.usage[i].user + '</td><td>' + json.usage[i].count + '</td></tr>'));

                              }

                              if (length > 0) {
                                  $('#userTable').show();
                                  var plot1 = jQuery.jqplot('userChart', [data],
                                                            {
                                                                seriesDefaults:{
                                                                    // Make this a pie chart.
                                                                    renderer:jQuery.jqplot.PieRenderer,
                                                                    rendererOptions:{
                                                                        // Put data labels on the pie slices.
                                                                        // By default, labels show the percentage of the slice.
                                                                        showDataLabels:true
                                                                    }
                                                                },
                                                                legend:{ show:true, location:'e' }
                                                            }
                                          );
                              } else {
                                  $('#userTable').hide();
                                  $('#userChart').css("fontSize", 14);
                                  $('#userChart').text('No Data Found ...');
                              }

                          } else {
                              jagg.message(result.message);
                          }
                      }, "json");



        }
    });
});

Object.size = function(obj) {
    var size = 0, key;
    for (key in obj) {
        if (obj.hasOwnProperty(key)) {
            size++;
        }
    }
    return size;
};


