$(document).ready(function() {
    $.validator.addMethod('contextExists', function(value, element) {
        if (value.charAt(0) != "/") {
            value = "/" + value;
        }
        var contextExist = false;
        var oldContext=$('#spanContext').text();
        jagg.syncPost("/site/blocks/item-add/ajax/add.jag", { action:"isContextExist", context:value,oldContext:oldContext },
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

    $.validator.addMethod('validRegistryName', function(value, element) {
        var illegalChars = /([~!@#;%^*+={}\|\\<>\"\'\/,])/;
        return !illegalChars.test(value);
    }, 'The Name contains one or more illegal characters (~ ! @ #  ; % ^ * + = { } | &lt; &gt;, \' / " \\ ) .');
});