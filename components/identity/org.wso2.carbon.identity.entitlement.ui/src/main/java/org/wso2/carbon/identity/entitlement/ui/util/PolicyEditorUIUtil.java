/*
*  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.identity.entitlement.ui.util;

/**
 *
 */
public class PolicyEditorUIUtil {

    public static String[] getCategories(){
        return new String[] {"Resource/s", "Subject/s", "UserAttribute", "Action/s", "Environment/s"};
    }

    public static String[] getTargetPreFunctions(){
        return new String[] {"is-not", "is", "are-not", "are"};
    }

    public static String[] getTargetFunctions(){
        return new String[] {"=", "<", "regex", "are"};
    }

    public static String[] getPreFunctions(){
        return new String[] {"is-not", "is", "are-not", "are"};
    }

    public static String[] getFunctions(){
        return new String[] {"=", "<", "regex", "are"};
    }

    public static String[] getCombineFunctions(){
        return new String[] {"END", "AND", "OR"};        
    }


    public static String[] getUserAttributes(){
        return new String[] {"Email", "Age", "Address", "Role", "Group"};
    }


    
}
