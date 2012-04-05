/**
request Hostobject API(static)
==============================
string function getHeader(string name)
string function getParameter(string name)
readonly property object content //if the content type is either application/json or 
								 //application/json/badgerfish, then content will be a json object
readonly property string method
readonly property string protocol
readonly property string queryString
readonly property string contentType
readonly property number contentLength
**/
function doGet(request, response, session) {
	response.write("<html><body>");
	response.write("Method : " + request.method + "<br/>");
	response.write("Protocol : " + request.protocol + "<br/>");
	response.write("User-Agent : " + request.getHeader("User-Agent"));
	response.write("</body></html>");
}
