var t_on = {
            'apiChart':1,
            'subsChart':1,
            'serviceTimeChart':1,
            'tempLoadingSpace':1
            };
function fillProgress(chartId)
    {
        if(t_on[chartId]){
            var progressBar = $('#'+chartId+' div.progress-striped div.bar')[0];

            var time = Math.floor((Math.random() * 400) + 800);
            var divider = Math.floor((Math.random() * 2) + 2);
            var currentWidth = parseInt(progressBar.style.width.split('%')[0]);
            var newWidth = currentWidth + parseInt((100 - currentWidth) / divider);
            newWidth += "%";
            $(progressBar).css('width', newWidth);
            var t = setTimeout('fillProgress("'+chartId+'")', time);
        }
    }
$(document).ready(function() {

    //Initiating the fake progress bar
    fillProgress('apiChart');fillProgress('subsChart');fillProgress('serviceTimeChart');fillProgress('tempLoadingSpace');

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
                              seriesColors: [ "#fdca19", "#fa8216", "#e10718", "#aa1e52", "#e74d92", "#4e2e86",
                                                                        "#028a7a", "#019d60", "#7ebe3a", "#cede3e"],
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
                      jagg.message(json.message);
                  }
                  t_on['serviceTimeChart'] = 0;
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
                                                        seriesColors: [ "#fdca19", "#fa8216", "#e10718", "#aa1e52", "#e74d92", "#4e2e86",
                                                                        "#028a7a", "#019d60", "#7ebe3a", "#cede3e"],
                                                        legend:{ show:true, location:'e' }
                                                    }
                                  );

                      } else {
                          $('#subsTable').hide();
                          $('#subsChart').css("fontSize", 14);
                          $('#subsChart').append($('<span class="label label-info">No data found. Check BAM server connectivity...</span>'));
                      }


                  } else {
                      jagg.message(json.message);
                  }
                  t_on['subsChart'] = 0;
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
                                                        seriesColors: [ "#fdca19", "#fa8216", "#e10718", "#aa1e52", "#e74d92", "#4e2e86",
                                                                        "#028a7a", "#019d60", "#7ebe3a", "#cede3e"],
                                                        legend:{ show:true, location:'e' }
                                                    }
                                  );

                      } else {
                          $('#apiTable').hide();
                          $('#apiChart').css("fontSize", 14);
                          $('#apiChart').append($('<span class="label label-info">No data found. Check BAM server connectivity...</span>'));
                      }


                  } else {
                      jagg.message(json.message);
                  }
                  t_on['apiChart'] = 0;
              }, "json");


    jagg.post("/site/blocks/stats/ajax/stats.jag", { action:"getProviderAPIVersionUserLastAccess",server:"https://localhost:9444/" },
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
                      jagg.message(json.message);
                  }
                  t_on['tempLoadingSpace'] = 0;
              }, "json");


});
