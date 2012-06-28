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

package org.wso2.carbon.cep.core.internal.builder;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cep.core.Bucket;
import org.wso2.carbon.cep.core.CEPServiceInterface;
import org.wso2.carbon.cep.core.exception.CEPConfigurationException;
import org.wso2.carbon.cep.core.internal.config.BucketHelper;
import org.wso2.carbon.cep.core.internal.registry.CEPRegistryInvoker;
import org.wso2.carbon.cep.core.internal.util.CEPConstants;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;

import javax.xml.namespace.QName;
import java.util.Iterator;

/**
 * this class is used to add CEP buckets to cep runtime engine
 */
public class CEPBucketBuilder {
    private static final Log log = LogFactory.getLog(CEPBucketBuilder.class);

    /**
     * This method is used to iterate through the buckets XML Element and if there are bucket available it will
     * invoke there registry invoker class and hand over the bucket object to that class to put
     * in to the registry
     *
     * @param bucketsElement - Buckets XML Element
     */
    public static void addNewBucketsToRegistry(OMElement bucketsElement,int tenantId) throws CEPConfigurationException {
        // add the buckets through an iterator
        OMElement bucketElement;
        for (Iterator bucketsIterator = bucketsElement.getChildElements();
             bucketsIterator.hasNext(); ) {
            bucketElement = (OMElement) bucketsIterator.next();
            addNewBucketToRegistry(bucketElement,tenantId);
        }
    }

    /**
     * This method is used to check for a bucket XML Element and will invoke the registry invoker class and
     * hand over the bucket object to that class to put in to the registry
     *
     * @param bucketElement - Bucket XML Element
     */
    public static void addNewBucketToRegistry(OMElement bucketElement,int tenantId)
            throws CEPConfigurationException {
        if (!bucketElement.getQName().equals(
                new QName(CEPConstants.CEP_CONF_NAMESPACE, CEPConstants.CEP_CONF_ELE_BUCKET))) {

            String errorMessage = "Invalid Element " + bucketElement.getQName()
                                  + "under buckets element";
            log.error(errorMessage);
            throw new CEPConfigurationException(errorMessage);
        }

        Bucket bucket = BucketHelper.fromOM(bucketElement);
        CEPRegistryInvoker.addBucketsToRegistry(bucket, tenantId);
    }

    /**
     * this method is used to load buckets from Registry  of a particular tenant with he given axisConfiguration
     * and add them to the CEP Runtime Engine
     *
     * @param cepService        - CEP ServiceInterface to add buckets
     * @param axisConfiguration - Axis Configuration of the tenant
     */
    public static void loadBucketsFromRegistry(CEPServiceInterface cepService, AxisConfiguration axisConfiguration)
            throws CEPConfigurationException {
        int tenantId = SuperTenantCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        Bucket[] buckets = CEPRegistryInvoker.loadBucketsFromRegistry(tenantId);
        if (buckets != null) {
            for (Bucket bucket : buckets) {
                cepService.addBucket(bucket, axisConfiguration);
            }
        }
    }

    /**
     * this method is used to load buckets from Registry  of a particular tenant with he given axisConfiguration
     * and add them to the CEP Runtime Engine
     *
     * @param cepService        - CEP ServiceInterface to add buckets
     * @param axisConfiguration - Axis Configuration of the tenant
     * @param bucketName        - Name of the bucket to be loaded
     */
    public static void loadBucketFromRegistry(CEPServiceInterface cepService,
                                              AxisConfiguration axisConfiguration,
                                              String bucketName)
            throws CEPConfigurationException {
        int tenantId = SuperTenantCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        Bucket bucket = CEPRegistryInvoker.loadBucketFromRegistry(tenantId, bucketName);
        if (bucket != null) {
            cepService.addBucket(bucket, axisConfiguration);
        }
    }
}
