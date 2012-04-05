function validate() {
    var email = document.getElementById('emailAddress').value;
    var password = document.getElementById('password').value;
    if (email.trim() == "") {
        alert("Please enter a valid email address!");
        return false;
    }

    if (password.trim() == "") {
        alert("Please enter a valid password value!");
        return false;
    }

    if (email.search(/^[a-zA-Z]+([_\.-]?[a-zA-Z0-9]+)*@[a-zA-Z0-9]+([\.-]?[a-zA-Z0-9]+)*(\.[a-zA-Z]{2,4})+$/) == -1) {
        alert("Email format is not correct. Please enter a valid email address.");
        return false;
    }
    return true;
}

function setCategory() {
    var categories = document.getElementById("categories");
    var category = categories[categories.selectedIndex].value;
    document.location.href = "index.jsp?category=" + category;
}

function redirectToExportDataPage(category) {
    document.location.href = "export.jsp?category=" + category;
}

function setVisibility() {
    if (document.getElementById('customCredentials').checked) {
        jQuery('#sfCredDiv').show();
        jQuery('#loginBtnDiv').hide();
        document.getElementById('loginMethod').value = "userDefined";
    } else {
        jQuery('#sfCredDiv').hide();
        jQuery('#loginBtnDiv').show();
        document.getElementById('loginMethod').value = "default";
    }
}


function validateFields() {
    var loginMethod = document.getElementById("loginMethod").value;
    var email = document.getElementById("emailAddress").value;
    var password = document.getElementById("password").value;
    var publicKey = document.getElementById("publicKey").value;

    if (loginMethod == "userDefined") {
        if (email.trim() == "") {
            alert("Please enter a valid email address!");
            return false;
        }

        if (password.trim() == "") {
            alert("Please enter a valid password value!");
            return false;
        }

        if (email.search(/^[a-zA-Z]+([_\.-]?[a-zA-Z0-9]+)*@[a-zA-Z0-9]+([\.-]?[a-zA-Z0-9]+)*(\.[a-zA-Z]{2,4})+$/) == -1) {
            alert("Email format is not correct. Please enter a valid email address.");
            return false;
        }
    }
    return true;
}

function validateGoogleExportFeilds() {
    var g_username = document.getElementById("g_username").value;
    var g_password = document.getElementById("g_password").value;
    var title = document.getElementById("title").value;

    if (g_username == null || g_username == "") {
        alert("Please enter a valid google username");
        return false;
    }
    if (g_password == null || g_password == "") {
        alert("Please enter a valid password");
        return false;
    }
    if (title == null || title == "") {
        alert("Please enter a valid name to be used as the google spreadsheet title");
        return false;
    }
    return true;
}
function popup() {

    // get the screen height and width  
    var maskHeight = jQuery(document).height();
    var scrollTop = jQuery(window).scrollTop();
    var windowHeight = jQuery(window).height();

    var maskWidth = jQuery(window).width();

    // calculate the values for center alignment
    var dialogTop = scrollTop + (windowHeight / 2) - 200;//(maskHeight/3) - (jQuery('#dialog-box').height());
    var dialogLeft = (maskWidth / 2) - 500;//(maskWidth/2) - (jQuery('#dialog-box').width()/2);

    // assign values to the overlay and dialog box
    if (!jQuery.browser.msie) {
        jQuery('#dialog-overlay').css({height:maskHeight, width:maskWidth}).show();
    }
    jQuery('#dialog-box').css({top:dialogTop, left:dialogLeft}).show();

    // display the message
    //jQuery('#dialog-message').html(message);

}

function checkAllExport() {
    if (document.getElementById("cbExportAll").checked) {
        document.getElementById("category").value = "All";
    }
}
