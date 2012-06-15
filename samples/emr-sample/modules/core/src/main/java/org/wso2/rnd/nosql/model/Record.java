package org.wso2.rnd.nosql.model;
/**
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 **/

import java.util.Hashtable;

/**
 * Keep EMR Record details
 */
public class Record {
    private String recordID;
    private String timeStamp;
    private String recordType;
    private String sickness;
    private String recordTypeData;
    private String recordData;
    private String userCommnet;

    private Hashtable<String, Object> emrDoc;

    public Record() {
    }

    public Record(String recordID, String timeStamp, String recordType) {
        this.recordID = recordID;
        this.timeStamp = timeStamp;
        this.recordType = recordType;
    }

    public void setRecordID(String recordID) {
        this.recordID = recordID;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public void setSickness(String description) {
        sickness = description;
    }


    public void putEmrDoc(String docID, Object document) {
        this.emrDoc.put(docID, document);
    }

    public String getRecordID() {
        return recordID;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getRecordType() {
        return recordType;
    }

    public String getSickness() {
        return sickness;
    }

    public String getRecordTypeData() {
        return recordTypeData;
    }

    public void setRecordTypeData(String recordTypeData) {
        this.recordTypeData = recordTypeData;
    }

    public String getRecordData() {
        return recordData;
    }

    public void setRecordData(String recordData) {
        this.recordData = recordData;
    }

    public String getUserCommnet() {
        return userCommnet;
    }

    public void setUserCommnet(String userCommnet) {
        this.userCommnet = userCommnet;
    }

    public Object getEmrDoc(String docID) {
        return this.emrDoc.get(docID);
    }

}
