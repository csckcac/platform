/*
   Copyright 2010 Gregor Latuske

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
*/
package org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.service.util;

import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.service.BPIService;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * This class loads the properties file of the {@link BPIService}.
 *
 * @author Gregor Latuske
 */
public class PropertiesUtil
	extends AbstractPropertiesUtil {

	/** The property name of properties file. */
	private static final String SERVICES_PROPERTIES_FILE = "services.properties";

	/** The instance of {@link PropertiesUtil} */
	private static PropertiesUtil instance;

	/**
	 * Constructor of PropertiesUtil.
	 *
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	private PropertiesUtil() throws URISyntaxException, IOException {
		super(SERVICES_PROPERTIES_FILE);
	}

	/**
	 * Returns the instance {@link PropertiesUtil}.
	 *
	 * @return The instance of {@link PropertiesUtil}.
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static synchronized PropertiesUtil getInstance() throws URISyntaxException, IOException {
        // Instance already created?
        if (instance == null) {
            instance = new PropertiesUtil();
        }
        return instance;
	}
}
