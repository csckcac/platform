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
import org.apache.axiom.om.OMElement;
import org.apache.ws.secpolicy.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.policybuilder.ui.internal.services.XMLFileReader;
import org.wso2.carbon.policybuilder.ui.internal.services.XMLReader;
import org.wso2.carbon.policybuilder.ui.internal.services.XMLStringReader;
import org.wso2.carbon.policybuilder.ui.internal.context.PolicyContext;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.ByteArrayOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Nov 7, 2008
 * Time: 9:41:08 AM
 * To change this template use File | Settings | File Templates.
 */

public class PolicyBuilder extends PolicyBehavior {

	private static Log log = LogFactory.getLog(PolicyBuilder.class);

	private ArrayList assertionsList;


	private OMElement curr_element;


	public PolicyBuilder() {
		assertionsList = new ArrayList();
		init();
	}

	public PolicyBuilder(AbstractSecurityAssertion a) {
		assertionsList = new ArrayList();
		init();
	}

	//for testing purposes
	public void init() {

		/*  symBind = new SymmetricBinding(11);
				asymBind = new AsymmetricBinding(11);
				signedPartsBind = new SignedEncryptedParts(true,11);
				encPartsBind = new SignedEncryptedParts(false,11);

				addAssertions(symBind);
				addAssertions(asymBind);
				addAssertions(signedPartsBind);
				addAssertions(encPartsBind);


				addBehavior(new SymmetricBindingBehavior(symBind));
				addBehavior(new AsymmetricBindingBehavior(asymBind));
				addBehavior(new SignedPartsBehavior(signedPartsBind));
				addBehavior(new EncryptedPartsBehavior(encPartsBind));
				*/
	}


	public void start(String[] args) {
		XMLReader myFileReader = new XMLFileReader(args[0]);
		curr_element = myFileReader.getDocumentRoot();
		PolicyBehavior temp;
		Iterator behaviorSet = nestedBehaviors.iterator();
		while (behaviorSet.hasNext()) {
			temp = (PolicyBehavior) behaviorSet.next();
			temp.evaluate(curr_element);
		}
		Iterator assertionSet = assertionsList.iterator();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		while (assertionSet.hasNext()) {
			try {
				//XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(System.out);
				XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
				//this.symBind.serialize(writer);
				AbstractSecurityAssertion a = (AbstractSecurityAssertion) assertionSet.next();
				a.serialize(writer);
				writer.flush();
				writer.close();
				out.flush();
				out.close();
			}
			catch (RuntimeException e) {
				// e.printStackTrace();
			}
			catch (XMLStreamException e) {
				//  e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
			catch (Exception e) {
				//  e.printStackTrace();
			}
		}
		if (log.isDebugEnabled()) {
			log.debug(out);
		}
		//System.out.println(out);
	}

	public String start(String policyText) {
		XMLReader stringReader = new XMLStringReader(policyText);
		curr_element = stringReader.getDocumentRoot();
		PolicyBehavior temp;
		Iterator behaviorSet = nestedBehaviors.iterator();
		while (behaviorSet.hasNext()) {
			temp = (PolicyBehavior) behaviorSet.next();
			temp.evaluate(curr_element);
		}
		Iterator assertionSet = assertionsList.iterator();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		while (assertionSet.hasNext()) {
			try {
				XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
				//this.symBind.serialize(writer);
				AbstractSecurityAssertion a = (AbstractSecurityAssertion) assertionSet.next();
				a.serialize(writer);
				writer.flush();
				writer.close();
				out.flush();
				out.close();
			}
			catch (RuntimeException e) {
				e.printStackTrace();
			}
			catch (XMLStreamException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return out.toString();
	}


	public void addAssertions(AbstractSecurityAssertion as) {
		if (as != null) {
			assertionsList.add(as);
		}
	}

	//for Testing purposes
	public static void main(String[] args) {
		//  SymmetricBinding a = new SymmetricBinding(11);
		//   SignedEncryptedParts a = new SignedEncryptedParts(false,11);
		String[] myArgs = {"/home/usw/soap.txt"};
		PolicyBuilder pb = new PolicyBuilder();
		pb.injectContext(new PolicyContext());
		pb.start(myArgs);

		/*
				try {
				  XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(System.out) ;
					ProtectionToken b = new ProtectionToken(11);
					AlgorithmSuite alg = new AlgorithmSuite(11);
					alg.setAlgorithmSuite(Constants.ALGO_SUITE_BASIC128);
					alg.setC14n(Constants.C14N);
					//alg.setAsymmetricKeyWrap(SP11Constants.);
					X509Token tkn = new X509Token(11);
					tkn.setDerivedKeys(true);
					tkn.setRequireEmbeddedTokenReference(true);
					tkn.setRequireIssuerSerialReference(true);
					tkn.setTokenVersionAndType(Constants.WSS_X509_V3_TOKEN10);

					b.setToken(tkn);
					//a.setAlgorithmSuite(alg);
				   // a.setProtectionToken(b);

					a.serialize(writer);

					writer.flush();
					writer.close();



					//tProtectionToken(new ProtectionToken(1));
					   // alg.serialize(writer);
				   // tkn.serialize(writer);
				 //   a.serialize(writer);
						System.out.println( org.apache.ws.security.conversation.ConversationConstants.TOKEN_TYPE_SECURITY_CONTEXT_TOKEN);
				} catch (XMLStreamException e) {
					e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				} catch (WSSPolicyException e) {
					e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				} //catch (IOException e) {
					//e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				//}

				*/
	}
}
