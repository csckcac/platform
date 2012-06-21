/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpel.b4p.extension;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.runtime.extension.AbstractLongRunningExtensionOperation;
import org.apache.ode.bpel.runtime.extension.ExtensionContext;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Class that implements <code>&lt;peopleActivity&gt;</code> related to BPEL4People.
 */
public class BPEL4PeopleExtensionOperation extends AbstractLongRunningExtensionOperation {
    private static Log log = LogFactory.getLog(BPEL4PeopleExtensionOperation.class);

    private ExtensionContext extensionContext;

    private String cid;

    private String outputVarName;

    private PeopleActivity peopleActivity;

    /**
     * Initial stuff and calling an external service which causes to send back a response in an indefinite time.
     * Correlation values should be set within this method.
     *
     * @param extensionContext ExtensionContext
     * @param cid              cid
     * @param element          ExtensionActivity
     */
    @Override
    public void runAsync(ExtensionContext extensionContext, String cid, Element element)
            throws FaultException {
        this.extensionContext = extensionContext;
        this.cid = cid;

        peopleActivity = new PeopleActivity(extensionContext, element);
        String taskID = peopleActivity.invoke(extensionContext);
        extensionContext.setCorrelationValues(new String[]{taskID});
        extensionContext.setCorrelatorId(peopleActivity.inferCorrelatorId(extensionContext));
        outputVarName = peopleActivity.getOutputVarName();
    }

    /**
     * Called when the response for the above service is received
     *
     * @param mexId MessageExchange id
     */
    @Override
    public void onRequestReceived(String mexId) throws FaultException {
        log.info("Response received");
        //((ExtensionContextImpl)extensionContext).setBpelRuntimeContext(context);
        Element notificationMessageEle = extensionContext.getInternalInstance().getMyRequest(mexId);
        Node part = extensionContext.getPartData(notificationMessageEle,
                outputVarName);
        log.info("RESPONSE: " + DOMUtils.domToString(notificationMessageEle));
        log.info("PART: " + DOMUtils.domToString(part));
        extensionContext.writeVariable(outputVarName, notificationMessageEle);
        extensionContext.complete(cid);
    }
}
