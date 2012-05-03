/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.wso2.carbon.autoscaler.service.adapters.ec2;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.*;

/**
 * Utility class for EC2 Adaptor specific functionality
 */
public class EC2Util {

    private static final Log log = LogFactory.getLog(EC2Util.class);

    private EC2Util(){
    }


    public static EC2InstanceManager createEC2InstanceManager(String accessKey,
                                                              String secretKey,
                                                              String instanceMgtEPR) {
        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        AmazonEC2Client ec2Client = new AmazonEC2Client(awsCredentials);
        ec2Client.setEndpoint(instanceMgtEPR);
        return new EC2InstanceManager(ec2Client);
    }



}
