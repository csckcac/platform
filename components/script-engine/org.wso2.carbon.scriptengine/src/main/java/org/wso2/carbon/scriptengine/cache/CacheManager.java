package org.wso2.carbon.scriptengine.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.*;
import org.mozilla.javascript.optimizer.ClassCompiler;
import org.mozilla.javascript.tools.ToolErrorReporter;
import org.wso2.carbon.scriptengine.exceptions.ScriptException;
import org.wso2.carbon.scriptengine.util.HostObjectUtil;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CacheManager {

    private static final Log log = LogFactory.getLog(CacheManager.class);

    private static final String PACKAGE_NAME = "org.wso2.carbon.rhino";

    private ClassCompiler compiler;

    private String name;

    private String cacheManagerDir;

    private String cacheDir;

    private ConcurrentMap<String, TenantWrapper> tenants = new ConcurrentHashMap<String, TenantWrapper>();

    private final Object lock0 = new Object();
    private final Object lock1 = new Object();

    public CacheManager(String name, String cacheDir, CompilerEnvirons compilerEnv) {
        this.name = name;
        this.cacheManagerDir = filterNameForDir(name);
        this.cacheDir = cacheDir;
        if (compilerEnv == null) {
            compilerEnv = new CompilerEnvirons();
            compilerEnv.setErrorReporter(new ToolErrorReporter(true));
        }
        this.compiler = new ClassCompiler(compilerEnv);
    }

    public CacheManager(String name, String cacheDir) {
        this(name, cacheDir, null);
    }

    public String getName() {
        return name;
    }

    public void invalidateCache(ScriptCachingContext sctx) throws ScriptException {
        synchronized (lock0) {
            CachingContext ctx = getCachingContext(sctx);
            if (ctx == null) {
                return;
            }
            String location = ctx.getClassFilePath();
            File classFile = new File(location);
            if (classFile.exists() && !classFile.delete()) {
                String msg = "Unable to delete the file " + location;
                log.warn(msg);
                throw new ScriptException(msg);
            }
            TenantWrapper tenant = tenants.get(ctx.getTenantId());
            tenant.removeCachingContext(sctx);
        }
    }

    public synchronized void cacheScript(Reader scriptReader, ScriptCachingContext sctx) throws ScriptException {
        if (scriptReader == null) {
            String msg = "Unable to find the Reader for script source in CachingContext";
            log.error(msg);
            throw new ScriptException(msg);
        }
        String className;
        String classLocation;
        TenantWrapper tenant = initContexts(sctx);
        CachingContext ctx = getCachingContext(sctx);
        if (ctx != null) {
            if (sctx.getSourceModifiedTime() <= ctx.getSourceModifiedTime()) {
                return;
            }
            className = ctx.getClassName();
            classLocation = ctx.getClassFilePath();
            invalidateCache(sctx);
            ctx.setSourceModifiedTime(0L);
            ctx.setCacheUpdatedTime(0L);
        } else {
            className = getClassName(tenant, sctx);
            classLocation = getClassLocation(sctx, className);
            ctx = new CachingContext(sctx.getContext(), sctx.getPath(), sctx.getCacheKey());
            ctx.setTenantId(sctx.getTenantId());
            ctx.setContext(sctx.getContext());
            ctx.setPath(sctx.getPath());
            ctx.setCacheKey(sctx.getCacheKey());
            ctx.setClassName(className);
            ctx.setClassFilePath(classLocation);
        }
        try {
            String scriptPath = sctx.getContext() + sctx.getPath() + sctx.getCacheKey();
            Object[] compiled = compiler.compileToClassFiles(
                    HostObjectUtil.readerToString(scriptReader), scriptPath, 1, className);
            writeClass(classLocation, (byte[]) compiled[1]);
            updateContexts(className, ctx, sctx);
            tenant.setCachingContext(ctx);
        } catch (Exception e) {
            throw new ScriptException(e);
        }
    }

    public Script getScriptObject(Reader scriptReader, ScriptCachingContext sctx) throws ScriptException {
        CachingContext ctx = getCachingContext(sctx);
        if (ctx == null) {
            return null;
        }
        // source file has been modified, so we recreate
        if (sctx.getSourceModifiedTime() > ctx.getSourceModifiedTime()) {
            cacheScript(scriptReader, sctx);
            ctx = getCachingContext(sctx);
        }
        return ctx.getScript();
    }

    public boolean isCached(ScriptCachingContext sctx) {
        return !(getCachingContext(sctx) == null);
    }

    public boolean isOlder(ScriptCachingContext sctx) {
        CachingContext ctx = getCachingContext(sctx);
        return ctx == null || sctx.getSourceModifiedTime() > ctx.getSourceModifiedTime();
    }


    private void writeClass(String classLocation, byte[] classBytes) throws ScriptException {

        FileOutputStream classFileStream = null;
        try {
            String parentDir = classLocation.substring(0, classLocation.lastIndexOf(File.separator));
            File parent = new File(parentDir);
            if (!parent.exists()) {
                if (!parent.mkdirs()) {
                    String msg = "Error while creating the directory : " + parentDir;
                    throw new ScriptException(msg);
                }
            }
            classFileStream = new FileOutputStream(classLocation);
            classFileStream.write(classBytes);
        } catch (FileNotFoundException e) {
            String msg = "Error while creating the file file " + classLocation;
            log.error(msg, e);
            throw new ScriptException(msg, e);
        } catch (IOException e) {
            String msg = "Error while writing the file " + classLocation;
            log.error(msg, e);
            throw new ScriptException(msg, e);
        } finally {
            if (classFileStream != null) {
                try {
                    classFileStream.close();
                } catch (IOException e) {
                    log.warn(e);
                }
            }
        }
    }

    private CompilationContext getCompilationContext(ScriptCachingContext sctx) throws ScriptException {
        TenantWrapper tenant = initContexts(sctx);
        CompilationContext cCtx = tenant.getCompilationContext(sctx);
        if (cCtx != null) {
            return cCtx;
        }
        synchronized (lock1) {
            cCtx = tenant.getCompilationContext(sctx);
            if (cCtx != null) {
                return cCtx;
            }
            cCtx = new CompilationContext();
            String url = "file:" + getSourceDir(sctx) + File.separator;

            cCtx.setClassLoader(createClassLoader(url));
            cCtx.setClassPath(url);
            tenant.setCompilationContext(sctx, cCtx);
            return cCtx;
        }
    }

    private ClassLoader createClassLoader(String classpath) throws ScriptException {
        try {
            ClassLoader parent = Thread.currentThread().getContextClassLoader();
            return URLClassLoader.newInstance(new URL[]{new URL(classpath)}, parent);
        } catch (MalformedURLException e) {
            log.error(e);
            throw new ScriptException(e);
        }
    }

    private TenantWrapper initContexts(ScriptCachingContext sctx) {
        TenantWrapper tenant = tenants.get(sctx.getTenantId());
        if (tenant == null) {
            tenant = new TenantWrapper();
            tenants.put(sctx.getTenantId(), tenant);
        }
        ContextWrapper context = tenant.getContext(sctx);
        if (context == null) {
            context = new ContextWrapper();
            tenant.setContext(sctx, context);
        }
        PackageWrapper packageWrapper = context.getPackage(sctx.getPath());
        if (packageWrapper == null) {
            packageWrapper = new PackageWrapper();
            context.setPackage(sctx.getPath(), packageWrapper);
        }
        return tenant;
    }

    private void updateContexts(String className, CachingContext ctx, ScriptCachingContext sctx) throws ScriptException {
        //update the classLoader in CompilationContext
        CompilationContext cCtx = getCompilationContext(sctx);
        ClassLoader classLoader = createClassLoader(cCtx.getClassPath());
        cCtx.setClassLoader(classLoader);
        //setup rest of the CachingContext
        ctx.setCompilationContext(cCtx);
        //commend following line to avoid in-memory caching
        ctx.setScript(loadScript(classLoader, className));
        ctx.setCacheUpdatedTime(System.currentTimeMillis());
        ctx.setSourceModifiedTime(sctx.getSourceModifiedTime());
    }

    protected static Script loadScript(ClassLoader classLoader, String className) throws ScriptException {
        try {
            Class clazz = classLoader.loadClass(className);
            Constructor<?> constructor = clazz.getConstructor(new Class[0]);
            return (Script) constructor.newInstance();
        } catch (ClassNotFoundException e) {
            String msg = "Cached *.class file of " + className + " cannot be found";
            log.error(msg, e);
            throw new ScriptException(msg, e);
        } catch (InvocationTargetException e) {
            String msg = "Error invoking constructor of " + className;
            log.error(msg, e);
            throw new ScriptException(msg, e);
        } catch (InstantiationException e) {
            String msg = "Error instantiating object of " + className;
            log.error(msg, e);
            throw new ScriptException(msg, e);
        } catch (IllegalAccessException e) {
            String msg = "Error accessing the constructor of " + className;
            log.error(msg, e);
            throw new ScriptException(msg, e);
        } catch (NoSuchMethodException e) {
            String msg = "Empty contructor cannot be found for " + className;
            log.error(msg, e);
            throw new ScriptException(msg, e);
        }
    }

    private String getClassLocation(ScriptCachingContext sctx, String className) {
        String classLocation = className.replace(".", File.separator);
        return getSourceDir(sctx) + File.separator + classLocation + ".class";
    }

    private String getSourceDir(ScriptCachingContext sctx) {
        String filteredTenantId = filterNameForDir(sctx.getTenantId());
        String filteredContext = filterNameForDir(sctx.getContext());
        return cacheDir + cacheManagerDir + filteredTenantId + filteredContext;
    }

    private static String getClassName(TenantWrapper tenant, ScriptCachingContext sctx) throws ScriptException {
        String filteredPath = getPackage(sctx.getPath());
        PackageWrapper packageWrapper = tenant.getPath(sctx);
        long classIndex = packageWrapper.getClassIndex();
        packageWrapper.setClassIndex(classIndex + 1);
        return PACKAGE_NAME + filteredPath + ".c" + classIndex;
    }

    public static String getPackage(String path) {
        path = normalizePath(path);
        path = path.replaceAll("[-\\(\\)\\s]", "_")
                .replace("/", ".")
                .replaceAll("(.)([0-9])", "$1_$2");
        return path;
    }

    public static String filterNameForDir(String path) {
        path = normalizePath(path);
        return path.replace("/", File.separator);
    }

    public static String normalizePath(String path) {
        if (path.equals("") || path.equals("/")) {
            return "";
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    private CachingContext getCachingContext(ScriptCachingContext sctx) {
        TenantWrapper tenant = tenants.get(sctx.getTenantId());
        if (tenant == null) {
            return null;
        }
        return tenant.getCachingContext(sctx);
    }
}
