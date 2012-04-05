//for IE
if (typeof String.prototype.trim !== 'function') {
    String.prototype.trim = function() {
        return this.replace(/^\s+|\s+$/g, '');
    }
}

function validateCredentials() {

    var key = document.getElementById('txtKey').value;

    var url = document.getElementById('txtURL').value;

    var username = document.getElementById('txtUserName').value.trim();

    var password = document.getElementById('txtPassword').value;

    var reTypedPassword = document.getElementById('txtRetypePassword').value;

    var isStratosService = document.getElementById('isStratosService').value;

    if (key == '' && isStratosService == 'false') {
        CARBON.showWarningDialog("Unique name to represent a JIRA account is mandatory");
        return false;
    }

    if (url == '' && isStratosService == 'false') {
        CARBON.showWarningDialog("JIRA url is mandatory");
        return false;
    }

    if (username == '') {
        CARBON.showWarningDialog("Username is mandatory");
        return false;
    }

    if (password == '') {
        CARBON.showWarningDialog("Password is mandatory");
        return false;
    }

    if (reTypedPassword == '') {
        CARBON.showWarningDialog("Please retype the password");
        return false;
    }

    if (password != reTypedPassword) {
        CARBON.showWarningDialog("Password and Password Repeat donot match. Please re-enter.");
        return false;
    }

    return true;


}


function validateLoginForm() {

    var isStratosService = document.getElementById('isStratosService').value;

    if (isStratosService == 'false') {
        var key = document.getElementById('txtKey').value;
        var url = document.getElementById('txtURL').value;
        if (key == '' && isStratosService == 'false') {
            CARBON.showWarningDialog("Unique name to represent a JIRA account is mandatory");
            return false;
        }

        // check for spaces
        if (key.indexOf(invalid) > -1 && isStratosService == 'false') {
            CARBON.showWarningDialog("Sorry, spaces are not allowed in key.");
            return false;
        }

        if (url == '' && isStratosService == 'false') {
            CARBON.showWarningDialog("JIRA url is mandatory");
            return false;
        }
    }

    var username = document.getElementById('txtUserName').value.trim();

    var email = document.getElementById('txtEmail').value.trim();

    if (email == '') {
        CARBON.showWarningDialog("Email is mandatory");
        return false;
    }


    var password = document.getElementById('txtPassword').value;

    var reTypedPassword = document.getElementById('txtRetypePassword').value;

    var invalid = " ";


    if (username == '') {
        CARBON.showWarningDialog("Username is mandatory");
        return false;
    }

    if (password == '') {
        CARBON.showWarningDialog("Password is mandatory");
        return false;
    }

    if (reTypedPassword == '') {
        CARBON.showWarningDialog("Please retype the password");
        return false;
    }

    if (password != reTypedPassword) {
        CARBON.showWarningDialog("Password and Password Repeat donot match. Please re-enter.");
        return false;
    }

    document.forms["accountForm"].submit();

    return true;
}


function validateIssue() {

    var isStratosService = document.getElementById('isStratosService').value;

    if (isStratosService == 'false') {
        var account = document.getElementById("accountNames").value;

        var project = document.getElementById("projectList").value;

        if (account == '--Select--' && isStratosService == 'false') {
            CARBON.showWarningDialog("Please select a JIRA account.\nIf you have not already created an account go to Issue " +
                    "Tracker -> Accounts and create a new account.");
            return false;
        }

        if (project == '--Select--' && isStratosService == 'false') {
            CARBON.showWarningDialog("Please select a project");
            return false;
        }
    }
    var summary = document.getElementById("summary").value.trim();

    var type = document.getElementById("type").value;


    if (type == '--Select--') {
        CARBON.showWarningDialog("Issue type is mandatory");
        return false;
    }

    if (summary == '') {
        CARBON.showWarningDialog("Summary is mandatory");
        return false;
    }
    var invalidString = new RegExp("<input>");
    if (summary.search(invalidString) != -1) //if match
    {
        CARBON.showWarningDialog("Summary contains invalid characters");
        return false;
    }
    var description = document.getElementById("description").value.trim();
    if (description == '') {
        CARBON.showWarningDialog("Please enter a description about the issue");
        return false;
    }


    return true;

}

function validateNewOTAccount() {
    var email = document.getElementById('email').value.trim();
    var firstname = document.getElementById('firstName').value.trim();
    var lastname = document.getElementById('lastName').value.trim();
    var company = document.getElementById('company').value.trim();
    var country = document.getElementById('country').value;
    var industry = document.getElementById('edit-profile-industry').value;


    if (email == '') {
        CARBON.showWarningDialog("Email is mandatory");
        return false;
    }

    if (firstname == '') {
        CARBON.showWarningDialog("Firstname is mandatory");
        return false;
    }


    if (lastname == '') {
        CARBON.showWarningDialog("Lastname is mandatory");
        return false;
    }

    if (company == '') {
        CARBON.showWarningDialog("Company field is mandatory");
        return false;
    }

    if (country == 'Please select the country') {
        CARBON.showWarningDialog("Country field is mandatory");
        return false;
    }


    if (industry == 'Please select the industry') {
        CARBON.showWarningDialog("Indutry field is mandatory");
        return false;
    }

    document.forms["newAccountForm"].submit();
}

function validateSupportForm() {
    var username = document.getElementById('txtUserName').value.trim();

    var password = document.getElementById('txtPassword').value;

    var reTypedPassword = document.getElementById('txtRetypePassword').value;

    if (username == '') {
        CARBON.showWarningDialog("Email is mandatory");
        return false;
    }

    if (password == '') {
        CARBON.showWarningDialog("Password is mandatory");
        return false;
    }

    if (reTypedPassword == '') {
        CARBON.showWarningDialog("Please retype the password");
        return false;
    }

    if (password != reTypedPassword) {
        CARBON.showWarningDialog("Password and Password Repeat donot match. Please re-enter.");
        return false;
    }

    document.forms["supportForm"].submit();

    return true;
}

function validateExistingOTForm() {
    var email = document.getElementById('txtEmail').value.trim();

    var password = document.getElementById('txtPassword').value;


    if (email == '') {
        CARBON.showWarningDialog("Email is mandatory");
        return false;
    }


    if (password == '') {
        CARBON.showWarningDialog("Password is mandatory");
        return false;
    }

    document.forms["supportForm"].submit();

    return true;
}
