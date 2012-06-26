var t_on = {
            'apiChart':1,
            'subsChart':1,
            'serviceTimeChart':1,
            'tempLoadingSpace':1
            };
$(document).ready(function() {

    //Initiating the fake progress bar
    jagg.fillProgress('apiChart');jagg.fillProgress('subsChart');jagg.fillProgress('serviceTimeChart');jagg.fillProgress('tempLoadingSpace');
    var currentLocation=window.location.pathname;
    jagg.post("/site/blocks/stats/ajax/stats.jag", { action:"getProviderAPIServiceTime",currentLocation:currentLocation },
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
                                  rendererOptions: {fillToZero: true} ,
                                  tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
                                  tickOptions: {
                                      angle: -30,
                                      fontSize: '10pt'
                                  }
                              },
                              seriesColors: [ "#ed3c3c", "#ffe03e", "#48ca48", "#49baff","#7d7dff", "#ff468b", "#de621d", "#cb68c9"],
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
                          $('#serviceTimeChart').append($('<span class="label label-info">No data found. Check BAM server connectivity...</span>'));
                      }


                  } else {
                      jagg.message({content:json.message,type:"error"});
                  }
                  t_on['serviceTimeChart'] = 0;
              }, "json");


    jagg.post("/site/blocks/stats/ajax/stats.jag", { action:"getSubscriberCountByAPIs",currentLocation:currentLocation  },
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
                                                        seriesColors: [ "#ed3c3c", "#ffe03e", "#48ca48", "#49baff","#7d7dff", "#ff468b", "#de621d", "#cb68c9"],
                                                        legend:{ show:true, location:'e' }
                                                    }
                                  );

                      } else {
                          $('#subsTable').hide();
                          $('#subsChart').css("fontSize", 14);
                          $('#subsChart').append($('<span class="label label-info">No data found. Check BAM server connectivity...</span>'));
                      }


                  } else {
                      jagg.message({content:json.message,type:"error"});
                  }
                  t_on['subsChart'] = 0;
              }, "json");


    jagg.post("/site/blocks/stats/ajax/stats.jag", { action:"getProviderAPIUsage",currentLocation:currentLocation  },
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
                                                        seriesColors: [ "#ed3c3c", "#ffe03e", "#48ca48", "#49baff","#7d7dff", "#ff468b", "#de621d", "#cb68c9"],
                                                        legend:{ show:true, location:'e' }
                                                    }
                                  );

                      } else {
                          $('#apiTable').hide();
                          $('#apiChart').css("fontSize", 14);
                          $('#apiChart').append($('<span class="label label-info">No data found. Check BAM server connectivity...</span>'));
                      }


                  } else {
                      jagg.message({content:json.message,type:"error"});
                  }
                  t_on['apiChart'] = 0;
              }, "json");


    jagg.post("/site/blocks/stats/ajax/stats.jag", { action:"getProviderAPIVersionUserLastAccess",currentLocation:currentLocation  },
              function (json) {
                  if (!json.error) {
                      $('#lastAccessTable').find("tr:gt(0)").remove();
                      var length = json.usage.length;
                      $('#lastAccessTable').show();
                      for (var i = 0; i < json.usage.length; i++) {
                          $('#lastAccessTable').append($('<tr><td>' + json.usage[i].api_name + '</td><td>' + json.usage[i].api_version + '</td><td>' + json.usage[i].user + '</td><td>' + json.usage[i].lastAccess + '</td></tr>'));
                      }
                      if (length == 0) {
                          $('#lastAccessTable').hide();
                          $('#tempLoadingSpace').html('');
                          $('#tempLoadingSpace').append($('<span class="label label-info">No data found. Check BAM server connectivity...</span>'));

                      }else{
                          $('#tempLoadingSpace').hide();
                      }

                  } else {
                      jagg.message({content:json.message,type:"error"});
                  }
                  t_on['tempLoadingSpace'] = 0;
              }, "json");


});
