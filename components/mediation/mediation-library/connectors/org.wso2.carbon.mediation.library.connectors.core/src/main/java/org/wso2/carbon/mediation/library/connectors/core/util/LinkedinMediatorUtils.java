package org.wso2.carbon.mediation.library.connectors.core.util;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.mediators.template.TemplateContext;

import java.util.Stack;

/**
 * Created by IntelliJ IDEA.
 * User: charitha
 * Date: 2/27/12
 * Time: 1:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class LinkedinMediatorUtils {
    public static String lookupFunctionParam(MessageContext ctxt, String paramName) {
        Stack<TemplateContext> funcStack = (Stack) ctxt.getProperty(SynapseConstants.SYNAPSE__FUNCTION__STACK);
        TemplateContext currentFuncHolder = funcStack.peek();
        String paramValue = (String) currentFuncHolder.getParameterValue(paramName);
        return paramValue;
    }

   
}
