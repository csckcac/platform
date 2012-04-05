package org.wso2.carbon.business.messaging.hl7;
/*
*  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*
*/
public class HL7Constants {
    // HL7 message formating constants
    public static final String HL7_CONTENT_TYPE="application/edi-hl7";
    public static final String HL7_FIELD_SEPARATOR = "|";
    public static final String HL7_ENCODING_CHARS = "^~\\&";
    public static final String HL7_ACK_CODE_AR = "AR";
    public static final String HL7_RAW_MESSAGE_PROPERTY_NAME = "HL7_RAW_MESSAGE";
}
