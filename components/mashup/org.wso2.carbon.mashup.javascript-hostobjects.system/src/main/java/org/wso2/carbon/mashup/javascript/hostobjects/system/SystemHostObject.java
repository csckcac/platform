/**
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.mashup.javascript.hostobjects.system;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.scriptengine.engine.RhinoEngine;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.mozilla.javascript.*;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.mashup.javascript.messagereceiver.JavaScriptEngineUtils;
import org.wso2.carbon.mashup.utils.MashupConstants;
import org.wso2.carbon.mashup.utils.MashupUtils;
import org.wso2.carbon.utils.NetworkUtils;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p/>
 * This is a JavaScript Rhino host object aimed to provide a set of system
 * specific utility functions to the javascript service developers.
 * </p>
 */
public class SystemHostObject extends ScriptableObject {

    private static final long serialVersionUID = 5003413793187124449L;

    private static final Log log = LogFactory.getLog(SystemHostObject.class);

    private static final String TASK_EXTENSION_NS =
            "http://www.wso2.org/products/wso2commons/tasks";

    private static final OMFactory FACTORY = OMAbstractFactory.getOMFactory();

    private static final OMNamespace TASK_OM_NAMESPACE =
            FACTORY.createOMNamespace(TASK_EXTENSION_NS, "task");

    public void jsConstructor() {
    }

    /**
     * Type to be used for this object inside the javascript.
     */
    public String getClassName() {
        return "System";
    }

    /**
     * <p/>
     * Waits the execution of the script for the given time in miliseconds or
     * waits for 10 miliseconds when the time is not given.
     * </p>
     * <p/>
     * <pre>
     * system.wait();
     * system.wait(1000);
     * </pre>
     *
     * @throws CarbonException Thrown in case any exceptions occur
     */
    public static void jsFunction_wait(Context cx, Scriptable thisObj, Object[] arguments,
                                       Function funObj) throws CarbonException {
        try {
            if (arguments.length > 1) {
                throw new CarbonException("Invalid number of arguments.");
            }
            if (arguments.length == 0) {
                Thread.sleep(10);
            } else if (arguments[0] instanceof String) {
                String timePeriod = (String) arguments[0];
                Thread.sleep(Long.parseLong(timePeriod));
            } else if (arguments[0] instanceof Integer) {
                Integer timePeriod = (Integer) arguments[0];
                Thread.sleep(timePeriod.longValue());
            } else {
                throw new CarbonException("Unsupported parameter.");
            }
        } catch (Throwable e) {
            throw new CarbonException(e);
        }
    }

    /**
     * <p/>
     * Get the string defining the hostname of the system.
     * </p>
     * <p/>
     * <pre>
     * var hostname = system.localHostName;
     * </pre>
     *
     * @throws CarbonException Thrown in case any exceptions occur
     */
    public String jsGet_localHostName() throws CarbonException {
        try {
            return NetworkUtils.getLocalHostname();
        } catch (SocketException e) {
            throw new CarbonException(e);
        }
    }

    /**
     * <p/>
     * Imports the external scripts given as the arguments, to the java script
     * run time. Paths of the scripts to be imported can be given using a comma
     * separated list as arguments. Imported script files needs to be placed in
     * the {service_file_name}.resources directory of the mashup service. If the
     * path represents a file it should be given relative to the
     * {service_file_name}.resources directory. If a file is not found at this
     * location, the path is treated as an URL. The URL can be given relative to
     * the services context root (eg: http://127.0.0.1:7762/services) or it can
     * also be an absolute URL.
     * </p>
     * <p/>
     * <pre>
     * system.include(&quot;include.js&quot;);
     * system.include(&quot;version?stub&amp;lang=e4x&quot;, &quot;lib2.js&quot;);
     * system.include(&quot;http://tempuri.org/js/temp.js&quot;);
     * </pre>
     *
     * @throws CarbonException Thrown in case any exceptions occur
     */
    public static void jsFunction_include(Context cx, Scriptable thisObj, Object[] arguments,
                                          Function funObj) throws CarbonException {

        AxisService axisService;
        // retrieves the AxisService object from the Rhino context
        Object axisServiceObject = cx.getThreadLocal(MashupConstants.AXIS2_SERVICE);
        if (axisServiceObject != null && axisServiceObject instanceof AxisService) {
            axisService = (AxisService) axisServiceObject;
        } else {
            throw new CarbonException("Error obtaining the Service Meta Data: Axis2 Service");
        }

        // Retrieves the service.resources directory corresponding to this
        // mashup service
        Parameter parameter = axisService.getParameter(MashupConstants.RESOURCES_FOLDER);
        Object resourceFileObject = parameter.getValue();
        File resourceFolder;
        if (resourceFileObject != null && resourceFileObject instanceof File) {
            resourceFolder = (File) resourceFileObject;
        } else {
            throw new CarbonException("Mashup Resources folder not found.");
        }

        // Creates the base URI for URI resolving. URI's are resolved relative
        // to the serviceContextRoot of the Mashup server.
        // (eg:http://localhost:7762/services/)
        URI baseURI;
        //todo need to handle this scenario
        /*try {
            String contextPath = AdminUIServletContextListener.contextPath;
            if (!contextPath.endsWith(MashupConstants.FORWARD_SLASH)) {
                contextPath += MashupConstants.FORWARD_SLASH;
            }
            baseURI = new URI("http", null, NetworkUtils.getLocalHostname(),
                              ServerManager.getInstance().getHttpPort(),
                              contextPath +
                                      configurationContext.getServicePath() + "/",
                              null, null);
        } catch (Exception e) {
            throw new CarbonException("Cannot create the server base URI.", e);
        }*/

        RhinoEngine engine = JavaScriptEngineUtils.getEngine();
        ConfigurationContext configurationContext =
                (ConfigurationContext) RhinoEngine.getContextProperty(MashupConstants.AXIS2_CONFIGURATION_CONTEXT);

        for (Object argument : arguments) {
            Reader reader;
            String path = argument.toString();
            File f = new File(resourceFolder, path);

            try {
                // Check whether this is a file in the service.resources directory
                if (f.exists() && !f.isDirectory()) {
                    reader = new FileReader(f);
                    //TODO : we need to cache this too, store updated time against path as an axis param
                    engine.exec(reader, JavaScriptEngineUtils.getActiveScope(), MashupUtils.getScriptCachingContext(
                            axisService, configurationContext, f.getAbsolutePath(), f.lastModified()));
                } else {
                    // This is not a file.. So we check whether this is a URL
                    //todo need to check this
                    //                    readFromURI(engine, baseURI, path);
                }
            } catch (IOException e) {
                throw new CarbonException(e);
            } catch (ScriptException e) {
                throw new CarbonException(e);
            }
        }
    }

    private static SystemHostObject checkInstance(Scriptable obj) {
        if (obj == null || !(obj instanceof SystemHostObject)) {
            throw Context.reportRuntimeError("Called on incompatible object");
        }
        return (SystemHostObject) obj;
    }

    /**
     * <p/>
     * This method allows the scheduling of a JavaScript function periodically. There are 2 mandatory parameters.
     * A javascript function (or a javascript expression) and the time interval between two consecutive executions. Optionally one can specify a start time, indicating
     * when to begin the function execution (after given number of milliseconds in the frequency parameter by default). It is also possible to give a start time and an end time.
     * <p/>
     * The method returns a String UUID, which can be used to refer to this function scheduling instance.
     * </p>
     * <p/>
     * <p/>
     * Imagine you have a javascript function in your service as follows
     * <p/>
     * <pre>
     * function myJavaScriptFunction(function-parameter)
     * {
     *      print("The parameter value is " + function-parameter);
     * }
     * </pre>
     * </p>
     * <p/>
     * example 1:
     * <pre>
     *    //Setting up 'myJavaScriptFunction' to be executed in 2000 millisecond intervals, starting now and continuing forever.
     *    var id = system.setInterval(myJavaScriptFunction, 2000, 'I am a parameter value');
     * </pre>
     * <p/>
     * example 2:
     * <pre>
     *    //Setting up 'myJavaScriptFunction' to be executed in 2000 millisecond intervals, starting now and continuing forever.
     *    //But passing the function as a javascript expression.
     *    var id = system.setInterval('myJavaScriptFunction("I am a parameter value");', 2000);
     * </pre>
     * <p/>
     * example 3:
     * <pre>
     *    //Setting to start in 2 minutes from now
     *    var startTime = new Date();
     *    startTime.setMinutes(startTime.getMinutes() + 2);
     * <p/>
     *    var id = system.setInterval(myJavaScriptFunction, 2000, 'I am a parameter value', startTime);
     *    or
     *    var id = system.setInterval('myJavaScriptFunction("I am a parameter value");', 2000, null, startTime);
     * </pre>
     * <p/>
     * example 4:
     * <pre>
     *    //Setting to start in 2 minutes from now
     *    var startTime = new Date();
     *    startTime.setMinutes(startTime.getMinutes() + 2);
     * <p/>
     *    //Setting to end in 4 minutes after starting
     *    var endtime = new Date();
     *    endtime.setMinutes(startTime.getMinutes() + 4);
     * <p/>
     *    var id = system.setInterval(myJavaScriptFunction, 2000, 'I am a parameter value', startTime, endtime);
     *    or
     *    var id = system.setInterval('myJavaScriptFunction("I am a parameter value");', 2000, null, startTime, endtime);
     * </pre>
     *
     * @throws CarbonException Thrown in case any exceptions occur
     */

    public static String jsFunction_setInterval(Context cx, Scriptable thisObj, Object[] arguments,
                                                Function funObj) throws CarbonException, AxisFault, IOException {

        // sanity check
        SystemHostObject system = checkInstance(thisObj);

        AxisService axisService;
        // retrieves the AxisService object from the Rhino context
        Object axisServiceObject = cx.getThreadLocal(MashupConstants.AXIS2_SERVICE);

        if (axisServiceObject != null && axisServiceObject instanceof AxisService) {
            axisService = (AxisService) axisServiceObject;
        } else {
            throw new CarbonException("Error obtaining the Service Meta Data: Axis2 Service");
        }
        ConfigurationContext configurationContext;
        // retrieves the ConfigurationContext object from the Rhino Engine
        Object configurationContextObject =
                cx.getThreadLocal(MashupConstants.AXIS2_CONFIGURATION_CONTEXT);
        if (configurationContextObject != null &&
            configurationContextObject instanceof ConfigurationContext) {
            configurationContext = (ConfigurationContext) configurationContextObject;
        } else {
            throw new CarbonException(
                    "Error obtaining the Service Meta Data : Axis2 ConfigurationContext");
        }

        //Generating UUID + current time for the taskName
        String taskName =
                system.getFormattedCurrentDateTime() + "-" + UUIDGenerator.getUUID().substring(9);

        int argCount = arguments.length;
        Object jsFunction = null;
        Object[] functionParams = null;
        long frequency = 0;
        Date startTime = null;
        Date endTime = null;
        final Map<String, String> resources = new HashMap<String, String>();
        resources.put(MSTaskConstants.AXIS_SERVICE, axisService.getName());
        MSTaskInfo msTaskInfo = new MSTaskInfo();
        
        switch (argCount) {

            case 2://A javascript function and its execution frequency were passed

                //Extracting the javascript function from the arguments
                if ((arguments[0] instanceof Function) || ((arguments[0] instanceof String))) {
                    jsFunction = arguments[0];
                } else {
                    throw new CarbonException("Invalid parameter. The first parameter must be " +
                                              "a JavaScript function.");
                }

                //Extracting the frequency from the arguments
                if (arguments[1] != null && arguments[1] instanceof Number) {
                    frequency = ((Number) arguments[1]).longValue();
                } else {
                    throw new CarbonException("Invalid parameter. The second parameter " +
                                              "must be the execution frequency in milliseconds.");
                }

                //Storing the function meta-data to be used by the job at execution time
                resources.put(MSTaskConstants.FUNCTION_PARAMETERS, MSTaskUtils.toString(functionParams));
                resources.put(MSTaskConstants.TASK_NAME, taskName);
                axisService.addParameter(taskName, jsFunction);

                //Creating the trigger. There will be a one-to-one mapping between jobs and triggers in this implementation
                msTaskInfo.setName(taskName);
                msTaskInfo.setTaskCount(MSTaskConstants.REPEAT_INDEFINITELY);
                msTaskInfo.setTaskInterval(frequency);
                break;

            case 3://A javascript function its execution frequency and parameters were passed

                //Extracting the javascript function from the arguments=
                if ((arguments[0] instanceof Function) || ((arguments[0] instanceof String))) {
                    jsFunction = arguments[0];
                } else {
                    throw new CarbonException("Invalid parameter. The first parameter must " +
                                              "be a JavaScript function.");
                }

                //Extracting the frequency from the arguments
                if (arguments[1] != null && arguments[1] instanceof Number) {
                    frequency = ((Number) arguments[1]).longValue();
                } else {
                    throw new CarbonException(
                            "Invalid parameter. The second parameter must be the " +
                            "execution frequency in milliseconds.");
                }

                //Extracting function parameters from the arguments
                if (arguments[2] != null) {

                    if (arguments[2] instanceof NativeArray) {
                        NativeArray nativeArray = (NativeArray) arguments[2];
                        Object[] objects = nativeArray.getIds();
                        ArrayList tempParamHolder = new ArrayList();
                        for (int i = 0; i < objects.length; i++) {
                            Object currObject = objects[i];
                            if (currObject instanceof String) {
                                String property = (String) currObject;
                                if ("length".equals(property)) {
                                    continue;
                                }
                                tempParamHolder.add(nativeArray.get(property, nativeArray));
                            } else {
                                Integer property = (Integer) currObject;
                                tempParamHolder
                                        .add(nativeArray.get(property.intValue(), nativeArray));
                            }
                        }
                        //Convert the arraylist to an object array
                        functionParams = new Object[tempParamHolder.size()];
                        tempParamHolder.toArray(functionParams);

                    } else if (arguments[2] instanceof String) {
                        taskName = (String) arguments[2];
                    } else {
                        throw new CarbonException(
                                "Invalid parameter. The third parameter must be an Array " +
                                "of parameters to the argument, a string value for the task name or null.");
                    }
                }

                //Storing the function meta-data to be used by the job at execution time
                resources.put(MSTaskConstants.FUNCTION_PARAMETERS, MSTaskUtils.toString(functionParams));
                resources.put(MSTaskConstants.TASK_NAME, taskName);
                axisService.addParameter(taskName, jsFunction);

                //Creating the trigger. There will be a one-to-one mapping between jobs and triggers in this implementation
                msTaskInfo.setName(taskName);
                msTaskInfo.setTaskCount(MSTaskConstants.REPEAT_INDEFINITELY);
                msTaskInfo.setTaskInterval(frequency);
                break;

            case 4:// A javascript function, its execution frequnecy, function parameters and a start time is passed.

                //Extracting the javascript function from the arguments
                if ((arguments[0] instanceof Function) || ((arguments[0] instanceof String))) {
                    jsFunction = arguments[0];
                } else {
                    throw new CarbonException(
                            "Invalid parameter. The first parameter must be a JavaScript function.");
                }

                //Extracting the frequency from the arguments
                if (arguments[1] != null && arguments[1] instanceof Number) {
                    frequency = ((Number) arguments[1]).longValue();
                } else {
                    throw new CarbonException(
                            "Invalid parameter. The second parameter must be the execution " +
                            "frequency in milliseconds.");
                }

                //Extracting function parameters from the arguments
                if (arguments[2] != null) {
                    if (arguments[2] instanceof NativeArray) {
                        NativeArray nativeArray = (NativeArray) arguments[2];
                        Object[] objects = nativeArray.getIds();
                        ArrayList tempParamHolder = new ArrayList();
                        for (int i = 0; i < objects.length; i++) {
                            Object currObject = objects[i];
                            if (currObject instanceof String) {
                                String property = (String) currObject;
                                if ("length".equals(property)) {
                                    continue;
                                }
                                tempParamHolder.add(nativeArray.get(property, nativeArray));
                            } else {
                                Integer property = (Integer) currObject;
                                tempParamHolder
                                        .add(nativeArray.get(property.intValue(), nativeArray));
                            }
                        }
                        //Convert the arraylist to an object array
                        functionParams = new Object[tempParamHolder.size()];
                        tempParamHolder.toArray(functionParams);
                    } else {
                        throw new CarbonException(
                                "Invalid parameter. The third parameter must be an Array of " +
                                "parameters to the argument or null.");
                    }
                }

                if (arguments[3] != null) {
                    if (arguments[3] instanceof String) {
                        taskName = (String) arguments[3];
                    } else {
                        try {
                            startTime = (Date) Context.jsToJava(arguments[3], Date.class);
                        } catch (EvaluatorException e) {
                            throw new CarbonException(
                                    "Invalid parameter. The fourth parameter must be " +
                                    "the start time in date format or a string value " +
                                    "for the task name.", e);
                        }
                    }
                }

                //Storing the function meta-data to be used by the job at execution time
                resources.put(MSTaskConstants.FUNCTION_PARAMETERS, MSTaskUtils.toString(functionParams));
                resources.put(MSTaskConstants.TASK_NAME, taskName);
                axisService.addParameter(taskName, jsFunction);

                msTaskInfo.setName(taskName);
                msTaskInfo.setTaskCount(MSTaskConstants.REPEAT_INDEFINITELY);
                msTaskInfo.setTaskInterval(frequency);
                msTaskInfo.setStartTime(MSTaskUtils.dateToCal(startTime));

                break;

            case 5: // A javascript function, its execution frequnecy, function parameters, start time and an end time is passed.

                //Extracting the javascript function from the arguments
                if ((arguments[0] instanceof Function) || ((arguments[0] instanceof String))) {
                    jsFunction = arguments[0];
                } else {
                    throw new CarbonException("Invalid parameter. The first parameter must be a " +
                                              "JavaScript function.");
                }

                //Extracting the frequency from the arguments
                if (arguments[1] != null && arguments[1] instanceof Number) {
                    frequency = ((Number) arguments[1]).longValue();
                } else {
                    throw new CarbonException(
                            "Invalid parameter. The second parameter must be the execution " +
                            "frequency in milliseconds.");
                }

                //Extracting function parameters from the arguments
                if (arguments[2] != null) {
                    if (arguments[2] instanceof NativeArray) {
                        NativeArray nativeArray = (NativeArray) arguments[2];
                        Object[] objects = nativeArray.getIds();
                        ArrayList tempParamHolder = new ArrayList();
                        for (int i = 0; i < objects.length; i++) {
                            Object currObject = objects[i];
                            if (currObject instanceof String) {
                                String property = (String) currObject;
                                if ("length".equals(property)) {
                                    continue;
                                }
                                tempParamHolder.add(nativeArray.get(property, nativeArray));
                            } else {
                                Integer property = (Integer) currObject;
                                tempParamHolder
                                        .add(nativeArray.get(property.intValue(), nativeArray));
                            }
                        }
                        //Convert the arraylist to an object array
                        functionParams = new Object[tempParamHolder.size()];
                        tempParamHolder.toArray(functionParams);
                    } else {
                        throw new CarbonException(
                                "Invalid parameter. The third parameter must be an Array of " +
                                "parameters to the argument or null.");
                    }
                }

                //Extracting the start time from the arguments
                if (arguments[3] != null) {
                    if (arguments[3] instanceof String) {
                        taskName = (String) arguments[3];
                    } else {
                        try {
                            startTime = (Date) Context.jsToJava(arguments[3], Date.class);
                        } catch (EvaluatorException e) {
                            throw new CarbonException(
                                    "Invalid parameter. The fourth parameter must be " +
                                    "the start time in date format.", e);
                        }
                    }
                }

                //Extracting the end time from the arguments
                if (arguments[4] != null) {
                    if (arguments[4] instanceof String) {
                        taskName = (String) arguments[4];
                    } else {
                        try {
                            endTime = (Date) Context.jsToJava(arguments[4], Date.class);
                        } catch (EvaluatorException e) {
                            throw new CarbonException(
                                    "Invalid parameter. The fifth parameter must be " +
                                    "the end time in date format or a string value " +
                                    "for the task name.", e);
                        }
                    }
                }

                //Storing the function meta-data to be used by the job at execution time
                resources.put(MSTaskConstants.FUNCTION_PARAMETERS, MSTaskUtils.toString(functionParams));
                resources.put(MSTaskConstants.TASK_NAME, taskName);
                axisService.addParameter(taskName, jsFunction);

                msTaskInfo.setName(taskName);
                msTaskInfo.setTaskCount(MSTaskConstants.REPEAT_INDEFINITELY);
                msTaskInfo.setTaskInterval(frequency);
                msTaskInfo.setStartTime(MSTaskUtils.dateToCal(startTime));
                msTaskInfo.setEndTime(MSTaskUtils.dateToCal(endTime));

                break;

            case 6: // A javascript function, its execution frequnecy, function parameters, start time and an end time is passed.

                //Extracting the javascript function from the arguments
                if ((arguments[0] instanceof Function) || ((arguments[0] instanceof String))) {
                    jsFunction = arguments[0];
                } else {
                    throw new CarbonException("Invalid parameter. The first parameter must be a " +
                                              "JavaScript function.");
                }

                //Extracting the frequency from the arguments
                if (arguments[1] != null && arguments[1] instanceof Number) {
                    frequency = ((Number) arguments[1]).longValue();
                } else {
                    throw new CarbonException(
                            "Invalid parameter. The second parameter must be the execution " +
                            "frequency in milliseconds.");
                }

                //Extracting function parameters from the arguments
                if (arguments[2] != null) {
                    if (arguments[2] instanceof NativeArray) {
                        NativeArray nativeArray = (NativeArray) arguments[2];
                        Object[] objects = nativeArray.getIds();
                        ArrayList tempParamHolder = new ArrayList();
                        for (int i = 0; i < objects.length; i++) {
                            Object currObject = objects[i];
                            if (currObject instanceof String) {
                                String property = (String) currObject;
                                if ("length".equals(property)) {
                                    continue;
                                }
                                tempParamHolder.add(nativeArray.get(property, nativeArray));
                            } else {
                                Integer property = (Integer) currObject;
                                tempParamHolder
                                        .add(nativeArray.get(property.intValue(), nativeArray));
                            }
                        }
                        //Convert the arraylist to an object array
                        functionParams = new Object[tempParamHolder.size()];
                        tempParamHolder.toArray(functionParams);
                    } else {
                        throw new CarbonException(
                                "Invalid parameter. The third parameter must be an Array of " +
                                "parameters to the argument or null.");
                    }
                }

                //Extracting the start time from the arguments
                if (arguments[3] != null) {
                    try {
                        startTime = (Date) Context.jsToJava(arguments[3], Date.class);
                    } catch (EvaluatorException e) {
                        throw new CarbonException(
                                "Invalid parameter. The fourth parameter must be " +
                                "the start time in date format.", e);
                    }
                }

                if (arguments[4] != null) {
                    try {
                        endTime = (Date) Context.jsToJava(arguments[4], Date.class);
                    } catch (EvaluatorException e) {
                        throw new CarbonException(
                                "Invalid parameter. The fifth parameter must be " +
                                "the end time in date format.", e);
                    }
                }

                if (arguments[5] != null) {
                    if (arguments[5] instanceof String) {
                        taskName = (String) arguments[5];
                    } else {
                        throw new CarbonException(
                                "Invalid parameter. The sixth parameter must be a string value " +
                                "for the task name");
                    }
                }

                //Storing the function meta-data to be used by the job at execution time
              
                resources.put(MSTaskConstants.FUNCTION_PARAMETERS, MSTaskUtils.toString(functionParams));
                resources.put(MSTaskConstants.TASK_NAME, taskName);
                axisService.addParameter(taskName, jsFunction);

                msTaskInfo.setName(taskName);
                msTaskInfo.setTaskCount(MSTaskConstants.REPEAT_INDEFINITELY);
                msTaskInfo.setTaskInterval(frequency);
                msTaskInfo.setStartTime(MSTaskUtils.dateToCal(startTime));
                msTaskInfo.setEndTime(MSTaskUtils.dateToCal(endTime));

                break;

            default:
                throw new CarbonException("Invalid number of parameters.");
        }

        msTaskInfo.setTaskProperties(resources);
        MSTaskAdmin taskAdmin = new MSTaskAdmin();

        axisService.addParameter(MSTaskConstants.CONTEXT_FACTORY, cx.getFactory());
        axisService.addParameter(MSTaskConstants.TASK_SCOPE, thisObj);

        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put(MSTaskConstants.JAVASCRIPT_FUNCTION, jsFunction);
        paramMap.put(MSTaskConstants.FUNCTION_PARAMETERS, functionParams);
        paramMap.put(MSTaskConstants.AXIS_SERVICE, axisService);
        paramMap.put(MSTaskConstants.TASK_NAME, taskName);
        paramMap.put(MashupConstants.AXIS2_CONFIGURATION_CONTEXT, configurationContext);
        try {
            if (axisService.getParameterValue(
                    MSTaskConstants.JS_FUNCTION_MAP) != null) {
                // JobDataMap is added to AxisConfiguration
                HashMap tasksMap = (HashMap)axisService.getParameterValue(MSTaskConstants.JS_FUNCTION_MAP);
                tasksMap.put(taskName, paramMap);

            } else {
                // no function map in AxisConfiguration, new one is created
                HashMap tasksMap = new HashMap();
                tasksMap.put(taskName, paramMap);
                Parameter parameter = new Parameter(MSTaskConstants.JS_FUNCTION_MAP, tasksMap);
                axisService.addParameter(parameter);
            }
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
        
        try {
			taskAdmin.scheduleTask(msTaskInfo);
		} catch (AxisFault e) {
			throw new CarbonException("Unable to create the scheduling task");
		}

        return taskName;
    }

    /**
     * <p/>
     * Removes a JavaScript function scheduled for periodic execution using the job id
     * </p>
     * <p/>
     * <pre>
     *   system.clearInterval(id);
     * </pre>
     *
     * @throws CarbonException Thrown in case any exceptions occur
     */

    public static void jsFunction_clearInterval(Context cx, Scriptable thisObj, Object[] arguments,
                                                Function funObj) throws CarbonException {

        ConfigurationContext configurationContext;
        // retrieves the ConfigurationContext object from the Rhino Engine
        Object configurationContextObject =
                cx.getThreadLocal(MashupConstants.AXIS2_CONFIGURATION_CONTEXT);
        if (configurationContextObject != null &&
            configurationContextObject instanceof ConfigurationContext) {
            configurationContext = (ConfigurationContext) configurationContextObject;
        } else {
            throw new CarbonException(
                    "Error obtaining the Service Meta Data : Axis2 ConfigurationContext");
        }

        if (arguments[0] instanceof String) {
            deleteJob(arguments, configurationContext);
        } else {
            throw new CarbonException("Invalid parameter");
        }

    }

    private static void deleteJob(Object[] arguments, ConfigurationContext configCtx) {
        String taskName = (String) arguments[0];

        MSTaskAdmin taskAdmin = new MSTaskAdmin();
        try {
			taskAdmin.deleteTask(taskName);
		} catch (AxisFault e) {
			log.error("Unable to delete job : " + e.getFaultAction());
		}
    }

    public static boolean jsFunction_isTaskActive(Context cx, Scriptable thisObj,
                                                  Object[] arguments, Function funObj)
            throws CarbonException, AxisFault {

        if (arguments[0] instanceof String) {
            ConfigurationContext configurationContext;
            // retrieves the ConfigurationContext object from the Rhino Engine
            Object configurationContextObject =
                    cx.getThreadLocal(MashupConstants.AXIS2_CONFIGURATION_CONTEXT);
            if (configurationContextObject != null &&
                configurationContextObject instanceof ConfigurationContext) {
                configurationContext = (ConfigurationContext) configurationContextObject;
            } else {
                throw new CarbonException(
                        "Error obtaining the Service Meta Data : Axis2 ConfigurationContext");
            }
            MSTaskAdmin taskAdmin = new MSTaskAdmin();
            
            return taskAdmin.isTaskScheduled((String) arguments[0]);
        } else {
            return false;
        }
    }

    /**
     * <p>
     * Sends notification to a monitoring software via JMX. A single parameter is assumed to be the
     * message, while if another string is provided it is the title. A third parameter , if integer,
     * is assumed to be the message severity (info = 0, error = 1, warning = 2 and none = 3).
     * </p>
     * <pre>
     * system.notifyMonitor("Message");
     * system.notifyMonitor("Title", "Message");
     * system.notifyMonitor("Title", "Message", 3);
     * </pre>
     *
     * @throws MashupFault Thrown in case any exceptions occur
     */
    // todo lets revisit this once we get the System Tray component onto carbon
    /*public static void jsFunction_notifyMonitor(Context cx, Scriptable thisObj, Object[] arguments,
                                                Function funObj) throws MashupFault {
        String message;
        String title;
        int severity;
        try {
            SystemHostObject system = checkInstance(thisObj);
            if (arguments.length > 3 || arguments.length < 1) {
                throw new MashupFault("Invalid number of arguments.");
            }
            if (arguments.length == 1 && arguments[0] instanceof String) {
                message = (String) arguments[0];
                MashupUtils.notifyMonitor("Message", message, 3);
            } else if (arguments.length == 2 && arguments[0] instanceof String &&
                    arguments[1] instanceof String) {
                message = (String) arguments[0];
                title = (String) arguments[1];
                MashupUtils.notifyMonitor(title, message, 3);
            } else if (arguments.length == 3 && arguments[0] instanceof String &&
                    arguments[1] instanceof String && arguments[2] instanceof Integer) {
                message = (String) arguments[0];
                title = (String) arguments[1];
                severity = ((Integer) arguments[2]).intValue();
                MashupUtils.notifyMonitor(title, message, severity);
            } else {
                throw new MashupFault("Unsupported parameter.");
            }
        } catch (Throwable e) {
            throw new MashupFault(e);
        }
    }*/

    /**
     * <p>
     * Utility function to get an XML file over the network
     * </p>
     * <pre>
     * var history = system.getXML('http://wso2.org/repos/wso2/trunk/mashup/java/modules/samples/upgradeChecker/upgradeChecker.resources/history.xml');
     * <p/>
     * You can also do basic authentication by giving a username/password
     * <p/>
     * var history = system.getXML('http://wso2.org/repos/wso2/trunk/mashup/java/modules/samples/upgradeChecker/upgradeChecker.resources/history.xml', 'username', 'password');
     * </pre>
     *
     * @throws CarbonException Thrown in case any exceptions occur
     */
    public static Object jsFunction_getXML(Context context, Scriptable thisObj, Object[] arguments,
                                           Function funObj) throws CarbonException {
        if (arguments[0] == null || !(arguments[0] instanceof String)) {
            throw new CarbonException(
                    "The getXML function should be called with either a single parameter which is " +
                    "the url to fetch XML from or three parameters, which are the url to fetch XML " +
                    "from, the User Name and a Password for basic authentication.");
        }

        String urlString;
        String username = null;
        String password = null;

        if ((arguments.length > 1) && (arguments.length < 4)) {
            urlString = (String) arguments[0];

            // We have a username password combo as well
            if ((arguments[1] == null) || (!(arguments[1] instanceof String))) {
                throw new CarbonException(
                        "The second argument for getXML function should be a string containing the username ");
            } else {
                username = (String) arguments[1];
            }
            if ((arguments[2] == null) || (!(arguments[2] instanceof String))) {
                throw new CarbonException(
                        "The third argument for getXML function should be a string containing the password ");
            } else {
                password = (String) arguments[2];
            }
        } else if (arguments.length == 1) {
            // We have only a URL
            urlString = (String) arguments[0];
        } else {
            throw new CarbonException(
                    "The getXML function should be called with either a single parameter which is " +
                    "the url to fetch XML from or three parameters, which are the url to fetch XML " +
                    "from, the User Name and a Password for basic authentication.");
        }
        HttpMethod method = new GetMethod(urlString);
        try {
            URL url = new URL(urlString);
            int statusCode = MashupUtils.executeHTTPMethod(method, url, username, password);
            if (statusCode != HttpStatus.SC_OK) {
                throw new CarbonException(
                        "An error occured while getting the resource at " + url + ". Reason :" +
                        method.getStatusLine());
            }
            StAXOMBuilder staxOMBuilder =
                    new StAXOMBuilder(new ByteArrayInputStream(method.getResponseBody()));
            OMElement omElement = staxOMBuilder.getDocumentElement();
            Object[] objects = {omElement};
            return context.newObject(thisObj, "XML", objects);
        } catch (MalformedURLException e) {
            throw new CarbonException(e);
        } catch (IOException e) {
            throw new CarbonException(e);
        } catch (XMLStreamException e) {
            throw new CarbonException("Could not get the convert the content of " + urlString +
                                      " to XML. You may have " +
                                      "to use the scraper object to get this url and tidy it");
        } finally {
            // Release the connection.
            method.releaseConnection();
        }
    }

    /**
     * setTimeout() allows you to specify that a piece of JavaScript code (called an expression) will be run a specified number
     * of milliseconds from when the setTimeout() method was called.
     * <p/>
     * <p/>
     * <pre>
     *    ex: setTimeout (expression, timeout);
     * </pre>
     * <p/>
     * <p/>
     * where expression is the JavaScript code to run after timeout milliseconds have elapsed.
     * <p/>
     * setTimeout() also returns a numeric timeout ID that can be used to track the timeout. This is most commonly used with the clearTimeout() method
     *
     * @throws CarbonException Thrown in case any exceptions occur
     * @throws IOException 
     */

    public static String jsFunction_setTimeout(Context cx, Scriptable thisObj, Object[] arguments,
                                               Function funObj) throws CarbonException, IOException {

        // sanity check
        SystemHostObject system = checkInstance(thisObj);

        AxisService axisService;
        // retrieves the AxisService object from the Rhino context
        Object axisServiceObject = cx.getThreadLocal(MashupConstants.AXIS2_SERVICE);

        if (axisServiceObject != null && axisServiceObject instanceof AxisService) {
            axisService = (AxisService) axisServiceObject;
        } else {
            throw new CarbonException("Error obtaining the Service Meta Data: Axis2 Service");
        }
        ConfigurationContext configurationContext;
        // retrieves the ConfigurationContext object from the Rhino Engine
        Object configurationContextObject =
                cx.getThreadLocal(MashupConstants.AXIS2_CONFIGURATION_CONTEXT);
        if (configurationContextObject != null &&
            configurationContextObject instanceof ConfigurationContext) {
            configurationContext = (ConfigurationContext) configurationContextObject;
        } else {
            throw new CarbonException(
                    "Error obtaining the Service Meta Data : Axis2 ConfigurationContext");
        }

        //Generating UUID + current time for the taskName
        String taskName =
                system.getFormattedCurrentDateTime() + "-" + UUIDGenerator.getUUID().substring(9);

        int argCount = arguments.length;
        Object jsFunction = null;
        Object[] functionParams = null;
        long timeout = 0;
        Date currentTime = new Date();
        
        final Map<String, String> resources = new HashMap<String, String>();
        resources.put(MSTaskConstants.AXIS_SERVICE, axisService.getName());
        MSTaskInfo msTaskInfo = new MSTaskInfo();

        switch (argCount) {

            case 2://A javascript function and its timeout were passed

                //Extracting the javascript function from the arguments
                if ((arguments[0] instanceof Function) || ((arguments[0] instanceof String))) {
                    jsFunction = arguments[0];
                } else {
                    throw new CarbonException("Invalid parameter. The first parameter must be " +
                                              "a JavaScript function.");
                }

                //Extracting the frequency from the arguments
                if (arguments[1] != null && arguments[1] instanceof Number) {
                    timeout = ((Number) arguments[1]).longValue();
                } else {
                    throw new CarbonException("Invalid parameter. The second parameter " +
                                              "must be function starting timeout.");
                }

                //Storing the function meta-data to be used by the job at execution time
                resources.put(MSTaskConstants.FUNCTION_PARAMETERS, MSTaskUtils.toString(functionParams));
                resources.put(MSTaskConstants.TASK_NAME, taskName);
                axisService.addParameter(taskName, jsFunction);
                
                //Creating the trigger. There will be a one-to-one mapping between jobs and triggers in this implementation
                msTaskInfo.setName(taskName);
                msTaskInfo.setTaskCount(0);
                msTaskInfo.setTaskInterval(0);
                msTaskInfo.setStartTime(MSTaskUtils.dateToCal(new Date(currentTime.getTime() + timeout)));
                
                break;

            case 3://A javascript function its execution frequency and parameters were passed

                //Extracting the javascript function from the arguments=
                if ((arguments[0] instanceof Function) || ((arguments[0] instanceof String))) {
                    jsFunction = arguments[0];
                } else {
                    throw new CarbonException("Invalid parameter. The first parameter must " +
                                              "be a JavaScript function.");
                }

                //Extracting the frequency from the arguments
                if (arguments[1] != null && arguments[1] instanceof Number) {
                    timeout = ((Number) arguments[1]).longValue();
                } else {
                    throw new CarbonException(
                            "Invalid parameter. The second parameter must be the " +
                            "execution frequency in milliseconds.");
                }

                //Extracting function parameters from the arguments
                if (arguments[2] != null) {

                    if (arguments[2] instanceof String) {
                        taskName = (String) arguments[2];
                    } else {
                        throw new CarbonException(
                                "Invalid parameter. The third parameter must be a string " +
                                "value for the  the task name");
                    }
                }

                //Storing the function meta-data to be used by the job at execution time
                resources.put(MSTaskConstants.FUNCTION_PARAMETERS, MSTaskUtils.toString(functionParams));
                resources.put(MSTaskConstants.TASK_NAME, taskName);
                axisService.addParameter(taskName, jsFunction);
                
                //Creating the trigger. There will be a one-to-one mapping between jobs and triggers in this implementation
                msTaskInfo.setName(taskName);
                msTaskInfo.setTaskCount(0);
                msTaskInfo.setTaskInterval(0);
                msTaskInfo.setStartTime(MSTaskUtils.dateToCal(new Date(currentTime.getTime() + timeout)));

                break;

            default:
                throw new CarbonException("Invalid number of parameters.");
        }

        msTaskInfo.setTaskProperties(resources);
        MSTaskAdmin taskAdmin = new MSTaskAdmin();

        axisService.addParameter(MSTaskConstants.CONTEXT_FACTORY, cx.getFactory());
        axisService.addParameter(MSTaskConstants.TASK_SCOPE, thisObj);

        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put(MSTaskConstants.JAVASCRIPT_FUNCTION, jsFunction);
        paramMap.put(MSTaskConstants.FUNCTION_PARAMETERS, functionParams);
        paramMap.put(MSTaskConstants.AXIS_SERVICE, axisService);
        paramMap.put(MSTaskConstants.TASK_NAME, taskName);
        paramMap.put(MashupConstants.AXIS2_CONFIGURATION_CONTEXT, configurationContext);
        try {
            if (axisService.getParameterValue(
                    MSTaskConstants.JS_FUNCTION_MAP) != null) {
                // JobDataMap is added to AxisConfiguration
                HashMap tasksMap = (HashMap)axisService.getParameterValue(MSTaskConstants.JS_FUNCTION_MAP);
                tasksMap.put(taskName, paramMap);

            } else {
                // no function map in AxisConfiguration, new one is created
                HashMap tasksMap = new HashMap();
                tasksMap.put(taskName, paramMap);
                Parameter parameter = new Parameter(MSTaskConstants.JS_FUNCTION_MAP, tasksMap);
                axisService.addParameter(parameter);
            }
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
        
        try {
			taskAdmin.scheduleTask(msTaskInfo);
		} catch (AxisFault e) {
			throw new CarbonException("Unable to create the scheduling task");
		}

        return taskName;

    }


    /**
     * Sometimes it's useful to be able to cancel a timer before it goes off. The clearTimeout() method lets us do exactly that.
     * <p/>
     * <p/>
     * <pre>
     *    ex: clearTimeout ( timeoutId );
     * </pre>
     * <p/>
     * <p/>
     * where timeoutId is the ID of the timeout as returned from the setTimeout() method call.
     *
     * @throws CarbonException Thrown in case any exceptions occur
     */
    public static void jsFunction_clearTimeout(Context cx, Scriptable thisObj, Object[] arguments,
                                               Function funObj) throws CarbonException {

        ConfigurationContext configurationContext;
        // retrieves the ConfigurationContext object from the Rhino Engine
        Object configurationContextObject =
                cx.getThreadLocal(MashupConstants.AXIS2_CONFIGURATION_CONTEXT);
        if (configurationContextObject != null &&
            configurationContextObject instanceof ConfigurationContext) {
            configurationContext = (ConfigurationContext) configurationContextObject;
        } else {
            throw new CarbonException(
                    "Error obtaining the Service Meta Data : Axis2 ConfigurationContext");
        }

        if (arguments[0] instanceof String) {
            deleteJob(arguments, configurationContext);
        } else {
            throw new CarbonException("Invalid parameter");
        }
    }


    /**
     * Allows printing to the system log from a Mashup.
     * <p/>
     * <p>
     * <pre>
     *    ex: system.log(logmessage, loglevel);
     * </pre>
     * </p>
     * <p/>
     * Where logmessage contains a string to be written to the system log and (optional) loglevel indicates the logging level as
     * 'info', 'warn', 'debug', 'error' or 'fatal'. The logging level defaults to 'info' when one is not provided.
     *
     * @throws CarbonException Thrown in case any exceptions occur
     */
    public static void jsFunction_log(Context cx, Scriptable thisObj, Object[] arguments,
                                      Function funObj) throws CarbonException {
        String logMessage;
        String logLevel;

        Object argument = arguments[0];
        if (argument != null && !(argument instanceof Undefined || argument instanceof UniqueTag)) {
            logMessage = argument.toString();
        } else {
            throw new CarbonException("The first argument should contain a message to log");
        }

        if (arguments.length > 1) {
            if (arguments[1] instanceof String) {
                logLevel = (String) arguments[1];

                if (logLevel.equalsIgnoreCase("info")) {
                    log.info(logMessage);
                } else if (logLevel.equalsIgnoreCase("warn")) {
                    log.warn(logMessage);
                } else if (logLevel.equalsIgnoreCase("debug")) {
                    log.debug(logMessage);
                } else if (logLevel.equalsIgnoreCase("error")) {
                    log.error(logMessage);
                } else if (logLevel.equalsIgnoreCase("fatal")) {
                    log.fatal(logMessage);
                } else {
                    throw new CarbonException(
                            "Unsupported log level. Please refer documentation for this function.");
                }

            } else {
                throw new CarbonException(
                        "The second argument should contain a String indicating the log level");
            }
        } else {
            log.info(logMessage);
        }
    }

    /**
     * <p>
     * Utility function to get a JSON file over the network
     * </p>
     * <pre>
     * var history = system.getJSON('http://wso2.org/repos/wso2/trunk/mashup/java/modules/samples/upgradeChecker/upgradeChecker.resources/history.json');
     * <p/>
     * You can also do basic authentication by giving a username/password
     * <p/>
     * var history = system.getJSON('http://wso2.org/repos/wso2/trunk/mashup/java/modules/samples/upgradeChecker/upgradeChecker.resources/history.json', 'username', 'password');
     * </pre>
     *
     * @throws CarbonException Thrown in case any exceptions occur
     */
    public static Scriptable jsFunction_getJSON(Context cx, Scriptable thisObj, Object[] arguments,
                                                 Function funObj) throws CarbonException {
        if (arguments[0] == null || !(arguments[0] instanceof String)) {
            throw new CarbonException(
                    "The getJSON function should be called with either a single parameter which is " +
                    "the url to fetch JSON from or three parameters, which are the url to fetch JSON " +
                    "from, the User Name and a Password for basic authentication.");
        }

        String urlString;
        String username = null;
        String password = null;

        if ((arguments.length > 1) && (arguments.length < 4)) {
            urlString = (String) arguments[0];

            // We have a username password combo as well
            if ((arguments[1] == null) || (!(arguments[1] instanceof String))) {
                throw new CarbonException(
                        "The second argument for getJSON function should be a string containing the username ");
            } else {
                username = (String) arguments[1];
            }
            if ((arguments[2] == null) || (!(arguments[2] instanceof String))) {
                throw new CarbonException(
                        "The third argument for getJSON function should be a string containing the password ");
            } else {
                password = (String) arguments[2];
            }
        } else if (arguments.length == 1) {
            // We have only a URL
            urlString = (String) arguments[0];
        } else {
            throw new CarbonException(
                    "The getJSON function should be called with either a single parameter which is " +
                    "the url to fetch JSON from or three parameters, which are the url to fetch JSON " +
                    "from, the User Name and a Password for basic authentication.");
        }
        HttpMethod method = new GetMethod(urlString);
        BufferedReader bufferedReader = null;
        try {
            URL url = new URL(urlString);
            int statusCode = MashupUtils.executeHTTPMethod(method, url, username, password);
            if (statusCode != HttpStatus.SC_OK) {
                throw new CarbonException(
                        "An error occured while getting the resource at " + url + ". Reason :" +
                        method.getStatusLine());
            }
            bufferedReader = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line + "\n");
            }
            String json = "var x = " + sb.toString() + ";";
            cx.evaluateString(thisObj, json, "Get JSON", 0, null);
            return (Scriptable) thisObj.get("x", thisObj);
        } catch (MalformedURLException e) {
            String msg = "Malformed URL supplied for the system.getJSON()";
            log.error(msg, e);
            throw new CarbonException(msg, e);
        } catch (IOException e) {
            String msg = "Error while reading content from the URL in system.getJSON()";
            log.error(msg, e);
            throw new CarbonException(msg, e);
        } finally {
            method.releaseConnection();
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    log.warn("Error closing BufferedReader", e);
                }
            }
        }
    }

    private String getFormattedCurrentDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS-z");
        Date date = new Date();
        return dateFormat.format(date);
    }
}