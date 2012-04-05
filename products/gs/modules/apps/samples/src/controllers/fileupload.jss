function doGet(request, response, session) {
    var path = "/tmp";
    var file = new File(request, "file");
    var filePath = file.upload(path);

    response.write("File is written to " + filePath);
}

function doPost(request, response, session) {
    var path = "/tmp";
    var file = new File(request, "file");
    var filePath = file.upload(path);
    response.write("File is written to " + filePath);
}