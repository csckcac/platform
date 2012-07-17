
var checkURLValid = function(url,btn) {
    if($(btn).next().get(0).nodeName == "SPAN"){
        $(btn).next().remove();
    }
    if (url == '') {
        $(btn).after(' <span class="label label-important"><i class="icon-remove icon-white"></i> Invalid</span>');
        var toFade = $(btn).next();
        var foo = setTimeout(function(){$(toFade).hide()},3000);
        return;
    }

    jagg.post("/site/blocks/item-add/ajax/add.jag", { action:"isURLValid", url:url },
              function (result) {
                  if (!result.error) {

                      if (result.response == "success") {
                          $(btn).after(' <span class="label label-success"><i class="icon-ok icon-white"></i> Valid</span>');

                      } else {
                          $(btn).after(' <span class="label label-important"><i class="icon-remove icon-white"></i> Invalid</span>');
                      }
                      var toFade = $(btn).next();
                      var foo = setTimeout(function(){$(toFade).hide()},3000);

                  }
              }, "json");
};

