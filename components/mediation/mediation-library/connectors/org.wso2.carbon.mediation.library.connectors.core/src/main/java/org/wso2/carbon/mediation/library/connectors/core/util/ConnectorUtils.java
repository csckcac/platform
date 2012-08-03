package org.wso2.carbon.mediation.library.connectors.core.util;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.mediators.template.TemplateContext;

import java.util.Collection;
import java.util.Iterator;
import java.util.Stack;

public class ConnectorUtils {

    public static String lookupFunctionParam(MessageContext ctxt, String paramName) {
        Stack<TemplateContext> funcStack = (Stack) ctxt.getProperty(SynapseConstants.SYNAPSE__FUNCTION__STACK);
        TemplateContext currentFuncHolder = funcStack.peek();
        String paramValue = (String) currentFuncHolder.getParameterValue(paramName);
        return paramValue;
    }

    public static String lookupFunctionParam(MessageContext ctxt, int index) {
        Stack<TemplateContext> funcStack = (Stack) ctxt.getProperty(SynapseConstants.SYNAPSE__FUNCTION__STACK);
        TemplateContext currentFuncHolder = funcStack.peek();
        Collection paramList = currentFuncHolder.getParameters();
        Iterator it = paramList.iterator();
        int i = 0;
        while (it.hasNext()) {
            String param = (String) it.next();
            if (i == index) {
                return param;
            }
            i++;
        }
        return null;
    }
}
