package org.wso2.carbon.cep.core.internal.config;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.cep.core.Expression;
import org.wso2.carbon.cep.core.internal.util.CEPConstants;


import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;



import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;


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


   


	public static OMElement expressionToOM(Expression expression) {
		OMFactory factory = OMAbstractFactory.getOMFactory();
		OMElement omQueryExpression = factory.createOMElement(new QName(
				CEPConstants.CEP_CONF_NAMESPACE,
				CEPConstants.CEP_CONF_ELE_EXPRESSION,
				CEPConstants.CEP_CONF_CEP_NAME_SPACE_PREFIX));
		String queryExpressionType = expression.getType();
		String queryExpression = "";
		String queryExpressionLookUpKey = "";
		omQueryExpression.addAttribute(CEPConstants.CEP_REGISTRY_TYPE,
				queryExpressionType, null);
		if (queryExpressionType.equals(CEPConstants.CEP_CONF_EXPRESSION_INLINE)) {
			queryExpression = expression.getText();
			omQueryExpression.setText(queryExpression);
		} else if (queryExpressionType.equals(CEPConstants.CEP_REGISTRY_KEY)) {
			queryExpressionLookUpKey = expression.getText();
			omQueryExpression.addAttribute(
					CEPConstants.CEP_REGISTRY_EXPRESSION_LOOK_UP_KEY,
					queryExpressionLookUpKey, null);
		}
		return omQueryExpression;
	}
    
    

}