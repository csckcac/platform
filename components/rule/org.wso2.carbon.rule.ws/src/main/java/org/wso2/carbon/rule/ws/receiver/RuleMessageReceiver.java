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

package org.wso2.carbon.rule.ws.receiver;

import org.apache.axis2.receivers.AbstractInOutMessageReceiver;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.AxisFault;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.wso2.carbon.rule.kernel.engine.RuleSession;
import org.wso2.carbon.rule.kernel.engine.RuleEngine;
import org.wso2.carbon.rule.common.util.Constants;
import org.wso2.carbon.rule.common.exception.RuleRuntimeException;
import org.wso2.carbon.rule.common.Input;
import org.wso2.carbon.rule.common.Output;

public class RuleMessageReceiver extends AbstractInOutMessageReceiver {

    //TODO: handle sessions properly
    private RuleEngine ruleEngine;
    private Input input;
    private Output output;

    public RuleMessageReceiver(RuleEngine ruleEngine, Input input, Output output) {
        this.ruleEngine = ruleEngine;
        this.input = input;
        this.output = output;
    }

    public void invokeBusinessLogic(MessageContext inMessageContext,
                                    MessageContext outMessageContext) throws AxisFault {
        OMElement inputOMElement = inMessageContext.getEnvelope().getBody().getFirstElement();

        SOAPFactory soapFactory = getSOAPFactory(inMessageContext);
        SOAPEnvelope soapEnvelope = soapFactory.getDefaultEnvelope();
        RuleSession ruleSession = null;
        try {
            ruleSession = getRuleSession(inMessageContext);
            soapEnvelope.getBody().addChild(ruleSession.execute(inputOMElement, this.input, this.output));
            outMessageContext.setEnvelope(soapEnvelope);
        } catch (RuleRuntimeException e) {
            log.error("Can not create the rule session", e);
            throw new AxisFault("Can not create the rule session", e);
        }

    }

    private RuleSession getRuleSession(MessageContext inMessageContext) throws RuleRuntimeException {
        ServiceContext serviceContext = inMessageContext.getServiceContext();
        RuleSession ruleSession =
                (RuleSession) serviceContext.getProperty(Constants.RULE_SESSION_OBJECT);
        if (ruleSession == null){
            ruleSession = this.ruleEngine.createSession(Constants.RULE_STATEFUL_SESSION);
            serviceContext.setProperty(Constants.RULE_SESSION_OBJECT, ruleSession);
        }

        return ruleSession;
        
    }
}
