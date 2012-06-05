package org.wso2.carbon.scriptengine.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.*;
import org.wso2.carbon.scriptengine.cache.CacheManager;
import org.wso2.carbon.scriptengine.cache.CachingContext;
import org.wso2.carbon.scriptengine.cache.ScriptCachingContext;
import org.wso2.carbon.scriptengine.exceptions.ScriptException;
import org.wso2.carbon.scriptengine.util.HostObjectUtil;

import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * The <code>RhinoEngine</code> class acts as a global engine for executing JavaScript codes using Mozilla Rhino. Each engine instance
 * associates a scope and a caching manager.
 * <p>During the class initialization time, it creates a static global scope, which will be cloned upon request. This also has a constructor which
 * accepts class object itself as a parameter. So, it allows you to keep customised versions of RhinoEngine instances.
 * <p>It also has several util methods to register hostobjects, methods, properties with the engine's scope.
 */
@SuppressWarnings({"UnusedDeclaration"})
public class RhinoEngine {

    private static final Log log = LogFactory.getLog(RhinoEngine.class);

    private static ContextFactory contextFactory;

    private CacheManager cacheManager;
    private ScriptableObject engineScope;

    static {
        contextFactory = new CarbonContextFactory();
        ContextFactory.initGlobal(contextFactory);
    }

    /**
     * This constructor gets an existing <code>RhinoEngine</code> instance and returns a new engine which has a clone of the existing engine
     * scope as its scope. Then, any modifications to the scope of new engine instance will be independent of the existing one.
     * <p>Same <code>CacheManager</code> instance of the existing engine will be used as the cache manager of the new object.
     * @param engine An existing {@code RhinoEngine} instance
     */
    public RhinoEngine(RhinoEngine engine) {
        this.cacheManager = engine.getCacheManager();
        Context cx = enterContext();
        setEngineScope(cx, (ScriptableObject) engine.getEngineScope());
        exitContext();
    }

    /**
     * This constructor gets an existing <code>CacheManager</code> instance and returns a new engine instance with the new cache manager.
     * <p>Scope of the engine will be a clone of the static global scope associates with the <code>RhinoEngine</code> class.
     * @param cacheManager  A {@code CacheManager} instance to be used as the cache manager of the engine
     */
    public RhinoEngine(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
        Context cx = enterContext();
        setEngineScope(cx, new CarbonTopLevel(cx, false));
        exitContext();
    }

    /**
     * This constructor gets an existing <code>RhinoEngine</code> instance and a <code>CacheManager</code> instance and returns a new
     * RhinoEngine instance with a cloned scope of the passed engine instance.
     * @param engine An existing {@code RhinoEngine} instance.
     * @param cacheManager A {@code CacheManager} instance to be used as the cache manager of the engine
     * @throws ScriptException If the passed {@code CacheManager} instance is null
     */
    public RhinoEngine(RhinoEngine engine, CacheManager cacheManager) throws ScriptException {
        if (cacheManager == null) {
            String msg = "CacheManager cannot be null. Please specify a cache manager.";
            log.error(msg);
            throw new ScriptException(msg);
        }
        Context cx = enterContext();
        setEngineScope(cx, (ScriptableObject) engine.getEngineScope());
        exitContext();
    }

    /**
     * This method registers a hostobject in the engine scope.
     * @param clazz Hostobject class
     * @throws InvocationTargetException If specified class cannot be invoked
     * @throws InstantiationException If the specified class cannot be instantiated
     * @throws IllegalAccessException If the engine doesn't have access to the specified class
     */
    public void defineClass(Class<Scriptable> clazz) throws InvocationTargetException,
            InstantiationException, IllegalAccessException {
        ScriptableObject.defineClass(engineScope, clazz);
    }

    /**
     * This method registers a hostobject in the specified scope.
     * @param scope Scope to register the specified hostobject
     * @param clazz Hostobject class
     * @throws InvocationTargetException If specified class cannot be invoked
     * @throws InstantiationException If the specified class cannot be instantiated
     * @throws IllegalAccessException If the engine doesn't have access to the specified class
     */
    public static void defineClass(ScriptableObject scope, Class<Scriptable> clazz) throws InvocationTargetException,
            InstantiationException, IllegalAccessException {
        ScriptableObject.defineClass(scope, clazz);
    }

    /**
     * This method registers a script object in the engine scope.
     * @param script Script instance to be defined
     */
    public void defineScript(Script script) {
        Context cx = RhinoEngine.enterContext();
        script.exec(cx, engineScope);
        RhinoEngine.exitContext();
    }

    /**
     * This method registers a script object in the engine scope.
     * @param scriptReader Reader instance to get the script content
     * @throws ScriptException If an error occur while evaluating the script
     */
    public void defineScript(Reader scriptReader) throws ScriptException {
        exec(scriptReader, engineScope, null);
    }

    /**
     * This method registers the specified object in the specified scope.
     * @param scope The scope to register the bean object
     * @param name Property name to register the bean
     * @param object Bean object to be registered
     * @param attribute Integer value specifying access level. i.e. READONLY | DONTENUM | PERMANENT. Refer {@link ScriptableObject}
     */
    public static void defineProperty(ScriptableObject scope, String name, Object object, int attribute) {
        enterContext();
        if ((object instanceof Number) ||
                (object instanceof String) ||
                (object instanceof Boolean)) {
            scope.defineProperty(name, object, attribute);
        } else {
            // Must wrap non-scriptable objects before presenting to Rhino
            Scriptable wrapped = Context.toObject(object, scope);
            scope.defineProperty(name, wrapped, attribute);
        }
        exitContext();
    }

    /**
     * This method registers the specified object in the specified scope.
     * @param name Property name to register the bean
     * @param object Bean object to be registered
     * @param attribute Integer value specifying access level. i.e. readonly,
     */
    public void defineProperty(String name, Object object, int attribute) {
        enterContext();
        if ((object instanceof Number) ||
                (object instanceof String) ||
                (object instanceof Boolean)) {
            engineScope.defineProperty(name, object, attribute);
        } else {
            // Must wrap non-scriptable objects before presenting to Rhino
            Scriptable wrapped = Context.toObject(object, engineScope);
            engineScope.defineProperty(name, wrapped, attribute);
        }
        exitContext();
    }

    /**
     * Evaluates the specified script and the result is returned. If the <code>sctx</code> is provided and cache is upto date, cached script
     * will be evaluated instead of the original one. Otherwise, a either cache will be updated or evaluated the script directly without caching.
     * <p>A clone of the engine scope will be used as the scope during the evaluation.
     * @param scriptReader Reader object to read the script when ever needed
     * @param sctx Script caching context which contains caching data. When null is passed for this, caching will be disabled.
     * @return Returns the resulting object after evaluating script
     * @throws ScriptException If error occurred while evaluating
     */
    public Object eval(Reader scriptReader, ScriptCachingContext sctx) throws ScriptException {
        return evalScript(scriptReader, getRuntimeScope(), sctx);
    }

    /**
     * Evaluates the specified script and the result is returned. If the <code>sctx</code> is provided and cache is upto date, cached script
     * will be evaluated instead of the original one. Otherwise, either the cache will be updated or evaluated the script directly without caching.
     * <p>The specified scope will be used as the scope during the evaluation.
     * @param scriptReader Reader object to read the script when ever needed
     * @param scope Scope to be used during the evaluation
     * @param sctx Script caching context which contains caching data. When null is passed for this, caching will be disabled.
     * @return Returns the resulting object after evaluating script
     * @throws ScriptException If error occurred while evaluating
     */
    public Object eval(Reader scriptReader, ScriptableObject scope, ScriptCachingContext sctx)
            throws ScriptException {
        if (scope == null) {
            String msg = "ScriptableObject value for scope, can not be null.";
            log.error(msg);
            throw new ScriptException(msg);
        }
        return evalScript(scriptReader, scope, sctx);
    }

    /**
     * Executes the script on a clone of the engine scope and the scope is returned.
     * <p>If the <code>sctx</code> is provided and cache is upto date, cached script
     * will be evaluated instead of the original one. Otherwise, either the cache will be updated or evaluated the script directly without caching.
     * @param scriptReader Reader object to read the script when ever needed
     * @param sctx Script caching context which contains caching data. When null is passed for this, caching will be disabled.
     * @return Modified clone of the engine scope
     * @throws ScriptException If error occurred while evaluating
     */
    public ScriptableObject exec(Reader scriptReader, ScriptCachingContext sctx) throws ScriptException {
        return execScript(scriptReader, getRuntimeScope(), sctx);
    }

    /**
     * Executes the script on the specified scope.
     * <p>If the <code>sctx</code> is provided and cache is upto date, cached script
     * will be evaluated instead of the original one. Otherwise, either the cache will be updated or evaluated the script directly without caching.
     * @param scriptReader Reader object to read the script when ever needed
     * @param scope Scope to be used during the execution
     * @param sctx Script caching context which contains caching data. When null is passed for this, caching will be disabled.
     * @throws ScriptException If error occurred while evaluating
     */
    public void exec(Reader scriptReader, ScriptableObject scope, ScriptCachingContext sctx) throws ScriptException {
        if (scope == null) {
            String msg = "ScriptableObject value for scope, can not be null.";
            log.error(msg);
            throw new ScriptException(msg);
        }
        execScript(scriptReader, scope, sctx);
    }

    /**
     * Executes a particular JavaScript function from a script on a clone of engine's scope and returns the result.
     * @param scriptReader Reader object to read the script when ever needed
     * @param funcName Name of the function to be invoked
     * @param args Arguments for the functions as an array of objects
     * @param sctx Script caching context which contains caching data. When null is passed for this, caching will be disabled.
     * @return  Returns the resulting object after invoking the function
     * @throws ScriptException If error occurred while invoking the function
     */
    public Object call(Reader scriptReader, String funcName, Object[] args, ScriptCachingContext sctx)
            throws ScriptException {
        return execFunc(scriptReader, funcName, args, getRuntimeScope(), getRuntimeScope(), sctx);
    }

    /**
     * Executes a particular JavaScript function from a script on a clone of engine's scope and returns the result.
     * @param scriptReader Reader object to read the script when ever needed
     * @param funcName Name of the function to be invoked
     * @param args Arguments for the functions as an array of objects
     * @param thiz {@code this} object for the function
     * @param sctx Script caching context which contains caching data. When null is passed for this, caching will be disabled.
     * @return  Returns the resulting object after invoking the function
     * @throws ScriptException If error occurred while invoking the function
     */
    public Object call(Reader scriptReader, String funcName, Object[] args, ScriptableObject thiz,
                       ScriptCachingContext sctx) throws ScriptException {
        if (thiz == null) {
            String msg = "ScriptableObject value for thiz, can not be null.";
            log.error(msg);
            throw new ScriptException(msg);
        }
        return execFunc(scriptReader, funcName, args, thiz, getRuntimeScope(), sctx);
    }


    /**
     * Executes a particular JavaScript function from a script on the specified scope and returns the result.
     * @param scriptReader Reader object to read the script when ever needed
     * @param funcName Name of the function to be invoked
     * @param args Arguments for the functions as an array of objects
     * @param thiz {@code this} object for the function
     * @param scope The scope where function will be executed
     * @param sctx Script caching context which contains caching data. When null is passed for this, caching will be disabled.
     * @return  Returns the resulting object after invoking the function
     * @throws ScriptException If error occurred while invoking the function
     */
    public Object call(Reader scriptReader, String funcName, Object[] args, ScriptableObject thiz,
                       ScriptableObject scope, ScriptCachingContext sctx) throws ScriptException {
        if (scope == null) {
            String msg = "ScriptableObject value for scope, can not be null.";
            log.error(msg);
            throw new ScriptException(msg);
        }
        if (thiz == null) {
            String msg = "ScriptableObject value for thiz, can not be null.";
            log.error(msg);
            throw new ScriptException(msg);
        }
        return execFunc(scriptReader, funcName, args, thiz, scope, sctx);
    }

    /**
     * This clones the engine scope and returns.
     * @return Cloned scope
     */
    public ScriptableObject getRuntimeScope() {
        Context cx = enterContext();
        ScriptableObject global = new CarbonTopLevel(cx, false);
        ScriptableObject parentScope = (ScriptableObject) cx.newObject(global);
        ScriptableObject scope = (ScriptableObject) cx.newObject(global);

        parentScope.setPrototype(global);
        parentScope.setParentScope(null);

        scope.setPrototype(parentScope);
        scope.setParentScope(parentScope);
        copyEngineScope(engineScope, parentScope);

        exitContext();
        return scope;
    }

    /**
     * This method seals the engine scope. i.e. Any further modification to the engine scope will be voided.
     */
    public void sealEngine() {
        engineScope.sealObject();
    }

    /**
     *
     * @param constructor
     * @param scope
     * @param args
     * @return
     */
    public static Scriptable newObject(String constructor, ScriptableObject scope, Object[] args) {
        Context cx = enterContext();
        Scriptable obj = cx.newObject(scope, constructor, args);
        exitContext();
        return obj;
    }

    public static Scriptable newObject(ScriptableObject scope) {
        Context cx = enterContext();
        Scriptable obj = cx.newObject(scope);
        exitContext();
        return obj;
    }

    public Scriptable newObject() {
        Context cx = enterContext();
        Scriptable obj = cx.newObject(getRuntimeScope());
        exitContext();
        return obj;
    }

    public void defineFunction(String jsName, Method method, int attribute) {
        FunctionObject f = new FunctionObject(jsName, method, engineScope);
        engineScope.defineProperty(jsName, f, attribute);
    }

    public static void defineFunction(ScriptableObject scope, String jsName, Method method, int attribute) {
        FunctionObject f = new FunctionObject(jsName, method, scope);
        scope.defineProperty(jsName, f, attribute);
    }

    public static void putContextProperty(Object key, Object value) {
        Context cx = enterContext();
        cx.putThreadLocal(key, value);
        exitContext();
    }

    public static Object getContextProperty(Object key) {
        Context cx = enterContext();
        Object value = cx.getThreadLocal(key);
        exitContext();
        return value;
    }

    public static ScriptableObject cloneScope(ScriptableObject scope) {
        Context cx = enterContext();
        ScriptableObject clone = (ScriptableObject) cx.newObject(scope);
        clone.setPrototype(scope.getPrototype());
        clone.setParentScope(scope.getParentScope());
        exitContext();
        return clone;
    }

    public static Context enterContext() {
        return contextFactory.enterContext();
    }

    public static void exitContext() {
        Context.exit();
    }

    private void setEngineScope(Context cx, ScriptableObject scope) {
        engineScope = (ScriptableObject) cx.newObject(scope);
        engineScope.setPrototype(scope);
        engineScope.setParentScope(null);
    }

    private Scriptable getEngineScope() {
        return engineScope;
    }

    private CacheManager getCacheManager() {
        return cacheManager;
    }

    private Object execFunc(Reader scriptReader, String funcName, Object[] args, ScriptableObject thiz,
                            ScriptableObject scope, ScriptCachingContext sctx) throws ScriptException {
        Context cx = enterContext();
        try {
            if (sctx == null) {
                cx.evaluateString(scope, HostObjectUtil.readerToString(scriptReader), "wso2js", 1, null);
            } else {
                Script script = cacheManager.getScriptObject(scriptReader, sctx);
                if (script == null) {
                    cacheManager.cacheScript(scriptReader, sctx);
                    script = cacheManager.getScriptObject(scriptReader, sctx);
                }
                script.exec(cx, scope);
            }
            return execFunc(funcName, args, thiz, scope, cx);
        } catch (Exception e) {
            log.error(e);
            throw new ScriptException(e);
        } finally {
            exitContext();
        }
    }

    private static Object execFunc(String funcName, Object[] args, ScriptableObject thiz,
                                   ScriptableObject scope, Context cx) throws ScriptException {
        Object object = scope.get(funcName, scope);
        if (!(object instanceof Function)) {
            String msg = "Function cannot be found with the name '" + funcName + "', but a " + object.toString();
            log.error(msg);
            throw new ScriptException(msg);
        }
        try {
            return ((Function) object).call(cx, scope, thiz, args);
        } catch (Exception e) {
            log.error(e);
            throw new ScriptException(e);
        }
    }

    private Object evalScript(Reader scriptReader, ScriptableObject scope, ScriptCachingContext sctx)
            throws ScriptException {
        Context cx = enterContext();
        Object result;
        try {
            if (sctx == null) {
                result = cx.evaluateString(scope, HostObjectUtil.readerToString(scriptReader), "wso2js", 1, null);
            } else {
                Script script = cacheManager.getScriptObject(scriptReader, sctx);
                if (script == null) {
                    cacheManager.cacheScript(scriptReader, sctx);
                    script = cacheManager.getScriptObject(scriptReader, sctx);
                }
                result = script.exec(cx, scope);
            }
            return result;
        } catch (Exception e) {
            log.error(e);
            throw new ScriptException(e);
        } finally {
            exitContext();
        }
    }

    private ScriptableObject execScript(Reader scriptReader, ScriptableObject scope, ScriptCachingContext sctx)
            throws ScriptException {
        Context cx = enterContext();
        try {
            if (sctx == null) {
                cx.evaluateString(scope, HostObjectUtil.readerToString(scriptReader), "wso2js", 1, null);
            } else {
                Script script = cacheManager.getScriptObject(scriptReader, sctx);
                if (script == null) {
                    cacheManager.cacheScript(scriptReader, sctx);
                    script = cacheManager.getScriptObject(scriptReader, sctx);
                }
                script.exec(cx, scope);
            }
            return scope;
        } catch (Exception e) {
            log.error(e);
            throw new ScriptException(e);
        } finally {
            exitContext();
        }
    }

    private static void copyEngineScope(ScriptableObject engineScope, ScriptableObject scope) {
        Object[] objs = engineScope.getAllIds();
        for(Object obj : objs) {
            String id = (String) obj;
            scope.put(id, scope, engineScope.get(id, engineScope));
        }
    }
}
