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
                          if (j == 0) {
                              target.options[j].selected = 'selected';
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
              }, "json");
}

function setTiers() {
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
}


$(document).ready(function() {
    var v = $("#addAPIForm").validate({
                                          submitHandler: function(form) {
                                              $(form).ajaxSubmit({
                                                                     success:function(responseText,
                                                                                      statusText,
                                                                                      xhr, $form) {
                                                                         var current = window.location.pathname;
                                                                         if (current.indexOf(".jag") >= 0) {
                                                                             location.href = "index.jag";
                                                                         } else {
                                                                             location.href = 'site/pages/index.jag';
                                                                         }
                                                                     }
                                                                 });
                                          }
                                      });

    $.validator.addMethod('contextExists', function(value, element) {
        if (value.charAt(0) != "/") {
            value = "/" + value;
        }
        var contextExist = false;
        jagg.syncPost("/site/blocks/item-add/ajax/add.jag", { action:"isContextExist", context:value },
                      function (result) {
                          if (!result.error) {
                              contextExist = result.exist;
                          }
                      });
        return this.optional(element) || contextExist != "true";
    }, 'Duplicate context value.');


});