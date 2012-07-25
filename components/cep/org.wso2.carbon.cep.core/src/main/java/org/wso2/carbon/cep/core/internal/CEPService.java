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

package org.wso2.carbon.cep.core.internal;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cep.core.Bucket;
import org.wso2.carbon.cep.core.BucketBasicInfo;
import org.wso2.carbon.cep.core.CEPServiceInterface;
import org.wso2.carbon.cep.core.Query;
import org.wso2.carbon.cep.core.backend.CEPBackEndRuntime;
import org.wso2.carbon.cep.core.backend.CEPBackEndRuntimeFactory;
import org.wso2.carbon.cep.core.backend.CEPEngineProvider;
import org.wso2.carbon.cep.core.exception.CEPConfigurationException;
import org.wso2.carbon.cep.core.internal.ds.CEPServiceValueHolder;
import org.wso2.carbon.cep.core.internal.registry.CEPRegistryInvoker;
import org.wso2.carbon.cep.core.mapping.input.Input;
import org.wso2.carbon.cep.core.mapping.input.mapping.InputMapping;
import org.wso2.carbon.context.CarbonContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CEPService implements CEPServiceInterface {

    private static final Log log = LogFactory.getLog(CEPService.class);

    private Map<Integer, Map<String, CEPBucket>> tenantSpecificCEPBuckets;

    private Map<String, CEPEngineProvider> cepEngineProviderMap;

    /**
     * if the corresponding cep engine provider is not available when the
     * buckets are added then we keep such buckets to deploy when the corresponding
     * cep back end runtime get registered with the cep service.
     */
    private List<Bucket> unDeployedBuckets;

    private AxisConfiguration axisConfiguration;

    public CEPService() throws CEPConfigurationException {
        this.cepEngineProviderMap = new ConcurrentHashMap<String, CEPEngineProvider>();
        this.unDeployedBuckets = new ArrayList<Bucket>();
        this.tenantSpecificCEPBuckets = new ConcurrentHashMap<Integer, Map<String, CEPBucket>>();
    }

    public void registerCEPEngineProvider(CEPEngineProvider cepEngineProvider)
            throws CEPConfigurationException {
        this.cepEngineProviderMap.put(cepEngineProvider.getName(), cepEngineProvider);
        //deploy the undeployed buckets if they use this cep engine provider
        for (Bucket bucket : unDeployedBuckets) {
            if (bucket.getEngineProvider().equals(cepEngineProvider.getName())) {
                addBucket(bucket, cepEngineProvider, axisConfiguration);
            }
        }
    }

    public void addBucket(Bucket bucket, AxisConfiguration axisConfiguration) throws CEPConfigurationException {

        CEPEngineProvider cepEngineProvider;
        this.axisConfiguration = axisConfiguration;
        if (bucket.getEngineProvider() == null) {
            String errorMessage = "No cep engine provider is given";
            log.error(errorMessage);
            throw new CEPConfigurationException(errorMessage);
        } else {
            cepEngineProvider = this.cepEngineProviderMap.get(bucket.getEngineProvider());
            if (cepEngineProvider == null) {
                // we save this bucket to unDeployed Buckets list and simply return
                this.unDeployedBuckets.add(bucket);
                return;
            }
        }

        addBucket(bucket, cepEngineProvider, axisConfiguration);
    }

    private void addBucket(Bucket bucket,
                           CEPEngineProvider cepEngineProvider,
                           AxisConfiguration axisConfiguration)
            throws CEPConfigurationException {
        try {
            int tenantId = CarbonContext.getCurrentContext().getTenantId();
            String owner = CarbonContext.getCurrentContext().getUsername();
            if(owner != null && owner.length() > 0){
                bucket.setOwner(owner);
            }

            CEPBackEndRuntimeFactory cepBackEndRuntimeFactory =
                    (CEPBackEndRuntimeFactory) cepEngineProvider.getProviderClass().newInstance();

            List<InputMapping> inputMappings = new ArrayList<InputMapping>();
            if (bucket.getInputs() != null) {
                for (Input input : bucket.getInputs()) {
                    inputMappings.add(input.getInputMapping());
                }
            }

            CEPBackEndRuntime cepBackEndRuntime =
                    cepBackEndRuntimeFactory.createCEPBackEndRuntime(bucket.getName(),bucket.getProviderConfiguration(), inputMappings, tenantId);

            CEPBucket cepBucket = new CEPBucket(cepBackEndRuntime, bucket, axisConfiguration);
            cepBucket.init();
            CEPRegistryInvoker.addBucketsToRegistry(bucket, tenantId);

            Map<String, CEPBucket> buckets = this.tenantSpecificCEPBuckets.get(tenantId);
            if (buckets == null) {
                buckets = new ConcurrentHashMap<String, CEPBucket>();
                buckets.put(bucket.getName(), cepBucket);
                this.tenantSpecificCEPBuckets.put(tenantId, buckets);
            } else {
                buckets.put(bucket.getName(), cepBucket);
            }
            log.debug("Added bucket to the cep engine successfully");

        } catch (InstantiationException e) {
            String errorMessage = "Can not instantiate factory class ";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        } catch (IllegalAccessException e) {
            String errorMessage = "Error in adding buckets";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }
    }


    public void editBucket(Bucket newBucket) throws CEPConfigurationException {
        try {
            int tenantId = CarbonContext.getCurrentContext().getTenantId();
            Map<String, CEPBucket> existingBuckets = this.tenantSpecificCEPBuckets.get(tenantId);
            CEPBucket existingBucket = existingBuckets.get(newBucket.getName());

            //adding new inputs
            for (Input input : newBucket.getInputs()) {
                if (!existingBucket.getBucket().getInputs().contains(input)) {
                    existingBucket.processInput(input);
                    existingBucket.getBucket().addInput(input);
                }
            }
            //removing old inputs
            List<Input> inputsToRemove = new ArrayList<Input>();
            for (Input input : existingBucket.getBucket().getInputs()) {
                if (!newBucket.getInputs().contains(input)) {
                    existingBucket.unSubscribeFromInput(input);
                    inputsToRemove.add(input);
                }
            }

            // remove the non existing inputs
            for (Input inputToRemove : inputsToRemove){
                existingBucket.getBucket().getInputs().remove(inputToRemove);
            }

            //adding new queries
            for (Query query : newBucket.getQueries()) {
                if(!existingBucket.getBucket().getQueries().contains(query)){
                    existingBucket.processQuery(query);
                    existingBucket.getBucket().addQuery(query);
                }
            }
            
            //removing old queries
            Map<Integer, Query> existingQueries = existingBucket.getBucket().getQueriesMap();
            for (Map.Entry<Integer, Query> existingEntry : existingQueries.entrySet()) {

                if(!newBucket.getQueries().contains(existingEntry.getValue())){
                    existingBucket.removeQuery(existingEntry.getValue());
                    existingQueries.remove(existingEntry.getKey());
                }
            }

            CEPRegistryInvoker.modifyBucketInRegistry(existingBucket.getBucket(), tenantId);
        } catch (CEPConfigurationException e) {
            String errorMessage = "Can not edit the existing bucket";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }

    }

    public Bucket getBucket(String bucketName) throws CEPConfigurationException {
        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        return this.tenantSpecificCEPBuckets.get(tenantId).get(bucketName).getBucket();
    }

    public boolean removeBucket(String bucketName) throws CEPConfigurationException {
        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        Map<String, CEPBucket> buckets = this.tenantSpecificCEPBuckets.get(tenantId);
        if (buckets.containsKey(bucketName)) {
            CEPBucket cepBucket = buckets.remove(bucketName);
            cepBucket.delete();
            CEPRegistryInvoker.removeBucketFromRegistry(bucketName, tenantId);
            return true;
        } else {
            return false;
        }
    }

    public boolean removeAllBuckets() throws CEPConfigurationException {
        try {
            int tenantId = CarbonContext.getCurrentContext().getTenantId();
            Map <String, CEPBucket> tenantBuckets = this.tenantSpecificCEPBuckets.get(tenantId);
            for (CEPBucket cepBucket : tenantBuckets.values()){
                removeBucket(cepBucket.getBucket().getName());
            }
            return true;
        } catch (Exception e) {
            throw new CEPConfigurationException("Error in removing all the buckets " + e);
        }
    }

    public List<BucketBasicInfo> getBucketList() throws CEPConfigurationException {
        List<CEPBucket> cepBucketList;
        List<BucketBasicInfo> bucketBasicInfoList = new ArrayList<BucketBasicInfo>();
        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        if (this.tenantSpecificCEPBuckets.get(tenantId) != null) {
            cepBucketList = new ArrayList<CEPBucket>(this.tenantSpecificCEPBuckets.get(
                    tenantId).values());
            for (CEPBucket cepBucket : cepBucketList) {
                BucketBasicInfo bucketBasicInfo = new BucketBasicInfo();
                bucketBasicInfo.setName(cepBucket.getBucket().getName());
                bucketBasicInfo.setDescription(cepBucket.getBucket().getDescription());
                bucketBasicInfoList.add(bucketBasicInfo);
            }
        }
        return bucketBasicInfoList;
    }

    public String[] getCEPEngineProviders() throws CEPConfigurationException {
        Set engineProviders = cepEngineProviderMap.keySet();
        String[] providers = new String[engineProviders.size()];
        engineProviders.toArray(providers);
        return providers;
    }

    @Override
    public String[] getCEPEngineProviderConfigNames(String providerName)
            throws CEPConfigurationException {
       return cepEngineProviderMap.get(providerName).getConfigurationPropertyNames().toArray(new String[cepEngineProviderMap.get(providerName).getConfigurationPropertyNames().size()]);
    }

    public String[] getCEPBrokerNames() throws CEPConfigurationException {
        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        List<String> brokerConfigurationNames = CEPServiceValueHolder.getInstance().getBrokerManagerService().
                getAllBrokerConfigurationNames(tenantId);
        String[] brokerNames = new String[brokerConfigurationNames.size()];
        brokerConfigurationNames.toArray(brokerNames);
        return brokerNames;
    }

    public boolean removeQuery(String bucketName, String queryName)
            throws CEPConfigurationException {
        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        Map<String, CEPBucket> buckets = this.tenantSpecificCEPBuckets.get(tenantId);
        if (buckets.containsKey(bucketName)) {
            CEPBucket cepBucket = buckets.get(bucketName);
            if (cepBucket.getBucket().getQuery(queryName) != null) {
                cepBucket.removeQuery(cepBucket.getBucket().getQuery(queryName));
                cepBucket.getBucket().removeQuery(queryName);
                CEPRegistryInvoker.removeQueryFromRegistry(bucketName, queryName, tenantId);
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean removeAllQueries(String bucketName)
            throws CEPConfigurationException {
        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        Map<String, CEPBucket> buckets = this.tenantSpecificCEPBuckets.get(tenantId);
        if (buckets.containsKey(bucketName)) {
            CEPBucket cepBucket = buckets.get(bucketName);
            cepBucket.removeAllQueries();
            cepBucket.getBucket().getQueries().clear();
            return true;
        } else {
            return false;
        }
    }

    public boolean editQuery(String bucketName, Query query)
            throws CEPConfigurationException {
        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        Map<String, CEPBucket> buckets = this.tenantSpecificCEPBuckets.get(tenantId);
        if (buckets.containsKey(bucketName)) {
            CEPBucket cepBucket = buckets.get(bucketName);
            cepBucket.editQuery(query);
            return true;
        } else {
            return false;
        }
    }

    public boolean removeInput(String bucketName, String inputTopic)
            throws CEPConfigurationException {
        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        Map<String, CEPBucket> buckets = this.tenantSpecificCEPBuckets.get(tenantId);
        if (buckets.containsKey(bucketName)) {
            CEPBucket cepBucket = buckets.get(bucketName);
            if (cepBucket.getBucket().getInput(inputTopic) != null) {
                cepBucket.unSubscribeFromInput(cepBucket.getBucket().getInput(inputTopic));
                cepBucket.getBucket().removeInput(inputTopic);
                CEPRegistryInvoker.removeInputFromRegistry(bucketName, inputTopic, tenantId);
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean removeAllInputs(String bucketName)
            throws CEPConfigurationException {
        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        Map<String, CEPBucket> buckets = this.tenantSpecificCEPBuckets.get(tenantId);
        if (buckets.containsKey(bucketName)) {
            CEPBucket cepBucket = buckets.get(bucketName);
            cepBucket.unSubscribeFromAllInputs();
            cepBucket.getBucket().getInputs().clear();
            return true;
        } else {
            return false;
        }
    }

}
