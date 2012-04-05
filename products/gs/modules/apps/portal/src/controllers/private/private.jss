function doGet(request, response, session) {
    response.write("<html>");
    response.write("<h2>" + "This is a private page only the admin can see this" + "</h2>");
    response.write("</html>");
}

function doPost(request, response, session) {
    response.write("<html><h1>" + "This is a private page only the admin can see this" + "</h1></html>");
}