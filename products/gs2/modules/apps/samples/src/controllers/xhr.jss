/**
XMLHttpRequest API is same as which can be found in browser environment.
**/
function doGet(request, response, session) {
	var xhr = new XMLHttpRequest();
    xhr.open("GET", "http://www.google.lk");
    xhr.send();
	response.write(xhr.responseText);
}
