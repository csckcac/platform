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

    private static Logger logger = Logger.getLogger(LogHostObject.class.getName());

    @Override
    public String getClassName() {
        return HOSTOBJECT_NAME;
    }

    /**
     * Creates a new log object for the requested resource.
     * logger name will be the resource name separated by a . (dot)
     * i.e if resource is /foo/bar/mar.jag
     * the loger name will be
     * JAGGERY.foo.bar.mar
     *
     * by default the log level is set to debug
     *
     * @param cx
     * @param args
     * @param ctorObj
     * @param inNewExpr
     * @return
     * @throws ScriptException
     */
    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj,
                                           boolean inNewExpr) throws ScriptException {
        String requestString = ((WebAppContext) cx.getThreadLocal("jaggeryContext")).getServletRequest().getRequestURI();
        String loggerName = ROOT_LOGGER + requestString.replace("/", ".").replace(".jag", "");

        LogHostObject logObj = new LogHostObject();
        logObj.logger = Logger.getLogger(loggerName);

        //TODO need to remove this once the set from config is implemented
        logObj.logger.setLevel(Level.DEBUG);
        return logObj;
    }

    //prints a debug message
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
        logObj.logger.debug(args[0]);
    }

    //prints an info message
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
        logObj.logger.info(args[0]);
    }

    //prints an error message
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
        logObj.logger.error(args[0]);
    }

    //prints a warning message
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
        logObj.logger.warn(args[0]);
    }

    //prints a fatal message
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
        logObj.logger.fatal(args[0]);
    }

    //check if debug is enabled
    public boolean jsGet_isDebugEnabled() throws ScriptException {
        return this.logger.isDebugEnabled();
    }

    //check if trace is anabled
    public boolean jsGet_isTraceEnabled() throws ScriptException {
        return this.logger.isTraceEnabled();
    }
}
