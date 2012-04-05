

function processData(taskNew ,selectedTask){

     var dataSr ="reportCategory="+taskNew+"&selectedTask="+selectedTask ;

    $.ajax({
           type:"GET",
           url:'../bam-server-data/analytics_count_process.jsp',
           data: dataSr,
           dataType: "html",
           success:
                   function(data, status)
                   {


                   }
       });

}