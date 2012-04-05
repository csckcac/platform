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

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Jan 19, 2009
 * Time: 12:17:11 PM
 * To change this template use File | Settings | File Templates.
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Hashtable;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;


public class BehaviorLoader extends ClassLoader {

	private static Log log = LogFactory.getLog(BehaviorLoader.class);

	private Hashtable classes = new Hashtable();


	public BehaviorLoader() {
	}

	/**
	 * This sample function for reading class implementations reads
	 * them from the local file system
	 */
	private byte getClassImplFromDataBase(String className)[] {
		if (log.isDebugEnabled()) {
			log.debug(">> Fetching the implementation of \"+className");
		}
		//System.out.println("        >>>>>> Fetching the implementation of "+className);
		byte result[];
		try {

			//  System.out.println(System.getProperty("user.dir")+"/src/main/resources/store/"+className.replace('.','/')+".impl");
			FileInputStream fi = new FileInputStream(System.getProperty("user.dir") + "/src/main/resources/store/" + className + ".impl");
			result = new byte[fi.available()];
			fi.read(result);
			return result;
		} catch (Exception e) {

			/*
						* If we caught an exception, either the class wasnt found or it
						* was unreadable by our process.
						*/
			return null;
		}
	}

	/**
	 * This is a simple version for external clients since they
	 * will always want the class resolved before it is returned
	 * to them.
	 */
	public Class loadClass(String className) throws ClassNotFoundException {
		return (loadClass(className, true));
	}

	/**
	 * This is the required version of loadClass which is called
	 * both from loadClass above and from the internal function
	 * FindClassFromClass.
	 */
	public synchronized Class loadClass(String className, boolean resolveIt)
			throws ClassNotFoundException {
		Class result;
		byte classData[];
		if (log.isDebugEnabled()) {
			log.debug(">> Load class : \"+className");
		}
		//System.out.println("        >>>>>> Load class : "+className);

		/* Check our local cache of classes */
		result = (Class) classes.get(className);
		if (result != null) {
			if (log.isDebugEnabled()) {
				log.debug(">> returning cached result.");
			}
			// System.out.println("        >>>>>> returning cached result.");
			return result;
		}

		/* Check DataBase */
		classData = getClassImplFromDataBase(className);
		if (classData != null) {

			/* Define it (parse the class file) */
			result = defineClass(className, classData, 0, classData.length);
			if (result != null) {
				if (log.isDebugEnabled()) {
					log.debug(">> Returning newly loaded class.");
				}
				//  System.out.println("        >>>>>> Returning newly loaded class.");
				if (resolveIt) {
					resolveClass(result);
				}
				classes.put(className, result);
				return result;
			} else {
				if (log.isDebugEnabled()) {
					log.debug(">> Malformed Class.");
				}
				// System.out.println("        >>>>>> Malformed Class.");
			}
		} else if (classData == null) {
			if (log.isDebugEnabled()) {
				log.debug(">> Classes not found in the repo");
			}
			// System.out.println("        >>>>>> Classes not found in the repo");
		}
		try {
			result = this.getClass().getClassLoader().loadClass(className);
			if (log.isDebugEnabled()) {
				log.debug(">> returning class from current class loader");
			}
			//System.out.println("        >>>>>> returning class from current class loader");
			return result;
		} catch (ClassNotFoundException e) {
			if (log.isDebugEnabled()) {
				log.debug(">> Not a current class loader.");
			}
			// System.out.println("        >>>>>> Not a current class loader.");
		}

		/* Check with the primordial class loader */
		try {
			result = super.findSystemClass(className);
			if (log.isDebugEnabled()) {
				log.debug(">> returning system class (in CLASSPATH).");
			}
			//  System.out.println("        >>>>>> returning system class (in CLASSPATH).");
			return result;
		} catch (ClassNotFoundException e) {
			if (log.isDebugEnabled()) {
				log.debug(">> Not a system class.");
			}
			// System.out.println("        >>>>>> Not a system class.");
		}

		/* Try to load it from our repository */
		result = super.findLoadedClass(className);
		if (result != null) {
			if (log.isDebugEnabled()) {
				log.debug(">> returning loaded classes");
			}
			// System.out.println("        >>>>>> returning loaded classes");
			return result;
		} else {
			throw new ClassNotFoundException();
		}
	}
}
