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
                                                                legend:{ show:true, location:'e' }
                                                            }
                                          );
                              } else {
                                  $('#versionUserTable').hide();
                                  $('#versionUserChart').css("fontSize", 14);
                                  $('#versionUserChart').text('No Data Found ...');
                              }

                          } else {
                              jagg.message(json.message);
                          }
                      }, "json");

        }

        if (clickedTab == "users") {
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
                                                                legend:{ show:true, location:'e' }
                                                            }
                                          );
                              } else {
                                  $('#userTable').hide();
                                  $('#userChart').css("fontSize", 14);
                                  $('#userChart').text('No Data Found ...');
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
                                                                legend:{ show:true, location:'e' }
                                                            }
                                          );
                              } else {
                                  $('#userVersionTable').hide();
                                  $('#userVersionChart').css("fontSize", 14);
                                  $('#userVersionChart').text('No Data Found ...');
                              }

                          } else {
                              jagg.message(json.message);
                          }
                      }, "json");

            var responseTime = null;
            var lastAccessTime = null;
            jagg.post("/site/blocks/stats/ajax/stats.jag", { action:"getProviderAPIVersionUserLastAccess",server:"" },
                      function (json) {
                          if (!json.error) {
                              length = json.usage.length;
                              for (var i = 0; i < length; i++) {
                                  if (json.usage[i].api_name == api.name) {
                                      lastAccessTime = json.usage[i].lastAccess + " (Accessed version: " + json.usage[i].api_version + ")";
                                      break;
                                  }
                              }


                          } else {
                              jagg.message(json.message);
                          }
                      }, "json");
            jagg.post("/site/blocks/stats/ajax/stats.jag", { action:"getProviderAPIServiceTime",server:"" },
                      function (json) {
                          if (!json.error) {
                              var length = json.usage.length;
                              for (var i = 0; i < length; i++) {
                                  if (json.usage[i].apiName == api.name) {
                                      responseTime = json.usage[i].serviceTime + " ms";
                                      break;
                                  }
                              }


                          } else {
                              jagg.message(json.message);
                          }
                      }, "json");
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


