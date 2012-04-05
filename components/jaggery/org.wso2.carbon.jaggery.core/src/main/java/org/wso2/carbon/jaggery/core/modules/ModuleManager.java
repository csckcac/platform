package org.wso2.carbon.jaggery.core.modules;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.*;
import org.wso2.carbon.scriptengine.engine.RhinoEngine;
import org.wso2.carbon.scriptengine.exceptions.ScriptException;
import org.wso2.carbon.scriptengine.util.HostObjectUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ModuleManager {

    private static final String NAMESPACE = "namespace";

	private static final String EXPOSE = "expose";

	private static final String MODULE = "module";

	private static final String NAME = "name";

	private static final Log log = LogFactory.getLog(ModuleManager.class);

    public static final String MODULE_NAMESPACE = "http://wso2.org/projects/jaggery/modules.xml";

    private static final String MODULES_FILE = "modules.xml";

    private final Map<String, JaggeryModule> modules = new HashMap<String, JaggeryModule>();

    private RhinoEngine engine = null;
    private String jaggeryDir = null;

    public ModuleManager(RhinoEngine engine, String jaggeryDir) throws ScriptException {
        this.engine = engine;
        this.jaggeryDir = jaggeryDir;
        init();
    }

    private void init() throws ScriptException {
        //load framework modules
        InputStream xmlStream = ModuleManager.class.
                getClassLoader().getResourceAsStream("META-INF/" + MODULES_FILE);
        initModules(xmlStream, false);

        //load user-defined modules
        File file = new File(jaggeryDir + File.separator + MODULES_FILE);
        if (file.exists()) {
            try {
                xmlStream = new FileInputStream(file);
                initModules(xmlStream, true);
            } catch (FileNotFoundException e) {
                log.error(e.getMessage(), e);
                throw new ScriptException(e);
            }
        }
    }

    public Map<String, JaggeryModule> getModules() {
        return this.modules;
    }

    private void initModules(InputStream modulesXML, boolean custom) throws ScriptException {
        try {
            Context cx = RhinoEngine.enterContext();
            ScriptableObject runtime = engine.getRuntimeScope();
            StAXOMBuilder builder = new StAXOMBuilder(modulesXML);
            OMElement document = builder.getDocumentElement();
            Iterator itr = document.getChildrenWithName(new QName(MODULE_NAMESPACE, MODULE));
            while (itr.hasNext()) {
                OMElement moduleOM = (OMElement) itr.next();
                String moduleName = moduleOM.getAttributeValue(new QName(null, NAME));
                String namespace = moduleOM.getAttributeValue(new QName(null, NAMESPACE));
                boolean expose = "true".equalsIgnoreCase(moduleOM.getAttributeValue(new QName(null, EXPOSE)));

                JaggeryModule jaggeryModule = new JaggeryModule(moduleName);
                jaggeryModule.setNamespace(namespace);
                jaggeryModule.setExpose(expose);

                initHostObjects(moduleOM, runtime, jaggeryModule, custom);
                initMethods(moduleOM, jaggeryModule, custom);
                initScripts(moduleOM, cx, jaggeryModule, custom);

                modules.put(moduleName, jaggeryModule);
            }
        } catch (XMLStreamException e) {
            String msg = "Error while reading the modules.xml";
            log.error(msg, e);
            throw new ScriptException(msg, e);
        } finally {
            RhinoEngine.exitContext();
        }
    }

    private void initScripts(OMElement moduleOM, Context cx, JaggeryModule jaggeryModule, boolean custom) throws ScriptException {
        String name = null;
        String path = null;
        Iterator itr = moduleOM.getChildrenWithName(new QName(MODULE_NAMESPACE, "script"));
        while (itr.hasNext()) {
            try {
                //process methods
                OMElement scriptOM = (OMElement) itr.next();
                name = scriptOM.getFirstChildWithName(
                        new QName(MODULE_NAMESPACE, NAME)).getText();
                path = scriptOM.getFirstChildWithName(
                        new QName(MODULE_NAMESPACE, "path")).getText();
                JaggeryScript jaggeryScript = new JaggeryScript(name);
                jaggeryScript.setPath(path);

                Reader reader;
                if (custom) {
                    reader = new FileReader(jaggeryDir + filterPath(path));
                } else {
                    try {
                        Class c = Class.forName("org.wso2.carbon.hostobjects.scripts.ScriptInitializer");
                        ClassLoader loader = c.getClassLoader();
                        reader = new InputStreamReader(loader.getResourceAsStream(path));
                    } catch (ClassNotFoundException e) {
                        String msg = "Jaggery core scripts initializer cannot be found";
                        log.error(msg, e);
                        throw new ScriptException(msg, e);
                    }
                }
                jaggeryScript.setScript(cx.compileReader(reader, name, 1, null));
                jaggeryModule.addScript(jaggeryScript);
            } catch (FileNotFoundException e) {
                String msg = "Error executing script. Script cannot be found, name : " + name + ", path : " + path;
                log.error(msg, e);
                if(!custom) {
                    throw new ScriptException(msg, e);
                }
            } catch (IOException e) {
                String msg = "Error executing script. Script cannot be found, name : " + name + ", path : " + path;
                log.error(msg, e);
                if(!custom) {
                    throw new ScriptException(msg, e);
                }
            }
        }
    }

    private void initMethods(OMElement moduleOM, JaggeryModule jaggeryModule, boolean custom) throws ScriptException {
        String name = null;
        String className = null;
        Iterator itr = moduleOM.getChildrenWithName(new QName(MODULE_NAMESPACE, "method"));
        while (itr.hasNext()) {
            try {
                //process methods
                OMElement methodOM = (OMElement) itr.next();
                name = methodOM.getFirstChildWithName(
                        new QName(MODULE_NAMESPACE, NAME)).getText();
                className = methodOM.getFirstChildWithName(
                        new QName(MODULE_NAMESPACE, "className")).getText();
                JaggeryMethod jaggeryMethod = new JaggeryMethod(name);
                jaggeryMethod.setClassName(className);

                Class clazz = Class.forName(className);
                Method method = clazz.getDeclaredMethod(
                        name, Context.class, Scriptable.class, Object[].class, Function.class);
                jaggeryMethod.setMethod(method);
                jaggeryModule.addMethod(jaggeryMethod);
            } catch (NoSuchMethodException e) {
                String msg = "Error registering method. Method cannot be found, name : " +
                        name + ", class : " + className;
                log.error(msg, e);
                if(!custom) {
                    throw new ScriptException(msg, e);
                }
            } catch (ClassNotFoundException e) {
                String msg = "Error registering method. Class cannot be found, name : " +
                        name + ", class : " + className;
                log.error(msg, e);
                if(!custom) {
                    throw new ScriptException(msg, e);
                }
            }
        }
    }

    private void initHostObjects(OMElement moduleOM, ScriptableObject runtime,
                                 JaggeryModule jaggeryModule, boolean custom) throws ScriptException {
        Iterator itr = moduleOM.getChildrenWithName(new QName(MODULE_NAMESPACE, "hostObject"));
        while (itr.hasNext()) {
            //process hostobject
            OMElement hostObjectOM = (OMElement) itr.next();
            String name = hostObjectOM.getFirstChildWithName(
                    new QName(MODULE_NAMESPACE, NAME)).getText();
            String className = hostObjectOM.getFirstChildWithName(
                    new QName(MODULE_NAMESPACE, "className")).getText();
            JaggeryHostObject jaggeryHostObject = new JaggeryHostObject(name);
            jaggeryHostObject.setClassName(className);

            HostObjectUtil.registerHostObject(engine, runtime, className);
            Object obj = ScriptableObject.getProperty(runtime, name);
            if (obj instanceof UniqueTag) {
                String msg = "Error registering HostObject. Constructor cannot be found, name : " +
                        name + ", class : " + className;
                log.error(msg);
                if(!custom) {
                    throw new ScriptException(msg);
                }
            } else {
                BaseFunction constructor = (BaseFunction) ScriptableObject.getProperty(runtime, name);
                ScriptableObject.deleteProperty(runtime, name);
                jaggeryHostObject.setConstructor(constructor);
                jaggeryModule.addHostObject(jaggeryHostObject);
            }
        }
    }

    private String filterPath(String path) {
        String pathToReturn = path.replace('\\', File.separatorChar).replace('/', File.separatorChar);
        if (!pathToReturn.startsWith(File.separator)) {
        	pathToReturn = File.separator + pathToReturn;
        }
        return pathToReturn;
    }

}
