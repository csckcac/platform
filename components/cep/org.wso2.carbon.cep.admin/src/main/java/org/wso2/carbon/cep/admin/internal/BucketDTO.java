package org.wso2.carbon.cep.admin.internal;

/**
 * this class is used to send the buck data to front end
 */

/**
 * this class is used to send the buck data to front end
 */
public class BucketDTO {

    /**
     * name of the bucket
     */
    private String name;

    /**
     * description of the bucket
     */
    private String description;

    /**
     * engine provider to use with this bucket
     */
    private String engineProvider;

    /**
     * engine provider to use with this bucket
     */
    private CEPEngineProviderConfigPropertyDTO[] engineProviderConfigProperty;

    /**
     * query list of this bucket.
     */
    private QueryDTO[] queryDTOs;


    /**
     * Inputs for this bucket
     * */
    private InputDTO[] inputDTOs;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEngineProvider() {
        return engineProvider;
    }

    public void setEngineProvider(String engineProvider) {
        this.engineProvider = engineProvider;
    }


    public QueryDTO[] getQueries() {
        return queryDTOs;
    }

    public void setQueries(QueryDTO[] queryDTOs) {
        this.queryDTOs = queryDTOs;
    }

    public InputDTO[] getInputs() {
        return inputDTOs;
    }

    public void setInputs(InputDTO[] inputDTOs) {
        this.inputDTOs = inputDTOs;
    }

    public CEPEngineProviderConfigPropertyDTO[] getEngineProviderConfigProperty() {
        return engineProviderConfigProperty;
    }

    public void setEngineProviderConfigProperty(CEPEngineProviderConfigPropertyDTO[] engineProviderConfigProperty) {
        this.engineProviderConfigProperty = engineProviderConfigProperty;
    }
}