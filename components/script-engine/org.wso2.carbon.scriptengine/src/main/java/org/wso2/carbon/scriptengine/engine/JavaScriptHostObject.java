package org.wso2.carbon.scriptengine.engine;

public class JavaScriptHostObject {

    private String name = null;
    private Class clazz = null;

    public JavaScriptHostObject(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }
}
