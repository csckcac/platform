/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.attachment.mgt.core.dao.impl.jpa.openjpa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.attachment.mgt.api.attachment.Attachment;
import org.wso2.carbon.attachment.mgt.core.dao.AttachmentDAO;
import org.wso2.carbon.attachment.mgt.core.dao.AttachmentMgtDAOFactory;
import org.wso2.carbon.attachment.mgt.core.dao.impl.jpa.openjpa.entity.AttachmentDAOImpl;
import org.wso2.carbon.attachment.mgt.core.exceptions.AttachmentMgtException;
import org.wso2.carbon.attachment.mgt.util.URLGeneratorUtil;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

/**
 * OpenJPA specific implementation on {@link AttachmentMgtDAOFactory}
 */
//TODO: Please fix this class methods by moving .begin, .commit methods out from the real
// business logic
public class AttachmentMgtDAOFactoryImpl implements AttachmentMgtDAOFactory {
    /**
     * Class Logger
     */
    private static Log log = LogFactory.getLog(AttachmentMgtDAOFactoryImpl.class);

    private EntityManager entityManager;

    public AttachmentMgtDAOFactoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public AttachmentDAO addAttachment(Attachment attachment) throws AttachmentMgtException {
        AttachmentDAO attachmentDAO = new AttachmentDAOImpl();
        attachmentDAO.setName(attachment.getName());
        attachmentDAO.setCreatedBy(attachment.getCreatedBy());
        attachmentDAO.setContentType(attachment.getContentType());
        attachmentDAO.setUrl(URLGeneratorUtil.generateURL());
        attachmentDAO.setContent(attachment.getContent());

        /*try {
            AttachmentServerHolder.getInstance().getAttachmentServer().getTransactionManager()
                    .setTransactionTimeout(0);
            AttachmentServerHolder.getInstance().getAttachmentServer().getTransactionManager().begin();
            entityManager.persist(attachmentDAO);
            AttachmentServerHolder.getInstance().getAttachmentServer().getTransactionManager().commit();
        } catch (NotSupportedException e) {
            e.printStackTrace();
        } catch (SystemException e) {
            e.printStackTrace();
        } catch (HeuristicRollbackException e) {
            e.printStackTrace();
        } catch (RollbackException e) {
            e.printStackTrace();
        } catch (HeuristicMixedException e) {
            e.printStackTrace();
        }*/


        log.warn("Please properly fix this code.");

        /*boolean existingTransaction;
        try {
            existingTransaction = entityManager.getTransaction() != null;
        } catch (Exception ex) {
            String errMsg = "Internal Error, could not get current transaction.";
            throw new AttachmentMgtException(errMsg, ex);
        }
        // already in transaction, execute and return directly
        if (existingTransaction) {
            entityManager.persist(attachmentDAO);
            return attachmentDAO;
        }*/

        try {
            if (log.isDebugEnabled()) {
                log.debug("Beginning a new transaction");
            }
            entityManager.getTransaction().begin();
        } catch (Exception e) {
            String errMsg = "Internal Error, could not begin transaction.";
            throw new AttachmentMgtException(errMsg, e);
        }

        try {
            entityManager.persist(attachmentDAO);
            entityManager.getTransaction().commit();
        } catch (RuntimeException rtEx) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            log.error(rtEx.getLocalizedMessage(), rtEx);
            throw new AttachmentMgtException(rtEx.getLocalizedMessage(), rtEx);
        }

        return attachmentDAO;
    }

    @Override
    public AttachmentDAO getAttachmentInfo(String id) throws AttachmentMgtException {
        AttachmentDAO attachmentDAO = null;
        entityManager.getTransaction().begin();
        attachmentDAO = entityManager.find(AttachmentDAOImpl.class, id);
        entityManager.getTransaction().commit();

        if (attachmentDAO != null) {
            return attachmentDAO;
        } else {
            throw new AttachmentMgtException("Attachment not found for id : " + id);
        }
    }

    @Override
    public boolean removeAttachment(String id) throws AttachmentMgtException {
        boolean existingTransaction;
        try {
            existingTransaction = entityManager.getTransaction() != null;
        } catch (Exception ex) {
            String errMsg = "Internal Error, could not get current transaction.";
            throw new AttachmentMgtException(errMsg, ex);
        }

        // already in transaction, execute and return directly
        if (existingTransaction) {
            entityManager.remove(getAttachmentInfo(id));
        } else {
            entityManager.getTransaction().begin();
            entityManager.remove(getAttachmentInfo(id));
            entityManager.getTransaction().commit();
        }

        return true;
    }

    @Override
    public AttachmentDAO getAttachmentInfoFromURL(String attachmentURI) throws AttachmentMgtException {
        Query query = entityManager.createQuery("SELECT x FROM org.wso2.carbon.attachment.mgt.core.dao.impl.jpa.openjpa.entity.AttachmentDAOImpl AS x WHERE x.url = :attachmentURI");
        query.setParameter("attachmentURI", attachmentURI);

        List<AttachmentDAO> daoList = query.getResultList();

        if (daoList.isEmpty()) {
            throw new AttachmentMgtException("Attachment not found for the uri:" + attachmentURI);
        } else if (daoList.size() != 1) {
            String errorMsg = "There exist more than one attachment for the attachment URI:" + attachmentURI + ". org" +
                              ".wso2.carbon.attachment.mgt.util.URLGeneratorUtil.generateURL method has generated " +
                              "similar uris for different attachments. This has caused a major inconsistency for " +
                              "attachment management.";
            log.fatal(errorMsg);
            throw new AttachmentMgtException(errorMsg);
        } else {
            return daoList.get(0);
        }
    }
}
