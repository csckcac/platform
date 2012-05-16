
package org.wso2.carbon.registry.extensions.handlers.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServiceUtils {
    private static final List<String> modifiedList =
            Collections.synchronizedList(new ArrayList<String>());

    public static void addToModifiedList(String path){
            modifiedList.add(path) ;
    }

    public static List<String> getModifiedList(){
        return modifiedList;
    }
}
