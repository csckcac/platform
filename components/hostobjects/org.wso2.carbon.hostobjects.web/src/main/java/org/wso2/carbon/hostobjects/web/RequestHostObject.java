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

    public Object jsGet_content() throws ScriptException {
        try {
            if (content != null) {
                return content;
            }
            String data = HostObjectUtil.streamToString(request.getInputStream());
            String contentType = request.getContentType();
            if (contentType != null && (
                    contentType.equals("application/json") ||
                            contentType.equals("application/json/badgerfish"))) {
                content = cx.evaluateString(this, data, "wso2js", 1, null);
            } else {
                content = data;
            }
            return content;
        } catch (IOException e) {
            String msg = "Error occurred while reading Servlet InputStream";
            log.warn(msg, e);
            throw new ScriptException(msg, e);
        }
    }

    public String jsGet_method() throws ScriptException {
        return request.getMethod();
    }

    public String jsGet_protocol() throws ScriptException {
        return request.getProtocol();
    }

    public String jsGet_queryString() throws ScriptException {
        return request.getQueryString();
    }

    public String jsGet_contentType() throws ScriptException {
        return request.getContentType();
    }

    public int jsGet_contentLength() throws ScriptException {
        return request.getContentLength();
    }

    public String jsGet_uri() throws ScriptException {
        return request.getRequestURI();
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
