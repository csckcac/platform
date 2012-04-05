function getConfigRT(){
    return {
        // Turns on animatino for all series in this plot.
        animate: true,
        // Will animate plot on calls to plot1.replot({resetAxes:true})
//        animateReplot: true,
        legend: {
            show: true,
			placement:'outside',
			location:'n'
        },
        series:[
            {
                label:'Response Time',
                color:'#ffb000'
			}
        ],
        highlighter: {
            show: true,
            showLabel: true,
            tooltipAxes: 'y',
            sizeAdjust: 7.5 , tooltipLocation : 'ne'
        }
    };
}

function getConfig(){
    return {
        // Turns on animatino for all series in this plot.
        //animate: true,
        // Will animate plot on calls to plot1.replot({resetAxes:true})
//        animateReplot: true,
        legend: {
            show: true,
			placement:'outside',
			location:'n'
        },
        series:[
            {
                label:'Request Count',
                color:'#168900'
			},
            {
                label:'Response Count',
                color:'#39627d'
			},
            {
                label:'Fault Count',
                color:'#ff1e00'
            }
        ],
        highlighter: {
            show: true,
            showLabel: true,
            tooltipAxes: 'y',
            sizeAdjust: 7.5 , tooltipLocation : 'ne'
        }
    };
}

