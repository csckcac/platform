package org.wso2.carbon.cep.core.internal.config;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.cep.core.Expression;
import org.wso2.carbon.cep.core.internal.util.CEPConstants;

import javax.xml.namespace.QName;

/**
 * this class will help to build and Expression from a given OM Element
 */
public class ExpressionHelper {
    public static Expression fromOM(OMElement expressionElement) {

        Expression expression = new Expression();

        String type = expressionElement.getAttribute(new QName(CEPConstants.CEP_CONT_ATTR_TYPE)).getAttributeValue();
        expression.setType(type);

        if (expressionElement.getAttribute(new QName(CEPConstants.CEP_CONT_ATTR_LISTENER_NAME)) != null){
            String listenerName = expressionElement.getAttribute(new QName(CEPConstants.CEP_CONT_ATTR_LISTENER_NAME)).getAttributeValue();
            expression.setListenerName(listenerName);
        }

        String text = expressionElement.getText();
        expression.setText(text);
        return expression;
    }
}
