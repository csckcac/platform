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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

/**
 * This class provides access to properties files stored in the WAR of the application or in a project of the
 * IDE.
 * <p>
 * Child class should use the Singleton pattern to reduce access to the file system.
 * 
 * @author Gregor Latuske
 */
public abstract class AbstractPropertiesUtil {
    /** The logger instance. */
	private static final Log log = LogFactory.getLog(AbstractPropertiesUtil.class);

	/** The path to the properties files in the WAR. */
	private static final String CONFIG_PATH = "config/";

	/** The loaded properties. */
	private final Properties properties = new Properties();

	/**
	 * Constructor of AbstractPropertiesUtil.
	 * <p>
	 * Retrieves the properties file from file system.
	 * 
	 * @param propertiesFileName The name of the properties file.
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	protected AbstractPropertiesUtil(String propertiesFileName) throws URISyntaxException, IOException {
		String uri = getClass().getResource("").toURI().toString();
		URL url = null;
		InputStream inputStream = null;

		// 1): Application is running in the IDE
		if (uri.startsWith("file:/")) {
			url = new URL(getBinPath(uri) + propertiesFileName);
		} else if (uri.startsWith("jar:file:/")) {
			// 2): Application is running on the server
			url = new URL(getWarPath(uri) + propertiesFileName);

			// 3): Application is running on the server with IDE integration
			if (!new File(url.toURI()).exists()) {
				url = new URL(getJarPath(uri) + propertiesFileName);
			}
		} else if (uri.startsWith("bundleresource:/")) {
			// 4): Application is running on WSO2 server
			url = new URL(getWSO2Path(uri) + propertiesFileName);
		}

        if (url != null) {
            try {
                inputStream = url.openStream();
                this.properties.load(inputStream);
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } else {
            String cause = "Mentioned properties file:" + propertiesFileName + "doesn't have a correct url";
            log.error(cause, new NullPointerException(cause));
        }

    }

	/**
	 * Searches for the property with the specified key in this property list. The method returns null if the
	 * property is not found.
	 * 
	 * @param key The property key. Returns: the value in this property list with the specified key value.
	 * @returns null if the property is not found.
	 */
	public String getProperty(String key) {
		return this.properties.getProperty(key);
	}

	/**
	 * Returns the path to the properties file from the BIN directory and BUILD directory respectively.
	 * 
	 * @param uri The URI of the resource.
	 * @return The path to the properties file from the BIN directory and BUILD directory respectively.
	 */
	private String getBinPath(String uri) {
		int endIndex = uri.length();
		String bin = "/bin/";
		String build = "/build/classes/";

		// BIN or BUILD?
		if (uri.indexOf(bin) > 0) {
			endIndex = uri.indexOf(bin) + bin.length();
		} else if (uri.indexOf(build) > 0) {
			endIndex = uri.indexOf(build) + build.length();
		}

		uri = uri.substring(0, endIndex);

		return uri;
	}

	/**
	 * Returns the path to the properties file from the JAR file.
	 * 
	 * @param uri The URI of the resource.
	 * @return The path to the properties file from the JAR file.
	 */
	private String getJarPath(String uri) {
		int endIndex = uri.length();
		String jar = ".jar!/";

		if (uri.indexOf(jar) > 0) {
			endIndex = uri.indexOf(jar) + jar.length();
		}

		uri = uri.substring(0, endIndex);

		return uri;
	}

	/**
	 * Returns the path to the properties file from the WAR directory.
	 * 
	 * @param uri The URI of the resource.
	 * @return The path to the properties file from the WAR directory.
	 */
	private String getWarPath(String uri) {
		int endIndex = uri.length();
		String jar = "jar:";
		String webInf = "WEB-INF/";

		if (uri.indexOf(webInf) > 0) {
			endIndex = uri.indexOf(webInf) + webInf.length();
		}

		uri = uri.substring(jar.length(), endIndex) + CONFIG_PATH;

		return uri;
	}
	/**
	 * Returns the path to the properties file when deployed into WSO2 Carbon
	 * 
	 * @param uri The URI of the resource
	 * @return The path to the properties file
	 */
	private String getWSO2Path(String uri) {
		int endIndex = uri.length();
		
		// There might be a better way
		String cut = getClass().getPackage().getName();
		cut = cut.replace(".", "/");
		
		if (uri.indexOf(cut) > 0) {
			endIndex = uri.indexOf(cut);
		}

		uri = uri.substring(0, endIndex) + CONFIG_PATH;
		
		return uri;
	}

}
