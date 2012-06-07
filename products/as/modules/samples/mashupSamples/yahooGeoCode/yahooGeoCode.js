/*
* Copyright 2005-2007 WSO2, Inc. http://www.wso2.org
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
this.serviceName = "yahooGeoCode";
this.documentation = <div>This service wraps the <a href = "http://developer.yahoo.com/maps/rest/V1/geocode.html" target = "_blank">
                     Yahoo! Maps Web Services - Geocoding API.</a></div> ;

geocode.documentation = "Given an address returns a specific latitude and longitude" ;
geocode.safe = true;
geocode.inputTypes = {
    "street" : "string" ,
    "city" : "string",
    "state" : "AL | AK | AZ | AR | CA | CO | CT | DC | DE | FL | GA | HI | ID | IL | IN | IA | KS | KY | LA | ME | MD | MA | MI | MN | MS | MO | MT | NE | NV | NH | NJ | NM | NY | NC | ND | OH | OK | OR | PA | RC | SC | SD | TN | TX | UT | VT | VA | WA | WV | WI | WY"  };
geocode.outputType = "xml";

function geocode(street, city, state)
{
    var key = getApiKey();
    if (key == null) throw("You must first provision this service with a valid Yahoo API Key.  See http://developer.yahoo.com/wsregapp/index.php.");

    var yahoo = new WSRequest();
    var options = new Array();
    options["useSOAP"] = "false";
    options["HTTPMethod"] = "get";

    yahoo.open(options, "http://local.yahooapis.com/MapsService/V1/geocode", false);
    var request = "<parameters><appid>" + key + "</appid><street>" + street + "</street><city>" + city + "</city><state>" + state + "</state></parameters>";
    yahoo.send(request);
    return yahoo.responseXML;
}

//  This sample demonstrates provisioning a service with confidential information.  By saving the apikey in
//  the _private area, it will not be shared with others (unless they somehow have file system access).
//  Thus the apikey is kept confidential.  When someone downloads and uses the mashup, they must provide
//  their own apikey in a write-once operation.  The author need not worry that sharing the mashup will
//  compromise the confidential information.  A similar technique would work for usernames and passwords.

provisionApiKey.documentation = <div>Before using the geocode service, register for a Yahoo API Key [1] and use it to provision the service (write-once only).<br/>[1] <a href="http://developer.yahoo.com/wsregapp/index.php">http://developer.yahoo.com/wsregapp/index.php</a>.<br/>
                                For sample purposes (though it kind of defeats the exercise) you can use ours: "rPJmz.HV34Hn654ySbBEOJf2i4nuae6LqGKX5EmrN30Q9aTeabb3vwThv2jRDN.rf3aAIw--".</div>
provisionApiKey.inputTypes = {"key" : "string"};
provisionApiKey.outputType = "boolean";
function provisionApiKey (key) {
    if (getApiKey() == null) {
        var f = new File("_private/apikey.txt");
        if (!f.exists)
            f.createFile();
        f.openForWriting();
        f.write(key);
        f.close();
    } else throw ("Yahoo api key has already been provisioned for this service.");
    return true;
}

getApiKey.visible = false;
function getApiKey() {
    var key = null;

    var f = new File("_private/apikey.txt");
    if (f.exists) {
        f.openForReading();
        key = new XML(f.readAll());
    }
    f.close();

    return key;
}
