function doGet(request, response, session) {
    var obj = {
        name : "ruchira",
        age : 27,
        address : {
            number : "16",
            city : "ahangama"
        }
    };
	session.put("myObj", obj);
    response.write("<html>");
	response.write("<h2>" + session.get("myObj").toSource() + "</h2>");
    response.write("<h2>" + request.contentType + "</h2>");
    response.write("<h2>" + request.queryString + "</h2>");
    response.write("<h2>" + request.getHeader("User-Agent") + "</h2>");
    response.write("<h2>" + request.getParameter("lang") + "</h2>");
    response.write("</html>");
}

function doPost(request, response, session) {
    response.write("<html><h1>" + request.content +"</h1></html>");
}