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
package org.wso2.carbon.apimgt.api.model;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Provider's & system's view of API
 */
@SuppressWarnings("unused")
public class API {

    private APIIdentifier id;

    private String description;
    private String url;
    private String sandboxUrl;
    private String wsdlUrl;
    private String wadlUrl;
    private String context;
    private String thumbnailUrl;
    private Set<String> tags = new LinkedHashSet<String>();
    private Set<Documentation> documents = new LinkedHashSet<Documentation>();
    private String httpVerb;
    private Date lastUpdated;
    private Set<Tier> availableTiers = new LinkedHashSet<Tier>();
    private AuthorizationPolicy authorizationPolicy;
    private Set<URITemplate> uriTemplates = new LinkedHashSet<URITemplate>();

    private APIStatus status;

    private String technicalOwner;
    private String technicalOwnerEmail;
    private String businessOwner;
    private String businessOwnerEmail;

    public String getTechnicalOwner() {
        return technicalOwner;
    }

    public void setTechnicalOwner(String technicalOwner) {
        this.technicalOwner = technicalOwner;
    }

    public String getTechnicalOwnerEmail() {
        return technicalOwnerEmail;
    }

    public void setTechnicalOwnerEmail(String technicalOwnerEmail) {
        this.technicalOwnerEmail = technicalOwnerEmail;
    }

    public String getBusinessOwner() {
        return businessOwner;
    }

    public void setBusinessOwner(String businessOwner) {
        this.businessOwner = businessOwner;
    }

    public String getBusinessOwnerEmail() {
        return businessOwnerEmail;
    }

    public void setBusinessOwnerEmail(String businessOwnerEmail) {
        this.businessOwnerEmail = businessOwnerEmail;
    }
    /**
     * The average rating provided by the API subscribers
     */
    private float rating;

    private boolean isLatest;

    //TODO: missing - total user count, up time statistics,tier


    public API(APIIdentifier id) {
        this.id = id;
    }

    public APIIdentifier getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSandboxUrl() {
        return sandboxUrl;
    }

    public void setSandboxUrl(String sandboxUrl) {
        this.sandboxUrl = sandboxUrl;
    }

    public String getWsdlUrl() {
        return wsdlUrl;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getContext() {
        return context;
    }

    public void setWsdlUrl(String wsdlUrl) {
        this.wsdlUrl = wsdlUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public Set<String> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    public void addTags(Set<String> tags) {
        this.tags.addAll(tags);
    }

    public void removeTags(Set<String> tags) {
        this.tags.removeAll(tags);
    }

    public Set<Documentation> getDocuments() {
        return Collections.unmodifiableSet(documents);
    }

    public void addDocuments(Set<Documentation> documents) {
        this.documents.addAll(documents);
    }

    public void removeDocuments(Set<Documentation> documents) {
        this.documents.removeAll(documents);
    }

    public String getHttpVerb() {
        return httpVerb;
    }

    public void setHttpVerb(String httpVerb) {
        this.httpVerb = httpVerb;
    }

    public Date getLastUpdated() {
        return new Date(lastUpdated.getTime());
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = new Date(lastUpdated.getTime());
    }

    public Set<Tier> getAvailableTiers() {
        return Collections.unmodifiableSet(availableTiers);
    }

    public void addAvailableTiers(Set<Tier> availableTiers) {
        this.availableTiers.addAll(availableTiers);
    }

    public void removeAvailableTiers(Set<Tier> availableTiers) {
        this.availableTiers.removeAll(availableTiers);
    }

    public Set<URITemplate> getUriTemplates() {
        return uriTemplates;
    }

    public void setUriTemplates(Set<URITemplate> uriTemplates) {
        this.uriTemplates = uriTemplates;
    }

    public APIStatus getStatus() {
        return status;
    }

    public void setStatus(APIStatus status) {
        this.status = status;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public void setLatest(boolean latest) {
        isLatest = latest;
    }

    /**
     * @return true if the current version of the API is the latest
     */
    public boolean isLatest() {
        return isLatest;
    }

    public AuthorizationPolicy getAuthorizationPolicy() {
        return authorizationPolicy;
    }

    public void setAuthorizationPolicy(AuthorizationPolicy authorizationPolicy) {
        this.authorizationPolicy = authorizationPolicy;
    }

    public String getWadlUrl() {
        return wadlUrl;
    }

    public void setWadlUrl(String wadlUrl) {
        this.wadlUrl = wadlUrl;
    }
}
