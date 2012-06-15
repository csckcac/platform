function showrecordEditor(recordId, userId) {
    location.href = "jsp/createEditRecord.jsp?mode=edit&userId=" + userId + "&recordId=" + recordId;
}
function deleterecord(userId, recordId) {
    location.href = "jsp/confirmDeleteRecord.jsp?mode=delete&userId=" + userId + "&recordId=" + recordId;
}
function uploadimage(name) {
    location.href = "jsp/uploadImages.jsp?recordId=" + name;

}

function showBlobs(recordId) {
    location.href = "jsp/showBlobs.jsp?recordId=" + recordId;
}

function deleteblob(blobId, recordId) {
    location.href = "deleteBlob.jsp?blobId=" + blobId + "&recordId=" + recordId;

}
function validatorAddEditRegister()
{
	var errors = [];
	var error;
	error=isValidName(document.dataForm.fullname.value);
	if(error!=null)
	{
		errors[errors.length]=error;
	}
	error=isValidEmail(document.dataForm.contactDetails.value);
	if(error!=null)
	{
		errors[errors.length]=error;
	}
	error=isValidDay(document.dataForm.dob.value);
	if(error!=null)
	{
		errors[errors.length]=error;
	}
	error=isValidBloodGroup(document.dataForm.bloodType.value)
	if(error!=null)
	{
		errors[errors.length]=error;
	}
	if (errors.length > 0) {
		 error=reportErrors(errors);
		 alert(error);
		 return false;
		}
	else
		{
		return true;
		}
}

function validateCreateEditRecord()
{
	var warnings=[];
	var warning;
	var string;
	string = jQuery.trim(document.getElementById("recordData").value);
	if(string=="")
		{
		warnings[warnings.length]="Warning:Record data is empty";
		}
	else{
		document.getElementById("recordData").innerHTML=string;
		}
	string=jQuery.trim(document.getElementById("userComment").value);
	if(string == "")
		{
		warnings[warnings.length]="Warning:User comment is empty";
		}
	else
		{
		document.getElementById("userComment").value=string;
		}
	if (warnings.length > 0) {
		 warning=reportErrors(warnings);
		 var confirmation=confirm(warning);
		 if(confirmation==true)
			 {
			 return true;
			 }
		 else
			 {
			 return false;
			 }
		}
	else
		{
		return true;
		}
	
}

function reportErrors(errors){
var msg = "There were some problems...\n";
for (var i = 0; i<errors.length; i++) {
 var numError = i + 1;
 msg += "\n" + numError + ". " + errors[i];
}
return msg;
}
function isValidName(name)
{
	var RE_NAME=/^[a-zA-Z ]+$/;
	if(!RE_NAME.test(name))
		{
		return "Name is not valid";
		}
	else
		{
		return null;
		}
	
}

function isValidEmail(email)
{
	var RE_EMAIL = /^(\w+[\-\.])*\w+@(\w+\.)+[A-Za-z]+$/;
		if(!RE_EMAIL.test(email))
			{
			return "email address is not valid";
			}
		else
			{
			return null;
			}
}

function isValidDay(day)
{
	var RE_BDAY=/^\d{4}-\d{2}-\d{2}$/;
		if(!RE_BDAY.test(day))
			{
			return "Invalid day type.Require yyyy-mm-dd";
			}
		else 
			{
			return null;
			}
		
}
function isValidBloodGroup(bGroup)
{
	var RE_BGROUP=/^(A|B|AB|O)[+-]$/;
		if(!RE_BGROUP.test(bGroup))
			{
			return "Invalid bood group"
			}
		else
			{
			return null;
			}
}
function populateRecordTypeData() {
    var recordType = document.getElementById("recordType");
//    alert(recordType.selectedIndex);
    if (recordType.options[recordType.selectedIndex].text == "Wellness") {
        var recordTypeData = document.getElementById("recordTypeData");
        var tmpOption = document.createElement('option');
            tmpOption.value="Blood pressure";
            tmpOption.text="Blood pressure";
            try {
                recordTypeData.add(tmpOption, null); // standards compliant; doesn't work in IE
            }
            catch(ex) {
                recordTypeData.add(tmpOption, null); // IE only
            }

//        recordType.add();
//        recordTypeData.options[0].text = "Blood pressure";
//        recordTypeData.options[1].text = "Hours slept";
//        recordTypeData.options[2].text = "Steps taken ";
//        recordTypeData.options[3].text = "Weight (with BMI)";
    }
}

