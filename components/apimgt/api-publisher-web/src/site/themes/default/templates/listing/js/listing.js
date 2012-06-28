var removeAPI = function(name, version, provider) {
    jagg.message({
        content:"Are you sure you want to delete the API - " + name + " - " + version ,
        type:"confirm",
        title:"Confirm Delete",
        okCallback:function(){

            jagg.post("/site/blocks/item-add/ajax/remove.jag", { action:"removeAPI",name:name, version:version,provider:provider },
              function (result) {
                  if (!result.error) {
                      window.location.reload();
                  }
              }, "json");

    }});

};

