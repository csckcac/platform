/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.automation.common.test.greg.metadatasearch;

import org.testng.annotations.AfterSuite;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.suiteutills.MasterTestSuite;
import org.wso2.platform.test.core.utils.suiteutills.SuiteVariables;

import java.util.ArrayList;
import java.util.List;

public class GRegMetadataSearchTestSuite extends MasterTestSuite {
    @AfterSuite
    public void suiteRunner() {
        List<SuiteVariables> suiteVariablesList = new ArrayList<SuiteVariables>();
        suiteVariablesList.add(new SuiteVariables("SaveDeleteFilterTest",
                                                  SaveDeleteFilterTest.class));
        suiteVariablesList.add(new SuiteVariables("RegistrySearchByTags",
                                                  RegistrySearchByTags.class));
        suiteVariablesList.add(new SuiteVariables("RegistrySearchByComments",
                                                  RegistrySearchByComments.class));
        suiteVariablesList.add(new SuiteVariables("RegistrySearchByAssociationType",
                                                  RegistrySearchByAssociationType.class));
        suiteVariablesList.add(new SuiteVariables("RegistrySearchByAssociationDestination",
                                                  RegistrySearchByAssociationDestination.class));
        suiteVariablesList.add(new SuiteVariables("RegistrySearchByPropertyName",
                                                  RegistrySearchByPropertyName.class));
        suiteVariablesList.add(new SuiteVariables("RegistrySearchByResourceNameTest",
                                                  RegistrySearchByResourceNameTest.class));
        suiteVariablesList.add(new SuiteVariables("RegistrySearchByKeyword",
                                                  RegistrySearchByKeyword.class));
        suiteVariablesList.add(new SuiteVariables("RegistrySearchByCratedData",
                                                  RegistrySearchByCratedData.class));
        suiteVariablesList.add(new SuiteVariables("RegistrySearchByUpdatedData",
                                                  RegistrySearchByUpdatedData.class));
        suiteVariablesList.add(new SuiteVariables("RegistrySearchByAuthor",
                                                  RegistrySearchByAuthor.class));
        suiteVariablesList.add(new SuiteVariables("RegistrySearchByUpdater",
                                                  RegistrySearchByUpdater.class));
        suiteVariablesList.add(new SuiteVariables("RegistrySearchByMediaType",
                                                  RegistrySearchByMediaType.class));
        suiteVariablesList.add(new SuiteVariables("RegistrySearchByAll",
                                                  RegistrySearchByAll.class));

        setServerList(ProductConstant.GREG_SERVER_NAME);
        superSuite("GRegMetadataSearchTestSuite", suiteVariablesList).run();

    }

    public static void main(String[] args) {
        new GRegMetadataSearchTestSuite().suiteRunner();

    }
}
