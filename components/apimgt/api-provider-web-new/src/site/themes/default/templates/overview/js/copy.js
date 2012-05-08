var copyAPIToNewVersion = function () {
    var apiName=$("td#api-name").text();
    var version=$("td#api-version").text() ;
    var newVersion = $("#copy-api #new-version").val();
    jagg.post("/site/blocks/overview/ajax/overview.jag", { action:"createNewAPI", apiName:apiName, version:version, newVersion:newVersion },
              function (result) {
                  if (!result.error) {
                      location.href = 'index.jag';
                  } else {
                      jagg.message(result.message);
                  }
              }, "json");

};