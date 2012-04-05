package org.wso2.carbon.hostobjects.stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.carbon.scriptengine.exceptions.ScriptException;
import org.wso2.carbon.scriptengine.util.HostObjectUtil;

import java.io.InputStream;

public class StreamHostObject extends ScriptableObject {

    private static final Log log = LogFactory.getLog(StreamHostObject.class);

    private static final String hostObjectName = "Stream";

    private InputStream stream = null;

    public StreamHostObject() {

    }

    @Override
    public String getClassName() {
        return hostObjectName;
    }

    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj, boolean inNewExpr)
            throws ScriptException {
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(hostObjectName, hostObjectName, argsCount, true);
        }
        if (!(args[0] instanceof InputStream)) {
            HostObjectUtil.getReservedHostObjectWarn(hostObjectName);
        }
        StreamHostObject sho = new StreamHostObject();
        sho.stream = (InputStream) args[0];
        return sho;
    }

    public InputStream getStream() {
        return stream;
    }
}
