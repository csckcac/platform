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

import org.apache.axiom.om.OMElement;
import org.apache.ws.secpolicy.model.AlgorithmSuite;
import org.apache.ws.secpolicy.model.*;
import org.apache.ws.secpolicy.WSSPolicyException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.policybuilder.ui.internal.property.AlgorithmSuitePropertyFactory;
import org.wso2.carbon.policybuilder.ui.internal.property.MessageProperty;
import org.wso2.carbon.policybuilder.ui.internal.services.ElementReader;
import org.wso2.carbon.policybuilder.ui.internal.services.AlgoSuiteComparator;
import org.wso2.carbon.policybuilder.ui.internal.services.AlgoSuite;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.PriorityQueue;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Nov 13, 2008
 * Time: 10:11:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class AlgorithmSuiteBehavior extends PolicyBehavior {

	private static Log log = LogFactory.getLog(AlgorithmSuiteBehavior.class);
	private boolean hasAsymKWAlg = false;
	private boolean hasEncAlg = false;
	private boolean hasSigAlg = false;
	private String digestMethod = "";
	private String encMethod = "";
	private String asymKW = "";
	private String algoSuite = "";
	private AlgorithmSuite suite;

	public AlgorithmSuiteBehavior() {

		// assertionsMap = new HashMap();
		this.isBehaviorCompleted = false;
		init();
	}


	public AlgorithmSuiteBehavior(AbstractSecurityAssertion assertion) {

		// assertionsMap = new HashMap();
		super();
		this.isBehaviorCompleted = false;
		this.assertion = assertion;
		init();
	}


	public int evaluate(OMElement e) {
		super.evaluate(e);
		doEvaluate(e);
		return 0;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public void init() {
		this.msgProp = new MessageProperty(new AlgorithmSuitePropertyFactory());

		//To change body of implemented methods use File | Settings | File Templates.
	}


	private void doEvaluate(OMElement e) {
		OMElement current;
		String elementName;
		while (!isEmptyList()) {
			current = next();
			elementName = current.getQName().toString();
			if (this.msgProp.contains(elementName)) {
				if (elementName.equals((String) msgProp.getProperties(AlgorithmSuitePropertyFactory.K_DigestMethod))) {
					digestMethod = current.getAttributeValue(new QName("Algorithm"));
					this.hasSigAlg = true;
				} else
				if (elementName.equals((String) msgProp.getProperties(AlgorithmSuitePropertyFactory.K_EncData))) {
					ElementReader encDataReader = new ElementReader(current);
					while (encDataReader.next()) {
						String eName = encDataReader.getCurrentElementName();
						OMElement element = encDataReader.getCurrentElement();
						if (eName.equals(msgProp.getProperties(AlgorithmSuitePropertyFactory.K_EncryptMethod))) {
							encMethod = element.getAttributeValue(new QName("Algorithm"));
							this.hasEncAlg = true;
						}
					}
				} else
				if (elementName.equals((String) msgProp.getProperties(AlgorithmSuitePropertyFactory.K_EncryptKey))) {
					ElementReader encDataReader = new ElementReader(current);
					while (encDataReader.next()) {
						String eName = encDataReader.getCurrentElementName();
						OMElement element = encDataReader.getCurrentElement();
						if (eName.equals(msgProp.getProperties(AlgorithmSuitePropertyFactory.K_EncryptMethod))) {
							asymKW = element.getAttributeValue(new QName("Algorithm"));
							this.hasAsymKWAlg = true;
						}
					}
				}
			}
		}
		if (hasEncMethod() || hasSigMethod()) {
			checkProperties();
		} else {
			this.isBehaviorCompleted = false;
		}
		doAssertionLoad(isBehaviorCompleted);
	}


	public void checkProperties() {
		ArrayList algoList = (ArrayList) msgProp.getProperties(AlgorithmSuitePropertyFactory.K_AlgoList);
		Iterator algoSuites = algoList.iterator();
		ArrayList tempAlgo;
		PriorityQueue q = new PriorityQueue(20, new AlgoSuiteComparator());
		while (algoSuites.hasNext()) {
			tempAlgo = (ArrayList) algoSuites.next();
			Iterator algorithms = tempAlgo.iterator();
			int i = 0;
			String algorithmName = "";
			while (algorithms.hasNext()) {
				algorithmName = (String) algorithms.next();
				if (algorithmName.equals(encMethod)) {
					i++;
				} else if (algorithmName.equals(asymKW)) {
					i++;
				} else if (algorithmName.equals(digestMethod)) {
					i++;
				}
			}
			if (i > 0) {
				q.add(new AlgoSuite(algorithmName, i));
			}

			/*
						if(i>=2){

							 this.suite = new AlgorithmSuite(11);
							try {
								this.suite.setAlgorithmSuite((String)tempAlgo.get(3));
								System.out.println("AlgoSuite : "+(String)tempAlgo.get(3));


							} catch (WSSPolicyException e) {
								e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
							}
							this.isBehaviorCompleted=true;

							 System.out.println("Digest Method : " + digestMethod);
							 System.out.println("Encryption Method for Data : " + encMethod);
							 System.out.println("Encryption Method for Key : " + asymKW);
							 break;

						}

						*/
		}
		if (!q.isEmpty()) {
			this.suite = new AlgorithmSuite(11);
			try {
				String algoSuiteName = ((AlgoSuite) q.poll()).getSuite();
				this.suite.setAlgorithmSuite(algoSuiteName);
				if (log.isDebugEnabled()) {
					log.debug("AlgoSuite : " + algoSuiteName);
				}
				//  System.out.println("AlgoSuite : "+algoSuiteName);
			} catch (WSSPolicyException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
			this.isBehaviorCompleted = true;
			if (log.isDebugEnabled()) {
				log.debug("Digest Method : " + digestMethod);
			}
			//System.out.println("Digest Method : " + digestMethod);
			if (log.isDebugEnabled()) {
				log.debug("Encryption Method for Data : " + encMethod);
			}
			//System.out.println("Encryption Method for Data : " + encMethod);
			if (log.isDebugEnabled()) {
				log.debug("Encryption Method for Key : " + asymKW);
			}
			//  System.out.println("Encryption Method for Key : " + asymKW);
		}
	}


	public void setContext() {

		// PolicyContext.setValue(ContextConstant.isX509Included,isX509CertificateIncluded());
	}


	public boolean hasSigMethod() {
		return this.hasSigAlg;
	}

	public boolean hasEncMethod() {
		return this.hasEncAlg;
	}

	public boolean hasAsymKWMethod() {
		return this.hasAsymKWAlg;
	}

	public void doAssertionLoad(boolean behaviorCompleted) {
		if (behaviorCompleted == true && this.assertion != null) {
			if ((this.assertion instanceof SymmetricBinding)) {
				((SymmetricBinding) this.assertion).setAlgorithmSuite(this.suite);
			} else if ((this.assertion instanceof AsymmetricBinding)) {
				((AsymmetricBinding) this.assertion).setAlgorithmSuite(this.suite);
			}
		}
		handleSuccessor(this.root);
	}


	public static void main(String[] args) {
		AlgorithmSuite suite = new AlgorithmSuite(11);
		try {
			XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(System.out);
			suite.setAlgorithmSuite(org.apache.ws.secpolicy.Constants.ALGO_SUITE_BASIC256);
			suite.serialize(writer);
			writer.flush();
			writer.close();
		} catch (WSSPolicyException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (XMLStreamException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		System.out.println(org.apache.ws.secpolicy.Constants.ALGO_SUITE_BASIC256_RSA15);
	}
}
