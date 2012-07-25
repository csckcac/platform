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
package org.apache.hadoop.hive.metastore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.common.CarbonContextThreadLocal;
import org.apache.hadoop.hive.common.JavaUtils;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.api.Database;
import org.apache.hadoop.hive.metastore.api.InvalidObjectException;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.hadoop.hive.metastore.api.NoSuchObjectException;
import org.apache.hadoop.hive.metastore.hooks.JDOConnectionURLHook;
import org.apache.hadoop.util.ReflectionUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.hadoop.hive.metastore.MetaStoreUtils.DEFAULT_DATABASE_COMMENT;
import static org.apache.hadoop.hive.metastore.MetaStoreUtils.DEFAULT_DATABASE_NAME;

// WSO2 Fix. Holds multi tenant Hive configurations
public class HiveContext {

    private static final Log log = LogFactory.getLog(HiveContext.class);

    private static final String DATABASE_WAREHOUSE_SUFFIX = ".db";

    private static final ThreadLocal<Integer> tenantIdThreadLocal = new ThreadLocal<Integer>();

    private static Map<Integer, HiveContext> contextMap =
            new ConcurrentHashMap<Integer, HiveContext>();

    private static Map<Integer, Map<String, String>> tenantPropertyMap =
            new ConcurrentHashMap<Integer, Map<String, String>>();

    private static JDOConnectionURLHook urlHook = null;
    private static String urlHookClassName = "";

    private static ClassLoader classLoader;

    {
        classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = HiveConf.class.getClassLoader();
        }
    }

    private HiveConf conf; // Tenant specific Hive Configuration. Holds tenant meta store database
    // information

    private RawStore ms; // Tenant meta store

    private Warehouse wh; // Tenant warehouse

    private HiveContext(HiveConf conf, RawStore ms) throws MetaException {
        this.conf = conf;
        this.ms = ms;

        int tenantId = CarbonContextThreadLocal.getTenantId();
        String whRootString = HiveConf.getVar(conf, HiveConf.ConfVars.METASTOREWAREHOUSE);
        whRootString = whRootString + tenantId;

        HiveConf.setVar(conf, HiveConf.ConfVars.METASTOREWAREHOUSE, whRootString);

        this.wh = new Warehouse(conf);
    }

    public static void startTenantFlow(int tenantId) {
        tenantIdThreadLocal.set(tenantId);
    }

    public static void endTenantFlow() {
        tenantIdThreadLocal.remove();
    }

    public static HiveContext getCurrentContext() {
        int tenantId = tenantIdThreadLocal.get();

        HiveContext context = contextMap.get(tenantId);
        if (context == null) {

            HiveConf conf = new HiveConf();
            conf.setInt(HiveConf.ConfVars.CURRENTTENANT.varname, tenantId);

            try {
                initConnectionUrlHook(conf);
                String connectionURL = urlHook.getJdoConnectionUrl(conf);

                conf.setVar(HiveConf.ConfVars.METASTORECONNECTURLKEY, connectionURL);
            } catch (ClassNotFoundException e) {
                handleException("Unable to find the Connection URL Hook class : " +
                                urlHookClassName, e);
            } catch (Exception e) {
                handleException("Unable to fetch the JDO Connection URL for meta store..", e);
            }

            RawStore ms = null;
            try {
                ms = createMetaStore(conf);
            } catch (MetaException e) {
                handleException("Unable to create meta store for tenant : " + tenantId, e);
            }

            try {
                context = new HiveContext(conf, ms);
            } catch (MetaException e) {
                handleException("Unable to initialize warehouse for tenant : " + tenantId, e);
            }

            try {
                ms.getDatabase(DEFAULT_DATABASE_NAME);
            } catch (NoSuchObjectException e) {
                try {
                    ms.createDatabase(
                            new Database(DEFAULT_DATABASE_NAME, DEFAULT_DATABASE_COMMENT,
                                         getDefaultDatabasePath(DEFAULT_DATABASE_NAME,
                                                                context.getWarehouse()).
                                                 toString(), null));
                } catch (InvalidObjectException e1) {
                    handleException("Unable to create hive database for tenant : " + tenantId,
                                    e1);
                } catch (MetaException e1) {
                    handleException("Unable to create hive database for tenant : " + tenantId,
                                    e1);
                }
            }

            contextMap.put(tenantId, context);

            return context;
        }

        return context;

    }

    public HiveConf getConf() {
        return conf;
    }

    public RawStore getMetaStore() {
        return ms;
    }

    public Warehouse getWarehouse() {
        return wh;
    }

    public int getTenantId() {
        return tenantIdThreadLocal.get();
    }

    public void setProperty(String key, String value) {
        Map<String, String> properties = tenantPropertyMap.get(getTenantId());

        if (properties == null) {
            properties = new ConcurrentHashMap<String, String>();
            tenantPropertyMap.put(getTenantId(), properties);
        }

        properties.put(key, value);

    }

    public String getProperty(String key) {
        Map<String, String> properties = tenantPropertyMap.get(getTenantId());

        if (properties == null) {
            return properties.get(key);
        }

        return null;
        
    }

    // Multiple threads could try to initialize at the same time.

    synchronized private static void initConnectionUrlHook(HiveConf hiveConf)
            throws ClassNotFoundException {

        String className =
                hiveConf.get(HiveConf.ConfVars.METASTORECONNECTURLHOOK.toString(), "").trim();
        if (className.equals("")) {
            urlHookClassName = "";
            urlHook = null;
            return;
        }
        boolean urlHookChanged = !urlHookClassName.equals(className);
        if (urlHook == null || urlHookChanged) {
            urlHookClassName = className.trim();

            Class<?> urlHookClass = Class.forName(urlHookClassName, true,
                                                  JavaUtils.getClassLoader());
            urlHook = (JDOConnectionURLHook) ReflectionUtils.newInstance(urlHookClass, null);
        }
        return;
    }

    private static RawStore createMetaStore(HiveConf hiveConf) throws MetaException {
        String rawStoreClassName = hiveConf.getVar(HiveConf.ConfVars.METASTORE_RAW_STORE_IMPL);

        if (log.isDebugEnabled()) {
            log.debug("Opening raw store with implemenation class:" + rawStoreClassName);
        }

        RawStore ms = (RawStore) ReflectionUtils.newInstance(getClass(rawStoreClassName,
                                                                      RawStore.class), hiveConf);
        ms.setConf(hiveConf);

        return ms;
    }

    private static Class<?> getClass(String rawStoreClassName, Class<?> class1)
            throws MetaException {
        try {
            return Class.forName(rawStoreClassName, true, classLoader);
        } catch (ClassNotFoundException e) {
            throw new MetaException(rawStoreClassName + " class not found");
        }
    }

    private static Path getDefaultDatabasePath(String dbName, Warehouse wh) throws MetaException {
        if (dbName.equalsIgnoreCase(DEFAULT_DATABASE_NAME)) {
            return wh.getWhRoot();
        }
        return new Path(wh.getWhRoot(), dbName.toLowerCase() + DATABASE_WAREHOUSE_SUFFIX);
    }

    private static void handleException(String message, Throwable e) {
        log.error(message, e);
        throw new RuntimeException(message, e);
    }

    private static void handleException(String message) {
        log.error(message);
        throw new RuntimeException(message);
    }

}
