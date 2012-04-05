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
package org.wso2.carbon.policybuilder.ui.internal.assembler;


import org.wso2.carbon.policybuilder.ui.internal.engine.*;
import org.wso2.carbon.policybuilder.ui.internal.context.PolicyContext;
import org.wso2.carbon.policybuilder.ui.internal.context.Context;
import org.wso2.carbon.policybuilder.ui.internal.services.XMLReader;
import org.wso2.carbon.policybuilder.ui.internal.services.XMLFileReader;
import org.wso2.carbon.policybuilder.ui.internal.services.XMLStringReader;
import org.apache.ws.secpolicy.model.*;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.ByteArrayOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Jan 27, 2009
 * Time: 3:28:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class ManualAssembler extends EngineAssembler {

	private static Log log = LogFactory.getLog(ManualAssembler.class);
	public boolean hasNewConfig = false;


	public String assemble(String input) {
		Context context = new PolicyContext();
		context.init();
		
		SymmetricBinding symBind = new SymmetricBinding(11);
		AsymmetricBinding asymBind = new AsymmetricBinding(11);
		SignedEncryptedParts signedPartsBind = new SignedEncryptedParts(true, 11);
		SignedEncryptedParts encPartsBind = new SignedEncryptedParts(false, 11);
		PolicyBehavior temp;
		PolicyBehavior pb = new PolicyBuilder();
		PolicyBehavior sb = new SymmetricBindingBehavior(symBind);
		PolicyBehavior tb = new TimeStampBehavior(symBind);
		PolicyBehavior sp = new SymmetricProtectionBehavior(symBind);
		PolicyBehavior tkb = new TokenBehavior(new ProtectionToken(11));
		PolicyBehavior as1 = new AlgorithmSuiteBehavior(symBind);
		PolicyBehavior eo = new EncryptSignOrderBehavior(symBind);
		PolicyBehavior ab = new AsymmetricBindingBehavior(asymBind);
		PolicyBehavior tb2 = new TimeStampBehavior(asymBind);
		PolicyBehavior it = new InitiatorTokenBehavior(asymBind);
		PolicyBehavior at1 = new AsymmetricTokenBehavior(new InitiatorToken(11));
		PolicyBehavior rt = new RecipientTokenBehavior(asymBind);
		PolicyBehavior at2 = new AsymmetricTokenBehavior(new RecipientToken(11));
		PolicyBehavior as2 = new AlgorithmSuiteBehavior(asymBind);
		PolicyBehavior eo2 = new EncryptSignOrderBehavior(asymBind);
		PolicyBehavior sgn = new SignedPartsBehavior(signedPartsBind);
		PolicyBehavior enc = new EncryptedPartsBehavior(encPartsBind);
		sb.setID("1");
		ab.setID("2");
		sgn.setID("3");
		enc.setID("4");
		it.setID("5");
		rt.setID("6");
		as2.setID("7");
		sb.setSkipTag("2");
		ab.setSkipTag("3");
		it.setSkipTag("6");
		rt.setSkipTag("3");
		pb.setSuccessor(sb);
		sb.setSuccessor(tb);
		tb.setSuccessor(sp);
		sp.setSuccessor(tkb);
		tkb.setSuccessor(as1);
		as1.setSuccessor(eo);
		eo.setSuccessor(ab);
		ab.setSuccessor(tb2);
		tb2.setSuccessor(it);
		it.setSuccessor(at1);
		at1.setSuccessor(rt);
		rt.setSuccessor(at2);
		at2.setSuccessor(as2);
		as2.setSuccessor(eo2);
		eo2.setSuccessor(sgn);
		sgn.setSuccessor(enc);
		pb.injectContext(context);
		sb.injectContext(context);
		tb.injectContext(context);
		sp.injectContext(context);
		tkb.injectContext(context);
		as1.injectContext(context);
		eo.injectContext(context);
		ab.injectContext(context);
		tb2.injectContext(context);
		it.injectContext(context);
		at1.injectContext(context);
		rt.injectContext(context);
		at2.injectContext(context);
		as2.injectContext(context);
		eo2.injectContext(context);
		sgn.injectContext(context);
		enc.injectContext(context);
		assertionsList.add(symBind);
		assertionsList.add(asymBind);
		assertionsList.add(signedPartsBind);
		assertionsList.add(encPartsBind);
		configList.add(assertionsList);
		XMLReader myReader = new XMLFileReader("/home/usw/my.txt");
		// XMLReader myReader = new XMLStringReader(input);
		OMElement curr_element = myReader.getDocumentRoot();
		pb.handleSuccessor(curr_element);
		if (log.isDebugEnabled()) {
			log.info("ready to print");
		}
		//  System.out.println("ready 2 print");
		// System.out.println(print());
		String out = printConfigs();
		if (log.isDebugEnabled()) {
			log.info(out);
		}
		//System.out.println(out);
		return out;
	}


	public static void main(String[] args) {
		new ManualAssembler().assemble("");
	}
}
