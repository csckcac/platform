package org.wso2.carbon.jaggery.core.manager;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.carbon.hostobjects.stream.StreamHostObject;
import org.wso2.carbon.jaggery.core.ScriptReader;
import org.wso2.carbon.jaggery.core.modules.JaggeryHostObject;
import org.wso2.carbon.jaggery.core.modules.JaggeryMethod;
import org.wso2.carbon.jaggery.core.modules.JaggeryModule;
import org.wso2.carbon.jaggery.core.modules.JaggeryScript;
import org.wso2.carbon.jaggery.core.modules.ModuleManager;
import org.wso2.carbon.scriptengine.cache.CacheManager;
import org.wso2.carbon.scriptengine.cache.ScriptCachingContext;
import org.wso2.carbon.scriptengine.engine.RhinoEngine;
import org.wso2.carbon.scriptengine.exceptions.ScriptException;
import org.wso2.carbon.scriptengine.util.HostObjectUtil;

import javax.servlet.ServletContext;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Stack;

/**
 * Shares the common functionality, of initialization of functions, and Host Objects
 */
public class CommonManager {

    private static final int BYTE_BUFFER_SIZE = 1024;

    private static final Log log = LogFactory.getLog(CommonManager.class);

    public static final int ENV_WEBAPP = 0;

    public static final int ENV_COMMAND_LINE = 1;
    public static final String JAGGERY_CONTEXT = "jaggeryContext";
    public static final String JAGGERY_PREFIX = "$j";
    public static final String JAGGERY_URLS_MAP = "jaggery.urls.map";

    public static final String HOST_OBJECT_NAME = "CarbonTopLevel";

    private RhinoEngine engine = null;
    private ModuleManager moduleManager = null;
    private String scriptsDir = null;

    public CommonManager(String jaggeryDir) throws ScriptException {
        this.scriptsDir = jaggeryDir;
        init();
    }

    public RhinoEngine getEngine() {
        return this.engine;
    }

    public ModuleManager getModuleManager() {
        return this.moduleManager;
    }

    public String getScriptsDir() {
        return this.scriptsDir;
    }

    private void init() throws ScriptException {
        String dir = System.getProperty("java.io.tmpdir");
        if (dir != null) {
            this.engine = new RhinoEngine(new CacheManager("jaggery", dir));
        } else {
            String msg = "Please specify java.io.tmpdir system property";
            log.error(msg);
            throw new ScriptException(msg);
        }
        this.moduleManager = new ModuleManager(this.engine, scriptsDir);
        exposeDefaultModules();
    }

    protected void initContext(JaggeryContext context) {
        context.setManager(this);
        context.setEngine(this.engine);
        context.setScope(this.engine.getRuntimeScope());
        setJaggeryContext(context);
    }

    protected void initModule(String module, ScriptableObject object, JaggeryContext context) {

    }

    private void exposeDefaultModules() {
        Context cx = RhinoEngine.enterContext();
        for (JaggeryModule module : this.moduleManager.getModules().values()) {
            if (module.isExpose()) {
                String namespace = module.getNamespace();
                if (namespace == null || namespace.equals("")) {
                    //expose globally
                    exposeModule(module);
                } else {
                    ScriptableObject object = (ScriptableObject) this.engine.newObject();
                    exposeModule(cx, module, object);
                    this.engine.defineProperty(namespace, object, ScriptableObject.READONLY);
                }
            }
        }

    }

    public static void include(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "include";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(HOST_OBJECT_NAME, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(HOST_OBJECT_NAME, functionName, "1", "string", args[0], false);
        }
        JaggeryContext jaggeryContext = CommonManager.getJaggeryContext();
        RhinoEngine engine = jaggeryContext.getEngine();
        if (engine == null) {
            log.error("Rhino Engine in Jaggery context is null");
            throw new ScriptException("Rhino Engine in Jaggery context is null");
        }
        ScriptableObject scope = jaggeryContext.getScope();
        Stack<String> includesCallstack = jaggeryContext.getIncludesCallstack();
        String parent = includesCallstack.lastElement();
        String fileURL = (String) args[0];
        ScriptReader source;
        if (isHTTP(fileURL) || isHTTP(parent)) {
            if (!isHTTP(fileURL)) {
                fileURL = parent + fileURL;
            }
            //this is a remote file url
            try {
                URL url = new URL(fileURL);
                url.openConnection();
                source = new ScriptReader(url.openStream());
                includesCallstack.push(fileURL);
                engine.exec(source, scope, null);
                includesCallstack.pop();
            } catch (MalformedURLException e) {
                String msg = "Malformed URL. function : import, url : " + fileURL;
                log.warn(msg, e);
                throw new ScriptException(msg, e);
            } catch (IOException e) {
                String msg = "IO exception while importing content from url : " + fileURL;
                log.warn(msg, e);
                throw new ScriptException(msg, e);
            }
        } else {
            String msg = "Unsupported file include : " + fileURL;
            throw new ScriptException(msg);
        }
    }

    public static boolean isHTTP(String url) {
        return url.matches("^[hH][tT][tT][pP][sS]?.*");
    }

    public static ScriptableObject require(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException, IOException {
        String functionName = "require";
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs(HOST_OBJECT_NAME, functionName, argsCount, false);
        }
        if (!(args[0] instanceof String)) {
            HostObjectUtil.invalidArgsError(HOST_OBJECT_NAME, functionName, "1", "string", args[0], false);
        }
        String moduleName = (String) args[0];
        JaggeryContext context = getJaggeryContext();
        //RhinoEngine engine = context.getEngine();
        //ScriptableObject scope = context.getScope();
        CommonManager manager = context.getManager();
        ModuleManager moduleManager = manager.getModuleManager();
        JaggeryModule module = moduleManager.getModules().get(moduleName);

        if (module == null) {
            String msg = "A module cannot be found with the specified name : " + moduleName;
            log.error(msg);
            throw new ScriptException(msg);
        }

        ScriptableObject object = (ScriptableObject) RhinoEngine.newObject((ScriptableObject) thisObj);
        exposeModule(cx, module, object);
        manager.initModule(moduleName, object, context);
        return object;
    }

    private static void exposeModule(Context cx, JaggeryModule module, ScriptableObject object) {
        List<JaggeryHostObject> hostObjects = module.getJaggeryHostObjects();
        for (JaggeryHostObject hostObject : hostObjects) {
            RhinoEngine.defineProperty(
                    object, hostObject.getName(), hostObject.getConstructor(), ScriptableObject.READONLY);
        }

        List<JaggeryMethod> methods = module.getJaggeryMethods();
        for (JaggeryMethod method : methods) {
            RhinoEngine.defineFunction(object, method.getName(), method.getMethod(), ScriptableObject.READONLY);
        }

        List<JaggeryScript> scripts = module.getJaggeryScripts();
        for (JaggeryScript script : scripts) {
            script.getScript().exec(cx, object);
        }
    }

    private void exposeModule(JaggeryModule module) {
        List<JaggeryHostObject> hostObjects = module.getJaggeryHostObjects();
        for (JaggeryHostObject hostObject : hostObjects) {
            this.engine.defineProperty(hostObject.getName(), hostObject.getConstructor(), ScriptableObject.READONLY);
        }

        List<JaggeryMethod> methods = module.getJaggeryMethods();
        for (JaggeryMethod method : methods) {
            this.engine.defineFunction(method.getName(), method.getMethod(), ScriptableObject.READONLY);
        }

        List<JaggeryScript> scripts = module.getJaggeryScripts();
        for (JaggeryScript script : scripts) {
            this.engine.defineScript(script.getScript());
        }
    }

    /**
     * JaggeryMethod responsible of writing to the output stream
     */
    public static void print(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "print";
        JaggeryContext jaggeryContext = CommonManager.getJaggeryContext();
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs("CarbonTopLevel", functionName, argsCount, false);
        }
        OutputStream out = jaggeryContext.getOutputStream();
        if (args[0] instanceof StreamHostObject) {
            InputStream in = ((StreamHostObject) args[0]).getStream();
            try {
                byte[] buffer = new byte[BYTE_BUFFER_SIZE];
                int count;
                while ((count = in.read(buffer)) >= 0) {
                    out.write(buffer, 0, count);
                }
                in.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new ScriptException(e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        } else {
            try {
                String content = HostObjectUtil.serializeObject(args[0]);
                for (int i = 0; i < content.length(); i++) {
                    out.write(content.codePointAt(i));
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new ScriptException(e);
            }
        }
    }

    public static JaggeryContext getJaggeryContext() {
        return (JaggeryContext) RhinoEngine.getContextProperty(CommonManager.JAGGERY_CONTEXT);
    }

    public static void setJaggeryContext(JaggeryContext jaggeryContext) {
        RhinoEngine.putContextProperty(CommonManager.JAGGERY_CONTEXT, jaggeryContext);
    }
}
