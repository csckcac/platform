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
this.serviceName = "sessionTest";
this.documentation = "Testing Session Host Object";
this.scope = "application";

var key = "number";

putValue.documentation = "Put a value to the Session.";
function putValue() {
    try {
        session.put(key, 2);
        return key;
    } catch (e) {
        return "Error occurred while putting a value to the Session.";
    }
}

getValue.documentation = "Get a value from the Session.";
function getValue() {
    try {
        var number = session.get(key);
        return number;
    } catch (e) {
        return "Error occurred while getting a value from the Session.";
    }
}

removeValue.documentation = "Remove a value from the Session.";
function removeValue() {
    try {
        session.remove(key);
        return <success/>;
    } catch (e) {
        return "Error occurred while removing a value from the Session.";
    }
}

clearSession.documentation = "Clear the Session.";
function clearSession() {
    try {
        session.clear();
        return <success/>;
    } catch (e) {
        return "Error occurred while clearing the Session.";
    }
}
