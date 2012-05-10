$(document).ready(function() {
    tinyMCE.init({
                     mode : "textareas",
                     theme : "advanced",
                     theme_advanced_buttons1 : "newdocument,|,bold,italic,underline,|,justifyleft,justifycenter,justifyright,fontselect,fontsizeselect,formatselect",
                     theme_advanced_buttons2 : "cut,copy,paste,|,bullist,numlist,|,outdent,indent,|,undo,redo,|,link,unlink,anchor,image,|,code,preview,|,forecolor,backcolor",
                     theme_advanced_buttons3 : "insertdate,inserttime,|,spellchecker,advhr,,removeformat,|,sub,sup,|,charmap,emotions",
                     theme_advanced_toolbar_location : "top",
                     theme_advanced_toolbar_align : "left",
                     theme_advanced_statusbar_location : "bottom",
                     theme_advanced_resizing : true

                 });
});


function loadDefaultTinyMCEContent(apiName, version, docName) {
    jagg.post("/site/blocks/documentation/ajax/docs.jag", { action:"getInlineContent", apiName:apiName,version:version,docName:docName password:pass },
              function (json) {
                  if (!json.error) {
                      var docName = json.doc[0].docName;
                      var apiName = json.doc[0].apiName;
                      var docContent = json.doc[0].docContent;
                      $('#apiDeatils').empty().html('<p><h1> ' + docName + '</h1></p>');
                      console.log(docContent);
                      var stringOut = decodeURI(docContent);
                      var xout = stringOut.replace(/%2F/g, "/");
                      // xout= xout.replace(/+/g, " ");
                      xout = xout.replace(/%3D/g, "=");
                      xout = xout.replace(/%23/g, "=");
                      console.log(xout);
                      tinyMCE.activeEditor.setContent(xout);
                  } else {
                      jagg.message(result.message);
                  }
              }, "json");


    apiProviderApp.call("action=getInlineContent&apiName=" + apiProviderApp.currentAPIName + "&version=" + apiProviderApp.currentVersion + "&docName=" + apiProviderApp.currentDocName, function (json) {

    });
}
;
