function loadTiers() {
    var target = document.getElementById("tier");
    jagg.post("/site/blocks/item-add/ajax/add.jag", { action:"getTiers" },
              function (result) {
                  if (!result.error) {
                      var arr = [];
                      for (var i = 0; i < result.tiers.length; i++) {
                          arr.push(result.tiers[i].tierName);
                      }
                      for (var j = 0; j < arr.length; j++) {
                          option = new Option(arr[j], arr[j]);
                          target.options[j] = option;
                          target.options[j].title = result.tiers[j].tierDescription;
                          if (j == 0) {
                              target.options[j].selected = 'selected';
                              $("#tiersHelp").html(result.tiers[0].tierDescription);
                              var tierArr = [];
                              tierArr.push(target.options[j].value);
                              $('<input>').attr('type', 'hidden')
                                      .attr('name', 'tiersCollection')
                                      .attr('id', 'tiersCollection')
                                      .attr('value', tierArr)
                                      .appendTo('#addAPIForm');
                          }
                      }
                  }
              }, "json");}



$(document).ready(function() {
    $("select[name='tier']").change(function() {
        // multipleValues will be an array
        var multipleValues = $(this).val() || [];
        var countLength = $('#tiersCollection').length;
        if (countLength == 0) {

            $('<input>').attr('type', 'hidden')
                    .attr('name', 'tiersCollection')
                    .attr('id', 'tiersCollection')
                    .attr('value', multipleValues)
                    .appendTo('#addAPIForm');
        } else {
            $('#tiersCollection').attr('value', multipleValues);

        }

    });
    var v = $("#addAPIForm").validate({
                                          submitHandler: function(form) {
                                              $('#saveMessage').show();
                                              $('#saveButtons').hide();
                                              $(form).ajaxSubmit({
                                                                     success:function(responseText,
                                                                                      statusText,
                                                                                      xhr, $form) {
                                                                         if (!responseText.error) {
                                                                             var current = window.location.pathname;
                                                                             if (current.indexOf(".jag") >= 0) {
                                                                                 location.href = "index.jag";
                                                                             } else {
                                                                                 location.href = 'site/pages/index.jag';
                                                                             }
                                                                         } else {
                                                                             jagg.message({content:responseText.message,type:"error"});
                                                                              $('#saveMessage').hide();
                                                                              $('#saveButtons').show();
                                                                         }
                                                                     }, dataType: 'json'
                                                                 });
                                          }
                                      });

});

function getContextValue() {
    var context = $('#context').val();
    var version = $('#version').val();

    if (context == "" && version != "") {
        $('#contextForUrl').html("/{context}/" + version);
        $('#contextForUrlDefault').html("/{context}/" + version);
    }if (context != "" && version == "") {
        if (context.charAt(0) != "/") {
            context = "/" + context;
        }
        $('#contextForUrl').html(context + "/{version}");
        $('#contextForUrlDefault').html(context + "/{version}");
    }if (context != "" && version != "") {
        if (context.charAt(0) != "/") {
            context = "/" + context;
        }
        $('#contextForUrl').html(context + "/" + version);
        $('#contextForUrlDefault').html(context + "/" + version);
    }
}