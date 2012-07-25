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

package org.wso2.carbon.cep.core;

import org.wso2.carbon.cep.core.mapping.input.Input;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * this class is used to send the buck data to front end
 */
public class Bucket {

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
     * query list of this bucket.
     */
    private Map<Integer,Query> queries;

    /**
     * Inputs for this bucket
     * */
    private List<Input> inputs;

    /**
     * Owner (Name of the person who created the bucket)
     * */
    private String owner;

    /**
     * Permission to over write the bucket details, if already stored in registry
     * */
    private boolean overWriteRegistry = false;

    /**
     * Configuration related to engine providers
     */
    private Properties providerConfiguration=null;


    public Bucket() {
        this.queries = new ConcurrentHashMap<Integer,Query>();
        this.inputs = new ArrayList<Input>();
    }

    public void addQuery(Query query){
        this.queries.put(query.getQueryIndex(), query);
    }

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


    public List<Query> getQueries() {
         return new ArrayList (queries.values());
    }

    public Map<Integer,Query> getQueriesMap(){
        return queries;
    }

    public void setQueries(List<Query> queries) {
        if (queries != null) {
            for (Query query : queries){
                this.queries.put(query.getQueryIndex(),query);
            }
        }
    }

    public List<Input> getInputs() {
        return inputs;
    }

    public void setInputs(List<Input> inputs) {
        this.inputs = inputs;
    }

    public void addInput(Input input){
        this.inputs.add(input);
    }
    public Input getInput (String topic){
        for(Input input : this.inputs){
            if(input.getTopic().equals(topic.trim())){
                return input;
            }
        }
        return null;
    }
    public void removeInput(String topic){
         int topicIndex = 0 ;
         for(Input input : this.inputs) {
             if (input.getTopic().equals(topic.trim())) {
                 break;
             }
             topicIndex++;
         }
        this.inputs.remove(topicIndex);
    }

    public Query getQuery (String name){
        for(Query query : this.queries.values()){
            if(query.getName().equals(name.trim())){
                return query;
            }
        }
        return null;
    }
    public void removeQuery(String name){
         int queryIndex = 0 ;
         for(Query query : this.queries.values()) {
             if (query.getName().equals(name.trim())) {
                 break;
             }
             queryIndex++;
         }
        this.queries.remove(queryIndex);
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean isOverWriteRegistry() {
        return overWriteRegistry;
    }

    public void setOverWriteRegistry(boolean overWriteRegistry) {
        this.overWriteRegistry = overWriteRegistry;
    }

    public void setProviderConfiguration(Properties providerConfiguration) {
        this.providerConfiguration = providerConfiguration;
    }

    public Properties getProviderConfiguration() {
        return providerConfiguration;
    }
}

