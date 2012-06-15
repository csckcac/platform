/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.rnd.nosql.model;

import java.util.UUID;

/**
 * To keep EMR related binary files ( images, pdf and videos )
 */
public class Blob {
    private UUID blobId;
    private String fileName;
    private Long fileSize;
    private String contentType;
    private byte[] fileContent;
    private String timeStamp;
    private String comment;

    public Blob() {
    }

    public Blob(UUID blobId, String comment, String timeStamp, byte[] fileContent,
                String contentType, Long fileSize, String fileName) {
        this.blobId = blobId;
        this.comment = comment;
        this.timeStamp = timeStamp;
        this.fileContent = fileContent;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.fileName = fileName;
    }

    public UUID getBlobId() {
        return blobId;
    }

    public void setBlobId(UUID blobId) {
        this.blobId = blobId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getFileContent() {
        return fileContent;
    }

    public void setFileContent(byte[] fileContent) {
        this.fileContent = fileContent;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
