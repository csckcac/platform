/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.core.configurations;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.bam.core.dataobjects.Cursor;
import org.wso2.carbon.bam.core.dataobjects.Record;
import org.wso2.carbon.bam.core.internal.ServiceHolder;
import org.wso2.carbon.bam.core.persistence.PersistenceManager;
import org.wso2.carbon.bam.core.persistence.PersistencyConstants;
import org.wso2.carbon.bam.core.persistence.QueryIndex;
import org.wso2.carbon.bam.core.persistence.QueryManager;
import org.wso2.carbon.bam.core.persistence.exceptions.ConfigurationException;
import org.wso2.carbon.bam.core.persistence.exceptions.StoreException;
import org.wso2.carbon.bam.core.utils.Utils;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public abstract class AbstractMetaDataPersistor implements MetaDataPersistor {

    private static final String INDEX_REGISTRY_PATH = "indexes/";
    private static final String TABLE_REGISTRY_PATH = "tables/";
    private static final String CURSOR_REGISTRY_PATH = "cursors/";

/*    @Override
    public String getIndexRegistryPath() {
        return INDEX_REGISTRY_PATH;
    }

    @Override
    public String getTableRegistryPath() {
        return TABLE_REGISTRY_PATH;
    }

    @Override
    public String getCursorRegistryPath() {
        return CURSOR_REGISTRY_PATH;
    }*/

/*    @Override
    public final void persistIndexMetaData(int tenantId, IndexConfiguration configuration)
            throws ConfigurationException {
        try {
            updateIndexTracker(tenantId);
        } catch (RegistryException e) {
            throw new ConfigurationException("Unable to persist index meta data..", e);
        }

        persistIndex(tenantId, configuration);

    }*/

    public final void persistIndexMetaData(
            Map<String, String> credentials, IndexConfiguration configuration)
            throws ConfigurationException {

        int tenantId;
        try {
            tenantId = Utils.getTenantIdFromUserName(
                    credentials.get(PersistencyConstants.USER_NAME));
        } catch (StoreException e) {
            throw new ConfigurationException("Unable to obtain tenant information", e);
        }

        try {
            updateIndexTracker(tenantId);
        } catch (RegistryException e) {
            throw new ConfigurationException("Unable to persist index meta data..", e);
        }

        persistIndex(credentials, configuration);

    }

/*    public final void deleteIndexMetaData(int tenantId, String indexName)
            throws ConfigurationException {

        // Load tenant registry to ensure the mounts are created before getting the tenant registry
        ServiceHolder.getTenantRegistryLoader().loadTenantRegistry(tenantId);

        try {
            // Get the config registry of the tenant
            UserRegistry tenantConfigSystemRegistry = ServiceHolder.getRegistryService().
                    getConfigSystemRegistry(tenantId);

            String indexConfigurationPath = PersistencyConstants.COMPONENT_ROOT +
                                            getIndexRegistryPath() + indexName;

            if (tenantConfigSystemRegistry.resourceExists(indexConfigurationPath)) {
                tenantConfigSystemRegistry.delete(indexConfigurationPath);
            }

        } catch (RegistryException e) {
            throw new ConfigurationException("Unable to delete index meta data for index " +
                                             indexName + "..", e);
        }
    }*/

    public void deleteIndexMetaData(Map<String, String> credentials, String indexName)
            throws ConfigurationException {

        PersistenceManager persistenceManager = new PersistenceManager();
        try {
            persistenceManager.deleteRecord(credentials, PersistencyConstants.INDEX_INFO_TABLE,
                                            indexName);
        } catch (StoreException e) {
            throw new ConfigurationException("Unable to delete index meta data for index " +
                                             indexName + "..", e);
        }
    }

    public int[] getAllTenantsWithDefinedIndexes() throws ConfigurationException {
        int[] tenantArray;

        try {
            Integer[] tenantsWithIndexes = getTenantsWithIndexes();

            tenantArray = new int[tenantsWithIndexes.length];
            for (int i = 0; i < tenantsWithIndexes.length; i++) {
                tenantArray[i] = tenantsWithIndexes[i];
            }

        } catch (RegistryException e) {
            throw new ConfigurationException("Unable to fetch tenant index meta data from super " +
                                             "tenant registry..", e);
        }

        return tenantArray;

    }

/*    public void deleteTableMetaData(int tenantId, String tableName) throws ConfigurationException {
        // Load tenant registry to ensure the mounts are created before getting the tenant registry
        ServiceHolder.getTenantRegistryLoader().loadTenantRegistry(tenantId);

        try {
            // Get the config registry of the tenant
            UserRegistry tenantConfigSystemRegistry = ServiceHolder.getRegistryService().
                    getConfigSystemRegistry(tenantId);

            String tableResourcePath = PersistencyConstants.COMPONENT_ROOT +
                                       getTableRegistryPath() + tableName;

            if (tenantConfigSystemRegistry.resourceExists(tableResourcePath)) {
                tenantConfigSystemRegistry.delete(tableResourcePath);
            }

        } catch (RegistryException e) {
            throw new ConfigurationException("Unable to delete meta data for table " +
                                             tableName + "..", e);
        }
    }*/

    public void deleteTableMetaData(Map<String, String> credentials, String tableName)
            throws ConfigurationException {

        PersistenceManager persistenceManager = new PersistenceManager();
        try {
            persistenceManager.deleteRecord(credentials, PersistencyConstants.TABLE_INFO_TABLE,
                                            tableName);
        } catch (StoreException e) {
            throw new ConfigurationException("Unable to delete meta data for table " +
                                             tableName + "..", e);
        }

    }

/*
    public final void persistCursorMetaData(int tenantId, Cursor cursor)
            throws ConfigurationException {
        // Load tenant registry to ensure the mounts are created before getting the tenant registry
        ServiceHolder.getTenantRegistryLoader().loadTenantRegistry(tenantId);

        try {
            // Get the config registry of the tenant
            UserRegistry tenantConfigSystemRegistry = ServiceHolder.getRegistryService().
                    getConfigSystemRegistry(tenantId);

            initializeCollections(tenantConfigSystemRegistry);

            String table = cursor.getTable();
            String tableCursorPath = PersistencyConstants.COMPONENT_ROOT + getCursorRegistryPath() +
                                     table;

            if (!tenantConfigSystemRegistry.resourceExists(tableCursorPath)) {
                Resource tableCursorResource = tenantConfigSystemRegistry.newResource();
                tableCursorResource.setProperty(PersistencyConstants.NAME, table);

                tenantConfigSystemRegistry.put(tableCursorPath, tableCursorResource);

            }

            Resource tableCursorResource = tenantConfigSystemRegistry.get(tableCursorPath);
            tableCursorResource.setProperty(cursor.getCursorName(), cursor.getResumePoint());

            tenantConfigSystemRegistry.put(tableCursorPath, tableCursorResource);

        } catch (RegistryException e) {
            //rollback
            throw new ConfigurationException("Unable to store the cursor " +
                                             cursor.getCursorName() + "..", e);
        }
    }
*/

    public final void persistCursorMetaData(Map<String, String> credentials, Cursor cursor)
            throws ConfigurationException {

        Record record = new Record(cursor.getTable(), new HashMap<String, String>());
        record.addColumn(cursor.getCursorName(), cursor.getResumePoint());

        PersistenceManager persistenceManager = new PersistenceManager();
        try {
            persistenceManager.storeRecord(credentials, PersistencyConstants.CURSOR_INFO_TABLE,
                                           record);
        } catch (StoreException e) {
            throw new ConfigurationException("Unable to store the cursor " +
                                             cursor.getCursorName() + "..", e);
        }

    }

/*    public final List<Cursor> getAllCursorMetaData(int tenantId) throws ConfigurationException {
        // Load tenant registry to ensure the mounts are created before getting the tenant registry
        ServiceHolder.getTenantRegistryLoader().loadTenantRegistry(tenantId);

        List<Cursor> cursors = new ArrayList<Cursor>();
        try {
            // Get the config registry of the tenant
            UserRegistry tenantConfigSystemRegistry = ServiceHolder.getRegistryService().
                    getConfigSystemRegistry(tenantId);

            String tableCursorRootPath = PersistencyConstants.COMPONENT_ROOT +
                                         getCursorRegistryPath();

            if (tenantConfigSystemRegistry.resourceExists(tableCursorRootPath)) {
                Resource tableCursorRootResource = tenantConfigSystemRegistry.
                        get(tableCursorRootPath);
                Collection tableCursorCollection = (Collection) tableCursorRootResource;

                String[] tableResources = tableCursorCollection.getChildren();
                for (String tableResource : tableResources) {
                    Resource tableCursorResource = tenantConfigSystemRegistry.get(tableResource);
                    String table = tableCursorResource.getProperty(PersistencyConstants.NAME);

                    Properties cursorProperties = tableCursorResource.getProperties();
                    for (Map.Entry entry : cursorProperties.entrySet()) {
                        if (!entry.getKey().equals(PersistencyConstants.NAME)) {
                            String cursorName = entry.getKey().toString();
                            String resumePoint = entry.getValue().toString();

                            Cursor cursor = new Cursor(table, cursorName);
                            cursor.setResumePoint(resumePoint);

                            cursors.add(cursor);
                        }
                    }
                }
            }
        } catch (RegistryException e) {
            throw new ConfigurationException("Unable to fetch cursor meta data..", e);
        }

        return cursors;

    }*/

    public List<Cursor> getAllCursorMetaData(Map<String, String> credentials)
            throws ConfigurationException {

        QueryManager queryManager = new QueryManager();

        List<Cursor> cursors = new ArrayList<Cursor>();
        try {
            List<Record> records = queryManager.getRecords(
                    credentials, PersistencyConstants.CURSOR_INFO_TABLE, (QueryIndex) null, null);

            for (Record record : records) {
                String table = record.getKey();

                Map<String, String> columns = record.getColumns();
                for (Map.Entry<String, String> entry : columns.entrySet()) {
                    Cursor cursor = new Cursor(table, entry.getKey());
                    cursor.setResumePoint(entry.getValue());

                    cursors.add(cursor);
                }

            }
        } catch (StoreException e) {
            throw new ConfigurationException("Unable to fetch cursor meta data..", e);
        }

        return cursors;

    }

/*
    public final Cursor getCursorMetaData(int tenantId, String table, String cursorName)
            throws ConfigurationException {
        // Load tenant registry to ensure the mounts are created before getting the tenant registry
        ServiceHolder.getTenantRegistryLoader().loadTenantRegistry(tenantId);

        try {
            // Get the config registry of the tenant
            UserRegistry tenantConfigSystemRegistry = ServiceHolder.getRegistryService().
                    getConfigSystemRegistry(tenantId);

            String tableCursorPath = PersistencyConstants.COMPONENT_ROOT +
                                     getCursorRegistryPath() + table;

            if (tenantConfigSystemRegistry.resourceExists(tableCursorPath)) {
                Resource tableCursorResource = tenantConfigSystemRegistry.
                        get(tableCursorPath);
                Properties cursorProperties = tableCursorResource.getProperties();

                String resumePoint = null;
                if (cursorProperties.get(cursorName) != null) {
                    resumePoint = ((List<String>) cursorProperties.get(cursorName)).get(0);   // TODO: Revisit
                }

                if (resumePoint != null) {
                    Cursor cursor = new Cursor(table, cursorName);
                    cursor.setResumePoint(resumePoint);

                    return cursor;
                }

            }
        } catch (RegistryException e) {
            throw new ConfigurationException("Unable to fetch meta data for cursor " + cursorName +
                                             "..", e);
        }

        return null;

    }
*/

    public Cursor getCursorMetaData(Map<String, String> credentials, String table,
                                    String cursorName) throws ConfigurationException {

        QueryManager queryManager = new QueryManager();

        try {
            List<Record> records = queryManager.getRecords(
                    credentials, PersistencyConstants.CURSOR_INFO_TABLE, table, null);

            if (records != null && records.size() > 0) {
                Record record = records.get(0);  // Only one record per cursor details of a table

                Map<String, String> columns = record.getColumns();
                String resumePoint = columns.get(cursorName);

                Cursor cursor = new Cursor(table, cursorName);
                cursor.setResumePoint(resumePoint);

                return cursor;
            }
        } catch (StoreException e) {
            throw new ConfigurationException("Unable to fetch meta data for cursor " + cursorName +
                                             "..", e);
        }

        return null;

    }

    public void deleteCursorMetaData(Map<String, String> credentials, Cursor cursor)
            throws ConfigurationException {

        Record record = new Record(cursor.getTable(), new HashMap<String, String>());
        record.addColumn(cursor.getCursorName(), null);
        
        PersistenceManager persistenceManager = new PersistenceManager();
        try {
            persistenceManager.storeRecord(credentials, PersistencyConstants.CURSOR_INFO_TABLE,
                                           record);
        } catch (StoreException e) {
            throw new ConfigurationException("Unable to delete cursor meta data for cursor " +
                                             cursor.getCursorName() + "..", e);
        }

    }

/*    protected void initializeCollections(UserRegistry registry) throws RegistryException {
        if (!registry.resourceExists(PersistencyConstants.COMPONENT_ROOT)) {
            Collection rootCollection = registry.newCollection();
            registry.put(PersistencyConstants.COMPONENT_ROOT, rootCollection);
        }

        if (!registry.resourceExists(PersistencyConstants.COMPONENT_ROOT +
                                     getIndexRegistryPath())) {
            Collection indexCollection = registry.newCollection();
            registry.put(PersistencyConstants.COMPONENT_ROOT + getIndexRegistryPath(),
                         indexCollection);

        }

        if (!registry.resourceExists(PersistencyConstants.COMPONENT_ROOT +
                                     getCursorRegistryPath())) {
            Collection cursorCollection = registry.newCollection();
            registry.put(PersistencyConstants.COMPONENT_ROOT + getCursorRegistryPath(),
                         cursorCollection);

        }

        if (!registry.resourceExists(PersistencyConstants.COMPONENT_ROOT +
                                     getTableRegistryPath())) {
            Collection tableCollection = registry.newCollection();
            registry.put(PersistencyConstants.COMPONENT_ROOT + getTableRegistryPath(),
                         tableCollection);

        }

    }*/


    /*                Abstract methods                 */

/*    @Deprecated
    public abstract void persistIndex(int tenantId, IndexConfiguration configuration)
            throws ConfigurationException;*/

    public abstract void persistIndex(Map<String, String> credentials,
                                      IndexConfiguration configuration)
            throws ConfigurationException;


    /*                Private helper methods                */

    private void updateIndexTracker(int tenantId) throws RegistryException {

        ConfigurationContext superTenantContext = ServiceHolder.getConfigurationContextService().
                getServerConfigContext();
        UserRegistry superTenantRegistry = ServiceHolder.getRegistryService().
                getConfigSystemRegistry(SuperTenantCarbonContext.
                        getCurrentContext(superTenantContext).getTenantId());

        Resource tenantTrackerResource;

        String trackerPath = PersistencyConstants.INDEX_TRACKER_PATH;
        if (superTenantRegistry.resourceExists(trackerPath)) {
            tenantTrackerResource = superTenantRegistry.get(trackerPath);
            List<String> propertyValues = tenantTrackerResource.getPropertyValues(
                    PersistencyConstants.TENANTS_PROPERTY);
            if (propertyValues == null) {
                propertyValues = new ArrayList<String>();
            }
            propertyValues.add(String.valueOf(tenantId));
            //propertyValues.add("1");

            // Make sure that there is no duplication of tenant id's
            Set<String> uniqueProperties = new HashSet<String>();
            uniqueProperties.addAll(propertyValues);

            propertyValues.clear();
            for (String id : uniqueProperties) {
                propertyValues.add(id);
            }

            tenantTrackerResource.setProperty(PersistencyConstants.TENANTS_PROPERTY,
                                              propertyValues);
        } else {
            tenantTrackerResource = superTenantRegistry.newResource();
            List<String> propertyValues = new ArrayList<String>();
            propertyValues.add(String.valueOf(tenantId));

            // Make sure that there is no duplication of tenant id's
            Set<String> uniqueProperties = new HashSet<String>();
            uniqueProperties.addAll(propertyValues);

            propertyValues.clear();
            for (String id : uniqueProperties) {
                propertyValues.add(id);
            }

            tenantTrackerResource.setProperty(PersistencyConstants.TENANTS_PROPERTY,
                                              propertyValues);
        }

        superTenantRegistry.put(trackerPath, tenantTrackerResource);
    }

    private Integer[] getTenantsWithIndexes() throws RegistryException {
        ConfigurationContext superTenantContext = ServiceHolder.getConfigurationContextService().
                getServerConfigContext();
        UserRegistry superTenantRegistry = ServiceHolder.getRegistryService().
                getConfigSystemRegistry(SuperTenantCarbonContext.
                        getCurrentContext(superTenantContext).getTenantId());

        Resource tenantTrackerResource;

        String trackerPath = PersistencyConstants.INDEX_TRACKER_PATH;

        List<Integer> tenantsWithIndexes = new ArrayList<Integer>();
        if (superTenantRegistry.resourceExists(trackerPath)) {
            tenantTrackerResource = superTenantRegistry.get(trackerPath);
            List<String> propertyValues = tenantTrackerResource.getPropertyValues(
                    PersistencyConstants.TENANTS_PROPERTY);
            if (propertyValues == null) {
                propertyValues = new ArrayList<String>();
            }

            for (String propertyValue : propertyValues) {
                tenantsWithIndexes.add(Integer.valueOf(propertyValue));
            }
        }

        return tenantsWithIndexes.toArray(new Integer[]{});

    }

}
