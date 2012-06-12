/*
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
this.serviceName = "requestTest";
this.documentation = "Testing Request Host Object";

testAuthenticatedUser.documentation = "Test the authenticated user";
function testAuthenticatedUser() {
    try {
        var user = request.authenticatedUser.toString();
        system.log("This mashup was invoked by the following User : " + user);
        if (user.length != 0) {
            return "Authenticated user is not empty or null";
        } else {
            return "Authenticated user is empty or null";
        }
    } catch (e) {
        return "Error occurred while getting the Authenticated User";
    }
}

testRemoteIp.documentation = "Test the remote IP address";
function testRemoteIp() {
    try {
        var ip = request.remoteIP.toString();
        system.log("This mashup was invoked from the following IP : " + ip);
        if (ip.length != 0) {
            return "Remote IP is not empty or null";
        } else {
            return "Remote IP is empty or null";
        }
    } catch (e) {
        return "Error occurred while getting the Remote IP";
    }
}

testInvokedUrl.documentation = "Test the invoked URL";
function testInvokedUrl() {
    try {
        var address = request.address.toString();
        system.log("This mashup was invoked with the following URL : " + address);
        if (address.length != 0) {
            return "URL is not empty or null";
        } else {
            return "URL is empty or null";
        }
    } catch (e) {
        return "Error occurred while getting the invoked URL";
    }
}
