package org.wso2.carbon.hostobjects.log;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.carbon.jaggery.core.manager.WebAppContext;
import org.wso2.carbon.scriptengine.exceptions.ScriptException;
import org.wso2.carbon.scriptengine.util.HostObjectUtil;

public class LogHostObject extends ScriptableObject {

    public static final String HOSTOBJECT_NAME = "Log";
    private static final String ROOT_LOGGER = "JAGGERY";

    static Logger logger = Logger.getLogger(LogHostObject.class.getName());

    @Override
    public String getClassName() {
        return HOSTOBJECT_NAME;
    }

    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj,
                                           boolean inNewExpr) throws ScriptException {
        String requestString = ((WebAppContext) cx.getThreadLocal("jaggeryContext")).getServletRequest().getRequestURI();
        String loggerName = ROOT_LOGGER + requestString.replace("/", ".").replace(".jag", "");

        LogHostObject logObj = new LogHostObject();
        logObj.logger = Logger.getLogger(loggerName);
        logObj.logger.setLevel(Level.DEBUG);
        return logObj;
    }

    public static void jsFunction_debug(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "debug";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(HOSTOBJECT_NAME, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(HOSTOBJECT_NAME, functionName, "1", "string", args[0], false);
        }
        LogHostObject logObj = (LogHostObject) thisObj;
        logObj.logger.debug((String) args[0]);
    }

    public static void jsFunction_info(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "info";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(HOSTOBJECT_NAME, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(HOSTOBJECT_NAME, functionName, "1", "string", args[0], false);
        }
        LogHostObject logObj = (LogHostObject) thisObj;
        logObj.logger.info((String) args[0]);
    }

    public static void jsFunction_error(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "error";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(HOSTOBJECT_NAME, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(HOSTOBJECT_NAME, functionName, "1", "string", args[0], false);
        }
        LogHostObject logObj = (LogHostObject) thisObj;
        logObj.logger.error((String) args[0]);
    }

    public static void jsFunction_warn(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "warn";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(HOSTOBJECT_NAME, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(HOSTOBJECT_NAME, functionName, "1", "string", args[0], false);
        }
        LogHostObject logObj = (LogHostObject) thisObj;
        logObj.logger.warn((String) args[0]);
    }

    public static void jsFunction_fatal(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "fatal";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(HOSTOBJECT_NAME, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(HOSTOBJECT_NAME, functionName, "1", "string", args[0], false);
        }
        LogHostObject logObj = (LogHostObject) thisObj;
        logObj.logger.fatal((String) args[0]);
    }

    public static boolean jsFunction_isDebugEnabled(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        LogHostObject logObj = (LogHostObject) thisObj;
        return logObj.logger.isDebugEnabled();
    }

    public static boolean jsFunction_isTraceEnabled(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        LogHostObject logObj = (LogHostObject) thisObj;
        return logObj.logger.isTraceEnabled();
    }

    public static void jsSet_level(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "level";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(HOSTOBJECT_NAME, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(HOSTOBJECT_NAME, functionName, "1", "string", args[0], false);
        }

        String level = (String) args[0];
        LogHostObject logObj = (LogHostObject) thisObj;
        if (level.equalsIgnoreCase("error")) {
            logObj.logger.setLevel(Level.ERROR);
        } else if (level.equalsIgnoreCase("debug")) {
            logObj.logger.setLevel(Level.DEBUG);
        } else if (level.equalsIgnoreCase("info")) {
            logObj.logger.setLevel(Level.INFO);
        } else if (level.equalsIgnoreCase("fatal")) {
            logObj.logger.setLevel(Level.FATAL);
        } else if (level.equalsIgnoreCase("trace")) {
            logObj.logger.setLevel(Level.TRACE);
        }
    }
}
