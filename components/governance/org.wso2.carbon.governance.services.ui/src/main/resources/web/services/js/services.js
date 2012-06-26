/**
 * Created by IntelliJ IDEA.
 * User: fazlan
 * Date: 12/23/11
 * Time: 10:49 AM
 * To change this template use File | Settings | File Templates.
 */
function validateIllegalQName(fld, fldName) {
    var error = "";
    //var illegalChars = /([^a-zA-Z0-9_\-\x2E\&\?\/\:\,\s\(\)\[\]])/;
    var illegalChars = /([`()?\[\]~!@#;%^*+={}\|\\<>\"\',])/; // disallow ~!@#$;%^*+={}|\<>"',
    var illegalCharsInput = /(\<[a-zA-Z0-9\s\/]*>)/;
    if (illegalChars.test(fld.value) || illegalCharsInput.test(fld.value)) {
        error = org_wso2_carbon_governance_services_ui_jsi18n["the"] + " " + fldName + " " +
                org_wso2_carbon_governance_services_ui_jsi18n["qname.contains.illegal.chars"] + "<br />";
    } else {
//        fld.style.background = 'White';
    }

    return error;
}