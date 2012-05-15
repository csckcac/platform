/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.business.messaging.hl7.transport;

public class HL7Constants {

    public static final String TRANSPORT_NAME = "hl7";

    public static final String HL7_PORT = "transport.hl7.Port";

    public static final int DEFAULT_TIMEOUT = 60000;

    public static final int DEFAULT_SYNAPSE_HL7_PORT = 9792;

    public static final String TIMEOUT_PARAM = "timeout";
    
    public static final String HL7_RAW_MESSAGE_PROPERTY_NAME = "HL7_RAW_MESSAGE";
    
	public static final String HL7_VALIDATE_MESSAGE = "transport.hl7.ValidateMessage";
	
	public static final String HL7_AUTO_ACKNOWLEDGE = "transport.hl7.AutoAck";
	
	public static final String HL7_RESULT_MODE = "HL7_RESULT_MODE";
	
	public static final String HL7_RESULT_MODE_ACK = "ACK";
	
	public static final String HL7_RESULT_MODE_NACK = "NACK";
	
	public static final String HL7_NACK_MESSAGE = "HL7_NACK_MESSAGE";

}
