function showNewAccountTable() {
    document.getElementById('supportForm').style.display = 'none';
    document.getElementById('newAccountForm').style.display = '';
}

function showExistingAccountTable() {
    document.getElementById('supportForm').style.display = '';
    document.getElementById('newAccountForm').style.display = 'none';
}


function loadDropdowns() {
    sessionAwareFunction(function() {
        jQuery.noConflict();
        jQuery("#countries").load('loadCountries-ajaxprocessor.jsp');
        jQuery("#industries").load('loadIndustries-ajaxprocessor.jsp');
    });
}
