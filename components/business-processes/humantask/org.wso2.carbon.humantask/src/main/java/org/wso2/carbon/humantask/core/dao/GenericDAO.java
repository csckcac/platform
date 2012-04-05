/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.dao;

import javax.persistence.EntityNotFoundException;
import java.io.Serializable;
import java.util.List;

/**
 * Generic DAO (Data Access Object) with common methods to CRUD POJOs.
 * <p/>
 * <p>Extend this interface if you want typesafe (no casting necessary) DAO's for your
 * domain objects.
 *
 * @param <T>  a type variable
 * @param <PK> the primary key for that type
 */
@Deprecated
public interface GenericDAO<T extends EntityInterface<PK>, PK extends Serializable> {
    /**
     * Retrieve an persisted object using the given id as primary key.
     *
     * @param id object's primary key
     * @return object
     * @throws javax.persistence.EntityNotFoundException
     *          -
     *          if not found
     */
    T load(PK id) throws EntityNotFoundException;

    /**
     * Retrieve an persisted object using the given id as primary key.
     * <p/>
     * Returns null if not found.
     *
     * @param id object's primary key
     * @return object
     */
    T get(PK id);

    /**
     * Retrieve an persisted objects using the given ids as primary keys.
     *
     * @param ids objects's ids
     * @return list of objects
     */
    List<T> get(PK... ids);

    /**
     * Retrieve all persisted objects.
     *
     * @return list of objects
     */
    List<T> getAll();


    /**
     * Save all changes made to an object.
     *
     * @param object object
     */
    void save(T object);

    /**
     * Save all changes made to objects.
     *
     * @param objects objects
     */
    void save(T... objects);

    /**
     * Remove an object by given id.
     *
     * @param id object's pk
     */
    void delete(PK id);

    /**
     * Remove objects by given ids.
     *
     * @param ids objects's pk
     */
    void delete(PK... ids);

    /**
     * Remove an object.
     *
     * @param object object
     */
    void delete(T object);

    /**
     * Remove objects.
     *
     * @param objects objects
     */
    void delete(T... objects);

    /**
     * Delete all objects.
     */
    void deleteAll();
}
