$(document).ready(function() {

    jagg.post("/site/blocks/stats/ajax/stats.jag", { action:"getProviderAPIServiceTime",server:"https://localhost:9444/" },
              function (json) {
                  if (!json.error) {
                      var length = json.usage.length,s1 = [];
                      var ticks = [];
                      $('#serviceTimeChart').empty();
                      $('#serviceTimeTable').find("tr:gt(0)").remove();
                      for (var i = 0; i < length; i++) {
                          s1[i] = parseFloat(json.usage[i].serviceTime);
                          ticks[i] = json.usage[i].apiName;
                          $('#serviceTimeTable').append($('<tr><td>' + json.usage[i].apiName + '</td><td>' + json.usage[i].serviceTime + '</td></tr>'));

                      }
                      if (length > 0) {
                          $('#serviceTimeTable').show();

                          var plot1 = $.jqplot('serviceTimeChart', [s1], {
                              seriesDefaults:{
                                  renderer:$.jqplot.BarRenderer,
                                  rendererOptions: {fillToZero: true}
                              },
                              series:[
                                  {label:'API'}
                              ],
                              axes: {
                                  xaxis: {
                                      renderer: $.jqplot.CategoryAxisRenderer,
                                      ticks: ticks
                                  },
                                  yaxis: {
                                      pad: 1.05,
                                      tickOptions: {formatString: '%dms'}
                                  }
                              }
                          });

                      } else {
                          $('#serviceTimeTable').hide();
                          $('#serviceTimeChart').css("fontSize", 14);
                          $('#serviceTimeChart').text('No Data Found ...');
                      }


                  } else {
                      jagg.message(json.message);
                  }
              }, "json");


    jagg.post("/site/blocks/stats/ajax/stats.jag", { action:"getSubscriberCountByAPIs" },
              function (json) {
                  if (!json.error) {
                      var length = json.usage.length,data = [];
                      $('#subsChart').empty();
                      $('#subsTable').find("tr:gt(0)").remove();
                      for (var i = 0; i < length; i++) {
                          data[i] = parseFloat(json.usage[i].count);
                          data[i] = [json.usage[i].name, parseInt(json.usage[i].count)];
                          $('#subsTable').append($('<tr><td>' + json.usage[i].name + '</td><td>' + json.usage[i].count + '</td></tr>'));

                      }
                      if (length > 0) {
                          $('#subsTable').show();
                          var plot1 = jQuery.jqplot('subsChart', [data],
                                                    {
                                                        seriesDefaults:{
                                                            renderer:jQuery.jqplot.PieRenderer,
                                                            rendererOptions:{
                                                                showDataLabels:true
                                                            }
                                                        },
                                                        legend:{ show:true, location:'e' }
                                                    }
                                  );

                      } else {
                          $('#subsTable').hide();
                          $('#subsChart').css("fontSize", 14);
                          $('#subsChart').text('No Data Found ...');
                      }


                  } else {
                      jagg.message(json.message);
                  }
              }, "json");


    jagg.post("/site/blocks/stats/ajax/stats.jag", { action:"getProviderAPIUsage",server:"https://localhost:9444/" },
              function (json) {
                  if (!json.error) {
                      var length = json.usage.length,data = [];
                      $('#apiChart').empty();
                      $('#apiTable').find("tr:gt(0)").remove();
                      for (var i = 0; i < length; i++) {
                          data[i] = [json.usage[i].apiName, parseInt(json.usage[i].count)];
                          $('#apiTable').append($('<tr><td>' + json.usage[i].apiName + '</td><td>' + json.usage[i].count + '</td></tr>'));

                      }
                      if (length > 0) {
                          $('#apiTable').show();
                          var plot1 = jQuery.jqplot('apiChart', [data],
                                                    {
                                                        seriesDefaults:{
                                                            renderer:jQuery.jqplot.PieRenderer,
                                                            rendererOptions:{
                                                                showDataLabels:true
                                                            }
                                                        },
                                                        legend:{ show:true, location:'e' }
                                                    }
                                  );

                      } else {
                          $('#apiTable').hide();
                          $('#apiChart').css("fontSize", 14);
                          $('#apiChart').text('No Data Found ...');
                      }


                  } else {
                      jagg.message(json.message);
                  }
              }, "json");


    jagg.post("/site/blocks/stats/ajax/stats.jag", { action:"getProviderAPIVersionUserLastAccess",server:"https://localhost:9444/" },
              function (json) {
                  if (!json.error) {
                      $('#lastAccessTable').find("tr:gt(0)").remove();
                      var length = json.usage.length;
                      for (var i = 0; i < json.usage.length; i++) {
                          $('#lastAccessTable').append($('<tr><td>' + json.usage[i].api_name + '</td><td>' + json.usage[i].api_version + '</td><td>' + json.usage[i].user + '</td><td>' + json.usage[i].lastAccess + '</td></tr>'));
                      }

                      if (length == 0) {
                          $('#lastAccessTable').css("fontSize", 14);
                          $('#lastAccessTable').text('No Data Found ...');
                      }

                  } else {
                      jagg.message(json.message);
                  }
              }, "json");


});
