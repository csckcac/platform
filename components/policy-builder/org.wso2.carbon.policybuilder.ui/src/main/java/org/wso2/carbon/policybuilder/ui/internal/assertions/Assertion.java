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
package org.wso2.carbon.policybuilder.ui.internal.assertions;

import org.wso2.carbon.policybuilder.ui.internal.property.MessageProperty;
import org.wso2.carbon.policybuilder.ui.internal.property.Property;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Nov 10, 2008
 * Time: 1:44:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class Assertion {

	private static Log log = LogFactory.getLog(Assertion.class);
	public boolean assert_completed = false;
	public boolean assert_apllying = false;
	public boolean nested_asserts = false;
	private String name;
	private int type;
	private String[] attributes;
	private String text;
	private Property assertionProperties;
	// private ArrayList properties;
	private ArrayList nestedAssertions;


	public Assertion(String name, int type) {
		this.name = name;
		nestedAssertions = new ArrayList();
		assertionProperties = null;
		attributes = new String[10];
	}

	public Assertion(int type) {
		nestedAssertions = new ArrayList();
		assertionProperties = null;
	}


	public Assertion(String name, int type, MessageProperty p) {
		this.name = name;
		nestedAssertions = new ArrayList();
		assertionProperties = p;
	}

	public void setChildAssertions(String name, int type) {
		if (name != null && type != -1) {
			nestedAssertions.add(new Assertion(name, type));
			this.nested_asserts = true;
		}
	}

	public void setChildAssertions(Assertion assertion) {
		if (assertion != null) {
			nestedAssertions.add(assertion);
			this.nested_asserts = true;
		}
	}


	public Iterator getNestedAssertions() {
		return nestedAssertions.iterator();
	}


	
}
