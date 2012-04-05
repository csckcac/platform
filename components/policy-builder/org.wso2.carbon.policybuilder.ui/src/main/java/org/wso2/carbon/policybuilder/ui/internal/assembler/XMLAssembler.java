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

import org.wso2.carbon.policybuilder.ui.internal.context.Context;
import org.wso2.carbon.policybuilder.ui.internal.context.PolicyContext;
import org.wso2.carbon.policybuilder.ui.internal.initiator.Initiator;
import org.wso2.carbon.policybuilder.ui.internal.exception.BehaviorLoadFault;
import org.wso2.carbon.policybuilder.ui.internal.exception.AssertionLoadFault;
import org.wso2.carbon.policybuilder.ui.internal.engine.PolicyBehavior;
import org.wso2.carbon.policybuilder.ui.internal.services.XMLReader;
import org.wso2.carbon.policybuilder.ui.internal.services.XMLFileReader;
import org.wso2.carbon.policybuilder.ui.internal.services.XMLStringReader;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Jan 29, 2009
 * Time: 10:30:44 AM
 * To change this template use File | Settings | File Templates.
 */
public class XMLAssembler extends EngineAssembler {

	private static Log log = LogFactory.getLog(XMLAssembler.class);
	private PolicyBehavior prev, main;
	private ArrayList<PolicyBehavior> mainList = new ArrayList<PolicyBehavior>();

	private ArrayList currentAssetionList;

	public String assemble(String input) {
		Initiator init = new Initiator();
		boolean hasConfig = init.initiate();
		boolean currentSearchFinished = false;
		while (hasConfig) {
			//if no behaviors left for loading
			Context context = new PolicyContext();
			context.init();
			prev = null;
			main = null;
			currentAssetionList = new ArrayList();
			while (true) {
				try {
					currentSearchFinished = init.searchNext();
					if (currentSearchFinished) {
						hasConfig = init.initiate();
						break;
					}
				} catch (BehaviorLoadFault behaviorLoadFault) {
					behaviorLoadFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
					hasConfig = init.initiate();
					break;
				} catch (AssertionLoadFault assertionLoadFault) {
					assertionLoadFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
					hasConfig = init.initiate();
					break;
				}
				doLoading(init, context);
			}
			if (main != null) {
				mainList.add(main);
				if (currentAssetionList != null)
					configList.add(currentAssetionList);
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("No new Config Present");
		}
		// System.out.println("\n No new Config");

		//for testing purposes
		// XMLReader myReader = new XMLFileReader("/home/usw/soap.txt");
		XMLReader myReader = new XMLStringReader(input);
		OMElement curr_element = myReader.getDocumentRoot();
		Iterator mainIterator = mainList.iterator();
		while (mainIterator.hasNext()) {
			PolicyBehavior tempMain = (PolicyBehavior) mainIterator.next();
			if (log.isDebugEnabled()) {
				log.debug("Evaluating Main behavior");
			}
			//System.out.println("\nEvaluating Main behavior");
			tempMain.handleSuccessor(curr_element);
		}
		String out = printConfigs();
		if (log.isDebugEnabled()) {
			log.debug(out);
		}
		//System.out.println(out);
		return out;
	}

	//for testing purposes
	public static void main(String[] args) {
		new XMLAssembler().assemble("");
	}

	private void doLoading(Initiator init, Context context) {
		if (init.hasSerialize()) {
			if (log.isDebugEnabled()) {
				log.debug("Assertion under this behavior is serialize enabled");
			}
			//System.out.println("____assertion under this behavior is serialize enabled");
			currentAssetionList.add(init.getCurrentAssertion());
		}
		init.getCurrentBehavior().injectContext(context);
		if (prev != null) {
			prev.setSuccessor(init.getCurrentBehavior());
		} else {
			main = init.getCurrentBehavior();
		}
		prev = init.getCurrentBehavior();
	}
}
