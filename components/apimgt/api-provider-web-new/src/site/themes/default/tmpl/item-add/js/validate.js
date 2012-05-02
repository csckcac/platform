$(function() {
    $(function() {
        apiProviderApp.callStore("action=getAllTags", function (json) {
            if (json.error == "true") {
                alert(json.message);
            }
            else {
                var tags = new Array();
                var jsonTagCloud = json.data.tagCloud;
                for (var i = 0; i < jsonTagCloud.length; i++) {
                    tags.push(jsonTagCloud[i].name);
                }
                $('.typeahead').typeahead({
                                              source:tags
                                          });
            }
        });
        var addAPIForm = $("#addAPIForm");
        addAPIForm.validation();
        /*$("#addNewAPIButton").click(function() {

         if(addAPIForm.validate()) {
         saveAPI();
         }
         });*/

        $('#addAPIForm').ajaxForm({
                                      complete: function() {
                                          $("#addAPIForm")[0].reset();
                                          location.href = "index.jag";
                                      }
                                  });
    });
});

