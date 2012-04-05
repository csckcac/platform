function doGet(request, response, session) {
	//LOCAL_REPOSITORY, SYSTEM_GOVERNANCE, SYSTEM_CONFIGURATION, USER_GOVERNANCE, USER_CONFIGURATION
	var registry = new Registry("SYSTEM_CONFIGURATION");
	var resource = registry.newResource();
	resource.content = '<products><product name="Mashup Server"/><product name="Gadget Server"/></products>';
	resource.addProperty("url", "http://wso2.com");  
	resource.addProperty("company", "WSO2 Inc.");  
	registry.put("wso2products.xml", resource);

	//now we read the resource again
	var res = registry.get("wso2products.xml");

	//html to show the result
	var html = 
		<html><body>
			<h2>Content</h2>
			<h3 style="color : #800000">{res.content.toXMLString()}</h3>
			<h2>Properties</h2>
			<h3 style="color : #800000">{res.getProperty("url")}</h3>
			<h3 style="color : #800000">{res.getProperty("company")}</h3>
		</body></html>
	response.write(html);
}

