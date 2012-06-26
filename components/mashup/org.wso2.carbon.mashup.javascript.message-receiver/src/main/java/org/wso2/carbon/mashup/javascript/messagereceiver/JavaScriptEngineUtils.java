/*
 * Copyright 2007 WSO2, Inc. http://www.wso2.org
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

package org.wso2.carbon.mashup.javascript.messagereceiver;

import org.apache.axis2.AxisFault;
import org.apache.axis2.util.Loader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Context;
import org.wso2.carbon.mashup.javascript.hostobjects.hostobjectservice.service.HostObjectService;
import org.wso2.carbon.mashup.utils.MashupConstants;
import org.jaggeryjs.scriptengine.engine.JavaScriptHostObject;
import org.jaggeryjs.scriptengine.engine.JavaScriptMethod;
import org.jaggeryjs.scriptengine.engine.JavaScriptProperty;
import org.jaggeryjs.scriptengine.engine.RhinoEngine;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JavaScriptEngineUtils {

    /*
    * setup for logging
    */
    private static final Log log = LogFactory.getLog(JavaScriptEngineUtils.class);
    private static HostObjectService hostObjectService = null;
    private static RhinoEngine engine = null;

    public static void setEngine(RhinoEngine engine) throws AxisFault {
        JavaScriptEngineUtils.engine = engine;
        loadHostObjects();
        JavaScriptMethod method = new JavaScriptMethod("print");
        method.setClazz(JavaScriptEngineUtils.class);
        method.setMethodName("print");
        method.setAttribute(ScriptableObject.DONTENUM);
        engine.defineMethod(method);
    }

    /**
     * Prints the value of each element in the args array.
     * This is a similar implementation to the Rhino's print()
     * functionality in the shell.
     * <strong>We load this method to the JS Engine to be used internally by the java scripts.</strong>
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     */
    public static void print(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                System.out.print(" ");
            }
            String s = Context.toString(args[i]);
            System.out.print(s);
        }
        System.out.println();
    }

    public static RhinoEngine getEngine() {
        return JavaScriptEngineUtils.engine;
    }

    public static void initialize() {
        ScriptableObject scope = getActiveScope();
        Map<String, String> globalObjects = hostObjectService.getGlobalObjects();
        Set<Map.Entry<String, String>> entries = globalObjects.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            String hostObject = entry.getKey();
            String objectName = entry.getValue();
            if ((objectName != null) && (!"".equals(objectName)) && (hostObject != null) && (!"".equals(hostObject))) {
                Scriptable entryHostObject = RhinoEngine.newObject(hostObject, scope, new Object[0]);
                JavaScriptProperty property = new JavaScriptProperty(objectName);
                property.setValue(entryHostObject);
                property.setAttribute(ScriptableObject.READONLY);
                RhinoEngine.defineProperty(scope, property);
            }
        }

    }

    public static void setHostObjectService(HostObjectService hostObjectService) {
        JavaScriptEngineUtils.hostObjectService = hostObjectService;
    }

    public static ScriptableObject getActiveScope() {
        return (ScriptableObject) RhinoEngine.getContextProperty(MashupConstants.ACTIVE_SCOPE);
    }

    public static void setActiveScope(ScriptableObject scope) {
        Context cx = RhinoEngine.enterContext();
        cx.evaluateString(scope, "new XML();", "XML() initialization", 0, null);
        RhinoEngine.putContextProperty(MashupConstants.ACTIVE_SCOPE, scope);
        RhinoEngine.exitContext();
    }

    public static void loadHostObjects() throws AxisFault {

        if (hostObjectService != null) {
                List<String> classes = hostObjectService.getHostObjectClasses();
                for (String classStr : classes) {
                    try {
                        Class clazz = Class.forName(classStr);
                        Method method = clazz.getMethod("getClassName");
                        String name = (String) method.invoke(clazz.newInstance());
                        JavaScriptHostObject hostObject = new JavaScriptHostObject(name);
                        hostObject.setClazz(clazz);
                        engine.defineHostObject(hostObject);
                    } catch (IllegalAccessException e) {
                        log.fatal(e);
                        throw new AxisFault("Error occured while loading the host object :" + classStr, e);
                    } catch (InstantiationException e) {
                        log.fatal(e);
                        throw new AxisFault("Error occured while loading the host object :" + classStr, e);
                    } catch (InvocationTargetException e) {
                        log.fatal(e);
                        throw new AxisFault("Error occured while loading the host object :" + classStr, e);
                    } catch (ClassNotFoundException e) {
                        log.fatal(e);
                        throw new AxisFault("Error occured while loading the host object :" + classStr, e);
                    } catch (NoSuchMethodException e) {
                        log.fatal(e);
                        throw new AxisFault("Error occured while loading the host object :" + classStr, e);
                    }
                }
        }
    }

    private static Class loadClass(final String className) throws PrivilegedActionException {
        return (Class) org.apache.axis2.java.security.AccessController
                .doPrivileged(new PrivilegedExceptionAction() {
                    public Object run() throws AxisFault {
                        Class selectorClass;
                        try {
                            if ((className != null) && !"".equals(className)) {
                                selectorClass = Loader.loadClass(Thread.currentThread()
                                        .getContextClassLoader(), className);
                            } else {
                                log.fatal("Invalid Class Name for the HostObject");
                                throw new AxisFault("Invalid Class Name");
                            }
                        } catch (ClassNotFoundException e) {
                            log.fatal(e);
                            throw new AxisFault("Error occured while loading the host object :"
                                    + className, e);
                        }
                        return selectorClass;
                    }
                });
    }


}
