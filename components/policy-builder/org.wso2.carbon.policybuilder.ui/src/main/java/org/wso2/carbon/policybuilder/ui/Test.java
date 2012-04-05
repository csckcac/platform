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
package org.wso2.carbon.policybuilder.ui;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Feb 16, 2009
 * Time: 12:56:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class Test {

	// for Testing Purposes
	public static void main(String[] args) {
		DataHandler[] dh = new DataHandler[1];
		// dh[0] =    new DataHandler(new FileDataSource("/home/usw/soap.txt"));
		dh[0] = new DataHandler(new FileDataSource("/home/usw/soap.txt"));
		//  dh[1] =    new DataHandler(new FileDataSource("/home/usw/my.txt"));
		UploaderService up = new UploaderService();
		for (int i = 0; i < dh.length; i++) {
			String out = up.getString(dh[i]);
		
		}
	}
}
