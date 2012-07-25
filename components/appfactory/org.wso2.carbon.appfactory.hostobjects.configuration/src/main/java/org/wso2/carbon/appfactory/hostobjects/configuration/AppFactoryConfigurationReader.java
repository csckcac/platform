package org.wso2.carbon.appfactory.hostobjects.configuration;


import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;

/**
 * This is a host object used to get app factory configuration
 */
public class AppFactoryConfigurationReader extends ScriptableObject {
    private static final String hostObjectName = "AppFactoryConfigurationReader";
    private static AppFactoryConfiguration appFactoryConfiguration = AppFactoryConfigurationHolder
            .getInstance().getAppFactoryConfiguration();

    @Override
    public String getClassName() {
        return hostObjectName;
    }

    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj,
                                           boolean inNewExpr) throws Exception {
        return new AppFactoryConfigurationReader();
    }

    public String jsFunction_getFirstProperty(String key) throws Exception {
        return appFactoryConfiguration.getFirstProperty(key);
    }

    public NativeArray jsFunction_getProperties(String key) throws Exception {
        String[] values = appFactoryConfiguration.getPropertiesAsNativeArray(key);
        NativeArray array = new NativeArray(values.length);
        for (int i = 0; i < values.length; i++) {
            String element = values[i];
            array.put(i, array, element);
        }
        return array;
    }
}
