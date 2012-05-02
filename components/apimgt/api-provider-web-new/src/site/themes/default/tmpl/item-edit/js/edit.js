$(document).ready(function () {
    var options = {
        success:function () {
            location.href = "index.jag?place=api-details&name=" + apiProviderApp.currentAPIName + "&version=" + apiProviderApp.currentVersion;
        }
    };
    $('#editAPIForm').submit(function () {
        $(this).ajaxSubmit(options);
        return false;
    });

    $('#editAPIForm #apiName').val(apiProviderApp.currentAPIName);
    $('#editAPIForm #version').val(apiProviderApp.currentVersion)



});
