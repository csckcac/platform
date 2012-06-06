$(document).ready(function() {
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

    $.validator.addMethod('selected', function(value, element) {
        return value!="";
    }, 'Select a value for the tier.');
});