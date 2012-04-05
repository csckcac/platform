/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.cep;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.bam.core.util.BAMUtil;
import org.wso2.carbon.bam.util.BAMException;

import javax.xml.namespace.QName;

import java.net.SocketException;

/**
 * This class using to process bam.xml file for bam-cep
 */
 class BAMCepConfigure {
    protected static String bamConfigurationType;
    protected static String serverDataEpr;
    protected static String mediationDataEpr;

    protected String getBAMConfigType() throws BAMException {

        OMElement bamConfFileOMElement = new BAMCepUtils().getBAMConfigFile();
        OMElement configType = null;

        if (bamConfFileOMElement != null) {
            configType = bamConfFileOMElement.getFirstElement();
        }
        if (configType == null) {
            throw new BAMException("configType  does not exist in bam.xml file");
        }

        return configType.getAttributeValue(new QName("type"));

    }

    protected String getBAMConfigType(OMElement bamConfig, String elementName) throws BAMException {

        OMElement configType = bamConfig.getFirstElement();
        if (configType == null) {
            throw new BAMException(elementName + "  does not exist in bam.xml file");
        }

        return configType.getAttributeValue(new QName("type"));
    }

    protected String getPublisherEPR(OMElement bamConfig, String eprName) throws BAMException {
        OMElement bamMsgProcessor = bamConfig.getFirstChildWithName(new QName("bamMessageProcessor"));
        if (bamMsgProcessor == null) {
            throw new BAMException("bamMessageProcessor did not mention in bam.xml");
        }
        OMElement serverDataEpr = bamMsgProcessor.getFirstChildWithName(new QName(eprName));
        if (serverDataEpr == null) {
            throw new BAMException(eprName + " did not mention in bam.xml");
        }

        return serverDataEpr.getText();
    }

    protected String getReceiverURL(String service) throws SocketException, BAMException {
        return BAMUtil.getBackendServerURLHTTPS() + "services/" + service;

    }

    protected static String getBAMConfigValue() throws BAMException {
        if(bamConfigurationType==null){
           BAMCepConfigure bamCepConfigure = new BAMCepConfigure();
            bamConfigurationType = bamCepConfigure.getBAMConfigType();
        }
     return bamConfigurationType;
    }

    protected static String getServerEprValue() throws BAMException {
        if(serverDataEpr==null){
            BAMCepConfigure bamCepConfigure = new BAMCepConfigure();
         serverDataEpr = bamCepConfigure.getPublisherEPR(new BAMCepUtils().getBAMConfigFile() ,"serverDataEPR");

        }
        return serverDataEpr;
    }

    protected static String getMediationEprValue()throws  BAMException {
        if(mediationDataEpr == null){
            BAMCepConfigure  bamCepConfigure = new BAMCepConfigure();
            mediationDataEpr = bamCepConfigure.getPublisherEPR(new BAMCepUtils().getBAMConfigFile() ,"mediationDataEPR");
        }
        return mediationDataEpr;
    }

}
