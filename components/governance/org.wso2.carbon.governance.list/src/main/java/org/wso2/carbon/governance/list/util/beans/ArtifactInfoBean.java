package org.wso2.carbon.governance.list.util.beans;

public class ArtifactInfoBean {
    private String name;
    private String nameSpace;
    private String computedPath;
    private String version;
    private String lifecycleName;
    private String lifecycleState;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    public String getComputedPath() {
        return computedPath;
    }

    public void setComputedPath(String computedPath) {
        this.computedPath = computedPath;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getLifecycleName() {
        return lifecycleName;
    }

    public void setLifecycleName(String lifecycleName) {
        this.lifecycleName = lifecycleName;
    }

    public String getLifecycleState() {
        return lifecycleState;
    }

    public void setLifecycleState(String lifecycleState) {
        this.lifecycleState = lifecycleState;
    }
}
