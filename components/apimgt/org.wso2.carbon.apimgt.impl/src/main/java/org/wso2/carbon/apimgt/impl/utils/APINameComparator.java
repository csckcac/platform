package org.wso2.carbon.apimgt.impl.utils;

import org.wso2.carbon.apimgt.api.model.API;

import java.util.Comparator;
/**
 * This comparator used to order APIs by name.
 */
public class APINameComparator implements Comparator<API> {

    public int compare(API api1, API api2) {
        return api1.getId().getApiName().compareTo(api2.getId().getApiName());
    }
}
