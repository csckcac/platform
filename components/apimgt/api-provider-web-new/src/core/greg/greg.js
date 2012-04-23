var getServerURL = function() {
    //TODO : this should be initially fetched using APIStore HostObject and store in Context for subsequent calls
    return "https://localhost:9443";
};

var getServer = function() {
    return "localhost";
};

var getPort = function() {
    return "9443";
};

var getAdminCookie = function() {
    //TODO : this should be set in the Context during the deployment

};

var getAPIProviderObj = function() {
    var provider = require('apistore');
    return new provider.APIProvider();
};