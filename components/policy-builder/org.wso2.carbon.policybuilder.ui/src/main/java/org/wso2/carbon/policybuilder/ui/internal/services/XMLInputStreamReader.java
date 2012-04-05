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

import java.io.Reader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Mar 18, 2009
 * Time: 1:00:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class XMLInputStreamReader extends XMLReader {

	private InputStream in;

	public XMLInputStreamReader(InputStream in) {
		this.in = in;
	}

	public Reader getReader() {
		return new InputStreamReader(in);  //To change body of implemented methods use File | Settings | File Templates.
	}
}
