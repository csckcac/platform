CoffeShopAppUtil = new function() {
    this.makeRequest = function(type, u, d, callback) {
        $.ajax({
            type: type,
            url: u,
            data: d,
            dataType: "json",
            success: callback
        });
    };
    
    this.makePost = function(u, d, callback) {
        $.ajax({
            type: "POST",
            url: u,
            data: d,
            dataType: "text",
            success: callback
        });
    };
    this.makeJsonRequest = function(u, d, callback) {
        $.ajax({
            type: "GET",
            url: u,
            data: d,
            dataType: "json",
            success: callback
        });
    };
}


