/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.messagebox.sqs.internal.dispatcher;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.engine.AbstractDispatcher;
import org.apache.axis2.util.MultipleEntryHashMap;
import org.wso2.carbon.messagebox.MessageBoxConstants;

public class SQSActionBasedDispatcher extends AbstractDispatcher {
    public static final String NAME = "SQSActionBasedDispatcher";

    @Override
    public AxisOperation findOperation(AxisService axisService, MessageContext messageContext)
            throws AxisFault {

        MultipleEntryHashMap multipleEntryHashMap =
                (MultipleEntryHashMap) messageContext.getProperty(Constants.REQUEST_PARAMETER_MAP);

        if (multipleEntryHashMap != null) {
            Object actionNameProperty = multipleEntryHashMap.get(MessageBoxConstants.ACTION);
            multipleEntryHashMap.put(MessageBoxConstants.ACTION, actionNameProperty);

            if (actionNameProperty != null) {
                String actionName = actionNameProperty.toString();
                return axisService.getOperationBySOAPAction(actionName);

            }
        }
        return null;                                                       
    }

    @Override
    public AxisService findService(MessageContext messageContext) throws AxisFault {
        return null;
    }

    @Override
    public void initDispatcher() {
        init(new HandlerDescription(NAME));
    }
}
