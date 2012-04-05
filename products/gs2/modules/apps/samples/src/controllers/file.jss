function doGet(request, response, session) {

    //taking the file from server and printing it to the browser. This is a dummy
    var path = request.getParameter("filePath");
    var file = new File(path);
    file.openForReading();

    var message = "";

    while ((message = file.readLine()) != null) {
        response.write(message);
    }

    file.close();
}