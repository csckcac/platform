$('a[data-toggle="tab"]').on('shown', function (e) {
    var clickedTab = e.target.href.split('#')[1];
    ////////////// edit tab
    if (clickedTab == "edit") {
        var addRow = function () {
            if ($(this).parent().parent().parent().parent()[0].rows.length == 2) {
                $('#resourceTableError').show('fast');
            } else {
                $('#resourceTableError').hide('fast');
                $(this).parent().parent().remove();
            }
        };
/*        $(document).ready(function () {
                $('#resourceAddBtn').click(
                    function () {
                        var rowClone = $('#resourceRow').clone();
                        $('#resourceTable').append(rowClone);
                        $('.btn-danger', rowClone).click(addRow);
                    }
                );
            }
        );*/

//        $('#resourceTable .btn-danger').click(addRow);
    }

    ////////////// edit tab
    if (clickedTab == "versions") {
        apiProviderApp.call("action=getProviderAPIVersionUsage&providerName="+ apiProviderApp.loggedUser +"&apiName="+ apiProviderApp.currentAPIName +"&server=https://localhost:9444/", function (json) {
            if (json.error == "true") {
                alert(json.message);
            }
            else {
                var data = new Array();
                $('#versionChart').empty();
                $('#versionTable').find("tr:gt(0)").remove();
                for (var i = 0; i < json.data.usage.length; i++) {
                    data[i] = [json.data.usage[i].version, parseInt(json.data.usage[i].count)];
                    $('#versionTable').append($('<tr><td>' + json.data.usage[i].version + '</td><td>' + json.data.usage[i].count + '</td></tr>'));

                }
                // jQuery Plot chart data format
                /*[
                 ['v1.0.0', 12],
                 ['v1.1.0', 9],
                 ['v1.2.0', 14],
                 ['v1.3.0', 16],
                 ['v1.4.0', 7],
                 ['v1.3.0', 9]
                 ];*/
                if(json.data.usage.length > 0){
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
                }else{
                    $('#versionTable').hide();
                    $('#versionChart').css("fontSize",14);
                    $('#versionChart').text('No Data Found ...');
                }

            }
        });

        apiProviderApp.call("action=getProviderAPIVersionUserUsage&providerName="+ apiProviderApp.loggedUser +"&apiName="+ apiProviderApp.currentAPIName +"&server=https://localhost:9444/", function (json) {
            if (json.error == "true") {
                alert(json.message);
            }
            else {

                $('#versionUserChart').empty();
                $('#versionUserTable').find("tr:gt(0)").remove();
                var version_user_associative_array = new Array();
                for (var i = 0; i < json.data.usage.length; i++) {
                    if(!version_user_associative_array.hasOwnProperty(json.data.usage[i].version)){
                        version_user_associative_array[json.data.usage[i].version] = new Array();
                    }
                    for (var j = 0; j < json.data.usage.length; j++) {
                        version_user_associative_array[json.data.usage[i].version][json.data.usage[j].user] = 0;
                    }
                }
                for (var i = 0; i < json.data.usage.length; i++) {
                    version_user_associative_array[json.data.usage[i].version][json.data.usage[i].user] = parseInt(json.data.usage[i].count);
                }

                var data = new Array();
                var ticks = new Array();
                var series = new Array();

                var key;
                for(key in version_user_associative_array){
                    ticks[ticks.length] = key;
                }
                for(key in version_user_associative_array[ticks[0]]){
                    series[series.length] = {label:key};
                }
                for(var i = 0; i<ticks.length; i++){
                    if(i==0){
                        data[i] = new Array();
                    }
                    for(var j = 0; j<series.length; j++){
                        data[i][j] = version_user_associative_array[ticks[i]][[series[j].label]];
                    }
                }
                for (var i = 0; i < json.data.usage.length; i++) {
                    $('#versionUserTable').append($('<tr><td>' + json.data.usage[i].version + '</td><td>' + json.data.usage[i].user + '</td></tr><td>' + json.data.usage[i].count + '</td></tr>'));
                }

                if(json.data.usage.length > 0){
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

                }else{
                    $('#versionUserTable').hide();
                    $('#versionUserChart').css("fontSize",14);
                    $('#versionUserChart').text('No Data Found ...');
                }
            }
        });
    }

    if (clickedTab == "users") {

        apiProviderApp.call("action=getProviderAPIUserUsage&providerName="+ apiProviderApp.loggedUser +"&apiName="+ apiProviderApp.currentAPIName +"&server=https://localhost:9444/", function (json) {
            if (json.error == "true") {
                alert(json.message);
            }
            else {
                var data = new Array();
                $('#userChart').empty();
                $('#userTable').find("tr:gt(0)").remove();
                for (var i = 0; i < json.data.usage.length; i++) {
                    data[i] = [json.data.usage[i].user, parseInt(json.data.usage[i].count)];
                    $('#userTable').append($('<tr><td>' + json.data.usage[i].user + '</td><td>' + json.data.usage[i].count + '</td></tr>'));

                }
                // jQuery Plot chart data format
                /*[
                 ['v1.0.0', 12],
                 ['v1.1.0', 9],
                 ['v1.2.0', 14],
                 ['v1.3.0', 16],
                 ['v1.4.0', 7],
                 ['v1.3.0', 9]
                 ];*/
                if(json.data.usage.length > 0){
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
                }else{
                    $('#userTable').hide();
                    $('#userChart').css("fontSize",14);
                    $('#userChart').text('No Data Found ...');
                }

            }
        });


        apiProviderApp.call("action=getProviderAPIVersionUserUsage&providerName="+ apiProviderApp.loggedUser +"&apiName="+ apiProviderApp.currentAPIName +"&server=https://localhost:9444/", function (json) {
            if (json.error == "true") {
                alert(json.message);
            }
            else {

                $('#userVersionChart').empty();
                $('#userVersionTable').find("tr:gt(0)").remove();
                var user_version_associative_array = new Array();
                for (var i = 0; i < json.data.usage.length; i++) {
                    if(!user_version_associative_array.hasOwnProperty(json.data.usage[i].user)){
                        user_version_associative_array[json.data.usage[i].user] = new Array();
                    }
                    for (var j = 0; j < json.data.usage.length; j++) {
                        user_version_associative_array[json.data.usage[i].user][json.data.usage[j].version] = 0;
                    }
                }
                for (var i = 0; i < json.data.usage.length; i++) {
                    user_version_associative_array[json.data.usage[i].user][json.data.usage[i].version] = parseInt(json.data.usage[i].count);
                }

                var data = new Array();
                var ticks = new Array();
                var series = new Array();

                var key;
                for(key in user_version_associative_array){
                    ticks[ticks.length] = key;
                }
                for(key in user_version_associative_array[ticks[0]]){
                    series[series.length] = {label:key};
                }
                for(var i = 0; i<ticks.length; i++){
                    if(i==0){
                        data[i] = new Array();
                    }
                    for(var j = 0; j<series.length; j++){
                        data[i][j] = user_version_associative_array[ticks[i]][[series[j].label]];
                    }
                }
                for (var i = 0; i < json.data.usage.length; i++) {
                    $('#userVersionTable').append($('<tr><td>' + json.data.usage[i].user + '</td><td>' + json.data.usage[i].version + '</td></tr><td>' + json.data.usage[i].count + '</td></tr>'));
                }

                if(json.data.usage.length > 0){
                    $('#userVersionTable').show();
                    var plot1 = $.jqplot('userVersionChart', data, {

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

                }else{
                    $('#versionUserTable').hide();
                    $('#versionUserChart').css("fontSize",14);
                    $('#versionUserChart').text('No Data Found ...');
                }
            }
        });

    }
});

Object.size = function(obj) {
    var size = 0, key;
    for (key in obj) {
        if (obj.hasOwnProperty(key)) size++;
    }
    return size;
};

var resourcesCount = 1;
var rowNums = new Array();

var addResourcesToApi = function () {
    $('#resourceTableError').hide('fast');
    $('#resourceRow').clone(true).attr('id', 'item-' + resourcesCount).insertAfter($('#resourceRow'));
    $('#item-' + resourcesCount + ' #resourceMethod').val($('#resourceMethod').val());
    $('#item-' + resourcesCount + ' #uriTemplate').attr('disabled', 'disabled');
    $('#item-' + resourcesCount + ' #resourceMethod').attr('disabled', 'disabled');
    $('#item-' + resourcesCount + ' td#buttons a#resourceAddBtn').remove();
    if($('#item-' + resourcesCount + ' td#buttons a#resourceDelBtn').length>0){
    $('#item-' + resourcesCount + ' td#buttons a#resourceDelBtn').remove();
    }
    $('#item-' + resourcesCount + ' td#buttons').append("<a class=\"btn btn-danger\" onclick=\"deleteResource(" + resourcesCount + ")\"><i class=\"icon-trash icon-white\"></i> Delete</a>");


    $('<input>').attr('type', 'hidden')
        .attr('name', 'resourceMethod-' + resourcesCount).attr('value', $('#resourceMethod').val())
        .appendTo('#addAPIForm');

    $('<input>').attr('type', 'hidden')
        .attr('name', 'uriTemplate-' + resourcesCount).attr('value', $('#uriTemplate').val())
        .appendTo('#addAPIForm');
    rowNums.push(resourcesCount);
    resourcesCount++;

    if ($('#resourceCount').length == 0) {
        $('<input>').attr('type', 'hidden')
            .attr('name', 'resourceCount')
            .attr('id', 'resourceCount')
            .attr('value', rowNums)
            .appendTo('#addAPIForm');
    } else {
        $('#resourceCount').attr('value', rowNums);
    }

    $('#uriTemplate').val('');
    $('#resourceMethod').val('GET');

};

var updateResourcesToApi = function () {
    $('#resourceTableError').hide('fast');
    $('#resourceRow').clone(true).attr('id', 'item-' + resourcesCount).insertAfter($('#resourceRow'));
    $('#item-' + resourcesCount + ' #resourceMethod').val($('#resourceMethod').val());
    $('#item-' + resourcesCount + ' #uriTemplate').attr('disabled', 'disabled');
    $('#item-' + resourcesCount + ' #resourceMethod').attr('disabled', 'disabled');
    $('#item-' + resourcesCount + ' td#buttons a#resourceAddBtn').remove();
    $('#item-' + resourcesCount + ' td#buttons').append("<a class=\"btn btn-danger\" onclick=\"deleteResource(" + resourcesCount + ")\"><i class=\"icon-trash icon-white\"></i> Delete</a>");

    $('<input>').attr('type', 'hidden')
        .attr('name', 'resourceMethod-' + resourcesCount).attr('value', $('#resourceMethod').val())
        .appendTo('#editAPIForm');

    $('<input>').attr('type', 'hidden')
        .attr('name', 'uriTemplate-' + resourcesCount).attr('value', $('#uriTemplate').val())
        .appendTo('#editAPIForm');
    rowNums.push(resourcesCount);
    resourcesCount++;

    if ($('#resourceCount').length == 0) {
        $('<input>').attr('type', 'hidden')
            .attr('name', 'resourceCount')
            .attr('id', 'resourceCount')
            .attr('value', rowNums)
            .appendTo('#editAPIForm');
    } else {
        $('#resourceCount').attr('value', rowNums);
    }

    $('#uriTemplate').val('');
    $('#resourceMethod').val('GET');

};

