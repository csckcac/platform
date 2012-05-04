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
        apiProviderApp.call("action=getProviderAPIVersionUsage&providerName="+ apiProviderApp.currentProviderName +"&apiName="+ apiProviderApp.currentAPIName +"&server=https://localhost:9444/", function (json) {
            if (json.error == "true") {
                alert(json.message);
            }
            else {
                var data = new Array();
                $('#versionTable').find("tr:gt(0)").remove();
                for (var i = 0; i < json.data.usage.length; i++) {
                    data[i] = [json.data.usage[i].version, parseFloat(json.data.usage[i].count)];
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

            }
        });


    }

    if (clickedTab == "users") {
        apiProviderApp.call("action=getProviderAPIUserUsage&providerName="+ apiProviderApp.currentProviderName +"&apiName="+ apiProviderApp.currentAPIName +"&server=https://localhost:9444/", function (json) {
            if (json.error == "true") {
                alert(json.message);
            }
            else {
                var data = new Array();
                $('#userTable').find("tr:gt(0)").remove();
                for (var i = 0; i < json.data.usage.length; i++) {
                    data[i] = [json.data.usage[i].user, parseFloat(json.data.usage[i].count)];
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

            }
        });


    }
});

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

