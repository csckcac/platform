package org.wso2.carbon.jaggery.core.modules;


import org.mozilla.javascript.BaseFunction;

public class JaggeryHostObject {

    private String className = null;
    private String name = null;
    private BaseFunction constructor = null;

    public JaggeryHostObject(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BaseFunction getConstructor() {
        return constructor;
    }

    public void setConstructor(BaseFunction constructor) {
        this.constructor = constructor;
    }
}
