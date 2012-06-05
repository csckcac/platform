package org.wso2.carbon.scriptengine.engine;

import org.mozilla.javascript.ClassShutter;

public class CarbonClassShutter implements ClassShutter {
    @Override
    public boolean visibleToScripts(String s) {
        return false;
    }
}
