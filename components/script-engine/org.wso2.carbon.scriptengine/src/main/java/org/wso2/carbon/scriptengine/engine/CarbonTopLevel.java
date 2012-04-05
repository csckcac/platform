package org.wso2.carbon.scriptengine.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.*;
import org.wso2.carbon.scriptengine.util.HostObjectUtil;
import org.wso2.carbon.scriptengine.exceptions.ScriptException;
import org.wso2.javascript.xmlimpl.XML;

import java.io.StringReader;
import java.lang.reflect.Method;


public class CarbonTopLevel extends ImporterTopLevel {

    private static final Log log = LogFactory.getLog(CarbonTopLevel.class);

    private static final String hostObjectName = "CarbonTopLevel";

    public CarbonTopLevel(Context context, boolean sealed) {
        super(context, sealed);
        //defineGlobalFunctions(context);
    }

    public static void log(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "log";
        int argsCount = args.length;
        if (argsCount < 1 || argsCount > 2) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        String msg = HostObjectUtil.serializeObject(args[0]);
        String level = "info";
        if (argsCount == 2) {
            if (!(args[1] instanceof String)) {
                HostObjectUtil.invalidArgsError(hostObjectName, functionName, "2", "string", args[1], false);
            }
            level = ((String) args[1]).toLowerCase();
        }

        if (level.equalsIgnoreCase("info")) {
            log.info(msg);
        } else if (level.equalsIgnoreCase("warn")) {
            log.warn(msg);
        } else if (level.equalsIgnoreCase("debug")) {
            log.debug(msg);
        } else if (level.equalsIgnoreCase("error")) {
            log.error(msg);
        } else if (level.equalsIgnoreCase("fatal")) {
            log.fatal(msg);
        } else {
            throw new ScriptException("Unknown log level : " + level);
        }
    }

    public static Object parse(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "parse";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, hostObjectName, "1", "string", args[0], true);
        }
        String source = "var x = " + args[0] + ";";
        cx.evaluateString(funObj, source, "wso2js", 1, null);
        return funObj.get("x", funObj);
    }

    public static String stringify(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "stringify";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        return HostObjectUtil.serializeJSON(args[0]);
    }

    private void defineGlobalFunctions(Context cx) {
        try {
            Method method = CarbonTopLevel.class.getDeclaredMethod("log", Context.class,
                    Scriptable.class, Object[].class, Function.class);
            RhinoEngine.defineFunction(this, "log", method, ScriptableObject.PERMANENT);
            method = CarbonTopLevel.class.getDeclaredMethod("stringify", Context.class,
                    Scriptable.class, Object[].class, Function.class);
            RhinoEngine.defineFunction(this, "stringify", method, ScriptableObject.PERMANENT);
            method = CarbonTopLevel.class.getDeclaredMethod("parse", Context.class,
                    Scriptable.class, Object[].class, Function.class);
            RhinoEngine.defineFunction(this, "parse", method, ScriptableObject.PERMANENT);
        } catch (NoSuchMethodException e) {
            log.error(e);
        }
    }
}
