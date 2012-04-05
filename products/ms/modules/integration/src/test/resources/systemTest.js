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
this.serviceName = "systemTest";
this.documentation = "Test System Host Object";

includeJsFile.documentation = "Test including an external JavaScript file";
function includeJsFile() {
    try {
        system.include("concatscript.js");
        // concat is a function in the included concatscript.js javascript
        var answer = concat("20", "12");
        if (answer == "2012") {
            return "Successfully concatenated.";
        } else {
            return "Error occurred during concatenation.";
        }
    } catch (e) {
        return "Error occurred during include operation in System Host Object.";
    }
}

testLocalHostName.documentation = "Test the Local Host Name";
function testLocalHostName() {
    try {
        var localHostName = system.localHostName;
        if (localHostName.length != 0) {
            return "Successfully got localHostName";
        } else {
            return "Length of localHostName is zero.";
        }
    } catch (e) {
        return "Error occurred while getting localHostName in System Host Object.";
    }
}

logAString.documentation = "Test logging a sample string";
function logAString() {
    try {
        system.log("This is a test.");
        return "Successfully logged a String.";
    } catch (e) {
        return "Error occurred while logging in System Host Object.";
    }
}

waitSomeTime.documentation = "Test waiting some time";
function waitSomeTime() {
    try {
        //waits for 1/8 of a second
        system.wait(125);
        //waits for 1/100 of a second
        system.wait();
        return "Successfully waited";
    } catch (e) {
        return "Error occurred while logging in System Host Object.";
    }
}
