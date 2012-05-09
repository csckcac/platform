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
import org.apache.openjpa.persistence.criteria.OpenJPACriteriaBuilder;
import org.apache.openjpa.persistence.query.QueryBuilder;
import org.apache.openjpa.persistence.query.QueryDefinition;
import org.apache.openjpa.persistence.*;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.Metamodel;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;


public class JUDDIEntityManager implements OpenJPAEntityManager {

    private static Logger log = Logger.getLogger(JUDDIEntityManager.class);

    OpenJPAEntityManager openJPAEntityManager;

    public JUDDIEntityManager(OpenJPAEntityManager openJPAEntityManager) {
        this.openJPAEntityManager = openJPAEntityManager;
    }


    public OpenJPAEntityManagerFactory getEntityManagerFactory() {
        return openJPAEntityManager.getEntityManagerFactory();
    }

    public FetchPlan getFetchPlan() {
        return openJPAEntityManager.getFetchPlan();
    }

    public FetchPlan pushFetchPlan() {
        return openJPAEntityManager.pushFetchPlan();
    }

    public void popFetchPlan() {
        openJPAEntityManager.popFetchPlan();
    }

    public ConnectionRetainMode getConnectionRetainMode() {
        return openJPAEntityManager.getConnectionRetainMode();
    }

    public boolean isTransactionManaged() {
        return openJPAEntityManager.isTransactionManaged();
    }

    /**
     * @deprecated
     */
    public boolean isManaged() {
        return openJPAEntityManager.isManaged();
    }

    public boolean getSyncWithManagedTransactions() {
        return openJPAEntityManager.getSyncWithManagedTransactions();
    }

    public void setSyncWithManagedTransactions(boolean b) {
        openJPAEntityManager.setSyncWithManagedTransactions(b);
    }

    public ClassLoader getClassLoader() {
        return openJPAEntityManager.getClassLoader();
    }

    public String getConnectionUserName() {
        return openJPAEntityManager.getConnectionUserName();
    }

    public String getConnectionPassword() {
        return openJPAEntityManager.getConnectionPassword();
    }

    public boolean getMultithreaded() {
        return openJPAEntityManager.getMultithreaded();
    }

    public void setMultithreaded(boolean b) {
        openJPAEntityManager.setMultithreaded(b);
    }

    public boolean getIgnoreChanges() {
        return openJPAEntityManager.getIgnoreChanges();
    }

    public void setIgnoreChanges(boolean b) {
        openJPAEntityManager.setIgnoreChanges(b);
    }

    public boolean getNontransactionalRead() {
        return openJPAEntityManager.getNontransactionalRead();
    }

    public void setNontransactionalRead(boolean b) {
        openJPAEntityManager.setNontransactionalRead(b);
    }

    public boolean getNontransactionalWrite() {
        return openJPAEntityManager.getNontransactionalWrite();
    }

    public void setNontransactionalWrite(boolean b) {
        openJPAEntityManager.setNontransactionalWrite(b);
    }

    public boolean getOptimistic() {
        return openJPAEntityManager.getOptimistic();
    }

    public void setOptimistic(boolean b) {
        openJPAEntityManager.setOptimistic(b);
    }

    public RestoreStateType getRestoreState() {
        return openJPAEntityManager.getRestoreState();
    }

    public void setRestoreState(RestoreStateType restoreStateType) {
        openJPAEntityManager.setRestoreState(restoreStateType);
    }

    public boolean getRetainState() {
        return openJPAEntityManager.getRetainState();
    }

    public void setRetainState(boolean b) {
        openJPAEntityManager.setRetainState(b);
    }

    public DetachStateType getDetachState() {
        return openJPAEntityManager.getDetachState();
    }

    public void setDetachState(DetachStateType detachStateType) {
        openJPAEntityManager.setDetachState(detachStateType);
    }

    public AutoClearType getAutoClear() {
        return openJPAEntityManager.getAutoClear();
    }

    public void setAutoClear(AutoClearType autoClearType) {
        openJPAEntityManager.setAutoClear(autoClearType);
    }

    public EnumSet<AutoDetachType> getAutoDetach() {
        return openJPAEntityManager.getAutoDetach();
    }

    public void setAutoDetach(AutoDetachType autoDetachType) {
        openJPAEntityManager.setAutoDetach(autoDetachType);
    }

    public void setAutoDetach(EnumSet<AutoDetachType> autoDetachTypes) {
        openJPAEntityManager.setAutoDetach(autoDetachTypes);
    }

    public void setAutoDetach(AutoDetachType autoDetachType, boolean b) {
        openJPAEntityManager.setAutoDetach(autoDetachType, b);
    }

    public boolean getEvictFromStoreCache() {
        return openJPAEntityManager.getEvictFromStoreCache();
    }

    public void setEvictFromStoreCache(boolean b) {
        openJPAEntityManager.setEvictFromStoreCache(b);
    }

    public boolean getPopulateStoreCache() {
        return openJPAEntityManager.getPopulateStoreCache();
    }

    public void setPopulateStoreCache(boolean b) {
        openJPAEntityManager.setPopulateStoreCache(b);
    }

    public boolean isTrackChangesByType() {
        return openJPAEntityManager.isTrackChangesByType();
    }

    public void setTrackChangesByType(boolean b) {
        openJPAEntityManager.setTrackChangesByType(b);
    }

    public Object putUserObject(Object o, Object o1) {
        return openJPAEntityManager.putUserObject(o, o1);
    }

    public Object getUserObject(Object o) {
        return openJPAEntityManager.getUserObject(o);
    }

    public <T> T[] findAll(Class<T> tClass, Object... objects) {
        return openJPAEntityManager.findAll(tClass, objects);
    }

    public <T> Collection<T> findAll(Class<T> tClass, Collection collection) {
        return openJPAEntityManager.findAll(tClass, collection);
    }

    public <T> T findCached(Class<T> tClass, Object o) {
        return openJPAEntityManager.findCached(tClass, o);
    }

    public Class getObjectIdClass(Class aClass) {
        return openJPAEntityManager.getObjectIdClass(aClass);
    }

    public OpenJPAEntityTransaction getTransaction() {
        return openJPAEntityManager.getTransaction();
    }

    public void setSavepoint(String s) {
        openJPAEntityManager.setSavepoint(s);
    }

    public void rollbackToSavepoint() {
        openJPAEntityManager.rollbackToSavepoint();
    }

    public void rollbackToSavepoint(String s) {
        openJPAEntityManager.rollbackToSavepoint(s);
    }

    public void releaseSavepoint() {
        openJPAEntityManager.releaseSavepoint();
    }

    public void releaseSavepoint(String s) {
        openJPAEntityManager.releaseSavepoint(s);
    }

    public void preFlush() {
        openJPAEntityManager.preFlush();
    }

    public void validateChanges() {
        openJPAEntityManager.validateChanges();
    }

    public boolean isStoreActive() {
        return openJPAEntityManager.isStoreActive();
    }

    public void beginStore() {
        openJPAEntityManager.beginStore();
    }

    public boolean containsAll(Object... objects) {
        return openJPAEntityManager.containsAll(objects);
    }

    public boolean containsAll(Collection collection) {
        return openJPAEntityManager.containsAll(collection);
    }

    public void persistAll(Object... objects) {
        openJPAEntityManager.persistAll(objects);
    }

    public void persistAll(Collection collection) {
        openJPAEntityManager.persistAll(collection);
    }

    public void removeAll(Object... objects) {
        openJPAEntityManager.removeAll(objects);
    }

    public void removeAll(Collection collection) {
        openJPAEntityManager.removeAll(collection);
    }

    public void release(Object o) {
        openJPAEntityManager.release(o);
    }

    public void releaseAll(Object... objects) {
        openJPAEntityManager.releaseAll(objects);
    }

    public void releaseAll(Collection collection) {
        openJPAEntityManager.releaseAll(collection);
    }

    public void retrieve(Object o) {
        openJPAEntityManager.retrieve(o);
    }

    public void retrieveAll(Object... objects) {
        openJPAEntityManager.retrieveAll(objects);
    }

    public void retrieveAll(Collection collection) {
        openJPAEntityManager.retrieveAll(collection);
    }

    public void refreshAll(Object... objects) {
        openJPAEntityManager.refreshAll(objects);
    }

    public void refreshAll(Collection collection) {
        openJPAEntityManager.refreshAll(collection);
    }

    public void refreshAll() {
        openJPAEntityManager.refreshAll();
    }

    public void evict(Object o) {
        openJPAEntityManager.evict(o);
    }

    public void evictAll(Object... objects) {
        openJPAEntityManager.evictAll(objects);
    }

    public void evictAll(Collection collection) {
        openJPAEntityManager.evictAll(collection);
    }

    public void evictAll() {
        openJPAEntityManager.evictAll();
    }

    public void evictAll(Class aClass) {
        openJPAEntityManager.evictAll(aClass);
    }

    public void evictAll(Extent extent) {
        openJPAEntityManager.evictAll(extent);
    }

//    public <T> T detach(T t) {
//        return openJPAEntityManager.detach(t);
//    }

    public Collection detachAll(Collection collection) {
        return openJPAEntityManager.detachAll(collection);
    }

    public Object[] detachAll(Object... objects) {
        return openJPAEntityManager.detachAll(objects);
    }

    public Object[] mergeAll(Object... objects) {
        return openJPAEntityManager.mergeAll(objects);
    }

    public Collection mergeAll(Collection collection) {
        return openJPAEntityManager.mergeAll(collection);
    }

    public void transactional(Object o, boolean b) {
        openJPAEntityManager.transactional(o, b);
    }

    public void transactionalAll(Collection collection, boolean b) {
        openJPAEntityManager.transactionalAll(collection, b);
    }

    public void transactionalAll(Object[] objects, boolean b) {
        openJPAEntityManager.transactionalAll(objects, b);
    }

    public void nontransactional(Object o) {
        openJPAEntityManager.nontransactional(o);
    }

    public void nontransactionalAll(Collection collection) {
        openJPAEntityManager.nontransactionalAll(collection);
    }

    public void nontransactionalAll(Object[] objects) {
        openJPAEntityManager.nontransactionalAll(objects);
    }

    public Generator getNamedGenerator(String s) {
        return openJPAEntityManager.getNamedGenerator(s);
    }

    public Generator getIdGenerator(Class aClass) {
        return openJPAEntityManager.getIdGenerator(aClass);
    }

    public Generator getFieldGenerator(Class aClass, String s) {
        return openJPAEntityManager.getFieldGenerator(aClass, s);
    }

    public <T> Extent<T> createExtent(Class<T> tClass, boolean b) {
        return openJPAEntityManager.createExtent(tClass, b);
    }

    //TODO: capture the persist object and store it in GREG.
    public void persist(Object o) {
        openJPAEntityManager.persist(o);
    }

    public <T> T merge(T t) {
        return openJPAEntityManager.merge(t);
    }

    public void remove(Object o) {
        openJPAEntityManager.remove(o);
    }

    public <T> T find(Class<T> tClass, Object o) {
        return openJPAEntityManager.find(tClass, o);
    }

    @Override
    public <T> T find(Class<T> tClass, Object o, Map<String, Object> stringObjectMap) {
        return openJPAEntityManager.find(tClass, o, stringObjectMap);
    }

    @Override
    public <T> T find(Class<T> tClass, Object o, LockModeType lockModeType) {
        return openJPAEntityManager.find(tClass, o, lockModeType);
    }

    @Override
    public <T> T find(Class<T> tClass, Object o, LockModeType lockModeType, Map<String, Object> stringObjectMap) {
        return openJPAEntityManager.find(tClass, o, lockModeType, stringObjectMap);
    }

    public <T> T getReference(Class<T> tClass, Object o) {
        return openJPAEntityManager.getReference(tClass, o);
    }

    public void flush() {
        openJPAEntityManager.flush();
    }

    public void setFlushMode(FlushModeType flushModeType) {
        openJPAEntityManager.setFlushMode(flushModeType);
    }

    public FlushModeType getFlushMode() {
        return openJPAEntityManager.getFlushMode();
    }

    public void lock(Object o, LockModeType lockModeType) {
        openJPAEntityManager.lock(o, lockModeType);
    }

    @Override
    public void lock(Object o, LockModeType lockModeType, Map<String, Object> stringObjectMap) {
        openJPAEntityManager.lock(o, lockModeType, stringObjectMap);
    }

    public void refresh(Object o) {
        openJPAEntityManager.refresh(o);
    }

    @Override
    public void refresh(Object o, Map<String, Object> stringObjectMap) {
        openJPAEntityManager.refresh(o, stringObjectMap);
    }

    @Override
    public void refresh(Object o, LockModeType lockModeType) {
        openJPAEntityManager.refresh(o, lockModeType);
    }

    @Override
    public void refresh(Object o, LockModeType lockModeType, Map<String, Object> stringObjectMap) {
        openJPAEntityManager.refresh(o, lockModeType, stringObjectMap);
    }

    public void clear() {
        openJPAEntityManager.clear();
    }

    @Override
    public void detach(Object o) {
        openJPAEntityManager.detach(o);
    }

    public boolean contains(Object o) {
        return openJPAEntityManager.contains(o);
    }

    public OpenJPAQuery createQuery(String s) {
        return openJPAEntityManager.createQuery(s);
    }

    @Override
    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> tCriteriaQuery) {
        return openJPAEntityManager.createQuery(tCriteriaQuery);
    }

    @Override
    public <T> TypedQuery<T> createQuery(String s, Class<T> tClass) {
        return openJPAEntityManager.createQuery(s, tClass);
    }

    public OpenJPAQuery createNamedQuery(String s) {
        return openJPAEntityManager.createNamedQuery(s);
    }

    @Override
    public <T> TypedQuery<T> createNamedQuery(String s, Class<T> tClass) {
        return openJPAEntityManager.createNamedQuery(s, tClass);
    }

    public OpenJPAQuery createNativeQuery(String s) {
        return openJPAEntityManager.createNativeQuery(s);
    }

    public OpenJPAQuery createNativeQuery(String s, Class aClass) {
        return openJPAEntityManager.createNativeQuery(s, aClass);
    }

    public OpenJPAQuery createNativeQuery(String s, String s1) {
        return openJPAEntityManager.createNativeQuery(s, s1);
    }

    public void joinTransaction() {
        openJPAEntityManager.joinTransaction();
    }

    @Override
    public <T> T unwrap(Class<T> tClass) {
        return openJPAEntityManager.unwrap(tClass);
    }

    public Object getDelegate() {
        return openJPAEntityManager.getDelegate();
    }

    public void close() {
        openJPAEntityManager.close();
    }

    public boolean isOpen() {
        return openJPAEntityManager.isOpen();
    }

    public OpenJPAQuery createQuery(Query query) {
        return openJPAEntityManager.createQuery(query);
    }

    public OpenJPAQuery createQuery(String s, String s1) {
        return openJPAEntityManager.createQuery(s, s1);
    }

    public LockModeType getLockMode(Object o) {
        return openJPAEntityManager.getLockMode(o);
    }

    @Override
    public void setProperty(String s, Object o) {
        openJPAEntityManager.setProperty(s, o);
    }

    @Override
    public Map<String, Object> getProperties() {
        return openJPAEntityManager.getProperties();
    }

    public void lock(Object o, LockModeType lockModeType, int i) {
        openJPAEntityManager.lock(o, lockModeType, i);
    }

    public void lock(Object o) {
        openJPAEntityManager.lock(o);
    }

    public void lockAll(Collection collection, LockModeType lockModeType, int i) {
        openJPAEntityManager.lockAll(collection, lockModeType, i);
    }

    public void lockAll(Collection collection) {
        openJPAEntityManager.lockAll(collection);
    }

    public void lockAll(Object[] objects, LockModeType lockModeType, int i) {
        openJPAEntityManager.lockAll(objects, lockModeType, i);
    }

    public void lockAll(Object... objects) {
        openJPAEntityManager.lockAll(objects);
    }

    public boolean cancelAll() {
        return openJPAEntityManager.cancelAll();
    }

    public Object getConnection() {
        return openJPAEntityManager.getConnection();
    }

    public Collection getManagedObjects() {
        return openJPAEntityManager.getManagedObjects();
    }

    public Collection getTransactionalObjects() {
        return openJPAEntityManager.getTransactionalObjects();
    }

    public Collection getPendingTransactionalObjects() {
        return openJPAEntityManager.getPendingTransactionalObjects();
    }

    public Collection getDirtyObjects() {
        return openJPAEntityManager.getDirtyObjects();
    }

    public boolean getOrderDirtyObjects() {
        return openJPAEntityManager.getOrderDirtyObjects();
    }

    public void setOrderDirtyObjects(boolean b) {
        openJPAEntityManager.setOrderDirtyObjects(b);
    }

    public void dirtyClass(Class aClass) {
        openJPAEntityManager.dirtyClass(aClass);
    }

    public Collection<Class> getPersistedClasses() {
        return openJPAEntityManager.getPersistedClasses();
    }

    public Collection<Class> getRemovedClasses() {
        return openJPAEntityManager.getRemovedClasses();
    }

    public Collection<Class> getUpdatedClasses() {
        return openJPAEntityManager.getUpdatedClasses();
    }

    public <T> T createInstance(Class<T> tClass) {
        return openJPAEntityManager.createInstance(tClass);
    }

    public void dirty(Object o, String s) {
        openJPAEntityManager.dirty(o, s);
    }

    public Object getObjectId(Object o) {
        return openJPAEntityManager.getObjectId(o);
    }

    public boolean isDirty(Object o) {
        return openJPAEntityManager.isDirty(o);
    }

    public boolean isTransactional(Object o) {
        return openJPAEntityManager.isTransactional(o);
    }

    public boolean isPersistent(Object o) {
        return openJPAEntityManager.isPersistent(o);
    }

    public boolean isNewlyPersistent(Object o) {
        return openJPAEntityManager.isNewlyPersistent(o);
    }

    public boolean isRemoved(Object o) {
        return openJPAEntityManager.isRemoved(o);
    }

    public boolean isDetached(Object o) {
        return openJPAEntityManager.isDetached(o);
    }

    public Object getVersion(Object o) {
        return openJPAEntityManager.getVersion(o);
    }

    public Set<String> getSupportedProperties() {
        return openJPAEntityManager.getSupportedProperties();
    }

    public OpenJPACriteriaBuilder getCriteriaBuilder(){
        return openJPAEntityManager.getCriteriaBuilder();
    }

    @Override
    public Metamodel getMetamodel() {
        return openJPAEntityManager.getMetamodel();
    }

    public OpenJPAQuery createDynamicQuery(QueryDefinition dynamic){
        return openJPAEntityManager.createDynamicQuery(dynamic);
    }

    public <T> T detachCopy(T pc){
        return openJPAEntityManager.detachCopy(pc);
    }

    /**
     * @deprecated
     */
    public OpenJPAConfiguration getConfiguration() {
        return openJPAEntityManager.getConfiguration();
    }

    /**
     * @deprecated
     */
    public void setRestoreState(int i) {
        openJPAEntityManager.setRestoreState(i);
    }

    /**
     * @deprecated
     */
    public void setDetachState(int i) {
        openJPAEntityManager.setDetachState(i);
    }

    /**
     * @deprecated
     */
    public void setAutoClear(int i) {
        openJPAEntityManager.setAutoClear(i);
    }

    /**
     * @deprecated
     */
    public void setAutoDetach(int i) {
        openJPAEntityManager.setAutoDetach(i);
    }

    /**
     * @deprecated
     */
    public void setAutoDetach(int i, boolean b) {
        openJPAEntityManager.setAutoDetach(i, b);
    }

    /**
     * @deprecated
     */
    public boolean isLargeTransaction() {
        return openJPAEntityManager.isLargeTransaction();
    }

    /**
     * @deprecated
     */
    public void setLargeTransaction(boolean b) {
        openJPAEntityManager.setLargeTransaction(b);
    }

    /**
     * @deprecated
     */
    public void addTransactionListener(Object o) {
        openJPAEntityManager.addTransactionListener(o);
    }

    /**
     * @deprecated
     */
    public void removeTransactionListener(Object o) {
        openJPAEntityManager.removeTransactionListener(o);
    }

    /**
     * @deprecated
     */
    public int getTransactionListenerCallbackMode() {
        return openJPAEntityManager.getTransactionListenerCallbackMode();
    }

    /**
     * @deprecated
     */
    public void setTransactionListenerCallbackMode(int i) {
        openJPAEntityManager.setTransactionListenerCallbackMode(i);
    }

    /**
     * @deprecated
     */
    public void addLifecycleListener(Object o, Class... classes) {
        openJPAEntityManager.addLifecycleListener(o, classes);
    }

    /**
     * @deprecated
     */
    public void removeLifecycleListener(Object o) {
        openJPAEntityManager.removeLifecycleListener(o);
    }

    /**
     * @deprecated
     */
    public int getLifecycleListenerCallbackMode() {
        return openJPAEntityManager.getLifecycleListenerCallbackMode();
    }

    /**
     * @deprecated
     */
    public void setLifecycleListenerCallbackMode(int i) {
        openJPAEntityManager.setLifecycleListenerCallbackMode(i);
    }

    /**
     * @deprecated
     */
    public void begin() {
        openJPAEntityManager.begin();
    }

    /**
     * @deprecated
     */
    public void commit() {
        openJPAEntityManager.commit();
    }

    /**
     * @deprecated
     */
    public void rollback() {
        openJPAEntityManager.rollback();
    }

    /**
     * @deprecated
     */
    public boolean isActive() {
        return openJPAEntityManager.isActive();
    }

    /**
     * @deprecated
     */
    public void commitAndResume() {
        openJPAEntityManager.commitAndResume();
    }

    /**
     * @deprecated
     */
    public void rollbackAndResume() {
        openJPAEntityManager.rollbackAndResume();
    }

    /**
     * @deprecated
     */
    public void setRollbackOnly() {
        openJPAEntityManager.setRollbackOnly();
    }

    /**
     * @deprecated
     */
    public void setRollbackOnly(Throwable throwable) {
        openJPAEntityManager.setRollbackOnly(throwable);
    }

    /**
     * @deprecated
     */
    public Throwable getRollbackCause() {
        return openJPAEntityManager.getRollbackCause();
    }

    /**
     * @deprecated
     */
    public boolean getRollbackOnly() {
        return openJPAEntityManager.getRollbackOnly();
    }
}