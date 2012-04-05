/**
response Hostobject API(static)
==============================
string function getHeader(string name)
string function getParameter(string name)
property object content //if the passed object is either a json or xml, 
					    //then they will be serialized into strings
property string contentType
property number status
**/
function doGet(request, response, session) {
	response.write("<html><body>");
	response.write("Method : " + request.method + "<br/>");
	response.write("Protocol : " + request.protocol + "<br/>");
	response.write("User-Agent : " + request.getHeader("User-Agent"));
	response.write("</body></html>");
}
