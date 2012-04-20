/*
 * Copyright WSO2, Inc. (http://wso2.com)
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
package org.wso2.csg.integration.tests;

/**
 * This has the tests cases for testing the CSGAgent functionality with CSG server. Deploying and
 * invoking a CSG services for;
 * 1. SOAP service
 * 2. REST service
 * 3. JSON service
 */
public class CSGAgentTestCase extends CSGIntegrationTestCase {

    public CSGAgentTestCase(String adminService) {
        super("ProxyServiceAdmin");
    }
}
