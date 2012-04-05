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
package org.wso2.carbon.bam.analyzer.analyzers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.analyzer.analyzers.configs.Attribute;
import org.wso2.carbon.bam.analyzer.analyzers.configs.JMXConfig;
import org.wso2.carbon.bam.analyzer.analyzers.configs.Operation;
import org.wso2.carbon.bam.analyzer.analyzers.configs.Parameter;
import org.wso2.carbon.bam.analyzer.engine.DataContext;
import org.wso2.carbon.bam.core.dataobjects.Record;
import org.wso2.carbon.bam.core.persistence.PersistencyConstants;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/*
<jmx url="">
	<attributes>
		<attribute mbean='' name=''/>
	</attributes>
	<operations>
		<operation name='' mbean=''>
			<parameter type='' value=''/>
			<return type=''/>
		</operation>
	</operations>
</jmx>
 */

public class JMXAnalyzer extends AbstractAnalyzer {

    private static final Log log = LogFactory.getLog(JMXAnalyzer.class);

    private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private List<String> primitiveTypes;

    {
        primitiveTypes = new ArrayList<String>();

        primitiveTypes.add("java.lang.Short");
        primitiveTypes.add("java.lang.Integer");
        primitiveTypes.add("java.lang.Long");
        primitiveTypes.add("java.lang.Float");
        primitiveTypes.add("java.lang.Double");
        primitiveTypes.add("java.lang.String");

    }

    public JMXAnalyzer(AnalyzerConfig analyzerConfig) {
        super(analyzerConfig);
    }

    @Override
    public void analyze(DataContext dataContext) {

        JMXConfig config = (JMXConfig) getAnalyzerConfig();

        try {
            Map env = new HashMap();
            String[] creds = {"admin", "admin"};  // TODO: Get credentials using secure vault??
            env.put(JMXConnector.CREDENTIALS, creds);

            JMXServiceURL url =
                    new JMXServiceURL(config.getUrl());
            JMXConnector jmxc = JMXConnectorFactory.connect(url, env);

            MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

            List<Attribute> attributes = config.getAttributes();

            Map<String, String> values = new HashMap<String, String>();
            if (attributes != null) {
                for (Attribute attribute : attributes) {
                    Object value;
                    try {
                        value = mbsc.getAttribute(new ObjectName(attribute.getMbean()),
                                                  attribute.getName());
                    } catch (Exception e) {
                        log.error("Unable to fetch value of attribute " + attribute.getName() +
                                  "..", e);
                        continue;
                    }

                    if (value != null) {
                        insertToValueMap(attribute.getName(), value, values);
                    }
                }
            }

            List<Operation> operations = config.getOperations();

            if (operations != null) {
                for (Operation operation : operations) {

                    List<Parameter> parameters = operation.getParameters();

                    List<Object> parameterValues = null;
                    List<String> parameterTypes = null;
                    if (parameters != null) {

                        parameterValues = new ArrayList<Object>();
                        parameterTypes = new ArrayList<String>();
                        for (Parameter parameter : parameters) {
                            String parameterType = parameter.getType();
                            Object parameterValue = getPrimitiveObject(parameterType,
                                                                       parameter.getValue());

                            parameterValues.add(parameterValue);
                            parameterTypes.add(parameterType);
                        }
                    }

                    Object[] parameterValuesArray = null;
                    if (parameterValues != null) {
                        parameterValuesArray = parameterValues.toArray();
                    }

                    String[] parameterTypesArray = null;
                    if (parameterTypes != null) {
                        parameterTypesArray = parameterTypes.toArray(new String[]{});
                    }

                    Object value;
                    try {
                        value = mbsc.invoke(new ObjectName(operation.getMbean()),
                                            operation.getName(), parameterValuesArray,
                                            parameterTypesArray);
                    } catch (Exception e) {
                        log.error("Unable to fetch result of invocation of the operation " +
                                  operation.getName() + "..", e);
                        continue;
                    }

                    if (value != null) {
                        insertToValueMap(operation.getName(), value, values);
                    }
                }
            }

            String recordKey = formatter.format(new Date()) + PersistencyConstants.INDEX_DELIMITER +
                               UUID.randomUUID().toString();
            Record record = new Record(recordKey, values);

            List<Record> result = new ArrayList<Record>();
            result.add(record);

            setData(dataContext, result);

        } catch (MalformedURLException e) {
            log.error("JMX URL is invalid..", e);
        } catch (IOException e) {
            log.error("Unable to connect to JMX server..", e);
        }

    }

    private void insertToValueMap(String attribute, Object value, Map<String, String> values) {
        String className = value.getClass().getName();

        if (primitiveTypes.contains(className)) {
            values.put(attribute, value.toString());
        } else {
            insertComplexValueToMap(attribute, value, values);
        }
    }

    private void insertComplexValueToMap(String attribute, Object value,
                                         Map<String, String> values) {
        Class clazz = value.getClass();
        Method[] methods = clazz.getMethods();

        if (methods != null) {
            for (Method method : methods) {
                if (method.getName().startsWith("get")) {
                    try {
                        Object object = method.invoke(value);

                        String fieldName = method.getName().substring(3);
                        if (primitiveTypes.contains(object.getClass().getName())) {
                            values.put(attribute + "_" + fieldName, object.toString());
                        } else {
                            log.error("Attribute value should be a primitive or a bean with " +
                                      "primitive getters..");
                        }
                    } catch (IllegalAccessException e) {
                        log.error("Unable to fetch value for attribute " + attribute +
                                  "..");
                    } catch (InvocationTargetException e) {
                        log.error("Unable to fetch value for attribute " + attribute +
                                  "..");
                    }
                }
            }
        }
    }

    private Object getPrimitiveObject(String type, String value) {
        if (type.equals("java.lang.String")) {
            return value;
        } else if (type.equals("java.lang.Short")) {
            return Short.parseShort(value);
        } else if (type.equals("java.lang.Integer")) {
            return Integer.parseInt(value);
        } else if (type.equals("java.lang.Long")) {
            return Long.parseLong(value);
        } else if (type.equals("java.lang.Float")) {
            return Float.parseFloat(value);
        } else if (type.equals("java.lang.Double")) {
            return Double.parseDouble(value);
        } else {
            return value;
        }
    }

    public static void main(String[] args) {

        JMXConfig config = new JMXConfig("service:jmx:rmi:///jndi/rmi://:9999/jmxrmi");

        Attribute attribute0 = new Attribute("org.apache.synapse:Type=Endpoint,Name=endpoint_" +
                                             "3d13d60079b7d734b3a29fa3f18c5a8cee6133787a781409", "MetricsWindow");
        config.addAttribute(attribute0);

        Attribute attribute1 = new Attribute("org.apache.synapse:Type=Threading,Name=HttpClientWorker", "LastResetTime");
        config.addAttribute(attribute1);

        Attribute attribute2 = new Attribute("Tomcat:type=Connector,port=9443", "redirectPort");
        config.addAttribute(attribute2);

        Operation operation0 = new Operation("org.wso2.carbon:type=StatisticsAdmin", "getServiceRequestCount");

        Parameter parameter = new Parameter("echo", "java.lang.String");
        operation0.addParameter(parameter);

        config.addOperation(operation0);

        JMXAnalyzer analyzer = new JMXAnalyzer(config);
        analyzer.analyze(null);

    }

}
