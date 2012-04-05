/**
session Hostobject API(static)
==============================
void function put(string key, object value)
object function get(string key)
void function invalidate()
readonly property number created
readonly property number lastAccessed
property number maxInactive

number	maxIncat
**/
function doGet(request, response, session) {
	var obj = {
		name : "ruchira",
		age : 27,
		address : {
			number : "I",
			address1 : "do not",
			address2 : "have a",
			city : "house"
		}
	};
	session.put("ruchira", obj);
	
	var o = session.get("ruchira", obj);
	response.write("<html><body>");
	response.write(o);
	response.write("</body></html>");
}
