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

package org.wso2.carbon.humantask.core.dao.jpa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.humantask.core.dao.PaginatedResultList;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

public class JPAPaginatedResultList<T> implements PaginatedResultList<T> {
    private static final Log log = LogFactory.getLog(JPAPaginatedResultList.class);

    private static final int pageSize = 6;
    private Query rowCountQuery;
    private Query getRowsQuery;
    private int currentPosition;
    private int counter = 0;
    private EntityManager entityManager;

    public JPAPaginatedResultList(EntityManager entityManager, Query rowCountQuery, Query getRowsQuery) {
        this.rowCountQuery = rowCountQuery;
        this.getRowsQuery = getRowsQuery;
        this.currentPosition = 0;
        this.entityManager = entityManager;
    }

    public List<T> getPage(int page) {
        if (page < 0 || page == Integer.MAX_VALUE) {
            page = 0;
        }

        currentPosition = page + 1;

        int startIndex = page * pageSize;

        return getItems(page, startIndex);
    }

    public int getPageCount() {
        int pages = 0;
        if (log.isDebugEnabled()) {
            log.debug("Getting number of pages..");
        }

        entityManager.getTransaction().begin();
        double rowCount = ((Long) rowCountQuery.getSingleResult()).doubleValue();
        pages = (int) Math.ceil(rowCount / pageSize);
        entityManager.getTransaction().commit();

        if (log.isDebugEnabled()) {
            log.debug("Number of pages " + pages);
        }

        return pages;
    }

    public int getCurrentPageNumber() {
        return currentPosition;
    }

    private List<T> getItems(int page, int startIndex) {

        if(log.isDebugEnabled()){
            log.debug("Getting items for page " + page);
            log.debug("Getting items from " + startIndex);
        }

        Query query = getRowsQuery.setMaxResults(pageSize);
        if(page > 0){
            query = query.setFirstResult(startIndex);
        }
        entityManager.getTransaction().begin();
        List<T> results = (List<T>)query.getResultList();

        entityManager.getTransaction().commit();

        if(log.isDebugEnabled()){
            log.debug("JPA Query returns " + results.size() + " rows.");
        }

        return results;
    }
}
