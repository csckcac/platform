package org.wso2.carbon.hostobjects.file;


import org.wso2.carbon.scriptengine.exceptions.ScriptException;

public interface JavaScriptFileManager {

    public JavaScriptFile getFile(String path) throws ScriptException;

}
