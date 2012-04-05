
function entitlementMediatorValidate(){
    if(document.getElementById('remoteServiceUrl').value == ''){
        CARBON.showWarningDialog(enti18n["valid.remoteservice.required"]);
        return false;
    }
    if(document.getElementById('remoteServiceUserName').value == ''){
        CARBON.showWarningDialog(enti18n["valid.remoteservice.user.required"]);
        return false;
    }
    if(document.getElementById('remoteServicePassword').value == ''){
        CARBON.showWarningDialog(enti18n["valid.remoteservice.password.required"]);
        return false;
    }
    return true;
}