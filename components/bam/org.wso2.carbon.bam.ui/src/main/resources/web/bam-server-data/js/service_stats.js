var pageMode_cookie = "pageMode1";
var startHr_cookie = "startHr1";
var endHr_cookie = "endHr1";
var startDay_cookie = "startDay1";
var endDay_cookie = "endDay1";
var startMonth_cookie = "startMonth1";
var endMonth_cookie = "endMonth1";
YAHOO.util.Event.onDOMReady(function() {
  init();
});
function updatePage() {
    sessionAwareFunction(function() {
        var random = Math.floor(Math.random() * 2000);
        /*if (!firstStart) {
            fixTimes();
        }
        firstStart = false;*/
        var startHrToSend,endHrToSend;
        if (pageMode == 'hour') {
            startHrToSend = startHr - oneHour/ 2;
            endHrToSend = endHr - oneHour/ 2;
        }else if (pageMode == 'day') {
            startHrToSend = startDay - oneHour/ 2;
            endHrToSend = endDay - oneHour/ 2;
        }else if (pageMode == 'month') {
            startHrToSend = startMonth - oneHour/ 2;
            endHrToSend = endMonth - oneHour/ 2;
        }
        var dataStr = "time_start=" + startHrToSend + "&time_end=" + endHrToSend+"&pageMode="+pageMode+"&random="+random+"&chartWidth="+chartWidthToSend;
        document.getElementById('chartData').innerHTML = '<div class="loadingPosition"><img align="top" src="images/ajax-loader.gif"/></div>';
        $.ajax({
            type:"GET",
            url:'../bam-server-data/get_service_stats_ajaxprocessor.jsp',
            data: dataStr,
            dataType: "html",
            success:
                    function(data, status)
                    {
                        $('#chartData').html(data);

                    }
        });

    },"Session time out");
}



