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

package org.wso2.carbon.attachment.mgt.client.dto2api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.attachment.mgt.api.attachment.Attachment;
import org.wso2.carbon.attachment.mgt.core.AttachmentImpl;
import org.wso2.carbon.attachment.mgt.core.exceptions.AttachmentMgtException;
import org.wso2.carbon.attachment.mgt.skeleton.types.TAttachment;

import java.io.IOException;

/**
 * This class manages conversions between org.wso2.carbon.attachment.mgt.skeleton.types and org
 * .wso2.carbon.attachment.mgt.core.dao
 */
public class TransformerUtil {
    /**
     * Logger class
     */
    private static Log log = LogFactory.getLog(TransformerUtil.class);

    /**
     * Transform (DTO) {@link TAttachment} to {@link Attachment}
     * @param attachment
     * @return
     * @throws AttachmentMgtException
     */
    public static Attachment convertAttachment(TAttachment attachment) throws AttachmentMgtException {
        Attachment attachmentDTO = null;
        try {
            attachmentDTO = new AttachmentImpl(attachment.getName(),
                                               attachment.getCreatedBy(),
                                               attachment.getContentType(),
                                               attachment.getContent().getInputStream());
            return attachmentDTO;

        } catch (IOException e) {
            String errMsg = "Error occurred due to content of the attachment.";
            log.error(errMsg, e);
            throw new AttachmentMgtException(errMsg, e);
        }
    }

    /**
     * Transform {@link Attachment} to (DTO) {@link TAttachment}
     * @param attachment
     * @return
     */
    public static TAttachment convertAttachment(Attachment attachment) {
        log.warn("org.wso2.carbon.attachment.mgt.client.dto2api.TransformerUtil" +
                 ".convertAttachment still not fully implemented.");
        TAttachment attachmentDTO = new TAttachment();
        attachmentDTO.setId(attachment.getId());
        attachmentDTO.setName(attachment.getName());
        attachmentDTO.setCreatedBy(attachment.getCreatedBy());
        attachmentDTO.setContentType(attachment.getContentType());
        //attachmentDTO.setContent(new DataHandler(attachment.getContent()));      //TODO:

        return attachmentDTO;
    }




}
