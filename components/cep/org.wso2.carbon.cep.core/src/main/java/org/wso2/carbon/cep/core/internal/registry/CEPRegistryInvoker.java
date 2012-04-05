package org.wso2.carbon.cep.core.internal.registry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cep.core.Bucket;
import org.wso2.carbon.cep.core.exception.CEPConfigurationException;
import org.wso2.carbon.cep.core.internal.config.BucketHelper;
import org.wso2.carbon.cep.core.internal.ds.CEPServiceValueHolder;
import org.wso2.carbon.cep.core.internal.util.CEPConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

/**
 * this class is used to invoke the registry
 * for CEP Operations
 */
public class CEPRegistryInvoker {

    private static final Log log = LogFactory.getLog(CEPRegistryInvoker.class);

    /**
     * This method is used to load the buckets from registry
     *
     * @return Array of buckets
     */

    public static Bucket[] loadBucketsFromRegistry(int tenantId) throws CEPConfigurationException {
        Bucket[] buckets = null;
        Registry registry = null;
        try {
            registry = CEPServiceValueHolder.getInstance().getRegistry(tenantId);
        } catch (RegistryException e) {
            String errorMessage = "Error in getting registry specific to tenant :" + tenantId;
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }
        buckets = BucketHelper.loadBucketsFromRegistry(registry);
        if (log.isDebugEnabled()) {
            log.debug("Loaded buckets from the registry successfully");
        }
        return buckets;
    }

    /**
     * This method is used to load the given bucket from registry
     *
     * @return Bucket
     */

    public static Bucket loadBucketFromRegistry(int tenantId,String bucketName) throws CEPConfigurationException {
        Bucket bucket=null;
        Registry registry = null;
        try {
            registry = CEPServiceValueHolder.getInstance().getRegistry(tenantId);
        } catch (RegistryException e) {
            String errorMessage = "Error in getting registry specific to tenant :" + tenantId;
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }
        bucket = BucketHelper.loadBucketFromRegistry(registry,bucketName);
        if (log.isDebugEnabled()) {
            log.debug("Loaded "+bucketName+" from the registry successfully");
        }
        return bucket;
    }



    /**
     * this method is used to add a given
     * bucket to the config registry
     *
     * @param bucket
     */
    public static void addBucketsToRegistry(Bucket bucket, int tenantId) throws CEPConfigurationException {

        try {
            Registry registry = CEPServiceValueHolder.getInstance().getRegistry(tenantId);

            String parentCollectionPath = CEPConstants.CEP_CONF_ELE_CEP_BUCKETS + CEPConstants.CEP_REGISTRY_BS + bucket.getName();
            if (registry.resourceExists(parentCollectionPath) == true) {
                if (!bucket.isOverWriteRegistry()) {
                    return;
                } else {
                    registry.delete(parentCollectionPath);
                }
            }


            BucketHelper.addBucketToRegistry(bucket, registry, parentCollectionPath);
        } catch (RegistryException e) {
            String errorMessage = "Can not add bucket to the registry ";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Added the bucket to the registry successfully");
        }
    }

    /**
     * When there are new queries or inputs for an existing bucket
     * This method will modify the registry entire related to that existing bucket by
     * add new Inputs and New Queries
     *
     * @param bucket
     */
    public static void modifyBucketInRegistry(Bucket bucket, int tenantId) throws CEPConfigurationException {
        try {
            Registry registry = CEPServiceValueHolder.getInstance().getRegistry(tenantId);
            BucketHelper.modifyBucketsInRegistry(registry, bucket);
        } catch (RegistryException e) {
            String errorMessage = "Can not modify the bucket in registry ";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }
        log.debug("Modified the bucket successfully");
    }

    /**
     * this method is used to remove all the buckets
     * from registry
     */
    public static void removeAllBucketsFromRegistry(int tenantId) throws CEPConfigurationException {
        try {
            Registry registry = CEPServiceValueHolder.getInstance().getRegistry(tenantId);
            registry.delete(CEPConstants.CEP_CONF_ELE_CEP_BUCKETS);
        } catch (RegistryException e) {
            String errorMessage = "Error in removing all buckets from registry ";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }
    }

    /**
     * This method is used to remove a given bucket
     * from registry
     *
     * @param bucketName
     */
    public static void removeBucketFromRegistry(String bucketName, int tenantId) throws CEPConfigurationException {
        try {
            Registry registry = CEPServiceValueHolder.getInstance().getRegistry(tenantId);
            registry.delete(CEPConstants.CEP_CONF_ELE_CEP_BUCKETS + CEPConstants.CEP_REGISTRY_BS + bucketName);
        } catch (RegistryException e) {
            String errorMessage = "Error in removing bucket :" + bucketName + " from registry ";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }
    }

    /**
     * This method will remove the specified query from registry if exists
     *
     * @param bucketName -Name of the bucket which the query exists
     * @param queryName  - Name of the query to be deleted
     */
    public static void removeQueryFromRegistry(String bucketName, String queryName, int tenantId) throws CEPConfigurationException {
        String parentCollectionPath = CEPConstants.CEP_CONF_ELE_CEP_BUCKETS + CEPConstants.CEP_REGISTRY_BS + bucketName;
        String queriesCollectionPath = parentCollectionPath + CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_QUERIES;
        String queryPath = queriesCollectionPath + "/" + queryName;
        try {
            Registry registry = CEPServiceValueHolder.getInstance().getRegistry(tenantId);
            if (registry.resourceExists(queryPath)) {
                registry.delete(queryPath);
            }
        } catch (RegistryException e) {
            String errorMessage = "Error in deleting the query to be deleted ";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }
    }

    /**
     * This method will remove the specified Input from registry if exists
     *
     * @param bucketName - Name of the bucket which input exists
     * @param inputTopic - topic to be deleted from registry
     */
    public static void removeInputFromRegistry(String bucketName, String inputTopic, int tenantId) throws CEPConfigurationException {
        String parentCollectionPath = CEPConstants.CEP_CONF_ELE_CEP_BUCKETS + CEPConstants.CEP_REGISTRY_BS + bucketName;
        String inputsCollectionPath = parentCollectionPath + CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_INPUTS;
        String inputPath = inputsCollectionPath + "/" + inputTopic;
        try {
            Registry registry = CEPServiceValueHolder.getInstance().getRegistry(tenantId);
            if (registry.resourceExists(inputPath)) {
                registry.delete(inputPath);
            }
        } catch (RegistryException e) {
            String errorMessage = "Error in deleting the input to be deleted ";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }
    }
}
