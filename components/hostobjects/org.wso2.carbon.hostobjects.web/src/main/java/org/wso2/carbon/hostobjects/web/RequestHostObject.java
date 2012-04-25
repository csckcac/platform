package org.wso2.carbon.hostobjects.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.carbon.scriptengine.exceptions.ScriptException;
import org.wso2.carbon.scriptengine.util.HostObjectUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class RequestHostObject extends ScriptableObject {

    private static final Log log = LogFactory.getLog(RequestHostObject.class);

    private static final String hostObjectName = "Request";

    private HttpServletRequest request;

    private Object content = null;

    private Context cx;

    public RequestHostObject() {

    }

    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj, boolean inNewExpr)
            throws ScriptException {
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, hostObjectName, argsCount, true);
        }
        if (!(args[0] instanceof HttpServletRequest)) {
            HostObjectUtil.getReservedHostObjectWarn(hostObjectName);
        }
        RequestHostObject rho = new RequestHostObject();
        rho.request = (HttpServletRequest) args[0];
        rho.cx = cx;
        return rho;
    }

    @Override
    public String getClassName() {
        return hostObjectName;
    }

    public static Object jsFunction_getContent(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "getContent";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        try {
            if (rho.content != null) {
                return rho.content;
            }
            String data = HostObjectUtil.streamToString(rho.request.getInputStream());
            String contentType = rho.request.getContentType();
            if (contentType != null && (
                    contentType.equals("application/json") ||
                            contentType.equals("application/json/badgerfish"))) {
                rho.content = cx.evaluateString(thisObj, data, "wso2js", 1, null);
            } else {
                rho.content = data;
            }
            return rho.content;
        } catch (IOException e) {
            String msg = "Error occurred while reading Servlet InputStream";
            log.warn(msg, e);
            throw new ScriptException(msg, e);
        }
    }

    public static String jsFunction_getMethod(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "getMethod";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        return rho.request.getMethod();
    }

    public static String jsFunction_getProtocol(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "getProtocol";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        return rho.request.getProtocol();
    }

    public static String jsFunction_getQueryString(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "getQueryString";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        return rho.request.getQueryString();
    }

    public static String jsFunction_getContentType(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "getContentType";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        return rho.request.getContentType();
    }

    public static int jsFunction_getContentLength(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "getContentLength";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        return rho.request.getContentLength();
    }

    public static String jsFunction_getRequestURI(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "getRequestURI";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        return rho.request.getRequestURI();
    }

    public static String jsFunction_getHeader(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getHeader";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(
                    hostObjectName, functionName, "1", "string", args[0], false);
        }
        RequestHostObject rho = (RequestHostObject) thisObj;
        return rho.request.getHeader((String) args[0]);
    }
    
    public static String jsFunction_getRemoteAddr(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "getRemoteAddr";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        return rho.request.getRemoteAddr();
    }

    public static String jsFunction_getPathInfo(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "getPathInfo";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        return rho.request.getPathInfo();
    }
    
    public static int jsFunction_getLocalPort(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException {
        String functionName = "getPathInfo";
        int argsCount = args.length;
        if (argsCount != 0) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }

        RequestHostObject rho = (RequestHostObject) thisObj;
        return rho.request.getLocalPort();
    }
    public static String jsFunction_getParameter(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "getParameter";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(hostObjectName, functionName, "1", "string", args[0], false);
        }
        RequestHostObject rho = (RequestHostObject) thisObj;
        return rho.request.getParameter((String) args[0]);
    }

    public HttpServletRequest getHttpServletRequest() {
        return this.request;
    }


}
