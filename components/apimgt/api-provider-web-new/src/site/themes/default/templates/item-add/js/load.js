$(function() {

    jagg.post("/site/blocks/tag/ajax/tag.jag", {
        action:"getAllTags"


    }, function (result) {
        if (!result.error) {
            var tags = new Array();
            var jsonTagCloud = result.data.tags;
            for (var i = 0; i < jsonTagCloud.length; i++) {
                tags.push(jsonTagCloud[i].name);
            }
            $('.typeahead').typeahead({
                                          source:tags
                                      });
        } else {

            jagg.message(result.message);
        }
    }, "json");


});
var addAPIForm = $("#addAPIForm");
addAPIForm.validation();

$('#addAPIForm').ajaxForm({
                              complete: function() {
                                  $("#addAPIForm")[0].reset();
                                  location.href = "index.jag";
                              }

                          });


