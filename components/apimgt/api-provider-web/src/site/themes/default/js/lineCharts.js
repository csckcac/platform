var plot1=null;
var showChart = function(){

      var line1 = [['10/10', Math.floor ( Math.random() *100 )], ['10/11', Math.floor ( Math.random() *100 )], ['10/120', Math.floor ( Math.random() *100 )],
      ['10/13', Math.floor ( Math.random() *100 )], ['10/14', Math.floor ( Math.random() *100 )]
      ];
      if(plot1 != null){
          plot1.destroy();
      }
      plot1 = $.jqplot('userStats', [line1], {
        axesDefaults: {
            tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
            tickOptions: {
              angle: -30,
              fontSize: '10pt'
            }
        },
        axes: {
          xaxis: {
            renderer: $.jqplot.CategoryAxisRenderer,
            label:'Time'
          },
          yaxis: {
            label:'Hits'
          }
        },
        animate: true
      });

}