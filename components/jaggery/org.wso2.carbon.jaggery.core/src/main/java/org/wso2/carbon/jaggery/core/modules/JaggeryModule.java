package org.wso2.carbon.jaggery.core.modules;

import java.util.ArrayList;
import java.util.List;

public class JaggeryModule {

    private String name = null;
    private String namespace = null;
    private boolean expose = false;

    private final List<JaggeryHostObject> jaggeryHostObjects = new ArrayList<JaggeryHostObject>();
    private final List<JaggeryMethod> jaggeryMethods = new ArrayList<JaggeryMethod>();
    private final List<JaggeryScript> jaggeryScripts = new ArrayList<JaggeryScript>();

    public JaggeryModule(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public boolean isExpose() {
        return expose;
    }

    public void setExpose(boolean expose) {
        this.expose = expose;
    }

    public void addHostObject(JaggeryHostObject jaggeryHostObject) {
        this.jaggeryHostObjects.add(jaggeryHostObject);
    }

    public void addMethod(JaggeryMethod jaggeryMethod) {
        this.jaggeryMethods.add(jaggeryMethod);
    }

    public void addScript(JaggeryScript jaggeryScript) {
        this.jaggeryScripts.add(jaggeryScript);
    }

    public List<JaggeryHostObject> getJaggeryHostObjects() {
        return this.jaggeryHostObjects;
    }

    public List<JaggeryMethod> getJaggeryMethods() {
        return this.jaggeryMethods;
    }

    public List<JaggeryScript> getJaggeryScripts() {
        return this.jaggeryScripts;
    }
}
