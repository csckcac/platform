package org.wso2.carbon.automation.core.utils.frameworkutils.productvariables;

public class EnvironmentSettings {

    private boolean _runningOnStratos;
    private boolean _enableDipFramework;
    private boolean _enableSelenium;
    private boolean _enableRavana;
    private boolean _enebleStratosPort;
    private boolean _enableWebContextRoot;
    private boolean _enablecluster;
    private boolean _enableBuilder;

    public boolean is_runningOnStratos() {
        return _runningOnStratos;
    }

    public boolean is_builderEnabled() {
        return _enableBuilder;
    }

    public boolean is_enableRavana() {
        return _enableRavana;
    }

    public boolean is_enableSelenium() {
        return _enableSelenium;
    }

    public boolean isEnableDipFramework() {
        return _enableDipFramework;
    }

    public boolean isEnablePort() {
        return _enebleStratosPort;
    }

    public boolean isEnableCarbonWebContext() {
        return _enableWebContextRoot;
    }

    public boolean isClusterEnable() {
        return _enablecluster;
    }

    public void setEnvironmentSettings(boolean enableDipFramework, boolean runningOnStratos,
                                       boolean enebleSelenium, boolean enableRavana,
                                       boolean enableStratosPort, boolean enableWebContextRoot,
                                       boolean enableCluster, boolean enableBuilder) {
        this._enableDipFramework = enableDipFramework;
        this._enableRavana = enableRavana;
        this._runningOnStratos = runningOnStratos;
        this._enableSelenium = enebleSelenium;
        this._enebleStratosPort = enableStratosPort;
        this._enableWebContextRoot = enableWebContextRoot;
        this._enablecluster = enableCluster;
        this._enableBuilder = enableBuilder;
    }
}
