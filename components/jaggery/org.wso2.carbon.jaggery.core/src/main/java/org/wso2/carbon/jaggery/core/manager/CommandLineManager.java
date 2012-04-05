package org.wso2.carbon.jaggery.core.manager;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.carbon.scriptengine.cache.CacheManager;
import org.wso2.carbon.scriptengine.engine.RhinoEngine;
import org.wso2.carbon.scriptengine.exceptions.ScriptException;
import org.wso2.carbon.scriptengine.util.HostObjectUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.Iterator;

/**
 * Shares the common functionality, of initialization of functions, and Host Objects
 */
public final class CommandLineManager {

    private static final Log log = LogFactory.getLog(CommandLineManager.class);
    private static final RhinoEngine RHINO_ENGINE;

    public static final int ENV_WEBAPP = 0;
    public static final int ENV_COMMAND_LINE = 1;
    public static final String JAGGERY_CONTEXT = "jaggeryContext";

   
    private CommandLineManager() {
        //disable external instantiation
    }

    static {
        String dir = System.getProperty("java.io.tmpdir");
        if(dir != null) {
            RHINO_ENGINE = new RhinoEngine(new CacheManager("jaggery", dir));
            initEngine();
        } else {
            RHINO_ENGINE = null;
            String msg = "Please specify java.io.tmpdir system property";
            log.error(msg);
        }
    }

    public static RhinoEngine getEngine() {
        return RHINO_ENGINE;
    }

    protected static void initEngine() {
        try {
            InputStream inputStream = CommandLineManager.class.
                    getClassLoader().getResourceAsStream("META-INF/hostobjects.xml");
            StAXOMBuilder builder = new StAXOMBuilder(inputStream);
            OMElement document = builder.getDocumentElement();
            Iterator itr = document.getChildrenWithLocalName("hostObject");
            while (itr.hasNext()) {
                OMElement hostObject = (OMElement) itr.next();
                String className = hostObject.getFirstChildWithName(new QName(null, "className")).getText();
                String msg = "Error while registering the hostobject : " + className;
                try {
                    RHINO_ENGINE.defineClass((Class<Scriptable>) Class.forName(className));
                } catch (IllegalAccessException e) {
                    log.error(msg, e);
                } catch (InstantiationException e) {
                    log.error(msg, e);
                } catch (InvocationTargetException e) {
                    log.error(msg, e);
                } catch (ClassNotFoundException e) {
                    log.error(msg, e);
                }
            }
        } catch (XMLStreamException e) {
            log.error("Error while reading the hostobjects.xml", e);
        }
        initGlobalProperties();
        //engine.sealEngine();
    }

    private static void initGlobalProperties() {

        try {
            Method method = CommandLineManager.class.getDeclaredMethod(
                    "print", Context.class, Scriptable.class, Object[].class, Function.class);
            RHINO_ENGINE.defineFunction("print", method,  ScriptableObject.PERMANENT);
        } catch (NoSuchMethodException e) {
            log.error("Error initializing global properties", e);
        }
    }

    /**
     * Method responsible of writing to the output stream
     */
    public static void print(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws ScriptException, IOException {
        String functionName = "print";
        JaggeryContext jaggeryContext = CommandLineManager.getJaggeryContext();
        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs("CarbonTopLevel", functionName, argsCount, false);
        }
        PrintWriter writer = new PrintWriter(jaggeryContext.getOutputStream());
        writer.write(HostObjectUtil.serializeObject(args[0]));
        writer.close();
    }

    public static JaggeryContext getJaggeryContext() {
        return (JaggeryContext) RhinoEngine.getContextProperty(CommandLineManager.JAGGERY_CONTEXT);
    }

    public static void setJaggeryContext(JaggeryContext jaggeryContext) {
        RhinoEngine.putContextProperty(CommandLineManager.JAGGERY_CONTEXT, jaggeryContext);
    }
}
