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
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.receivers.AbstractMessageReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.wso2.carbon.bam.core.receivers.StatisticsEventingMessageReceiver;
import org.wso2.carbon.bam.util.BAMException;

import java.net.SocketException;

/**
 * This class used to receive event from all bam data publishers for bam-cep
 */
public class BAMMessageReceiver extends AbstractMessageReceiver {

    Log log = LogFactory.getLog(BAMMessageReceiver.class);

    @Override
    protected void invokeBusinessLogic(MessageContext messageContext) throws AxisFault {

        BAMCepUtils bamCepUtils = new BAMCepUtils();
        String nameSpaceURI = messageContext.getEnvelope().getBody().getFirstElementNS().getNamespaceURI().trim();
        OMElement soapBodyElement = messageContext.getEnvelope().getBody().getFirstElement();
        EndpointReference reference;

        String msgProcessType;
        try {

            msgProcessType = BAMCepConfigure.getBAMConfigValue();

        } catch (BAMException e) {
            log.error("Error occurred get BAMMessageProcessing type", e);
            return;
        }

        if (msgProcessType.equals("bam")) {
            BAMCepConfigure bamCepConfigure = new BAMCepConfigure();
            String backEndServerURL;
            if (nameSpaceURI.equals(BAMCepConstants.SERVER_URI)) {
                try {
                    backEndServerURL = bamCepConfigure.getReceiverURL(BAMCepConstants.SERVICE_DATA_RECEIVER);
                } catch (SocketException e) {
                    log.error("Error getting back end server url for the service stat message receiver", e);
                    return;
                } catch (BAMException e) {
                    log.error("BAM exception occurred getting back end server url for the service stat message receiver", e);
                    return;
                }
                reference = new EndpointReference(backEndServerURL);
                bamCepUtils.invokeSoapClient(soapBodyElement, reference);

            } else if (nameSpaceURI.equals(BAMCepConstants.MEDIATION_URI)) {
                try {
                    backEndServerURL = bamCepConfigure.getReceiverURL(BAMCepConstants.MEDIATION_DATA_RECEIVER);

                } catch (SocketException e) {
                    log.error("Error getting back end server url for the mediation stat message receiver", e);
                    return;
                } catch (BAMException e) {
                  log.error("BAM exception occurred getting back end server url for the service stat message receiver", e);
                   return;
                }
                reference = new EndpointReference(backEndServerURL);
                bamCepUtils.invokeSoapClient(soapBodyElement, reference);
            }else if(nameSpaceURI.equals(BAMCepConstants.ACTIVITY_URI)){

              try {
                    backEndServerURL = bamCepConfigure.getReceiverURL(BAMCepConstants.ACTIVITY_DATA_RECEIVER);

                } catch (SocketException e) {
                    log.error("Error getting back end server url for the activity stat message receiver", e);
                    return;
                } catch (BAMException e) {
                  log.error("BAM exception occurred getting back end server url for the service stat message receiver", e);
                  return;
              }
                reference = new EndpointReference(backEndServerURL);
                bamCepUtils.invokeSoapClient(soapBodyElement, reference);
            }
        } else if (msgProcessType.equals("cep")) {

            String eprForReceivers;
            soapBodyElement.detach();
            if (nameSpaceURI.equals(BAMCepConstants.SERVER_URI)) {

                try {

                    eprForReceivers = BAMCepConfigure.getServerEprValue();

                } catch (BAMException e) {
                    log.error("Error occurred getting ServerDataEPR ", e);
                    return;
                }
                reference = new EndpointReference(eprForReceivers);
                bamCepUtils.invokeSoapClient(soapBodyElement, reference);

            } else if (nameSpaceURI.equals(BAMCepConstants.MEDIATION_URI)) {

                try {

                    eprForReceivers = BAMCepConfigure.getMediationEprValue();

                } catch (BAMException e) {
                    log.error("Error occurred getting MediationDataEPR ", e);
                    return;
                }
                reference = new EndpointReference(eprForReceivers);
                bamCepUtils.invokeSoapClient(soapBodyElement, reference);
            }
        } else {
            log.error("bam config type should be CEP or BAM , found wrong type " + msgProcessType);
            return;
        }
    }
}
