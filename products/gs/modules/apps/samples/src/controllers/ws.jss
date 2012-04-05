function doGet(request, response, session) {
	var client = new WSRequest();
	var options = {
		useSOAP : 1.2,  
    	useWSA : 1.0,  
    	oaction : "urn:getVersion"
	};
	var payload = 
		<p:greet xmlns:p="http://www.wso2.org/types">
			<name>Ruchira</name>
		</p:greet>;
	var result;
	try {
		client.open(options,"http://appserver.stratoslive.wso2.com/services/t/ruchira.com/HelloService", false);  
		client.send(payload);  
		result = client.responseE4X;
	} catch(e) {
        result = e.toString();
	}

	//html to show the result
	var html = 
		<html><body>
			<p><h2>HelloService invoked and the response was</h2></p>
			<p><h3 style="color : #800000">{result}</h3></p>
		</body></html>; 
	response.write(html);
}
