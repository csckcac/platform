/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.impl.dao.test;


import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.dto.UserApplicationAPIUsage;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.APIKeyInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;

import java.util.Date;
import java.util.Set;

public class APIMgtDAOTest extends TestCase {
    Log log = LogFactory.getLog(APIMgtDAOTest.class);
    ApiMgtDAO apiMgtDAO;


    @Override
    protected void setUp() throws Exception {
        String dbConfigPath = System.getProperty("APIManagerDBConfigurationPath");
        apiMgtDAO = new ApiMgtDAO(dbConfigPath);
    }

    public void testGetSubscribersOfProvider() throws Exception{
        Set<Subscriber>  subscribers = apiMgtDAO.getSubscribersOfProvider("SUMEDHA");
        assertNotNull(subscribers);
        assertTrue(subscribers.size() > 0);
    }
    public void testAccessKeyForAPI() throws Exception {
        APIInfoDTO apiInfoDTO = new APIInfoDTO();
        apiInfoDTO.setApiName("API1");
        apiInfoDTO.setProviderId("SUMEDHA");
        apiInfoDTO.setVersion("V1.0.0");
        String accessKey = apiMgtDAO.getAccessKeyForAPI("SUMEDHA", "APPLICATION1", apiInfoDTO);
        assertNotNull(accessKey);
        assertTrue(accessKey.length() > 0);
    }


    public void testGetSubscribedAPIsOfUser()throws Exception{
        APIInfoDTO[] apis = apiMgtDAO.getSubscribedAPIsOfUser("SUMEDHA");
        assertNotNull(apis);
        assertTrue(apis.length > 1);
    }

    public void testValidateKey() throws Exception{
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = apiMgtDAO.validateKey("deli", "1.0.0", "a1b2c3d4");
        assertNotNull(apiKeyValidationInfoDTO);
    }

    public void testGetSubscribedUsersForAPI() throws Exception{
        APIInfoDTO apiInfoDTO = new APIInfoDTO();
        apiInfoDTO.setApiName("API1");
        apiInfoDTO.setProviderId("SUMEDHA");
        apiInfoDTO.setVersion("V1.0.0");
        APIKeyInfoDTO[] apiKeyInfoDTO = apiMgtDAO.getSubscribedUsersForAPI(apiInfoDTO);
        assertNotNull(apiKeyInfoDTO);
        assertTrue(apiKeyInfoDTO.length > 1);
    }

    public void testGetSubscriber() throws Exception{
        Subscriber subscriber = apiMgtDAO.getSubscriber("SUMEDHA");
        assertNotNull(subscriber);
        assertNotNull(subscriber.getName());
        assertNotNull(subscriber.getId());
    }

    public void testIsSubscribed() throws Exception{
        APIIdentifier apiIdentifier = new APIIdentifier("SUMEDHA","API1","V1.0.0");
        boolean isSubscribed = apiMgtDAO.isSubscribed(apiIdentifier, "SUMEDHA");
        assertTrue(isSubscribed);

        apiIdentifier = new APIIdentifier("P1","API2","V1.0.0");
        isSubscribed = apiMgtDAO.isSubscribed(apiIdentifier, "UDAYANGA");
        assertFalse(isSubscribed);
    }


    public void testGetAllAPIUsageByProvider() throws Exception{
        UserApplicationAPIUsage[] userApplicationAPIUsages = apiMgtDAO.getAllAPIUsageByProvider("SUMEDHA_API1_V1.0.0");
        assertNotNull(userApplicationAPIUsages);

    }
    
    
    public void testAddSubscription() throws Exception{
        APIIdentifier apiIdentifier = new APIIdentifier("SUMEDHA","API1","V1.0.0");
        apiIdentifier.setApplicationId("APPLICATION99");
        apiIdentifier.setTier("T1");
        apiMgtDAO.addSubscription(apiIdentifier, "SUMEDHA",100);
    }

    public void testRegisterAccessToken()throws  Exception{
        APIInfoDTO apiInfoDTO = new APIInfoDTO();
        apiInfoDTO.setApiName("API1");
        apiInfoDTO.setProviderId("ADMIN");
        apiInfoDTO.setVersion("1.0.0");
        apiMgtDAO.registerAccessToken("CON1","APPLICATION1","ADMIN",0,apiInfoDTO);
        //TODO : Add assertions
    }

    public void testGetSubscribedAPIs() throws Exception{
        Subscriber subscriber = new Subscriber("SUMEDHA");
        subscriber.setDescription("Subscriber description");
        Set<SubscribedAPI>  subscribedAPIs = apiMgtDAO.getSubscribedAPIs(subscriber);
        assertNotNull(subscribedAPIs);

    }
    
    public void testAddApplication() throws Exception{
        Subscriber subscriber = new Subscriber("SUMEDHA");
        subscriber.setDescription("Subscriber description");

        Application application = new Application("APPLICATION999",subscriber);
        Application application1 = new Application("APPLICATION998",subscriber);

        apiMgtDAO.addApplication(application, "SUMEDHA");
        apiMgtDAO.addApplication(application1, "SUMEDHA");

        Application[] applications = apiMgtDAO.getApplications(subscriber);
        assertNotNull(applications);
        assertTrue(applications.length > 0);
        for(int a = 0; a < applications.length; a++){
            assertTrue(applications[a].getId() > 0);
            assertNotNull(applications[a].getName());
        }

    }

    public void testAddApplication2() throws Exception{
        Application application = new Application("APPLICATION1000",null);
        apiMgtDAO.addApplication(application, "SUMEDHA");
        Application[] applications = apiMgtDAO.getApplications(null);
        assertNull(applications);

        Subscriber subscriber = new Subscriber("NEWUSER");
        applications = apiMgtDAO.getApplications(subscriber);
        assertNull(applications);

        subscriber = new Subscriber("SUMEDHA");
        applications = apiMgtDAO.getApplications(subscriber);
        assertNotNull(applications);
    }
    
    public void checkSubscribersEqual(Subscriber lhs, Subscriber rhs) throws Exception {
    	assertEquals(lhs.getId(), rhs.getId());
    	assertEquals(lhs.getEmail(), rhs.getEmail());
    	assertEquals(lhs.getName(), rhs.getName());
    	assertEquals(lhs.getSubscribedDate().getTime(), rhs.getSubscribedDate().getTime());
    	assertEquals(lhs.getTenantId(), rhs.getTenantId());
    }
    
    public void testAddGetSubscriber() throws Exception {
    	Subscriber subscriber1 = new Subscriber("LA_F");
    	subscriber1.setEmail("laf@wso2.com");
    	subscriber1.setSubscribedDate(new Date());
    	subscriber1.setTenantId(25);
    	apiMgtDAO.addSubscriber(subscriber1);
    	assertTrue(subscriber1.getId() > 0);
    	Subscriber subscriber2 = apiMgtDAO.getSubscriber(subscriber1.getId());
    	this.checkSubscribersEqual(subscriber1, subscriber2);
    }
    
    public void testUpdateGetSubscriber() throws Exception {
    	Subscriber subscriber1 = new Subscriber("LA_F");
    	subscriber1.setEmail("laf@wso2.com");
    	subscriber1.setSubscribedDate(new Date());
    	subscriber1.setTenantId(25);
    	apiMgtDAO.addSubscriber(subscriber1);
    	assertTrue(subscriber1.getId() > 0);
    	subscriber1.setEmail("laf2@wso2.com");
    	subscriber1.setSubscribedDate(new Date());
    	subscriber1.setTenantId(35);
    	apiMgtDAO.updateSubscriber(subscriber1);
    	Subscriber subscriber2 = apiMgtDAO.getSubscriber(subscriber1.getId());
    	this.checkSubscribersEqual(subscriber1, subscriber2);
    }

}

