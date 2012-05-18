$(document).ready(function() {
    if (($.cookie("tab") != null)) {
        var tabLink = $.cookie("tab");
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
                              var user_version_associative_array = new Array();
                              for (var i = 0; i < json.usage.length; i++) {
                                  if (!user_version_associative_array.hasOwnProperty(json.usage[i].user)) {
                                      user_version_associative_array[json.usage[i].user] = new Array();
                                  }
                                  for (var j = 0; j < json.usage.length; j++) {
                                      user_version_associative_array[json.usage[i].user][json.usage[j].version] = 0;
                                  }
                              }
                              for (var i = 0; i < json.usage.length; i++) {
                                  user_version_associative_array[json.usage[i].user][json.usage[i].version] = parseInt(json.usage[i].count);
                              }

                              var data = new Array();
                              var ticks = new Array();
                              var series = new Array();

                              var key;
                              for (key in user_version_associative_array) {
                                  ticks[ticks.length] = key;
                              }
                              for (key in user_version_associative_array[ticks[0]]) {
                                  series[series.length] = {label:key};
                              }
                              for (var i = 0; i < ticks.length; i++) {
                                  if (i == 0) {
                                      data[i] = new Array();
                                  }
                                  for (var j = 0; j < series.length; j++) {
                                      data[i][j] = user_version_associative_array[ticks[i]][[series[j].label]];
                                  }
                              }
                              for (var i = 0; i < json.usage.length; i++) {
                                  $('#versionUserTable').append($('<tr><td>' + json.usage[i].version + '</td><td>' + json.usage[i].user + '</td><td>' + json.usage[i].count + '</td></tr>'));
                              }

                              if (json.usage.length > 0) {
                                  $('#versionUserTable').show();
                                  var plot1 = $.jqplot('versionUserChart', data, {

                                      stackSeries: true,
                                      captureRightClick: true,
                                      seriesDefaults:{
                                          renderer:$.jqplot.BarRenderer,
                                          rendererOptions: {
                                              barMargin: 30,
                                              highlightMouseDown: true
                                          },
                                          pointLabels: {show: true}
                                      },

                                      series:series,

                                      legend: {
                                          show: true,
                                          location: 'e',
                                          placement: 'outside'
                                      },
                                      axes: {
                                          xaxis: {
                                              renderer: $.jqplot.CategoryAxisRenderer,
                                              ticks: ticks
                                          },
                                          yaxis: {
                                              padMin: 0,
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


