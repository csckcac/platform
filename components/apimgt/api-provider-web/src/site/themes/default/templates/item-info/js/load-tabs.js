var t_on = {
            'versionChart':1,
            'versionUserChart':1,
            'userVersionChart':1,
            'userChart':1
            };

var getLastAccessTime = function(name) {
    var lastAccessTime=null;
    jagg.syncPost("/site/blocks/stats/ajax/stats.jag", { action:"getProviderAPIVersionUserLastAccess",server:"" },
              function (json) {
                  if (!json.error) {
                      var length = json.usage.length;
                      for (var i = 0; i < length; i++) {
                          if (json.usage[i].api_name == name) {
                              lastAccessTime = json.usage[i].lastAccess + " (Accessed version: " + json.usage[i].api_version + ")";
                              break;
                          }
                      }
                  } else {
                      jagg.message(json.message);
                  }
              });
    return lastAccessTime;
};

var getResponseTime = function(name) {
    var responseTime=null;
    jagg.syncPost("/site/blocks/stats/ajax/stats.jag", { action:"getProviderAPIServiceTime",server:"" },
              function (json) {
                  if (!json.error) {
                      var length = json.usage.length;
                      for (var i = 0; i < length; i++) {
                          if (json.usage[i].apiName == name) {
                              responseTime = json.usage[i].serviceTime + " ms";
                              break;
                          }
                      }
                  } else {
                      jagg.message(json.message);
                  }
              });
    return responseTime;
};


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

            jagg.fillProgress('versionChart');jagg.fillProgress('versionUserChart');
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
                                                                seriesColors: [ "#019d60", "#7ebe3a","#fdca19","#028a7a", "#cede3e", "#fa8216", "#e10718", "#aa1e52", "#e74d92", "#4e2e86"],
                                                                legend:{ show:true, location:'e' }
                                                            }
                                          );
                              } else {
                                  $('#versionTable').hide();
                                  $('#versionChart').css("fontSize", 14);
                                  $('#versionChart').append($('<span class="label label-info">No Data Found ...</span>'));
                              }

                          } else {
                              jagg.message(json.message);
                          }
                      }, "json");


            jagg.post("/site/blocks/usage/ajax/usage.jag", { action:"getSubscriberCountByAPIVersions", apiName:apiName },
                      function (json) {
                          if (!json.error) {
                              var length = json.usage.length,data = [];
                              $('#versionUserChart').empty();
                              $('#versionUserTable').find("tr:gt(0)").remove();
                              for (var i = 0; i < length; i++) {
                                  data[i] = [json.usage[i].version, parseInt(json.usage[i].count)];
                                  $('#versionUserTable').append($('<tr><td>' + json.usage[i].version + '</td><td>' + json.usage[i].count + '</td></tr>'));
                              }
                              if (length > 0) {
                                  $('#versionUserTable').show();
                                  var plot1 = jQuery.jqplot('versionUserChart', [data],
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
                                                                seriesColors: [ "#cede3e", "#aa1e52", "#e74d92", "#4e2e86","#028a7a", "#019d60", "#fdca19", "#e10718","#7ebe3a", "#fa8216"],
                                                                legend:{ show:true, location:'e' }
                                                            }
                                          );
                              } else {
                                  $('#versionUserTable').hide();
                                  $('#versionUserChart').css("fontSize", 14);
                                  $('#versionUserChart').append($('<span class="label label-info">No Data Found ...</span>'));
                              }

                          } else {
                              jagg.message(json.message);
                          }
                      }, "json");

        }

        if (clickedTab == "users") {
            jagg.fillProgress('userVersionChart');jagg.fillProgress('userChart');
            var name = $("#item-info h2")[0].innerHTML.split("-v")[0];
            var version = $("#item-info h2")[0].innerHTML.split("-v")[1];
            var provider = $("#item-info #spanProvider").text();
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
                                                                seriesColors: [ "#e10718", "#aa1e52","#fdca19", "#fa8216", "#e74d92", "#4e2e86","#7ebe3a", "#cede3e","#028a7a", "#019d60"],
                                                                legend:{ show:true, location:'e' }
                                                            }
                                          );
                              } else {
                                  $('#userTable').hide();
                                  $('#userChart').css("fontSize", 14);
                                  $('#userChart').append($('<span class="label label-info">No Data Found ...</span>'));
                              }

                          } else {
                              jagg.message(json.message);
                          }
                      }, "json");

            jagg.post("/site/blocks/usage/ajax/usage.jag", { action:"getProviderAPIVersionUserUsage", apiName:name,version:version, server:"https://localhost:9444/" },
                      function (json) {
                          if (!json.error) {
                              var length = json.usage.length,data = [];
                              $('#userVersionChart').empty();
                              $('#userVersionTable').find("tr:gt(0)").remove();
                              for (var i = 0; i < length; i++) {
                                  data[i] = [json.usage[i].user, parseInt(json.usage[i].count)];
                                  $('#userVersionTable').append($('<tr><td>' + json.usage[i].user + '</td><td>' + json.usage[i].count + '</td></tr>'));

                              }

                              if (length > 0) {
                                  $('#userVersionTable').show();
                                  var plot1 = jQuery.jqplot('userVersionChart', [data],
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
                                                                seriesColors: [ "#e74d92", "#cede3e","#fdca19", "#fa8216", "#e10718", "#4e2e86","#7ebe3a", "#aa1e52","#028a7a", "#019d60"],
                                                                legend:{ show:true, location:'e' }
                                                            }
                                          );
                              } else {
                                  $('#userVersionTable').hide();
                                  $('#userVersionChart').css("fontSize", 14);
                                  $('#userVersionChart').append($('<span class="label label-info">No Data Found ...</span>'));
                              }

                          } else {
                              jagg.message(json.message);
                          }
                      }, "json");

            var responseTime = getResponseTime(name);
            var lastAccessTime = getLastAccessTime(name);

            if (responseTime != null && lastAccessTime != null) {
                $("#usageSummary").show();
                $('#usageTable').append($('<tr><td>' + json.usage[i].user + '</td><td>' + json.usage[i].count + '</td></tr>'));
                $('#usageTable').append($('<tbody><tr><td class="span4">Response Time (Across all versions)</td><td>' +
                                          responseTime != null ? responseTime : "Data unavailable" + '</td></tr><tr>' +
                                                                                '<td class="span4">Last access time (Across all versions)</td><td>' +
                                                                                lastAccessTime != null ? lastAccessTime : "Data unavailable" + '</td></tr></tbody>'));
            }


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



