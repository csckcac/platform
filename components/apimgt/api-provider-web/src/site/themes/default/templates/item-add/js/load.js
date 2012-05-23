 $(document).ready(function() {
    var v = $("#addAPIForm").validate({
			submitHandler: function(form) {
				$(form).ajaxSubmit({
                    success:function(responseText, statusText, xhr, $form) {
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