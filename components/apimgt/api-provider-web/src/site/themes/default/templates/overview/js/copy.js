var copyAPIToNewVersion = function (provider) {
    var api=$("#item-info h2")[0].innerHTML;
    var apiName = $.trim(api.split("-")[0]);
    var version = $.trim(api.split("-")[1]);
    var newVersion = $("#copy-api #new-version").val();
    jagg.post("/site/blocks/overview/ajax/overview.jag", { action:"createNewAPI", provider:provider,apiName:apiName, version:version, newVersion:newVersion },
              function (result) {
                  if (!result.error) {
                      $("#copy-api #new-version").val('');
                      var current = window.location.pathname;
                      if (current.indexOf(".jag") >= 0) {
                          location.href = "index.jag";
                      } else {
                          location.href = 'site/pages/index.jag';
                      }

                  } else {
                      jagg.message({content:result.message,type:"error"});
                  }
              }, "json");

};
$(document).ready(
                 function() {
                     $('#copyApiForm').validate({
                         submitHandler: function(form) {
                             copyAPIToNewVersion(provider)
                         }
                     });
                 }
        );