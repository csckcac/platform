/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.wso2.carbon.cep.core.internal.config;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cep.core.Bucket;
import org.wso2.carbon.cep.core.mapping.input.Input;
import org.wso2.carbon.cep.core.Query;
import org.wso2.carbon.cep.core.exception.CEPConfigurationException;
import org.wso2.carbon.cep.core.internal.config.input.InputHelper;
import org.wso2.carbon.cep.core.internal.util.CEPConstants;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.namespace.QName;
import java.util.Iterator;

/**
 * this class is used to parse the top level bucket attributes
 */

public class BucketHelper {
    private static final Log log = LogFactory.getLog(BucketHelper.class);


    public static Bucket fromOM(OMElement bucketElement) throws CEPConfigurationException {

        Bucket bucket = new Bucket();

        String name =
                bucketElement.getAttributeValue(new QName(CEPConstants.CEP_CONF_ATTR_NAME));
        bucket.setName(name);

        OMElement descriptionElement =
                bucketElement.getFirstChildWithName(new QName(CEPConstants.CEP_CONF_NAMESPACE,
                        CEPConstants.CEP_CONF_ELE_DESCRIPTION));
        if (descriptionElement != null) {
            bucket.setDescription(descriptionElement.getText());
        }

        String engineProvider =
                bucketElement.getAttributeValue(
                        new QName(CEPConstants.CEP_CONF_ATTR_ENGINE_PROVIDER));
        bucket.setEngineProvider(engineProvider);

        String owner = bucketElement.getAttributeValue(new QName(CEPConstants.CEP_CONF_ELE_CEP_BUCKET_OWNER));
        if (owner != null) {
            bucket.setOwner(owner);
        }

        String overWriteRegistry =
                bucketElement.getAttributeValue(new QName(CEPConstants.CEP_CONF_ATTR_OVER_WRITE_REGISTRY));
        if (overWriteRegistry != null && overWriteRegistry.length() > 3) {
            boolean overWriteRegistryB;
            try {
                overWriteRegistryB = Boolean.parseBoolean(overWriteRegistry);
            } catch (Exception e) {
                overWriteRegistryB = false;
            }
            bucket.setOverWriteRegistry(overWriteRegistryB);
        }

        OMElement inputOmElement = null;
        for (Iterator iter = bucketElement.getChildrenWithName(new QName(CEPConstants.CEP_CONF_NAMESPACE,
                CEPConstants.CEP_CONF_ELE_INPUT)); iter.hasNext();) {
            inputOmElement = (OMElement) iter.next();
            bucket.addInput(InputHelper.fromOM(inputOmElement));
        }


        OMElement queryOmElement = null;
        int queryIndex = 0;
        for (Iterator iterator = bucketElement.getChildrenWithName(new QName(CEPConstants.CEP_CONF_NAMESPACE,
                CEPConstants.CEP_CONF_ELE_QUERY)); iterator.hasNext();) {
            queryOmElement = (OMElement) iterator.next();
            Query query =  QueryHelper.fromOM(queryOmElement);
            query.setQueryIndex(queryIndex);
            bucket.addQuery(query);
            queryIndex++;
        }


        return bucket;

    }

    /**
     * This method adds  bucket information to registry
     * */
    public static void addBucketToRegistry(Bucket bucket,
                                           Registry registry,
                                           String parentCollectionPath) throws CEPConfigurationException {
        try {
            Collection bucketCollection = registry.newCollection();
            bucketCollection.addProperty(CEPConstants.CEP_CONF_ELE_NAME, bucket.getName());
            bucketCollection.addProperty(CEPConstants.CEP_CONF_ELE_DESCRIPTION, bucket.getDescription());
            bucketCollection.addProperty(CEPConstants.CEP_CONF_ELE_CEP_ENGINE_PROVIDER, bucket.getEngineProvider());
            bucketCollection.addProperty(CEPConstants.CEP_CONF_ELE_CEP_BUCKET_OWNER, bucket.getOwner());

            registry.put(parentCollectionPath, bucketCollection);

            InputHelper.addInputsToRegistry(bucket.getInputs(), registry, parentCollectionPath);

            QueryHelper.addQueriesToRegistry(bucket.getQueries(), registry, parentCollectionPath);


        } catch (RegistryException e) {
            String errorMessage = "Can not add bucket to the registry ";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }


    }

    public static Bucket[] loadBucketsFromRegistry(Registry registry) throws CEPConfigurationException {
        Bucket[] buckets = null;
        try {
            String parentCollectionPath = CEPConstants.CEP_CONF_ELE_CEP_BUCKETS + CEPConstants.CEP_REGISTRY_BS;
            if (registry.resourceExists(parentCollectionPath)) {
                if (registry.get(parentCollectionPath) instanceof Collection) {
                    Collection cepBucketsCollection = (Collection) registry.get(parentCollectionPath);
                    buckets = new Bucket[cepBucketsCollection.getChildCount()];
                    int bucketCount = 0;
                    for (String bucketName : cepBucketsCollection.getChildren()) {
                        Bucket bucket = new Bucket();
                        if (registry.get(bucketName) instanceof Collection) {
                            Collection bucketDetailsCollection = (Collection) registry.get(bucketName);
                            bucket = formatBucket(registry, bucketDetailsCollection);
                        }
                        buckets[bucketCount] = bucket;
                        bucketCount++;
                    }
                }
            }
        } catch (RegistryException e) {
            String errorMessage = "Unable to load buckets from registry ";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }
        return buckets;
    }


    public static Bucket loadBucketFromRegistry(Registry registry, String bucketName)
            throws CEPConfigurationException {
        try {
            String bucketPath = CEPConstants.CEP_CONF_ELE_CEP_BUCKETS + CEPConstants.CEP_REGISTRY_BS + bucketName;
            if (registry.resourceExists(bucketPath)) {
                if (registry.get(bucketPath) instanceof Collection) {
                    Collection bucketDetailsCollection = (Collection) registry.get(bucketPath);
                    return formatBucket(registry, bucketDetailsCollection);
                }
            }
        } catch (RegistryException e) {
            String errorMessage = "Unable to load buckets from registry ";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }
        return null;
    }

    private static Bucket formatBucket(Registry registry, Collection bucketDetailsCollection)
            throws RegistryException, CEPConfigurationException {
        Bucket bucket = new Bucket();
        bucket.setName(bucketDetailsCollection.getProperty(CEPConstants.CEP_CONF_ELE_NAME));
        bucket.setOwner(bucketDetailsCollection.getProperty(CEPConstants.CEP_CONF_ELE_CEP_BUCKET_OWNER));
        bucket.setDescription(bucketDetailsCollection.getProperty(CEPConstants.CEP_CONF_ELE_DESCRIPTION));
        bucket.setEngineProvider(bucketDetailsCollection.getProperty(CEPConstants.CEP_CONF_ELE_CEP_ENGINE_PROVIDER));

        for (String attirbute : bucketDetailsCollection.getChildren()) {
            if (registry.get(attirbute) instanceof Collection) {
                Input input;
                Query query;
                Collection attributeCollection = (Collection) registry.get(attirbute);
                for (String names : attributeCollection.getChildren()) {
                    if (registry.get(names) instanceof Collection) {
                        if ((CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_INPUTS)
                                .equals(attirbute.substring(attirbute.lastIndexOf(CEPConstants.CEP_REGISTRY_BS)))) {
                            InputHelper.loadInputsFromRegistry(registry, bucket, names);
                        } else if ((CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_QUERIES)
                                .equals(attirbute.substring(attirbute.lastIndexOf(CEPConstants.CEP_REGISTRY_BS)))) {
                            QueryHelper.loadQueriesFromRegistry(registry, bucket, names);
                        }
                    }
                }
            }
        }
        return bucket;
    }


    public static void modifyBucketsInRegistry(Registry registry,
                                               Bucket bucket) throws CEPConfigurationException {
        try {
            String parentCollectionPath = CEPConstants.CEP_CONF_ELE_CEP_BUCKETS + CEPConstants.CEP_REGISTRY_BS + bucket.getName();
            if (registry.resourceExists(parentCollectionPath)) {
                InputHelper.modifyInputsInRegistry(registry, bucket, parentCollectionPath);

                QueryHelper.modifyQueriesInRegistry(registry, bucket, parentCollectionPath);

            }
        } catch (RegistryException e) {
            String errorMessage = "Error in modifying buckets in registry ";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }

    }
}
