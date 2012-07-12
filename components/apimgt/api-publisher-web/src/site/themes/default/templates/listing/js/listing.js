var removeAPI = function(name, version, provider) {
    jagg.message({
        content:"Are you sure you want to delete the API - " + name + " - " + version ,
        type:"confirm",
        title:"Confirm Delete",
        anotherDialog:true,
        okCallback:function(){

            jagg.post("/site/blocks/item-add/ajax/remove.jag", { action:"removeAPI",name:name, version:version,provider:provider },
              function (result) {
                  if (result.message == "timeout") {
                      jagg.showLogin();
                  }
                  else if (!result.error) {
                      window.location.reload();
                  }
              }, "json");

    }});

};
$(document).ready(
         function() {
             if (($.cookie("selectedTab") != null)) {
                 $.cookie("selectedTab", null);
             }

         }
);

