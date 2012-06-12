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
this.serviceName = "scrapperTest";
this.documentation = "Test Scrapper Host Object";

testScrap.documentation = "Test a scrapped page from Google";

function testScrap() {
    try {
        var config = <config>
            <var-def name='response'>
                <html-to-xml>
                    <http method='get' url='http://www.google.com'/>
                </html-to-xml>
            </var-def>
        </config>;

        var scraper = new Scraper(config);
        result = scraper.response;
        if (!(result === null)) {
            return "Response is not null or empty";
        } else {
            return "Response is null or empty";
        }
    } catch (e) {
        return "Error occurred in during operations in Scrapper.";
    }
}
