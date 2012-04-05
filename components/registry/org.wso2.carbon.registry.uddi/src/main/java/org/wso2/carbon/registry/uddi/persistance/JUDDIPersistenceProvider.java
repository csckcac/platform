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
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.PersistenceProviderImpl;

import javax.persistence.spi.PersistenceUnitInfo;
import java.util.Map;


public class JUDDIPersistenceProvider extends PersistenceProviderImpl {

    private static Logger log = Logger.getLogger(JUDDIPersistenceProvider.class);

    public JUDDIPersistenceProvider() {
        super();
    }

    @Override
    public OpenJPAEntityManagerFactory createEntityManagerFactory(String name, String resource, Map m) {
        OpenJPAEntityManagerFactory factory = super.createEntityManagerFactory(name, resource, m);
        JUDDIEntityManagerFactory juddiEntityManagerFactory = new JUDDIEntityManagerFactory(factory);
        return juddiEntityManagerFactory;
    }

    @Override
    public OpenJPAEntityManagerFactory createEntityManagerFactory(String name, Map m) {
        OpenJPAEntityManagerFactory factory = super.createEntityManagerFactory(name, m);
        JUDDIEntityManagerFactory juddiEntityManagerFactory = new JUDDIEntityManagerFactory(factory);
        return juddiEntityManagerFactory;
    }

    @Override
    public OpenJPAEntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo pui, Map m) {
        OpenJPAEntityManagerFactory factory = super.createContainerEntityManagerFactory(pui, m);
        JUDDIEntityManagerFactory juddiEntityManagerFactory = new JUDDIEntityManagerFactory(factory);
        return juddiEntityManagerFactory;
    }
}
