/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.dao.jpa.openjpa.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.humantask.core.dao.AttachmentDAO;
import org.wso2.carbon.humantask.core.dao.OrganizationalEntityDAO;
import org.wso2.carbon.humantask.core.dao.TaskDAO;

import javax.persistence.*;
import java.util.Date;

/**
 * Task Attachment Persistent Class.
 */
@Entity
@Table(name = "TASK_ATTACHMENT")
public class Attachment implements AttachmentDAO {
    /**
     * Class logger
     */
    private static Log log = LogFactory.getLog(Attachment.class);

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    /**
     * used to specify attachment name. Several attachments may
     * have the same name and can then be retrieved as a collection.
     */
    @Column(name = "ATTACHMENT_NAME")
    private String name;

    /**
     * can be any valid XML schema type, including
     * xsd:any, or any MIME type.
     */
    @Column(name = "CONTENT_TYPE")
    private String contentType;

    /**
     * indicates if the attachment is specified inline or by
     * reference. In the inline case it contains the string constant “inline”. In this case the
     * value of the attachment data type contains the base64 encoded attachment. In case
     * the attachment is referenced it contains the string “URL”, indicating that the value of
     * the attachment data type contains the a URL from where the attachment can be
     * retrieved. Other values of the accessType element are allowed for extensibility
     * reasons, for example to enable inclusion of attachment content from content
     * management systems.
     */
    @Column(name = "ACCESS_TYPE")
    private String accessType;

    @Column(name = "ATTACHED_AT")
    @Temporal(TemporalType.TIMESTAMP)
    private Date attachedAt;

    @Column(name = "ATTACHED_BY")
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private OrganizationalEntity attachedBy;

    @Column(name="VALUE")
    private String value;

    @ManyToOne
    private Task task;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public String getAccessType() {
        return accessType;
    }

    @Override
    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    @Override
    public Date getAttachedAt() {
        return new Date(attachedAt.getTime());
    }

    @Override
    public void setAttachedAt(Date attachedAt) {
        this.attachedAt = new Date(attachedAt.getTime());
    }

    @Override
    public OrganizationalEntityDAO getAttachedBy() {
        return attachedBy;
    }

    @Override
    public void setAttachedBy(OrganizationalEntityDAO attachedBy) {
        this.attachedBy = (OrganizationalEntity)attachedBy;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public Task getTask() {
        return task;
    }

    @Override
    public void setTask(TaskDAO task) {
        this.task = (Task) task;
    }
}
