 $(document).ready(function() {
    var v = $("#addAPIForm").validate({
			submitHandler: function(form) {
				$(form).ajaxSubmit({
                    success:function(responseText, statusText, xhr, $form) {
                        $('#apiModal h3.modal-title').html('API Provider');
                        $('#apiModal div.modal-body').html('\n\nSuccessfully added new API.\n Do you want to leave this page?');
                        $('#apiModal a.btn-primary').html('Yes');
                        $('#apiModal a.btn-other').html('No. Stay and reset form.');
                        $('#apiModal div.modal-footer').append($('<a class="btn btn-other-noreset" data-dismiss="modal">No. Stay</a>'));
                        $('#apiModal a.btn-other').click(function(){
                            v.resetForm();
                        });
                        $('#apiModal a.btn-primary').click(function(){
                            location.href = "site/pages/index.jag";
                        });
                        $('#apiModal').modal();
                    }
                });
			}
		});

 });