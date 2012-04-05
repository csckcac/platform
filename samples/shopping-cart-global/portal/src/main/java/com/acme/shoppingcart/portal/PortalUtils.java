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
package com.acme.shoppingcart.portal;

import org.wso2.carbon.context.CarbonContext;

import javax.activation.DataHandler;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Utilities
 */
public class PortalUtils {
    private static Map<String, DataHandler> imageMap = new HashMap<String, DataHandler>();

    private static Properties props = new Properties();

    static {
        try {
            ClassLoader loader = PortalUtils.class.getClassLoader();
            String propFileStr = "acme.shopping.cart.eprs.properties";
            URL url = loader.getResource(propFileStr);
            props.load(url.openStream());

        } catch (Throwable ignored) {
            // If there is no properties file, we'll simply rely on WS-D to work.
        }
    }

    public static String getProperty(String key) {
        return props.getProperty(key);
    }

    public static String getEndpoint(String key) {
        String scope = null;
        if ("products.proxy.service".equals(key)) {
            scope = "http://shopping-cart.samples.stratos.wso2.org/service/products/proxy";
        } else if ("purchasing.proxy.service".equals(key)) {
            scope = "http://shopping-cart.samples.stratos.wso2.org/service/purchasing/proxy";
        } else if ("related.products.epr".equals(key)) {
            scope = "http://shopping-cart.samples.stratos.wso2.org/service/relatedproducts";
        }
        try {
            if (scope != null) {
                String[] endpoints = CarbonContext.getCurrentContext().discover(
                        new URI[]{new URI(scope)});
                if (endpoints != null) {
                    for (String endpoint : endpoints) {
                        if (endpoint.startsWith("http://")) {
                            return endpoint;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
            // If we are unable to fetch the EPR, we'll be using the properties file.
        }
        return getProperty(key);
    }
    
    public static void addImage(String productCode, DataHandler image) {
        imageMap.put(productCode, image);
    }

    public static DataHandler getImage(String productCode) {
        return imageMap.get(productCode);
    }
}
