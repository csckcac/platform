package org.wso2.carbon.hostobjects.file;


import org.wso2.carbon.scriptengine.exceptions.ScriptException;

import java.io.File;

public interface JavaScriptFileManager {

    public JavaScriptFile getJavaScriptFile(Object object) throws ScriptException;

    public File getFile(String path) throws ScriptException;

}
