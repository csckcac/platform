ServiceStoreApp = new function () {

    var url = "/apistore/services/registry.jag";
    this.ask = function (call) {
        var path = $("#path").val();
        var appName = $("#appName").val();
        
        console.log("Called for Funtion" + call + ' : ' + path);

        ServiceStoreAppUtil.makeRequest(
            url + "?action=" + call + "&path=" + path, "", function (html) {

                // $("#test").append(html.split('"')[0]);
                $("#response").text(html);
                console.log("result " + html);
            });
    }

    this.login = function () {
        var name = $("#name").val();
        var pass = $("#pass").val();
        console.log("Called for Funtion Login");
        ServiceStoreAppUtil.makeRequest(url + "?action=login&username=" + name
            + "&password=" + pass, "", function (html) {

            $("#response").text(html);
            console.log("result " + html);

        });

    }


    this.call = function (call, args) {
        var path = $("#path").val();
        console.log("Called for Funtion" + call + ' : ' + path);

        ServiceStoreAppUtil.makeRequest(url + "?action=" + call + args,"", function (html) {
                $("#response").text(html);
                console.log("result " + html);
            });
    }
}
