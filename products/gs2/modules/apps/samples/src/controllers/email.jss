function doPost(request, response, session) {

    var cont = request.getParameter("cont");
    var to = request.getParameter("to");
    var sub = request.getParameter("sub");

    //Add valid credentials
    var email = new Email("smtp.gmail.com", "25", "username", "password");

    //Fill the correct information
    email.from = "";
    email.to = to;
    email.cc = "";
    email.bcc = "";
    email.subject = sub;
    email.text = cont;
    email.send();

    response.write("email successfully sent to " + to);
}


