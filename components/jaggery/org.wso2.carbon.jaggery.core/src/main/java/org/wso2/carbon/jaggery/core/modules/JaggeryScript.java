package org.wso2.carbon.jaggery.core.modules;

import org.mozilla.javascript.Script;

public class JaggeryScript {
    private String name = null;
    private String path = null;
    private Script script = null;

    public JaggeryScript(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Script getScript() {
        return script;
    }

    public void setScript(Script script) {
        this.script = script;
    }
}
