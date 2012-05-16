/*
* Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.automation.common.test.greg.remoteregistry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.platform.test.core.utils.gregutils.GregRemoteRegistryProvider;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;

import static org.testng.Assert.*;

import org.testng.annotations.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;

public class RatingTest {
    private static final Log log = LogFactory.getLog(RatingTest.class);
    public RemoteRegistry registry;

    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, RegistryException {
        int tenantId = new GregUserIDEvaluator().getTenantID();
        registry = new GregRemoteRegistryProvider().getRegistry(tenantId);
        removeResource();
    }

    @Test(groups = {"wso2.greg"}, description = "test add rating to resource", priority = 1)
    public void testAddResourceRatingTest() throws RegistryException {
        String path = "/d16/d17/r1";
        Resource r1;
        try {
            r1 = registry.newResource();
            byte[] r1content = "R1 content".getBytes();
            r1.setContent(r1content);
            registry.put(path, r1);
            registry.rateResource(path, 5);
            float rating = registry.getAverageRating(path);
            assertEquals(rating, (float) 5.0, (float) 0.01, "Rating of the resource /d16/d17/r1 should be 5.");
            deleteResources("/d16");
            log.info("****************Registry API Add Rating to a  Resource Test - Passed***************");
        } catch (RegistryException e) {
            log.error("Registry API Add Rating to a  Resource Test - Failed:" + e);
            throw new RegistryException("Registry API Add Rating to a  Resource Test - Failed:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test add rating to a Collection", priority = 2)
    public void testAddCollectionRatingTest() throws RegistryException {
        String path = "/d16/d18";
        Resource r1;
        try {
            r1 = registry.newCollection();
            registry.put(path, r1);
            registry.rateResource(path, 4);
            float rating = registry.getAverageRating(path);
            assertEquals(rating, (float) 4.0, (float) 0.01, "Rating of the resource /d16/d18 should be 5.");
            deleteResources("/d16");
            log.info("****************Registry API Add Rating to a  Collection Test - Passed***************");
        } catch (RegistryException e) {
            log.error("Registry API Add Rating to a  Collection Test -Failed :" + e);
            throw new RegistryException("Registry API Add Rating to a  Collection Test  - Failed:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test edit rating in a resource", priority = 3)
    public void testEditResourceRatingTest() throws RegistryException {
        String path = "/d61/d17/d18/r1";
        Resource r1;
        try {
            r1 = registry.newResource();
            byte[] r1content = "R1 content".getBytes();
            r1.setContent(r1content);
            registry.put(path, r1);
            registry.rateResource(path, 5);

            float rating = registry.getAverageRating(path);
            assertEquals((float) 5.0, rating, (float) 0.01, "Rating of the resource /d61/d17/d18/r1 should be 5.");
            registry.rateResource(path, 3);
            float rating_edit = registry.getAverageRating(path);
            assertEquals((float) 3.0, rating_edit, (float) 0.01, "Rating of the resource /d61/d17/d18/r1 should be 3.");
            deleteResources("/d61");
            log.info("************Registry API Edit Resource Rating Test - Passed***************");
        } catch (RegistryException e) {
            log.error("Registry API Edit Resource Rating Test - Failed :" + e);
            throw new RegistryException("Registry API Edit Resource Rating Test  - Failed:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test add an query to registry", priority = 4)
    public void RatingsPathTest() throws IOException, RegistryException {
        Resource r5;
        try {
            r5 = registry.newResource();
            String r5Content = "this is r5 content";
            r5.setContent(r5Content.getBytes());
            r5.setDescription("production ready.");
            String r5Path = "/c1/r5";

            registry.put(r5Path, r5);
            registry.rateResource("/c1/r5", 3);
            Resource ratings = registry.get("/c1/r5;ratings");
            String[] ratingPaths = (String[]) ratings.getContent();

            Resource c1 = registry.get(ratingPaths[0]);
            InputStream stream = c1.getContentStream();
            StringWriter writer = new StringWriter();
            IOUtils.copy(stream, writer);
            String ratingString = writer.toString();
            assertEquals(ratingString, "3", "Ratings are not retrieved properly as resources.");
            deleteResources("/c1");
            log.info("**************Registry API Ratings Path Test - Passed**************");
        } catch (RegistryException e) {
            log.error("Registry API Ratings Path Test - Failed :" + e);
            throw new RegistryException("Registry API Ratings Path Test  - Failed:" + e);
        } catch (IOException e) {
            log.error("Registry API Ratings Path Test :" + e);
            throw new IOException("Registry API Ratings Path Test - Failed:" + e);
        }

    }

    private void removeResource() throws RegistryException {
        deleteResources("/d16");
        deleteResources("/d61");
        deleteResources("/c1");
    }

    public void deleteResources(String resourceName) throws RegistryException {
        try {
            if (registry.resourceExists(resourceName)) {
                registry.delete(resourceName);
            }
        } catch (RegistryException e) {
            log.error("deleteResources RegistryException thrown:" + e);
            throw new RegistryException("deleteResources RegistryException thrown:" + e);
        }
    }

}
