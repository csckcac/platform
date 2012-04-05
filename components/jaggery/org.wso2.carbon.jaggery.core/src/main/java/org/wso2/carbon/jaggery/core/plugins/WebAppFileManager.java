package org.wso2.carbon.jaggery.core.plugins;

import org.wso2.carbon.hostobjects.file.JavaScriptFile;
import org.wso2.carbon.hostobjects.file.JavaScriptFileManager;
import org.wso2.carbon.hostobjects.file.JavaScriptFileManagerImpl;
import org.wso2.carbon.scriptengine.exceptions.ScriptException;

import javax.servlet.ServletContext;

public class WebAppFileManager implements JavaScriptFileManager {

    private ServletContext context;

    public WebAppFileManager(ServletContext context) {
        this.context = context;
    }

    @Override
    public JavaScriptFile getFile(String path) throws ScriptException {
        if(path.startsWith("file://")) {
            return new JavaScriptFileManagerImpl().getFile(path);
        }
        return new WebAppFile(path, context);
    }
}
