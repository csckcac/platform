package org.wso2.carbon.mediation.library.connectors.linkedin.util;

import com.google.code.linkedinapi.schema.Person;
import org.wso2.carbon.mediation.library.connectors.linkedin.LinkedinConstants;
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

    public static void storeResponseStatus(MessageContext ctxt, Person person) {
        ctxt.setProperty(LinkedinConstants.LINKEDIN_STATUS_USER_ID, person.getId());
        ctxt.setProperty(LinkedinConstants.LINKEDIN_STATUS_STATUS, person.getCurrentStatus());
    }

    public static void storeResponseHeadline(MessageContext ctxt, String id, String headline) {
        ctxt.setProperty(LinkedinConstants.LINKEDIN_HEADLINE_USER_ID, id);
        ctxt.setProperty(LinkedinConstants.LINKEDIN_HEADLINE_HEADLINE, headline);
    }

    public static void storeResponseMessaging(MessageContext ctxt, String idList, String subject) {
        ctxt.setProperty(LinkedinConstants.LINKEDIN_MESSAGING_ID_LIST, idList);
        ctxt.setProperty(LinkedinConstants.LINKEDIN_MESSAGING_SUBJECT, subject);
    }

    public static void StoreRegisteringUser(MessageContext ctxt, String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret){
        ctxt.setProperty(LinkedinConstants.LINKEDIN_USER_CONSUMER_KEY, consumerKey);
        ctxt.setProperty(LinkedinConstants.LINKEDIN_USER_CONSUMER_SECRET, consumerSecret);
        ctxt.setProperty(LinkedinConstants.LINKEDIN_USER_ACCESS_TOKEN, accessToken);
        ctxt.setProperty(LinkedinConstants.LINKEDIN_USER_ACCESS_TOKEN_SECRET, accessTokenSecret);
    }

    public static void storeErrorResponseStatus(MessageContext ctxt, Exception e) {
        ctxt.setProperty(SynapseConstants.ERROR_EXCEPTION, e);
        ctxt.setProperty(SynapseConstants.ERROR_MESSAGE, e.getMessage());
    }

    public static void setupClassLoadingForLinkedIn(Class clazz){
        Thread.currentThread().setContextClassLoader(clazz.getClassLoader());
    }
}
