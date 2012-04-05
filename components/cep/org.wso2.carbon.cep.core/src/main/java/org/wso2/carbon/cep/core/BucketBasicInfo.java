package org.wso2.carbon.cep.core;


/**
 * This class contains the vary basic information of the bucket
 * */
public class BucketBasicInfo {
   /**
    * Name of the bucket
    * */
   private String  name;

    /**
     * Description about the bucket
     * */
   private String description;


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
}
