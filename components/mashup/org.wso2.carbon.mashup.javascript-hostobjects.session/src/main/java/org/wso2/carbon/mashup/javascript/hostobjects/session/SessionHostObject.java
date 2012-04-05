/*
 * Copyright 2006,2007 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.mashup.javascript.hostobjects.session;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.AxisFault;
import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

/**
 * <p/>
 * This is a JavaScript Rhino host object to provide the ability for the users
 * to share objects across different service invocations. Scope for the session
 * of a deployed mashup can be given using the "scope" service property
 * annotation.
 * </p>
 * <p/>
 * For more information refer to <a
 * href="http://www.wso2.org/wiki/display/mashup/Session+Host+Object">JavaScript
 * Session Host Object</a>.
 */
public class SessionHostObject extends ScriptableObject {

    Map sessionMap;

    private static Logger log = Logger.getLogger(SessionHostObject.class);

    public static final String MASHUP_SESSION_OBJECT = "mashupSessionObject";

    /**
     * Contructor that will be used by the java engine..
     */
    public SessionHostObject() {
        super();
        sessionMap = new Hashtable();
    }

    /**
     * <p>Constructor that gets called when creating the session object from Rhino.</p>
     * <p>Retrieve the instance of this class available in the ServiceGroupContext
     * of the current invocation and returns is to the user. If an instance of
     * this is not available in the ServiceGroupContext, create an instance of
     * this object and stores it in the ServiceGroupContext of the current
     * message.</p>
     *
     * @throws IOException
     */
    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj,
                                           boolean inNewExpr) throws AxisFault {
        SessionHostObject sessionHostObject;
        Object object = cx.getThreadLocal("messageContext");
        if (object instanceof MessageContext) {
            MessageContext messageContext = (MessageContext) object;
            Object mashupSessionObject =
                    messageContext.getServiceGroupContext().getProperty(MASHUP_SESSION_OBJECT);
            if (mashupSessionObject == null) {
                sessionHostObject = new SessionHostObject();
                ServiceGroupContext serviceGroupContext = messageContext.getServiceGroupContext();
                serviceGroupContext.setProperty(MASHUP_SESSION_OBJECT, sessionHostObject);
            } else {
                sessionHostObject = (SessionHostObject) mashupSessionObject;
            }
            if (log.isDebugEnabled()) {
                log.debug("Instantiated the Session Host Object");
            }
            return sessionHostObject;
        }
        if (log.isDebugEnabled()) {
            log.debug("Instantiated the Session Host Object without MessageContext.");
        }
        // This is to make sure that the java script service deployer does not
        // fail due to the non-availability of the MessageContext.
        return new SessionHostObject();
    }

    /* (non-Javadoc)
     * @see org.mozilla.javascript.ScriptableObject#getClassName()
     */
    public String getClassName() {
        return "Session";
    }

    /**
     * Puts the key value pair in to the java script session object. This pair
     * will be available in the session object for the service invocations
     * across the session.
     * <p/>
     * <pre>
     * session.put(&quot;name&quot;, &quot;WSO2 Mashup Server&quot;);
     * </pre>
     *
     * @param key - The key of the object added to the session
     * @param object - The value of the object added to the session
     */
    public void jsFunction_put(String key, Object object) {
        sessionMap.put(key, object);
    }

    /**
     * Retrieves the value associated with the given "key" from the current java
     * script session object.
     * <p/>
     * <pre>
     * session.get(&quot;name&quot;);
     * </pre>
     *
     * @param key - The key of the object that the value is needed for
     * @return the value associated with the given key or null if there isn't a associated value.
     */
    public Object jsFunction_get(String key) {
        return sessionMap.get(key);
    }

    /**
     * Removes the value associated with the given "key" from the current java
     * script session object.
     * <p/>
     * <pre>
     * session.remove(&quot;name&quot;);
     * </pre>
     *
     * @param key - The key of the object that needs to be removed
     */
    public void jsFunction_remove(String key) {
        sessionMap.remove(key);
    }

    /**
     * Removes all values (clears) from the current java script session object.
     * <p/>
     * <pre>
     * session.clear();
     * </pre>
     */
    public void jsFunction_clear() {
        sessionMap.clear();
    }

    /**
     * Returns the number of elements contained in the session
     * <pre>
     * var x = session.size();
     * </pre>
     * @return An integer containing the number of elements stored in session
     */
    public int jsFunction_size(){
        return sessionMap.size();
    }

    /**
     * Returns an array of strings containing all the key values, which represent the elements stored in session
     * <pre>
     * var keys = session.getKeys();
     * </pre>
     * @return An array of strings, which represent the keys of the elements stored in session
     */
    public String[] jsFunction_getKeys(){
        String[] keyArray = new String[sessionMap.size()];
        sessionMap.keySet().toArray(keyArray);
        return keyArray;
    }
}
