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
package org.wso2.carbon.policybuilder.ui.internal.initiator;

import org.apache.ws.secpolicy.model.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Jan 19, 2009
 * Time: 3:05:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class AssertionLoader {

	private AbstractSecurityAssertion assertion;
	private int ver;
	private boolean status;

	public AbstractSecurityAssertion loadAssertion(String type, int version, AbstractSecurityAssertion a) {
		this.ver = version;
		this.assertion = a;
		search(type);
		return assertion;
	}


	public AbstractSecurityAssertion loadAssertion(String type, int version) {
		this.ver = version;
		search(type);
		return assertion;
	}

	public AbstractSecurityAssertion loadAssertion(String type, int version, boolean status) {
		this.ver = version;
		this.status = status;
		search(type);
		return assertion;
	}

	/* public AbstractSecurityAssertion loadAssertion(String type,AbstractSecurityAssertion a){

			 if("default".equals(type)){
			   this.assertion = a;
			 }
			 return assertion;
		}*/

	public AbstractSecurityAssertion loadAssertion(String type, int version, AbstractSecurityAssertion a, boolean status) {
		this.ver = version;
		this.status = status;
		this.assertion = a;
		search(type);
		return assertion;
	}

	private void search(String type) {
		if ("SymmetricBinding".equals(type)) {
			this.assertion = new SymmetricBinding(ver);
		} else if ("AsymmetricBinding".equals(type)) {
			this.assertion = new AsymmetricBinding(ver);
		} else if ("SignedEncryptedParts".equals(type)) {
			this.assertion = new SignedEncryptedParts(this.status, ver);
		} else if ("ProtectionToken".equals(type)) {
			this.assertion = new ProtectionToken(ver);
		} else if ("RecipientToken".equals(type)) {
			this.assertion = new RecipientToken(ver);
		} else if ("InitiatorToken".equals(type)) {
			this.assertion = new InitiatorToken(ver);
		} else {
			this.assertion = new Binding(ver) {
				public QName getName() {
					return null;  //To change body of implemented methods use File | Settings | File Templates.
				}

				public void serialize(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
					//To change body of implemented methods use File | Settings | File Templates.
				}
			};
		}
	}
}
