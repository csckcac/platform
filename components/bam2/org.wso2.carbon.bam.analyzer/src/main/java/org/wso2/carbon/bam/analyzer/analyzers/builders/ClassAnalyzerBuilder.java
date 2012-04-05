/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.analyzer.analyzers.builders;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.analyzer.Utils;
import org.wso2.carbon.bam.analyzer.analyzers.AnalyzerBuilder;
import org.wso2.carbon.bam.analyzer.analyzers.AnalyzerConfig;
import org.wso2.carbon.bam.analyzer.analyzers.configs.ClassConfig;
import org.wso2.carbon.bam.analyzer.engine.Analyzer;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerConfigConstants;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ClassAnalyzerBuilder extends AnalyzerBuilder {

    private static final Log log = LogFactory.getLog(ClassAnalyzerBuilder.class);

    @Override
    protected AnalyzerConfig buildConfig(OMElement analyzerXML) throws AnalyzerException {
        String analyzerClassName = analyzerXML.getAttribute(AnalyzerConfigConstants.className).
                getAttributeValue();
        ClassConfig config = new ClassConfig();
        config.setClassName(analyzerClassName);

        if (analyzerClassName == null || "".equals(analyzerClassName)) {
            throw new AnalyzerException("class attribute should not be empty..");
        }

        Iterator propertyIterator = analyzerXML.getChildrenWithLocalName(AnalyzerConfigConstants.
                                                                                 PROPERTY);

        Map<String, String> properties = new HashMap<String, String>();
        while (propertyIterator.hasNext()) {
            Object propertyElementObject = propertyIterator.next();
            if (!(propertyElementObject instanceof OMElement)) {
                return null;
            }

            OMElement propertyEl = (OMElement) propertyElementObject;
            OMAttribute name = propertyEl.getAttribute(AnalyzerConfigConstants.name);
            OMAttribute value = propertyEl.getAttribute(AnalyzerConfigConstants.value);

            properties.put(name.getAttributeValue(), value.getAttributeValue());
        }

        config.setProperties(properties);

        return config;

    }

    @Override
    public Analyzer buildAnalyzer(OMElement analyzerXML) throws AnalyzerException {
        ClassConfig config = (ClassConfig) buildConfig(analyzerXML);

        Object analyzerObject;
        try {
            Class analyzerClass = Class.forName(config.getClassName());
            analyzerObject = analyzerClass.newInstance();

            Field[] field = analyzerClass.getDeclaredFields();
            Method[] methods = analyzerClass.getMethods();

            Map<String, Method> setterMap = new HashMap<String, Method>();

            if (methods != null && field != null) {
                for (int i = 0; i < methods.length; i++) {
                    String methodName = methods[i].getName();
                    if (methodName.startsWith("set")) {
                        for (int j = 0; j < field.length; j++) {
                            String fieldName = methodName.substring(3).toLowerCase();
                            if (field[j].getName().contains(fieldName)) {
                                setterMap.put(fieldName, methods[i]);
                                break;
                            }
                        }
                    }
                }
            }

            Map<String, String> properties = config.getProperties();
            for (Map.Entry<String, String> propertyEntry : properties.entrySet()) {
                for (Map.Entry<String, Method> methodEntry : setterMap.entrySet()) {
                    if (methodEntry.getKey().equals(propertyEntry.getKey())) {
                        try {
                            methodEntry.getValue().invoke(analyzerObject, propertyEntry.getValue());
                        } catch (InvocationTargetException e) {
                            log.error("Error setting field " + methodEntry.getKey() + " for " +
                                      "class analyzer..");
                        }

                        break;
                    }
                }
            }

        } catch (ClassNotFoundException e) {
            String msg = config.getClassName() + "Class not available in the class path";
            throw new AnalyzerException(msg, e);
        } catch (InstantiationException e) {
            String msg = config.getClassName() + "Class cannot be instantiated or has no nullary" +
                         " constructor";
            throw new AnalyzerException(msg, e);
        } catch (IllegalAccessException e) {
            String msg = config.getClassName() + "Class has a private constructor";
            throw new AnalyzerException(msg, e);
        }

        if (!(analyzerObject instanceof Analyzer)) {
            throw new AnalyzerException(config.getClassName() + " does not implement the Analyzer" +
                                        " interface");
        }

        return (Analyzer) analyzerObject;
    }

}
