/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.uddi.persistance;

import org.apache.log4j.Logger;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.QueryResultCache;
import org.apache.openjpa.persistence.StoreCache;


import java.util.Map;
import java.util.Properties;


public class JUDDIEntityManagerFactory implements OpenJPAEntityManagerFactory {

    OpenJPAEntityManagerFactory factory;

    private static Logger log = Logger.getLogger(JUDDIEntityManagerFactory.class);

    public JUDDIEntityManagerFactory(OpenJPAEntityManagerFactory openJPAEntityManagerFactory) {
        super();
        factory = openJPAEntityManagerFactory;
    }

    public Properties getProperties() {
        return factory.getProperties();
    }

    public Object putUserObject(Object o, Object o1) {
        return factory.putUserObject(o, o1);
    }

    public Object getUserObject(Object o) {
        return factory.getUserObject(o);
    }

    public StoreCache getStoreCache() {
        return factory.getStoreCache();
    }

    public StoreCache getStoreCache(String s) {
        return factory.getStoreCache(s);
    }

    public QueryResultCache getQueryResultCache() {
        return factory.getQueryResultCache();
    }

    public OpenJPAEntityManager createEntityManager() {
        OpenJPAEntityManager openJPAEntityManager = factory.createEntityManager();
        JUDDIEntityManager juddiEntityManager = new JUDDIEntityManager(openJPAEntityManager);
        return juddiEntityManager;
    }

    public OpenJPAEntityManager createEntityManager(Map map) {
        OpenJPAEntityManager openJPAEntityManager = factory.createEntityManager(map);
        JUDDIEntityManager juddiEntityManager = new JUDDIEntityManager(openJPAEntityManager);
        return juddiEntityManager;
    }

    public void close() {
        factory.close();
    }

    public boolean isOpen() {
        return factory.isOpen();
    }

    /**
     * @deprecated
     */
    public OpenJPAConfiguration getConfiguration() {
        return factory.getConfiguration();
    }

    /**
     * @deprecated
     */
    public void addLifecycleListener(Object o, Class... classes) {
        factory.addLifecycleListener(o, classes);
    }

    /**
     * @deprecated
     */
    public void removeLifecycleListener(Object o) {
        factory.removeLifecycleListener(o);
    }

    /**
     * @deprecated
     */
    public void addTransactionListener(Object o) {
        factory.addTransactionListener(o);
    }

    /**
     * @deprecated
     */
    public void removeTransactionListener(Object o) {
        factory.removeTransactionListener(o);
    }
}
