package org.wso2.carbon.scriptengine.engine;

import org.mozilla.javascript.ClassShutter;

public class CarbonClassShutter implements ClassShutter {
    @Override
    public boolean visibleToScripts(String className) {
        if(className.startsWith("org.mozilla.javascript.")) {
            return true;
        }
        return false;
    }
}
