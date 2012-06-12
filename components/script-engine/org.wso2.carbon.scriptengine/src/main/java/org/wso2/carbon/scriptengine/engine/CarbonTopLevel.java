package org.wso2.carbon.scriptengine.engine;

import com.google.gson.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.*;
import org.wso2.carbon.scriptengine.exceptions.ScriptException;
import org.wso2.carbon.scriptengine.util.HostObjectUtil;

import java.lang.reflect.Method;


public class CarbonTopLevel extends ImporterTopLevel {

    private static final Log log = LogFactory.getLog(CarbonTopLevel.class);

    private static final String hostObjectName = "CarbonTopLevel";

    public CarbonTopLevel(Context context, boolean sealed) {
        super(context, sealed);
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

        Gson gson = new Gson();
        JsonElement element = gson.fromJson((String) args[0], JsonElement.class);

        String source = "var x = " + element.toString() + ";";
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
}
