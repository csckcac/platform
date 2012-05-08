package org.wso2.carbon.hostobjects.sysprop;


import org.mozilla.javascript.ScriptableObject;

/**
 * This is a host object used to get system properties 
 */
public class SystemPropertyReaderObject extends ScriptableObject{
    private static final String CARBON_HOME="carbon.home";
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
