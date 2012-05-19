package org.wso2.carbon.hostobjects.sysprop;


import org.mozilla.javascript.ScriptableObject;

/**
 * This is a host object used to get system properties 
 */
public class SystemPropertyReaderObject extends ScriptableObject{

    private static final long serialVersionUID = -6209794911636336591L;
    
    private static final String hostObjectName = "SystemPropertyReaderObject";

    /**
     * 
     * @return systemProperty
     * @throws Exception
     */
    public String jsFunction_getProperty(String propertyKey) throws Exception {
        return System.getProperty(propertyKey);
    }
    @Override
    public String getClassName() {
        return hostObjectName;
    }
}
