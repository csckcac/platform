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
package org.wso2.carbon.policybuilder.ui.internal.engine;

import org.wso2.carbon.policybuilder.ui.internal.assertions.Assertion;
import org.wso2.carbon.policybuilder.ui.internal.context.ContextConstant;
import org.wso2.carbon.policybuilder.ui.internal.context.PolicyContext;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.ws.secpolicy.model.AbstractSecurityAssertion;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.policybuilder.ui.internal.property.MessageProperty;
import org.wso2.carbon.policybuilder.ui.internal.property.SymmetricPropertyFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Nov 7, 2008
 * Time: 9:33:03 AM
 * To change this template use File | Settings | File Templates.
 */
public class SymmetricBindingBehavior extends PolicyBehavior {

	private static Log log = LogFactory.getLog(SymmetricBindingBehavior.class);
	private TimeStampBehavior tsBehavior;
	private SymmetricProtectionBehavior spTokenBehavior;
	private AlgorithmSuiteBehavior algoSuiteBehavior;
	private EncryptSignOrderBehavior encOrderBehavior;

	private boolean isSymmetric = false;

	public SymmetricBindingBehavior() {
		this.isBehaviorCompleted = false;
		init();
	}

	public SymmetricBindingBehavior(AbstractSecurityAssertion assertion) {
		super();
		this.isBehaviorCompleted = false;
		this.assertion = assertion;
		init();
	}


	public int evaluate(OMElement e) {
		super.evaluate(e);
		doEvaluate();
		return 0;  //To change body of implemented methods use File | Settings | File Templates.
	}

	private void doEvaluate() {
		OMElement current;
		String elementName;

		//  boolean includeTimestamp=false;
		while (!isEmptyList()) {
			current = next();
			elementName = current.getQName().toString();
			if (this.msgProp.contains(elementName)) {
				if (elementName.equals((String) msgProp.getProperties(SymmetricPropertyFactory.K_DerivedKey))) {
					Iterator childrenDK = current.getChildElements();
					while (childrenDK.hasNext()) {
						OMElement tempDK = (OMElement) childrenDK.next();
						String tempDKName = tempDK.getQName().toString();
						if (tempDKName.equals((String) msgProp.getProperties(SymmetricPropertyFactory.K_STokenRef))) {
							Iterator childrenSTR = tempDK.getChildElements();
							while (childrenSTR.hasNext()) {
								OMElement tempSTR = (OMElement) childrenSTR.next();
								String tempSTRName = tempSTR.getQName().toString();
								if (tempSTRName.equals((String) msgProp.getProperties(SymmetricPropertyFactory.K_Ref))) {
									ArrayList valueList = (ArrayList) msgProp.getProperties(SymmetricPropertyFactory.K_ValueType);
									if (valueList.contains(tempSTR.getAttributeValue(new QName("ValueType")))) {
										isSymmetric = true;
									}
								}
							}
						}
					}
				}
				if (elementName.equals((String) msgProp.getProperties(SymmetricPropertyFactory.K_SignMethod))) {
					ArrayList valueList = (ArrayList) msgProp.getProperties(SymmetricPropertyFactory.K_AlgorithmType);
					if (valueList.contains(current.getAttributeValue(new QName("Algorithm")))) {
						isSymmetric = true;
					}
				}
				if (elementName.equals((String) msgProp.getProperties(SymmetricPropertyFactory.K_EncryptMethod))) {
					ArrayList valueList = (ArrayList) msgProp.getProperties(SymmetricPropertyFactory.K_AlgorithmType);
					if (valueList.contains(current.getAttributeValue(new QName("Algorithm")))) {
						isSymmetric = true;
					}
				}
			}
		}
		doAssertionLoad(isSymmetric);
	}

	public void doAssertionLoad(boolean behaviorCompleted) {
		if (behaviorCompleted == true) {
			this.setContext();
			//this.assertion = new SymmetricBinding(11);
			if (this.assertion != null) {
				this.isBehaviorCompleted = true;
				if (log.isDebugEnabled()) {
					log.debug("Processing Symmetric Binding handlers");
				}
				//System.out.println("Symmetric");
				handleSuccessor(this.root);
			}
		} else {
			skip(this.root);
		}
	}


	public void setContext() {
		context.setValue(ContextConstant.isSymmetric, isSymmetric());
	}


	public boolean isSymmetric() {
		return isSymmetric;
	}


	public void init() {
		//   Assertion assertion = new Assertion("SYM_BINDING",Consts.SYM_BINDING_ASSERT);
		this.msgProp = new MessageProperty(new SymmetricPropertyFactory());
	}

	public AbstractSecurityAssertion getAssertion() {
		if (isBehaviorCompleted == true) {
			return this.assertion;
		} else {
			return null;
		}
	}

	//@Depricated
	public void mapAssertions(Map m, Assertion a) {
	}


	//for Testing purposes
	public static void main(String[] args) {
		String xml = "<wsu:book xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">" + "<wsu:name>Quick-start Axis</wsu:name>" + "<isbn>978-286-8<id>200</id></isbn>" + "</wsu:book>";
		ByteArrayInputStream xmlStream = new ByteArrayInputStream(xml.getBytes());
		try {
			StAXBuilder builder = new StAXOMBuilder(xmlStream);
			OMElement oe = builder.getDocumentElement();
			Stack s = new Stack();
			s.push(oe);
			while (!s.isEmpty()) {
				oe = (OMElement) s.pop();
				System.out.println(oe.getLocalName());
				if (oe.getLocalName().equals("name")) {
					System.out.println(oe.getQName().toString());
				}
				//new SymmetricBindingBehavior().evaluate(oe);
				Iterator it = oe.getChildElements();
				while (it.hasNext()) {
					s.push(it.next());
				}

				
			}
		}
		catch (XMLStreamException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}
}
