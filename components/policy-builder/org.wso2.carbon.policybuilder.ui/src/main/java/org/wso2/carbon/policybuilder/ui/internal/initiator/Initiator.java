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

import org.wso2.carbon.policybuilder.ui.internal.services.ElementReader;
import org.wso2.carbon.policybuilder.ui.internal.services.OrderedElementReader;
import org.wso2.carbon.policybuilder.ui.internal.engine.PolicyBehavior;
import org.wso2.carbon.policybuilder.ui.internal.assembler.ManualAssembler;
import org.wso2.carbon.policybuilder.ui.internal.initiator.search.AbstractSearch;
import org.wso2.carbon.policybuilder.ui.internal.initiator.search.IndexedSearch;
import org.wso2.carbon.policybuilder.ui.internal.initiator.search.DefaultSearch;
import org.wso2.carbon.policybuilder.ui.internal.exception.AssertionLoadFault;
import org.wso2.carbon.policybuilder.ui.internal.exception.BehaviorLoadFault;
import org.apache.axiom.om.OMElement;
import org.apache.ws.secpolicy.model.AbstractSecurityAssertion;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Jan 15, 2009
 * Time: 12:11:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class Initiator implements StaticInitiator {

	private static Log log = LogFactory.getLog(Initiator.class);
	private String docNamespace;
	private final static String PACKAGE = "org.wso2.carbon.policybuilder.ui.internal.engine.";
	private String behaviorName;
	private OMElement behaviorElem;
	private AbstractSecurityAssertion currentAssertion;

	private PolicyBehavior currentBehavior;
	private Map behaviorAssertionMap;
	private Configurator initConfigs = new Configurator();

	public final static int VERSION = 11;
	private ManualAssembler loader;
	private AbstractSearch search;

	public Initiator() {
		this.behaviorAssertionMap = new HashMap();
	}

	private void setSearchType() {
		if ("sequence".equals(initConfigs.getSearchType())) {
			search = new IndexedSearch();
			if (log.isDebugEnabled()) {
				log.debug("Sequenced search enabled");
			}
			//System.out.println("Sequenced search enabled");
		} else {
			search = new DefaultSearch(initConfigs.getCurrentConfiguration(), this.docNamespace);
			if (log.isDebugEnabled()) {
				log.debug("Default search");
			}
			// System.out.println("Default search");
		}
	}

	private void resetInitiator() {
		this.currentAssertion = null;
		this.currentBehavior = null;
		this.behaviorAssertionMap = new HashMap();
	}


	public boolean hasConfig() {
		return true;
	}


	public boolean initiate() {
		boolean configAvailable = false;
		configAvailable = initConfigs.setNewConfiguration();
		if (configAvailable) {
			resetInitiator();
			this.docNamespace = initConfigs.getNamespace();
			setSearchType();
		}
		return configAvailable;
	}

	public boolean searchNext() throws BehaviorLoadFault, AssertionLoadFault {
		boolean noBehaviorsLeft = false;
		noBehaviorsLeft = search.search();
		if (!noBehaviorsLeft) {
			doBehaviorLoad();
		}
		return noBehaviorsLeft;
	}

	private String getCurrentBehaviorName() {
		return search.getBehaviorName();
	}

	private String getCurrentAssertionName() {
		return search.getAssertionName();
	}

	private String getCurrentAssertionParam() {
		return search.getAssertionParam();
	}

	private String getCurrentSkipTag() {
		return search.getSkipParam();
	}

	private String getCurrentID() {
		return search.getID();
	}

	public boolean hasSerialize() {
		return search.isSerializable();
	}


	private OMElement getCurrent() {
		return search.getCurrent();
	}

	public void serialize() {
		initConfigs.serialize(System.out);
	}

	private void doBehaviorLoad() throws BehaviorLoadFault, AssertionLoadFault {
		AbstractSecurityAssertion assertion;
		assertion = getAssertion(getCurrentAssertionName(), getCurrentAssertionParam());
		if (assertion != null) {
			PolicyBehavior temp = getBehavior(getCurrentBehaviorName(), assertion);
			if (log.isDebugEnabled()) {
				log.debug("Assertion found :" + getCurrentAssertionName() + " ; class:" + assertion.getClass().getName());
			}
			//  System.out.println("Assertion found :"+getCurrentAssertionName()+" ; class:"+assertion.getClass().getName());
			if (temp != null) {
				currentAssertion = assertion;
				currentBehavior = temp;
				behaviorAssertionMap.put(getCurrentID(), assertion);
				setCurrentBehaviorParams();
				if (log.isDebugEnabled()) {
					log.debug("Behavior found :" + getCurrentBehaviorName() + " ; class:" + temp.getClass().getName());
				}
				// System.out.println("Behavior found :"+getCurrentBehaviorName()+" ; class:"+temp.getClass().getName());
			} else {
				throw new BehaviorLoadFault();
			}
		} else {
			throw new AssertionLoadFault();
		}
	}

	private PolicyBehavior getBehavior(String elemNameAttrib, AbstractSecurityAssertion assertion) {
		BehaviorLoader behaviorLoader = new BehaviorLoader();
		PolicyBehavior pb = null;
		try {
			pb = (PolicyBehavior) behaviorLoader.loadClass(Initiator.PACKAGE + elemNameAttrib).getConstructor(AbstractSecurityAssertion.class).newInstance(assertion);
			if (log.isDebugEnabled()) {
				log.debug("Policy Behavior Loaded Successfully..");
			}
			//System.out.println("fine");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();   //To change body of catch statement use File | Settings | File Templates.
		} catch (InvocationTargetException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (NoSuchMethodException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (IllegalAccessException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (InstantiationException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		return pb;
	}

	private AbstractSecurityAssertion getAssertion(String assertionName, String assertionParam) {
		AbstractSecurityAssertion assertion = null;
		if (assertionParam == null && assertionName != null) {
			String[] assertID = assertionName.split(":");
			if (assertID[0] != null)
				if (log.isDebugEnabled()) {
					log.debug(assertID[0] + "------");
				}
			// System.out.println(assertID[0]+"------");
			if (assertID.length > 1 && assertID[1] != null)
				if (log.isDebugEnabled()) {
					log.debug(assertID[1] + "------");
				}
			//   System.out.println(assertID[1]+"------");
			if (assertID != null && assertID.length >= 2 && "default".equals(assertID[0])) {
				assertion = (AbstractSecurityAssertion) behaviorAssertionMap.get(assertID[1]);
				if (log.isDebugEnabled()) {
					log.debug("assertion extracted from map");
				}
				//System.out.println("assertion extracted from map");
			} else {
				assertion = new AssertionLoader().loadAssertion(assertionName, VERSION);
			}
		} else if (assertionParam != null && assertionName != null) {
			Boolean signed = new Boolean(assertionParam);
			assertion = new AssertionLoader().loadAssertion(assertionName, VERSION, signed.booleanValue());
		}
		return assertion;
	}

	private void setCurrentBehaviorParams() {
		if (currentBehavior != null) {
			this.currentBehavior.setID(getCurrentID());
			this.currentBehavior.setSkipTag(getCurrentSkipTag());
		}
	}

	public AbstractSecurityAssertion getCurrentAssertion() {
		return this.currentAssertion;
	}

	public PolicyBehavior getCurrentBehavior() {
		return this.currentBehavior;
	}


	//for Testing Purposes
	public static void main(String[] args) {
		/*  Initiator init;
		  init = new Initiator("PolicyBuilder");
		  init = new Initiator("SymmetricBindingBehavior");
		  init = new Initiator("TimeStampBehavior");
		  init = new Initiator("TimeStampBehavior");

		   init = new Initiator("org.wso2.carbon.policybuilder.ui.internal5");
		   init = new Initiator("org.wso2.carbon.policybuilder.ui.internal6");
		   init = new Initiator("org.wso2.carbon.policybuilder.ui.internal7");
		   init = new Initiator("org.wso2.carbon.policybuilder.ui.internal");
		   init.initiate();

		   BehaviorLoader bh = new BehaviorLoader();
		   try {
			   Object a =  bh.loadClass("MY").getConstructor(String.class).newInstance("asdaFDFD");
			   Class c = a.getClass();
			   Constructor m = c.getConstructor(String.class);
			   m.newInstance("sdfds");
			   m.newInstance();

			   System.out.println(s);
		   } catch (InstantiationException e) {
			   e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		   } catch (IllegalAccessException e) {
			   e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		   } catch (ClassNotFoundException e) {
			   e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		   } catch (InvocationTargetException e) {
			   e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		   } catch (NoSuchMethodException e) {
			   e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		   }
				*/
	}
}
