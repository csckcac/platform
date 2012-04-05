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

import org.wso2.carbon.policybuilder.ui.internal.engine.PolicyBuilder;
import org.wso2.carbon.policybuilder.ui.internal.assembler.EngineAssembler;
import org.wso2.carbon.policybuilder.ui.internal.assembler.XMLAssembler;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.sql.DataSource;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Jan 12, 2009
 * Time: 10:07:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class UploaderService {

	private static Log log = LogFactory.getLog(UploaderService.class);

	public String getString(DataHandler soapFile) {
		String l = "";
		String inString = "";
		try {
			InputStream in = soapFile.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			while ((l = br.readLine()) != null) {
				inString = inString + l + "\n";
			}
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		if (log.isDebugEnabled()) {
			log.debug(inString);
		}
		String out = invokePolicyDerivation(inString);
		if (log.isInfoEnabled()) {
			log.info("String Output : \n" + out);
			log.info("Policy Extraction Process Completed...");
		}
		System.out.println(out);
		return "<![CDATA[\n" + out + "\n]]>";
	}

	public String getPolicy(String soap) {
		if (log.isDebugEnabled()) {
			log.debug("This is soap text from tracer\n" + soap);
		}
		String out = invokePolicyDerivation(soap);
		if (log.isInfoEnabled()) {
			log.info("String Output : \n" + out);
		}
		return "<![CDATA[\n" + out + "\n]]>";
	}


	private String invokePolicyDerivation(String input) {

		// PolicyBuilder pb = new PolicyBuilder();
		//String outString = pb.start(input);
		EngineAssembler engine = new XMLAssembler();
		return engine.assemble(input);
	}

	public String test(DataHandler soapFile) {
		return "Hi";
	}
}
