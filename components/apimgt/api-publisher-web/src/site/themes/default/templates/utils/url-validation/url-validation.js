var checkURLValid = function(url) {
    if (url == '') {
        jagg.message({content:"Enter a value for URI to check the connectivity.",type:"info"});
        return;
    }
    jagg.post("/site/blocks/item-add/ajax/add.jag", { action:"isURLValid", url:url },
              function (result) {
                  if (!result.error) {
                      if (result.response == "success") {
                          jagg.message({content:"Successfully connected to the URI.",type:"info"});

                      } else {
                          jagg.message({content:"Unable to establish a connection to the URI.",type:"info"});
                      }

                  }
              }, "json");
};