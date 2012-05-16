function loadDefaultTinyMCEContent(){
	             apiProviderApp.call("action=getInlineContent&apiName="+apiProviderApp.currentAPIName+"&version="+apiProviderApp.currentVersion+"&docName="+apiProviderApp.currentDocName, function (json) {
	             	var docName = json.data.doc[0].docName;
	             	var apiName = json.data.doc[0].apiName;
	             	var docContent = json.data.doc[0].docContent;
	             	apiProviderApp.currentProviderName=json.data.doc[0].apiProvider;
	             	$('#apiDeatils').empty().html('<p><h1> '+docName+ '</h1></p>');
	             	console.log(docContent);  
	             	var stringOut = decodeURI(docContent);
	            	var xout= stringOut.replace(/%2F/g, "/");
	            	// xout= xout.replace(/+/g, " ");
	            	 xout= xout.replace(/%3D/g, "=");
	            	 xout= xout.replace(/%23/g, "=");
	            	console.log(xout); 
	             	tinyMCE.activeEditor.setContent(xout);
	             });
}