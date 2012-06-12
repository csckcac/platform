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
this.serviceName = "httpClientTest";
this.documentation = "Test HttpClient Host Object";

searchGoogle.documentation = "Search using Google Search";

function searchGoogle() {
    try {
        var client = new HttpClient();
        var content = [
            { name:"q", value:"query" },
            { name:"btnG", value:"Google Search" },
            { name:"hl", value:"en" },
            { name:"source", value:"hp" }
        ];
        var params = {
            followRedirect:true
        };

        var code = client.executeMethod("GET", "http://google.com/search", content, params);
        if (code == 200) {
            if (client.response.toString().length != 0) {
                return "Successfully invoked the Google Search";
            } else {
                return "Response from Google Search is empty";
            }
        } else {
            return "<failure><code>" + code + "</code><statusText>" + client.statusText + "</statusText></failure>";
        }
    } catch (e) {
        return "Error occurred while Google query in HttpClient Host Object";
    }
}
