/*
 * Copyright 2005,2006 WSO2, Inc. http://www.wso2.org
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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.impl.llom.OMSourcedElementImpl;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.json.JSONBadgerfishDataSource;
import org.apache.axis2.json.JSONDataSource;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.WrappedException;
import org.mozilla.javascript.UniqueTag;
import org.mozilla.javascript.Undefined;
import org.wso2.carbon.mashup.utils.MashupConstants;
import org.wso2.carbon.mashup.utils.MashupReader;
import org.wso2.carbon.mashup.utils.MashupUtils;
import org.wso2.carbon.scriptengine.cache.ScriptCachingContext;
import org.wso2.carbon.scriptengine.engine.RhinoEngine;
import org.wso2.carbon.scriptengine.exceptions.ScriptException;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Class JavaScriptEngine implements a simple Javascript evaluator using Rhino.
 * Two of the shell functionalities, print() and load(), are implemented as well.
 * Extending the ImporterTopLevel class gives access to the top level methods,
 * importClass() and importPackage().
 */
public class JavaScriptEngine {
    //TODO Do we really need this?? (thilina)
    public static String axis2RepositoryLocation;

    private boolean json = false;

    private String scriptName;

    /**
     * Constructs a new instance of the JavaScriptEngine class
     *
     * @param scriptName - Used to display error and warning messages
     */
    public JavaScriptEngine(String scriptName) {
        this.scriptName = scriptName;
        //defineFunctionProperties(names, JavaScriptEngine.class, ScriptableObject.DONTENUM);
    }

    /**
     * Evaluates a Reader instance associated to a Javascript source.
     *
     * @param service a Reader instance associated to a Javascript source
     * @throws ScriptException if the Reader instance generates an IOException
     */
    public void evaluate(AxisService service) throws ScriptException {
        ScriptableObject scope = JavaScriptEngineUtils.getActiveScope();
        ConfigurationContext configurationContext = (ConfigurationContext) RhinoEngine.getContextProperty(
                MashupConstants.AXIS2_CONFIGURATION_CONTEXT);
        RhinoEngine engine = JavaScriptEngineUtils.getEngine();
        engine.exec(new MashupReader(service), scope, MashupUtils.getScriptCachingContext(configurationContext, service));
    }

    /**
     * /**
     * Loads and executes a set of Javascript source files. The source files
     * are searched relative to the service archive. If not found then the
     * search will assume absolute path is given. If fails again then it will
     * search under classes folder in Axis2 repository.
     * <p/>
     * <strong>We load this method to the JS Engine to be used internally by the java scripts.</strong>
     *
     * @param cx      context to load the scriptable object
     * @param thisObj
     * @param args
     * @param funObj
     * @throws FileNotFoundException if the specified source cannot be found
     * @throws IOException           if evaluating the source produces an IOException
     */
    /*public static void load(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws IOException {

        JavaScriptEngine engine = (JavaScriptEngine) getTopLevelScope(thisObj);

        for (Object arg : args) {
            String path = Context.toString(arg);
            File f = new File(path);
            // Assumes resource's path is given as absolute
            if (!f.exists() && axis2RepositoryLocation != null) {
                // Assumes resource's path is given relative to the classes folder in Axis2 repository
                f = new File(axis2RepositoryLocation + File.separator + "classes"
                        + File.separator + Context.toString(arg));
            }
            FileReader fReader = new FileReader(f);
            engine.evaluate(fReader);
        }
    }*/

    /**
     * Evaluates the requested operation in the Javascript service implementation.
     *
     * @param method Javascript operation name
     * @param service a Reader instance associated with the Javascript service
     * @param args   an Object representing the input to the operation
     * @return an OMNode containing the result from executing the operation
     * @throws AxisFault- Thrown in case an exception occurs
     */
    private Object call(String method, AxisService service, Object args) throws AxisFault {
        Object functionArgs[];
        RhinoEngine engine = JavaScriptEngineUtils.getEngine();
        ScriptableObject scope = JavaScriptEngineUtils.getActiveScope();
        Context cx = RhinoEngine.enterContext();
        try {
            // Handle JSON messages
            if (args instanceof OMSourcedElementImpl) {
                OMDataSource datasource = ((OMSourcedElementImpl) args).getDataSource();
                if (datasource instanceof JSONDataSource) {
                    args = ((JSONDataSource) datasource).getCompleteJOSNString();
                } else if (datasource instanceof JSONBadgerfishDataSource) {
                    args = ((JSONBadgerfishDataSource) datasource).getCompleteJOSNString();
                } else {
                    throw new AxisFault("Unsupported Data Format");
                }
                //as getJSONString() method of the datasource has protected access, we can't get the json
                //content without operation name, so we remove it using a regex
                args = ((String) args).replaceAll("^[\\{][\\t\\s\\r\\n]*[\"](" + method +
                        ")[\"][\\t\\s\\r\\n]*[:][\\t\\s\\r\\n]*|[\\}]$", "");
                Gson gson = new Gson();
                JsonElement element = gson.fromJson((String) args, JsonElement.class);
                args = "var x = " + element.toString() + ";";
                ScriptableObject tmp = engine.getRuntimeScope();
                cx.evaluateString(tmp, (String) args, "Get JSON", 0, null);
                args = tmp.get("x", tmp);
                functionArgs = new Object[]{args};
                json = true;
            } else if (args instanceof Object[]) {
                functionArgs = (Object[]) args;
            } else if (args != null) {
                Object[] objects = {args};
                args = RhinoEngine.newObject("XML", scope, objects);
                functionArgs = new Object[]{args};
            } else {
                functionArgs = new Object[0];
            }

            ConfigurationContext configurationContext = (ConfigurationContext) RhinoEngine.getContextProperty(
                    MashupConstants.AXIS2_CONFIGURATION_CONTEXT);
            ScriptCachingContext sctx = MashupUtils.getScriptCachingContext(configurationContext, service);

            return engine.call(new MashupReader(service), method, functionArgs, scope, scope, sctx);

        } catch (WrappedException exception) {
            throw AxisFault.makeFault(exception.getCause());
        } catch (JavaScriptException exception) {
            throw new AxisFault(exception.getValue().toString(), exception);
        } catch (Throwable throwable) {
            throw AxisFault.makeFault(throwable);
        } finally {
            RhinoEngine.exitContext();
        }
    }

    public Object evaluateFunction(String func, Object[] args) throws ScriptException {
        func = "var x = " + func + ";";
        ScriptableObject scope = JavaScriptEngineUtils.getEngine().getRuntimeScope();
        Context cx = RhinoEngine.enterContext();
        cx.evaluateString(scope, func, "Eval Func", 0, null);
        Function function = (Function) scope.get("x", scope);
        Object result = function.call(cx, scope, scope, args);
        RhinoEngine.exitContext();
        return result;
    }

    /**
     * Evaluates the requested operation in the Javascript service
     * implementation. Any Javascript source defined under loadJSScripts
     * parameter is evaluated before evaluating the operation.
     *
     * @param method  Javascript operation name
     * @param service  a Reader instance associated with the Javascript service
     * @param args    an Object representing the input to the operation
     * @param scripts a string represnting a set of Javascript files to be evaluated
     *                before evaluating the service
     * @return an OMNode containing the result from executing the operation
     * @throws AxisFault - Thrown in case an exception occurs
     */
    public Object call(String method, AxisService service, Object args, String scripts) throws AxisFault {
        return call(method, service, args);
    }

    public boolean isJson() {
        return json;
    }

    public static boolean isNull(Object object) {
        return object == null || object instanceof UniqueTag || object instanceof Undefined;
    }
}
