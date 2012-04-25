var showTab = function (tabLink) {
    $('#' + tabLink).tab('show');
};
$(document).ready(function() {
    var data = [
                    ['v1.0.0', 12],
                    ['v1.1.0', 9],
                    ['v1.2.0', 14],
                    ['v1.3.0', 16],
                    ['v1.4.0', 7],
                    ['v1.3.0', 9]
                ];
    var plot1 = jQuery.jqplot('chart1', [data],
    {
        seriesDefaults: {
            // Make this a pie chart.
            renderer: jQuery.jqplot.PieRenderer,
            rendererOptions: {
                // Put data labels on the pie slices.
                // By default, labels show the percentage of the slice.
                showDataLabels: true
            }
        },
        legend: { show:true, location: 'e' }
    }
            );

});
$(function() {
    $('#registryPicker').tooltip('show');
    $('#myModal').modal();
    $('a[data-toggle="tab"]').on('shown', function (e) {
        var clickedTab = e.target.href.split('#')[1];
        if (clickedTab == "versions") {
            $(document).ready(function() {
                var data = [
                    ['v1.0.0', 12],
                    ['v1.1.0', 9],
                    ['v1.2.0', 14],
                    ['v1.3.0', 16],
                    ['v1.4.0', 7],
                    ['v1.3.0', 9]
                ];
                var plot1 = jQuery.jqplot('versionChart', [data],
                {
                    seriesDefaults: {
                        // Make this a pie chart.
                        renderer: jQuery.jqplot.PieRenderer,
                        rendererOptions: {
                            // Put data labels on the pie slices.
                            // By default, labels show the percentage of the slice.
                            showDataLabels: true
                        }
                    },
                    legend: { show:true, location: 'e' }
                }
                        );
            });
        }
    });
});
$(function() {
    $('#registryPicker').tooltip('show');
    $('#myModal').modal();
    $('a[data-toggle="tab"]').on('shown', function (e) {
        var clickedTab = e.target.href.split('#')[1];
        if (clickedTab == "users") {
            $(document).ready(function() {
                var data = [
                    ['John', 12],
                    ['Sara', 9],
                    ['Tom', 14],
                    ['Harry', 16],
                    ['Kevin', 7],
                    ['Anne', 9]
                ];
                var plot1 = jQuery.jqplot('userChart', [data],
                {
                    seriesDefaults: {
                        // Make this a pie chart.
                        renderer: jQuery.jqplot.PieRenderer,
                        rendererOptions: {
                            // Put data labels on the pie slices.
                            // By default, labels show the percentage of the slice.
                            showDataLabels: true
                        }
                    },
                    legend: { show:true, location: 'e' }
                }
                        );
            });
        }
    });
});
