$(document).ready(function () { 
	console.log('init Store of the getContent'+ServiceStoreApp.currentAPIName+ ':' +ServiceStoreApp.currentApiVersion);
	
	ServiceStoreApp.call("action=getInlineContent&apiName="+ServiceStoreApp.currentAPIName+"&version="+ServiceStoreApp.currentApiVersion+"&apiProvider="+ServiceStoreApp.currentApiProvider+"&docName="+ServiceStoreApp.currentDocName, function (json) {
        	var docName = json.data.doc[0].docName;
        	var apiName = json.data.doc[0].apiName;
        	var docContent = json.data.doc[0].docContent;
        	console.log('init of the getContent'+docContent);
        	ServiceStoreApp.currentProviderName=json.data.doc[0].apiProvider;
        	$('#apiDeatils').empty().html('<p><h1> '+docName+' </h1> </p>');
        	var stringOut = decodeURI(docContent);
        //	var xout= stringOut.replace("%2F", "/");
        	$('#inlineDoc').html(stringOut);        	   
        });

  
});