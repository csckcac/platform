var addNewDoc = function () {
    var apiName = apiProviderApp.currentAPIName;
    var version = apiProviderApp.currentVersion;
    var docName = $("#docName").val();
    var summary = $("#summary").val();
    var docType = getRadioValue($('input[name=optionsRadios]:radio:checked'));
    var sourceType = getRadioValue($('input[name=optionsRadios1]:radio:checked'));
    var docUrl = $("#docUrl").val();

    var doc = require("/core/docs/add.js");
    var result = doc.addDocumentation(apiName, version, docName, docType, summary, sourceType, docUrl);
    if (result.error) {
        alert(result.message);
    } else {
        clearDocs();
    }


};


var removeDocumentation = function (apiName, version, docName, docType) {
    var doc = require("/core/docs/remove.js");
    var result = doc.removeDocumentation(apiName, version, docName, docType);
    if (!result.error) {
        $('#' + apiName + '-' + docName).hide('slow');
    }

};

var copyDocumentation = function (apiName, version, docName, docType, summary) {
    $('#newDoc .btn-primary').text('update');
    $('#newDoc').show('slow');
    $('#newDoc #docName').val(docName + '-copy');
    $('#newDoc #summary').val(summary);

    for (var i = 1; i <= 6; i++)
        if ($('#optionsRadios' + i).val().toUpperCase().indexOf(docType.toUpperCase()) >= 0) {
            $('#optionsRadios' + i).attr('checked', true)
        }
};

var updateDocumentation = function (apiName, version, docName, docType, summary, docUrl) {
    $('#newDoc .btn-primary').text('update');
    $('#newDoc').show('slow');
    $('#newDoc #docName').val(docName);
    $('#newDoc #summary').val(summary);
    if (docUrl != "undefined") {
        $('#newDoc #docUrl').val(docUrl);
        $('#optionsRadios8').attr('checked', true);
        $('#newDoc #docUrl').show();
    }

    for (var i = 1; i <= 6; i++)
        if ($('#optionsRadios' + i).val().toUpperCase().indexOf(docType.toUpperCase()) >= 0) {
            $('#optionsRadios' + i).attr('checked', true);
        }
};

var editInlineContent = function (apiName, version, docName, docType, summary, docUrl) {

    window.open("includes/provider/inLineEditor.jag?docName=" + docName + "&apiName=" + apiName + "&version=" + version);

};

var clearDocs = function () {
    var doc=document;
    doc.getElementById('docName').value = '';
    doc.getElementById('summary').value = '';
    doc.getElementById('docUrl').value = '';
    $('#newDoc').hide('slow');
};


