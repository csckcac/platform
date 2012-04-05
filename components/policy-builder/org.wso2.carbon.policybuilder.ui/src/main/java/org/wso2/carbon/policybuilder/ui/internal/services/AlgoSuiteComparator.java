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
package org.wso2.carbon.policybuilder.ui.internal.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Nov 26, 2008
 * Time: 11:00:47 AM
 * To change this template use File | Settings | File Templates.
 */
public class AlgoSuiteComparator implements Comparator {

	private static Log log = LogFactory.getLog(AlgoSuiteComparator.class);

	public int compare(Object o1, Object o2) {
		AlgoSuite a1 = (AlgoSuite) o1;
		AlgoSuite a2 = (AlgoSuite) o2;
		int diff = a1.getPriority() - a2.getPriority();
		if (diff < 0) {
			return 1;
		} else if (diff > 0) {
			return -1;
		}
		return 0;  //To change body of implemented methods use File | Settings | File Templates.
	}

	// for Testing Purposes
	public static void main(String[] args) {
		AlgoSuite a = new AlgoSuite("dsfsdf", 5);
		AlgoSuite b = new AlgoSuite("asda", 5);
		AlgoSuite c = new AlgoSuite("ghfghgf", 0);
		AlgoSuite d = new AlgoSuite("qqqqqqqqf", 3);
		PriorityQueue q = new PriorityQueue(4, new AlgoSuiteComparator());
		q.add(a);
		q.add(b);
		q.add(c);
		q.add(d);
		Iterator it = q.iterator();
		while (it.hasNext()) {
			AlgoSuite temp = (AlgoSuite) it.next();
			System.out.println(temp.getSuite() + " : " + temp.getPriority());
		}
		while (!q.isEmpty()) {
			AlgoSuite temp = (AlgoSuite) q.poll();
			System.out.println(temp.getSuite() + " : " + temp.getPriority());
		}
	}
}
