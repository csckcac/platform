package org.wso2.carbon.jaggery.core.modules;

import java.lang.reflect.Method;

public class JaggeryMethod {

    private String name = null;
    private String className = null;
    private Method function = null;

    public JaggeryMethod(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Method getMethod() {
        return function;
    }

    public void setMethod(Method function) {
        this.function = function;
    }
}
